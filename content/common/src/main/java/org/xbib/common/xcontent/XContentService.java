package org.xbib.common.xcontent;

import org.xbib.common.xcontent.json.JsonXContent;
import org.xbib.common.xcontent.smile.SmileXContent;
import org.xbib.common.xcontent.yaml.YamlXContent;
import org.xbib.io.BytesArray;
import org.xbib.io.BytesReference;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class XContentService {

    private final static Map<String, XContent> xcontents = new HashMap<>();

    private final static XContentService instance = new XContentService();

    private XContentService() {
        try {
            ServiceLoader<XContent> loader = ServiceLoader.load(XContent.class);
            for (XContent xContent : loader) {
                if (!xcontents.containsKey(xContent.name())) {
                    xcontents.put(xContent.name(), xContent);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static XContentService getInstance() {
        return instance;
    }

    public static XContentBuilder builder(String name) throws IOException {
        return xcontents.containsKey(name) ? XContentBuilder.builder(xcontents.get(name)) : null;
    }

    public static XContentBuilder jsonBuilder() throws IOException {
        return XContentBuilder.builder(JsonXContent.jsonXContent);
    }

    public static XContentBuilder jsonBuilder(OutputStream out) throws IOException {
        return XContentBuilder.builder(JsonXContent.jsonXContent, out);
    }

    public static XContentBuilder smileBuilder() throws IOException {
        return XContentBuilder.builder(SmileXContent.smileXContent);
    }

    public static XContentBuilder smileBuilder(OutputStream out) throws IOException {
        return XContentBuilder.builder(SmileXContent.smileXContent, out);
    }

    public static XContentBuilder yamlBuilder() throws IOException {
        return XContentBuilder.builder(YamlXContent.yamlXContent);
    }

    public static XContentBuilder yamlBuilder(OutputStream out) throws IOException {
        return XContentBuilder.builder(YamlXContent.yamlXContent, out);
    }

    public static XContent xContent(byte[] data, int offset, int length) {
        return xContent(new BytesArray(data, offset, length));
    }

    public static XContent xContent(String charSequence) {
        return xContent(new BytesArray(charSequence));
    }

    public static XContent xContent(BytesReference bytes) {
        for (XContent xcontent : xcontents.values()) {
            if (xcontent.isXContent(bytes)) {
                return xcontent;
            }
        }
        return null;
    }

}