package org.xbib.io.ftp.connectors;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

public interface DataConnector extends Closeable {

    Socket openDataConnection() throws IOException;
}
