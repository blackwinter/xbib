package org.xbib.io.http.client.ws;

/**
 * Invoked when an {@link org.asynchttpclient.AsyncHandler.State#UPGRADE} is returned. Currently the
 * library only support {@link WebSocket} as type.
 *
 * @param <T> the result type
 */
public interface UpgradeHandler<T> {

    /**
     * If the HTTP Upgrade succeed (response's status code equals 101), the
     * {@link org.asynchttpclient.AsyncHttpClient} will invoke that method.
     *
     * @param t an Upgradable entity
     */
    void onSuccess(T t);

    /**
     * If the upgrade fail.
     *
     * @param t a {@link Throwable}
     */
    void onFailure(Throwable t);
}
