# Overview: 
This projects achieves the following:
1. Scrapes domains in a multithreading fashion for preconfigured datapoints (phone numbers, social media links, locations)
2. Analyzes scraped data and creates useful diagrams
3. Aggregates scraped data with existing one
4. Exposes an API for starting asynchronous processing of CSV domain files and retrieving best matching company on multiple datapoints input 
# Tech Stack used 
 - Python -> scraping domains + generate analysis
 - Java Spring Boot -> API handling bestMatching (GET) + async handling of CSV files to be processed by the python scripts (POST)
 - ElasticSearch -> storing and returning requested data
# Logic Overview 
![Logic](readmeImages/logic.png)
# Scraping part + analysis part (scraping_dir)
Location: **scraping_dir/scrapingScript.py** 

The script can be run both as standalone or as part of the POST api workflow (controlled by the application.properties values)

Parameter | Description                                                                                                 | required | application.properties |
---------- |-------------------------------------------------------------------------------------------------------------|----------|-----------|
-f / --file    | Path to CSV file to be processed                                                                            | yes      | file.processing.directory | 
-t / --threads    | Number of threads to process the file                                                                       | yes      | python.scraping.threads | 
-v/ --verbose | Should error logs be displayed                                                                              | no       | no        |
-m / --maps    | Specifies if the MAPS_API_KEY variable is set<br/>so the Google Maps APi can be used for location scraping  | yes      | maps.key |


## How does the scraping script work 

1. Each thread receives an equal chunk of data to process (the last thread receives everything that is left)
2. Threads start to process -> storing data in local thread variables
3. Parent process generates two files *statistics.txt* (current dir) and *still_processing.txt* (extracted data dir)
4. When each thread finishes scraping, it acquires lock on those files and writes to them 
5. After threads finish, parent process computes running time and writes it along with the timestamp to the statistics.txt file 
6. Marks the data file as "ready to be processed" -> for the java thread
7. Runs analyze script on statistics file

Snippet of statistics file 

![Stats](readmeImages/statistics.png)

Snippet of extracted data file

![Data](readmeImages/data.png)

Note: see more in **example** directory

## How is data scraped

- for phone numbers we have a strong matching regex ((\b\+?\d{10}\b)|(\(\d{3}\)\s\d{3}.\d{4})|(\d{3}\.\d{3}\.\d{4})|(\d{3}-\d{3}-\d{4})|(\+\d{1,4}-\d{1,4}-\d{1,10})|(\(\+\d{1,4}\) \d{3}-\d{4})|(\(\d{3}\)\d{7}))
- for social media links, we compare the hrefs elements with the targeted domains in targeted_social_media_domains.txt file
- for locations we indentify the google maps link and get the location from url or via Google Maps API

## How does the analyzer script work
1. Analyzes data based on statistics txt file 
2. Adds the total runtime of scraping script at the end of it
3. Computes graphical representation of extracted data in **statistics** folder
4. Marks files as processed by adding timestamps to them

# Data aggregation part

1. Data from sample-websites-company-name.csv is added to elasticSearch by **add_company_data_to_elastic.sh** script (along with a little formatting)
2. Documents are being updated by the content of the data file

# The querying part

1. API receives a GET request containing one or more datapoints
2. Tries to get the best match company from elastic by using levenshtein distance's logic
3. If no data is found, keeps increasing the levenshtein distance until the **levenshtein.max.distance** is reached
4. Returns the company data/ Not found response 

# Full workflow overview

1. After spring context is initialized, 2 threads are starting to read certain folders (controlled by processing.threads.sleep.interval) 
2. A csv is sent to API via post request and an acknowledge message is returned 
3. After validation, the csv is placed in **csv_input** folder
4. The first thread runs python process and marks file as processed
5. After scraping is done, the data file is placed in **extracted_data** directory
6. The second thread updates documents on elastic and marks file as processed

# How to run the project

clone it locally 
```
git clone https://github.com/radumocanu1/SearchAPI.git
```
navigate to elasticConfig
```
cd CompanyApi/elasticConfig
```
create docker network for elasticsearch and kibana
```
docker network create customnetwork
```
start elasticsearch container
```
docker run -d -p 9200:9200 -p 9300:9300 --name elasticsearch --network customnetwork -e "discovery.type=single-node" elasticsearch:7.14.0

```
start kibana container
```
docker run -d -p 5601:5601  --network customnetwork -v kibana.yaml:/usr/share/kibana/config kibana:7.14.0
```
Note: you should see kibana UI on http://localhost:5601/ 

open git bash, navigate to elasticConfig dir and run add data to elastic script
```
bash add_company_data_to_elastic.sh
```

Choose the desired parameters in application.properties

Run spring app
```
mvn spring-boot:run
```
POST requests -> localhost:8080/company/upload

![img_1.png](readmeImages/img_1.png)

GET requests -> localhost:8080/company 

![img.png](readmeImages/img.png)



# When are the datapoints updated in elasticsearch?
1. when the file in **scraping_dir/extracted_data** is marked as processed with the current timestamp
2. when the processing thread logs: Finished adding datapoints to elastic...
3. you can check on kibana 

Note: Depending on your system + number of threads set, it should take around 3 minutes

## Requirements 

- jdk 17
- mvn 
- python3
- python libraries (can be installed with pip) matplotlib, requests, bs4, urllib, csv, 
- docker
