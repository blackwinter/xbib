#!/bin/bash

# cron?
tty -s
if [ "$?" -gt "0" ]
then
    cd $HOME/xbib-tools
    pwd=$(pwd)/bin
else
    pwd="$( cd -P "$( dirname "$0" )" && pwd )"
fi

bin=${pwd}/../../bin
lib=${pwd}/../../lib

echo '
{
    "path" : "/Users/joerg/import/ftp.gfz-potsdam.de/pub/incoming/bib/lucene/nationallizenzen/netlibrary",
    "pattern" : "*.xml",
    "elements" : "/org/xbib/analyzer/mab/titel.json",
    "catalogid" : "Nationallizenzen",
    "collection" : "Nationallizenzen",
    "concurrency" : 1,
    "pipelines" : 4,
    "elasticsearch" : {
        "cluster" : "joerg",
        "host" : "localhost",
        "port" : 9300
    },
    "index" : "nlz",
    "type" : "nlz",
    "maxbulkactions" : 5000,
    "maxconcurrentbulkrequests" : 5,
    "mock" : true,
    "detect-unknown" : true,
    "client" : "bulk"
}
' | java \
    -cp ${lib}/\*:${bin}/\* \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.feed.elasticsearch.natliz.NatLiz
