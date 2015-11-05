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
 * Representation of an ASN.1 <code>BOOLEAN</code>.
 * <p/>
 * <p/>
 * The BOOLEAN type denotes a Boolean value: either true or false.
 *
 */
public final class ASN1Boolean extends ASN1Any {
    /**
     * This constant is the ASN.1 UNIVERSAL tag value for BOOLEAN.
     */

    public static final int TAG = 0x01;

    /**
     * Default constructor for an ASN.1 BOOLEAN object. It sets the tag
     * to the default of UNIVERSAL 1, and the value to bool.
     *
     * @param    bool the value of the BOOLEAN.
     */

    public ASN1Boolean(boolean bool) {
        value = bool;
    }

    /**
     * Constructor for an ASN.1 BOOLEAN object from a BER encoding.
     *
     * @param ber       The BER encoding to use.
     * @param check_tag If true, it checks the tag. Use false if is implicitly tagged.
     * @exception ASN1Exception if the BER encoding is incorrect.
     */

    public ASN1Boolean(BEREncoding ber, boolean check_tag)
            throws ASN1Exception {
        super(ber, check_tag); // superclass will call ber_decode
    }

    /**
     * Method for initializing the object from a BER encoding.
     *
     * @param ber_enc       The BER encoding to use.
     * @param check_tag If true, it checks the tag. Use false if is implicitly tagged.
     * @exception ASN1EncodingException if the BER encoding is incorrect.
     */

    public void ber_decode(BEREncoding ber_enc, boolean check_tag)
            throws ASN1EncodingException {
        if (check_tag) {
            if (ber_enc.tag_get() != TAG ||
                    ber_enc.tag_type_get() != BEREncoding.UNIVERSAL_TAG) {
                throw new ASN1EncodingException
                        ("ASN.1 BOOLEAN: bad BER: tag=" + ber_enc.tag_get() +
                                " expected " + "TAG\n");
            }
        }

        if (ber_enc instanceof BERPrimitive) {
            BERPrimitive ber = (BERPrimitive) ber_enc;

            int encoding[] = ber.peek();

            if (encoding.length != 1) {
                throw new ASN1EncodingException
                        ("ASN.1 BOOLEAN: invalid encoding, length = " + encoding.length);
            }

            if (encoding[0] == 0) {
                value = false;
            } else {
                value = true;
            }
        } else {
            throw new ASN1EncodingException
                    ("ASN.1 BOOLEAN: bad BER: decoding constructed NOT IMPLEMENTED YET");
            //???
        }
    }

    /**
     * Returns a BER encoding of the BOOLEAN.
     *
     * @return The BER encoding of the BOOLEAN
     * @exception ASN1Exception when the BOOLEAN is invalid
     * and cannot be encoded.
     */
    public BEREncoding ber_encode()
            throws ASN1Exception {
        return ber_encode(BEREncoding.UNIVERSAL_TAG, TAG);
    }

    /**
     * Returns a BER encoding of the BOOLEAN. Implicitly tagged.
     *
     * @return The BER encoding of the BOOLEAN
     * @exception ASN1Exception when the BOOLEAN is invalid
     * and cannot be encoded.
     */

    public BEREncoding ber_encode(int tag_type, int tag)
            throws ASN1Exception {
        // Generate BER encoding of the Boolean

        int encoding[] = new int[1];

        if (value) {
            encoding[0] = 0xff; // TRUE (in fact, any non-zero will do)
        } else {
            encoding[0] = 0x00; // FALSE
        }

        return new BERPrimitive(tag_type, tag, encoding);
    }

    /**
     * Method to set the boolean's value.
     *
     * @param new_val the value to set the BOOLEAN to.
     */

    public ASN1Boolean
    set(boolean new_val) {
        value = new_val;
        return this;
    }

    /**
     * Method to get the boolean's value.
     *
     * @return the BOOLEAN's current value.
     */

    public boolean
    get() {
        return value;
    }

    /**
     * Returns a new String object representing this ASN.1 object's value.
     */

    public String
    toString() {
        return ((value) ? "true" : "false");
    }

    /**
     * The value of the BOOLEAN is stored in this variable.
     */

    private boolean value;

}