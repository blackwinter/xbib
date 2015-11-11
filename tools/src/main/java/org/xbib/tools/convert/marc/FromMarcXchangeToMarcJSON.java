package org.xbib.tools.convert.marc;

import org.xbib.marc.json.MarcXchangeJSONLinesWriter;
import org.xbib.marc.xml.MarcXchangeReader;
import org.xbib.tools.Converter;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This converter is for mapping from MarcXchange file to Marc JSON lines file
 */
public class FromMarcXchangeToMarcJSON extends Converter {

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    protected WorkerProvider provider() {
        return FromMarcXchangeToMarcJSON::new;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(URI uri) throws Exception {

        String fileName = uri.getSchemeSpecificPart();
        InputStream in = new FileInputStream(fileName);
        Integer bufferSize = settings.getAsInt("buffersize", null);
        if (fileName.endsWith(".gz")) {
            in = bufferSize != null ? new GZIPInputStream(in, bufferSize) : new GZIPInputStream(in);
        }
        try (OutputStream out = new GZIPOutputStream(new FileOutputStream(fileName + ".marc.jsonl.gz"))) {
            MarcXchangeReader reader = new MarcXchangeReader(in);
            MarcXchangeJSONLinesWriter writer = new MarcXchangeJSONLinesWriter(out);
            reader.setMarcXchangeListener(writer);
            writer.startDocument();
            writer.beginCollection();
            reader.parse();
            writer.endCollection();
            writer.endDocument();
        } finally {
            in.close();
        }
    }

}
