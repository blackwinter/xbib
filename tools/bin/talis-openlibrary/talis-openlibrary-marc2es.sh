#!/bin/sh

# cron?
tty -s
if [ "$?" -gt "0" ]
then
    # cron
    cd $HOME/xbib
    pwd=$(pwd)
    bin=${pwd}/bin
    lib=${pwd}/lib
else
    pwd="$( cd -P "$( dirname "$0" )" && pwd )"
    bin=${pwd}/../../../bin
    lib=${pwd}/../../../lib
fi

java="java"

echo '
{
    "path" : "'${HOME}'/import/talis-openlibrary/",
    "pattern" : "*.mrc.gz",
    "elements" : "/org/xbib/analyzer/marc/bib.json",
    "concurrency" : 1,
    "pipelines" : 16,
    "elasticsearch" : {
        "cluster" : "elasticsearch",
        "host" : "localhost",
        "port" : 9300,
        "sniff" : false
    },
    "index" : "talismarc",
    "type" : "title",
    "maxbulkactions" : 10000,
    "maxconcurrentbulkrequests" : 16,
    "mock" : false,
    "detect-unknown" : true,
    "client" : "ingest"
}
' | ${java} \
     -cp $(pwd)/bin:$(pwd)/bin/\*:$(pwd)/lib/tools-1.0.0.Beta7-standalone.jar \
     -Dlog4j.configurationFile=$(pwd)/bin/log4j2.xml \
     org.xbib.tools.Runner org.xbib.tools.feed.elasticsearch.marc.FromMARC
