package z3950.RS_Explain;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;

/**
 * Class for representing a <code>PrivateCapabilities</code> from <code>RecordSyntax-explain</code>
 *
 * <pre>
 * PrivateCapabilities ::=
 * SEQUENCE {
 *   operators [0] IMPLICIT SEQUENCE OF PrivateCapabilities_operators OPTIONAL
 *   searchKeys [1] IMPLICIT SEQUENCE OF SearchKey OPTIONAL
 *   description [2] IMPLICIT SEQUENCE OF HumanString OPTIONAL
 * }
 * </pre>
 *
 */
public final class PrivateCapabilities extends ASN1Any {
    /**
     * Default constructor for a PrivateCapabilities.
     */

    public PrivateCapabilities() {
    }



    /**
     * Constructor for a PrivateCapabilities from a BER encoding.
     *
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public PrivateCapabilities(BEREncoding ber, boolean check_tag)
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
        // PrivateCapabilities should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("PrivateCapabilities: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_operators = null;
        s_searchKeys = null;
        s_description = null;

        // Decoding: operators [0] IMPLICIT SEQUENCE OF PrivateCapabilities_operators OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 0 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_operators = new PrivateCapabilities_operators[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_operators[n] = new PrivateCapabilities_operators(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Decoding: searchKeys [1] IMPLICIT SEQUENCE OF SearchKey OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 1 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_searchKeys = new SearchKey[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_searchKeys[n] = new SearchKey(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Decoding: description [2] IMPLICIT SEQUENCE OF HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_description = new HumanString[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_description[n] = new HumanString(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("PrivateCapabilities: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the PrivateCapabilities.
     *
     * @exception ASN1Exception Invalid or cannot be encoded.
     * @return The BER encoding.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        return ber_encode(BEREncoding.UNIVERSAL_TAG, ASN1Sequence.TAG);
    }



    /**
     * Returns a BER encoding of PrivateCapabilities, implicitly tagged.
     *
     * @param tag_type The type of the implicit tag.
     * @param tag      The implicit tag.
     * @return The BER encoding of the object.
     * @exception ASN1Exception When invalid or cannot be encoded.
     * @see asn1.BEREncoding#UNIVERSAL_TAG
     * @see asn1.BEREncoding#APPLICATION_TAG
     * @see asn1.BEREncoding#CONTEXT_SPECIFIC_TAG
     * @see asn1.BEREncoding#PRIVATE_TAG
     */

    public BEREncoding
    ber_encode(int tag_type, int tag)
            throws ASN1Exception {
        // Calculate the number of fields in the encoding

        int num_fields = 0; // number of mandatories
        if (s_operators != null) {
            num_fields++;
        }
        if (s_searchKeys != null) {
            num_fields++;
        }
        if (s_description != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding f2[];
        int p;

        // Encoding s_operators: SEQUENCE OF OPTIONAL

        if (s_operators != null) {
            f2 = new BEREncoding[s_operators.length];

            for (p = 0; p < s_operators.length; p++) {
                f2[p] = s_operators[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 0, f2);
        }

        // Encoding s_searchKeys: SEQUENCE OF OPTIONAL

        if (s_searchKeys != null) {
            f2 = new BEREncoding[s_searchKeys.length];

            for (p = 0; p < s_searchKeys.length; p++) {
                f2[p] = s_searchKeys[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 1, f2);
        }

        // Encoding s_description: SEQUENCE OF OPTIONAL

        if (s_description != null) {
            f2 = new BEREncoding[s_description.length];

            for (p = 0; p < s_description.length; p++) {
                f2[p] = s_description[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 2, f2);
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the PrivateCapabilities.
     */

    public String
    toString() {
        int p;
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        if (s_operators != null) {
            str.append("operators ");
            str.append("{");
            for (p = 0; p < s_operators.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_operators[p]);
            }
            str.append("}");
            outputted++;
        }

        if (s_searchKeys != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("searchKeys ");
            str.append("{");
            for (p = 0; p < s_searchKeys.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_searchKeys[p]);
            }
            str.append("}");
            outputted++;
        }

        if (s_description != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("description ");
            str.append("{");
            for (p = 0; p < s_description.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_description[p]);
            }
            str.append("}");
            outputted++;
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public PrivateCapabilities_operators s_operators[]; // optional
    public SearchKey s_searchKeys[]; // optional
    public HumanString s_description[]; // optional

}