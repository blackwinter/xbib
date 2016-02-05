package org.xbib.io.sftp;

import java.nio.file.attribute.GroupPrincipal;

public class SFTPGroupPrincipal extends SFTPUserPrincipal implements GroupPrincipal {

    protected SFTPGroupPrincipal(int uId) {
        super(uId);
    }

}
