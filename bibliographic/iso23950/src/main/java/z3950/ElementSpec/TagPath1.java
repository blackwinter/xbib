package z3950.ElementSpec;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Null;
import asn1.BERConstructed;
import asn1.BEREncoding;



/**
 * Class for representing a <code>TagPath1</code> from <code>ElementSpecificationFormat-eSpec-1</code>
 * <p/>
 * <pre>
 * TagPath1 ::=
 * CHOICE {
 *   specificTag [1] IMPLICIT TagPath_specificTag
 *   wildThing [2] EXPLICIT Occurrences
 *   wildPath [3] IMPLICIT NULL
 * }
 * </pre>
 *
 */



public final class TagPath1 extends ASN1Any {

    /**
     * Default constructor for a TagPath1.
     */

    public TagPath1() {
    }



    /**
     * Constructor for a TagPath1 from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public TagPath1(BEREncoding ber, boolean check_tag)
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

        c_specificTag = null;
        c_wildThing = null;
        c_wildPath = null;

        // Try choice specificTag
        if (ber.tag_get() == 1 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_specificTag = new TagPath_specificTag(ber, false);
            return;
        }

        // Try choice wildThing
        if (ber.tag_get() == 2 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                tagwrapper = (BERConstructed) ber;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("TagPath1: bad BER form\n");
            }
            if (tagwrapper.number_components() != 1) {
                throw new ASN1EncodingException
                        ("TagPath1: bad BER form\n");
            }
            c_wildThing = new Occurrences(tagwrapper.elementAt(0), true);
            return;
        }

        // Try choice wildPath
        if (ber.tag_get() == 3 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_wildPath = new ASN1Null(ber, false);
            return;
        }

        throw new ASN1Exception("TagPath1: bad BER encoding: choice not matched");
    }



    /**
     * Returns a BER encoding of TagPath1.
     *
     * @return The BER encoding.
     * @exception ASN1Exception Invalid or cannot be encoded.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        BEREncoding chosen = null;

        BEREncoding enc[];

        // Encoding choice: c_specificTag
        if (c_specificTag != null) {
            chosen = c_specificTag.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
        }

        // Encoding choice: c_wildThing
        if (c_wildThing != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            enc = new BEREncoding[1];
            enc[0] = c_wildThing.ber_encode();
            chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 2, enc);
        }

        // Encoding choice: c_wildPath
        if (c_wildPath != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_wildPath.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
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

        throw new ASN1EncodingException("TagPath1: cannot implicitly tag");
    }



    /**
     * Returns a new String object containing a text representing
     * of the TagPath1.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");

        boolean found = false;

        if (c_specificTag != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: specificTag> ");
            }
            found = true;
            str.append("specificTag ");
            str.append(c_specificTag);
        }

        if (c_wildThing != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: wildThing> ");
            }
            found = true;
            str.append("wildThing ");
            str.append(c_wildThing);
        }

        if (c_wildPath != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: wildPath> ");
            }
            found = true;
            str.append("wildPath ");
            str.append(c_wildPath);
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public TagPath_specificTag c_specificTag;
    public Occurrences c_wildThing;
    public ASN1Null c_wildPath;

}