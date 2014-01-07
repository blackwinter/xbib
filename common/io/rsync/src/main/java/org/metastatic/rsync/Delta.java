
package org.metastatic.rsync;

/**
 * A Delta is, in the Rsync algorithm, one of two things: (1) a block
 * of bytes and an offset, or (2) a pair of offsets, one old and one
 * new.
 *
 * @see DataBlock
 * @see Offsets
 */
public interface Delta {
    /**
     * The size of the block of data this class represents.
     *
     * @return The size of the block of data this class represents.
     */
    int getBlockLength();

    /**
     * Get the offset at which this Delta should be written.
     *
     * @return The write offset.
     */
    long getWriteOffset();
}
