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
import asn1.ASN1Null;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.StringOrNumeric;



/**
 * Class for representing a <code>AttributeOccurrence_attributeValues</code> from <code>RecordSyntax-explain</code>
 * <p/>
 * <pre>
 * AttributeOccurrence_attributeValues ::=
 * CHOICE {
 *   any-or-none [3] IMPLICIT NULL
 *   specific [4] IMPLICIT SEQUENCE OF StringOrNumeric
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class AttributeOccurrence_attributeValues extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a AttributeOccurrence_attributeValues.
     */

    public AttributeOccurrence_attributeValues() {
    }



    /**
     * Constructor for a AttributeOccurrence_attributeValues from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public AttributeOccurrence_attributeValues(BEREncoding ber, boolean check_tag)
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
        // Null out all choices

        c_any_or_none = null;
        c_specific = null;

        // Try choice any-or-none
        if (ber.tag_get() == 3 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_any_or_none = new ASN1Null(ber, false);
            return;
        }

        // Try choice specific
        if (ber.tag_get() == 4 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            BEREncoding ber_data;
            ber_data = ber;
            BERConstructed ber_cons;
            try {
                ber_cons = (BERConstructed) ber_data;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun AttributeOccurrence_attributeValues: bad BER form\n");
            }

            int num_parts = ber_cons.number_components();
            int p;

            c_specific = new StringOrNumeric[num_parts];

            for (p = 0; p < num_parts; p++) {
                c_specific[p] = new StringOrNumeric(ber_cons.elementAt(p), true);
            }
            return;
        }

        throw new ASN1Exception("Zebulun AttributeOccurrence_attributeValues: bad BER encoding: choice not matched");
    }



    /**
     * Returns a BER encoding of AttributeOccurrence_attributeValues.
     *
     * @return The BER encoding.
     * @exception ASN1Exception Invalid or cannot be encoded.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        BEREncoding chosen = null;

        BEREncoding f2[];
        int p;
        // Encoding choice: c_any_or_none
        if (c_any_or_none != null) {
            chosen = c_any_or_none.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
        }

        // Encoding choice: c_specific
        if (c_specific != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            f2 = new BEREncoding[c_specific.length];

            for (p = 0; p < c_specific.length; p++) {
                f2[p] = c_specific[p].ber_encode();
            }

            chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 4, f2);
        }

        // Check for error of having none of the choices set
        if (chosen == null) {
            throw new ASN1Exception("CHOICE not set");
        }

        return chosen;
    }



    /**
     * Generating a BER encoding of the object
     * and implicitly tagging it.
     * <p/>
     * This method is for internal use only. You should use
     * the ber_encode method that does not take a parameter.
     * <p/>
     * This function should never be used, because this
     * production is a CHOICE.
     * It must never have an implicit tag.
     * <p/>
     * An exception will be thrown if it is called.
     *
     * @param tag_type the type of the tag.
     * @param tag      the tag.
     * @throws ASN1Exception if it cannot be BER encoded.
     */

    public BEREncoding
    ber_encode(int tag_type, int tag)
            throws ASN1Exception {
        // This method must not be called!

        // Method is not available because this is a basic CHOICE
        // which does not have an explicit tag on it. So it is not
        // permitted to allow something else to apply an implicit
        // tag on it, otherwise the tag identifying which CHOICE
        // it is will be overwritten and lost.

        throw new ASN1EncodingException("Zebulun AttributeOccurrence_attributeValues: cannot implicitly tag");
    }



    /**
     * Returns a new String object containing a text representing
     * of the AttributeOccurrence_attributeValues.
     */

    public String
    toString() {
        int p;
        StringBuffer str = new StringBuffer("{");

        boolean found = false;

        if (c_any_or_none != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: any-or-none> ");
            }
            found = true;
            str.append("any-or-none ");
            str.append(c_any_or_none);
        }

        if (c_specific != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: specific> ");
            }
            found = true;
            str.append("specific ");
            str.append("{");
            for (p = 0; p < c_specific.length; p++) {
                str.append(c_specific[p]);
            }
            str.append("}");
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public ASN1Null c_any_or_none;
    public StringOrNumeric c_specific[];

} // AttributeOccurrence_attributeValues


//EOF
