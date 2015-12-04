package org.xbib.graphics.barcode.impl.int2of5;

import org.xbib.graphics.barcode.ChecksumMode;

/**
 * This class is an implementation of the ITF-14 barcode.
 *
 */
public class ITF14LogicImpl extends Interleaved2Of5LogicImpl {

    /**
     * Main constructor.
     * @param mode the checksum mode
     * @param displayChecksum true if the checksum shall be displayed
     */
    public ITF14LogicImpl(ChecksumMode mode, boolean displayChecksum) {
        super(mode, displayChecksum);
    }

    /** {@inheritDoc} */
    protected String handleChecksum(StringBuffer sb) {
        int msgLen = sb.length();
        if (getChecksumMode() == ChecksumMode.CP_AUTO) {
            switch (msgLen) {
            case 13:
                return doHandleChecksum(sb, ChecksumMode.CP_ADD);
            case 14:
                return doHandleChecksum(sb, ChecksumMode.CP_CHECK);
            default:
                throw new IllegalArgumentException(
                        "Message must have a length of exactly 13 or 14 digits. This message has "
                            + msgLen + " characters.");
            }
        } else {
            if (getChecksumMode() == ChecksumMode.CP_ADD) {
                verifyMessageLength(msgLen, 13);
            } else {
                verifyMessageLength(msgLen, 14);
            }
            return super.handleChecksum(sb);
        }
    }

    private void verifyMessageLength(int actualLength, int expectedLength) {
        if (actualLength != expectedLength) {
            throw new IllegalArgumentException(
                    "Message must have a length of exactly " + expectedLength
                    + " digits. This message has "
                        + actualLength + " characters.");
        }
    }

}
