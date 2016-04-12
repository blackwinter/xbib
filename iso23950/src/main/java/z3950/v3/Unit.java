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
 * Class for representing a <code>Unit</code> from <code>Z39-50-APDU-1995</code>
 * <p/>
 * <pre>
 * Unit ::=
 * SEQUENCE {
 *   unitSystem [1] EXPLICIT InternationalString OPTIONAL
 *   unitType [2] EXPLICIT StringOrNumeric OPTIONAL
 *   unit [3] EXPLICIT StringOrNumeric OPTIONAL
 *   scaleFactor [4] IMPLICIT INTEGER OPTIONAL
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class Unit extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a Unit.
     */

    public Unit() {
    }



    /**
     * Constructor for a Unit from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public Unit(BEREncoding ber, boolean check_tag)
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
        // Unit should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun Unit: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;
        BERConstructed tagged;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at endStream of BER

        s_unitSystem = null;
        s_unitType = null;
        s_unit = null;
        s_scaleFactor = null;

        // Decoding: unitSystem [1] EXPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 1 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagged = (BERConstructed) p;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun Unit: bad BER encoding: s_unitSystem tag bad\n");
            }
            if (tagged.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun Unit: bad BER encoding: s_unitSystem tag bad\n");
            }

            s_unitSystem = new InternationalString(tagged.elementAt(0), true);
            part++;
        }

        // Decoding: unitType [2] EXPLICIT StringOrNumeric OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagged = (BERConstructed) p;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun Unit: bad BER encoding: s_unitType tag bad\n");
            }
            if (tagged.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun Unit: bad BER encoding: s_unitType tag bad\n");
            }

            s_unitType = new StringOrNumeric(tagged.elementAt(0), true);
            part++;
        }

        // Decoding: unit [3] EXPLICIT StringOrNumeric OPTIONAL

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
                        ("Zebulun Unit: bad BER encoding: s_unit tag bad\n");
            }
            if (tagged.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun Unit: bad BER encoding: s_unit tag bad\n");
            }

            s_unit = new StringOrNumeric(tagged.elementAt(0), true);
            part++;
        }

        // Decoding: scaleFactor [4] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 4 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_scaleFactor = new ASN1Integer(p, false);
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Zebulun Unit: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the Unit.
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
     * Returns a BER encoding of Unit, implicitly tagged.
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
        if (s_unitSystem != null) {
            num_fields++;
        }
        if (s_unitType != null) {
            num_fields++;
        }
        if (s_unit != null) {
            num_fields++;
        }
        if (s_scaleFactor != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding enc[];

        // Encoding s_unitSystem: InternationalString OPTIONAL

        if (s_unitSystem != null) {
            enc = new BEREncoding[1];
            enc[0] = s_unitSystem.ber_encode();
            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 1, enc);
        }

        // Encoding s_unitType: StringOrNumeric OPTIONAL

        if (s_unitType != null) {
            enc = new BEREncoding[1];
            enc[0] = s_unitType.ber_encode();
            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 2, enc);
        }

        // Encoding s_unit: StringOrNumeric OPTIONAL

        if (s_unit != null) {
            enc = new BEREncoding[1];
            enc[0] = s_unit.ber_encode();
            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 3, enc);
        }

        // Encoding s_scaleFactor: INTEGER OPTIONAL

        if (s_scaleFactor != null) {
            fields[x++] = s_scaleFactor.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the Unit.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        if (s_unitSystem != null) {
            str.append("unitSystem ");
            str.append(s_unitSystem);
            outputted++;
        }

        if (s_unitType != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("unitType ");
            str.append(s_unitType);
            outputted++;
        }

        if (s_unit != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("unit ");
            str.append(s_unit);
            outputted++;
        }

        if (s_scaleFactor != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("scaleFactor ");
            str.append(s_scaleFactor);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public InternationalString s_unitSystem; // optional
    public StringOrNumeric s_unitType; // optional
    public StringOrNumeric s_unit; // optional
    public ASN1Integer s_scaleFactor; // optional

} // Unit


//EOF
