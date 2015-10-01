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

bin=${pwd}/../../../bin
lib=${pwd}/../../../lib

java="java"

echo '
{
    "path" : "/Users/joerg/import/zdb",
    "pattern" : "1509*lok*mrc.gz",
    "concurrency" : 1,
    "pipelines" : 8,
    "identifier" : "DE-600",
    "collection" : "Zeitschriften",
    "elasticsearch" : {
        "cluster" : "zbn-1.5",
        "host" : "zephyros",
        "port" : 19300,
        "autodiscover" : true
    },
    "index" : "zdbholdingsdirect",
    "type" : "zdbholdingsdirect",
    "maxbulkactions" : 2000,
    "maxconcurrentbulkrequests" : 8,
    "mock" : false,
    "detect-unknown" : true,
    "client" : "bulk",
    "timewindow" : "yyyyMMddHH",
    "aliases" : true,
    "ignoreindexcreationerror" : true
}
' | ${java} \
    -cp ${lib}/\*:${bin}/\* \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.feed.elasticsearch.zdb.hol.DirectHol
