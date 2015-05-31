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

echo '
{
    "path" : "'${HOME}'/import/viaf",
    "pattern" : "viaf-20150416-clusters-rdf.xml.gz",
    "elasticsearch" : {
        "cluster" : "zbn-1.5",
        "host" : "zephyros",
        "port" : 19300
    },
    "index" : "viaf",
    "type" : "viaf",
    "index-settings" : "classpath:org/xbib/tools/feed/elasticsearch/viaf/settings.json",
    "type-mapping" : "classpath:org/xbib/tools/feed/elasticsearch/viaf/mapping.json",
    "maxbulkactions" : 3000,
    "maxconcurrentbulkrequests" : 10,
    "client" : "ingest",
    "mock" : false
}
' | java \
    -cp ${lib}/\*:${bin}/\* \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.feed.elasticsearch.viaf.VIAF
