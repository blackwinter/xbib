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
 * Representation of an ASN.1 NULL.
 * <p/>
 * This class represents a null value. A NULL is used
 * when only the tag is of interest, and not any value.
 *
 */


public final class ASN1Null extends ASN1Any {
    /**
     * This constant is the ASN.1 UNIVERSAL tag value for NULL.
     */

    public static final int TAG = 0x05;

    /**
     * Default constructor for an ASN.1 NULL object. The tag is set
     * to the default tag of UNIVERSAL 5. A NULL has no value.
     */

    public ASN1Null() {
    }

    /**
     * Constructor for an ASN.1 NULL object from a primitive BER encoding.
     *
     * @param ber       The BER encoding to use.
     * @param check_tag If true, it checks the tag. Use false if is implicitly tagged.
     * @exception ASN1Exception If the BER encoding is incorrect.
     */

    public ASN1Null(BEREncoding ber, boolean check_tag)
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

    public void
    ber_decode(BEREncoding ber_enc, boolean check_tag)
            throws ASN1EncodingException {
        if (check_tag) {
            if (ber_enc.tag_get() != TAG ||
                    ber_enc.tag_type_get() != BEREncoding.UNIVERSAL_TAG) {
                throw new ASN1EncodingException
                        ("ASN.1 NULL: bad BER: tag=" + ber_enc.tag_get() +
                                " expected " + TAG + "\n");
            }
        }

        if (ber_enc instanceof BERPrimitive) {
            // We do not check the contents at all since we are not interested
            // in it at all.
        } else {
            throw new ASN1EncodingException("ASN.1 NULL: bad form, constructed");
        }
    }

    /**
     * Returns a BER encoding of the NULL.
     *
     * @return The BER encoding of the NULL
     * @exception ASN1Exception when the NULL is invalid
     * and cannot be encoded.
     */

    public BEREncoding ber_encode()
            throws ASN1Exception {
        return ber_encode(BEREncoding.UNIVERSAL_TAG, TAG);
    }

    /**
     * Returns a BER encoding of the NULL.
     *
     * @return The BER encoding of the NULL
     * @exception ASN1Exception when the NULL is invalid
     * and cannot be encoded.
     */

    public BEREncoding ber_encode(int tag_type, int tag)
            throws ASN1Exception {
        int encoding[] = new int[0]; // trivial encoding
        return new BERPrimitive(tag_type, tag, encoding);
    }

    /**
     * Returns a new String object representing this ASN.1 object's value.
     */

    public String
    toString() {
        return "null";
    }

}