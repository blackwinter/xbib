package org.xbib.openurl.internal.entities;

import org.xbib.openurl.descriptors.Descriptor;
import org.xbib.openurl.entities.Resolver;

import java.util.List;

/**
 * The resource at which a service request pertaining to
 * the <em>Referent</em> is targeted; an <em>Entity</em>
 * of the <em>ContextObject</em>.
 */
public class ResolverImpl extends EntityImpl implements Resolver {

    /**
     * Construct a Resolver.
     *
     * @param descriptors Descriptor(s) for the Resolver
     */
    public ResolverImpl(List<Descriptor> descriptors) {
        super(descriptors);
    }
}
