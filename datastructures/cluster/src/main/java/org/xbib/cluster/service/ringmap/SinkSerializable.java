package org.xbib.cluster.service.ringmap;

import com.google.common.hash.PrimitiveSink;

public interface SinkSerializable {
    void writeTo(PrimitiveSink sink);
}
