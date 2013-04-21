#!/bin/bash

# Testdaten Bielefeld/Köln: HBZ_update_dump201250001.xml

# Format-Dokumenation
# http://www.zeitschriftendatenbank.de/fileadmin/user_upload/ZDB/pdf/services/Datenlieferdienst_ZDB_EZB_Lizenzdatenformat.pdf

java \
    -cp bin:lib/xbib-search-tools-1.0-SNAPSHOT-elasticsearch.jar \
    org.xbib.tools.indexer.elasticsearch.EZB \
    --elasticsearch "es://hostname:9300?es.cluster.name=joerg" \
    --threads 1 \
    --maxbulkactions 1000 \
    --index "ezb" \
    --type "licenses" \
    --path "$HOME/Daten/EZB/" \
    --pattern "HBZ_update_dump201250001.xml"

