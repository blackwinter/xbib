package org.xbib.openurl.internal.entities;

import org.xbib.openurl.descriptors.Descriptor;
import org.xbib.openurl.entities.Requester;

import java.util.List;

/**
 * The resource that requests services pertaining to the
 * Referent; an <em>Entity</em> of the <em>ContextObject</em>.
 */
public class RequesterImpl extends EntityImpl implements Requester {

    /**
     * Construct a Requester.
     *
     * @param descriptors Descriptor(s) for the Requester.
     */
    public RequesterImpl(List<Descriptor> descriptors) {
        super(descriptors);
    }
}
