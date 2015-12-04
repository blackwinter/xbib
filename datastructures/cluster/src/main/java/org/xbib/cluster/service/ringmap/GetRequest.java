package org.xbib.cluster.service.ringmap;

import org.xbib.cluster.OperationContext;
import org.xbib.cluster.Request;

class GetRequest<K, V> implements Request<AbstractRingMap, V> {
    private final K key;
    private AbstractRingMap ringMap;

    public GetRequest(AbstractRingMap ringMap, K key) {
        this.key = key;
        this.ringMap = ringMap;
    }

    @Override
    public void run(AbstractRingMap service, OperationContext ctx) {
        ctx.reply(service.getBucket(ringMap.getRing().findBucketId(key)).get(key));
    }
}
