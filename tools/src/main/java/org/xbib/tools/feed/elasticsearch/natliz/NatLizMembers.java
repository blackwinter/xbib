package org.xbib.tools.feed.elasticsearch.natliz;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.tools.convert.Converter;
import org.xbib.util.InputService;
import org.xbib.tools.feed.elasticsearch.Feeder;
import org.xbib.util.concurrent.WorkerProvider;

import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.xbib.common.xcontent.XContentService.jsonBuilder;

public class NatLizMembers extends Feeder {

    @Override
    protected WorkerProvider<Converter> provider() {
        return p -> new NatLizMembers().setPipeline(p);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(URI uri) throws Exception {
        try (InputStream in = InputService.getInputStream(uri)) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = mapper.readValue(in, Map.class);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                List<Map<String, Object>> members = (List<Map<String, Object>>) entry.getValue();
                XContentBuilder builder = jsonBuilder();
                builder.startObject()
                        .field("key", key);
                List<String> values = new LinkedList<>();
                for (Map<String, Object> member : members) {
                    List<String> isils = (List<String>) member.get("isil");
                    values.addAll(isils);
                }
                builder.array("values", values);
                builder.endObject();
                ingest.index(settings.get("index"), settings.get("type"), key, builder.string());
            }
        }
    }

}
