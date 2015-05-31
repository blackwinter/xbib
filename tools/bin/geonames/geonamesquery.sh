
curl -XPOST 'http://localhost:9200/geonames/_search?pretty' -d '
{
  "query" : {
    "filtered" : {
      "query" : {
        "match_all" : {}
      },
      "filter" : {
        "geo_distance" : {
          "distance" : "10km",
          "location" : {
            "lat" : 50.94,
            "lon" : 6.95
          }
        }
      }
    }
  }
}
'
