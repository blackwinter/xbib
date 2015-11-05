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
 * Class for representing a <code>ScanRequest</code> from <code>Z39-50-APDU-1995</code>
 * <p/>
 * <pre>
 * ScanRequest ::=
 * SEQUENCE {
 *   referenceId ReferenceId OPTIONAL
 *   databaseNames [3] IMPLICIT SEQUENCE OF DatabaseName
 *   attributeSet AttributeSetId OPTIONAL
 *   termListAndStartPoint AttributesPlusTerm
 *   stepSize [5] IMPLICIT INTEGER OPTIONAL
 *   numberOfTermsRequested [6] IMPLICIT INTEGER
 *   preferredPositionInResponse [7] IMPLICIT INTEGER OPTIONAL
 *   otherInfo OtherInformation OPTIONAL
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class ScanRequest extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a ScanRequest.
     */

    public ScanRequest() {
    }



    /**
     * Constructor for a ScanRequest from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public ScanRequest(BEREncoding ber, boolean check_tag)
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
        // ScanRequest should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun ScanRequest: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Decoding: referenceId ReferenceId OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun ScanRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        try {
            s_referenceId = new ReferenceId(p, true);
            part++; // yes, consumed
        } catch (ASN1Exception e) {
            s_referenceId = null; // no, not present
        }

        // Decoding: databaseNames [3] IMPLICIT SEQUENCE OF DatabaseName

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun ScanRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 3 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun ScanRequest: bad tag in s_databaseNames\n");
        }

        try {
            BERConstructed cons = (BERConstructed) p;
            int parts = cons.number_components();
            s_databaseNames = new DatabaseName[parts];
            int n;
            for (n = 0; n < parts; n++) {
                s_databaseNames[n] = new DatabaseName(cons.elementAt(n), true);
            }
        } catch (ClassCastException e) {
            throw new ASN1EncodingException("Bad BER");
        }
        part++;

        // Decoding: attributeSet AttributeSetId OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun ScanRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        try {
            s_attributeSet = new AttributeSetId(p, true);
            part++; // yes, consumed
        } catch (ASN1Exception e) {
            s_attributeSet = null; // no, not present
        }

        // Decoding: termListAndStartPoint AttributesPlusTerm

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun ScanRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        s_termListAndStartPoint = new AttributesPlusTerm(p, true);
        part++;

        // Decoding: stepSize [5] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun ScanRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 5 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_stepSize = new ASN1Integer(p, false);
            part++;
        }

        // Decoding: numberOfTermsRequested [6] IMPLICIT INTEGER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun ScanRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 6 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun ScanRequest: bad tag in s_numberOfTermsRequested\n");
        }

        s_numberOfTermsRequested = new ASN1Integer(p, false);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at endStream of BER

        s_preferredPositionInResponse = null;
        s_otherInfo = null;

        // Decoding: preferredPositionInResponse [7] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 7 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_preferredPositionInResponse = new ASN1Integer(p, false);
            part++;
        }

        // Decoding: otherInfo OtherInformation OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        try {
            s_otherInfo = new OtherInformation(p, true);
            part++; // yes, consumed
        } catch (ASN1Exception e) {
            s_otherInfo = null; // no, not present
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Zebulun ScanRequest: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the ScanRequest.
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
     * Returns a BER encoding of ScanRequest, implicitly tagged.
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
        if (s_referenceId != null) {
            num_fields++;
        }
        if (s_attributeSet != null) {
            num_fields++;
        }
        if (s_stepSize != null) {
            num_fields++;
        }
        if (s_preferredPositionInResponse != null) {
            num_fields++;
        }
        if (s_otherInfo != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding f2[];
        int p;

        // Encoding s_referenceId: ReferenceId OPTIONAL

        if (s_referenceId != null) {
            fields[x++] = s_referenceId.ber_encode();
        }

        // Encoding s_databaseNames: SEQUENCE OF

        f2 = new BEREncoding[s_databaseNames.length];

        for (p = 0; p < s_databaseNames.length; p++) {
            f2[p] = s_databaseNames[p].ber_encode();
        }

        fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 3, f2);

        // Encoding s_attributeSet: AttributeSetId OPTIONAL

        if (s_attributeSet != null) {
            fields[x++] = s_attributeSet.ber_encode();
        }

        // Encoding s_termListAndStartPoint: AttributesPlusTerm

        fields[x++] = s_termListAndStartPoint.ber_encode();

        // Encoding s_stepSize: INTEGER OPTIONAL

        if (s_stepSize != null) {
            fields[x++] = s_stepSize.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 5);
        }

        // Encoding s_numberOfTermsRequested: INTEGER

        fields[x++] = s_numberOfTermsRequested.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 6);

        // Encoding s_preferredPositionInResponse: INTEGER OPTIONAL

        if (s_preferredPositionInResponse != null) {
            fields[x++] = s_preferredPositionInResponse.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 7);
        }

        // Encoding s_otherInfo: OtherInformation OPTIONAL

        if (s_otherInfo != null) {
            fields[x++] = s_otherInfo.ber_encode();
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the ScanRequest.
     */

    public String
    toString() {
        int p;
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        if (s_referenceId != null) {
            str.append("referenceId ");
            str.append(s_referenceId);
            outputted++;
        }

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("databaseNames ");
        str.append("{");
        for (p = 0; p < s_databaseNames.length; p++) {
            if (p != 0) {
                str.append(", ");
            }
            str.append(s_databaseNames[p]);
        }
        str.append("}");
        outputted++;

        if (s_attributeSet != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("attributeSet ");
            str.append(s_attributeSet);
            outputted++;
        }

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("termListAndStartPoint ");
        str.append(s_termListAndStartPoint);
        outputted++;

        if (s_stepSize != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("stepSize ");
            str.append(s_stepSize);
            outputted++;
        }

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("numberOfTermsRequested ");
        str.append(s_numberOfTermsRequested);
        outputted++;

        if (s_preferredPositionInResponse != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("preferredPositionInResponse ");
            str.append(s_preferredPositionInResponse);
            outputted++;
        }

        if (s_otherInfo != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("otherInfo ");
            str.append(s_otherInfo);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public ReferenceId s_referenceId; // optional
    public DatabaseName s_databaseNames[];
    public AttributeSetId s_attributeSet; // optional
    public AttributesPlusTerm s_termListAndStartPoint;
    public ASN1Integer s_stepSize; // optional
    public ASN1Integer s_numberOfTermsRequested;
    public ASN1Integer s_preferredPositionInResponse; // optional
    public OtherInformation s_otherInfo; // optional

} // ScanRequest


//EOF
