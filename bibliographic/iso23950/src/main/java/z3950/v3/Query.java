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
import asn1.ASN1OctetString;
import asn1.BERConstructed;
import asn1.BEREncoding;



/**
 * Class for representing a <code>Query</code> from <code>Z39-50-APDU-1995</code>
 * <p/>
 * <pre>
 * Query ::=
 * CHOICE {
 *   type-0 [0] EXPLICIT ANY
 *   type-1 [1] IMPLICIT RPNQuery
 *   type-2 [2] EXPLICIT OCTET STRING
 *   type-100 [100] EXPLICIT OCTET STRING
 *   type-101 [101] IMPLICIT RPNQuery
 *   type-102 [102] EXPLICIT OCTET STRING
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class Query extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a Query.
     */

    public Query() {
    }



    /**
     * Constructor for a Query from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public Query(BEREncoding ber, boolean check_tag)
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

        c_type_0 = null;
        c_type_1 = null;
        c_type_2 = null;
        c_type_100 = null;
        c_type_101 = null;
        c_type_102 = null;

        // Try choice type-0
        if (ber.tag_get() == 0 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagwrapper = (BERConstructed) ber;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun Query: bad BER form\n");
            }
            if (tagwrapper.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun Query: bad BER form\n");
            }
            c_type_0 = new ASN1Any(tagwrapper.elementAt(0), true);
            return;
        }

        // Try choice type-1
        if (ber.tag_get() == 1 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_type_1 = new RPNQuery(ber, false);
            return;
        }

        // Try choice type-2
        if (ber.tag_get() == 2 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagwrapper = (BERConstructed) ber;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun Query: bad BER form\n");
            }
            if (tagwrapper.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun Query: bad BER form\n");
            }
            c_type_2 = new ASN1OctetString(tagwrapper.elementAt(0), true);
            return;
        }

        // Try choice type-100
        if (ber.tag_get() == 100 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagwrapper = (BERConstructed) ber;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun Query: bad BER form\n");
            }
            if (tagwrapper.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun Query: bad BER form\n");
            }
            c_type_100 = new ASN1OctetString(tagwrapper.elementAt(0), true);
            return;
        }

        // Try choice type-101
        if (ber.tag_get() == 101 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_type_101 = new RPNQuery(ber, false);
            return;
        }

        // Try choice type-102
        if (ber.tag_get() == 102 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagwrapper = (BERConstructed) ber;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("Zebulun Query: bad BER form\n");
            }
            if (tagwrapper.number_components() != 1) {
                throw new ASN1EncodingException
                        ("Zebulun Query: bad BER form\n");
            }
            c_type_102 = new ASN1OctetString(tagwrapper.elementAt(0), true);
            return;
        }

        throw new ASN1Exception("Zebulun Query: bad BER encoding: choice not matched");
    }



    /**
     * Returns a BER encoding of Query.
     *
     * @return The BER encoding.
     * @exception ASN1Exception Invalid or cannot be encoded.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        BEREncoding chosen = null;

        BEREncoding enc[];

        // Encoding choice: c_type_0
        if (c_type_0 != null) {
            enc = new BEREncoding[1];
            enc[0] = c_type_0.ber_encode();
            chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 0, enc);
        }

        // Encoding choice: c_type_1
        if (c_type_1 != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_type_1.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
        }

        // Encoding choice: c_type_2
        if (c_type_2 != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            enc = new BEREncoding[1];
            enc[0] = c_type_2.ber_encode();
            chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 2, enc);
        }

        // Encoding choice: c_type_100
        if (c_type_100 != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            enc = new BEREncoding[1];
            enc[0] = c_type_100.ber_encode();
            chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 100, enc);
        }

        // Encoding choice: c_type_101
        if (c_type_101 != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_type_101.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 101);
        }

        // Encoding choice: c_type_102
        if (c_type_102 != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            enc = new BEREncoding[1];
            enc[0] = c_type_102.ber_encode();
            chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 102, enc);
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

        throw new ASN1EncodingException("Zebulun Query: cannot implicitly tag");
    }



    /**
     * Returns a new String object containing a text representing
     * of the Query.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");

        boolean found = false;

        if (c_type_0 != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: type-0> ");
            }
            found = true;
            str.append("type-0 ");
            str.append(c_type_0);
        }

        if (c_type_1 != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: type-1> ");
            }
            found = true;
            str.append("type-1 ");
            str.append(c_type_1);
        }

        if (c_type_2 != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: type-2> ");
            }
            found = true;
            str.append("type-2 ");
            str.append(c_type_2);
        }

        if (c_type_100 != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: type-100> ");
            }
            found = true;
            str.append("type-100 ");
            str.append(c_type_100);
        }

        if (c_type_101 != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: type-101> ");
            }
            found = true;
            str.append("type-101 ");
            str.append(c_type_101);
        }

        if (c_type_102 != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: type-102> ");
            }
            found = true;
            str.append("type-102 ");
            str.append(c_type_102);
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public ASN1Any c_type_0;
    public RPNQuery c_type_1;
    public ASN1OctetString c_type_2;
    public ASN1OctetString c_type_100;
    public RPNQuery c_type_101;
    public ASN1OctetString c_type_102;

} // Query


//EOF
