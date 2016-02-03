package org.xbib.io.ftp.principals;

import java.io.IOException;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;

public final class DummyPrincipleLookupService extends UserPrincipalLookupService {
    @Override
    public UserPrincipal lookupPrincipalByName(String name) throws IOException {
        return null;
    }

    @Override
    public GroupPrincipal lookupPrincipalByGroupName(String group) throws IOException {
        return null;
    }
}
