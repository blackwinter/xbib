package org.xbib.rdf.io.source;

import org.xbib.rdf.io.sink.CharSink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

final class CharSource extends AbstractSource<CharSink> {

    CharSource(CharSink sink) {
        super(sink);
    }

    @Override
    public void process(Reader reader, String mimeType, String baseUri) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            sink.setBaseUri(baseUri);
            char[] buffer = new char[1024];
            int read;
            while ((read = bufferedReader.read(buffer)) != -1) {
                sink.process(buffer, 0, read);
            }
        }
    }

    @Override
    public void process(InputStream inputStream, String mimeType, String baseUri) throws IOException {
        try (Reader reader = new InputStreamReader(inputStream, Charset.forName("UTF-8"))) {
            process(reader, mimeType, baseUri);
        }
    }

}