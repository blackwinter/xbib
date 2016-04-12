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
import asn1.ASN1External;
import asn1.BEREncoding;

/**
 * Class for representing a <code>DiagRec</code> from <code>Z39-50-APDU-1995</code>
 * <p/>
 * <pre>
 * DiagRec ::=
 * CHOICE {
 *   defaultFormat DefaultDiagFormat
 *   externallyDefined EXTERNAL
 * }
 * </pre>
 *
 */

public final class DiagRec extends ASN1Any {

    /**
     * Default constructor for a DiagRec.
     */

    public DiagRec() {
    }

    /**
     * Constructor for a DiagRec from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public DiagRec(BEREncoding ber, boolean check_tag)
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

        c_defaultFormat = null;
        c_externallyDefined = null;

        // Try choice defaultFormat
        try {
            c_defaultFormat = new DefaultDiagFormat(ber, check_tag);
            return;
        } catch (ASN1Exception e) {
            // failed to decode, continue on
        }

        // Try choice externallyDefined
        try {
            c_externallyDefined = new ASN1External(ber, check_tag);
            return;
        } catch (ASN1Exception e) {
            // failed to decode, continue on
        }

        throw new ASN1Exception("Zebulun DiagRec: bad BER encoding: choice not matched");
    }

    /**
     * Returns a BER encoding of DiagRec.
     *
     * @return The BER encoding.
     * @exception ASN1Exception Invalid or cannot be encoded.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        BEREncoding chosen = null;

        // Encoding choice: c_defaultFormat
        if (c_defaultFormat != null) {
            chosen = c_defaultFormat.ber_encode();
        }

        // Encoding choice: c_externallyDefined
        if (c_externallyDefined != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_externallyDefined.ber_encode();
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

        throw new ASN1EncodingException("Zebulun DiagRec: cannot implicitly tag");
    }

    /**
     * Returns a new String object containing a text representing
     * of the DiagRec.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");

        boolean found = false;

        if (c_defaultFormat != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: defaultFormat> ");
            }
            found = true;
            str.append("defaultFormat ");
            str.append(c_defaultFormat);
        }

        if (c_externallyDefined != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: externallyDefined> ");
            }
            found = true;
            str.append("externallyDefined ");
            str.append(c_externallyDefined);
        }

        str.append("}");

        return str.toString();
    }

/*
 * Internal variables for class.
 */

    public DefaultDiagFormat c_defaultFormat;
    public ASN1External c_externallyDefined;

}