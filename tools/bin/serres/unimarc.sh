#!/bin/bash

# cron?
tty -s
if [ "$?" -gt "0" ]
then
    cd $HOME/hbz-toolbox
    pwd=$(pwd)/bin
else
    pwd="$( cd -P "$( dirname "$0" )" && pwd )"
fi

bin=${pwd}/../../bin
lib=${pwd}/../../lib

java="java"

echo '
{
    "path" : "/Users/joerg/import/serres",
    "pattern" : "*mrc",
    "concurrency" : 1,
    "pipelines" : 8,
    "identifier" : "SERRES",
    "collection" : "Unimarc",
    "elasticsearch" : {
        "cluster" : "joerg",
        "host" : "localhost",
        "port" : 9300,
        "sniff" : false
    },
    "index" : "serres",
    "type" : "serres",
    "index-settings" : "classpath:org/xbib/tools/feed/elasticsearch/serres/settings.json",
    "type-mapping" : "classpath:org/xbib/tools/feed/elasticsearch/serres/mapping.json",
    "maxbulkactions" : 1000,
    "maxconcurrentbulkrequests" : 8,
    "mock" : false,
    "client" : "bulk"
}
' | ${java} \
    -cp ${lib}/\*:${bin}/\* \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.feed.elasticsearch.serres.Unimarc
