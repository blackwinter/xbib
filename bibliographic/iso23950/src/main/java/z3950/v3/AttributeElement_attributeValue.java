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
import asn1.BEREncoding;

/**
 * Class for representing a <code>AttributeElement_attributeValue</code> from <code>Z39-50-APDU-1995</code>
 * <p/>
 * <pre>
 * AttributeElement_attributeValue ::=
 * CHOICE {
 *   numeric [121] IMPLICIT INTEGER
 *   complex [224] IMPLICIT AttributeElement_attributeValue_complex
 * }
 * </pre>
 *
 */

public final class AttributeElement_attributeValue extends ASN1Any {

    /**
     * Default constructor for a AttributeElement_attributeValue.
     */

    public AttributeElement_attributeValue() {
    }

    /**
     * Constructor for a AttributeElement_attributeValue from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public AttributeElement_attributeValue(BEREncoding ber, boolean check_tag)
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

        c_numeric = null;
        c_complex = null;

        // Try choice numeric
        if (ber.tag_get() == 121 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_numeric = new ASN1Integer(ber, false);
            return;
        }

        // Try choice complex
        if (ber.tag_get() == 224 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_complex = new AttributeElement_attributeValue_complex(ber, false);
            return;
        }

        throw new ASN1Exception("Zebulun AttributeElement_attributeValue: bad BER encoding: choice not matched");
    }

    /**
     * Returns a BER encoding of AttributeElement_attributeValue.
     *
     * @return The BER encoding.
     * @exception ASN1Exception Invalid or cannot be encoded.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        BEREncoding chosen = null;

        // Encoding choice: c_numeric
        if (c_numeric != null) {
            chosen = c_numeric.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 121);
        }

        // Encoding choice: c_complex
        if (c_complex != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_complex.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 224);
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

        throw new ASN1EncodingException("Zebulun AttributeElement_attributeValue: cannot implicitly tag");
    }

    /**
     * Returns a new String object containing a text representing
     * of the AttributeElement_attributeValue.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");

        boolean found = false;

        if (c_numeric != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: numeric> ");
            }
            found = true;
            str.append("numeric ");
            str.append(c_numeric);
        }

        if (c_complex != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: complex> ");
            }
            found = true;
            str.append("complex ");
            str.append(c_complex);
        }

        str.append("}");

        return str.toString();
    }

/*
 * Internal variables for class.
 */

    public ASN1Integer c_numeric;
    public AttributeElement_attributeValue_complex c_complex;

}