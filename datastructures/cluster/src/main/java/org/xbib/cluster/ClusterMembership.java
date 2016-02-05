package org.xbib.cluster;

public interface ClusterMembership {
    void addMember(Member member);

    void removeMember(Member member);
}
