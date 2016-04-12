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
package z3950.DiagFormat;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Null;
import asn1.ASN1ObjectIdentifier;
import asn1.BERConstructed;
import asn1.BEREncoding;


/**
 * Class for representing a <code>DiagFormat_accessCtrl</code> from <code>DiagnosticFormatDiag1</code>
 * <p/>
 * <pre>
 * DiagFormat_accessCtrl ::=
 * CHOICE {
 *   noUser [1] IMPLICIT NULL
 *   refused [2] IMPLICIT NULL
 *   simple [3] IMPLICIT NULL
 *   oid [4] IMPLICIT SEQUENCE OF OBJECT IDENTIFIER
 *   alternative [5] IMPLICIT SEQUENCE OF OBJECT IDENTIFIER
 *   pwdInv [6] IMPLICIT NULL
 *   pwdExp [7] IMPLICIT NULL
 * }
 * </pre>
 *
 */
public final class DiagFormat_accessCtrl extends ASN1Any {

    /**
     * Default constructor for a DiagFormat_accessCtrl.
     */

    public DiagFormat_accessCtrl() {
    }

    /**
     * Constructor for a DiagFormat_accessCtrl from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public DiagFormat_accessCtrl(BEREncoding ber, boolean check_tag)
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

        c_noUser = null;
        c_refused = null;
        c_simple = null;
        c_oid = null;
        c_alternative = null;
        c_pwdInv = null;
        c_pwdExp = null;

        // Try choice noUser
        if (ber.tag_get() == 1 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_noUser = new ASN1Null(ber, false);
            return;
        }

        // Try choice refused
        if (ber.tag_get() == 2 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_refused = new ASN1Null(ber, false);
            return;
        }

        // Try choice simple
        if (ber.tag_get() == 3 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_simple = new ASN1Null(ber, false);
            return;
        }

        // Try choice oid
        if (ber.tag_get() == 4 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            BEREncoding ber_data;
            ber_data = ber;
            BERConstructed ber_cons;
            try {
                ber_cons = (BERConstructed) ber_data;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun DiagFormat_accessCtrl: bad BER form\n");
            }

            int num_parts = ber_cons.number_components();
            int p;

            c_oid = new ASN1ObjectIdentifier[num_parts];

            for (p = 0; p < num_parts; p++) {
                c_oid[p] = new ASN1ObjectIdentifier(ber_cons.elementAt(p), true);
            }
            return;
        }

        // Try choice alternative
        if (ber.tag_get() == 5 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            BEREncoding ber_data;
            ber_data = ber;
            BERConstructed ber_cons;
            try {
                ber_cons = (BERConstructed) ber_data;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun DiagFormat_accessCtrl: bad BER form\n");
            }

            int num_parts = ber_cons.number_components();
            int p;

            c_alternative = new ASN1ObjectIdentifier[num_parts];

            for (p = 0; p < num_parts; p++) {
                c_alternative[p] = new ASN1ObjectIdentifier(ber_cons.elementAt(p), true);
            }
            return;
        }

        // Try choice pwdInv
        if (ber.tag_get() == 6 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_pwdInv = new ASN1Null(ber, false);
            return;
        }

        // Try choice pwdExp
        if (ber.tag_get() == 7 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_pwdExp = new ASN1Null(ber, false);
            return;
        }

        throw new ASN1Exception("Zebulun DiagFormat_accessCtrl: bad BER encoding: choice not matched");
    }

    /**
     * Returns a BER encoding of DiagFormat_accessCtrl.
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
        // Encoding choice: c_noUser
        if (c_noUser != null) {
            chosen = c_noUser.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
        }

        // Encoding choice: c_refused
        if (c_refused != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_refused.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
        }

        // Encoding choice: c_simple
        if (c_simple != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_simple.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
        }

        // Encoding choice: c_oid
        if (c_oid != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            f2 = new BEREncoding[c_oid.length];

            for (p = 0; p < c_oid.length; p++) {
                f2[p] = c_oid[p].ber_encode();
            }

            chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 4, f2);
        }

        // Encoding choice: c_alternative
        if (c_alternative != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            f2 = new BEREncoding[c_alternative.length];

            for (p = 0; p < c_alternative.length; p++) {
                f2[p] = c_alternative[p].ber_encode();
            }

            chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 5, f2);
        }

        // Encoding choice: c_pwdInv
        if (c_pwdInv != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_pwdInv.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 6);
        }

        // Encoding choice: c_pwdExp
        if (c_pwdExp != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_pwdExp.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 7);
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

        throw new ASN1EncodingException("Zebulun DiagFormat_accessCtrl: cannot implicitly tag");
    }



    /**
     * Returns a new String object containing a text representing
     * of the DiagFormat_accessCtrl.
     */

    public String
    toString() {
        int p;
        StringBuffer str = new StringBuffer("{");

        boolean found = false;

        if (c_noUser != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: noUser> ");
            }
            found = true;
            str.append("noUser ");
            str.append(c_noUser);
        }

        if (c_refused != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: refused> ");
            }
            found = true;
            str.append("refused ");
            str.append(c_refused);
        }

        if (c_simple != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: simple> ");
            }
            found = true;
            str.append("simple ");
            str.append(c_simple);
        }

        if (c_oid != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: oid> ");
            }
            found = true;
            str.append("oid ");
            str.append("{");
            for (p = 0; p < c_oid.length; p++) {
                str.append(c_oid[p]);
            }
            str.append("}");
        }

        if (c_alternative != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: alternative> ");
            }
            found = true;
            str.append("alternative ");
            str.append("{");
            for (p = 0; p < c_alternative.length; p++) {
                str.append(c_alternative[p]);
            }
            str.append("}");
        }

        if (c_pwdInv != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: pwdInv> ");
            }
            found = true;
            str.append("pwdInv ");
            str.append(c_pwdInv);
        }

        if (c_pwdExp != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: pwdExp> ");
            }
            found = true;
            str.append("pwdExp ");
            str.append(c_pwdExp);
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public ASN1Null c_noUser;
    public ASN1Null c_refused;
    public ASN1Null c_simple;
    public ASN1ObjectIdentifier c_oid[];
    public ASN1ObjectIdentifier c_alternative[];
    public ASN1Null c_pwdInv;
    public ASN1Null c_pwdExp;

} // DiagFormat_accessCtrl


//EOF
