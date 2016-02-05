package org.xbib.io.ftp.principals;

import java.nio.file.attribute.UserPrincipal;

public final class DummyUserPrincipal implements UserPrincipal {
    @Override
    public String getName() {
        return null;
    }
}
