package org.xbib.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * This is a handle (a connection) to some resource, which may
 * be a class, native library, text file, image, etc. Handles are returned
 * by a ResourceFinder. A resource handle allows easy access to the resource data
 * (using methods {@link #getInputStream} or {@link #getBytes}) as well as
 * access resource metadata, such as attributes, certificates, etc.
 * As soon as the handle is no longer in use, it should be explicitly
 * {@link #close}d, similarly to I/O streams.
 */
public interface ResourceHandle {
    /**
     * Return the name of the resource. The name is a "/"-separated path
     * name that identifies the resource.
     *
     * @return the name
     */
    String getName();

    /**
     * Returns the URL of the resource.
     *
     * @return the URL
     */
    URL getUrl();

    /**
     * Does this resource refer to a directory.  Directory resources are commly used
     * as the basis for a URL in client application.  A directory resource has 0 bytes for it's content.
     *
     * @return true if directory
     */
    boolean isDirectory();

    /**
     * Returns the code source URL for the class or resource.
     *
     * @return the code source URL
     */
    URL getCodeSourceUrl();

    /**
     * Returns and InputStream for reading this resource data.
     *
     * @return the input stream
     * @throws IOException if method fails
     */
    InputStream getInputStream() throws IOException;

    /**
     * Returns the length of this resource data, or -1 if unknown.
     *
     * @return the content length
     */
    int getContentLength();

    /**
     * Returns this resource data as an array of bytes.
     *
     * @return the bytes
     * @throws IOException if method fails
     */
    byte[] getBytes() throws IOException;

    /**
     * Returns the Manifest of the JAR file from which this resource
     * was loaded, or null if none.
     *
     * @return the manifest
     * @throws IOException if method fails
     */
    Manifest getManifest() throws IOException;

    /**
     * Return the Certificates of the resource, or null if none.
     *
     * @return the certificates
     */
    Certificate[] getCertificates();

    /**
     * Return the Attributes of the resource, or null if none.
     *
     * @return the attributes
     * @throws IOException if method fails
     */
    Attributes getAttributes() throws IOException;

    /**
     * Closes a connection to the resource indentified by this handle. Releases
     * any I/O objects associated with the handle.
     */
    void close();
}
