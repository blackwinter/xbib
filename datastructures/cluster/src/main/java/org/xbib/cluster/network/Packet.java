package org.xbib.cluster.network;

public class Packet {
    public final int sequence;
    public final Object data;
    public int service;

    public Packet(int sequence, Object data, int service) {
        this.sequence = sequence;
        this.data = data;
        this.service = service;
    }

    public Packet(Object data, int service) {
        this.sequence = -1;
        this.data = data;
        this.service = service;
    }

    public Object getData() {
        return data;
    }

    public Object getService() {
        return service;
    }

    @Override
    public String toString() {
        return "Packet{" + "sequence=" + sequence + ", data=" + data + '}';
    }
}
