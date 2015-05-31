#!/bin/bash

java \
    -cp bin:lib/tools-1.0-SNAPSHOT-content.jar \
    org.xbib.tools.convert.OpenLibrary \
    --input "src/test/resources/openlibrary-couchdb-export-sample.txt" \
    --output "openlibrary.ttl"
