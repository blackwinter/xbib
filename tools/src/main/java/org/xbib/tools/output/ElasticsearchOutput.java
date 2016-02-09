package org.xbib.tools.output;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.unit.TimeValue;
import org.xbib.common.settings.Settings;
import org.xbib.elasticsearch.helper.client.ClientBuilder;
import org.xbib.elasticsearch.helper.client.IndexAliasAdder;
import org.xbib.elasticsearch.helper.client.Ingest;
import org.xbib.elasticsearch.helper.client.LongAdderIngestMetric;
import org.xbib.elasticsearch.helper.client.MockTransportClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ElasticsearchOutput {

    private final static Logger logger = LogManager.getLogger(ElasticsearchOutput.class);

    public Ingest createIngest(Settings settings) throws IOException {
        if (!settings.containsSetting("cluster")) {
            return null;
        }
        org.elasticsearch.common.settings.Settings elasticsearchSettings = org.elasticsearch.common.settings.Settings.builder()
                .put(settings.getAsMap())
                .put("cluster.name", settings.get("cluster", "elasticsearch"))
                .put("sniff", settings.getAsBoolean("sniff", false))
                .put("autodiscover", settings.getAsBoolean("autodiscover", false))
                .putArray("host", settings.getAsArray("host", new String[]{"localhost"}))
                .build();
        ClientBuilder clientBuilder = ClientBuilder.builder()
                .put(elasticsearchSettings)
                .put(ClientBuilder.MAX_ACTIONS_PER_REQUEST, settings.getAsInt("maxbulkactions", 1000))
                .put(ClientBuilder.MAX_CONCURRENT_REQUESTS, settings.getAsInt("maxconcurrentbulkrequests",
                        Runtime.getRuntime().availableProcessors()))
                .setMetric(new LongAdderIngestMetric());
        if (settings.getAsBoolean("mock", false)) {
            logger.info("mock");
            return clientBuilder.toMockTransportClient();
        }
        if ("ingest".equals(settings.get("client"))) {
            return clientBuilder.toIngestTransportClient();
        }
        return clientBuilder.toBulkTransportClient();
    }

    public Map<String,IndexDefinition> makeIndexDefinitions(final Ingest ingest, Map<String,Settings> map) {
        boolean mock = ingest.client() instanceof MockTransportClient;
        Map<String,IndexDefinition> defs = new LinkedHashMap<>();
        for (Map.Entry<String,Settings> entry : map.entrySet()) {
            Settings settings = entry.getValue();
            String indexName = settings.get("name", entry.getKey());
            String concreteIndexName = indexName;
            String timeWindow = settings.get("timewindow");
            if (timeWindow != null) {
                String timeWindowStr = DateTimeFormatter.ofPattern(timeWindow).format(LocalDate.now());
                concreteIndexName = ingest.resolveAlias(indexName + timeWindowStr);
                logger.info("index name {} resolved to concrete index name = {}", indexName, concreteIndexName);
            }
            defs.put(entry.getKey(), new IndexDefinition(indexName, concreteIndexName,
                    settings.get("type"),
                    settings.get("settings", "classpath:org/xbib/tools/feed/elasticsearch/settings.json"),
                    settings.get("mapping", "classpath:org/xbib/tools/feed/elasticsearch/mapping.json"),
                    timeWindow,
                    settings.getAsBoolean("mock", mock),
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

    public void createIndex(final Ingest ingest, final IndexDefinition indexDefinition) throws IOException {
        if (ingest == null || ingest.client() == null) {
            return;
        }
        ingest.waitForCluster("YELLOW", TimeValue.timeValueSeconds(30));
        String indexSettings = indexDefinition.getSettingDef();
        if (indexSettings == null) {
            throw new IllegalArgumentException("no settings defined for index " + indexDefinition.getIndex());
        }
        String indexMappings = indexDefinition.getMappingDef();
        if (indexMappings == null) {
            throw new IllegalArgumentException("no mappings defined for index " + indexDefinition.getIndex());
        }
        try (InputStream indexSettingsInput = new URL(indexSettings).openStream();
             InputStream indexMappingsInput = new URL(indexMappings).openStream()) {
            ingest.newIndex(indexDefinition.getConcreteIndex(), indexDefinition.getType(), indexSettingsInput, indexMappingsInput);
        } catch (Exception e) {
            if (!indexDefinition.ignoreErrors()) {
                throw new IOException(e);
            } else {
                logger.warn("error while creating index '" + indexDefinition.getConcreteIndex()
                        + "', but configured to ignore: " + e.getMessage(), e);
            }
        }
    }

    public void startup(Ingest ingest, Map<String,IndexDefinition> defs) throws IOException {
        if (ingest == null || ingest.client() == null) {
            return;
        }
        for (Map.Entry<String,IndexDefinition> entry : defs.entrySet()) {
            IndexDefinition def = entry.getValue();
            ingest.startBulk(def.getConcreteIndex(), -1, 1);
        }
    }

    public void close(Ingest ingest, Map<String,IndexDefinition> defs) throws IOException {
        if (ingest == null || ingest.client() == null || defs == null) {
            return;
        }
        try {
            logger.info("flush bulk");
            ingest.flushIngest();
            logger.info("waiting for all bulk responses from Elasticsearch cluster");
            ingest.waitForResponses(TimeValue.timeValueSeconds(120));
            logger.info("all bulk responses received");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            logger.info("updating cluster settings of {}", defs.keySet());
            for (Map.Entry<String,IndexDefinition> entry : defs.entrySet()) {
                IndexDefinition def = entry.getValue();
                ingest.stopBulk(def.getConcreteIndex());
            }
        }
    }

    public void switchIndex(Ingest ingest, IndexDefinition indexDefinition, List<String> extraAliases) {
        if (ingest == null || ingest.client() == null) {
            return;
        }
        if (extraAliases == null) {
            return;
        }
        if (indexDefinition.isSwitchAliases()) {
            // filter out null/empty values
            List<String> validAliases = extraAliases.stream()
                    .filter(a -> a != null && !a.isEmpty())
                    .collect(Collectors.toList());
            try {
                ingest.switchAliases(indexDefinition.getIndex(), indexDefinition.getConcreteIndex(), validAliases);
            } catch (Exception e) {
                logger.warn("switching index failed: " + e.getMessage(), e);
            }
        }
    }

    public void switchIndex(Ingest ingest, IndexDefinition indexDefinition,
                            List<String> extraAliases, IndexAliasAdder indexAliasAdder) {
        if (ingest == null || ingest.client() == null) {
            return;
        }
        if (extraAliases == null) {
            return;
        }
        if (indexDefinition.isSwitchAliases()) {
            // filter out null/empty values
            List<String> validAliases = extraAliases.stream()
                    .filter(a -> a != null && !a.isEmpty())
                    .collect(Collectors.toList());
            try {
                ingest.switchAliases(indexDefinition.getIndex(), indexDefinition.getConcreteIndex(),
                        validAliases, indexAliasAdder);
            } catch (Exception e) {
                logger.warn("switching index failed: " + e.getMessage(), e);
            }
        }
    }

    public void retention(Ingest ingest, IndexDefinition indexDefinition) {
        if (ingest == null || ingest.client() == null) {
            return;
        }
        logger.info("retention parameters: name={} enabled={} timestampDiff={} minToKeep={}",
                indexDefinition.getIndex(),
                indexDefinition.hasRetention(),
                indexDefinition.getTimestampDiff(),
                indexDefinition.getMinToKeep());
        if (indexDefinition.hasRetention() && (indexDefinition.getTimestampDiff() > 0 || indexDefinition.getMinToKeep() > 0)) {
            ingest.performRetentionPolicy(indexDefinition.getIndex(),
                    indexDefinition.getConcreteIndex(),
                    indexDefinition.getTimestampDiff(),
                    indexDefinition.getMinToKeep());
        }
    }

    public void replica(Ingest ingest, IndexDefinition indexDefinition) {
        if (ingest == null || ingest.client() == null) {
            return;
        }
        if (indexDefinition.getReplicaLevel() > 0) {
            try {
                ingest.updateReplicaLevel(indexDefinition.getConcreteIndex(), indexDefinition.getReplicaLevel());
            } catch (Exception e) {
                logger.warn("setting replica failed: " + e.getMessage(), e);
            }
        }
    }

    public void shutdown(Ingest ingest) {
        if (ingest == null || ingest.client() == null) {
            return;
        }
        ingest.shutdown();
    }

}
