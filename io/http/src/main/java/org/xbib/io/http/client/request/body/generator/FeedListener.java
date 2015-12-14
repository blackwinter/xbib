package org.xbib.io.http.client.request.body.generator;

public interface FeedListener {
    void onContentAdded();

    void onError(Throwable t);
}
