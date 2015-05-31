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

d=`gdate +%Y-%m-%d`
p1=`gdate -d "${d} - 1 years" +"%Y-%m-%dT%H:%M:%SZ"`
p2=`gdate -d "${d}" +"%Y-%m-%dT%H:%M:%SZ"`

echo '
  {
    "uri" : [
        "http://dl380-47.gbv.de/oai/natliz/?verb=ListRecords&metadataPrefix=extpp2&from='${p1}'&until='${p2}'"
    ],
    "count" : 5,
    "concurrency" : 1,
    "pipelines" : 1,
    "elasticsearch" : {
        "cluster" : "zbn-1.5",
        "host" : "zephyros",
        "port" : 19300
    },
    "index" : "natliz",
    "type" : "natliz",
    "maxbulkactions" : 1000,
    "maxconcurrentbulkrequests" : 4,
    "mock" : true,
    "client" : "ingest"
  }
  ' | ${java} \
    -cp ${lib}/\*:${bin}/\* \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.feed.elasticsearch.oai.NatLiz