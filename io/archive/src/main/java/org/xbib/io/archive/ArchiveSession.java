/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.io.archive;

import org.xbib.io.Session;
import org.xbib.io.StreamCodecService;
import org.xbib.io.StringPacket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Set;

/**
 * Archive session
 */
public abstract class ArchiveSession<I extends ArchiveInputStream, O extends ArchiveOutputStream>
        implements Session<StringPacket> {

    private final static StreamCodecService codecFactory = StreamCodecService.getInstance();

    private final static int DEFAULT_INPUT_BUFSIZE = 65536;

    protected int bufferSize = DEFAULT_INPUT_BUFSIZE;

    private boolean isOpen;

    private Path path;

    private OpenOption option;

    protected ArchiveSession() {
    }

    public ArchiveSession setPath(Path path, OpenOption option) {
        this.path = path;
        this.option = option;
        return this;
    }

    public ArchiveSession setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    @Override
    public synchronized void open(Mode mode) throws IOException {
        if (isOpen) {
            return;
        }
        final String suffix = getSuffix();
        switch (mode) {
            case READ: {
                InputStream in = newInputStream(path, option, "." + suffix);
                open(in);
                this.isOpen = getInputStream() != null;
                break;
            }
            case WRITE: {
                OutputStream out = newOutputStream(path, option, "." + suffix);
                open(out);
                this.isOpen = getOutputStream() != null;
                break;
            }
        }
    }

    @Override
    public StringPacket newPacket() {
        return new StringPacket();
    }

    @Override
    public synchronized StringPacket read() throws IOException {
        if (!isOpen()) {
            throw new IOException("not open");
        }
        if (getInputStream() == null) {
            throw new IOException("no input stream found");
        }
        ArchiveEntry entry = getInputStream().getNextEntry();
        if (entry == null) {
            return null;
        }
        StringPacket packet = newPacket();
        String name = entry.getName();
        packet.name(name);
        int size = (int)entry.getEntrySize();
        byte[] b = new byte[size];
        getInputStream().read(b, 0, size);
        packet.packet(new String(b));
        return packet;
    }

    @Override
    public synchronized void write(StringPacket packet) throws IOException {
        if (!isOpen()) {
            throw new IOException("not open");
        }
        if (getOutputStream() == null) {
            throw new IOException("no output stream found");
        }
        if (packet == null || packet.toString() == null) {
            throw new IOException("no packet to write");
        }
        byte[] buf = packet.toString().getBytes();
        if (buf.length > 0) {
            String name = packet.name();
            ArchiveEntry entry = getOutputStream().newArchiveEntry();
            entry.setName(name);
            entry.setLastModified(new Date());
            entry.setEntrySize(buf.length);
            getOutputStream().putArchiveEntry(entry);
            getOutputStream().write(buf);
            getOutputStream().closeArchiveEntry();
        }
    }

    /**
     * Close session
     */
    @Override
    public synchronized void close() throws IOException {
        if (!isOpen) {
            return;
        }
        if (getOutputStream() != null) {
            getOutputStream().close();
        }
        if (getInputStream() != null) {
            getInputStream().close();
        }
        this.isOpen = false;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    public boolean canOpen(URI uri) {
        return canOpen(uri, getSuffix(), true);
    }

    public static boolean canOpen(URI uri, String suffix, boolean withCodecs) {
        final String scheme = uri.getScheme();
        final String part = uri.getSchemeSpecificPart();
        if (scheme.equals(suffix) ||
                (scheme.equals("file") && part.endsWith("." + suffix.toLowerCase())) ||
                (scheme.equals("file") && part.endsWith("." + suffix.toUpperCase()))) {
            return true;
        }
        if (withCodecs) {
            Set<String> codecs = StreamCodecService.getCodecs();
            for (String codec : codecs) {
                String s = "." + suffix + "." + codec;
                if (part.endsWith(s) || part.endsWith(s.toLowerCase()) || part.endsWith(s.toUpperCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    protected abstract String getSuffix();

    protected abstract void open(InputStream in) throws IOException;

    protected abstract void open(OutputStream in) throws IOException;

    protected abstract I getInputStream();

    protected abstract O getOutputStream();

    /**
     * Helper method for creating the FileInputStream
     *
     * @param suffix the suffix
     * @return an InputStream
     * @throws java.io.IOException if existence or access rights do not suffice
     */
    public static InputStream newInputStream(Path path, OpenOption option, String suffix) throws IOException {
        String part = path.toUri().getSchemeSpecificPart();
        path = suffix != null ? Paths.get(part.endsWith(suffix) ? part : part + suffix) : path;
        if (Files.isReadable(path) && Files.isRegularFile(path)) {
            InputStream in = Files.newInputStream(path, option);
            Set<String> codecs = StreamCodecService.getCodecs();
            for (String codec : codecs) {
                String s = "." + codec;
                if (part.endsWith(s.toLowerCase()) || part.endsWith(s.toUpperCase())) {
                    in = StreamCodecService.getInstance().getCodec(codec).decode(in);
                }
            }
            return in;
        } else {
            throw new IOException("can't open for input, check existence or access rights: " + path);
        }
    }

    /**
     * Helper method for creating the FileOutputStream. Creates the directory if
     * it does not exist.
     *
     * @throws java.io.IOException if existence or access rights do not suffice
     */
    public static OutputStream newOutputStream(Path path, OpenOption option, String suffix) throws IOException {
        String part = path.toUri().getSchemeSpecificPart();
        path = suffix != null ? Paths.get(part.endsWith(suffix) ? part : part + suffix) : path;
        if (Files.isWritable(path) && Files.isRegularFile(path)) {
            OutputStream out = Files.newOutputStream(path, option);
            Set<String> codecs = StreamCodecService.getCodecs();
            for (String codec : codecs) {
                String s = "." + codec;
                if (part.endsWith(s.toLowerCase()) || part.endsWith(s.toUpperCase())) {
                    out = StreamCodecService.getInstance().getCodec(codec).encode(out);
                }
            }
            return out;
        } else {
            throw new IOException("can't open for output, check existence or access rights: " + path);
        }
    }

}
