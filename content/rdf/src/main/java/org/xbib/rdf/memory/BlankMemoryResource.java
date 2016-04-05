package org.xbib.rdf.memory;

import org.xbib.iri.IRI;

import java.util.concurrent.atomic.AtomicLong;

public class BlankMemoryResource extends MemoryResource {

    private final static AtomicLong nodeID = new AtomicLong(0L);

    // for test
    public static void reset() {
        nodeID.set(0L);
    }

    // for test
    public static long next() {
        return nodeID.incrementAndGet();
    }

    public BlankMemoryResource() {
        this(IRI.builder().curie(GENID, "b" + next()).build());
    }

    public BlankMemoryResource(String id) {
        this(id != null && id.startsWith(PLACEHOLDER) ?
                IRI.builder().curie(id).build() : IRI.builder().curie(GENID, id).build());
    }

    public BlankMemoryResource(IRI id) {
        super(id);
    }

}
