package org.xbib.io.http.client.spnego;

import java.io.IOException;

/**
 * Abstract SPNEGO token generator. Implementations should take an Kerberos ticket and transform
 * into a SPNEGO token.
 * Implementations of this interface are expected to be thread-safe.
 */
public interface SpnegoTokenGenerator {

    byte[] generateSpnegoDERObject(byte[] kerberosTicket) throws IOException;
}
