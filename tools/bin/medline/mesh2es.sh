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
    "path" : "'${HOME}'/import/mesh/",
    "pattern" : "MeSH-2015.xml",
    "concurrency" : 1,
    "pipelines" : 1,
    "elasticsearch" : {
        "cluster" : "zbn-1.5",
        "host" : "zephyros",
        "port" : 19300
    },
    "index" : "mesh",
    "index-settings" : "classpath:org/xbib/tools/feed/elasticsearch/mesh/settings.json",
    "type" : "mesh",
    "type-mapping" : "classpath:org/xbib/tools/feed/elasticsearch/mesh/mapping.json",
    "maxbulkactions" : 1000,
    "maxconcurrentbulkrequests" : 1,
    "mock" : true,
    "client" : "ingest"
}
' | ${java} \
    -cp ${lib}/\*:${bin}/\* \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.feed.elasticsearch.medline.Mesh