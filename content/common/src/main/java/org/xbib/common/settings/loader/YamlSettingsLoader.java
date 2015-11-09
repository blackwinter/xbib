
package org.xbib.common.settings.loader;

import java.io.IOException;
import java.util.Map;

import org.xbib.common.xcontent.XContent;
import org.xbib.common.xcontent.yaml.YamlXContent;

/**
 * Settings loader that loads (parses) the settings in a yaml format by flattening them
 * into a map.
 */
public class YamlSettingsLoader extends XContentSettingsLoader {

    @Override
    public XContent content() {
        return YamlXContent.yamlXContent;
    }

    @Override
    public Map<String, String> load(String source) throws IOException {
        // replace tabs with whitespace (yaml does not accept tabs, but many users might use it still...)
        return super.load(source.replace("\t", "  "));
    }
}
