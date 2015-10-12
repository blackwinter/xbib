package org.xbib.tools.feed.elasticsearch.natliz;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.io.InputService;
import org.xbib.pipeline.Pipeline;
import org.xbib.pipeline.PipelineProvider;
import org.xbib.tools.Feeder;

import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

public class NatLizMembers extends Feeder {

    @Override
    public String getName() {
        return "nlz-members-elasticsearch";
    }

    @Override
    protected PipelineProvider<Pipeline> pipelineProvider() {
        return NatLizMembers::new;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(URI uri) throws Exception {
        InputStream in = InputService.getInputStream(uri);
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> map = mapper.readValue(in, Map.class);
        for (String key : map.keySet()) {
            List<Map<String,Object>> members = (List<Map<String, Object>>) map.get(key);
            XContentBuilder builder = jsonBuilder();
            builder.startObject()
                    .field("key", key);
            List<String> values = new LinkedList<>();
            for (Map<String,Object> member : members) {
                List<String> isils = (List<String>) member.get("isil");
                values.addAll(isils);
            }
            builder.array("values", values);
            builder.endObject();
            ingest.index(settings.get("index"), settings.get("type"), key, builder.string());
        }
    }

}