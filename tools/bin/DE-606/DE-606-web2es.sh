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
    "uri" : "http://rzbvm016.ur.de/flr/by_zdbid/",
    "concurrency" : 1,
    "pipelines" : 1,
    "identifier" : "DE-606",
    "collection" : "Elektronische Zeitschriften",
    "elasticsearch" : {
        "cluster" : "joerg",
        "host" : "localhost",
        "port" : 9300
    },
    "index" : "de-606-web",
    "type" : "de-606-web",
    "maxbulkactions" : 3000,
    "maxconcurrentbulkrequests" : 8,
    "mock" : false,
    "timewindow" : "yyyyMMddHH",
    "aliases" : true,
    "ignoreindexcreationerror" : true
}
' | ${java} \
    -cp ${lib}/\*:${bin}/\* \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.feed.elasticsearch.ezb.EZBWeb
