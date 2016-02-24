
package org.xbib.common.settings.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides the ability to load settings (in the form of a simple Map) from
 * the actual source content that represents them.
 */
public interface SettingsLoader {

    class Helper {

        public static Map<String, String> loadNestedFromMap(Map<Object, Object> map) {
            Map<String, String> settings = new HashMap<>();
            if (map == null) {
                return settings;
            }
            StringBuilder sb = new StringBuilder();
            List<String> path = new ArrayList<>();
            serializeMap(settings, sb, path, map);
            return settings;
        }

        @SuppressWarnings("unchecked")
        private static void serializeMap(Map<String, String> settings, StringBuilder sb, List<String> path, Map<Object, Object> map) {
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    path.add((String) entry.getKey());
                    serializeMap(settings, sb, path, (Map<Object, Object>) entry.getValue());
                    path.remove(path.size() - 1);
                } else if (entry.getValue() instanceof List) {
                    path.add((String) entry.getKey());
                    serializeList(settings, sb, path, (List) entry.getValue());
                    path.remove(path.size() - 1);
                } else {
                    serializeValue(settings, sb, path, (String) entry.getKey(), entry.getValue());
                }
            }
        }

        @SuppressWarnings("unchecked")
        private static void serializeList(Map<String, String> settings, StringBuilder sb, List<String> path, List list) {
            int counter = 0;
            for (Object obj : list) {
                if (obj instanceof Map) {
                    path.add(Integer.toString(counter));
                    serializeMap(settings, sb, path, (Map<Object, Object>) obj);
                    path.remove(path.size() - 1);
                } else if (obj instanceof List) {
                    path.add(Integer.toString(counter));
                    serializeList(settings, sb, path, (List) obj);
                    path.remove(path.size() - 1);
                } else {
                    serializeValue(settings, sb, path, Integer.toString(counter), obj);
                }
                counter++;
            }
        }

        private static void serializeValue(Map<String, String> settings, StringBuilder sb, List<String> path, String name, Object value) {
            if (value == null) {
                return;
            }
            sb.setLength(0);
            for (String pathEle : path) {
                sb.append(pathEle).append('.');
            }
            sb.append(name);
            settings.put(sb.toString(), value.toString());
        }
    }


    /**
     * Loads (parses) the settings from a source string.
     */
    Map<String, String> load(String source) throws IOException;

    /**
     * Loads (parses) the settings from a source bytes.
     */
    Map<String, String> load(byte[] source) throws IOException;
}
