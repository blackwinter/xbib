package org.xbib.io.http.client;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import static org.xbib.io.http.client.Dsl.asyncHttpClient;
import static org.xbib.io.http.client.Dsl.config;

/**
 * Simple stress test for exercising the follow redirect.
 */
public class FollowingThreadTest extends AbstractBasicTest {

    private static final int COUNT = 10;

    @Test(groups = "online", timeOut = 30 * 1000)
    public void testFollowRedirect() throws IOException, ExecutionException, TimeoutException, InterruptedException {

        final CountDownLatch countDown = new CountDownLatch(COUNT);
        ExecutorService pool = Executors.newCachedThreadPool();
        try {
            for (int i = 0; i < COUNT; i++) {
                pool.submit(new Runnable() {

                    private int status;

                    public void run() {
                        final CountDownLatch l = new CountDownLatch(1);
                        try (AsyncHttpClient ahc = asyncHttpClient(config().setFollowRedirect(true))) {
                            ahc.prepareGet("http://www.google.com/").execute(new AsyncHandler<Integer>() {

                                public void onThrowable(Throwable t) {
                                    t.printStackTrace();
                                }

                                public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
                                    System.out.println(new String(bodyPart.getBodyPartBytes()));
                                    return State.CONTINUE;
                                }

                                public State onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
                                    status = responseStatus.getStatusCode();
                                    System.out.println(responseStatus.getStatusText());
                                    return State.CONTINUE;
                                }

                                public State onHeadersReceived(HttpResponseHeaders headers) throws Exception {
                                    return State.CONTINUE;
                                }

                                public Integer onCompleted() throws Exception {
                                    l.countDown();
                                    return status;
                                }
                            });

                            l.await();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            countDown.countDown();
                        }
                    }
                });
            }
            countDown.await();
        } finally {
            pool.shutdown();
        }
    }
}
