package org.xbib.io.sftp;

import java.io.IOException;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SFTPPosixFileAttributes extends SFTPBasicFileAttributes implements PosixFileAttributes {

    protected SFTPPosixFileAttributes(SFTPPath path) throws IOException {
        super(path);
    }

    public UserPrincipal owner() {
        return new SFTPUserPrincipal(this.attrs.getUId());
    }

    public GroupPrincipal group() {
        return new SFTPGroupPrincipal(this.attrs.getUId());
    }

    public Set<PosixFilePermission> permissions() {

        // The permission string in a list of PosixFilePermission
        List<PosixFilePermission> listPermissions = new ArrayList<PosixFilePermission>();
        listPermissions.add(null);
        listPermissions.add(PosixFilePermission.OWNER_READ);
        listPermissions.add(PosixFilePermission.OWNER_WRITE);
        listPermissions.add(PosixFilePermission.OWNER_EXECUTE);
        listPermissions.add(PosixFilePermission.GROUP_READ);
        listPermissions.add(PosixFilePermission.GROUP_WRITE);
        listPermissions.add(PosixFilePermission.GROUP_EXECUTE);
        listPermissions.add(PosixFilePermission.OTHERS_READ);
        listPermissions.add(PosixFilePermission.OTHERS_WRITE);
        listPermissions.add(PosixFilePermission.OTHERS_EXECUTE);

        // We get the permission string and we create it by looking up
        String permissionString = this.attrs.getPermissionsString();
        Set<PosixFilePermission> permissions = new HashSet<PosixFilePermission>();
        char nothing = "-".charAt(0);
        for (int i = 1; i < permissionString.length(); i++) {
            if (permissionString.charAt(i) != nothing) {
                permissions.add(listPermissions.get(i));
            }
        }

        return permissions;

    }

}
