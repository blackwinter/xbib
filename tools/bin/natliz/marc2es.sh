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
    "path" : "/Users/joerg/import/ZDB-1-ALD",
    "pattern" : "*.mrc",
    "elements" : "/org/xbib/analyzer/marc/nlz/bib.json",
    "catalogid" : "ZDB-1-ALD",
    "collection" : "ZDB-1-ALD",
    "concurrency" : 1,
    "pipelines" : 4,
    "elasticsearch" : {
        "cluster" : "joerg",
        "host" : "localhost",
        "port" : 9300
    },
    "index" : "zdb-1-ald",
    "type" : "zbd-1-ald",
    "maxbulkactions" : 5000,
    "maxconcurrentbulkrequests" : 5,
    "mock" : true,
    "detect-unknown" : true
}
' | java \
    -cp ${lib}/\*:${bin}/\* \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.feed.elasticsearch.natliz.NatLizMarc
