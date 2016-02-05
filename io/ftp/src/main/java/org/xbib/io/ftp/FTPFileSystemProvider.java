package org.xbib.io.ftp;

import org.xbib.io.ftp.agent.FtpAgent;
import org.xbib.io.ftp.agent.FtpAgentFactory;
import org.xbib.io.ftp.agent.FtpAgentQueue;
import org.xbib.io.ftp.agent.FtpFileView;
import org.xbib.io.ftp.agent.impl.DefaultFtpAgentFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class FTPFileSystemProvider extends FileSystemProvider {

    private static final int MAX_AGENTS = 5;

    private final FtpAgentFactory agentFactory;

    private final Map<URI, FTPFileSystem> fileSystems = new HashMap<>();

    private final Map<FileSystem, FtpAgentQueue> agentQueues = new ConcurrentHashMap<>();

    public FTPFileSystemProvider() {
        this(new DefaultFtpAgentFactory());
    }

    public FTPFileSystemProvider(final FtpAgentFactory agentFactory) {
        this.agentFactory = agentFactory;
    }

    @Override
    public String getScheme() {
        return "ftp";
    }

    @Override
    @SuppressWarnings("unchecked")
    public FileSystem newFileSystem(final URI uri, final Map<String, ?> env)
            throws IOException {
        final URI normalized = normalizeAndCheck(uri);
        final FTPConfiguration.Builder builder = FTPConfiguration.newBuilder()
                .setHostname(normalized.getHost());
        if (normalized.getPort() != -1) {
            builder.setPort(normalized.getPort());
        }
        final Map<String, String> params = (Map<String, String>) env;
        final String username = params != null && params.containsKey("username") ? params.get("username") : "anonymous";
        final String password = params != null && params.containsKey("password") ? params.get("password") : "jftp@";
        builder.setUsername(username);
        builder.setPassword(password);
        builder.setPath(uri.getPath());
        final FTPConfiguration cfg = builder.build();
        synchronized (fileSystems) {
            if (fileSystems.containsKey(normalized)) {
                throw new FileSystemAlreadyExistsException();
            }
            final FTPFileSystem fs = new FTPFileSystem(this, normalized);
            final FtpAgentQueue agentQueue = new FtpAgentQueue(agentFactory, cfg, MAX_AGENTS);
            fileSystems.put(normalized, fs);
            agentQueues.put(fs, agentQueue);
            return fs;
        }
    }

    @Override
    public InputStream newInputStream(final Path path,
                                      final OpenOption... options) throws IOException {
        final FtpAgentQueue queue = agentQueues.get(path.getFileSystem());
        return queue.getAgent().getInputStream(path);
    }

    @Override
    public OutputStream newOutputStream(final Path path,
                                        final OpenOption... options) throws IOException {
        throw new IllegalStateException();
    }

    @Override
    public FileSystem getFileSystem(final URI uri) {
        synchronized (fileSystems) {
            final FileSystem ret = fileSystems.get(uri);
            if (ret == null) {
                throw new FileSystemNotFoundException();
            }
            return ret;
        }
    }

    @Override
    public Path getPath(final URI uri) {
        URI rel;
        synchronized (fileSystems) {
            Set<Map.Entry<URI, FTPFileSystem>> set = fileSystems.entrySet();
            for (Map.Entry<URI, FTPFileSystem> entry : set) {
                rel = entry.getKey().relativize(uri);
                if (!rel.isAbsolute()) {
                    return entry.getValue().getPath(rel.toString());
                }
            }
            throw new IllegalStateException();
        }
    }

    @Override
    public SeekableByteChannel newByteChannel(final Path path,
                                              final Set<? extends OpenOption> options,
                                              final FileAttribute<?>... attrs)
            throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(final Path dir,
                                                    final DirectoryStream.Filter<? super Path> filter) throws IOException {
        final FTPFileSystem fs = (FTPFileSystem) dir.getFileSystem();
        final FtpAgentQueue queue = agentQueues.get(fs);
        try (final FtpAgent agent = queue.getAgent()) {
            final String absPath = dir.toRealPath().toString();
            final Iterator<String> names = agent.getDirectoryNames(absPath).iterator();
            return new DirectoryStream<Path>() {
                @Override
                public Iterator<Path> iterator() {
                    return new Iterator<Path>() {
                        @Override
                        public boolean hasNext() {
                            return names.hasNext();
                        }

                        @Override
                        public Path next() {
                            if (!hasNext()) {
                                throw new NoSuchElementException();
                            }
                            return fs.getPath(names.next());
                        }

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }

                @Override
                public void close()
                        throws IOException {
                }
            };
        }
    }

    @Override
    public void createDirectory(final Path dir, final FileAttribute<?>... attrs)
            throws IOException {
        throw new IllegalStateException();
    }

    @Override
    public void delete(final Path path)
            throws IOException {
        throw new IllegalStateException();
    }

    @Override
    public void copy(final Path source, final Path target,
                     final CopyOption... options) throws IOException {
        throw new IllegalStateException();
    }

    @Override
    public void move(final Path source, final Path target,
                     final CopyOption... options) throws IOException {
        throw new IllegalStateException();
    }

    @Override
    public boolean isSameFile(final Path path, final Path path2) throws IOException {
        return path.toRealPath().equals(path2.toRealPath());
    }

    @Override
    public boolean isHidden(final Path path) throws IOException {
        if (path.getNameCount() == 0) {
            return false;
        }
        final String name = path.getFileName().toString();
        return !(".".equals(name) || "..".equals(name)) && name.startsWith(".");
    }

    @Override
    public FileStore getFileStore(final Path path) throws IOException {
        return path.getFileSystem().getFileStores().iterator().next();
    }

    @Override
    public void checkAccess(final Path path, final AccessMode... modes) throws IOException {
        final FtpAgentQueue queue = agentQueues.get(path.getFileSystem());
        final String name = path.toRealPath().toString();
        final List<AccessMode> modeList = Arrays.asList(modes);
        try (final FtpAgent agent = queue.getAgent()) {
            final FtpFileView view = agent.getFileView(name);
            if (!view.getAccess().containsAll(modeList)) {
                throw new AccessDeniedException(name);
            }
        }
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(final Path path,
                                                                final Class<V> type, final LinkOption... options) {
        if (type != BasicFileAttributeView.class) {
            return null;
        }
        final FtpAgentQueue queue = agentQueues.get(path.getFileSystem());
        final String name;
        try {
            name = path.toRealPath(options).toString();
        } catch (IOException ignored) {
            return null;
        }
        try (final FtpAgent agent = queue.getAgent()) {
            final FtpFileView view = agent.getFileView(name);
            return type.isAssignableFrom(view.getClass()) ? type.cast(view) : null;
        } catch (IOException e) {
            // FIXME
            return null;
        }
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(final Path path,
                                                            final Class<A> type,
                                                            final LinkOption... options) throws IOException {
        final String name = path.toString();
        final FtpAgentQueue queue = agentQueues.get(path.getFileSystem());
        try (final FtpAgent agent = queue.getAgent()) {
            final BasicFileAttributes attributes = agent.getFileView(name).readAttributes();
            return type.isAssignableFrom(attributes.getClass()) ? type.cast(attributes) : null;
        }
    }

    @Override
    public Map<String, Object> readAttributes(final Path path,
                                              final String attributes, final LinkOption... options) throws IOException {
        final Set<BasicFileAttributesEnum> set = getAttributes(attributes);
        final FtpAgentQueue queue = agentQueues.get(path.getFileSystem());
        final String file = path.toRealPath().toString();
        final BasicFileAttributes attrs;
        try (final FtpAgent agent = queue.getAgent()) {
            attrs = agent.getFileView(file).readAttributes();
        }
        final Map<String, Object> ret = new HashMap<>();
        for (final BasicFileAttributesEnum attr : set) {
            ret.put(attr.toString(), attr.getValue(attrs));
        }
        return ret;
    }

    @Override
    public void setAttribute(final Path path, final String attribute,
                             final Object value, final LinkOption... options)
            throws IOException {
        throw new IllegalStateException();
    }

    void unregister(final FTPFileSystem fs) {
        synchronized (fileSystems) {
            final URI uri = fs.getUri();
            final FtpAgentQueue queue = agentQueues.get(fs);
            fileSystems.remove(uri);
            try {
                queue.close();
            } catch (IOException ignored) {
            }
            agentQueues.remove(fs);
        }
    }

    public static URI normalizeAndCheck(final URI uri) {
        Objects.requireNonNull(uri, "uri cannot be null");
        checkThat(uri.isAbsolute(), "uri must be absolute");
        checkThat("ftp".equalsIgnoreCase(uri.getScheme()), "uri scheme must be \"ftp\"");
        checkThat(uri.getUserInfo() == null, "uri must not contain user info");
        checkThat(uri.getHost() != null, "uri must have a hostname");
        try {
            return new URI("ftp", null, uri.getHost().toLowerCase(), uri.getPort(), null, null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException("How did I get there??", e);
        }
    }

    private static void checkThat(final boolean condition, final String msg) {
        if (!condition) {
            throw new IllegalArgumentException(msg);
        }
    }

    private static final Pattern COMMA = Pattern.compile(",");
    private static final String ALL = "*";
    private static final String BASIC = "basic";
    private static final char COLON = ':';

    public static EnumSet<BasicFileAttributesEnum> getAttributes(
            final String input) {
        final String[] attrs = COMMA.split(input);
        final EnumSet<BasicFileAttributesEnum> set
                = EnumSet.noneOf(BasicFileAttributesEnum.class);

        String type, name;
        int index;
        BasicFileAttributesEnum attr;

        for (final String attrName : attrs) {
            if (ALL.equals(attrName)) {
                return EnumSet.allOf(BasicFileAttributesEnum.class);
            }
            index = attrName.indexOf(COLON);
            if (index == -1) {
                type = BASIC;
                name = attrName;
            } else {
                type = attrName.substring(0, index);
                name = attrName.substring(index + 1, attrName.length());
            }
            if (!BASIC.equals(type)) {
                throw new UnsupportedOperationException();
            }
            if (ALL.equals(name)) {
                return EnumSet.allOf(BasicFileAttributesEnum.class);
            }
            attr = BasicFileAttributesEnum.forName(name);
            if (attr == null) {
                throw new UnsupportedOperationException();
            }
            set.add(attr);
        }

        return EnumSet.copyOf(set);
    }
}
