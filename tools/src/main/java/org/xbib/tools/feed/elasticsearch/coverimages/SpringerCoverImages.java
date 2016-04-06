package org.xbib.tools.feed.elasticsearch.coverimages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.helper.client.SearchTransportClient;
import org.xbib.tools.convert.Converter;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.tools.input.ElasticsearchInput;
import org.xbib.util.IndexDefinition;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

public class SpringerCoverImages extends Feeder {

    private final static Logger logger = LogManager.getLogger(SpringerCoverImages.class);

    protected SearchTransportClient search;

    protected ElasticsearchInput elasticsearchInput;

    protected Map<String,IndexDefinition> inputIndexDefinitionMap;

    @Override
    @SuppressWarnings("unchecked")
    protected WorkerProvider<Converter> provider() {
        return p -> new SpringerCoverImages().setPipeline(p);
    }

    @Override
    protected  void prepareResources() throws IOException {
        super.prepareResources();
        logger.info("prepareResources: {}", settings.getAsMap());
        // we have to prepare this input before the workers come up!
        Map<String,Settings> inputMap = settings.getGroups("input");
        for (Map.Entry<String,Settings> entry : inputMap.entrySet()) {
            if ("elasticsearch".equals(entry.getKey())) {
                logger.info("preparing Elasticsearch for input");
                prepareElasticsearchForInput(entry.getValue());
            }
        }
        if (this.inputIndexDefinitionMap == null) {
            throw new IllegalArgumentException("no input definition map found");
        }
    }

    protected void prepareElasticsearchForInput(Settings elasticsearchSettings) throws IOException {
        this.search = elasticsearchInput.createSearch(elasticsearchSettings);
        this.inputIndexDefinitionMap = elasticsearchInput.makeIndexDefinitions(search, elasticsearchSettings.getGroups("index"));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(URI uri) throws Exception {
        SearchTransportClient search = new SearchTransportClient();
        try {
            Set<String> issns = new TreeSet<>();
            search = search.init(Settings.settingsBuilder()
                    .put("cluster.name", settings.get("elasticsearch.cluster"))
                    .put("host", settings.get("elasticsearch.host"))
                    .put("port", settings.getAsInt("elasticsearch.port", 9300))
                    .put("sniff", settings.getAsBoolean("elasticsearch.sniff", false))
                    .put("autodiscover", settings.getAsBoolean("elasticsearch.autodiscover", false))
                    .build().getAsMap());
            Client client = search.client();
            SearchRequestBuilder searchRequestBuilder = client.prepareSearch()
                    .setIndices(settings.get("ezdb-index", "ezdb"))
                    .setTypes(settings.get("ezdb-type", "Manifestation"))
                    .setSize(1000) // per shard
                    .setScroll(TimeValue.timeValueMillis(1000));

            QueryBuilder queryBuilder =
                    boolQuery().must(matchAllQuery()).filter(existsQuery("identifiers.issn"));
            searchRequestBuilder.setQuery(queryBuilder)
                    .addFields("identifiers.issn");

            SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
            while (searchResponse.getScrollId() != null) {
                searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMillis(1000))
                        .execute().actionGet();
                SearchHits hits = searchResponse.getHits();
                if (hits.getHits().length == 0) {
                    break;
                }
                for (SearchHit hit : hits) {
                    if (hit.getFields().containsKey("identifiers.issn")) {
                        List<Object> l = hit.getFields().get("identifiers.issn").getValues();
                        for (Object o : l) {
                            issns.add(o.toString());
                        }
                    }
                }
            }

        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        } finally {
            search.shutdown();
        }
    }
}
