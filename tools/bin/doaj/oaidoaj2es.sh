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

echo '
{
    "uri" : [
        "http://doaj.org/oai?verb=ListRecords&metadataPrefix=oai_dc&from=2000-01-01&until=2015-06-01"
    ],
    "elasticsearch" : {
        "cluster" : "zbn-1.5",
        "host" : "zephyros",
        "port" : 19300
    },
    "index" : "doaj",
    "type" : "doaj",
    "index-settings" : "classpath:org/xbib/tools/feed/elasticsearch/doaj/settings.json",
    "index-mapping" : "classpath:org/xbib/tools/feed/elasticsearch/doaj/mapping.json",
    "maxbulkactions" : 1000,
    "maxconcurrentbulkrequests" : 8,
    "mock" : false,
    "client" : "ingest"
}
' | java \
    -cp ${lib}/\*:${bin}/\* \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.feed.elasticsearch.oai.DOAJ
