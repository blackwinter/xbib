
package org.xbib.histogram;

import java.nio.ByteBuffer;

/**
 * A base class for all encodable (and decodable) histogram classes. Log readers and writers
 * will generally use this base class to provide common log processing across the integer value
 * based AbstractHistogram subclasses and the double value based DoubleHistogram class.
 */
public abstract class EncodableHistogram {

    static EncodableHistogram decodeFromByteBuffer(ByteBuffer buffer, final long minBarForHighestTrackableValue) {
        int cookie = buffer.getInt(buffer.position());
        if (DoubleHistogram.isDoubleHistogramCookie(cookie)) {
            return DoubleHistogram.decodeFromByteBuffer(buffer, minBarForHighestTrackableValue);
        } else {
            return Histogram.decodeFromByteBuffer(buffer, minBarForHighestTrackableValue);
        }
    }

    public abstract int encodeIntoByteBuffer(final ByteBuffer targetBuffer);

    public abstract int getNeededByteBufferCapacity();

    public abstract long getStartTimeStamp();

    public abstract void setStartTimeStamp(long startTimeStamp);

    public abstract long getEndTimeStamp();

    public abstract void setEndTimeStamp(long endTimestamp);

    public abstract double getMaxValueAsDouble();
}
