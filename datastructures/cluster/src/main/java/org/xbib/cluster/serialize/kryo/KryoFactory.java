package org.xbib.cluster.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.xbib.cluster.operation.heartbeat.HeartbeatOperation;
import org.xbib.cluster.Member;
import org.xbib.cluster.service.ringmap.ConsistentHashRing;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class KryoFactory {
    private static final Class<?>[] REG_CLASSES = {
            Collections.unmodifiableList(new ArrayList()).getClass(),
            HeartbeatOperation.class,
            Member.class,
            InetSocketAddress.class,
            ConsistentHashRing.class,
            ConsistentHashRing.Bucket.class,
    };

    private static final Map<Class, com.esotericsoftware.kryo.Serializer> SERIALIZERS = new HashMap<Class, com.esotericsoftware.kryo.Serializer>() {
        {
            put(InetSocketAddress.class, new InetSocketAddressSerializer());
            put(Collections.unmodifiableList(new ArrayList()).getClass(), new UnmodifiableCollectionsSerializer());
            put(Collections.unmodifiableSet(new HashSet<>()).getClass(), new UnmodifiableCollectionsSerializer());
        }
    };

    private static final ThreadLocal<Kryo> kryos = new ThreadLocal<Kryo>() {
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            UnmodifiableCollectionsSerializer.registerSerializers(kryo);
            for (Class<?> clazz : REG_CLASSES) {
                com.esotericsoftware.kryo.Serializer serializer = SERIALIZERS.get(clazz);
                if (serializer == null) {
                    kryo.register(clazz);
                } else {
                    kryo.register(clazz, serializer);
                }
            }
            kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
            return kryo;
        }
    };

    public static Kryo getKryoInstance() {
        return kryos.get();
    }
}
