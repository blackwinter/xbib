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
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;



/**
 * Class for representing a <code>SortRequest</code> from <code>Z39-50-APDU-1995</code>
 * <p/>
 * <pre>
 * SortRequest ::=
 * SEQUENCE {
 *   referenceId ReferenceId OPTIONAL
 *   inputResultSetNames [3] IMPLICIT SEQUENCE OF InternationalString
 *   sortedResultSetName [4] IMPLICIT InternationalString
 *   sortSequence [5] IMPLICIT SEQUENCE OF SortKeySpec
 *   otherInfo OtherInformation OPTIONAL
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class SortRequest extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a SortRequest.
     */

    public SortRequest() {
    }



    /**
     * Constructor for a SortRequest from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public SortRequest(BEREncoding ber, boolean check_tag)
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
        // SortRequest should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun SortRequest: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Decoding: referenceId ReferenceId OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SortRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        try {
            s_referenceId = new ReferenceId(p, true);
            part++; // yes, consumed
        } catch (ASN1Exception e) {
            s_referenceId = null; // no, not present
        }

        // Decoding: inputResultSetNames [3] IMPLICIT SEQUENCE OF InternationalString

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SortRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 3 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun SortRequest: bad tag in s_inputResultSetNames\n");
        }

        try {
            BERConstructed cons = (BERConstructed) p;
            int parts = cons.number_components();
            s_inputResultSetNames = new InternationalString[parts];
            int n;
            for (n = 0; n < parts; n++) {
                s_inputResultSetNames[n] = new InternationalString(cons.elementAt(n), true);
            }
        } catch (ClassCastException e) {
            throw new ASN1EncodingException("Bad BER");
        }
        part++;

        // Decoding: sortedResultSetName [4] IMPLICIT InternationalString

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SortRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 4 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun SortRequest: bad tag in s_sortedResultSetName\n");
        }

        s_sortedResultSetName = new InternationalString(p, false);
        part++;

        // Decoding: sortSequence [5] IMPLICIT SEQUENCE OF SortKeySpec

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun SortRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 5 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun SortRequest: bad tag in s_sortSequence\n");
        }

        try {
            BERConstructed cons = (BERConstructed) p;
            int parts = cons.number_components();
            s_sortSequence = new SortKeySpec[parts];
            int n;
            for (n = 0; n < parts; n++) {
                s_sortSequence[n] = new SortKeySpec(cons.elementAt(n), true);
            }
        } catch (ClassCastException e) {
            throw new ASN1EncodingException("Bad BER");
        }
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at endStream of BER

        s_otherInfo = null;

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
            throw new ASN1Exception("Zebulun SortRequest: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the SortRequest.
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
     * Returns a BER encoding of SortRequest, implicitly tagged.
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

        // Encoding s_inputResultSetNames: SEQUENCE OF

        f2 = new BEREncoding[s_inputResultSetNames.length];

        for (p = 0; p < s_inputResultSetNames.length; p++) {
            f2[p] = s_inputResultSetNames[p].ber_encode();
        }

        fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 3, f2);

        // Encoding s_sortedResultSetName: InternationalString

        fields[x++] = s_sortedResultSetName.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);

        // Encoding s_sortSequence: SEQUENCE OF

        f2 = new BEREncoding[s_sortSequence.length];

        for (p = 0; p < s_sortSequence.length; p++) {
            f2[p] = s_sortSequence[p].ber_encode();
        }

        fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 5, f2);

        // Encoding s_otherInfo: OtherInformation OPTIONAL

        if (s_otherInfo != null) {
            fields[x++] = s_otherInfo.ber_encode();
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the SortRequest.
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
        str.append("inputResultSetNames ");
        str.append("{");
        for (p = 0; p < s_inputResultSetNames.length; p++) {
            if (p != 0) {
                str.append(", ");
            }
            str.append(s_inputResultSetNames[p]);
        }
        str.append("}");
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("sortedResultSetName ");
        str.append(s_sortedResultSetName);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("sortSequence ");
        str.append("{");
        for (p = 0; p < s_sortSequence.length; p++) {
            if (p != 0) {
                str.append(", ");
            }
            str.append(s_sortSequence[p]);
        }
        str.append("}");
        outputted++;

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
    public InternationalString s_inputResultSetNames[];
    public InternationalString s_sortedResultSetName;
    public SortKeySpec s_sortSequence[];
    public OtherInformation s_otherInfo; // optional

} // SortRequest


//EOF
