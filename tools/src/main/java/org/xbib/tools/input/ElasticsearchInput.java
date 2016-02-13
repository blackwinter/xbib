package org.xbib.tools.input;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.helper.client.SearchTransportClient;
import org.xbib.util.IndexDefinition;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class ElasticsearchInput {

    private final static Logger logger = LogManager.getLogger(ElasticsearchInput.class);

    public SearchTransportClient createSearch(Settings settings) throws IOException {
        SearchTransportClient client = new SearchTransportClient().init(Settings.settingsBuilder()
                .put("cluster.name", settings.get("cluster", "elasticsearch"))
                .putArray("host", settings.getAsArray("host", new String[]{"localhost"}))
                .put("sniff", settings.getAsBoolean("sniff", false))
                .put("autodiscover", settings.getAsBoolean("autodiscover", false))
                .build().getAsMap());

        return client;
    }

    public Map<String,IndexDefinition> makeIndexDefinitions(SearchTransportClient search, Map<String,Settings> map) {
        Map<String,IndexDefinition> defs = new LinkedHashMap<>();
        for (Map.Entry<String,Settings> entry : map.entrySet()) {
            Settings settings = entry.getValue();
            String indexName = settings.get("name", entry.getKey());
            String concreteIndexName = indexName;
            String timeWindow = settings.get("timewindow");
            if (timeWindow != null) {
                String timeWindowStr = DateTimeFormatter.ofPattern(timeWindow).format(LocalDate.now());
                concreteIndexName = search.resolveAlias(indexName + timeWindowStr);
                logger.info("index name {} resolved to concrete index name = {}", indexName, concreteIndexName);
            }
            defs.put(entry.getKey(), new IndexDefinition(indexName, concreteIndexName,
                    settings.get("type"),
                    settings.get("settings", "classpath:org/xbib/tools/feed/elasticsearch/settings.json"),
                    settings.get("mapping", "classpath:org/xbib/tools/feed/elasticsearch/mapping.json"),
                    timeWindow,
                    false,
                    settings.getAsBoolean("skiperrors", false),
                    settings.getAsBoolean("aliases", true),
                    settings.getAsBoolean("retention.enabled", false),
                    settings.getAsInt("retention.diff", 0),
                    settings.getAsInt("retention.mintokeep", 0),
                    settings.getAsInt("replica", 0)));
        }
        logger.info("{}", defs);
        return defs;
    }

    public void close(SearchTransportClient search, Map<String,IndexDefinition> defs) throws IOException {
        if (search == null || defs == null) {
            return;
        }
        try {
            search.shutdown();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
