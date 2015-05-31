
java="java"

echo '
{
    "path" : "'${HOME}'/import/harvard/",
    "pattern" : "*.mrc",
    "elements" : "/org/xbib/analyzer/marc/bib.json",
    "concurrency" : 3,
    "elasticsearch" : {
        "cluster" : "elasticsearch",
        "host" : "localhost",
        "port" : 9300,
        "sniff" : false
    },
    "index" : "harvard",
    "type" : "title",
    "shards" : 1,
    "replica" : 0,
    "maxbulkactions" : 1000,
    "maxconcurrentbulkrequests" : 10,
    "maxtimewait" : "180s",
    "mock" : false,
    "detect" : true,
    "client" : "ingest",
    "direct" : true
}
' | ${java} \
     -cp $(pwd)/bin:$(pwd)/bin/\*:$(pwd)/lib/tools-1.0.0.Beta7-standalone.jar \
     -Dlog4j.configurationFile=$(pwd)/bin/log4j2.xml \
     org.xbib.tools.Runner org.xbib.tools.feed.elasticsearch.marc.FromMARC
