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
 * Class for representing a <code>SearchResponse</code> from <code>Z39-50-APDU-1995</code>
 * <p/>
 * <pre>
 * SearchResponse ::=
 * SEQUENCE {
 *   referenceId ReferenceId OPTIONAL
 *   resultCount [23] IMPLICIT INTEGER
 *   numberOfRecordsReturned [24] IMPLICIT INTEGER
 *   nextResultSetPosition [25] IMPLICIT INTEGER
 *   searchStatus [22] IMPLICIT BOOLEAN
 *   resultSetStatus [26] IMPLICIT INTEGER OPTIONAL
 *   presentStatus PresentStatus OPTIONAL
 *   records Records OPTIONAL
 *   additionalSearchInfo [203] IMPLICIT OtherInformation OPTIONAL
 *   otherInfo OtherInformation OPTIONAL
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class SearchResponse extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a SearchResponse.
     */

    public SearchResponse() {
    }



    /**
     * Constructor for a SearchResponse from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public SearchResponse(BEREncoding ber, boolean check_tag)
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
        // SearchResponse should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun SearchResponse: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Decoding: referenceId ReferenceId OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SearchResponse: incomplete");
        }
        p = ber_cons.elementAt(part);

        try {
            s_referenceId = new ReferenceId(p, true);
            part++; // yes, consumed
        } catch (ASN1Exception e) {
            s_referenceId = null; // no, not present
        }

        // Decoding: resultCount [23] IMPLICIT INTEGER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SearchResponse: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 23 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun SearchResponse: bad tag in s_resultCount\n");
        }

        s_resultCount = new ASN1Integer(p, false);
        part++;

        // Decoding: numberOfRecordsReturned [24] IMPLICIT INTEGER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SearchResponse: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 24 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun SearchResponse: bad tag in s_numberOfRecordsReturned\n");
        }

        s_numberOfRecordsReturned = new ASN1Integer(p, false);
        part++;

        // Decoding: nextResultSetPosition [25] IMPLICIT INTEGER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SearchResponse: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 25 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun SearchResponse: bad tag in s_nextResultSetPosition\n");
        }

        s_nextResultSetPosition = new ASN1Integer(p, false);
        part++;

        // Decoding: searchStatus [22] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SearchResponse: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 22 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun SearchResponse: bad tag in s_searchStatus\n");
        }

        s_searchStatus = new ASN1Boolean(p, false);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at endStream of BER

        s_resultSetStatus = null;
        s_presentStatus = null;
        s_records = null;
        s_additionalSearchInfo = null;
        s_otherInfo = null;

        // Decoding: resultSetStatus [26] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 26 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_resultSetStatus = new ASN1Integer(p, false);
            part++;
        }

        // Decoding: presentStatus PresentStatus OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        try {
            s_presentStatus = new PresentStatus(p, true);
            part++; // yes, consumed
        } catch (ASN1Exception e) {
            s_presentStatus = null; // no, not present
        }

        // Decoding: records Records OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        try {
            s_records = new Records(p, true);
            part++; // yes, consumed
        } catch (ASN1Exception e) {
            s_records = null; // no, not present
        }

        // Decoding: additionalSearchInfo [203] IMPLICIT OtherInformation OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 203 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_additionalSearchInfo = new OtherInformation(p, false);
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
            throw new ASN1Exception("Zebulun SearchResponse: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the SearchResponse.
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
     * Returns a BER encoding of SearchResponse, implicitly tagged.
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
        if (s_referenceId != null) {
            num_fields++;
        }
        if (s_resultSetStatus != null) {
            num_fields++;
        }
        if (s_presentStatus != null) {
            num_fields++;
        }
        if (s_records != null) {
            num_fields++;
        }
        if (s_additionalSearchInfo != null) {
            num_fields++;
        }
        if (s_otherInfo != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;

        // Encoding s_referenceId: ReferenceId OPTIONAL

        if (s_referenceId != null) {
            fields[x++] = s_referenceId.ber_encode();
        }

        // Encoding s_resultCount: INTEGER

        fields[x++] = s_resultCount.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 23);

        // Encoding s_numberOfRecordsReturned: INTEGER

        fields[x++] = s_numberOfRecordsReturned.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 24);

        // Encoding s_nextResultSetPosition: INTEGER

        fields[x++] = s_nextResultSetPosition.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 25);

        // Encoding s_searchStatus: BOOLEAN

        fields[x++] = s_searchStatus.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 22);

        // Encoding s_resultSetStatus: INTEGER OPTIONAL

        if (s_resultSetStatus != null) {
            fields[x++] = s_resultSetStatus.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 26);
        }

        // Encoding s_presentStatus: PresentStatus OPTIONAL

        if (s_presentStatus != null) {
            fields[x++] = s_presentStatus.ber_encode();
        }

        // Encoding s_records: Records OPTIONAL

        if (s_records != null) {
            fields[x++] = s_records.ber_encode();
        }

        // Encoding s_additionalSearchInfo: OtherInformation OPTIONAL

        if (s_additionalSearchInfo != null) {
            fields[x++] = s_additionalSearchInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 203);
        }

        // Encoding s_otherInfo: OtherInformation OPTIONAL

        if (s_otherInfo != null) {
            fields[x++] = s_otherInfo.ber_encode();
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the SearchResponse.
     */

    public String
    toString() {
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
        str.append("resultCount ");
        str.append(s_resultCount);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("numberOfRecordsReturned ");
        str.append(s_numberOfRecordsReturned);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("nextResultSetPosition ");
        str.append(s_nextResultSetPosition);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("searchStatus ");
        str.append(s_searchStatus);
        outputted++;

        if (s_resultSetStatus != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("resultSetStatus ");
            str.append(s_resultSetStatus);
            outputted++;
        }

        if (s_presentStatus != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("presentStatus ");
            str.append(s_presentStatus);
            outputted++;
        }

        if (s_records != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("records ");
            str.append(s_records);
            outputted++;
        }

        if (s_additionalSearchInfo != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("additionalSearchInfo ");
            str.append(s_additionalSearchInfo);
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
    public ASN1Integer s_resultCount;
    public ASN1Integer s_numberOfRecordsReturned;
    public ASN1Integer s_nextResultSetPosition;
    public ASN1Boolean s_searchStatus;
    public ASN1Integer s_resultSetStatus; // optional
    public PresentStatus s_presentStatus; // optional
    public Records s_records; // optional
    public OtherInformation s_additionalSearchInfo; // optional
    public OtherInformation s_otherInfo; // optional


/*
 * Enumerated constants for class.
 */

    // Enumerated constants for resultSetStatus
    public static final int E_subset = 1;
    public static final int E_interim = 2;
    public static final int E_none = 3;

} // SearchResponse


//EOF
