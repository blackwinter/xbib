package org.xbib.io.http.client.handler.resumable;

import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;

public class PropertiesBasedResumableProcesserTest {

    @Test(groups = "standalone")
    public void testSaveLoad() throws Exception {
        PropertiesBasedResumableProcessor p = new PropertiesBasedResumableProcessor();
        p.put("http://localhost/test.url", 15L);
        p.put("http://localhost/test2.url", 50L);
        p.save(null);
        p = new PropertiesBasedResumableProcessor();
        Map<String, Long> m = p.load();
        assertEquals(m.size(), 2);
        assertEquals(m.get("http://localhost/test.url"), Long.valueOf(15L));
        assertEquals(m.get("http://localhost/test2.url"), Long.valueOf(50L));
    }
}
