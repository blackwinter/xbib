package org.ghost4j;

import com.sun.jna.Native;
import com.sun.jna.Platform;

class GhostscriptLibraryLoader {

    static GhostscriptLibrary loadLibrary() {
        String libName = "gs";
        if (Platform.isWindows()) {
            libName = "gsdll" + System.getProperty("sun.arch.data.model");
        }
        return (GhostscriptLibrary) Native.loadLibrary(libName, GhostscriptLibrary.class);
    }
}
