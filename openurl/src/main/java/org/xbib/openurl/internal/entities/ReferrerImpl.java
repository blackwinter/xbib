package org.xbib.openurl.internal.entities;

import org.xbib.openurl.descriptors.Descriptor;
import org.xbib.openurl.entities.Referrer;

import java.util.List;

/**
 * The resource that generates the ContextObject; an <em>Entity</em>
 * of the <em>ContextObject</em>.
 */
public class ReferrerImpl extends EntityImpl<Descriptor> implements Referrer {

    /**
     * Constructs a Referrer.
     *
     * @param descriptors Descriptor(s) for the Referrer.
     */
    public ReferrerImpl(List<Descriptor> descriptors) {
        super(descriptors);
    }
}
