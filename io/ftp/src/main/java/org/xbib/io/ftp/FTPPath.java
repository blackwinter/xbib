package org.xbib.io.ftp;

import org.xbib.io.ftp.path.SlashPath;
import org.xbib.io.ftp.watch.NopWatchKey;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;

public final class FTPPath implements Path {
    private final FileSystem fs;
    private final URI uri;
    private final SlashPath path;

    public FTPPath(final FileSystem fs, final URI uri, final SlashPath path) {
        this.fs = fs;
        this.uri = uri;
        this.path = path;
    }

    private static SlashPath stripDotDots(final SlashPath path) {
        final int len = path.getNameCount();
        int i = 0;
        final Iterator<String> iterator = path.iterator();
        while (iterator.hasNext() && "..".equals(iterator.next())) {
            i++;
        }
        return SlashPath.ROOT.resolve(path.subpath(i, len));
    }

    @Override
    public FileSystem getFileSystem() {
        return fs;
    }

    @Override
    public boolean isAbsolute() {
        return path.isAbsolute();
    }

    @Override
    public Path getRoot() {
        return isAbsolute() ? new FTPPath(fs, uri, SlashPath.ROOT) : null;
    }

    @Override
    public Path getFileName() {
        final SlashPath name = path.getLastName();
        return new FTPPath(fs, uri, name != null ? name : SlashPath.fromString("/"));
    }

    @Override
    public Path getParent() {
        final SlashPath parent = path.getParent();
        return new FTPPath(fs, uri, parent != null ? parent : SlashPath.fromString("/"));
    }

    @Override
    public int getNameCount() {
        return path.getNameCount();
    }

    @Override
    public Path getName(final int index) {
        return new FTPPath(fs, uri, path.getName(index));
    }

    @Override
    public Path subpath(final int beginIndex, final int endIndex) {
        return new FTPPath(fs, uri, path.subpath(beginIndex, endIndex));
    }

    @Override
    public boolean startsWith(final Path other) {
        if (!fs.equals(other.getFileSystem())) {
            return false;
        }
        final FTPPath otherPath = (FTPPath) other;
        return path.startsWith(otherPath.path);
    }

    @Override
    public boolean startsWith(final String other) {
        return startsWith(new FTPPath(fs, uri, SlashPath.fromString(other)));
    }

    @Override
    public boolean endsWith(final Path other) {
        if (!fs.equals(other.getFileSystem())) {
            return false;
        }
        final FTPPath otherPath = (FTPPath) other;
        return path.endsWith(otherPath.path);
    }

    @Override
    public boolean endsWith(final String other) {
        return endsWith(new FTPPath(fs, uri, SlashPath.fromString(other)));
    }

    @Override
    public Path normalize() {
        return path.isNormalized() ? this
                : fs.getPath(path.normalize().toString());
    }

    @Override
    public Path resolve(final Path other) {
        if (!fs.provider().equals(other.getFileSystem().provider())) {
            throw new ProviderMismatchException();
        }
        if (other.isAbsolute()) {
            return other;
        }
        final FTPPath otherPath = (FTPPath) other;
        return SlashPath.EMPTY.equals(otherPath.path) ? this
                : new FTPPath(fs, uri, path.resolve(otherPath.path));
    }

    @Override
    public Path resolve(final String other) {
        return new FTPPath(fs, uri, path.resolve(SlashPath.fromString(other)));
    }

    @Override
    public Path resolveSibling(final Path other) {
        if (!fs.provider().equals(other.getFileSystem().provider())) {
            throw new ProviderMismatchException();
        }
        final SlashPath parent = path.getParent();
        if (parent == null) {
            return other;
        }
        final FTPPath otherPath = (FTPPath) other;
        return new FTPPath(fs, uri, parent.resolve(otherPath.path));
    }

    @Override
    public Path resolveSibling(final String other) {
        final FTPPath otherPath
                = new FTPPath(fs, uri, SlashPath.fromString(other));
        return resolveSibling(otherPath);
    }

    @Override
    public Path relativize(final Path other) {
        if (!fs.provider().equals(other.getFileSystem().provider())) {
            throw new ProviderMismatchException();
        }
        final FTPPath otherPath = (FTPPath) other;
        return new FTPPath(fs, uri, path.relativize(otherPath.path));
    }

    @Override
    public URI toUri() {
        return URI.create(uri.toString() + stripDotDots(path.normalize()));
    }

    @Override
    public Path toAbsolutePath() {
        return isAbsolute() ? this : new FTPPath(fs, uri, stripDotDots(path));
    }

    @Override
    public Path toRealPath(final LinkOption... options)
            throws IOException {
        return toAbsolutePath();
    }

    @Override
    public File toFile() {
        return new File(path.toString());
    }

    @Override
    public WatchKey register(final WatchService watcher,
                             final WatchEvent.Kind<?>[] events,
                             final WatchEvent.Modifier... modifiers)
            throws IOException {
        return new NopWatchKey();
    }

    @Override
    public WatchKey register(final WatchService watcher,
                             final WatchEvent.Kind<?>... events)
            throws IOException {
        return new NopWatchKey();
    }

    @Override
    public Iterator<Path> iterator() {
        return new Iterator<Path>() {
            private final Iterator<String> it = path.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Path next() {
                return fs.getPath(it.next());
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public int compareTo(final Path other) {
        return path.toString().compareTo(other.toString());
    }

    @Override
    public int hashCode() {
        return 31 * uri.hashCode() + path.hashCode();
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
        final FTPPath other = (FTPPath) obj;
        return uri.equals(other.uri) && path.equals(other.path);
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
