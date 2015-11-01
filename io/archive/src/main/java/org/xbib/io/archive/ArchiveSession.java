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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
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

    private URI uri;

    protected ArchiveSession() {
    }

    public ArchiveSession setURI(URI uri) {
        this.uri = uri;
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
        final String scheme = uri.getScheme() != null ? uri.getScheme() : "file";
        final String part = uri.getSchemeSpecificPart();
        switch (mode) {
            case READ: {
                FileInputStream fin;
                if (scheme.equals(suffix) || part.endsWith("." + suffix)) {
                    fin = createFileInputStream(uri, "." + suffix);
                    open(fin);
                } else {
                    Set<String> codecs = StreamCodecService.getCodecs();
                    for (String codec : codecs) {
                        if (scheme.equals(suffix + codec) || part.endsWith("." + suffix + "." + codec)) {
                            fin = createFileInputStream(uri, "." + suffix + "." + codec);
                            open(codecFactory.getCodec(codec).decode(fin, bufferSize));
                            break;
                        }
                    }
                }
                this.isOpen = getInputStream() != null;
                if (!isOpen) {
                    throw new FileNotFoundException("can't open for input, check existence or access rights: " + uri);
                }
                break;
            }
            case WRITE: {
                FileOutputStream fout;
                if (scheme.equals(suffix) || part.endsWith("." + suffix)) {
                    fout = createFileOutputStream(uri, "." + suffix);
                    open(fout);
                } else {
                    Set<String> codecs = StreamCodecService.getCodecs();
                    for (String codec : codecs) {
                        if (scheme.equals(suffix + codec) || part.endsWith("." + suffix + "." + codec)) {
                            fout = createFileOutputStream(uri, "." + suffix + "." + codec);
                            open(codecFactory.getCodec(codec).encode(fout));
                            break;
                        }
                    }
                }
                this.isOpen = getOutputStream() != null;
                if (!isOpen) {
                    throw new FileNotFoundException("can't open for output, check existence or access rights: " + uri);
                }
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
     * @param uri    the URI
     * @param suffix the suffix
     * @return a FileInputStream
     * @throws java.io.IOException if existence or access rights do not suffice
     */
    private FileInputStream createFileInputStream(URI uri, String suffix) throws IOException {
        String part = uri.getSchemeSpecificPart();
        String s = part.endsWith(suffix) ? part : part + suffix;
        File f = new File(s);
        if (f.isFile() && f.canRead()) {
            return new FileInputStream(f);
        } else {
            throw new FileNotFoundException("can't open for input, check existence or access rights: " + s);
        }
    }

    /**
     * Helper method for creating the FileOutputStream. Creates the directory if
     * it does not exist.
     *
     * @param uri    the URI
     * @param suffix the suffix
     * @throws java.io.IOException
     */
    private FileOutputStream createFileOutputStream(URI uri, String suffix) throws IOException {
        String part = uri.getSchemeSpecificPart();
        String s = part.endsWith(suffix) ? part : part + suffix;
        File f = new File(s);
        if (!f.getAbsoluteFile().getParentFile().exists()
                && !f.getAbsoluteFile().getParentFile().mkdirs()) {
            throw new IOException("could not create directories to write: " + f);
        }
        if (!f.exists()) {
            return new FileOutputStream(f);
        } else {
            throw new IOException("file " + f.getAbsolutePath() + " already exists");
        }
    }

}
