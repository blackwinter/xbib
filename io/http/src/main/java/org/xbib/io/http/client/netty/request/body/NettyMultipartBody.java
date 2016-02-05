package org.xbib.io.http.client.netty.request.body;

import io.netty.handler.codec.http.HttpHeaders;
import org.xbib.io.http.client.AsyncHttpClientConfig;
import org.xbib.io.http.client.request.body.multipart.MultipartBody;
import org.xbib.io.http.client.request.body.multipart.Part;

import java.util.List;

import static org.xbib.io.http.client.request.body.multipart.MultipartUtils.newMultipartBody;

public class NettyMultipartBody extends NettyBodyBody {

    private final String contentType;

    public NettyMultipartBody(List<Part> parts, HttpHeaders headers, AsyncHttpClientConfig config) {
        this(newMultipartBody(parts, headers), config);
    }

    private NettyMultipartBody(MultipartBody body, AsyncHttpClientConfig config) {
        super(body, config);
        contentType = body.getContentType();
    }

    @Override
    public String getContentType() {
        return contentType;
    }
}
