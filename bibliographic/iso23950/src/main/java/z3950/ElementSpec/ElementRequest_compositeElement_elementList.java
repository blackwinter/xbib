package z3950.ElementSpec;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.InternationalString;

/**
 * Class for representing a <code>ElementRequest_compositeElement_elementList</code> from <code>ElementSpecificationFormat-eSpec-1</code>
 * <p/>
 * <pre>
 * ElementRequest_compositeElement_elementList ::=
 * CHOICE {
 *   primitives [1] IMPLICIT SEQUENCE OF InternationalString
 *   specs [2] IMPLICIT SEQUENCE OF SimpleElement
 * }
 * </pre>
 *
 */
public final class ElementRequest_compositeElement_elementList extends ASN1Any {

    /**
     * Default constructor for a ElementRequest_compositeElement_elementList.
     */

    public ElementRequest_compositeElement_elementList() {
    }



    /**
     * Constructor for a ElementRequest_compositeElement_elementList from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public ElementRequest_compositeElement_elementList(BEREncoding ber, boolean check_tag)
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

        c_primitives = null;
        c_specs = null;

        // Try choice primitives
        if (ber.tag_get() == 1 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            BEREncoding ber_data;
            ber_data = ber;
            BERConstructed ber_cons;
            try {
                ber_cons = (BERConstructed) ber_data;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("ElementRequest_compositeElement_elementList: bad BER form\n");
            }

            int num_parts = ber_cons.number_components();
            int p;

            c_primitives = new InternationalString[num_parts];

            for (p = 0; p < num_parts; p++) {
                c_primitives[p] = new InternationalString(ber_cons.elementAt(p), true);
            }
            return;
        }

        // Try choice specs
        if (ber.tag_get() == 2 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            BEREncoding ber_data;
            ber_data = ber;
            BERConstructed ber_cons;
            try {
                ber_cons = (BERConstructed) ber_data;
            } catch (ClassCastException e) {
                throw new ASN1EncodingException
                        ("ElementRequest_compositeElement_elementList: bad BER form\n");
            }

            int num_parts = ber_cons.number_components();
            int p;

            c_specs = new SimpleElement[num_parts];

            for (p = 0; p < num_parts; p++) {
                c_specs[p] = new SimpleElement(ber_cons.elementAt(p), true);
            }
            return;
        }

        throw new ASN1Exception("ElementRequest_compositeElement_elementList: bad BER encoding: choice not matched");
    }



    /**
     * Returns a BER encoding of ElementRequest_compositeElement_elementList.
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
        // Encoding choice: c_primitives
        if (c_primitives != null) {
            f2 = new BEREncoding[c_primitives.length];

            for (p = 0; p < c_primitives.length; p++) {
                f2[p] = c_primitives[p].ber_encode();
            }

            chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 1, f2);
        }

        // Encoding choice: c_specs
        if (c_specs != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            f2 = new BEREncoding[c_specs.length];

            for (p = 0; p < c_specs.length; p++) {
                f2[p] = c_specs[p].ber_encode();
            }

            chosen = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 2, f2);
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

        throw new ASN1EncodingException("ElementRequest_compositeElement_elementList: cannot implicitly tag");
    }



    /**
     * Returns a new String object containing a text representing
     * of the ElementRequest_compositeElement_elementList.
     */

    public String
    toString() {
        int p;
        StringBuilder str = new StringBuilder("{");

        boolean found = false;

        if (c_primitives != null) {
            found = true;
            str.append("primitives ");
            str.append("{");
            for (p = 0; p < c_primitives.length; p++) {
                str.append(c_primitives[p]);
            }
            str.append("}");
        }

        if (c_specs != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: specs> ");
            }
            found = true;
            str.append("specs ");
            str.append("{");
            for (p = 0; p < c_specs.length; p++) {
                str.append(c_specs[p]);
            }
            str.append("}");
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public InternationalString c_primitives[];
    public SimpleElement c_specs[];

}