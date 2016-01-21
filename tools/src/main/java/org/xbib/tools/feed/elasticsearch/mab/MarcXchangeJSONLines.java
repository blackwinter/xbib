package org.xbib.tools.feed.elasticsearch.mab;

import org.xbib.etl.marc.dialects.mab.MABEntityQueue;
import org.xbib.marc.json.MarcXchangeJSONLinesReader;
import org.xbib.marc.keyvalue.MarcXchange2KeyValue;
import org.xbib.tools.convert.Converter;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.IOException;
import java.io.InputStream;

public class MarcXchangeJSONLines extends TitleHoldingsFeeder {

    @Override
    protected WorkerProvider<Converter> provider() {
        return p -> new MarcXchangeJSONLines().setPipeline(p);
    }

    @Override
    public void process(InputStream in, MABEntityQueue queue) throws IOException {
        final MarcXchange2KeyValue kv = new MarcXchange2KeyValue()
                .addListener(queue);
        MarcXchangeJSONLinesReader reader = new MarcXchangeJSONLinesReader(in, kv);
        reader.parse();
    }

}
