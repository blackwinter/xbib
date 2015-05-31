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
    "path" : "'${HOME}'/import/orcid/",
    "pattern" : "public_profiles-2014.tar.gz",
    "concurrency" : 1,
    "elasticsearch" : {
        "cluster" : "zbn-1.5",
        "host" : "zephyros",
        "port" : 19300
    },
    "index" : "orcid",
    "type" : "orcid",
    "index-settings" : "classpath:org/xbib/tools/feed/elasticsearch/orcid/settings.json",
    "type-mapping" : "classpath:org/xbib/tools/feed/elasticsearch/orcid/mapping.json",
    "mock" : false,
    "ignoreindexcreationerror": true
}
' | ${java} \
    -cp ${lib}/\*:${bin}/\* \
    -Dlog4j.configurationFile=${bin}/log4j2.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.feed.elasticsearch.orcid.ORCID
