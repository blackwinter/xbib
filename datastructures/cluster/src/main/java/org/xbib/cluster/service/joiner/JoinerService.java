
package org.xbib.cluster.service.joiner;

import org.xbib.cluster.ClusterMembership;

public interface JoinerService {
    void onStart(ClusterMembership membership);

    default void onClose() {
    }
}
