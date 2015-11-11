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
package z3950.UserInfoFormat_searchResult;

import asn1.ASN1Any;
import asn1.ASN1Boolean;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Integer;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.IntUnit;
import z3950.v3.InternationalString;

/**
 * Class for representing a <code>SearchInfoReport1</code> from <code>UserInfoFormat-searchResult-1</code>
 * <p/>
 * <pre>
 * SearchInfoReport1 ::=
 * SEQUENCE {
 *   subqueryId [1] IMPLICIT InternationalString OPTIONAL
 *   fullQuery [2] IMPLICIT BOOLEAN
 *   subqueryExpression [3] EXPLICIT QueryExpression OPTIONAL
 *   subqueryInterpretation [4] EXPLICIT QueryExpression OPTIONAL
 *   subqueryRecommendation [5] EXPLICIT QueryExpression OPTIONAL
 *   subqueryCount [6] IMPLICIT INTEGER OPTIONAL
 *   subqueryWeight [7] IMPLICIT IntUnit OPTIONAL
 *   resultsByDB [8] IMPLICIT ResultsByDB OPTIONAL
 * }
 * </pre>
 *
 */


public final class SearchInfoReport1 extends ASN1Any {

    /**
     * Default constructor for a SearchInfoReport1.
     */

    public SearchInfoReport1() {
    }

    /**
     * Constructor for a SearchInfoReport1 from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public SearchInfoReport1(BEREncoding ber, boolean check_tag)
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
        // SearchInfoReport1 should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun SearchInfoReport1: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;
        BERConstructed tagged;

        // Decoding: subqueryId [1] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SearchInfoReport1: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 1 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_subqueryId = new InternationalString(p, false);
            part++;
        }

        // Decoding: fullQuery [2] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SearchInfoReport1: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 2 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun SearchInfoReport1: bad tag in s_fullQuery\n");
        }

        s_fullQuery = new ASN1Boolean(p, false);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at endStream of BER

        s_subqueryExpression = null;
        s_subqueryInterpretation = null;
        s_subqueryRecommendation = null;
        s_subqueryCount = null;
        s_subqueryWeight = null;
        s_resultsByDB = null;

        // Decoding: subqueryExpression [3] EXPLICIT QueryExpression OPTIONAL

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
                        ("Zebulun SearchInfoReport1: bad BER encoding: s_subqueryExpression tag bad\n");
            }
            if (tagged.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun SearchInfoReport1: bad BER encoding: s_subqueryExpression tag bad\n");
            }

            s_subqueryExpression = new QueryExpression(tagged.elementAt(0), true);
            part++;
        }

        // Decoding: subqueryInterpretation [4] EXPLICIT QueryExpression OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 4 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagged = (BERConstructed) p;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun SearchInfoReport1: bad BER encoding: s_subqueryInterpretation tag bad\n");
            }
            if (tagged.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun SearchInfoReport1: bad BER encoding: s_subqueryInterpretation tag bad\n");
            }

            s_subqueryInterpretation = new QueryExpression(tagged.elementAt(0), true);
            part++;
        }

        // Decoding: subqueryRecommendation [5] EXPLICIT QueryExpression OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 5 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagged = (BERConstructed) p;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun SearchInfoReport1: bad BER encoding: s_subqueryRecommendation tag bad\n");
            }
            if (tagged.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun SearchInfoReport1: bad BER encoding: s_subqueryRecommendation tag bad\n");
            }

            s_subqueryRecommendation = new QueryExpression(tagged.elementAt(0), true);
            part++;
        }

        // Decoding: subqueryCount [6] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 6 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_subqueryCount = new ASN1Integer(p, false);
            part++;
        }

        // Decoding: subqueryWeight [7] IMPLICIT IntUnit OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 7 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_subqueryWeight = new IntUnit(p, false);
            part++;
        }

        // Decoding: resultsByDB [8] IMPLICIT ResultsByDB OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 8 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_resultsByDB = new ResultsByDB(p, false);
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Zebulun SearchInfoReport1: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }


    /**
     * Returns a BER encoding of the SearchInfoReport1.
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
     * Returns a BER encoding of SearchInfoReport1, implicitly tagged.
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
        if (s_subqueryId != null) {
            num_fields++;
        }
        if (s_subqueryExpression != null) {
            num_fields++;
        }
        if (s_subqueryInterpretation != null) {
            num_fields++;
        }
        if (s_subqueryRecommendation != null) {
            num_fields++;
        }
        if (s_subqueryCount != null) {
            num_fields++;
        }
        if (s_subqueryWeight != null) {
            num_fields++;
        }
        if (s_resultsByDB != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding enc[];

        // Encoding s_subqueryId: InternationalString OPTIONAL

        if (s_subqueryId != null) {
            fields[x++] = s_subqueryId.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
        }

        // Encoding s_fullQuery: BOOLEAN

        fields[x++] = s_fullQuery.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);

        // Encoding s_subqueryExpression: QueryExpression OPTIONAL

        if (s_subqueryExpression != null) {
            enc = new BEREncoding[1];
            enc[0] = s_subqueryExpression.ber_encode();
            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 3, enc);
        }

        // Encoding s_subqueryInterpretation: QueryExpression OPTIONAL

        if (s_subqueryInterpretation != null) {
            enc = new BEREncoding[1];
            enc[0] = s_subqueryInterpretation.ber_encode();
            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 4, enc);
        }

        // Encoding s_subqueryRecommendation: QueryExpression OPTIONAL

        if (s_subqueryRecommendation != null) {
            enc = new BEREncoding[1];
            enc[0] = s_subqueryRecommendation.ber_encode();
            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 5, enc);
        }

        // Encoding s_subqueryCount: INTEGER OPTIONAL

        if (s_subqueryCount != null) {
            fields[x++] = s_subqueryCount.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 6);
        }

        // Encoding s_subqueryWeight: IntUnit OPTIONAL

        if (s_subqueryWeight != null) {
            fields[x++] = s_subqueryWeight.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 7);
        }

        // Encoding s_resultsByDB: ResultsByDB OPTIONAL

        if (s_resultsByDB != null) {
            fields[x++] = s_resultsByDB.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 8);
        }

        return new BERConstructed(tag_type, tag, fields);
    }

    /**
     * Returns a new String object containing a text representing
     * of the SearchInfoReport1.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        if (s_subqueryId != null) {
            str.append("subqueryId ");
            str.append(s_subqueryId);
            outputted++;
        }

        if (0 < outputted) {
             str.append(", ");
        }


        str.append("fullQuery ");
        str.append(s_fullQuery);
        outputted++;

        if (s_subqueryExpression != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("subqueryExpression ");
            str.append(s_subqueryExpression);
            outputted++;
        }

        if (s_subqueryInterpretation != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("subqueryInterpretation ");
            str.append(s_subqueryInterpretation);
            outputted++;
        }

        if (s_subqueryRecommendation != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("subqueryRecommendation ");
            str.append(s_subqueryRecommendation);
            outputted++;
        }

        if (s_subqueryCount != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("subqueryCount ");
            str.append(s_subqueryCount);
            outputted++;
        }

        if (s_subqueryWeight != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("subqueryWeight ");
            str.append(s_subqueryWeight);
            outputted++;
        }

        if (s_resultsByDB != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("resultsByDB ");
            str.append(s_resultsByDB);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }

/*
 * Internal variables for class.
 */

    public InternationalString s_subqueryId; // optional
    public ASN1Boolean s_fullQuery;
    public QueryExpression s_subqueryExpression; // optional
    public QueryExpression s_subqueryInterpretation; // optional
    public QueryExpression s_subqueryRecommendation; // optional
    public ASN1Integer s_subqueryCount; // optional
    public IntUnit s_subqueryWeight; // optional
    public ResultsByDB s_resultsByDB; // optional

}