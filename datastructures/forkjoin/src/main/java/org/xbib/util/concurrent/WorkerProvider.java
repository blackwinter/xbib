
package org.xbib.util.concurrent;

public interface WorkerProvider<W extends Worker> {

    W get(Pipeline pipeline);

}
