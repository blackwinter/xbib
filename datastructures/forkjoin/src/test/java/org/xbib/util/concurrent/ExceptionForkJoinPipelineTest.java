package org.xbib.util.concurrent;

import org.junit.Assert;
import org.junit.Test;

public class ExceptionForkJoinPipelineTest extends Assert {

    @Test
    public void testForkJoinWithException() throws Exception {
        ExceptionMockWorker worker = new ExceptionMockWorker();
        try {
            worker.bootstrap();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assertEquals(2, worker.getPipeline().getWorkerErrors().getThrowables().size());
    }

}
