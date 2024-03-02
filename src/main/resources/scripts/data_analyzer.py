import matplotlib.pyplot as plt

path_to_save_statistics_files = './statistics/'
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

    categories = ['Phone Numbers', 'Social Media Links', 'Locations']
    plt.bar(categories, [phone_numbers_found, social_media_links_found, locations_found], color=['purple', 'green', 'orange'])
    plt.ylabel('Number of Data Points')
    plt.title(f'Datapoints Extracted from {crawled_websites} Websites')
    plt.savefig(path_to_save_statistics_files + f'datapoints_extracted.{timestamp}.jpg')
    plt.close()

    # --------- datapoints_on_each_website ---------

    categories = ['has phone number', 'has Social Links', 'has Locations']
    plt.bar(categories, [websites_with_phone_numbers, websites_with_social_media_links, websites_with_locations],color=['yellow', 'blue', 'darkgrey'])
    plt.ylabel('datapoints per website')
    plt.title(f'Out of  {crawled_websites} Websites')
    plt.savefig(path_to_save_statistics_files + f'datapoints_on_each_website.{timestamp}.jpg')
    plt.close()


file_timestamp = extract_data_from_statistics_file('statistics.txt')
plot_analyzed_data(file_timestamp)
print(file_timestamp)

    

