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
    "path" : "'${HOME}'/import/ftp.nlm.nih.gov/nlmdata/",
    "pattern" : "*.xml.gz",
    "concurrency" : 4,
    "pipelines" : 8,
    "elasticsearch" : {
        "cluster" : "zbn-1.5",
        "host" : "zephyros",
        "port" : 19300
    },
    "index" : "medline",
    "index-settings" : "classpath:org/xbib/tools/feed/elasticsearch/medline/settings.json",
    "type" : "medline",
    "type-mapping" : "classpath:org/xbib/tools/feed/elasticsearch/medline/mapping.json",
    "maxbulkactions" : 10000,
    "maxconcurrentbulkrequests" : 1,
    "mock" : false,
    "client" : "ingest"
}
' | ${java} \
    -cp ${lib}/\*:${bin}/\* \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.feed.elasticsearch.medline.Medline