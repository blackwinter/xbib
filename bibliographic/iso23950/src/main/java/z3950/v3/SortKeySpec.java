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
 * Class for representing a <code>SortKeySpec</code> from <code>Z39-50-APDU-1995</code>
 * <p/>
 * <pre>
 * SortKeySpec ::=
 * SEQUENCE {
 *   sortElement SortElement
 *   sortRelation [1] IMPLICIT INTEGER
 *   caseSensitivity [2] IMPLICIT INTEGER
 *   missingValueAction [3] EXPLICIT SortKeySpec_missingValueAction OPTIONAL
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class SortKeySpec extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a SortKeySpec.
     */

    public SortKeySpec() {
    }



    /**
     * Constructor for a SortKeySpec from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public SortKeySpec(BEREncoding ber, boolean check_tag)
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
        // SortKeySpec should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun SortKeySpec: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;
        BERConstructed tagged;

        // Decoding: sortElement SortElement

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SortKeySpec: incomplete");
        }
        p = ber_cons.elementAt(part);

        s_sortElement = new SortElement(p, true);
        part++;

        // Decoding: sortRelation [1] IMPLICIT INTEGER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SortKeySpec: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 1 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun SortKeySpec: bad tag in s_sortRelation\n");
        }

        s_sortRelation = new ASN1Integer(p, false);
        part++;

        // Decoding: caseSensitivity [2] IMPLICIT INTEGER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SortKeySpec: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 2 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun SortKeySpec: bad tag in s_caseSensitivity\n");
        }

        s_caseSensitivity = new ASN1Integer(p, false);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at endStream of BER

        s_missingValueAction = null;

        // Decoding: missingValueAction [3] EXPLICIT SortKeySpec_missingValueAction OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 3 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagged = (BERConstructed) p;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun SortKeySpec: bad BER encoding: s_missingValueAction tag bad\n");
            }
            if (tagged.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun SortKeySpec: bad BER encoding: s_missingValueAction tag bad\n");
            }

            s_missingValueAction = new SortKeySpec_missingValueAction(tagged.elementAt(0), true);
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Zebulun SortKeySpec: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the SortKeySpec.
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
     * Returns a BER encoding of SortKeySpec, implicitly tagged.
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

        int num_fields = 3; // number of mandatories
        if (s_missingValueAction != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding enc[];

        // Encoding s_sortElement: SortElement

        fields[x++] = s_sortElement.ber_encode();

        // Encoding s_sortRelation: INTEGER

        fields[x++] = s_sortRelation.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);

        // Encoding s_caseSensitivity: INTEGER

        fields[x++] = s_caseSensitivity.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);

        // Encoding s_missingValueAction: SortKeySpec_missingValueAction OPTIONAL

        if (s_missingValueAction != null) {
            enc = new BEREncoding[1];
            enc[0] = s_missingValueAction.ber_encode();
            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 3, enc);
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the SortKeySpec.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        str.append("sortElement ");
        str.append(s_sortElement);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("sortRelation ");
        str.append(s_sortRelation);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("caseSensitivity ");
        str.append(s_caseSensitivity);
        outputted++;

        if (s_missingValueAction != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("missingValueAction ");
            str.append(s_missingValueAction);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public SortElement s_sortElement;
    public ASN1Integer s_sortRelation;
    public ASN1Integer s_caseSensitivity;
    public SortKeySpec_missingValueAction s_missingValueAction; // optional


/*
 * Enumerated constants for class.
 */

    // Enumerated constants for sortRelation
    public static final int E_ascending = 0;
    public static final int E_descending = 1;
    public static final int E_ascendingByFrequency = 3;
    public static final int E_descendingByfrequency = 4;

    // Enumerated constants for caseSensitivity
    public static final int E_caseSensitive = 0;
    public static final int E_caseInsensitive = 1;

} // SortKeySpec


//EOF
