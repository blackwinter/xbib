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
package z3950.RS_summary;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Integer;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.InternationalString;
import z3950.v3.OtherInformation;

/**
 * Class for representing a <code>BriefBib</code> from <code>RecordSyntax-summary</code>
 * <p/>
 * <pre>
 * BriefBib ::=
 * SEQUENCE {
 *   title [1] IMPLICIT InternationalString
 *   author [2] IMPLICIT InternationalString OPTIONAL
 *   callNumber [3] IMPLICIT InternationalString OPTIONAL
 *   recordType [4] IMPLICIT InternationalString OPTIONAL
 *   bibliographicLevel [5] IMPLICIT InternationalString OPTIONAL
 *   format [6] IMPLICIT SEQUENCE OF FormatSpec OPTIONAL
 *   publicationPlace [7] IMPLICIT InternationalString OPTIONAL
 *   publicationDate [8] IMPLICIT InternationalString OPTIONAL
 *   targetSystemKey [9] IMPLICIT InternationalString OPTIONAL
 *   satisfyingElement [10] IMPLICIT InternationalString OPTIONAL
 *   rank [11] IMPLICIT INTEGER OPTIONAL
 *   documentId [12] IMPLICIT InternationalString OPTIONAL
 *   abstract [13] IMPLICIT InternationalString OPTIONAL
 *   otherInfo OtherInformation OPTIONAL
 * }
 * </pre>
 *
 */



public final class BriefBib extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a BriefBib.
     */

    public BriefBib() {
    }



    /**
     * Constructor for a BriefBib from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public BriefBib(BEREncoding ber, boolean check_tag)
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
        // BriefBib should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun BriefBib: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Decoding: title [1] IMPLICIT InternationalString

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun BriefBib: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 1 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun BriefBib: bad tag in s_title\n");
        }

        s_title = new InternationalString(p, false);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at endStream of BER

        s_author = null;
        s_callNumber = null;
        s_recordType = null;
        s_bibliographicLevel = null;
        s_format = null;
        s_publicationPlace = null;
        s_publicationDate = null;
        s_targetSystemKey = null;
        s_satisfyingElement = null;
        s_rank = null;
        s_documentId = null;
        s_abstract = null;
        s_otherInfo = null;

        // Decoding: author [2] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_author = new InternationalString(p, false);
            part++;
        }

        // Decoding: callNumber [3] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 3 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_callNumber = new InternationalString(p, false);
            part++;
        }

        // Decoding: recordType [4] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 4 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_recordType = new InternationalString(p, false);
            part++;
        }

        // Decoding: bibliographicLevel [5] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 5 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_bibliographicLevel = new InternationalString(p, false);
            part++;
        }

        // Decoding: format [6] IMPLICIT SEQUENCE OF FormatSpec OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 6 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_format = new FormatSpec[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_format[n] = new FormatSpec(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Decoding: publicationPlace [7] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 7 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_publicationPlace = new InternationalString(p, false);
            part++;
        }

        // Decoding: publicationDate [8] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 8 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_publicationDate = new InternationalString(p, false);
            part++;
        }

        // Decoding: targetSystemKey [9] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 9 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_targetSystemKey = new InternationalString(p, false);
            part++;
        }

        // Decoding: satisfyingElement [10] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 10 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_satisfyingElement = new InternationalString(p, false);
            part++;
        }

        // Decoding: rank [11] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 11 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_rank = new ASN1Integer(p, false);
            part++;
        }

        // Decoding: documentId [12] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 12 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_documentId = new InternationalString(p, false);
            part++;
        }

        // Decoding: abstract [13] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 13 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_abstract = new InternationalString(p, false);
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
            throw new ASN1Exception("Zebulun BriefBib: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the BriefBib.
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
     * Returns a BER encoding of BriefBib, implicitly tagged.
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
        if (s_author != null) {
            num_fields++;
        }
        if (s_callNumber != null) {
            num_fields++;
        }
        if (s_recordType != null) {
            num_fields++;
        }
        if (s_bibliographicLevel != null) {
            num_fields++;
        }
        if (s_format != null) {
            num_fields++;
        }
        if (s_publicationPlace != null) {
            num_fields++;
        }
        if (s_publicationDate != null) {
            num_fields++;
        }
        if (s_targetSystemKey != null) {
            num_fields++;
        }
        if (s_satisfyingElement != null) {
            num_fields++;
        }
        if (s_rank != null) {
            num_fields++;
        }
        if (s_documentId != null) {
            num_fields++;
        }
        if (s_abstract != null) {
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

        // Encoding s_title: InternationalString

        fields[x++] = s_title.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);

        // Encoding s_author: InternationalString OPTIONAL

        if (s_author != null) {
            fields[x++] = s_author.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
        }

        // Encoding s_callNumber: InternationalString OPTIONAL

        if (s_callNumber != null) {
            fields[x++] = s_callNumber.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
        }

        // Encoding s_recordType: InternationalString OPTIONAL

        if (s_recordType != null) {
            fields[x++] = s_recordType.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);
        }

        // Encoding s_bibliographicLevel: InternationalString OPTIONAL

        if (s_bibliographicLevel != null) {
            fields[x++] = s_bibliographicLevel.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 5);
        }

        // Encoding s_format: SEQUENCE OF OPTIONAL

        if (s_format != null) {
            f2 = new BEREncoding[s_format.length];

            for (p = 0; p < s_format.length; p++) {
                f2[p] = s_format[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 6, f2);
        }

        // Encoding s_publicationPlace: InternationalString OPTIONAL

        if (s_publicationPlace != null) {
            fields[x++] = s_publicationPlace.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 7);
        }

        // Encoding s_publicationDate: InternationalString OPTIONAL

        if (s_publicationDate != null) {
            fields[x++] = s_publicationDate.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 8);
        }

        // Encoding s_targetSystemKey: InternationalString OPTIONAL

        if (s_targetSystemKey != null) {
            fields[x++] = s_targetSystemKey.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 9);
        }

        // Encoding s_satisfyingElement: InternationalString OPTIONAL

        if (s_satisfyingElement != null) {
            fields[x++] = s_satisfyingElement.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 10);
        }

        // Encoding s_rank: INTEGER OPTIONAL

        if (s_rank != null) {
            fields[x++] = s_rank.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 11);
        }

        // Encoding s_documentId: InternationalString OPTIONAL

        if (s_documentId != null) {
            fields[x++] = s_documentId.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 12);
        }

        // Encoding s_abstract: InternationalString OPTIONAL

        if (s_abstract != null) {
            fields[x++] = s_abstract.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 13);
        }

        // Encoding s_otherInfo: OtherInformation OPTIONAL

        if (s_otherInfo != null) {
            fields[x++] = s_otherInfo.ber_encode();
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the BriefBib.
     */

    public String
    toString() {
        int p;
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        str.append("title ");
        str.append(s_title);
        outputted++;

        if (s_author != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("author ");
            str.append(s_author);
            outputted++;
        }

        if (s_callNumber != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("callNumber ");
            str.append(s_callNumber);
            outputted++;
        }

        if (s_recordType != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("recordType ");
            str.append(s_recordType);
            outputted++;
        }

        if (s_bibliographicLevel != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("bibliographicLevel ");
            str.append(s_bibliographicLevel);
            outputted++;
        }

        if (s_format != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("format ");
            str.append("{");
            for (p = 0; p < s_format.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_format[p]);
            }
            str.append("}");
            outputted++;
        }

        if (s_publicationPlace != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("publicationPlace ");
            str.append(s_publicationPlace);
            outputted++;
        }

        if (s_publicationDate != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("publicationDate ");
            str.append(s_publicationDate);
            outputted++;
        }

        if (s_targetSystemKey != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("targetSystemKey ");
            str.append(s_targetSystemKey);
            outputted++;
        }

        if (s_satisfyingElement != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("satisfyingElement ");
            str.append(s_satisfyingElement);
            outputted++;
        }

        if (s_rank != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("rank ");
            str.append(s_rank);
            outputted++;
        }

        if (s_documentId != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("documentId ");
            str.append(s_documentId);
            outputted++;
        }

        if (s_abstract != null) {
            if (0 < outputted) {
             str.append(", ");
            }


            str.append("abstract ");
            str.append(s_abstract);
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

    public InternationalString s_title;
    public InternationalString s_author; // optional
    public InternationalString s_callNumber; // optional
    public InternationalString s_recordType; // optional
    public InternationalString s_bibliographicLevel; // optional
    public FormatSpec s_format[]; // optional
    public InternationalString s_publicationPlace; // optional
    public InternationalString s_publicationDate; // optional
    public InternationalString s_targetSystemKey; // optional
    public InternationalString s_satisfyingElement; // optional
    public ASN1Integer s_rank; // optional
    public InternationalString s_documentId; // optional
    public InternationalString s_abstract; // optional
    public OtherInformation s_otherInfo; // optional

}