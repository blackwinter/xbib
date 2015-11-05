package org.xbib.query.cql;

/**
 * Abstract syntax tree of CQL, the sort specification
 */
public class SortSpec extends AbstractNode {

    private SortSpec sortspec;
    private SingleSpec spec;

    public SortSpec(SortSpec sortspec, SingleSpec spec) {
        this.sortspec = sortspec;
        this.spec = spec;
    }

    public SortSpec(SingleSpec spec) {
        this.spec = spec;
    }

    public SortSpec getSortSpec() {
        return sortspec;
    }

    public SingleSpec getSingleSpec() {
        return spec;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return (sortspec != null ? sortspec + " " : "") + spec;
    }

}
