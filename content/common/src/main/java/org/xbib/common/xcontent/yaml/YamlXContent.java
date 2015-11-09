
package org.xbib.common.xcontent.yaml;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.xbib.io.BytesReference;
import org.xbib.io.FastStringReader;
import org.xbib.common.xcontent.XContent;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.common.xcontent.XContentGenerator;
import org.xbib.common.xcontent.XContentParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;


/**
 * A YAML based content implementation using Jackson.
 */
public class YamlXContent implements XContent {

    public static XContentBuilder contentBuilder() throws IOException {
        return XContentBuilder.builder(yamlXContent);
    }

    final static YAMLFactory yamlFactory;
    public final static YamlXContent yamlXContent;

    static {
        yamlFactory = new YAMLFactory();
        yamlXContent = new YamlXContent();
    }

    public YamlXContent() {
    }

    @Override
    public String name() {
        return "yaml";
    }

    public byte streamSeparator() {
        throw new UnsupportedOperationException("yaml does not support stream parsing...");
    }


    @Override
    public XContentGenerator createGenerator(OutputStream os) throws IOException {
        return new YamlXContentGenerator(yamlFactory.createGenerator(os, JsonEncoding.UTF8));
    }


    @Override
    public XContentGenerator createGenerator(Writer writer) throws IOException {
        return new YamlXContentGenerator(yamlFactory.createGenerator(writer));
    }


    @Override
    public XContentParser createParser(String content) throws IOException {
        return new YamlXContentParser(yamlFactory.createParser(new FastStringReader(content)));
    }


    @Override
    public XContentParser createParser(InputStream is) throws IOException {
        return new YamlXContentParser(yamlFactory.createParser(is));
    }


    @Override
    public XContentParser createParser(byte[] data) throws IOException {
        return new YamlXContentParser(yamlFactory.createParser(data));
    }


    @Override
    public XContentParser createParser(byte[] data, int offset, int length) throws IOException {
        return new YamlXContentParser(yamlFactory.createParser(data, offset, length));
    }


    @Override
    public XContentParser createParser(BytesReference bytes) throws IOException {
        if (bytes.hasArray()) {
            return createParser(bytes.array(), bytes.arrayOffset(), bytes.length());
        }
        return createParser(bytes.streamInput());
    }


    @Override
    public XContentParser createParser(Reader reader) throws IOException {
        return new YamlXContentParser(yamlFactory.createParser(reader));
    }

    @Override
    public boolean isXContent(BytesReference bytes) {
        int length = bytes.length() < 20 ? bytes.length() : 20;
        if (length == 0) {
            return false;
        }
        byte first = bytes.get(0);
        if (length > 2 && first == '-' && bytes.get(1) == '-' && bytes.get(2) == '-') {
            return true;
        }
        return false;
    }
}
