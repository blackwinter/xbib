
package org.xbib.io.archive.tar;

import org.xbib.io.archive.ArchiveEntry;
import org.xbib.io.archive.ArchiveInputStream;
import org.xbib.io.archive.ArchiveUtils;
import org.xbib.io.archive.entry.ArchiveEntryEncoding;
import org.xbib.io.archive.entry.ArchiveEntryEncodingHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The TarInputStream reads a UNIX tar archive as an InputStream.
 * methods are provided to position at each successive entry in
 * the archive, and the read each entry as a normal input stream
 * using read().
 */
public class TarArchiveInputStream extends ArchiveInputStream {

    private static final int SMALL_BUFFER_SIZE = 256;

    private static final int BUFFER_SIZE = 8 * 1024;

    private boolean hasHitEOF;

    private long entrySize;

    private long entryOffset;

    private byte[] readBuf;

    protected final TarBuffer buffer;

    private TarArchiveEntry currEntry;

    private final ArchiveEntryEncoding encoding;

    /**
     * Constructor for TarInputStream.
     *
     * @param is the input stream to use
     */
    public TarArchiveInputStream(InputStream is) {
        this(is, TarBuffer.DEFAULT_BLKSIZE, TarBuffer.DEFAULT_RCDSIZE);
    }

    /**
     * Constructor for TarInputStream.
     *
     * @param is       the input stream to use
     * @param encoding name of the encoding to use for file names
     */
    public TarArchiveInputStream(InputStream is, String encoding) {
        this(is, TarBuffer.DEFAULT_BLKSIZE, TarBuffer.DEFAULT_RCDSIZE, encoding);
    }

    /**
     * Constructor for TarInputStream.
     *
     * @param is        the input stream to use
     * @param blockSize the block size to use
     */
    public TarArchiveInputStream(InputStream is, int blockSize) {
        this(is, blockSize, TarBuffer.DEFAULT_RCDSIZE);
    }

    /**
     * Constructor for TarInputStream.
     *
     * @param is        the input stream to use
     * @param blockSize the block size to use
     * @param encoding  name of the encoding to use for file names
     */
    public TarArchiveInputStream(InputStream is, int blockSize,
                                 String encoding) {
        this(is, blockSize, TarBuffer.DEFAULT_RCDSIZE, encoding);
    }

    /**
     * Constructor for TarInputStream.
     *
     * @param is         the input stream to use
     * @param blockSize  the block size to use
     * @param recordSize the record size to use
     */
    public TarArchiveInputStream(InputStream is, int blockSize, int recordSize) {
        this(is, blockSize, recordSize, null);
    }

    /**
     * Constructor for TarInputStream.
     *
     * @param is         the input stream to use
     * @param blockSize  the block size to use
     * @param recordSize the record size to use
     * @param encoding   name of the encoding to use for file names
     */
    public TarArchiveInputStream(InputStream is, int blockSize, int recordSize,
                                 String encoding) {
        this.buffer = new TarBuffer(is, blockSize, recordSize);
        this.readBuf = null;
        this.hasHitEOF = false;
        this.encoding = ArchiveEntryEncodingHelper.getEncoding(encoding);
    }

    /**
     * Closes this stream. Calls the TarBuffer's close() method.
     *
     * @throws java.io.IOException on error
     */
    @Override
    public void close() throws IOException {
        buffer.close();
    }

    /**
     * Get the record size being used by this stream's TarBuffer.
     *
     * @return The TarBuffer record size.
     */
    public int getRecordSize() {
        return buffer.getRecordSize();
    }

    /**
     * Get the available data that can be read from the current
     * entry in the archive. This does not indicate how much data
     * is left in the entire archive, only in the current entry.
     * This value is determined from the entry's size header field
     * and the amount of data already read from the current entry.
     * Integer.MAX_VALUE is returen in case more than Integer.MAX_VALUE
     * bytes are left in the current entry in the archive.
     *
     * @return The number of available bytes for the current entry.
     * @throws java.io.IOException for signature
     */
    @Override
    public int available() throws IOException {
        if (entrySize - entryOffset > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) (entrySize - entryOffset);
    }

    /**
     * Skip bytes in the input buffer. This skips bytes in the
     * current entry's data, not the entire archive, and will
     * stop at the end of the current entry's data if the number
     * to skip extends beyond that point.
     *
     * @param numToSkip The number of bytes to skip.
     * @return the number actually skipped
     * @throws java.io.IOException on error
     */
    @Override
    public long skip(long numToSkip) throws IOException {
        // REVIEW
        // This is horribly inefficient, but it ensures that we
        // properly skip over bytes via the TarBuffer...
        //
        byte[] skipBuf = new byte[BUFFER_SIZE];
        long skip = numToSkip;
        while (skip > 0) {
            int realSkip = (int) (skip > skipBuf.length ? skipBuf.length : skip);
            int numRead = read(skipBuf, 0, realSkip);
            if (numRead == -1) {
                break;
            }
            skip -= numRead;
        }
        return (numToSkip - skip);
    }

    /**
     * Since we do not support marking just yet, we do nothing.
     */
    @Override
    public synchronized void reset() {
    }

    /**
     * Get the next entry in this tar archive. This will skip
     * over any remaining data in the current entry, if there
     * is one, and place the input stream at the header of the
     * next entry, and read the header and instantiate a new
     * TarEntry from the header bytes and return that entry.
     * If there are no more entries in the archive, null will
     * be returned to indicate that the end of the archive has
     * been reached.
     *
     * @return The next TarEntry in the archive, or null.
     * @throws java.io.IOException on error
     */
    public synchronized TarArchiveEntry getNextTarEntry() throws IOException {
        if (hasHitEOF) {
            return null;
        }

        if (currEntry != null) {
            long numToSkip = entrySize - entryOffset;

            while (numToSkip > 0) {
                long skipped = skip(numToSkip);
                if (skipped <= 0) {
                    throw new RuntimeException("failed to skip current tar entry");
                }
                numToSkip -= skipped;
            }

            readBuf = null;
        }

        byte[] headerBuf = getRecord();

        if (hasHitEOF) {
            currEntry = null;
            return null;
        }

        try {
            currEntry = new TarArchiveEntry(headerBuf, encoding);
        } catch (IllegalArgumentException e) {
            IOException ioe = new IOException("Error detected parsing the header");
            ioe.initCause(e);
            throw ioe;
        }
        entryOffset = 0;
        entrySize = currEntry.getEntrySize();

        if (currEntry.isGNULongNameEntry()) {
            // read in the name
            StringBuilder longName = new StringBuilder();
            byte[] buf = new byte[SMALL_BUFFER_SIZE];
            int length = 0;
            while ((length = read(buf)) >= 0) {
                longName.append(new String(buf, 0, length)); // TODO default charset?
            }
            getNextEntry();
            if (currEntry == null) {
                // Bugzilla: 40334
                // Malformed tar file - long entry name not followed by entry
                return null;
            }
            // remove trailing null terminator
            if (longName.length() > 0
                    && longName.charAt(longName.length() - 1) == 0) {
                longName.deleteCharAt(longName.length() - 1);
            }
            currEntry.setName(longName.toString());
        }

        if (currEntry.isPaxHeader()) { // Process Pax headers
            paxHeaders();
        }

        if (currEntry.isGNUSparse()) { // Process sparse files
            readGNUSparse();
        }

        // If the size of the next element in the archive has changed
        // due to a new size being reported in the posix header
        // information, we update entrySize here so that it contains
        // the correct value.
        entrySize = currEntry.getEntrySize();
        return currEntry;
    }

    /**
     * Get the next record in this tar archive. This will skip
     * over any remaining data in the current entry, if there
     * is one, and place the input stream at the header of the
     * next entry.
     * If there are no more entries in the archive, null will
     * be returned to indicate that the end of the archive has
     * been reached.
     *
     * @return The next header in the archive, or null.
     * @throws java.io.IOException on error
     */
    private byte[] getRecord() throws IOException {
        if (hasHitEOF) {
            return null;
        }

        byte[] headerBuf = buffer.readRecord();

        if (headerBuf == null) {
            hasHitEOF = true;
        } else if (buffer.isEOFRecord(headerBuf)) {
            hasHitEOF = true;
        }

        return hasHitEOF ? null : headerBuf;
    }

    private void paxHeaders() throws IOException {
        Map<String, String> headers = parsePaxHeaders(this);
        getNextEntry(); // Get the actual file entry
        applyPaxHeadersToCurrentEntry(headers);
    }

    private Map<String, String> parsePaxHeaders(InputStream i) throws IOException {
        Map<String, String> headers = new HashMap<String, String>();
        // Format is "length keyword=value\n";
        while (true) { // get length
            int ch;
            int len = 0;
            int read = 0;
            while ((ch = i.read()) != -1) {
                read++;
                if (ch == ' ') { // End of length string
                    // Get keyword
                    ByteArrayOutputStream coll = new ByteArrayOutputStream();
                    while ((ch = i.read()) != -1) {
                        read++;
                        if (ch == '=') { // end of keyword
                            String keyword = coll.toString("UTF-8");
                            // Get rest of entry
                            byte[] rest = new byte[len - read];
                            int got = i.read(rest);
                            if (got != len - read) {
                                throw new IOException("Failed to read "
                                        + "Paxheader. Expected "
                                        + (len - read)
                                        + " bytes, read "
                                        + got);
                            }
                            // Drop trailing NL
                            String value = new String(rest, 0,
                                    len - read - 1, Charset.forName("UTF-8"));
                            headers.put(keyword, value);
                            break;
                        }
                        coll.write((byte) ch);
                    }
                    break; // Processed single header
                }
                len *= 10;
                len += ch - '0';
            }
            if (ch == -1) { // EOF
                break;
            }
        }
        return headers;
    }

    private void applyPaxHeadersToCurrentEntry(Map<String, String> headers) {
        /*
         * The following headers are defined for Pax.
         * atime, ctime, charset: cannot use these without changing TarArchiveEntry fields
         * mtime
         * comment
         * gid, gname
         * linkpath
         * size
         * uid,uname
         * SCHILY.devminor, SCHILY.devmajor: don't have setters/getters for those
         */
        for (Entry<String, String> ent : headers.entrySet()) {
            String key = ent.getKey();
            String val = ent.getValue();
            if ("path".equals(key)) {
                currEntry.setName(val);
            } else if ("linkpath".equals(key)) {
                currEntry.setLinkName(val);
            } else if ("gid".equals(key)) {
                currEntry.setGroupId(Integer.parseInt(val));
            } else if ("gname".equals(key)) {
                currEntry.setGroupName(val);
            } else if ("uid".equals(key)) {
                currEntry.setUserId(Integer.parseInt(val));
            } else if ("uname".equals(key)) {
                currEntry.setUserName(val);
            } else if ("size".equals(key)) {
                currEntry.setEntrySize(Long.parseLong(val));
            } else if ("mtime".equals(key)) {
                long mtime = (long) (Double.parseDouble(val) * 1000);
                currEntry.setLastModified(new Date(mtime));
            } else if ("SCHILY.devminor".equals(key)) {
                currEntry.setDevMinor(Integer.parseInt(val));
            } else if ("SCHILY.devmajor".equals(key)) {
                currEntry.setDevMajor(Integer.parseInt(val));
            }
        }
    }

    /**
     * Adds the sparse chunks from the current entry to the sparse chunks,
     * including any additional sparse entries following the current entry.
     *
     * @throws java.io.IOException on error
     * @todo Sparse files get not yet really processed.
     */
    private void readGNUSparse() throws IOException {
        /* we do not really process sparse files yet
        sparses = new ArrayList();
        sparses.addAll(currEntry.getSparses());
        */
        if (currEntry.isExtended()) {
            TarArchiveSparseEntry entry;
            do {
                byte[] headerBuf = getRecord();
                if (hasHitEOF) {
                    currEntry = null;
                    break;
                }
                entry = new TarArchiveSparseEntry(headerBuf);
                /* we do not really process sparse files yet
                sparses.addAll(entry.getSparses());
                */
            } while (entry.isExtended());
        }
    }

    @Override
    public ArchiveEntry getNextEntry() throws IOException {
        return getNextTarEntry();
    }

    /**
     * Reads bytes from the current tar archive entry.
     * <p/>
     * This method is aware of the boundaries of the current
     * entry in the archive and will deal with them as if they
     * were this stream's start and EOF.
     *
     * @param buf       The buffer into which to place bytes read.
     * @param offset    The offset at which to place bytes read.
     * @param numToRead The number of bytes to read.
     * @return The number of bytes read, or -1 at EOF.
     * @throws java.io.IOException on error
     */
    @Override
    public int read(byte[] buf, int offset, int numToRead) throws IOException {
        int totalRead = 0;

        if (entryOffset >= entrySize) {
            return -1;
        }

        if ((numToRead + entryOffset) > entrySize) {
            numToRead = (int) (entrySize - entryOffset);
        }

        if (readBuf != null) {
            int sz = (numToRead > readBuf.length) ? readBuf.length
                    : numToRead;

            System.arraycopy(readBuf, 0, buf, offset, sz);

            if (sz >= readBuf.length) {
                readBuf = null;
            } else {
                int newLen = readBuf.length - sz;
                byte[] newBuf = new byte[newLen];

                System.arraycopy(readBuf, sz, newBuf, 0, newLen);

                readBuf = newBuf;
            }

            totalRead += sz;
            numToRead -= sz;
            offset += sz;
        }

        while (numToRead > 0) {
            byte[] rec = buffer.readRecord();

            if (rec == null) {
                throw new IOException("unexpected EOF with " + numToRead + " bytes unread");
            }
            int sz = numToRead;
            int recLen = rec.length;

            if (recLen > sz) {
                System.arraycopy(rec, 0, buf, offset, sz);

                readBuf = new byte[recLen - sz];

                System.arraycopy(rec, sz, readBuf, 0, recLen - sz);
            } else {
                sz = recLen;

                System.arraycopy(rec, 0, buf, offset, recLen);
            }

            totalRead += sz;
            numToRead -= sz;
            offset += sz;
        }

        entryOffset += totalRead;

        return totalRead;
    }

    protected final TarArchiveEntry getCurrentEntry() {
        return currEntry;
    }

    protected final void setCurrentEntry(TarArchiveEntry e) {
        currEntry = e;
    }

    protected final boolean isAtEOF() {
        return hasHitEOF;
    }

    protected final void setAtEOF(boolean b) {
        hasHitEOF = b;
    }

    /**
     * Checks if the signature matches what is expected for a tar file.
     *
     * @param signature the bytes to check
     * @param length    the number of bytes to check
     * @return true, if this stream is a tar archive stream, false otherwise
     */
    public static boolean matches(byte[] signature, int length) {
        if (length < TarConstants.VERSION_OFFSET + TarConstants.VERSIONLEN) {
            return false;
        }

        if (ArchiveUtils.matchAsciiBuffer(TarConstants.MAGIC_POSIX,
                signature, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN)
                &&
                ArchiveUtils.matchAsciiBuffer(TarConstants.VERSION_POSIX,
                        signature, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN)
                ) {
            return true;
        }
        if (ArchiveUtils.matchAsciiBuffer(TarConstants.MAGIC_GNU,
                signature, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN)
                &&
                (
                        ArchiveUtils.matchAsciiBuffer(TarConstants.VERSION_GNU_SPACE,
                                signature, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN)
                                ||
                                ArchiveUtils.matchAsciiBuffer(TarConstants.VERSION_GNU_ZERO,
                                        signature, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN)
                )
                ) {
            return true;
        }
        if (ArchiveUtils.matchAsciiBuffer(TarConstants.MAGIC_ANT,
                signature, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN)
                &&
                ArchiveUtils.matchAsciiBuffer(TarConstants.VERSION_ANT,
                        signature, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN)
                ) {
            return true;
        }
        return false;
    }

}
