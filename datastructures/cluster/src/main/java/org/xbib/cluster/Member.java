package org.xbib.cluster;

import com.esotericsoftware.kryo.NotNull;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import org.xbib.cluster.serialize.kryo.InetSocketAddressSerializer;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Member {
    @NotNull
    @FieldSerializer.Bind(InetSocketAddressSerializer.class)
    private final InetSocketAddress address;
    private final boolean client;

    /*public Member(InetSocketAddress address) {
        this.address = address;
        client = false;
    }*/

    /*public Member(InetSocketAddress address, boolean client) {
        this.address = address;
        this.client = client;
    }*/

    public Member(String host, int port) {
        this(host, port, false);
    }

    public Member(String host, int port, boolean isClient) {
        address = new InetSocketAddress(host, port);
        client = isClient;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public boolean isClient() {
        return client;
    }

    @Override
    public String toString() {
        return "Member{" + "address=" + address + ", client=" + client + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Member)) {
            return false;
        }
        Member member = (Member) o;
        return address.equals(member.address);

    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }
}
