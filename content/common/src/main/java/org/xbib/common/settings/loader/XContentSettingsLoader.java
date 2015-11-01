
package org.xbib.common.settings.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xbib.common.xcontent.XContentFactory;
import org.xbib.common.xcontent.XContentParser;
import org.xbib.common.xcontent.XContentType;

/**
 * Settings loader that loads (parses) the settings in a xcontent format by flattening them
 * into a map.
 */
public abstract class XContentSettingsLoader implements SettingsLoader {

    public abstract XContentType contentType();

    @Override
    public Map<String, String> load(String source) throws IOException {
        XContentParser parser = XContentFactory.xContent(contentType()).createParser(source);
        try {
            return load(parser);
        } finally {
            parser.close();
        }
    }

    @Override
    public Map<String, String> load(byte[] source) throws IOException {
        try (XContentParser parser = XContentFactory.xContent(contentType()).createParser(source)) {
            return load(parser);
        }
    }

    public Map<String, String> load(XContentParser jp) throws IOException {
        StringBuilder sb = new StringBuilder();
        Map<String, String> settings = new HashMap<>();
        List<String> path = new ArrayList<>();
        XContentParser.Token token = jp.nextToken();
        if (token == null) {
            return settings;
        }
        serializeObject(settings, sb, path, jp, null);
        return settings;
    }

    private void serializeObject(Map<String, String> settings, StringBuilder sb, List<String> path, XContentParser parser, String objFieldName) throws IOException {
        if (objFieldName != null) {
            path.add(objFieldName);
        }

        String currentFieldName = null;
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.START_OBJECT) {
                serializeObject(settings, sb, path, parser, currentFieldName);
            } else if (token == XContentParser.Token.START_ARRAY) {
                serializeArray(settings, sb, path, parser, currentFieldName);
            } else if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == XContentParser.Token.VALUE_NULL) {
                // ignore this
            } else {
                serializeValue(settings, sb, path, parser, currentFieldName);

            }
        }

        if (objFieldName != null) {
            path.remove(path.size() - 1);
        }
    }

    private void serializeArray(Map<String, String> settings, StringBuilder sb, List<String> path, XContentParser parser, String fieldName) throws IOException {
        XContentParser.Token token;
        int counter = 0;
        while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
            if (token == XContentParser.Token.START_OBJECT) {
                serializeObject(settings, sb, path, parser, fieldName + '.' + (counter++));
            } else if (token == XContentParser.Token.START_ARRAY) {
                serializeArray(settings, sb, path, parser, fieldName + '.' + (counter++));
            } else if (token == XContentParser.Token.FIELD_NAME) {
                fieldName = parser.currentName();
            } else if (token == XContentParser.Token.VALUE_NULL) {
                // ignore
            } else {
                serializeValue(settings, sb, path, parser, fieldName + '.' + (counter++));
            }
        }
    }

    private void serializeValue(Map<String, String> settings, StringBuilder sb, List<String> path, XContentParser parser, String fieldName) throws IOException {
        sb.setLength(0);
        for (String pathEle : path) {
            sb.append(pathEle).append('.');
        }
        sb.append(fieldName);
        settings.put(sb.toString(), parser.text());
    }

}
