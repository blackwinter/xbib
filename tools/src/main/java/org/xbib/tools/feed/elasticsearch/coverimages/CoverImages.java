package org.xbib.tools.feed.elasticsearch.coverimages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilders;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.helper.client.SearchTransportClient;
import org.xbib.io.archive.StreamUtil;
import org.xbib.tools.convert.Converter;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.tools.input.ElasticsearchInput;
import org.xbib.util.IndexDefinition;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

public class CoverImages extends Feeder {

    private final static Logger logger = LogManager.getLogger(CoverImages.class);

    protected SearchTransportClient search;

    protected ElasticsearchInput elasticsearchInput;

    protected Map<String,IndexDefinition> inputIndexDefinitionMap;

    @Override
    @SuppressWarnings("unchecked")
    protected WorkerProvider<Converter> provider() {
        return p -> new CoverImages().setPipeline(p);
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
        try {
            SearchRequestBuilder searchRequestBuilder = search.client().prepareSearch()
                    .setIndices(inputIndexDefinitionMap.get("ezdb").getIndex())
                    .setTypes(inputIndexDefinitionMap.get("ezdb").getType())
                    .setSize(1000) // per shard
                    .setScroll(TimeValue.timeValueMillis(1000))
                    .addSort(SortBuilders.fieldSort("_doc"));
            QueryBuilder queryBuilder =
                    boolQuery().must(matchAllQuery()).filter(existsQuery("identifiers.issn"));
            searchRequestBuilder.setQuery(queryBuilder)
                    .addFields("identifiers.issn");
            SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
            while (searchResponse.getScrollId() != null) {
                searchResponse = search.client().prepareSearchScroll(searchResponse.getScrollId())
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
                            lookupISSN(o.toString());
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

    private void lookupISSN(String issn) throws Exception {
        retrieveFromDeGruyter(issn);
    }

    private void retrieveFromDeGruyter(String issn) throws Exception {
        String urlStr = String.format("http://www.degruyter.com/doc/cover/s%s.jpg", issn);
        fetchURL(new URL(urlStr), issn);
    }

    private void retrieveFromElsevierr(String issn) throws Exception {
        String urlStr = String.format("http://ars.els-cdn.com/content/image/S%s.gif", issn);
        fetchURL(new URL(urlStr), issn);
    }

    // ISSN
    // https://static-content.springer.com/cover/journal/236/53/3.jpg

    private void fetchURL(URL url, String issn) throws Exception {
        try (InputStream in = url.openStream()) {
            FileOutputStream out = new FileOutputStream(new File(issn));
            StreamUtil.copy(in, out);
            out.close();
        }
    }
}
