import shutil

import matplotlib.pyplot as plt
import os
import sys

scraping_time = float(sys.argv[1])
script_directory = os.path.dirname(os.path.abspath(__file__))
path_to_save_statistics_files = f'{script_directory}/statistics/'
statistics_file_path = f'{script_directory}/statistics.txt'
total_websites = 0
unable_to_crawl = 0
phone_numbers_found = 0
social_media_links_found = 0
locations_found = 0
websites_with_phone_numbers = 0
websites_with_social_media_links = 0
websites_with_locations = 0


def update_global_statistics(stats_list):
    global total_websites
    global unable_to_crawl
    global phone_numbers_found
    global social_media_links_found
    global locations_found
    global websites_with_phone_numbers
    global websites_with_social_media_links
    global websites_with_locations
    total_websites += stats_list[0]
    unable_to_crawl += stats_list[1]
    phone_numbers_found += stats_list[2]
    social_media_links_found += stats_list[3]
    locations_found += stats_list[4]
    websites_with_phone_numbers += stats_list[5]
    websites_with_social_media_links += stats_list[6]
    websites_with_locations += stats_list[7]

def mark_file_as_processed(processed_file, timestamp):
    # add scraping time to file
    file = open(statistics_file_path, 'a')
    file.write(f'\nCsv file was scraped in {int(scraping_time//60)} minutes and {scraping_time%60} seconds')
    file.close()
    new_file_name = f"statistics.{timestamp}.processed"
    original_directory = os.path.dirname(processed_file)
    new_file_path = os.path.join(original_directory, new_file_name)
    shutil.move(processed_file, new_file_path)

def extract_data_from_statistics_file(statistics_file_path):
    file = open(statistics_file_path, 'r')
    # jump over the comments
    current_line = file.readline()
    while current_line[0] == '#':
        current_line = file.readline()
    # get_timestamp
    timestamp = file.readline().strip()
    # jump over the empty line and thread name
    for i in range(2):
        file.readline()
    while True:
        # jump over the thread name
        current_line = file.readline()
    # check end of file
        if not current_line:
            return timestamp
        stats_from_current_thread = []
        # read all stats from current thread
        for _ in range (8):
            stats_from_current_thread.append(int(current_line.strip()))
            current_line = file.readline()
        update_global_statistics(stats_from_current_thread)
    file.close()


def plot_analyzed_data(timestamp):
    # --------- crawl_coverage_file ---------
    crawled_websites = total_websites - unable_to_crawl
    labels = [f'{unable_to_crawl} failed', f'{crawled_websites} succeeded']
    sizes = [unable_to_crawl, crawled_websites]
    explode = (0, 0.1)
    plt.pie(sizes, explode=explode, labels=labels, autopct='%1.1f%%', startangle=140)
    plt.axis('equal')
    plt.title(f'Tried to crawl {total_websites} websites')
    plt.savefig(path_to_save_statistics_files + f'crawl_coverage_file.{timestamp}.jpg')
    plt.close()

    # --------- datapoints_extracted ---------

    categories = [f'{phone_numbers_found} Phone Numbers', f'{social_media_links_found} Social Media Links', f'{locations_found} Locations']
    plt.bar(categories, [phone_numbers_found, social_media_links_found, locations_found], color=['purple', 'green', 'orange'])
    plt.ylabel('Number of Data Points')
    plt.title(f'Datapoints Extracted from {crawled_websites} Websites')
    plt.savefig(path_to_save_statistics_files + f'datapoints_extracted.{timestamp}.jpg')
    plt.close()

    # --------- datapoints_on_each_website ---------

    categories = [f'{websites_with_phone_numbers} have phone number', f' {websites_with_social_media_links} have Social Links', f'{websites_with_locations} have Locations']
    plt.bar(categories, [websites_with_phone_numbers, websites_with_social_media_links, websites_with_locations],color=['yellow', 'blue', 'darkgrey'])
    plt.ylabel('datapoints per website')
    plt.title(f'Out of  {crawled_websites} Websites')
    plt.savefig(path_to_save_statistics_files + f'datapoints_on_each_website.{timestamp}.jpg')
    plt.close()


file_timestamp = extract_data_from_statistics_file(statistics_file_path)
plot_analyzed_data(file_timestamp)
mark_file_as_processed(statistics_file_path, file_timestamp)

    

