
package org.xbib.io.archive.dump;

/**
 * Directory entry.
 */
class Dirent {
    private int ino;
    private int parentIno;
    private int type;
    private String name;

    /**
     * Constructor
     *
     * @param ino
     * @param parentIno
     * @param type
     * @param name
     */
    Dirent(int ino, int parentIno, int type, String name) {
        this.ino = ino;
        this.parentIno = parentIno;
        this.type = type;
        this.name = name;
    }

    /**
     * Get ino.
     *
     * @return the i-node
     */
    int getIno() {
        return ino;
    }

    /**
     * Get ino of parent directory.
     *
     * @return the parent i-node
     */
    int getParentIno() {
        return parentIno;
    }

    /**
     * Get entry type.
     *
     * @return the entry type
     */
    int getType() {
        return type;
    }

    /**
     * Get name of directory entry.
     *
     * @return the directory name
     */
    String getName() {
        return name;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return String.format("[%d]: %s", Integer.valueOf(ino), name);
    }
}
