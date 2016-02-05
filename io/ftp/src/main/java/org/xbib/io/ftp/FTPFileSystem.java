package org.xbib.io.ftp;

import org.xbib.io.ftp.path.GlobPattern;
import org.xbib.io.ftp.path.SlashPath;
import org.xbib.io.ftp.principals.DummyPrincipleLookupService;
import org.xbib.io.ftp.watch.NopWatchService;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public final class FTPFileSystem extends FileSystem {

    private final FTPFileSystemProvider provider;

    private final URI uri;

    private final FileStore fileStore;

    private final AtomicBoolean open = new AtomicBoolean(true);

    public FTPFileSystem(final FTPFileSystemProvider provider, final URI uri) {
        this.provider = provider;
        this.uri = uri; // already normalized
        fileStore = new FTPFileStore(uri);
    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    @Override
    public void close()
            throws IOException {
        if (open.getAndSet(false)) {
            provider.unregister(this);
        }
    }

    @Override
    public boolean isOpen() {
        return open.get();
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public String getSeparator() {
        return "/";
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return Collections.singleton(getPath("/"));
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return Collections.singletonList(fileStore);
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return Collections.singleton("basic");
    }

    @Override
    public Path getPath(final String first, final String... more) {
        SlashPath path = SlashPath.fromString(first);
        for (final String component : more) {
            path = path.resolve(SlashPath.fromString(component));
        }
        return new FTPPath(this, uri, path);
    }

    @Override
    public PathMatcher getPathMatcher(final String syntaxAndPattern) {
        if (!syntaxAndPattern.contains(":")) {
            throw new IllegalArgumentException("PathMatcher requires input syntax:expression");
        }
        String[] parts = syntaxAndPattern.split(":", 2);
        Pattern pattern;
        if ("glob".equals(parts[0])) {
            pattern = GlobPattern.compile(parts[1]);
        } else if ("regex".equals(parts[0])) {
            pattern = Pattern.compile(parts[1]);
        } else {
            throw new UnsupportedOperationException("Unknown PathMatcher syntax: " + parts[0]);
        }
        return new FTPPathMatcher(pattern);
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return new DummyPrincipleLookupService();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        return NopWatchService.INSTANCE;
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FTPFileSystem other = (FTPFileSystem) obj;
        return uri.equals(other.uri);
    }

    URI getUri() {
        return uri;
    }
}
