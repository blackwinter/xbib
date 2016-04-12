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
 * Representation of an ASN.1 OBJECT IDENTIFIER.
 * <p/>
 * The <code>OBJECT IDENTIFIER<code> type denotes an object identifier,
 * which is a sequence of integer components. An OBJECT IDENTIFIER can
 * have any number of components, whch are generally non-negative.
 * This type is a non-string type.
 *
 */
public final class ASN1ObjectIdentifier extends ASN1Any {
    /**
     * This constant is the ASN.1 UNIVERSAL tag value for OBJECT IDENTIFIER.
     */

    public static final int TAG = 0x06;

    /**
     * Constructor for an ASN.1 OBJECT IDENTIFER object. The tag is set
     * to the default tag of UNIVERSAL 6, and the given OID value.
     */

    public ASN1ObjectIdentifier(int oid_value[]) {
        oid = oid_value;
    }

    /**
     * Constructor for an ASN.1 OBJECT IDENTIFIER object from a BER encoding.
     *
     * @param ber       The BER encoding to use.
     * @param check_tag If true, it checks the tag. Use false if is implicitly tagged.
     * @exception ASN1Exception If the BER encoding is incorrect.
     */

    public ASN1ObjectIdentifier(BEREncoding ber, boolean check_tag)
            throws ASN1Exception {
        super(ber, check_tag); // superclass will call ber_decode
    }

    /**
     * Method for initializing the object from a BER encoding.
     *
     * @param ber_enc   The BER encoding to use.
     * @param check_tag If true, it checks the tag. Use false if is implicitly tagged.
     * @exception ASN1EncodingException If the BER encoding cannot be decoded.
     */

    public void
    ber_decode(BEREncoding ber_enc, boolean check_tag)
            throws ASN1EncodingException {
        if (check_tag) {
            if (ber_enc.tag_get() != TAG ||
                    ber_enc.tag_type_get() != BEREncoding.UNIVERSAL_TAG) {
                throw new ASN1EncodingException
                        ("ASN.1 OBJECT IDENTIFIER: bad BER: tag=" + ber_enc.tag_get() +
                                " expected " + TAG + "\n");
            }
        }

        if (!(ber_enc instanceof BERPrimitive)) {
            throw new ASN1EncodingException
                    ("ASN.1 OBJECT IDENTIFIER: bad form, constructed");
        }

        BERPrimitive ber = (BERPrimitive) ber_enc;

        int encoding[] = ber.peek();

        if (encoding.length < 2) {
            throw new ASN1EncodingException
                    ("ASN1 OBJECT IDENTIFER: invalid encoding, length = " +
                            encoding.length);
        }

        // Calculate total number of components

        int num_components = 2;
        for (int index = 1; index < encoding.length; index++) {
            // Each value is in base 128, with bit-8 set except in last byte

            if ((encoding[index] & 0x80) == 0) {
                num_components++;
            }
        }

        // Allocate space to store it

        oid = new int[num_components];

        // Decode

        oid[0] = encoding[0] / 40; // first octet encodes first two values
        oid[1] = encoding[0] % 40;

        int index = 1;
        for (int component = 2; component < num_components; component++) {
            // Rest of values in base 128, with bit-8 set except for in last byte

            oid[component] = 0;

            int octet;
            do {
                octet = encoding[index++];

                oid[component] <<= 7;
                oid[component] |= (octet & 0x7f);
            } while ((octet & 0x80) != 0);
        }
    }


    /**
     * Returns a BER encoding of the OBJECT IDENTIFIER.
     * The current implementation rejects negative OID components (should it?)
     *
     * @return The BER encoding of the OBJECT IDENTIFIER
     * @exception ASN1Exception when the OBJECT IDENTIFIER is invalid
     * and cannot be encoded. According to X.208, an OBJECT
     * IDENTIFIER must have at least two components, the first
     * has values of (0, 1, or 2) and the second between 0 and 39
     * inclusive.
     */

    public BEREncoding ber_encode()
            throws ASN1Exception {
        return ber_encode(BEREncoding.UNIVERSAL_TAG, TAG);
    }


    /**
     * Returns a BER encoding of the OBJECT IDENTIFIER.
     * The current implementation rejects negative OID components (should it?)
     *
     * @return The BER encoding of the OBJECT IDENTIFIER
     * @exception ASN1Exception when the OBJECT IDENTIFIER is invalid
     * and cannot be encoded. According to X.208, an OBJECT
     * IDENTIFIER must have at least two components, the first
     * has values of (0, 1, or 2) and the second between 0 and 39
     * inclusive.
     */
    public BEREncoding ber_encode(int tag_type, int tag)
            throws ASN1Exception {
        // Validity checking

        if (oid.length < 2) {
            throw new ASN1Exception
                    ("OBJECT IDENTIFIER: less than 2 components, violates X.208");
        }
        if (oid[0] < 0 || 2 < oid[0]) {
            throw new ASN1Exception
                    ("OBJECT IDENTIFIER: First component invalid, value = " + oid[0]);
        }
        if (oid[1] < 0 || 39 < oid[1]) {
            throw new ASN1Exception
                    ("OBJECT IDENTIFIER: Second component invalid, value = " + oid[1]);
        }

        // Calculate total length of encoding

        int num_bytes = 1;
        for (int index = 2; index < oid.length; index++) {
            // Each value in base 128, with bit-8 set except for in last byte

            int tmp_value = oid[index];

            if (tmp_value < 0) {
                throw new ASN1Exception
                        ("OBJECT IDENTIFIER: component " + (index + 1) +
                                " is negative, value = " + tmp_value);
            }

            do {
                num_bytes++;
                tmp_value >>= 7;
            } while (tmp_value != 0);
        }

        // Encode

        int octets[] = new int[num_bytes];

        octets[0] = (40 * oid[0]) + oid[1]; // first octet encodes first two values

        int bcount = 0;
        for (int index = 2; index < oid.length; index++) {
            // Rest of values in base 128, with bit-8 set except for in last byte

            int number_bytes = 0;
            int tmp_value = oid[index];
            do {
                number_bytes++;
                tmp_value >>= 7;
            } while (tmp_value != 0);

            tmp_value = oid[index];

            for (int digit = number_bytes - 1; 0 <= digit; digit--) {
                octets[++bcount] = (tmp_value >> (digit * 7)) & 0x7f;
                if (digit != 0) {
                    octets[bcount] |= 0x80; // bit-8 set in last byte
                }
            }
        }

        return new BERPrimitive(tag_type, tag, octets);
    }

    /**
     * Method to set the OBJECT IDENTIFIER's value.
     *
     * @param new_val the value to set the OBJECT IDENTIFIER to.
     */

    public ASN1ObjectIdentifier set(int new_val[]) {
        oid = new_val;
        return this;
    }


    /**
     * Method to get the OBJECT IDENTIFIER's value. The returned value
     * should not be modified in any way.
     *
     * @return the OBJECT IDENTIFIER's current value.
     */

    public int[] get() {
        return oid;
    }

    /**
     * Returns a new String object representing this ASN.1 object's value.
     */

    public String toString() {
        StringBuffer str = new StringBuffer();

        for (int index = 0; index < oid.length; index++) {
            if (index != 0) {
                str.append('.');
            }

            str.append(String.valueOf(oid[index]));
        }

        return new String(str);
    }

    /**
     * The components of the OBJECT IDENTIFER are stored in this
     * variable as an array of integers.
     */

    private int oid[];

}