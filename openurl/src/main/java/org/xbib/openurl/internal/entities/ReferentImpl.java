package org.xbib.openurl.internal.entities;

import org.xbib.openurl.descriptors.Descriptor;
import org.xbib.openurl.entities.Referent;

import java.util.List;

/**
 * Referent is a resource that is referenced on a network, and
 * about which the ContextObject is created; an <em>Entity</em>
 * of the <em>ContextObject</em>.
 */
public class ReferentImpl extends EntityImpl implements Referent {

    /**
     * Constructs a Referent.
     *
     * @param descriptors Descriptor(s) for the Referent.
     */
    public ReferentImpl(List<Descriptor> descriptors) {
        super(descriptors);
    }
}
