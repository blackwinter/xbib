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
import asn1.BERConstructed;
import asn1.BEREncoding;



/**
 * Class for representing a <code>Records</code> from <code>Z39-50-APDU-1995</code>
 * <p/>
 * <pre>
 * Records ::=
 * CHOICE {
 *   responseRecords [28] IMPLICIT SEQUENCE OF NamePlusRecord
 *   nonSurrogateDiagnostic [130] IMPLICIT DefaultDiagFormat
 *   multipleNonSurDiagnostics [205] IMPLICIT SEQUENCE OF DiagRec
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class Records extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a Records.
     */

    public Records() {
    }



    /**
     * Constructor for a Records from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public Records(BEREncoding ber, boolean check_tag)
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

        c_responseRecords = null;
        c_nonSurrogateDiagnostic = null;
        c_multipleNonSurDiagnostics = null;

        // Try choice responseRecords
        if (ber.tag_get() == 28 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            BEREncoding ber_data;
            ber_data = ber;
            BERConstructed ber_cons;
            try {
                ber_cons = (BERConstructed) ber_data;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun Records: bad BER form\n");
            }

            int num_parts = ber_cons.number_components();
            int p;

            c_responseRecords = new NamePlusRecord[num_parts];

            for (p = 0; p < num_parts; p++) {
                c_responseRecords[p] = new NamePlusRecord(ber_cons.elementAt(p), true);
            }
            return;
        }

        // Try choice nonSurrogateDiagnostic
        if (ber.tag_get() == 130 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_nonSurrogateDiagnostic = new DefaultDiagFormat(ber, false);
            return;
        }

        // Try choice multipleNonSurDiagnostics
        if (ber.tag_get() == 205 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            BEREncoding ber_data;
            ber_data = ber;
            BERConstructed ber_cons;
            try {
                ber_cons = (BERConstructed) ber_data;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun Records: bad BER form\n");
            }

            int num_parts = ber_cons.number_components();
            int p;

            c_multipleNonSurDiagnostics = new DiagRec[num_parts];

            for (p = 0; p < num_parts; p++) {
                c_multipleNonSurDiagnostics[p] = new DiagRec(ber_cons.elementAt(p), true);
            }
            return;
        }

        throw new ASN1Exception("Zebulun Records: bad BER encoding: choice not matched");
    }



    /**
     * Returns a BER encoding of Records.
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
        // Encoding choice: c_responseRecords
        if (c_responseRecords != null) {
            f2 = new BEREncoding[c_responseRecords.length];

            for (p = 0; p < c_responseRecords.length; p++) {
                f2[p] = c_responseRecords[p].ber_encode();
            }

            chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 28, f2);
        }

        // Encoding choice: c_nonSurrogateDiagnostic
        if (c_nonSurrogateDiagnostic != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_nonSurrogateDiagnostic.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 130);
        }

        // Encoding choice: c_multipleNonSurDiagnostics
        if (c_multipleNonSurDiagnostics != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            f2 = new BEREncoding[c_multipleNonSurDiagnostics.length];

            for (p = 0; p < c_multipleNonSurDiagnostics.length; p++) {
                f2[p] = c_multipleNonSurDiagnostics[p].ber_encode();
            }

            chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 205, f2);
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

        throw new ASN1EncodingException("Zebulun Records: cannot implicitly tag");
    }



    /**
     * Returns a new String object containing a text representing
     * of the Records.
     */

    public String
    toString() {
        int p;
        StringBuffer str = new StringBuffer("{");

        boolean found = false;

        if (c_responseRecords != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: responseRecords> ");
            }
            found = true;
            str.append("responseRecords ");
            str.append("{");
            for (p = 0; p < c_responseRecords.length; p++) {
                str.append(c_responseRecords[p]);
            }
            str.append("}");
        }

        if (c_nonSurrogateDiagnostic != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: nonSurrogateDiagnostic> ");
            }
            found = true;
            str.append("nonSurrogateDiagnostic ");
            str.append(c_nonSurrogateDiagnostic);
        }

        if (c_multipleNonSurDiagnostics != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: multipleNonSurDiagnostics> ");
            }
            found = true;
            str.append("multipleNonSurDiagnostics ");
            str.append("{");
            for (p = 0; p < c_multipleNonSurDiagnostics.length; p++) {
                str.append(c_multipleNonSurDiagnostics[p]);
            }
            str.append("}");
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public NamePlusRecord c_responseRecords[];
    public DefaultDiagFormat c_nonSurrogateDiagnostic;
    public DiagRec c_multipleNonSurDiagnostics[];

} // Records


//EOF
