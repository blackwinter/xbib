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
import asn1.ASN1ObjectIdentifier;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;




/**
 * Class for representing a <code>PresentRequest</code> from <code>Z39-50-APDU-1995</code>
 * <p/>
 * <pre>
 * PresentRequest ::=
 * SEQUENCE {
 *   referenceId ReferenceId OPTIONAL
 *   resultSetId ResultSetId
 *   resultSetStartPoint [30] IMPLICIT INTEGER
 *   numberOfRecordsRequested [29] IMPLICIT INTEGER
 *   additionalRanges [212] IMPLICIT SEQUENCE OF Range OPTIONAL
 *   recordComposition PresentRequest_recordComposition OPTIONAL
 *   preferredRecordSyntax [104] IMPLICIT OBJECT IDENTIFIER OPTIONAL
 *   maxSegmentCount [204] IMPLICIT INTEGER OPTIONAL
 *   maxRecordSize [206] IMPLICIT INTEGER OPTIONAL
 *   maxSegmentSize [207] IMPLICIT INTEGER OPTIONAL
 *   otherInfo OtherInformation OPTIONAL
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class PresentRequest extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a PresentRequest.
     */

    public PresentRequest() {
    }



    /**
     * Constructor for a PresentRequest from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public PresentRequest(BEREncoding ber, boolean check_tag)
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
        // PresentRequest should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun PresentRequest: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Decoding: referenceId ReferenceId OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun PresentRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        try {
            s_referenceId = new ReferenceId(p, true);
            part++; // yes, consumed
        } catch (ASN1Exception e) {
            s_referenceId = null; // no, not present
        }

        // Decoding: resultSetId ResultSetId

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun PresentRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        s_resultSetId = new ResultSetId(p, true);
        part++;

        // Decoding: resultSetStartPoint [30] IMPLICIT INTEGER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun PresentRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 30 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun PresentRequest: bad tag in s_resultSetStartPoint\n");
        }

        s_resultSetStartPoint = new ASN1Integer(p, false);
        part++;

        // Decoding: numberOfRecordsRequested [29] IMPLICIT INTEGER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun PresentRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 29 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun PresentRequest: bad tag in s_numberOfRecordsRequested\n");
        }

        s_numberOfRecordsRequested = new ASN1Integer(p, false);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at endStream of BER

        s_additionalRanges = null;
        s_recordComposition = null;
        s_preferredRecordSyntax = null;
        s_maxSegmentCount = null;
        s_maxRecordSize = null;
        s_maxSegmentSize = null;
        s_otherInfo = null;

        // Decoding: additionalRanges [212] IMPLICIT SEQUENCE OF Range OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 212 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_additionalRanges = new Range[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_additionalRanges[n] = new Range(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Decoding: recordComposition PresentRequest_recordComposition OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        try {
            s_recordComposition = new PresentRequest_recordComposition(p, true);
            part++; // yes, consumed
        } catch (ASN1Exception e) {
            s_recordComposition = null; // no, not present
        }

        // Decoding: preferredRecordSyntax [104] IMPLICIT OBJECT IDENTIFIER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 104 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_preferredRecordSyntax = new ASN1ObjectIdentifier(p, false);
            part++;
        }

        // Decoding: maxSegmentCount [204] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 204 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_maxSegmentCount = new ASN1Integer(p, false);
            part++;
        }

        // Decoding: maxRecordSize [206] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 206 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_maxRecordSize = new ASN1Integer(p, false);
            part++;
        }

        // Decoding: maxSegmentSize [207] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 207 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_maxSegmentSize = new ASN1Integer(p, false);
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
            throw new ASN1Exception("Zebulun PresentRequest: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the PresentRequest.
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
     * Returns a BER encoding of PresentRequest, implicitly tagged.
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
        if (s_additionalRanges != null) {
            num_fields++;
        }
        if (s_recordComposition != null) {
            num_fields++;
        }
        if (s_preferredRecordSyntax != null) {
            num_fields++;
        }
        if (s_maxSegmentCount != null) {
            num_fields++;
        }
        if (s_maxRecordSize != null) {
            num_fields++;
        }
        if (s_maxSegmentSize != null) {
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

        // Encoding s_resultSetId: ResultSetId

        fields[x++] = s_resultSetId.ber_encode();

        // Encoding s_resultSetStartPoint: INTEGER

        fields[x++] = s_resultSetStartPoint.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 30);

        // Encoding s_numberOfRecordsRequested: INTEGER

        fields[x++] = s_numberOfRecordsRequested.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 29);

        // Encoding s_additionalRanges: SEQUENCE OF OPTIONAL

        if (s_additionalRanges != null) {
            f2 = new BEREncoding[s_additionalRanges.length];

            for (p = 0; p < s_additionalRanges.length; p++) {
                f2[p] = s_additionalRanges[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 212, f2);
        }

        // Encoding s_recordComposition: PresentRequest_recordComposition OPTIONAL

        if (s_recordComposition != null) {
            fields[x++] = s_recordComposition.ber_encode();
        }

        // Encoding s_preferredRecordSyntax: OBJECT IDENTIFIER OPTIONAL

        if (s_preferredRecordSyntax != null) {
            fields[x++] = s_preferredRecordSyntax.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 104);
        }

        // Encoding s_maxSegmentCount: INTEGER OPTIONAL

        if (s_maxSegmentCount != null) {
            fields[x++] = s_maxSegmentCount.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 204);
        }

        // Encoding s_maxRecordSize: INTEGER OPTIONAL

        if (s_maxRecordSize != null) {
            fields[x++] = s_maxRecordSize.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 206);
        }

        // Encoding s_maxSegmentSize: INTEGER OPTIONAL

        if (s_maxSegmentSize != null) {
            fields[x++] = s_maxSegmentSize.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 207);
        }

        // Encoding s_otherInfo: OtherInformation OPTIONAL

        if (s_otherInfo != null) {
            fields[x++] = s_otherInfo.ber_encode();
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the PresentRequest.
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
        str.append("resultSetId ");
        str.append(s_resultSetId);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("resultSetStartPoint ");
        str.append(s_resultSetStartPoint);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("numberOfRecordsRequested ");
        str.append(s_numberOfRecordsRequested);
        outputted++;

        if (s_additionalRanges != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("additionalRanges ");
            str.append("{");
            for (p = 0; p < s_additionalRanges.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_additionalRanges[p]);
            }
            str.append("}");
            outputted++;
        }

        if (s_recordComposition != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("recordComposition ");
            str.append(s_recordComposition);
            outputted++;
        }

        if (s_preferredRecordSyntax != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("preferredRecordSyntax ");
            str.append(s_preferredRecordSyntax);
            outputted++;
        }

        if (s_maxSegmentCount != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("maxSegmentCount ");
            str.append(s_maxSegmentCount);
            outputted++;
        }

        if (s_maxRecordSize != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("maxRecordSize ");
            str.append(s_maxRecordSize);
            outputted++;
        }

        if (s_maxSegmentSize != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("maxSegmentSize ");
            str.append(s_maxSegmentSize);
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
    public ResultSetId s_resultSetId;
    public ASN1Integer s_resultSetStartPoint;
    public ASN1Integer s_numberOfRecordsRequested;
    public Range s_additionalRanges[]; // optional
    public PresentRequest_recordComposition s_recordComposition; // optional
    public ASN1ObjectIdentifier s_preferredRecordSyntax; // optional
    public ASN1Integer s_maxSegmentCount; // optional
    public ASN1Integer s_maxRecordSize; // optional
    public ASN1Integer s_maxSegmentSize; // optional
    public OtherInformation s_otherInfo; // optional

} // PresentRequest


//EOF
