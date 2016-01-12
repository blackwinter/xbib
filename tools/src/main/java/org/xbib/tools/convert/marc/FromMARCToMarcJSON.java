package org.xbib.tools.convert.marc;

import org.xbib.marc.Iso2709Reader;
import org.xbib.marc.json.MarcXchangeJSONLinesWriter;
import org.xbib.tools.convert.Converter;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This converter is for mapping from Marc file to Marc JSON lines file
 */
public class FromMARCToMarcJSON extends Converter {

    private final static Charset UTF8 = Charset.forName("UTF-8");

    @Override
    protected WorkerProvider provider() {
        return p -> new FromMARCToMarcJSON().setPipeline(p);
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
        Charset charset = Charset.forName(settings.get("encoding","US-ASCII"));

        try (OutputStream out = new GZIPOutputStream(new FileOutputStream(fileName + ".marc.jsonl.gz"))) {
            Iso2709Reader reader = new Iso2709Reader(new InputStreamReader(in, charset));
            MarcXchangeJSONLinesWriter writer = new MarcXchangeJSONLinesWriter(out);
            reader.setStringTransformer(value -> Normalizer.normalize(new String(value.getBytes(charset), UTF8), Normalizer.Form.NFKC));
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
