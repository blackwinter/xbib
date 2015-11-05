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
package z3950.RS_Explain;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;


/**
 * Class for representing a <code>Costs</code> from <code>RecordSyntax-explain</code>
 * <p/>
 * <pre>
 * Costs ::=
 * SEQUENCE {
 *   connectCharge [0] IMPLICIT Charge OPTIONAL
 *   connectTime [1] IMPLICIT Charge OPTIONAL
 *   displayCharge [2] IMPLICIT Charge OPTIONAL
 *   searchCharge [3] IMPLICIT Charge OPTIONAL
 *   subscriptCharge [4] IMPLICIT Charge OPTIONAL
 *   otherCharges [5] IMPLICIT SEQUENCE OF Costs_otherCharges OPTIONAL
 * }
 * </pre>
 *
 */

public final class Costs extends ASN1Any {

    /**
     * Default constructor for a Costs.
     */

    public Costs() {
    }

    /**
     * Constructor for a Costs from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public Costs(BEREncoding ber, boolean check_tag)
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
        // Costs should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun Costs: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_connectCharge = null;
        s_connectTime = null;
        s_displayCharge = null;
        s_searchCharge = null;
        s_subscriptCharge = null;
        s_otherCharges = null;

        // Decoding: connectCharge [0] IMPLICIT Charge OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 0 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_connectCharge = new Charge(p, false);
            part++;
        }

        // Decoding: connectTime [1] IMPLICIT Charge OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 1 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_connectTime = new Charge(p, false);
            part++;
        }

        // Decoding: displayCharge [2] IMPLICIT Charge OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_displayCharge = new Charge(p, false);
            part++;
        }

        // Decoding: searchCharge [3] IMPLICIT Charge OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 3 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_searchCharge = new Charge(p, false);
            part++;
        }

        // Decoding: subscriptCharge [4] IMPLICIT Charge OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 4 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_subscriptCharge = new Charge(p, false);
            part++;
        }

        // Decoding: otherCharges [5] IMPLICIT SEQUENCE OF Costs_otherCharges OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 5 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_otherCharges = new Costs_otherCharges[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_otherCharges[n] = new Costs_otherCharges(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Zebulun Costs: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the Costs.
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
     * Returns a BER encoding of Costs, implicitly tagged.
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
        if (s_connectCharge != null) {
            num_fields++;
        }
        if (s_connectTime != null) {
            num_fields++;
        }
        if (s_displayCharge != null) {
            num_fields++;
        }
        if (s_searchCharge != null) {
            num_fields++;
        }
        if (s_subscriptCharge != null) {
            num_fields++;
        }
        if (s_otherCharges != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding f2[];
        int p;

        // Encoding s_connectCharge: Charge OPTIONAL

        if (s_connectCharge != null) {
            fields[x++] = s_connectCharge.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 0);
        }

        // Encoding s_connectTime: Charge OPTIONAL

        if (s_connectTime != null) {
            fields[x++] = s_connectTime.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
        }

        // Encoding s_displayCharge: Charge OPTIONAL

        if (s_displayCharge != null) {
            fields[x++] = s_displayCharge.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
        }

        // Encoding s_searchCharge: Charge OPTIONAL

        if (s_searchCharge != null) {
            fields[x++] = s_searchCharge.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
        }

        // Encoding s_subscriptCharge: Charge OPTIONAL

        if (s_subscriptCharge != null) {
            fields[x++] = s_subscriptCharge.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);
        }

        // Encoding s_otherCharges: SEQUENCE OF OPTIONAL

        if (s_otherCharges != null) {
            f2 = new BEREncoding[s_otherCharges.length];

            for (p = 0; p < s_otherCharges.length; p++) {
                f2[p] = s_otherCharges[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 5, f2);
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the Costs.
     */

    public String
    toString() {
        int p;
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        if (s_connectCharge != null) {
            str.append("connectCharge ");
            str.append(s_connectCharge);
            outputted++;
        }

        if (s_connectTime != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("connectTime ");
            str.append(s_connectTime);
            outputted++;
        }

        if (s_displayCharge != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("displayCharge ");
            str.append(s_displayCharge);
            outputted++;
        }

        if (s_searchCharge != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("searchCharge ");
            str.append(s_searchCharge);
            outputted++;
        }

        if (s_subscriptCharge != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("subscriptCharge ");
            str.append(s_subscriptCharge);
            outputted++;
        }

        if (s_otherCharges != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("otherCharges ");
            str.append("{");
            for (p = 0; p < s_otherCharges.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_otherCharges[p]);
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

    public Charge s_connectCharge; // optional
    public Charge s_connectTime; // optional
    public Charge s_displayCharge; // optional
    public Charge s_searchCharge; // optional
    public Charge s_subscriptCharge; // optional
    public Costs_otherCharges s_otherCharges[]; // optional

} // Costs


//EOF
