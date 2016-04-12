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
package asn1;

/**
 * Representation of an ASN.1 INTEGER.
 * <p/>
 * The <code>INTEGER</code> type denotes an arbitary integer.
 * ASN.1 INTEGER values can be positive, negative, or zero; and can
 * have any magnitude.
 * <p/>
 * <p/>
 * The current implementation limits the values of INTEGERs to 32-bit
 * two's complement values.
 *
 */
public final class ASN1Integer extends ASN1Any {
    /**
     * This constant is the ASN.1 UNIVERSAL tag value for INTEGER.
     */

    public static final int TAG = 0x02;

    /**
     * Constructor for an ASN.1 INTEGER object. The tag is
     * set to the default tag of UNIVERSAL 2, and the value to the
     * given number.
     *
     * @param    number the value of the INTEGER.
     */

    public ASN1Integer(int number) {
        value = number;
    }

    /**
     * Constructor for an ASN.1 INTEGER object from a BER encoding.
     *
     * @param ber       The BER encoding to use.
     * @param check_tag If true, it checks the tag. Use false if is implicitly tagged.
     * @exception ASN1Exception If the BER encoding is incorrect.
     */

    public ASN1Integer(BEREncoding ber, boolean check_tag)
            throws ASN1Exception {
        super(ber, check_tag); // superclass will call ber_decode
    }

    /**
     * Method for initializing the object from a BER encoding.
     *
     * @param ber_enc   The BER encoding to use.
     * @param check_tag If true, it checks the tag. Use false if is implicitly tagged.
     * @exception ASN1EncodingException If the BER encoding is incorrect.
     */

    public void ber_decode(BEREncoding ber_enc, boolean check_tag)
            throws ASN1EncodingException {
        if (check_tag) {
            if (ber_enc.tag_get() != TAG ||
                    ber_enc.tag_type_get() != BEREncoding.UNIVERSAL_TAG) {
                throw new ASN1EncodingException
                        ("ASN.1 INTEGER: bad BER: tag=" + ber_enc.tag_get() +
                                " expected " + TAG + "\n");
            }
        }

        if (!(ber_enc instanceof BERPrimitive)) {
            throw new ASN1EncodingException("ASN.1 INTEGER: bad form, constructed");
        }

        BERPrimitive ber = (BERPrimitive) ber_enc;

        int encoding[] = ber.peek();

        if (encoding.length < 1) {
            throw new ASN1EncodingException
                    ("ASN.1 INTEGER: invalid encoding, length = " + encoding.length);
        }

        value = (byte) encoding[0]; // to ensure sign extension

        for (int x = 1; x < encoding.length; x++) {
            value <<= 8;
            value |= (encoding[x] & 0xff);
        }
    }

    /**
     * Returns a BER encoding of the INTEGER.
     *
     * @return The BER encoding of the INTEGER
     * @exception ASN1Exception when the INTEGER is invalid
     * and cannot be encoded.
     */

    public BEREncoding ber_encode()
            throws ASN1Exception {
        return ber_encode(BEREncoding.UNIVERSAL_TAG, TAG);
    }

    /**
     * Returns a BER encoding of the INTEGER. Explicitly tagged with
     * the supplied tag.
     *
     * @return The BER encoding of the INTEGER
     * @exception ASN1Exception when the INTEGER is invalid
     * and cannot be encoded.
     */

    public BEREncoding ber_encode(int tag_type, int tag)
            throws ASN1Exception {
        // Calculate length of encoding

        int length = 0;

        int shifted = value;
        if (value < 0) {
            shifted = ~value;
        }

        boolean need_pad; // when MSB confuses sign (causes 1st 0x00 or 0xFF digit)

        do {
            need_pad = ((shifted & 0x80) == 0x80);

            shifted >>= 8;
            length++;
        } while (shifted != 0);

        if (need_pad) {
            length++;
        }

        // Generate BER encoding of the integer (base 256 encoding)

        int encoding[] = new int[length];

        int index = 0;
        while (0 < length) {
            encoding[index++] = ((value >> (8 * (length - 1))) & 0xff);
            length--;
        }

        return new BERPrimitive(tag_type, tag, encoding);
    }

    /**
     * Method to set the integer's value.
     *
     * @param new_val the value to set the INTEGER to.
     * @return the object.
     */

    public ASN1Integer
    set(int new_val) {
        value = new_val;
        return this;
    }

    /**
     * Method to get the integer's value.
     *
     * @return the INTEGER's current value.
     */

    public int
    get() {
        return value;
    }

    /**
     * Returns a new String object representing this ASN.1 object's value.
     */

    public String
    toString() {
        return String.valueOf(value);
    }

    /**
     * The value of the INTEGER is stored in this variable.
     * This is private for good information hiding, so that we are able
     * to change its representation (e.g. to a long) at a later date
     * without affecting the interface.
     */

    private int value;

}