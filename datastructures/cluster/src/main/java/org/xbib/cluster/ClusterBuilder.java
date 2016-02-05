package org.xbib.cluster;

import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.cluster.service.ServiceListBuilder;
import org.xbib.cluster.service.joiner.JoinerService;
import org.xbib.cluster.transport.TransportConstructor;
import org.xbib.cluster.transport.NettyTransport;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;

public class ClusterBuilder {

    private final static Logger logger = LogManager.getLogger(ClusterBuilder.class);

    private Collection<Member> members;
    private ImmutableList<ServiceListBuilder.Constructor> services;
    private InetSocketAddress serverAddress;
    private boolean mustJoinCluster;
    private boolean client = false;
    private JoinerService joinerService;
    private TransportConstructor transport;

    public ClusterBuilder members(Collection<Member> members) {
        this.members = members;
        return this;
    }

    public Collection<Member> members() {
        return members;
    }

    public ClusterBuilder joinStrategy(JoinerService joinerService) {
        this.joinerService = joinerService;
        return this;
    }

    public JoinerService joinStrategy() {
        return joinerService;
    }

    public ClusterBuilder transport(TransportConstructor transport) {
        this.transport = transport;
        return this;
    }

    public TransportConstructor transport() {
        return transport;
    }

    public ClusterBuilder services(ImmutableList<ServiceListBuilder.Constructor> services) {
        this.services = services;
        return this;
    }

    public boolean mustJoinCluster() {
        return mustJoinCluster;
    }

    public boolean client() {
        return client;
    }

    public ClusterBuilder client(boolean client) {
        this.client = client;
        return this;
    }

    public ClusterBuilder mustJoinCluster(boolean join) {
        mustJoinCluster = join;
        return this;
    }

    public ImmutableList<ServiceListBuilder.Constructor> services() {
        return services;
    }

    public ClusterBuilder serverAddress(InetSocketAddress serverAddress) {
        this.serverAddress = serverAddress;
        return this;
    }

    public ClusterBuilder serverAddress(String host, int port) {
        this.serverAddress = new InetSocketAddress(host, port);
        return this;
    }

    public InetSocketAddress serverAddress() {
        return serverAddress;
    }

    public Cluster start() {
        if (members == null) {
            members = new ArrayList<>();
        }
        if (serverAddress == null) {
            //serverAddress = new InetSocketAddress(NetworkUtil.getDefaultAddress(), 0);
            serverAddress = new InetSocketAddress("localhost", 5001);
            logger.debug("no server address given, using default {}", serverAddress);
        }
        if (transport == null) {
            transport = NettyTransport::new;
        }
        if (services == null) {
            services = ImmutableList.of();
        }
        return new Cluster(members, services, transport, serverAddress, joinerService, mustJoinCluster, client);
    }
}
