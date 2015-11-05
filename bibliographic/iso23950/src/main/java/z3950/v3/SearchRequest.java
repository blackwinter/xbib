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
import asn1.ASN1ObjectIdentifier;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;




/**
 * Class for representing a <code>SearchRequest</code> from <code>Z39-50-APDU-1995</code>
 * <p/>
 * <pre>
 * SearchRequest ::=
 * SEQUENCE {
 *   referenceId ReferenceId OPTIONAL
 *   smallSetUpperBound [13] IMPLICIT INTEGER
 *   largeSetLowerBound [14] IMPLICIT INTEGER
 *   mediumSetPresentNumber [15] IMPLICIT INTEGER
 *   replaceIndicator [16] IMPLICIT BOOLEAN
 *   resultSetName [17] IMPLICIT InternationalString
 *   databaseNames [18] IMPLICIT SEQUENCE OF DatabaseName
 *   smallSetElementSetNames [100] EXPLICIT ElementSetNames OPTIONAL
 *   mediumSetElementSetNames [101] EXPLICIT ElementSetNames OPTIONAL
 *   preferredRecordSyntax [104] IMPLICIT OBJECT IDENTIFIER OPTIONAL
 *   query [21] EXPLICIT Query
 *   additionalSearchInfo [203] IMPLICIT OtherInformation OPTIONAL
 *   otherInfo OtherInformation OPTIONAL
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class SearchRequest extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a SearchRequest.
     */

    public SearchRequest() {
    }



    /**
     * Constructor for a SearchRequest from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public SearchRequest(BEREncoding ber, boolean check_tag)
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
        // SearchRequest should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun SearchRequest: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;
        BERConstructed tagged;

        // Decoding: referenceId ReferenceId OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SearchRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        try {
            s_referenceId = new ReferenceId(p, true);
            part++; // yes, consumed
        } catch (ASN1Exception e) {
            s_referenceId = null; // no, not present
        }

        // Decoding: smallSetUpperBound [13] IMPLICIT INTEGER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SearchRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 13 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun SearchRequest: bad tag in s_smallSetUpperBound\n");
        }

        s_smallSetUpperBound = new ASN1Integer(p, false);
        part++;

        // Decoding: largeSetLowerBound [14] IMPLICIT INTEGER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SearchRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 14 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun SearchRequest: bad tag in s_largeSetLowerBound\n");
        }

        s_largeSetLowerBound = new ASN1Integer(p, false);
        part++;

        // Decoding: mediumSetPresentNumber [15] IMPLICIT INTEGER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SearchRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 15 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun SearchRequest: bad tag in s_mediumSetPresentNumber\n");
        }

        s_mediumSetPresentNumber = new ASN1Integer(p, false);
        part++;

        // Decoding: replaceIndicator [16] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SearchRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 16 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun SearchRequest: bad tag in s_replaceIndicator\n");
        }

        s_replaceIndicator = new ASN1Boolean(p, false);
        part++;

        // Decoding: resultSetName [17] IMPLICIT InternationalString

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SearchRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 17 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun SearchRequest: bad tag in s_resultSetName\n");
        }

        s_resultSetName = new InternationalString(p, false);
        part++;

        // Decoding: databaseNames [18] IMPLICIT SEQUENCE OF DatabaseName

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SearchRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 18 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun SearchRequest: bad tag in s_databaseNames\n");
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

        // Decoding: smallSetElementSetNames [100] EXPLICIT ElementSetNames OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SearchRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 100 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagged = (BERConstructed) p;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun SearchRequest: bad BER encoding: s_smallSetElementSetNames tag bad\n");
            }
            if (tagged.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun SearchRequest: bad BER encoding: s_smallSetElementSetNames tag bad\n");
            }

            s_smallSetElementSetNames = new ElementSetNames(tagged.elementAt(0), true);
            part++;
        }

        // Decoding: mediumSetElementSetNames [101] EXPLICIT ElementSetNames OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SearchRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 101 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagged = (BERConstructed) p;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun SearchRequest: bad BER encoding: s_mediumSetElementSetNames tag bad\n");
            }
            if (tagged.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun SearchRequest: bad BER encoding: s_mediumSetElementSetNames tag bad\n");
            }

            s_mediumSetElementSetNames = new ElementSetNames(tagged.elementAt(0), true);
            part++;
        }

        // Decoding: preferredRecordSyntax [104] IMPLICIT OBJECT IDENTIFIER OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SearchRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 104 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_preferredRecordSyntax = new ASN1ObjectIdentifier(p, false);
            part++;
        }

        // Decoding: query [21] EXPLICIT Query

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SearchRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 21 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun SearchRequest: bad tag in s_query\n");
        }

        try {
            tagged = (BERConstructed) p;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun SearchRequest: bad BER encoding: s_query tag bad\n");
        }
        if (tagged.number_components() != 1) {
            throw new ASN1EncodingException
                    ("Zebulun SearchRequest: bad BER encoding: s_query tag bad\n");
        }

        s_query = new Query(tagged.elementAt(0), true);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at endStream of BER

        s_additionalSearchInfo = null;
        s_otherInfo = null;

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
            throw new ASN1Exception("Zebulun SearchRequest: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the SearchRequest.
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
     * Returns a BER encoding of SearchRequest, implicitly tagged.
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

        int num_fields = 7; // number of mandatories
        if (s_referenceId != null) {
            num_fields++;
        }
        if (s_smallSetElementSetNames != null) {
            num_fields++;
        }
        if (s_mediumSetElementSetNames != null) {
            num_fields++;
        }
        if (s_preferredRecordSyntax != null) {
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
        BEREncoding f2[];
        int p;
        BEREncoding enc[];

        // Encoding s_referenceId: ReferenceId OPTIONAL

        if (s_referenceId != null) {
            fields[x++] = s_referenceId.ber_encode();
        }

        // Encoding s_smallSetUpperBound: INTEGER

        fields[x++] = s_smallSetUpperBound.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 13);

        // Encoding s_largeSetLowerBound: INTEGER

        fields[x++] = s_largeSetLowerBound.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 14);

        // Encoding s_mediumSetPresentNumber: INTEGER

        fields[x++] = s_mediumSetPresentNumber.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 15);

        // Encoding s_replaceIndicator: BOOLEAN

        fields[x++] = s_replaceIndicator.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 16);

        // Encoding s_resultSetName: InternationalString

        fields[x++] = s_resultSetName.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 17);

        // Encoding s_databaseNames: SEQUENCE OF

        f2 = new BEREncoding[s_databaseNames.length];

        for (p = 0; p < s_databaseNames.length; p++) {
            f2[p] = s_databaseNames[p].ber_encode();
        }

        fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 18, f2);

        // Encoding s_smallSetElementSetNames: ElementSetNames OPTIONAL

        if (s_smallSetElementSetNames != null) {
            enc = new BEREncoding[1];
            enc[0] = s_smallSetElementSetNames.ber_encode();
            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 100, enc);
        }

        // Encoding s_mediumSetElementSetNames: ElementSetNames OPTIONAL

        if (s_mediumSetElementSetNames != null) {
            enc = new BEREncoding[1];
            enc[0] = s_mediumSetElementSetNames.ber_encode();
            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 101, enc);
        }

        // Encoding s_preferredRecordSyntax: OBJECT IDENTIFIER OPTIONAL

        if (s_preferredRecordSyntax != null) {
            fields[x++] = s_preferredRecordSyntax.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 104);
        }

        // Encoding s_query: Query

        enc = new BEREncoding[1];
        enc[0] = s_query.ber_encode();
        fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 21, enc);

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
     * of the SearchRequest.
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
        str.append("smallSetUpperBound ");
        str.append(s_smallSetUpperBound);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("largeSetLowerBound ");
        str.append(s_largeSetLowerBound);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("mediumSetPresentNumber ");
        str.append(s_mediumSetPresentNumber);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("replaceIndicator ");
        str.append(s_replaceIndicator);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("resultSetName ");
        str.append(s_resultSetName);
        outputted++;

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

        if (s_smallSetElementSetNames != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("smallSetElementSetNames ");
            str.append(s_smallSetElementSetNames);
            outputted++;
        }

        if (s_mediumSetElementSetNames != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("mediumSetElementSetNames ");
            str.append(s_mediumSetElementSetNames);
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

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("query ");
        str.append(s_query);
        outputted++;

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
    public ASN1Integer s_smallSetUpperBound;
    public ASN1Integer s_largeSetLowerBound;
    public ASN1Integer s_mediumSetPresentNumber;
    public ASN1Boolean s_replaceIndicator;
    public InternationalString s_resultSetName;
    public DatabaseName s_databaseNames[];
    public ElementSetNames s_smallSetElementSetNames; // optional
    public ElementSetNames s_mediumSetElementSetNames; // optional
    public ASN1ObjectIdentifier s_preferredRecordSyntax; // optional
    public Query s_query;
    public OtherInformation s_additionalSearchInfo; // optional
    public OtherInformation s_otherInfo; // optional

} // SearchRequest


//EOF
