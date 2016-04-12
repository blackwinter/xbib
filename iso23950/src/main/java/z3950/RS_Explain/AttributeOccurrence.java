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
package z3950.RS_Explain;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Integer;
import asn1.ASN1Null;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.AttributeSetId;



/**
 * Class for representing a <code>AttributeOccurrence</code> from <code>RecordSyntax-explain</code>
 * <p/>
 * <pre>
 * AttributeOccurrence ::=
 * SEQUENCE {
 *   attributeSet [0] IMPLICIT AttributeSetId OPTIONAL
 *   attributeType [1] IMPLICIT INTEGER
 *   mustBeSupplied [2] IMPLICIT NULL OPTIONAL
 *   attributeValues AttributeOccurrence_attributeValues
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class AttributeOccurrence extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a AttributeOccurrence.
     */

    public AttributeOccurrence() {
    }



    /**
     * Constructor for a AttributeOccurrence from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public AttributeOccurrence(BEREncoding ber, boolean check_tag)
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
        // AttributeOccurrence should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun AttributeOccurrence: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Decoding: attributeSet [0] IMPLICIT AttributeSetId OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun AttributeOccurrence: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 0 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_attributeSet = new AttributeSetId(p, false);
            part++;
        }

        // Decoding: attributeType [1] IMPLICIT INTEGER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun AttributeOccurrence: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 1 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun AttributeOccurrence: bad tag in s_attributeType\n");
        }

        s_attributeType = new ASN1Integer(p, false);
        part++;

        // Decoding: mustBeSupplied [2] IMPLICIT NULL OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun AttributeOccurrence: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_mustBeSupplied = new ASN1Null(p, false);
            part++;
        }

        // Decoding: attributeValues AttributeOccurrence_attributeValues

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun AttributeOccurrence: incomplete");
        }
        p = ber_cons.elementAt(part);

        s_attributeValues = new AttributeOccurrence_attributeValues(p, true);
        part++;

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Zebulun AttributeOccurrence: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the AttributeOccurrence.
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
     * Returns a BER encoding of AttributeOccurrence, implicitly tagged.
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

        int num_fields = 2; // number of mandatories
        if (s_attributeSet != null) {
            num_fields++;
        }
        if (s_mustBeSupplied != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;

        // Encoding s_attributeSet: AttributeSetId OPTIONAL

        if (s_attributeSet != null) {
            fields[x++] = s_attributeSet.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 0);
        }

        // Encoding s_attributeType: INTEGER

        fields[x++] = s_attributeType.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);

        // Encoding s_mustBeSupplied: NULL OPTIONAL

        if (s_mustBeSupplied != null) {
            fields[x++] = s_mustBeSupplied.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
        }

        // Encoding s_attributeValues: AttributeOccurrence_attributeValues

        fields[x++] = s_attributeValues.ber_encode();

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the AttributeOccurrence.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        if (s_attributeSet != null) {
            str.append("attributeSet ");
            str.append(s_attributeSet);
            outputted++;
        }

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("attributeType ");
        str.append(s_attributeType);
        outputted++;

        if (s_mustBeSupplied != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("mustBeSupplied ");
            str.append(s_mustBeSupplied);
            outputted++;
        }

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("attributeValues ");
        str.append(s_attributeValues);
        outputted++;

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public AttributeSetId s_attributeSet; // optional
    public ASN1Integer s_attributeType;
    public ASN1Null s_mustBeSupplied; // optional
    public AttributeOccurrence_attributeValues s_attributeValues;

} // AttributeOccurrence


//EOF
