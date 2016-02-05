package org.xbib.cluster.operation;

import org.xbib.cluster.Request;
import org.xbib.cluster.service.Service;

public interface Operation<T extends Service> extends Request<T, Void> {
}
