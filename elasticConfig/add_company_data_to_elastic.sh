#!/bin/bash

ELASTICSEARCH_URL="http://localhost:9200"
INDEX="companii"
TYPE="_doc"
CSV_FILE="sample-websites-company-names.csv"

if [ ! -f "$CSV_FILE" ]; then
    echo "CSV File not found"
    exit 1
fi

# Iterați prin fiecare linie a fișierului CSV
tail -n +2 "$CSV_FILE" | tr -d '\r' | while IFS=, read -r domain company_commercial_name company_legal_name company_all_available_names; do
    # convert company_all_available_names to array
    IFS='|' read -ra names_array <<< "$company_all_available_names"

    # convert array to json
    names_json=$(printf '"%s",' "${names_array[@]}")
    names_json="[${names_json%,}]"

    # add document to json
    echo '{"index":{"_index":"'"$INDEX"'","_type":"'"$TYPE"'","_id":"'"$domain"'"}}' >> data.json
    echo '{"domain":"'"$domain"'","company_commercial_name":"'"$company_commercial_name"'","company_legal_name":"'"$company_legal_name"'","company_all_available_names":'"$names_json"',"phoneNumbers":[],"socialMediaLinks":[],"locations":[]}' >> data.json
done

# update elasticsearch instance
curl -XPOST "$ELASTICSEARCH_URL/$INDEX/$TYPE/_bulk" -H 'Content-Type: application/json' --data-binary "@data.json"

# remove temporary file
rm -f data.json
