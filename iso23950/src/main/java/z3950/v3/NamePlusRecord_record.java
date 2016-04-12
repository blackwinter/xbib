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
import asn1.BERConstructed;
import asn1.BEREncoding;




/**
 * Class for representing a <code>NamePlusRecord_record</code> from <code>Z39-50-APDU-1995</code>
 * <p/>
 * <pre>
 * NamePlusRecord_record ::=
 * CHOICE {
 *   retrievalRecord [1] EXPLICIT EXTERNAL
 *   surrogateDiagnostic [2] EXPLICIT DiagRec
 *   startingFragment [3] EXPLICIT FragmentSyntax
 *   intermediateFragment [4] EXPLICIT FragmentSyntax
 *   finalFragment [5] EXPLICIT FragmentSyntax
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class NamePlusRecord_record extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a NamePlusRecord_record.
     */

    public NamePlusRecord_record() {
    }



    /**
     * Constructor for a NamePlusRecord_record from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public NamePlusRecord_record(BEREncoding ber, boolean check_tag)
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
        BERConstructed tagwrapper;

        // Null out all choices

        c_retrievalRecord = null;
        c_surrogateDiagnostic = null;
        c_startingFragment = null;
        c_intermediateFragment = null;
        c_finalFragment = null;

        // Try choice retrievalRecord
        if (ber.tag_get() == 1 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagwrapper = (BERConstructed) ber;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun NamePlusRecord_record: bad BER form\n");
            }
            if (tagwrapper.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun NamePlusRecord_record: bad BER form\n");
            }
            c_retrievalRecord = new ASN1External(tagwrapper.elementAt(0), true);
            return;
        }

        // Try choice surrogateDiagnostic
        if (ber.tag_get() == 2 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagwrapper = (BERConstructed) ber;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun NamePlusRecord_record: bad BER form\n");
            }
            if (tagwrapper.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun NamePlusRecord_record: bad BER form\n");
            }
            c_surrogateDiagnostic = new DiagRec(tagwrapper.elementAt(0), true);
            return;
        }

        // Try choice startingFragment
        if (ber.tag_get() == 3 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagwrapper = (BERConstructed) ber;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun NamePlusRecord_record: bad BER form\n");
            }
            if (tagwrapper.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun NamePlusRecord_record: bad BER form\n");
            }
            c_startingFragment = new FragmentSyntax(tagwrapper.elementAt(0), true);
            return;
        }

        // Try choice intermediateFragment
        if (ber.tag_get() == 4 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagwrapper = (BERConstructed) ber;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun NamePlusRecord_record: bad BER form\n");
            }
            if (tagwrapper.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun NamePlusRecord_record: bad BER form\n");
            }
            c_intermediateFragment = new FragmentSyntax(tagwrapper.elementAt(0), true);
            return;
        }

        // Try choice finalFragment
        if (ber.tag_get() == 5 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagwrapper = (BERConstructed) ber;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun NamePlusRecord_record: bad BER form\n");
            }
            if (tagwrapper.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun NamePlusRecord_record: bad BER form\n");
            }
            c_finalFragment = new FragmentSyntax(tagwrapper.elementAt(0), true);
            return;
        }

        throw new ASN1Exception("Zebulun NamePlusRecord_record: bad BER encoding: choice not matched");
    }



    /**
     * Returns a BER encoding of NamePlusRecord_record.
     *
     * @return The BER encoding.
     * @exception ASN1Exception Invalid or cannot be encoded.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        BEREncoding chosen = null;

        BEREncoding enc[];

        // Encoding choice: c_retrievalRecord
        if (c_retrievalRecord != null) {
            enc = new BEREncoding[1];
            enc[0] = c_retrievalRecord.ber_encode();
            chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 1, enc);
        }

        // Encoding choice: c_surrogateDiagnostic
        if (c_surrogateDiagnostic != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            enc = new BEREncoding[1];
            enc[0] = c_surrogateDiagnostic.ber_encode();
            chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 2, enc);
        }

        // Encoding choice: c_startingFragment
        if (c_startingFragment != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            enc = new BEREncoding[1];
            enc[0] = c_startingFragment.ber_encode();
            chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 3, enc);
        }

        // Encoding choice: c_intermediateFragment
        if (c_intermediateFragment != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            enc = new BEREncoding[1];
            enc[0] = c_intermediateFragment.ber_encode();
            chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 4, enc);
        }

        // Encoding choice: c_finalFragment
        if (c_finalFragment != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            enc = new BEREncoding[1];
            enc[0] = c_finalFragment.ber_encode();
            chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 5, enc);
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

        throw new ASN1EncodingException("Zebulun NamePlusRecord_record: cannot implicitly tag");
    }



    /**
     * Returns a new String object containing a text representing
     * of the NamePlusRecord_record.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");

        boolean found = false;

        if (c_retrievalRecord != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: retrievalRecord> ");
            }
            found = true;
            str.append("retrievalRecord ");
            str.append(c_retrievalRecord);
        }

        if (c_surrogateDiagnostic != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: surrogateDiagnostic> ");
            }
            found = true;
            str.append("surrogateDiagnostic ");
            str.append(c_surrogateDiagnostic);
        }

        if (c_startingFragment != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: startingFragment> ");
            }
            found = true;
            str.append("startingFragment ");
            str.append(c_startingFragment);
        }

        if (c_intermediateFragment != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: intermediateFragment> ");
            }
            found = true;
            str.append("intermediateFragment ");
            str.append(c_intermediateFragment);
        }

        if (c_finalFragment != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: finalFragment> ");
            }
            found = true;
            str.append("finalFragment ");
            str.append(c_finalFragment);
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public ASN1External c_retrievalRecord;
    public DiagRec c_surrogateDiagnostic;
    public FragmentSyntax c_startingFragment;
    public FragmentSyntax c_intermediateFragment;
    public FragmentSyntax c_finalFragment;

} // NamePlusRecord_record


//EOF
