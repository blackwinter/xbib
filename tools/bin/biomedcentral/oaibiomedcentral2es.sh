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
        "http://www.biomedcentral.com/oai/2.0/?verb=ListRecords&metadataPrefix=bmc_bibl"
    ],
    "elasticsearch" : {
        "cluster" : "zbn-1.5",
        "host" : "zephyros",
        "port" : 19300
    },
    "index" : "bmc",
    "type" : "bmc",
    "index-settings" : "classpath:org/xbib/tools/feed/elasticsearch/bmc/settings.json",
    "index-mapping" : "classpath:org/xbib/tools/feed/elasticsearch/bmc/mapping.json",
    "maxbulkactions" : 1000,
    "maxconcurrentbulkrequests" : 8,
    "mock" : true,
    "client" : "ingest"
}
' | java \
    -cp ${lib}/\*:${bin}/\* \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.feed.elasticsearch.oai.BioMedCentral
