#!/bin/bash

ELASTICSEARCH_URL="http://localhost:9200"
INDEX="companii"
curl -XDELETE "$ELASTICSEARCH_URL/$INDEX"
