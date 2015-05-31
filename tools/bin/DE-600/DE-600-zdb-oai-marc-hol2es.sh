#!/bin/sh

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

#set=zdb
set="zdb:holdings"

d=`gdate +%Y-%m-%d`
p1=`gdate -d "${d} - 1 days" +"%Y-%m-%dT%H:%M:%SZ"`
p2=`gdate -d "${d}" +"%Y-%m-%dT%H:%M:%SZ"`

echo '
  {
    "uri" : [
        "http://services.dnb.de/oai/repository?verb=ListRecords&metadataPrefix=MARC21-xml&set='${set}'&from='${p1}'&until='${p2}'"
    ],
    "count" : 2,
    "elements" : "/org/xbib/analyzer/marc/zdb/hol.json",
    "package" : "org.xbib.analyzer.marc.zdb.hol",
    "concurrency" : 1,
    "pipelines" : 1,
    "identifier" : "DE-600",
    "collection" : "Zeitschriftennachweise",
    "elasticsearch" : {
        "cluster" : "zbn-1.5",
        "host" : "zephyros",
        "port" : 19300
    },
    "hol-index" : "zdbholdings",
    "hol-type" : "zdbholdings",
    "maxbulkactions" : 1000,
    "maxconcurrentbulkrequests" : 4,
    "mock" : true,
    "detect-unknown" : true,
    "client" : "ingest",
    "timewindow" : "yyyyMMddHH",
    "aliases" : true,
    "ignoreindexcreationerror" : true
  }
  ' | ${java} \
    -cp ${lib}/\*:${bin}/\* \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.feed.elasticsearch.zdb.hol.MarcHolOAI