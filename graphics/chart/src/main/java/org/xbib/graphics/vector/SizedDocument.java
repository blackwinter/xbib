package org.xbib.graphics.vector;

import org.xbib.graphics.vector.util.PageSize;

public abstract class SizedDocument implements Document {
    private final PageSize pageSize;

    public SizedDocument(PageSize pageSize) {
        this.pageSize = pageSize;
    }

    public PageSize getPageSize() {
        return pageSize;
    }

    public void close() {
    }
}

