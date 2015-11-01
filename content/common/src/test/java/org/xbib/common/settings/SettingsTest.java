package org.xbib.common.settings;

import org.testng.annotations.Test;
import org.xbib.common.xcontent.XContentHelper;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.xbib.common.settings.Settings.settingsBuilder;

public class SettingsTest {

    @Test
    public void testArray() {
        Settings settings = Settings.settingsBuilder()
                .putArray("input", Arrays.asList("a","b","c")).build();
        assertEquals("a", settings.getAsArray("input")[0]);
        assertEquals("b", settings.getAsArray("input")[1]);
        assertEquals("c", settings.getAsArray("input")[2]);
    }

    @Test
    public void testMapForSettings() {
        Map<String,Object> map = new HashMap<>();
        map.put("hello", "world");
        Map<String,Object> settingsMap = new HashMap<>();
        settingsMap.put("map", map);
        Settings settings = settingsBuilder().loadFromMap(settingsMap).build();
        assertEquals("{map.hello=world}", settings.getAsMap().toString());
    }

    @Test
    public void testMapSettingsFromReader() {
        StringReader reader = new StringReader("{\"map\":{\"hello\":\"world\"}}");
        Map<String,Object> spec = XContentHelper.convertFromJsonToMap(reader);
        Settings settings = settingsBuilder().loadFromMap(spec).build();
        assertEquals("{map.hello=world}", settings.getAsMap().toString());
    }

}
