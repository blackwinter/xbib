package org.xbib.io.http.client.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpMessage;
import org.testng.annotations.Test;
import org.xbib.io.http.client.AbstractBasicTest;
import org.xbib.io.http.client.AsyncHttpClient;
import org.xbib.io.http.client.AsyncHttpClientConfig;
import org.xbib.io.http.client.Response;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.config;
import static org.xbib.io.http.client.Dsl.get;

public class EventPipelineTest extends AbstractBasicTest {

    @Test(groups = "standalone")
    public void asyncPipelineTest() throws Exception {

        AsyncHttpClientConfig.AdditionalChannelInitializer httpAdditionalPipelineInitializer = new AsyncHttpClientConfig.AdditionalChannelInitializer() {
            public void initChannel(Channel channel) throws Exception {
                channel.pipeline().addBefore("inflater", "copyEncodingHeader", new CopyEncodingHandler());
            }
        };

        try (AsyncHttpClient p = asyncHttpClient(config().setHttpAdditionalChannelInitializer(httpAdditionalPipelineInitializer))) {
            final CountDownLatch l = new CountDownLatch(1);
            p.executeRequest(get(getTargetUrl()), new AsyncCompletionHandlerAdapter() {
                @Override
                public Response onCompleted(Response response) throws Exception {
                    try {
                        assertEquals(response.getStatusCode(), 200);
                        assertEquals(response.getHeader("X-Original-Content-Encoding"), "<original encoding>");
                    } finally {
                        l.countDown();
                    }
                    return response;
                }
            }).get();
            if (!l.await(TIMEOUT, TimeUnit.SECONDS)) {
                fail("Timeout out");
            }
        }
    }

    private static class CopyEncodingHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object e) {
            if (e instanceof HttpMessage) {
                HttpMessage m = (HttpMessage) e;
                // for test there is no Content-Encoding header so just hard
                // coding value
                // for verification
                m.headers().set("X-Original-Content-Encoding", "<original encoding>");
            }
            ctx.fireChannelRead(e);
        }
    }
}
