
package org.xbib.common.settings.loader;

import org.xbib.io.FastByteArrayInputStream;
import org.xbib.io.FastStringReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Settings loader that loads (parses) the settings in a properties format.
 */
public class PropertiesSettingsLoader implements SettingsLoader {

    @Override
    public Map<String, String> load(String source) throws IOException {
        Properties props = new Properties();
        try (FastStringReader reader = new FastStringReader(source)) {
            props.load(reader);
            Map<String, String> result = new HashMap<>();
            for (Map.Entry entry : props.entrySet()) {
                result.put((String) entry.getKey(), (String) entry.getValue());
            }
            return result;
        }
    }

    @Override
    public Map<String, String> load(byte[] source) throws IOException {
        Properties props = new Properties();
        try (FastByteArrayInputStream stream = new FastByteArrayInputStream(source)) {
            props.load(stream);
            Map<String, String> result = new HashMap<>();
            for (Map.Entry entry : props.entrySet()) {
                result.put((String) entry.getKey(), (String) entry.getValue());
            }
            return result;
        }
    }
}
