package org.xbib.io.http.client.handler;

import org.xbib.io.http.client.AsyncHandler;

/**
 * An extended {@link AsyncHandler} with two extra callback who get invoked during the content upload to a remote
 * server.
 * This {@link AsyncHandler} must be used only with PUT and POST request.
 */
public interface ProgressAsyncHandler<T> extends AsyncHandler<T> {

    /**
     * Invoked when the content (a {@link java.io.File}, {@link String} or {@link java.io.FileInputStream} has been
     * fully
     * written on the I/O socket.
     *
     * @return a {@link AsyncHandler.State} telling to CONTINUE or ABORT the current processing.
     */
    State onHeadersWritten();

    /**
     * Invoked when the content (a {@link java.io.File}, {@link String} or {@link java.io.FileInputStream} has been
     * fully
     * written on the I/O socket.
     *
     * @return a {@link AsyncHandler.State} telling to CONTINUE or ABORT the current processing.
     */
    State onContentWritten();

    /**
     * Invoked when the I/O operation associated with the {@link Request} body wasn't fully written in a single I/O
     * write
     * operation. This method is never invoked if the write operation complete in a sinfle I/O write.
     *
     * @param amount  The amount of bytes to transfer.
     * @param current The amount of bytes transferred
     * @param total   The total number of bytes transferred
     * @return a {@link AsyncHandler.State} telling to CONTINUE or ABORT the current processing.
     */
    State onContentWriteProgress(long amount, long current, long total);
}
