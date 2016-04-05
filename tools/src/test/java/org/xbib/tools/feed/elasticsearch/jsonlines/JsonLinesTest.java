package org.xbib.tools.feed.elasticsearch.jsonlines;

import org.junit.Test;
import org.xbib.common.settings.Settings;
import org.xbib.tools.feed.elasticsearch.mab.MarcXchangeJSONLines;

public class JsonLinesTest {

    @Test
    public void testJsonLines() throws Exception {
        String resourceDir = System.getProperty("RESOURCES_DIR");
        String prefix = "file://" + resourceDir + "/" + getClass().getPackage().getName().replace('.','/');

        MarcXchangeJSONLines marcXchangeJSONLines = new MarcXchangeJSONLines();
        Settings settings = Settings.settingsBuilder()
                .put("input.queue.uri", prefix + "/test.jsonl")
                .put("elements", "/org/xbib/analyzer/mab/titel.json")
                .put("catalogid", "DE-605")
                .put("collection", "hbz Verbundkatalog")
                .put("sigel2isil", "org/xbib/analyzer/mab/sigel2isil.json")
                .put("concurrency", 1)
                .put("pipelines", 1)
                .put("mock", true)
                .build();

        marcXchangeJSONLines.run(settings);
    }
}
