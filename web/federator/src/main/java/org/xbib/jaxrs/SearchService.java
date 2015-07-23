package org.xbib.jaxrs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.io.iso23950.Diagnostics;
import org.xbib.io.iso23950.client.ZClient;
import org.xbib.io.iso23950.client.ZClientFactory;
import org.xbib.io.iso23950.searchretrieve.ZSearchRetrieveRequest;
import org.xbib.io.iso23950.searchretrieve.ZSearchRetrieveResponse;
import org.xbib.xml.transform.StylesheetTransformer;

import javax.servlet.ServletConfig;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class SearchService {

    private final static Logger logger = LogManager.getLogger(SearchService.class.getName());

    @Context
    ServletConfig servletConfig;

    @POST
    @Produces({"application/xhtml+xml; charset=UTF-8"})
    public StreamingOutput postXHTML(@QueryParam("q") final String query,
                                     @QueryParam("from") final int from,
                                     @QueryParam("size") final int size,
                                     @QueryParam("service") final String service)
            throws Exception {
        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {
                    ZClient client = ZClientFactory.getInstance().newZClient(service);
                    ZSearchRetrieveRequest request = client.newCQLSearchRetrieveRequest()
                            .setQuery(query)
                            .setFrom(from)
                            .setSize(size);
                    ZSearchRetrieveResponse response = request.execute();
                    StylesheetTransformer transformer = new StylesheetTransformer().setPath("xsl");
                    response.setStylesheetTransformer(transformer)
                            .setOutputFormat("html")
                            .to(new OutputStreamWriter(output, "UTF-8"));
                    transformer.close();
                    client.close();
                } catch (Diagnostics d) {
                    logger.error(d.getMessage(), d);
                    throw new IOException(d);
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    throw new IOException(e);
                }
            }
        };
    }
}
