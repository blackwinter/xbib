package org.xbib.io.jdbc.pool.tomcat;

public class XADataSource extends DataSource implements javax.sql.XADataSource {

    /**
     * Constructor for reflection only. A default set of pool properties will be created.
     */
    public XADataSource() {
        super();
    }

    /**
     * Constructs a DataSource object wrapping a connection
     *
     * @param poolProperties
     */
    public XADataSource(PoolConfiguration poolProperties) {
        super(poolProperties);
    }

}
