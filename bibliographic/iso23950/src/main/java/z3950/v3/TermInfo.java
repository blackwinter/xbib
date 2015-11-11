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
 * Class for representing a <code>TermInfo</code> from <code>Z39-50-APDU-1995</code>
 * <p/>
 * <pre>
 * TermInfo ::=
 * SEQUENCE {
 *   term Term
 *   displayTerm [0] IMPLICIT InternationalString OPTIONAL
 *   suggestedAttributes AttributeList OPTIONAL
 *   alternativeTerm [4] IMPLICIT SEQUENCE OF AttributesPlusTerm OPTIONAL
 *   globalOccurrences [2] IMPLICIT INTEGER OPTIONAL
 *   byAttributes [3] IMPLICIT OccurrenceByAttributes OPTIONAL
 *   otherTermInfo OtherInformation OPTIONAL
 * }
 * </pre>
 *
 */

public final class TermInfo extends ASN1Any {

    /**
     * Default constructor for a TermInfo.
     */

    public TermInfo() {
    }

    /**
     * Constructor for a TermInfo from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public TermInfo(BEREncoding ber, boolean check_tag)
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
        // TermInfo should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun TermInfo: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Decoding: term Term

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun TermInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        s_term = new Term(p, true);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at endStream of BER

        s_displayTerm = null;
        s_suggestedAttributes = null;
        s_alternativeTerm = null;
        s_globalOccurrences = null;
        s_byAttributes = null;
        s_otherTermInfo = null;

        // Decoding: displayTerm [0] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 0 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_displayTerm = new InternationalString(p, false);
            part++;
        }

        // Decoding: suggestedAttributes AttributeList OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        try {
            s_suggestedAttributes = new AttributeList(p, true);
            part++; // yes, consumed
        } catch (ASN1Exception e) {
            s_suggestedAttributes = null; // no, not present
        }

        // Decoding: alternativeTerm [4] IMPLICIT SEQUENCE OF AttributesPlusTerm OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 4 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_alternativeTerm = new AttributesPlusTerm[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_alternativeTerm[n] = new AttributesPlusTerm(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Decoding: globalOccurrences [2] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_globalOccurrences = new ASN1Integer(p, false);
            part++;
        }

        // Decoding: byAttributes [3] IMPLICIT OccurrenceByAttributes OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 3 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_byAttributes = new OccurrenceByAttributes(p, false);
            part++;
        }

        // Decoding: otherTermInfo OtherInformation OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        try {
            s_otherTermInfo = new OtherInformation(p, true);
            part++; // yes, consumed
        } catch (ASN1Exception e) {
            s_otherTermInfo = null; // no, not present
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Zebulun TermInfo: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }

    /**
     * Returns a BER encoding of the TermInfo.
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
     * Returns a BER encoding of TermInfo, implicitly tagged.
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
        if (s_displayTerm != null) {
            num_fields++;
        }
        if (s_suggestedAttributes != null) {
            num_fields++;
        }
        if (s_alternativeTerm != null) {
            num_fields++;
        }
        if (s_globalOccurrences != null) {
            num_fields++;
        }
        if (s_byAttributes != null) {
            num_fields++;
        }
        if (s_otherTermInfo != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding f2[];
        int p;

        // Encoding s_term: Term

        fields[x++] = s_term.ber_encode();

        // Encoding s_displayTerm: InternationalString OPTIONAL

        if (s_displayTerm != null) {
            fields[x++] = s_displayTerm.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 0);
        }

        // Encoding s_suggestedAttributes: AttributeList OPTIONAL

        if (s_suggestedAttributes != null) {
            fields[x++] = s_suggestedAttributes.ber_encode();
        }

        // Encoding s_alternativeTerm: SEQUENCE OF OPTIONAL

        if (s_alternativeTerm != null) {
            f2 = new BEREncoding[s_alternativeTerm.length];

            for (p = 0; p < s_alternativeTerm.length; p++) {
                f2[p] = s_alternativeTerm[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 4, f2);
        }

        // Encoding s_globalOccurrences: INTEGER OPTIONAL

        if (s_globalOccurrences != null) {
            fields[x++] = s_globalOccurrences.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
        }

        // Encoding s_byAttributes: OccurrenceByAttributes OPTIONAL

        if (s_byAttributes != null) {
            fields[x++] = s_byAttributes.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
        }

        // Encoding s_otherTermInfo: OtherInformation OPTIONAL

        if (s_otherTermInfo != null) {
            fields[x++] = s_otherTermInfo.ber_encode();
        }

        return new BERConstructed(tag_type, tag, fields);
    }

    /**
     * Returns a new String object containing a text representing
     * of the TermInfo.
     */

    public String
    toString() {
        int p;
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        str.append("term ");
        str.append(s_term);
        outputted++;

        if (s_displayTerm != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("displayTerm ");
            str.append(s_displayTerm);
            outputted++;
        }

        if (s_suggestedAttributes != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("suggestedAttributes ");
            str.append(s_suggestedAttributes);
            outputted++;
        }

        if (s_alternativeTerm != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("alternativeTerm ");
            str.append("{");
            for (p = 0; p < s_alternativeTerm.length; p++) {
                if (p != 0) {
                   str.append(", ");
                }
                str.append(s_alternativeTerm[p]);
            }
            str.append("}");
            outputted++;
        }

        if (s_globalOccurrences != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("globalOccurrences ");
            str.append(s_globalOccurrences);
            outputted++;
        }

        if (s_byAttributes != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("byAttributes ");
            str.append(s_byAttributes);
            outputted++;
        }

        if (s_otherTermInfo != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("otherTermInfo ");
            str.append(s_otherTermInfo);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }

/*
 * Internal variables for class.
 */

    public Term s_term;
    public InternationalString s_displayTerm; // optional
    public AttributeList s_suggestedAttributes; // optional
    public AttributesPlusTerm s_alternativeTerm[]; // optional
    public ASN1Integer s_globalOccurrences; // optional
    public OccurrenceByAttributes s_byAttributes; // optional
    public OtherInformation s_otherTermInfo; // optional

}