package org.xbib.openurl.internal;

import org.xbib.openurl.ContextObject;
import org.xbib.openurl.OpenURLRequest;

/**
 * OpenURLRequest implementation.
 */
public class InternalOpenURLRequest implements OpenURLRequest {

    private ContextObject[] contextObjects;

    public InternalOpenURLRequest(ContextObject[] contextObjects) {
        this.contextObjects = contextObjects;
    }

    @Override
    public ContextObject[] getContextObjects() {
        return contextObjects;
    }
}
