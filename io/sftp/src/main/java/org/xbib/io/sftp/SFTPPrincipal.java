package org.xbib.io.sftp;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Set;

public class SFTPPrincipal implements Principal {

    private final int uId;

    protected SFTPPrincipal(int uId) {
        this.uId = uId;
    }

    public String getName() {
        return String.valueOf(uId);
    }

    public boolean implies(Subject subject) {
        if (subject != null) {
            Set<Principal> principals = subject.getPrincipals();
            return principals.contains(this);
        } else {
            return false;
        }

    }
}
