
package org.xbib.io.archive.cpio;

import org.xbib.io.Connection;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Cpio connection
 */
public class CpioConnection extends URLConnection implements Connection<CpioSession> {

    private CpioSession session;

    private Path path;

    private OpenOption option;

    /**
     * Constructs a URL connection to the specified URL. A connection to
     * the object referenced by the URL is not created.
     *
     * @param url the specified URL.
     */
    public CpioConnection(URL url) throws URISyntaxException {
        super(url);
        this.path = Paths.get(url.toURI().getSchemeSpecificPart());
        this.option = StandardOpenOption.READ;
    }

    @Override
    public void connect() throws IOException {
        this.session = createSession();
    }

    public void setPath(Path path, OpenOption option) {
        this.path = path;
        this.option = option;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public CpioSession createSession() throws IOException {
        CpioSession session = new CpioSession();
        session.setPath(path, option);
        return session;
    }

    @Override
    public void close() throws IOException {
        session.close();
    }
}
