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
import asn1.ASN1Boolean;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Integer;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;



/**
 * Class for representing a <code>ProximityOperator</code> from <code>Z39-50-APDU-1995</code>
 * <p/>
 * <pre>
 * ProximityOperator ::=
 * SEQUENCE {
 *   exclusion [1] IMPLICIT BOOLEAN OPTIONAL
 *   distance [2] IMPLICIT INTEGER
 *   ordered [3] IMPLICIT BOOLEAN
 *   relationType [4] IMPLICIT INTEGER
 *   proximityUnitCode [5] EXPLICIT ProximityOperator_proximityUnitCode
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class ProximityOperator extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a ProximityOperator.
     */

    public ProximityOperator() {
    }



    /**
     * Constructor for a ProximityOperator from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public ProximityOperator(BEREncoding ber, boolean check_tag)
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
        // ProximityOperator should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun ProximityOperator: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;
        BERConstructed tagged;

        // Decoding: exclusion [1] IMPLICIT BOOLEAN OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun ProximityOperator: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 1 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_exclusion = new ASN1Boolean(p, false);
            part++;
        }

        // Decoding: distance [2] IMPLICIT INTEGER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun ProximityOperator: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 2 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun ProximityOperator: bad tag in s_distance\n");
        }

        s_distance = new ASN1Integer(p, false);
        part++;

        // Decoding: ordered [3] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun ProximityOperator: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 3 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun ProximityOperator: bad tag in s_ordered\n");
        }

        s_ordered = new ASN1Boolean(p, false);
        part++;

        // Decoding: relationType [4] IMPLICIT INTEGER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun ProximityOperator: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 4 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun ProximityOperator: bad tag in s_relationType\n");
        }

        s_relationType = new ASN1Integer(p, false);
        part++;

        // Decoding: proximityUnitCode [5] EXPLICIT ProximityOperator_proximityUnitCode

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun ProximityOperator: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 5 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun ProximityOperator: bad tag in s_proximityUnitCode\n");
        }

        try {
            tagged = (BERConstructed) p;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun ProximityOperator: bad BER encoding: s_proximityUnitCode tag bad\n");
        }
        if (tagged.number_components() != 1) {
            throw new ASN1EncodingException
                    ("Zebulun ProximityOperator: bad BER encoding: s_proximityUnitCode tag bad\n");
        }

        s_proximityUnitCode = new ProximityOperator_proximityUnitCode(tagged.elementAt(0), true);
        part++;

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Zebulun ProximityOperator: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the ProximityOperator.
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
     * Returns a BER encoding of ProximityOperator, implicitly tagged.
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

        int num_fields = 4; // number of mandatories
        if (s_exclusion != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding enc[];

        // Encoding s_exclusion: BOOLEAN OPTIONAL

        if (s_exclusion != null) {
            fields[x++] = s_exclusion.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
        }

        // Encoding s_distance: INTEGER

        fields[x++] = s_distance.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);

        // Encoding s_ordered: BOOLEAN

        fields[x++] = s_ordered.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);

        // Encoding s_relationType: INTEGER

        fields[x++] = s_relationType.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);

        // Encoding s_proximityUnitCode: ProximityOperator_proximityUnitCode

        enc = new BEREncoding[1];
        enc[0] = s_proximityUnitCode.ber_encode();
        fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 5, enc);

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the ProximityOperator.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        if (s_exclusion != null) {
            str.append("exclusion ");
            str.append(s_exclusion);
            outputted++;
        }

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("distance ");
        str.append(s_distance);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("ordered ");
        str.append(s_ordered);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("relationType ");
        str.append(s_relationType);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("proximityUnitCode ");
        str.append(s_proximityUnitCode);
        outputted++;

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public ASN1Boolean s_exclusion; // optional
    public ASN1Integer s_distance;
    public ASN1Boolean s_ordered;
    public ASN1Integer s_relationType;
    public ProximityOperator_proximityUnitCode s_proximityUnitCode;


/*
 * Enumerated constants for class.
 */

    // Enumerated constants for relationType
    public static final int E_lessThan = 1;
    public static final int E_lessThanOrEqual = 2;
    public static final int E_equal = 3;
    public static final int E_greaterThanOrEqual = 4;
    public static final int E_greaterThan = 5;
    public static final int E_notEqual = 6;

} // ProximityOperator


//EOF
