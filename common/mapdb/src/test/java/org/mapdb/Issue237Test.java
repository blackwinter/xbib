package org.mapdb;

import org.junit.Test;

import java.io.File;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.assertEquals;

public class Issue237Test {

    File file = Utils.tempDbFile();


    @Test
    public void testReopenAsync() throws InterruptedException {
        DB database = DBMaker.newFileDB(file).asyncWriteEnable().make();
        testQueue(database);

        database = DBMaker.newFileDB(file).asyncWriteEnable().make();
        testQueue(database);
    }

    @Test
    public void testReopenSync() throws InterruptedException {
        file.delete();

        DB database = DBMaker.newFileDB(file).make();
        testQueue(database);

        database = DBMaker.newFileDB(file).make();
        testQueue(database);
    }

    private void testQueue(DB database) throws InterruptedException {
        BlockingQueue<String> queue = database.getQueue("test-queue");
        queue.add("test-value");
        database.commit();
        assertEquals(queue.take(), "test-value");
        database.commit();
        database.close();
    }

}