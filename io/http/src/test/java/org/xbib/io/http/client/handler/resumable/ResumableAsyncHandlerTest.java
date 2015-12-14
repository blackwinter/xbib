package org.xbib.io.http.client.handler.resumable;

import io.netty.handler.codec.http.HttpHeaders;
import org.testng.annotations.Test;
import org.xbib.io.http.client.Request;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.xbib.io.http.client.Dsl.get;

public class ResumableAsyncHandlerTest {

    @Test(groups = "standalone")
    public void testAdjustRange() {
        MapResumableProcessor proc = new MapResumableProcessor();

        ResumableAsyncHandler h = new ResumableAsyncHandler(proc);
        Request request = get("http://test/url").build();
        Request newRequest = h.adjustRequestRange(request);
        assertEquals(newRequest.getUri(), request.getUri());
        String rangeHeader = newRequest.getHeaders().get(HttpHeaders.Names.RANGE);
        assertNull(rangeHeader);

        proc.put("http://test/url", 5000);
        newRequest = h.adjustRequestRange(request);
        assertEquals(newRequest.getUri(), request.getUri());
        rangeHeader = newRequest.getHeaders().get(HttpHeaders.Names.RANGE);
        assertEquals(rangeHeader, "bytes=5000-");
    }
}
