import argparse
import csv
from datetime import datetime
import re
import urllib.parse
import os
import threading
import requests
from bs4 import BeautifulSoup
from requests.exceptions import RequestException, SSLError
import time
import subprocess


file_write_lock = threading.Lock()
# used to store specific information for each thread (such as scraping statistics)
local_thread_instance = threading.local()
file_with_comments = './comments_for_stats_file.txt'
stats_file = './statistics.txt'
def init_local_thread_instance():
    # values used for data analysis
    local_thread_instance.total_number_of_websites = 0
    local_thread_instance.unsuccessfully_scraped_websites = 0
    local_thread_instance.phone_numbers_found = 0
    local_thread_instance.social_media_links_found = 0
    local_thread_instance.locations_found = 0
    local_thread_instance.website_with_social_media_link = 0
    local_thread_instance.website_with_phone_number = 0
    local_thread_instance.website_with_location = 0


def add_stats_to_statistics_file_synchronous(thread_number):
    with open(stats_file, 'a') as file:
        file_write_lock.acquire()
        try:
            file.write(f'----------- Thread {thread_number} -----------\n')
            file.write(str(local_thread_instance.total_number_of_websites) + '\n')
            file.write(str(local_thread_instance.unsuccessfully_scraped_websites) + '\n')
            file.write(str(local_thread_instance.phone_numbers_found) + '\n')
            file.write(str(local_thread_instance.social_media_links_found) + '\n')
            file.write(str(local_thread_instance.locations_found) + '\n')
            file.write(str(local_thread_instance.website_with_phone_number) + '\n')
            file.write(str(local_thread_instance.website_with_social_media_link) + '\n')
            file.write(str(local_thread_instance.website_with_location) + '\n')
        finally:
            # Release the lock after writing to the file
            file_write_lock.release()

def generate_empty_stats_file():
    with open(file_with_comments, 'r') as source_file:
        file_content = source_file.read()
    with open(stats_file, 'w') as destination_file:
        destination_file.write(file_content)

def prepare_environment(social_media_domains_file_path):
    global social_media_platforms
    global phone_regex
    global location_regex
    global api_key
    generate_empty_stats_file()
    api_key = os.environ['MAPS_API_KEY']
    social_media_domains_file = open(social_media_domains_file_path)
    social_media_platforms = [platform.strip() for platform in social_media_domains_file]
    social_media_domains_file.close()
    phone_regex = re.compile(
        r'''((\b\+?\d{10}\b)|(\(\d{3}\)\s\d{3}.\d{4})|(\d{3}\.\d{3}\.\d{4})|(\d{3}-\d{3}-\d{4})|(\+\d{1,4}-\d{1,4}-\d{1,10})|(\(\+\d{1,4}\) \d{3}-\d{4})|(\(\d{3}\)\d{7}))''',
        re.VERBOSE)
    location_regex = re.compile(
        r'''\?q=(.*?)&''',
        re.VERBOSE
    )


def scrape_media_links(hrefs):
    return set(href for href in hrefs if
               any(href.startswith(platform) for platform in social_media_platforms))



def scrape_phone_numbers(page_soup):
    text = page_soup.getText(strip=True)
    return set(matches[0] for matches in phone_regex.findall(text))

def call_google_maps_api(page_url):
    lat_lng = page_url.split('?ll=')[1].split('&')[0].split(',')
    latitude, longitude = lat_lng[0], lat_lng[1]
    api_url = f'https://maps.googleapis.com/maps/api/geocode/json?latlng={latitude},{longitude}&key={api_key}'
    response = requests.get(api_url)
    data = response.json()
    if data['status'] == 'OK' and 'results' in data and len(data['results']) > 0:
        return data['results'][0]['formatted_address']


def scrape_location(hrefs):
    locations_set = set()
    for url in hrefs:
        if url.startswith('https://maps.google.com'):
            # case 1: the url has the coordinates of the location
            if '?ll=' in url:
                continue
                location = call_google_maps_api(url)
                if location:
                    locations_set.add(location)
            # case 2: the url contains the exact location (url_encoded)
            match = re.search(location_regex , url)
            if match:
                address = match.group(1).replace("+", " ")
                decoded_address = urllib.parse.unquote(address)
                locations_set.add(decoded_address)

    return locations_set





def get_page_data(domain):
    try:
        page_data = requests.get('https://' + domain, timeout=3)
    except SSLError:
        # try again without tls encryption
        page_data = requests.get('http://' + domain, timeout=3)
    return page_data




def scrape_websites_from_csv_chunk(csv_chunk):
    for row in csv_chunk:
        local_thread_instance.total_number_of_websites += 1
        domain = row[0]
        print(domain)
        try:
            page_data = get_page_data(domain)
        except Exception as e:
            local_thread_instance.unsuccessfully_scraped_websites += 1
            if verbose:
                print(f"Error fetching page '{domain}', cause:  {e}")
            continue
        page_soup = BeautifulSoup(page_data.content, 'html.parser')
        hrefs = [a['href'] for a in page_soup.find_all('a', href=True)]
        phone_numbers_set = scrape_phone_numbers(page_soup)
        media_links_set = scrape_media_links(hrefs)
        locations_set = scrape_location(hrefs)
        # for data analysis part
        if phone_numbers_set:
            local_thread_instance.website_with_phone_number += 1
            local_thread_instance.phone_numbers_found += len(phone_numbers_set)
        if media_links_set:
            local_thread_instance.website_with_social_media_link += 1
            local_thread_instance.social_media_links_found += len(media_links_set)
        if locations_set:
            local_thread_instance.website_with_location += 1
            local_thread_instance.locations_found += len(locations_set)
        # print(phone_numbers_set)
        # print(media_links_set)
        # print(locations_set)

def start_threads(no_of_threads, csv_file):
    with open(csv_file, 'r') as csvfile:
        csv_reader = csv.reader(csvfile)
        # Skip header
        next(csv_reader)
        total_rows = sum(1 for _ in csvfile)
        # make sure the threads are processing similar amount of data
        chunk_size = total_rows // no_of_threads
        # make sure all rows will be processed
        remaining_rows = total_rows % no_of_threads
        threads = []
        # read again the csv ( it was consumed after the domains count)
        with open(csv_file, 'r') as csvfile:
            csv_reader = csv.reader(csvfile)
            # Skip header
            next(csv_reader)
            for i in range(no_of_threads - 1):
                chunk = [next(csv_reader) for _ in range(chunk_size)]
                thread = threading.Thread(target=worker_function, args=(chunk, i))
                threads.append(thread)
                thread.start()

            # Last thread should process all rows his chunk + remaining rows
            last_chunk = [next(csv_reader) for _ in range(chunk_size + remaining_rows)]
            thread = threading.Thread(target=worker_function, args=(last_chunk, no_of_threads - 1))
            threads.append(thread)
            thread.start()
            # wait for all threads to finish
            for thread in threads:
                thread.join()


def worker_function(csv_chunk, thread_number):
    init_local_thread_instance()
    scrape_websites_from_csv_chunk(csv_chunk)
    add_stats_to_statistics_file_synchronous(thread_number)
    return

def add_timestamp():
    with open(stats_file, 'r') as file:
        lines = file.readlines()

    # Găsește a doua linie care nu începe cu #
    for index, line in enumerate(lines):
        if not line.startswith('#'):
            lines.insert(index + 1, str(datetime.now().strftime("%d.%m.%Y-%H.%M.%S")) + '\n')
            break

    with open(stats_file, 'w') as file:
        file.writelines(lines)


if __name__ == '__main__':
    start_time = time.time()
    if True:
        global verbose
        global total_number_of_websites
        global unsuccessfuly_scraped_websites
        global phone_numbers_found
        global social_media_links_found
        global locations_found
        parser = argparse.ArgumentParser(description='Scrape data from a CSV file.')
        parser.add_argument('-f', '--file', required=True, help='Path to the CSV file')
        parser.add_argument('-s', '--social', required=True, help='Path to the targeted social media domains file')
        parser.add_argument('-v', '--verbose', action='store_true', help='Print verbose error messages')
        parser.add_argument('-t', '--threads', required=True, help='Number of threads for scraping')
        args = parser.parse_args()
        verbose = args.verbose
        prepare_environment(args.social)
        start_threads(int(args.threads), args.file)
        print("A durat " + str((time.time() - start_time) // 60) + "minute")
        # add timestamp to statistics file to be processed by the data_analyzer.py script
        add_timestamp()
        # run data analysis on statistics file
        subprocess.run(['python', 'data_analyzer.py'])

    else:
        prepare_environment('targeted_social_media_domains.txt')
        start_threads(10, 'sample-websites.csv')
    # write file with extracted data