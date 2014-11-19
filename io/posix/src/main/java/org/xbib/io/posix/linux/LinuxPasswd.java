package org.xbib.io.posix.linux;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import org.xbib.io.posix.Passwd;

import java.util.List;

public class LinuxPasswd extends Structure implements Passwd {
    public String pw_name;   // user name
    public String pw_passwd; // password (encrypted)
    public int pw_uid;       // user id
    public int pw_gid;       // user id
    public String pw_gecos;  // login info
    public String pw_dir;    // home directory
    public String pw_shell;  // default shell

    LinuxPasswd(Pointer memory) {
        useMemory(memory);
        read();
    }

    @Override
    protected List getFieldOrder() {
        return null;
    }

    public String getAccessClass() {
        return "";
    }

    public String getGECOS() {
        return pw_gecos;
    }

    public long getGID() {
        return pw_gid;
    }

    public String getHome() {
        return pw_dir;
    }

    public String getLoginName() {
        return pw_name;
    }

    public String getPassword() {
        return pw_passwd;
    }

    public String getShell() {
        return pw_shell;
    }

    public long getUID() {
        return pw_uid;
    }

    public int getPasswdChangeTime() {
        return 0;
    }

    public int getExpire() {
        return Integer.MAX_VALUE;
    }
}
