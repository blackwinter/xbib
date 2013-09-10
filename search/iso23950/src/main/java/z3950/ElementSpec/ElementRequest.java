/*
 * $Source$
 * $Date$
 * $Revision$
 *
 * Copyright (C) 1998, Hoylen Sue.  All Rights Reserved.
 * <h.sue@ieee.org>
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  Refer to
 * the supplied license for more details.
 *
 * Generated by Zebulun ASN1tojava: 1998-09-08 03:15:30 UTC
 */



package z3950.ElementSpec;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.BEREncoding;



/**
 * Class for representing a <code>ElementRequest</code> from <code>ElementSpecificationFormat-eSpec-1</code>
 * <p/>
 * <pre>
 * ElementRequest ::=
 * CHOICE {
 *   simpleElement [1] IMPLICIT SimpleElement
 *   compositeElement [2] IMPLICIT ElementRequest_compositeElement
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class ElementRequest extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a ElementRequest.
     */

    public ElementRequest() {
    }



    /**
     * Constructor for a ElementRequest from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public ElementRequest(BEREncoding ber, boolean check_tag)
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

        c_simpleElement = null;
        c_compositeElement = null;

        // Try choice simpleElement
        if (ber.tag_get() == 1 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_simpleElement = new SimpleElement(ber, false);
            return;
        }

        // Try choice compositeElement
        if (ber.tag_get() == 2 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_compositeElement = new ElementRequest_compositeElement(ber, false);
            return;
        }

        throw new ASN1Exception("Zebulun ElementRequest: bad BER encoding: choice not matched");
    }



    /**
     * Returns a BER encoding of ElementRequest.
     *
     * @return The BER encoding.
     * @exception ASN1Exception Invalid or cannot be encoded.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        BEREncoding chosen = null;

        // Encoding choice: c_simpleElement
        if (c_simpleElement != null) {
            chosen = c_simpleElement.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
        }

        // Encoding choice: c_compositeElement
        if (c_compositeElement != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_compositeElement.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
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

        throw new ASN1EncodingException("Zebulun ElementRequest: cannot implicitly tag");
    }



    /**
     * Returns a new String object containing a text representing
     * of the ElementRequest.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");

        boolean found = false;

        if (c_simpleElement != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: simpleElement> ");
            }
            found = true;
            str.append("simpleElement ");
            str.append(c_simpleElement);
        }

        if (c_compositeElement != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: compositeElement> ");
            }
            found = true;
            str.append("compositeElement ");
            str.append(c_compositeElement);
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public SimpleElement c_simpleElement;
    public ElementRequest_compositeElement c_compositeElement;

} // ElementRequest


//EOF
