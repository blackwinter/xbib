package org.xbib.util.concurrent;

import org.junit.Assert;
import org.junit.Test;

public class ExceptionForkJoinPipelineTest extends Assert {

    @Test
    public void testForkJoinTwoWorkersWithException() throws Exception {
        ExceptionMockWorker worker = new ExceptionMockWorker();
        try {
            worker.bootstrap();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // 1 (on slow machines) or 2 exceptions
        assertTrue(worker.getPipeline().getWorkerErrors().getThrowables().size() > 0);
    }

}
