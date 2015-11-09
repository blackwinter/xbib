
package org.xbib.common.xcontent.smile;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.dataformat.smile.SmileParser;
import org.xbib.common.xcontent.XContent;
import org.xbib.io.BytesReference;
import org.xbib.common.xcontent.json.JsonXContentGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SmileXContentGenerator extends JsonXContentGenerator {

    public SmileXContentGenerator(JsonGenerator generator) {
        super(generator);
    }

    @Override
    public XContent content() {
        return SmileXContent.smileXContent;
    }

    @Override
    public void writeRawField(String fieldName, InputStream content, OutputStream bos) throws IOException {
        writeFieldName(fieldName);
        SmileParser parser = SmileXContent.smileFactory.createParser(content);
        try {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        } finally {
            parser.close();
        }
    }

    @Override
    public void writeRawField(String fieldName, byte[] content, OutputStream bos) throws IOException {
        writeFieldName(fieldName);
        SmileParser parser = SmileXContent.smileFactory.createParser(content);
        try {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        } finally {
            parser.close();
        }
    }

    @Override
    public void writeRawField(String fieldName, BytesReference content, OutputStream bos) throws IOException {
        writeFieldName(fieldName);
        SmileParser parser;
        if (content.hasArray()) {
            parser = SmileXContent.smileFactory.createParser(content.array(), content.arrayOffset(), content.length());
        } else {
            parser = SmileXContent.smileFactory.createParser(content.streamInput());
        }
        try {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        } finally {
            parser.close();
        }
    }

    @Override
    public void writeRawField(String fieldName, byte[] content, int offset, int length, OutputStream bos) throws IOException {
        writeFieldName(fieldName);
        SmileParser parser = SmileXContent.smileFactory.createParser(content, offset, length);
        try {
            parser.nextToken();
            generator.copyCurrentStructure(parser);
        } finally {
            parser.close();
        }
    }
}
