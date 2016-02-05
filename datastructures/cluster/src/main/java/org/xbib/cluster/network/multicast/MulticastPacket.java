package org.xbib.cluster.network.multicast;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.xbib.cluster.Member;
import org.xbib.cluster.operation.Operation;

public class MulticastPacket implements KryoSerializable {
    public Operation data;
    public Member sender;

    public MulticastPacket(Operation data, Member sender) {
        this.data = data;
        this.sender = sender;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        kryo.writeClassAndObject(output, data);
        kryo.writeObject(output, sender);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        data = (Operation) kryo.readClassAndObject(input);
        sender = kryo.readObject(input, Member.class);
    }
}
