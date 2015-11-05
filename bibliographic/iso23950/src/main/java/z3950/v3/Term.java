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
import asn1.ASN1GeneralizedTime;
import asn1.ASN1Integer;
import asn1.ASN1Null;
import asn1.ASN1ObjectIdentifier;
import asn1.ASN1OctetString;
import asn1.BEREncoding;



/**
 * Class for representing a <code>Term</code> from <code>Z39-50-APDU-1995</code>
 * <p/>
 * <pre>
 * Term ::=
 * CHOICE {
 *   general [45] IMPLICIT OCTET STRING
 *   numeric [215] IMPLICIT INTEGER
 *   characterString [216] IMPLICIT InternationalString
 *   oid [217] IMPLICIT OBJECT IDENTIFIER
 *   dateTime [218] IMPLICIT GeneralizedTime
 *   external [219] IMPLICIT EXTERNAL
 *   integerAndUnit [220] IMPLICIT IntUnit
 *   null [221] IMPLICIT NULL
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class Term extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a Term.
     */

    public Term() {
    }



    /**
     * Constructor for a Term from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public Term(BEREncoding ber, boolean check_tag)
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

        c_general = null;
        c_numeric = null;
        c_characterString = null;
        c_oid = null;
        c_dateTime = null;
        c_external = null;
        c_integerAndUnit = null;
        c_null = null;

        // Try choice general
        if (ber.tag_get() == 45 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_general = new ASN1OctetString(ber, false);
            return;
        }

        // Try choice numeric
        if (ber.tag_get() == 215 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_numeric = new ASN1Integer(ber, false);
            return;
        }

        // Try choice characterString
        if (ber.tag_get() == 216 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_characterString = new InternationalString(ber, false);
            return;
        }

        // Try choice oid
        if (ber.tag_get() == 217 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_oid = new ASN1ObjectIdentifier(ber, false);
            return;
        }

        // Try choice dateTime
        if (ber.tag_get() == 218 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_dateTime = new ASN1GeneralizedTime(ber, false);
            return;
        }

        // Try choice external
        if (ber.tag_get() == 219 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_external = new ASN1External(ber, false);
            return;
        }

        // Try choice integerAndUnit
        if (ber.tag_get() == 220 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_integerAndUnit = new IntUnit(ber, false);
            return;
        }

        // Try choice null
        if (ber.tag_get() == 221 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_null = new ASN1Null(ber, false);
            return;
        }

        throw new ASN1Exception("Zebulun Term: bad BER encoding: choice not matched");
    }



    /**
     * Returns a BER encoding of Term.
     *
     * @return The BER encoding.
     * @exception ASN1Exception Invalid or cannot be encoded.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        BEREncoding chosen = null;

        // Encoding choice: c_general
        if (c_general != null) {
            chosen = c_general.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 45);
        }

        // Encoding choice: c_numeric
        if (c_numeric != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_numeric.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 215);
        }

        // Encoding choice: c_characterString
        if (c_characterString != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_characterString.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 216);
        }

        // Encoding choice: c_oid
        if (c_oid != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_oid.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 217);
        }

        // Encoding choice: c_dateTime
        if (c_dateTime != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_dateTime.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 218);
        }

        // Encoding choice: c_external
        if (c_external != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_external.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 219);
        }

        // Encoding choice: c_integerAndUnit
        if (c_integerAndUnit != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_integerAndUnit.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 220);
        }

        // Encoding choice: c_null
        if (c_null != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_null.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 221);
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

        throw new ASN1EncodingException("Zebulun Term: cannot implicitly tag");
    }



    /**
     * Returns a new String object containing a text representing
     * of the Term.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");

        boolean found = false;

        if (c_general != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: general> ");
            }
            found = true;
            str.append("general ");
            str.append(c_general);
        }

        if (c_numeric != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: numeric> ");
            }
            found = true;
            str.append("numeric ");
            str.append(c_numeric);
        }

        if (c_characterString != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: characterString> ");
            }
            found = true;
            str.append("characterString ");
            str.append(c_characterString);
        }

        if (c_oid != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: oid> ");
            }
            found = true;
            str.append("oid ");
            str.append(c_oid);
        }

        if (c_dateTime != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: dateTime> ");
            }
            found = true;
            str.append("dateTime ");
            str.append(c_dateTime);
        }

        if (c_external != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: external> ");
            }
            found = true;
            str.append("external ");
            str.append(c_external);
        }

        if (c_integerAndUnit != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: integerAndUnit> ");
            }
            found = true;
            str.append("integerAndUnit ");
            str.append(c_integerAndUnit);
        }

        if (c_null != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: null> ");
            }
            found = true;
            str.append("null ");
            str.append(c_null);
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public ASN1OctetString c_general;
    public ASN1Integer c_numeric;
    public InternationalString c_characterString;
    public ASN1ObjectIdentifier c_oid;
    public ASN1GeneralizedTime c_dateTime;
    public ASN1External c_external;
    public IntUnit c_integerAndUnit;
    public ASN1Null c_null;

} // Term


//EOF
