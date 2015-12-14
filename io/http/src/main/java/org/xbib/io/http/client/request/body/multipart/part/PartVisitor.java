package org.xbib.io.http.client.request.body.multipart.part;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public interface PartVisitor {

    void withBytes(byte[] bytes);

    void withByte(byte b);

    class CounterPartVisitor implements PartVisitor {

        private int count = 0;

        @Override
        public void withBytes(byte[] bytes) {
            count += bytes.length;
        }

        @Override
        public void withByte(byte b) {
            count++;
        }

        public int getCount() {
            return count;
        }
    }

    class ByteBufferVisitor implements PartVisitor {

        private final ByteBuffer target;

        public ByteBufferVisitor(ByteBuffer target) {
            this.target = target;
        }

        @Override
        public void withBytes(byte[] bytes) {
            target.put(bytes);
        }

        @Override
        public void withByte(byte b) {
            target.put(b);
        }
    }

    class ByteBufVisitor implements PartVisitor {
        private final ByteBuf target;

        public ByteBufVisitor(ByteBuf target) {
            this.target = target;
        }

        @Override
        public void withBytes(byte[] bytes) {
            target.writeBytes(bytes);
        }

        @Override
        public void withByte(byte b) {
            target.writeByte(b);
        }
    }
}
