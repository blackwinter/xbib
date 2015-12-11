
package org.xbib.io;

import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class ConnectionServiceTest {

    @Test
    public void testFileTmp() throws Exception {
        ConnectionService.getInstance()
                .getConnectionFactory(URI.create("file:dummy"))
                .getConnection(URI.create("file:///tmp"));
    }

    @Test(expected = java.util.ServiceConfigurationError.class)
    public void testUnkownScheme() throws URISyntaxException, IOException {
        URI uri = URI.create("unknownscheme://localhost");
        ConnectionService.getInstance()
                .getConnectionFactory(uri)
                .getConnection(uri);
    }

}
