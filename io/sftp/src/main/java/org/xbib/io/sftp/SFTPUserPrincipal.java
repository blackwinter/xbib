package org.xbib.io.sftp;

import java.nio.file.attribute.UserPrincipal;

public class SFTPUserPrincipal extends SFTPPrincipal implements UserPrincipal {

    protected SFTPUserPrincipal(int uId) {
        super(uId);
    }

}
