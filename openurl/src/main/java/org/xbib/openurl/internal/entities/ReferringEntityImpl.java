package org.xbib.openurl.internal.entities;

import org.xbib.openurl.descriptors.Descriptor;
import org.xbib.openurl.entities.ReferringEntity;

import java.util.List;

/**
 * The resource that references the Referent; an <em>Entity</em>
 * of the <em>ContextObject</em>.
 */
public class ReferringEntityImpl extends EntityImpl<Descriptor> implements ReferringEntity {

    /**
     * Constructs a ReferringEntity.
     *
     * @param descriptors Descriptor(s) for the ReferringEntity
     */
    public ReferringEntityImpl(List<Descriptor> descriptors) {
        super(descriptors);
    }
}
