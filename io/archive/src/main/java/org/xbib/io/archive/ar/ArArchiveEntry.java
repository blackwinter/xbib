
package org.xbib.io.archive.ar;

import org.xbib.io.archive.ArchiveEntry;

import java.io.File;
import java.util.Date;


/**
 * Represents an archive entry in the "ar" format.
 * <p/>
 * Each AR archive starts with "!<arch>" followed by a LF. After these 8 bytes
 * the archive entries are listed. The format of an entry header is as it follows:
 * <p/>
 * <pre>
 * START BYTE   END BYTE    NAME                    FORMAT      LENGTH
 * 0            15          File name               ASCII       16
 * 16           27          Modification timestamp  Decimal     12
 * 28           33          Owner ID                Decimal     6
 * 34           39          Group ID                Decimal     6
 * 40           47          File mode               Octal       8
 * 48           57          File size (bytes)       Decimal     10
 * 58           59          File magic              \140\012    2
 * </pre>
 * <p/>
 * This specifies that an ar archive entry header contains 60 bytes.
 * <p/>
 * Due to the limitation of the file name length to 16 bytes GNU and
 * BSD has their own variants of this format. Currently this code
 * can read but not write the GNU variant and doesn't support
 * the BSD variant at all.
 *
 * @see <a href="http://www.freebsd.org/cgi/man.cgi?query=ar&sektion=5">ar man page</a>
 */
public class ArArchiveEntry implements ArchiveEntry {

    /**
     * The header for each entry
     */
    public static final String HEADER = "!<arch>\n";

    /**
     * The trailer for each entry
     */
    public static final String TRAILER = "`\012";

    private static final int DEFAULT_MODE = 33188; // = (octal) 0100644

    /**
     * SVR4/GNU adds a trailing / to names; BSD does not.
     * They also vary in how names longer than 16 characters are represented.
     * (Not yet fully supported by this implementation)
     */
    private String name;

    private int userId;

    private int groupId;

    private int mode;

    private long lastModified;

    private long length;

    public ArArchiveEntry() {
    }

    /**
     * Create a new instance using a couple of default values.
     * <p/>
     * <p>Sets userId and groupId to 0, the octal file mode to 644 and
     * the last modified time to the current time.</p>
     *
     * @param name   name of the entry
     * @param length length of the entry in bytes
     */
    public ArArchiveEntry(String name, long length) {
        this(name, length, 0, 0, DEFAULT_MODE,
                System.currentTimeMillis() / 1000);
    }

    /**
     * Create a new instance.
     *
     * @param name         name of the entry
     * @param length       length of the entry in bytes
     * @param userId       numeric user id
     * @param groupId      numeric group id
     * @param mode         file mode
     * @param lastModified last modified time in seconds since the epoch
     */
    public ArArchiveEntry(String name, long length, int userId, int groupId,
                          int mode, long lastModified) {
        this.name = name;
        this.length = length;
        this.userId = userId;
        this.groupId = groupId;
        this.mode = mode;
        this.lastModified = lastModified;
    }

    /**
     * Create a new instance using the attributes of the given file
     */
    public ArArchiveEntry(File inputFile, String entryName) {
        // TODO sort out mode
        this(entryName, inputFile.isFile() ? inputFile.length() : 0,
                0, 0, DEFAULT_MODE, inputFile.lastModified() / 1000);
    }

    public ArArchiveEntry setEntrySize(long size) {
        this.length = size;
        return this;
    }

    public long getEntrySize() {
        return this.getLength();
    }

    public ArArchiveEntry setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public int getUserId() {
        return userId;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getMode() {
        return mode;
    }

    public ArArchiveEntry setLastModified(Date date) {
        this.lastModified = date.getTime() / 1000;
        return this;
    }

    /**
     * Last modified time in seconds since the epoch.
     */
    public Date getLastModified() {
        return new Date(1000 * lastModified);
    }

    public long getLength() {
        return length;
    }

    public boolean isDirectory() {
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ArArchiveEntry other = (ArArchiveEntry) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
