package org.xbib.openurl.internal.descriptors;

import org.xbib.openurl.descriptors.PrivateData;

/**
 * Private data descriptors have a custom object.
 */
public class PrivateDataImpl implements PrivateData {

    private Object data;

    public PrivateDataImpl(Object data) {
        this.data = data;
    }

    @Override
    public Object getPrivateData() {
        return data;
    }

    @Override
    public String toString() {
        return data != null ? data.toString() : null;
    }
}
