package org.asynchttpclient.async.util;

import static org.testng.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.asynchttpclient.async.HostnameVerifierTest;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.security.authentication.DigestAuthenticator;
import org.eclipse.jetty.security.authentication.LoginAuthenticator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class TestUtils {

    public static final String USER = "user";
    public static final String ADMIN = "admin";
    public static final String TEXT_HTML_CONTENT_TYPE_WITH_UTF_8_CHARSET = "text/html;charset=utf-8";
    public static final String TEXT_HTML_CONTENT_TYPE_WITH_ISO_8859_1_CHARSET = "text/html; charset=ISO-8859-1";
    private static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir"), "ahc-tests-" + UUID.randomUUID().toString().substring(0, 8));
    public static final byte[] PATTERN_BYTES = "FooBarBazQixFooBarBazQixFooBarBazQixFooBarBazQixFooBarBazQixFooBarBazQix".getBytes(Charset.forName("UTF-16"));
    public static final File LARGE_IMAGE_FILE;
    public static byte[] LARGE_IMAGE_BYTES;
    public static final File SIMPLE_TEXT_FILE;
    public static final String SIMPLE_TEXT_FILE_STRING;
    private static final LoginService LOGIN_SERVICE = new HashLoginService("MyRealm", "src/test/resources/realm.properties");

    static {
        try {
            TMP_DIR.mkdirs();
            TMP_DIR.deleteOnExit();
            LARGE_IMAGE_FILE = new File(TestUtils.class.getClassLoader().getResource("300k.png").toURI());
            LARGE_IMAGE_BYTES = readFileToByteArray(LARGE_IMAGE_FILE);
            SIMPLE_TEXT_FILE = new File(TestUtils.class.getClassLoader().getResource("SimpleTextFile.txt").toURI());
            SIMPLE_TEXT_FILE_STRING = readFileToString(SIMPLE_TEXT_FILE, "UTF-8");
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static synchronized int findFreePort() throws IOException {
        ServerSocket socket = null;

        try {
            socket = new ServerSocket(0);

            return socket.getLocalPort();
        } finally {
            if (socket != null)
                socket.close();
        }
    }

    public static File createTempFile(int approxSize) throws IOException {
        long repeats = approxSize / TestUtils.PATTERN_BYTES.length + 1;
        File tmpFile = File.createTempFile("tmpfile-", ".data", TMP_DIR);
        tmpFile.deleteOnExit();
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(tmpFile);
            for (int i = 0; i < repeats; i++) {
                out.write(PATTERN_BYTES);
            }

            long expectedFileSize = PATTERN_BYTES.length * repeats;
            assertEquals(tmpFile.length(), expectedFileSize, "Invalid file length");

            return tmpFile;
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static Server newJettyHttpServer(int port) {
        Server server = new Server();
        addHttpConnector(server, port);
        return server;
    }

    public static void addHttpConnector(Server server, int port) {
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);

        server.addConnector(connector);
    }

    public static Server newJettyHttpsServer(int port) throws URISyntaxException {
        Server server = new Server();
        addHttpsConnector(server, port);
        return server;
    }

    public static void addHttpsConnector(Server server, int port) throws URISyntaxException {
        ClassLoader cl = TestUtils.class.getClassLoader();

        URL keystoreUrl = cl.getResource("ssltest-keystore.jks");
        String keyStoreFile = new File(keystoreUrl.toURI()).getAbsolutePath();
        SslContextFactory sslContextFactory = new SslContextFactory(keyStoreFile);
        sslContextFactory.setKeyStorePassword("changeit");

        String trustStoreFile = new File(cl.getResource("ssltest-cacerts.jks").toURI()).getAbsolutePath();
        sslContextFactory.setTrustStorePath(trustStoreFile);
        sslContextFactory.setTrustStorePassword("changeit");

        HttpConfiguration httpsConfig = new HttpConfiguration();
        httpsConfig.setSecureScheme("https");
        httpsConfig.setSecurePort(port);
        httpsConfig.addCustomizer(new SecureRequestCustomizer());

        ServerConnector connector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(httpsConfig));
        connector.setPort(port);
        server.addConnector(connector);

        server.addConnector(connector);
    }

    public static void addBasicAuthHandler(Server server, boolean strict, Handler handler) {
        addAuthHandler(server, Constraint.__BASIC_AUTH, new BasicAuthenticator(), strict, handler);
    }

    public static void addDigestAuthHandler(Server server, boolean strict, Handler handler) {
        addAuthHandler(server, Constraint.__DIGEST_AUTH, new DigestAuthenticator(), strict, handler);
    }

    private static void addAuthHandler(Server server, String auth, LoginAuthenticator authenticator, boolean strict, Handler handler) {

        server.addBean(LOGIN_SERVICE);

        Constraint constraint = new Constraint();
        constraint.setName(auth);
        constraint.setRoles(new String[] { USER, ADMIN });
        constraint.setAuthenticate(true);

        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setConstraint(constraint);
        mapping.setPathSpec("/*");

        Set<String> knownRoles = new HashSet<String>();
        knownRoles.add(USER);
        knownRoles.add(ADMIN);

        List<ConstraintMapping> cm = new ArrayList<ConstraintMapping>();
        cm.add(mapping);

        ConstraintSecurityHandler security = new ConstraintSecurityHandler();
        security.setConstraintMappings(cm, knownRoles);
        security.setAuthenticator(authenticator);
        security.setLoginService(LOGIN_SERVICE);
        //security.setStrict(strict);
        security.setHandler(handler);
        server.setHandler(security);
    }

    public static SSLContext createSSLContext(AtomicBoolean trust) {
        try {
            InputStream keyStoreStream = HostnameVerifierTest.class.getResourceAsStream("ssltest-cacerts.jks");
            char[] keyStorePassword = "changeit".toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(keyStoreStream, keyStorePassword);

            // Set up key manager factory to use our key store
            char[] certificatePassword = "changeit".toCharArray();
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, certificatePassword);

            // Initialize the SSLContext to work with our key managers.
            KeyManager[] keyManagers = kmf.getKeyManagers();
            TrustManager[] trustManagers = new TrustManager[] { dummyTrustManager(trust) };
            SecureRandom secureRandom = new SecureRandom();

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, secureRandom);

            return sslContext;
        } catch (Exception e) {
            throw new Error("Failed to initialize the server-side SSLContext", e);
        }
    }

    private static final TrustManager dummyTrustManager(final AtomicBoolean trust) {
        return new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                if (!trust.get()) {
                    throw new CertificateException("Server certificate not trusted.");
                }
            }
        };
    }

    public static File getClasspathFile(String file) throws FileNotFoundException {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
        }
        if (cl == null) {
            cl = TestUtils.class.getClassLoader();
        }
        URL resourceUrl = cl.getResource(file);

        try {
            return new File(new URI(resourceUrl.toString()).getSchemeSpecificPart());
        } catch (URISyntaxException e) {
            throw new FileNotFoundException(file);
        }
    }

    public static String readFileToString(
            File file, String encoding) throws IOException {
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            return toString(in, encoding);
        } finally {
            closeQuietly(in);
        }
    }

    public static String toString(InputStream input, String encoding)
            throws IOException {
        StringWriter sw = new StringWriter();
        copy(input, sw, encoding);
        return sw.toString();
    }

    public static void copy(InputStream input, Writer output, String encoding)
            throws IOException {
        if (encoding == null) {
            copy(input, output);
        } else {
            InputStreamReader in = new InputStreamReader(input, encoding);
            copy(in, output);
        }
    }

    public static void copy(InputStream input, Writer output)
            throws IOException {
        InputStreamReader in = new InputStreamReader(input);
        copy(in, output);
    }

    public static int copy(Reader input, Writer output) throws IOException {
        char[] buffer = new char[8192];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static void closeQuietly(Reader input) {
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    public static void closeQuietly(InputStream input) {
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    public static int copy(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[8192];
        int count = 0;
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static byte[] readFileToByteArray(File file) throws IOException {
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            return toByteArray(in);
        } finally {
            closeQuietly(in);
        }
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

}
