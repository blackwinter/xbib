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
 * Class for representing a <code>ListEntries</code> from <code>Z39-50-APDU-1995</code>
 * <p/>
 * <pre>
 * ListEntries ::=
 * SEQUENCE {
 *   entries [1] IMPLICIT SEQUENCE OF Entry OPTIONAL
 *   nonsurrogateDiagnostics [2] IMPLICIT SEQUENCE OF DiagRec OPTIONAL
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class ListEntries extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a ListEntries.
     */

    public ListEntries() {
    }



    /**
     * Constructor for a ListEntries from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public ListEntries(BEREncoding ber, boolean check_tag)
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
        // ListEntries should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun ListEntries: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_entries = null;
        s_nonsurrogateDiagnostics = null;

        // Decoding: entries [1] IMPLICIT SEQUENCE OF Entry OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 1 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_entries = new Entry[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_entries[n] = new Entry(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Decoding: nonsurrogateDiagnostics [2] IMPLICIT SEQUENCE OF DiagRec OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_nonsurrogateDiagnostics = new DiagRec[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_nonsurrogateDiagnostics[n] = new DiagRec(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Zebulun ListEntries: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the ListEntries.
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
     * Returns a BER encoding of ListEntries, implicitly tagged.
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

        int num_fields = 0; // number of mandatories
        if (s_entries != null) {
            num_fields++;
        }
        if (s_nonsurrogateDiagnostics != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding f2[];
        int p;

        // Encoding s_entries: SEQUENCE OF OPTIONAL

        if (s_entries != null) {
            f2 = new BEREncoding[s_entries.length];

            for (p = 0; p < s_entries.length; p++) {
                f2[p] = s_entries[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 1, f2);
        }

        // Encoding s_nonsurrogateDiagnostics: SEQUENCE OF OPTIONAL

        if (s_nonsurrogateDiagnostics != null) {
            f2 = new BEREncoding[s_nonsurrogateDiagnostics.length];

            for (p = 0; p < s_nonsurrogateDiagnostics.length; p++) {
                f2[p] = s_nonsurrogateDiagnostics[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 2, f2);
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the ListEntries.
     */

    public String
    toString() {
        int p;
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        if (s_entries != null) {
            str.append("entries ");
            str.append("{");
            for (p = 0; p < s_entries.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_entries[p]);
            }
            str.append("}");
            outputted++;
        }

        if (s_nonsurrogateDiagnostics != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("nonsurrogateDiagnostics ");
            str.append("{");
            for (p = 0; p < s_nonsurrogateDiagnostics.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_nonsurrogateDiagnostics[p]);
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

    public Entry s_entries[]; // optional
    public DiagRec s_nonsurrogateDiagnostics[]; // optional

} // ListEntries


//EOF
