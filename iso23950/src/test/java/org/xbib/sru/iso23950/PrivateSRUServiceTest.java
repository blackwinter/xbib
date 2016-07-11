package org.xbib.sru.iso23950;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.xbib.io.iso23950.searchretrieve.ZSearchRetrieveRequest;
import org.xbib.io.iso23950.searchretrieve.ZSearchRetrieveResponse;
import org.xbib.sru.client.SRUClient;
import org.xbib.sru.iso23950.service.ZSRUServiceFactory;
import org.xbib.sru.service.SRUService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;

public class PrivateSRUServiceTest {

    private final static Logger logger = LogManager.getLogger(SRUServiceTest.class.getName());

    @Test
    public void testSRUService() throws IOException {
        for (String name : Arrays.asList("DE-600", "DE-601", "DE-602", "DE-604", "DE-604", "DE-605")) {
            logger.info("trying " + name);
            try {
                SRUService<ZSearchRetrieveRequest, ZSearchRetrieveResponse> service = ZSRUServiceFactory.getService(name);
                if (service != null) {
                    SRUClient<ZSearchRetrieveRequest, ZSearchRetrieveResponse> client = service.newClient();
                    File file = File.createTempFile("sru-" + service.getURI().getHost(), ".xml");
                    FileOutputStream out = new FileOutputStream(file);
                    Writer writer = new OutputStreamWriter(out, "UTF-8");
                    String query = "dc.title = test";
                    int from = 1;
                    int size = 10;
                    ZSearchRetrieveRequest request = client.newSearchRetrieveRequest(service.getURI().toString());
                    request.setQuery(query)
                            .setStartRecord(from)
                            .setMaximumRecords(size);
                    client.searchRetrieve(request).to(writer);
                    writer.close();
                    out.close();
                    client.close();
                }
            } catch (Exception e) {
                logger.warn(e.getMessage());
            }
        }
    }
}
