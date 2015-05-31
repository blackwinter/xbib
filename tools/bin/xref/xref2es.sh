#!/bin/bash

# cron?
tty -s
if [ "$?" -gt "0" ]
then
    cd $HOME/xbib-tools
    pwd=$(pwd)
    bin=${pwd}/bin
    lib=${pwd}/lib
else
    pwd="$( cd -P "$( dirname "$0" )" && pwd )"
    bin=${pwd}/../../bin
    lib=${pwd}/../../lib
fi

java="java"

echo '
{
    "path" : "/Users/joerg/Desktop",
    "pattern" : "*.json",
    "serials" : "titleFile.csv",
    "concurrency" : 4,
    "pipelines" : 8,
    "elasticsearch" : {
        "cluster" : "zbn-1.5",
        "host" : "zephyros",
        "port" : 19300
    },
    "index" : "xref",
    "index-settings" : "classpath:org/xbib/tools/feed/elasticsearch/articles/settings.json",
    "type" : "xref",
    "type-mapping" : "classpath:org/xbib/tools/feed/elasticsearch/articles/mapping.json",
    "maxbulkactions" : 10000,
    "maxconcurrentbulkrequests" : 8,
    "mock" : true,
    "client" : "ingest"
}
' | ${java} \
    -cp ${lib}/\*:${bin}/\* \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.feed.elasticsearch.articles.JsonCoins