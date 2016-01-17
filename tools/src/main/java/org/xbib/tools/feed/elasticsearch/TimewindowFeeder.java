package org.xbib.tools.feed.elasticsearch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.unit.TimeValue;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.xbib.etl.support.ClasspathURLStreamHandler;
import org.xbib.tools.convert.Converter;
import org.xbib.util.concurrent.ForkJoinPipeline;
import org.xbib.util.concurrent.Pipeline;
import org.xbib.util.concurrent.URIWorkerRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TimewindowFeeder extends Feeder {

    private final static Logger logger = LogManager.getLogger(TimewindowFeeder.class.getSimpleName());

    @Override
    protected void prepareSink() throws IOException {
        if (ingest == null) {
            // for resolveAlias
            ingest = createIngest();
        }
        String timeWindow = settings.get("timewindow") != null ?
                DateTimeFormat.forPattern(settings.get("timewindow")).print(new DateTime()) : "";
        String resolvedIndex = ingest.resolveAlias(settings.get(getIndexParameterName()) + timeWindow);
        logger.info("resolved index = {}", resolvedIndex);
        setConcreteIndex(resolvedIndex);
        Pattern pattern = Pattern.compile("^(.*?)\\d*$");
        Matcher m = pattern.matcher(resolvedIndex);
        setIndex(m.matches() ? m.group(1) : resolvedIndex);
        logger.info("base index name = {}, concrete index name = {}", getIndex(), getConcreteIndex());
        super.prepareSink();
    }

    @Override
    protected TimewindowFeeder createIndex(String index, String concreteIndex) throws IOException {
        if (ingest.client() == null) {
            return this;
        }
        ingest.waitForCluster(ClusterHealthStatus.YELLOW, TimeValue.timeValueSeconds(30));
        try {
            String indexSettings = settings.get(getIndexParameterName() + "-settings", getIndexSettingsSpec());
            logger.info("using index settings from {}", indexSettings);
            InputStream indexSettingsInput = (indexSettings.startsWith("classpath:") ?
                    new URL(null, indexSettings, new ClasspathURLStreamHandler()) :
                    new URL(indexSettings)).openStream();
            String indexMappings = settings.get(getIndexParameterName() + "-mapping", getIndexMappingsSpec() );
            logger.info("using index mappings from {}", indexMappings);
            InputStream indexMappingsInput = (indexMappings.startsWith("classpath:") ?
                    new URL(null, indexMappings, new ClasspathURLStreamHandler()) :
                    new URL(indexMappings)).openStream();
            logger.info("creating index {}", concreteIndex);
            ingest.newIndex(concreteIndex, getType(), indexSettingsInput, indexMappingsInput);
            indexSettingsInput.close();
            indexMappingsInput.close();
            ingest.startBulk(concreteIndex, -1, 1);
        } catch (Exception e) {
            if (!settings.getAsBoolean("ignoreindexcreationerror", false)) {
                throw e;
            } else {
                logger.warn("index creation error, but configured to ignore", e);
            }
        }
        return this;
    }

    protected String getIndexSettingsSpec() {
        return  "classpath:org/xbib/tools/feed/elasticsearch/settings.json";
    }

    protected String getIndexMappingsSpec() {
        return "classpath:org/xbib/tools/feed/elasticsearch/mapping.json";
    }

    @Override
    protected void disposeSink() throws IOException {
        if (ingest != null && ingest.client() != null) {
            if (settings.getAsBoolean("aliases", false) && !settings.getAsBoolean("mock", false)) {
                ingest.switchAliases(getIndex(), getConcreteIndex(), Collections.singletonList(settings.get("identifier")));
            } else {
                logger.info("not doing alias settings because of configuration");
            }
            if (getConcreteIndex() != null) {
                ingest.stopBulk(getConcreteIndex());
            }
        }
        super.disposeSink();
    }

    @Override
    protected ForkJoinPipeline newPipeline() {
        return new ConfiguredPipeline();
    }

    @Override
    public TimewindowFeeder setPipeline(Pipeline<Converter,URIWorkerRequest> pipeline) {
        super.setPipeline(pipeline);
        if (pipeline instanceof ConfiguredPipeline) {
            ConfiguredPipeline configuredPipeline = (ConfiguredPipeline) pipeline;
            setSettings(configuredPipeline.getSettings());
            setIndex(configuredPipeline.getIndex());
            setConcreteIndex(configuredPipeline.getConcreteIndex());
            setType(configuredPipeline.getType());
        }
        return this;
    }

    class ConfiguredPipeline extends ForkJoinPipeline {
        public org.xbib.common.settings.Settings getSettings() {
            return settings;
        }
        public String getIndex() {
            return index;
        }
        public String getConcreteIndex() {
            return concreteIndex;
        }
        public String getType() {
            return type;
        }
    }
}
