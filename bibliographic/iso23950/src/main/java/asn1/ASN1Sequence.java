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
 * Representation of an ASN.1 SEQUENCE.
 * <p/>
 * <p/>
 * The <code>SEQUENCE</code> type denotes an ordered collection
 * of one or more types. The SEQUENCE OF type denotes an ordered
 * collection of zero or more occurances of a given type.
 * <p/>
 * <p/>
 * This class is available for the generic handling of ASN.1
 * definitions. However, specialised ASN.1 productions will usually
 * use their own encoding for SEQUENCES directly.
 *
 */

public final class ASN1Sequence extends ASN1Any {
    /**
     * This constant tag value is the ASN.1 UNIVERSAL tag value for
     * a SEQUENCE or a SEQUENCE OF type.
     */

    public static final int TAG = 0x10;

    /**
     * Default constructor for an ASN.1 SEQUENCE object. The tag is set
     * to the default value.
     *
     * @param element_array the ASN.1 objects that make up the sequence.
     */

    public ASN1Sequence(ASN1Any element_array[]) {
        elements = element_array;
    }

    /**
     * Constructor for an ASN.1 SEQUENCE object from a BER encoding.
     *
     * @param ber       The BER encoding to use.
     * @param check_tag If true, it checks the tag. Use false if is implicitly tagged.
     * @exception ASN1Exception If the BER encoding is incorrect.
     */

    public ASN1Sequence(BEREncoding ber, boolean check_tag)
            throws ASN1Exception {
        super(ber, check_tag);
    }

    /**
     * Method for initializing the object from a BER encoding.
     *
     * @param ber_enc   The BER encoding to use.
     * @param check_tag If true, it checks the tag. Use false if is implicitly tagged.
     * @exception ASN1Exception If the BER encoding is incorrect.
     */

    public void ber_decode(BEREncoding ber_enc, boolean check_tag)
            throws ASN1Exception {
        if (check_tag) {
            if (ber_enc.tag_get() != TAG ||
                    ber_enc.tag_type_get() != BEREncoding.UNIVERSAL_TAG) {
                throw new ASN1EncodingException
                        ("ASN.1 SEQUENCE: bad BER: tag=" + ber_enc.tag_get() +
                                " expected " + TAG + "\n");
            }
        }

        if (ber_enc instanceof BERPrimitive) {
            throw new ASN1EncodingException("ASN.1 SEQUENCE: bad form, primitive");
        }

        BERConstructed ber = (BERConstructed) ber_enc;

        int len = ber.number_components();

        elements = new ASN1Any[len];

        for (int x = 0; x < len; x++) {
            elements[x] = ASN1Decoder.toASN1(ber.elementAt(x));
        }
    }

    /**
     * Returns a BER encoding with no implicit tag.
     *
     * @return The BER encoding
     * @exception ASN1Exception when the object is invalid and cannot be encoded.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        return ber_encode(BEREncoding.UNIVERSAL_TAG, TAG);
    }

    /**
     * Returns a BER encoding of the SEQUENCE implcitly tagged.
     *
     * @param tag_type The type of the implcit tag
     * @param tag      The implicit tag number
     * @return The BER encoding of the SEQUENCE
     * @exception ASN1Exception when the SEQUENCE is invalid
     * and cannot be encoded.
     */

    public BEREncoding
    ber_encode(int tag_type, int tag)
            throws ASN1Exception {
        int len = elements.length;
        BEREncoding encodings[] = new BEREncoding[len];

        for (int index = 0; index < len; index++) {
            encodings[index] = elements[index].ber_encode();
        }

        return new BERConstructed(tag_type, tag, encodings);
    }


    /**
     * Method to set the SEQUENCE's elements.
     *
     * @param element_array an array of ASN.1 object.
     */

    public ASN1Sequence
    set(ASN1Any element_array[]) {
        elements = element_array;
        return this;
    }

    /**
     * Method to get the elements of the SEQUENCE.
     *
     * @return an array containing the SEQUENCE's elements.
     */

    public ASN1Any[]
    get() {
        return elements;
    }

    /**
     * Returns a new String object representing this ASN.1 object's value.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");

        for (int index = 0; index < elements.length; index++) {
            if (index != 0) {
                str.append(", ");
            }

            str.append(elements[index].toString());
        }

        str.append('}');

        return new String(str);
    }

    /**
     * The values of the SEQUENCE are stored in this array.
     */

    private ASN1Any elements[];

}