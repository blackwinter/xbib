package org.xbib.io.http.client.request.body.multipart;

import java.io.File;
import java.nio.charset.Charset;

public class FilePart extends FileLikePart {

    private final File file;

    public FilePart(String name, File file) {
        this(name, file, null);
    }

    public FilePart(String name, File file, String contentType) {
        this(name, file, contentType, null);
    }

    public FilePart(String name, File file, String contentType, Charset charset) {
        this(name, file, contentType, charset, null);
    }

    public FilePart(String name, File file, String contentType, Charset charset, String fileName) {
        this(name, file, contentType, charset, fileName, null);
    }

    public FilePart(String name, File file, String contentType, Charset charset, String fileName, String contentId) {
        this(name, file, contentType, charset, fileName, contentId, null);
    }

    public FilePart(String name, File file, String contentType, Charset charset, String fileName, String contentId, String transferEncoding) {
        super(name, contentType, charset, contentId, transferEncoding);
        if (!file.isFile()) {
            throw new IllegalArgumentException("File is not a normal file " + file.getAbsolutePath());
        }
        if (!file.canRead()) {
            throw new IllegalArgumentException("File is not readable " + file.getAbsolutePath());
        }
        this.file = file;
        setFileName(fileName != null ? fileName : file.getName());
    }

    public File getFile() {
        return file;
    }
}
