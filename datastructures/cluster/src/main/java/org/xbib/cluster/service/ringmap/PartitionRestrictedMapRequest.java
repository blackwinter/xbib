package org.xbib.cluster.service.ringmap;

import org.xbib.cluster.Request;

public interface PartitionRestrictedMapRequest<C extends AbstractRingMap, V> extends Request<C, V> {
    int getPartition(AbstractRingMap service);
}
