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
package z3950.RS_generic;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1ObjectIdentifier;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;

/**
 * Class for representing a <code>Variant</code> from <code>RecordSyntax-generic</code>
 * <p/>
 * <pre>
 * Variant ::=
 * SEQUENCE {
 *   globalVariantSetId [1] IMPLICIT OBJECT IDENTIFIER OPTIONAL
 *   triples [2] IMPLICIT SEQUENCE OF Variant_triples
 * }
 * </pre>
 *
 */

public final class Variant extends ASN1Any {

    /**
     * Default constructor for a Variant.
     */

    public Variant() {
    }

    /**
     * Constructor for a Variant from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public Variant(BEREncoding ber, boolean check_tag)
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
        // Variant should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun Variant: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Decoding: globalVariantSetId [1] IMPLICIT OBJECT IDENTIFIER OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun Variant: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 1 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_globalVariantSetId = new ASN1ObjectIdentifier(p, false);
            part++;
        }

        // Decoding: triples [2] IMPLICIT SEQUENCE OF Variant_triples

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun Variant: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 2 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun Variant: bad tag in s_triples\n");
        }

        try {
            BERConstructed cons = (BERConstructed) p;
            int parts = cons.number_components();
            s_triples = new Variant_triples[parts];
            int n;
            for (n = 0; n < parts; n++) {
                s_triples[n] = new Variant_triples(cons.elementAt(n), true);
            }
        } catch (ClassCastException e) {
            throw new ASN1EncodingException("Bad BER");
        }
        part++;

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Zebulun Variant: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }

    /**
     * Returns a BER encoding of the Variant.
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
     * Returns a BER encoding of Variant, implicitly tagged.
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
        if (s_globalVariantSetId != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding f2[];
        int p;

        // Encoding s_globalVariantSetId: OBJECT IDENTIFIER OPTIONAL

        if (s_globalVariantSetId != null) {
            fields[x++] = s_globalVariantSetId.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
        }

        // Encoding s_triples: SEQUENCE OF

        f2 = new BEREncoding[s_triples.length];

        for (p = 0; p < s_triples.length; p++) {
            f2[p] = s_triples[p].ber_encode();
        }

        fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 2, f2);

        return new BERConstructed(tag_type, tag, fields);
    }

    /**
     * Returns a new String object containing a text representing
     * of the Variant.
     */

    public String
    toString() {
        int p;
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        if (s_globalVariantSetId != null) {
            str.append("globalVariantSetId ");
            str.append(s_globalVariantSetId);
            outputted++;
        }

        if (0 < outputted) {
             str.append(", ");
        }
        str.append("triples ");
        str.append("{");
        for (p = 0; p < s_triples.length; p++) {
            if (p != 0) {
             str.append(", ");
            }
            str.append(s_triples[p]);
        }
        str.append("}");
        outputted++;

        str.append("}");

        return str.toString();
    }

/*
 * Internal variables for class.
 */

    public ASN1ObjectIdentifier s_globalVariantSetId; // optional
    public Variant_triples s_triples[];

}