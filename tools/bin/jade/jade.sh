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
    "path" : "'${HOME}'/import/bielefeld/jade/",
    "pattern" : "*.gz",
    "concurrency" : 6,
    "elasticsearch" : {
        "cluster" : "zbn-1.5",
        "host" : "zephyros",
        "port" : 19300
    },
    "index" : "jade",
    "type" : "jade",
    "index-settings" : "classpath:org/xbib/tools/feed/elasticsearch/jade/settings.json",
    "type-mapping" : "classpath:org/xbib/tools/feed/elasticsearch/jade/mapping.json",
    "mock" : false
}
' | ${java} \
    -cp ${lib}/\*:${bin}/\* \
    -Dlog4j.configurationFile=${bin}/log4j2-file.xml \
    org.xbib.tools.Runner \
    org.xbib.tools.feed.elasticsearch.jade.Jade
