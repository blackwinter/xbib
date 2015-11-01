
package org.xbib.common.xcontent;

import org.xbib.io.BytesReference;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

public interface XContentGenerator {

    XContentType contentType();

    void usePrettyPrint();

    void usePrintLineFeedAtEnd();

    void writeStartArray() throws IOException;

    void writeEndArray() throws IOException;

    void writeStartObject() throws IOException;

    void writeEndObject() throws IOException;

    void writeFieldName(String name) throws IOException;

    void writeFieldName(XContentString name) throws IOException;

    void writeString(String text) throws IOException;

    void writeString(char[] text, int offset, int len) throws IOException;

    void writeUTF8String(byte[] text, int offset, int length) throws IOException;

    void writeBinary(byte[] data, int offset, int len) throws IOException;

    void writeBinary(byte[] data) throws IOException;

    void writeNumber(int v) throws IOException;

    void writeNumber(long v) throws IOException;

    void writeNumber(double d) throws IOException;

    void writeNumber(float f) throws IOException;

    void writeNumber(BigDecimal bd) throws IOException;

    void writeNumber(BigInteger bi) throws IOException;

    void writeBoolean(boolean state) throws IOException;

    void writeNull() throws IOException;

    void writeStringField(String fieldName, String value) throws IOException;

    void writeStringField(XContentString fieldName, String value) throws IOException;

    void writeBooleanField(String fieldName, boolean value) throws IOException;

    void writeBooleanField(XContentString fieldName, boolean value) throws IOException;

    void writeNullField(String fieldName) throws IOException;

    void writeNumberField(String fieldName, int value) throws IOException;

    void writeNumberField(XContentString fieldName, int value) throws IOException;

    void writeNumberField(String fieldName, long value) throws IOException;

    void writeNumberField(XContentString fieldName, long value) throws IOException;

    void writeNumberField(String fieldName, double value) throws IOException;

    void writeNumberField(XContentString fieldName, double value) throws IOException;

    void writeNumberField(String fieldName, float value) throws IOException;

    void writeNumberField(XContentString fieldName, float value) throws IOException;

    void writeNumberField(String fieldName, BigInteger value) throws IOException;

    void writeNumberField(XContentString fieldName, BigInteger value) throws IOException;

    void writeNumberField(String fieldName, BigDecimal value) throws IOException;

    void writeNumberField(XContentString fieldName, BigDecimal value) throws IOException;

    void writeBinaryField(String fieldName, byte[] data) throws IOException;

    void writeBinaryField(XContentString fieldName, byte[] data) throws IOException;

    void writeArrayFieldStart(String fieldName) throws IOException;

    void writeArrayFieldStart(XContentString fieldName) throws IOException;

    void writeObjectFieldStart(String fieldName) throws IOException;

    void writeObjectFieldStart(XContentString fieldName) throws IOException;

    void writeRawField(String fieldName, byte[] content, OutputStream bos) throws IOException;

    void writeRawField(String fieldName, byte[] content, int offset, int length, OutputStream bos) throws IOException;

    void writeRawField(String fieldName, InputStream content, OutputStream bos) throws IOException;

    void writeRawField(String fieldName, BytesReference content, OutputStream bos) throws IOException;

    void writeValue(XContentBuilder builder) throws IOException;

    void copy(XContentBuilder builder, OutputStream bos) throws IOException;

    void copyCurrentStructure(XContentParser parser) throws IOException;

    void flush() throws IOException;

    void close() throws IOException;
}
