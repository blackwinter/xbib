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
    "path" : "'${HOME}'/import/dnb/gnd/20150227/",
    "pattern" : "GND.rdf.gz",
    "elasticsearch" : {
        "cluster" : "zbn-1.5",
        "host" : "zephyros",
        "port" : 19300
    },
    "index" : "gnd2",
    "index-settings" : "classpath:org/xbib/tools/feed/elasticsearch/dnb/gnd/settings.json",
    "type" : "gnd",
    "type-mapping" : "classpath:org/xbib/tools/feed/elasticsearch/dnb/gnd/mapping.json",
    "maxbulkactions" : 10000,
    "maxconcurrentbulkrequests" : 8,
    "client" : "ingest",
    "mock" : false
}
' | ${java} \
    -cp ${lib}/\*:${bin}/\* \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.feed.elasticsearch.dnb.gnd.RdfXml
