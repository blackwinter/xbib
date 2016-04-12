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
package z3950.v3;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Integer;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;

/**
 * Class for representing a <code>AttributeElement_attributeValue_complex</code> from <code>Z39-50-APDU-1995</code>
 *
 * <pre>
 * AttributeElement_attributeValue_complex ::=
 * SEQUENCE {
 *   list [1] IMPLICIT SEQUENCE OF StringOrNumeric
 *   semanticAction [2] IMPLICIT SEQUENCE OF INTEGER OPTIONAL
 * }
 * </pre>
 *
 */

public final class AttributeElement_attributeValue_complex extends ASN1Any {

    /**
     * Default constructor for a AttributeElement_attributeValue_complex.
     */

    public AttributeElement_attributeValue_complex() {
    }


    /**
     * Constructor for a AttributeElement_attributeValue_complex from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public AttributeElement_attributeValue_complex(BEREncoding ber, boolean check_tag)
            throws ASN1Exception {
        super(ber, check_tag);
    }

    /**
     * Initializing object from a BER encoding.
     * This method is for internal use only. You should use
     * the constructor that takes a BEREncoding.
     *
     * @param ber       the BER to decode.
     * @param check_tag if the tag should be checked.
     * @throws ASN1Exception if the BER encoding is bad.
     */

    public void
    ber_decode(BEREncoding ber, boolean check_tag)
            throws ASN1Exception {
        // AttributeElement_attributeValue_complex should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun AttributeElement_attributeValue_complex: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Decoding: list [1] IMPLICIT SEQUENCE OF StringOrNumeric

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun AttributeElement_attributeValue_complex: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 1 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun AttributeElement_attributeValue_complex: bad tag in s_list\n");
        }

        try {
            BERConstructed cons = (BERConstructed) p;
            int parts = cons.number_components();
            s_list = new StringOrNumeric[parts];
            int n;
            for (n = 0; n < parts; n++) {
                s_list[n] = new StringOrNumeric(cons.elementAt(n), true);
            }
        } catch (ClassCastException e) {
            throw new ASN1EncodingException("Bad BER");
        }
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_semanticAction = null;

        // Decoding: semanticAction [2] IMPLICIT SEQUENCE OF INTEGER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_semanticAction = new ASN1Integer[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_semanticAction[n] = new ASN1Integer(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("AttributeElement_attributeValue_complex: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }

    /**
     * Returns a BER encoding of the AttributeElement_attributeValue_complex.
     *
     * @exception ASN1Exception Invalid or cannot be encoded.
     * @return The BER encoding.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        return ber_encode(BEREncoding.UNIVERSAL_TAG, ASN1Sequence.TAG);
    }

    /**
     * Returns a BER encoding of AttributeElement_attributeValue_complex, implicitly tagged.
     *
     * @param tag_type The type of the implicit tag.
     * @param tag      The implicit tag.
     * @return The BER encoding of the object.
     * @exception ASN1Exception When invalid or cannot be encoded.
     * @see asn1.BEREncoding#UNIVERSAL_TAG
     * @see asn1.BEREncoding#APPLICATION_TAG
     * @see asn1.BEREncoding#CONTEXT_SPECIFIC_TAG
     * @see asn1.BEREncoding#PRIVATE_TAG
     */

    public BEREncoding
    ber_encode(int tag_type, int tag)
            throws ASN1Exception {
        // Calculate the number of fields in the encoding

        int num_fields = 1; // number of mandatories
        if (s_semanticAction != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding f2[];
        int p;

        // Encoding s_list: SEQUENCE OF

        f2 = new BEREncoding[s_list.length];

        for (p = 0; p < s_list.length; p++) {
            f2[p] = s_list[p].ber_encode();
        }

        fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 1, f2);

        // Encoding s_semanticAction: SEQUENCE OF OPTIONAL

        if (s_semanticAction != null) {
            f2 = new BEREncoding[s_semanticAction.length];

            for (p = 0; p < s_semanticAction.length; p++) {
                f2[p] = s_semanticAction[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 2, f2);
        }

        return new BERConstructed(tag_type, tag, fields);
    }

    /**
     * Returns a new String object containing a text representing
     * of the AttributeElement_attributeValue_complex.
     */

    public String
    toString() {
        int p;
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        str.append("list ");
        str.append("{");
        for (p = 0; p < s_list.length; p++) {
            if (p != 0) {
             str.append(", ");
            }
            str.append(s_list[p]);
        }
        str.append("}");
        outputted++;

        if (s_semanticAction != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("semanticAction ");
            str.append("{");
            for (p = 0; p < s_semanticAction.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_semanticAction[p]);
            }
            str.append("}");
            outputted++;
        }

        str.append("}");

        return str.toString();
    }

/*
 * Internal variables for class.
 */

    public StringOrNumeric s_list[];
    public ASN1Integer s_semanticAction[]; // optional

}