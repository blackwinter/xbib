package org.xbib.cluster;

import java.util.Set;

public interface MembershipListener {
    default void memberAdded(Member member) {
    }

    default void memberRemoved(Member member) {
    }

    default void clusterMerged(Set<Member> newMembers) {
    }
}
