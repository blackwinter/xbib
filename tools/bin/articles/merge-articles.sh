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

echo '
{
    "concurrency" : 24,
    "source" : {
        "cluster" : "zbn-1.5",
        "host" : "zephyros",
        "port" : 19300,
        "autodiscover" : true
    },
    "target" : {
        "cluster" : "zbn-1.5",
        "host" : "zephyros",
        "port" : 19300,
        "autodiscover" : true
    },
    "ezdb-index" : "ezdb",
    "ezdb-type" : "Manifestation",
    "zdb-index" : "zdb",
    "zdb-type" : "zdb",
    "medline-index" : "medline",
    "medline-type" : "medline",
    "xref-index" : "xref",
    "xref-type" : "xref",
    "service-index" : "ezdb",
    "service-type" : "DateHoldings",
    "target-index" : "articles",
    "target-type" : "articles",
    "maxbulkactions" : 2000,
    "issnonly" : true
}
' | ${java} \
    -cp ${lib}/\*:${bin}/\* \
    -Dlog4j.configurationFile=${bin}/log4j2-file.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.merge.articles.WithArticles