package org.xbib.io.redis.protocol;

/**
 * Interface for protocol keywords providing an encoded representation.
 */
public interface ProtocolKeyword {

    /**
     * @return byte[] encoded representation.
     */
    byte[] getBytes();
}
