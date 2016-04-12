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
import asn1.ASN1ObjectIdentifier;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;

/**
 * Class for representing a <code>CompSpec</code> from <code>Z39-50-APDU-1995</code>
 *
 * <pre>
 * CompSpec ::=
 * SEQUENCE {
 *   selectAlternativeSyntax [1] IMPLICIT BOOLEAN
 *   generic [2] IMPLICIT Specification OPTIONAL
 *   dbSpecific [3] IMPLICIT SEQUENCE OF CompSpec_dbSpecific OPTIONAL
 *   recordSyntax [4] IMPLICIT SEQUENCE OF OBJECT IDENTIFIER OPTIONAL
 * }
 * </pre>
 *
 */

public final class CompSpec extends ASN1Any {

    /**
     * Default constructor for a CompSpec.
     */

    public CompSpec() {
    }

    /**
     * Constructor for a CompSpec from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public CompSpec(BEREncoding ber, boolean check_tag)
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
        // CompSpec should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun CompSpec: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Decoding: selectAlternativeSyntax [1] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun CompSpec: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 1 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun CompSpec: bad tag in s_selectAlternativeSyntax\n");
        }

        s_selectAlternativeSyntax = new ASN1Boolean(p, false);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_generic = null;
        s_dbSpecific = null;
        s_recordSyntax = null;

        // Decoding: generic [2] IMPLICIT Specification OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_generic = new Specification(p, false);
            part++;
        }

        // Decoding: dbSpecific [3] IMPLICIT SEQUENCE OF CompSpec_dbSpecific OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 3 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_dbSpecific = new CompSpec_dbSpecific[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_dbSpecific[n] = new CompSpec_dbSpecific(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Decoding: recordSyntax [4] IMPLICIT SEQUENCE OF OBJECT IDENTIFIER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 4 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_recordSyntax = new ASN1ObjectIdentifier[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_recordSyntax[n] = new ASN1ObjectIdentifier(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("CompSpec: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }

    /**
     * Returns a BER encoding of the CompSpec.
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
     * Returns a BER encoding of CompSpec, implicitly tagged.
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
        if (s_generic != null) {
            num_fields++;
        }
        if (s_dbSpecific != null) {
            num_fields++;
        }
        if (s_recordSyntax != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding f2[];
        int p;

        // Encoding s_selectAlternativeSyntax: BOOLEAN

        fields[x++] = s_selectAlternativeSyntax.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);

        // Encoding s_generic: Specification OPTIONAL

        if (s_generic != null) {
            fields[x++] = s_generic.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
        }

        // Encoding s_dbSpecific: SEQUENCE OF OPTIONAL

        if (s_dbSpecific != null) {
            f2 = new BEREncoding[s_dbSpecific.length];

            for (p = 0; p < s_dbSpecific.length; p++) {
                f2[p] = s_dbSpecific[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 3, f2);
        }

        // Encoding s_recordSyntax: SEQUENCE OF OPTIONAL

        if (s_recordSyntax != null) {
            f2 = new BEREncoding[s_recordSyntax.length];

            for (p = 0; p < s_recordSyntax.length; p++) {
                f2[p] = s_recordSyntax[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 4, f2);
        }

        return new BERConstructed(tag_type, tag, fields);
    }

    /**
     * Returns a new String object containing a text representing
     * of the CompSpec.
     */

    public String
    toString() {
        int p;
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        str.append("selectAlternativeSyntax ");
        str.append(s_selectAlternativeSyntax);
        outputted++;

        if (s_generic != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("generic ");
            str.append(s_generic);
            outputted++;
        }

        if (s_dbSpecific != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("dbSpecific ");
            str.append("{");
            for (p = 0; p < s_dbSpecific.length; p++) {
                if (p != 0) {
                  str.append(", ");
                }
                str.append(s_dbSpecific[p]);
            }
            str.append("}");
            outputted++;
        }

        if (s_recordSyntax != null) {
            if (0 < outputted) {
             str.append(", ");
            }
            str.append("recordSyntax ");
            str.append("{");
            for (p = 0; p < s_recordSyntax.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_recordSyntax[p]);
            }
            str.append("}");
            outputted++;
        }

        str.append("}");

        return str.toString();
    }

/*
 * Internal variables for class.
 */

    public ASN1Boolean s_selectAlternativeSyntax;
    public Specification s_generic; // optional
    public CompSpec_dbSpecific s_dbSpecific[]; // optional
    public ASN1ObjectIdentifier s_recordSyntax[]; // optional

}