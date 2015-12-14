package org.xbib.io.http.client;

public class BoundRequestBuilder extends RequestBuilderBase<BoundRequestBuilder> {

    private final AsyncHttpClient client;

    public BoundRequestBuilder(AsyncHttpClient client, String method, boolean isDisableUrlEncoding, boolean validateHeaders) {
        super(method, isDisableUrlEncoding, validateHeaders);
        this.client = client;
    }

    public BoundRequestBuilder(AsyncHttpClient client, String method, boolean isDisableUrlEncoding) {
        super(method, isDisableUrlEncoding);
        this.client = client;
    }

    public BoundRequestBuilder(AsyncHttpClient client, Request prototype) {
        super(prototype);
        this.client = client;
    }

    public <T> ListenableFuture<T> execute(AsyncHandler<T> handler) {
        return client.executeRequest(build(), handler);
    }

    public ListenableFuture<Response> execute() {
        return client.executeRequest(build(), new AsyncCompletionHandlerBase());
    }
}
