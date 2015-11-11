
package org.xbib.util.concurrent;

public interface WorkerRequest<E> {

    E get();

    WorkerRequest<E> set(E e);

}
