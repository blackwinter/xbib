
package org.xbib.util.concurrent;

import java.io.IOException;

public interface Sink<R extends WorkerRequest> {

    void sink(R request) throws IOException;
}
