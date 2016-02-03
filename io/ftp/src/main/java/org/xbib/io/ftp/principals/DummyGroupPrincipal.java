package org.xbib.io.ftp.principals;

import java.nio.file.attribute.GroupPrincipal;

public final class DummyGroupPrincipal
        implements GroupPrincipal {
    @Override
    public String getName() {
        return null;
    }
}
