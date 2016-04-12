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
import asn1.ASN1VisibleString;
import asn1.BEREncoding;

/**
 * Class for representing a <code>DefaultDiagFormat_addinfo</code> from <code>Z39-50-APDU-1995</code>
 * <p/>
 * <pre>
 * DefaultDiagFormat_addinfo ::=
 * CHOICE {
 *   v2Addinfo VisibleString
 *   v3Addinfo InternationalString
 * }
 * </pre>
 *
 */
public final class DefaultDiagFormat_addinfo extends ASN1Any {

    /**
     * Default constructor for a DefaultDiagFormat_addinfo.
     */

    public DefaultDiagFormat_addinfo() {
    }

    /**
     * Constructor for a DefaultDiagFormat_addinfo from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public DefaultDiagFormat_addinfo(BEREncoding ber, boolean check_tag)
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

        c_v2Addinfo = null;
        c_v3Addinfo = null;

        // Try choice v2Addinfo
        try {
            c_v2Addinfo = new ASN1VisibleString(ber, check_tag);
            return;
        } catch (ASN1Exception e) {
            // failed to decode, continue on
        }

        // Try choice v3Addinfo
        try {
            c_v3Addinfo = new InternationalString(ber, check_tag);
            return;
        } catch (ASN1Exception e) {
            // failed to decode, continue on
        }

        throw new ASN1Exception("Zebulun DefaultDiagFormat_addinfo: bad BER encoding: choice not matched");
    }

    /**
     * Returns a BER encoding of DefaultDiagFormat_addinfo.
     *
     * @return The BER encoding.
     * @exception ASN1Exception Invalid or cannot be encoded.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        BEREncoding chosen = null;

        // Encoding choice: c_v2Addinfo
        if (c_v2Addinfo != null) {
            chosen = c_v2Addinfo.ber_encode();
        }

        // Encoding choice: c_v3Addinfo
        if (c_v3Addinfo != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_v3Addinfo.ber_encode();
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

        throw new ASN1EncodingException("Zebulun DefaultDiagFormat_addinfo: cannot implicitly tag");
    }

    /**
     * Returns a new String object containing a text representing
     * of the DefaultDiagFormat_addinfo.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");

        boolean found = false;

        if (c_v2Addinfo != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: v2Addinfo> ");
            }
            found = true;
            str.append("v2Addinfo ");
            str.append(c_v2Addinfo);
        }

        if (c_v3Addinfo != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: v3Addinfo> ");
            }
            found = true;
            str.append("v3Addinfo ");
            str.append(c_v3Addinfo);
        }

        str.append("}");

        return str.toString();
    }

/*
 * Internal variables for class.
 */

    public ASN1VisibleString c_v2Addinfo;
    public InternationalString c_v3Addinfo;

}