package z3950.ElementSpec;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Null;
import asn1.BEREncoding;



/**
 * Class for representing a <code>Occurrences</code> from <code>ElementSpecificationFormat-eSpec-1</code>
 * <p/>
 * <pre>
 * Occurrences ::=
 * CHOICE {
 *   all [1] IMPLICIT NULL
 *   last [2] IMPLICIT NULL
 *   values [3] IMPLICIT Occurrences_values
 * }
 * </pre>
 *
 */

public final class Occurrences extends ASN1Any {



    /**
     * Default constructor for a Occurrences.
     */

    public Occurrences() {
    }



    /**
     * Constructor for a Occurrences from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public Occurrences(BEREncoding ber, boolean check_tag)
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

        c_all = null;
        c_last = null;
        c_values = null;

        // Try choice all
        if (ber.tag_get() == 1 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_all = new ASN1Null(ber, false);
            return;
        }

        // Try choice last
        if (ber.tag_get() == 2 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_last = new ASN1Null(ber, false);
            return;
        }

        // Try choice values
        if (ber.tag_get() == 3 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_values = new Occurrences_values(ber, false);
            return;
        }

        throw new ASN1Exception("Zebulun Occurrences: bad BER encoding: choice not matched");
    }



    /**
     * Returns a BER encoding of Occurrences.
     *
     * @return The BER encoding.
     * @exception ASN1Exception Invalid or cannot be encoded.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        BEREncoding chosen = null;

        // Encoding choice: c_all
        if (c_all != null) {
            chosen = c_all.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
        }

        // Encoding choice: c_last
        if (c_last != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_last.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
        }

        // Encoding choice: c_values
        if (c_values != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_values.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
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

        throw new ASN1EncodingException("Zebulun Occurrences: cannot implicitly tag");
    }



    /**
     * Returns a new String object containing a text representing
     * of the Occurrences.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");

        boolean found = false;

        if (c_all != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: all> ");
            }
            found = true;
            str.append("all ");
            str.append(c_all);
        }

        if (c_last != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: last> ");
            }
            found = true;
            str.append("last ");
            str.append(c_last);
        }

        if (c_values != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: values> ");
            }
            found = true;
            str.append("values ");
            str.append(c_values);
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public ASN1Null c_all;
    public ASN1Null c_last;
    public Occurrences_values c_values;

} // Occurrences


//EOF
