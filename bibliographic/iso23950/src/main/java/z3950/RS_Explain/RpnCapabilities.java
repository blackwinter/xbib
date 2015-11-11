package z3950.RS_Explain;

import asn1.ASN1Any;
import asn1.ASN1Boolean;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Integer;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;

/**
 * Class for representing a <code>RpnCapabilities</code> from <code>RecordSyntax-explain</code>
 * <p/>
 * <pre>
 * RpnCapabilities ::=
 * SEQUENCE {
 *   operators [0] IMPLICIT SEQUENCE OF INTEGER OPTIONAL
 *   resultSetAsOperandSupported [1] IMPLICIT BOOLEAN
 *   restrictionOperandSupported [2] IMPLICIT BOOLEAN
 *   proximity [3] IMPLICIT ProximitySupport OPTIONAL
 * }
 * </pre>
 */
public final class RpnCapabilities extends ASN1Any {
    /**
     * Default constructor for a RpnCapabilities.
     */

    public RpnCapabilities() {
    }

    /**
     * Constructor for a RpnCapabilities from a BER encoding.
     *
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public RpnCapabilities(BEREncoding ber, boolean check_tag)
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
        // RpnCapabilities should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("RpnCapabilities: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Decoding: operators [0] IMPLICIT SEQUENCE OF INTEGER OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("RpnCapabilities: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 0 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_operators = new ASN1Integer[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_operators[n] = new ASN1Integer(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Decoding: resultSetAsOperandSupported [1] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("RpnCapabilities: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 1 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("RpnCapabilities: bad tag in s_resultSetAsOperandSupported\n");
        }

        s_resultSetAsOperandSupported = new ASN1Boolean(p, false);
        part++;

        // Decoding: restrictionOperandSupported [2] IMPLICIT BOOLEAN

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("RpnCapabilities: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 2 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("RpnCapabilities: bad tag in s_restrictionOperandSupported\n");
        }

        s_restrictionOperandSupported = new ASN1Boolean(p, false);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_proximity = null;

        // Decoding: proximity [3] IMPLICIT ProximitySupport OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 3 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_proximity = new ProximitySupport(p, false);
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("RpnCapabilities: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the RpnCapabilities.
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
     * Returns a BER encoding of RpnCapabilities, implicitly tagged.
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

        int num_fields = 2; // number of mandatories
        if (s_operators != null) {
            num_fields++;
        }
        if (s_proximity != null) {
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

        // Encoding s_resultSetAsOperandSupported: BOOLEAN

        fields[x++] = s_resultSetAsOperandSupported.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);

        // Encoding s_restrictionOperandSupported: BOOLEAN

        fields[x++] = s_restrictionOperandSupported.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);

        // Encoding s_proximity: ProximitySupport OPTIONAL

        if (s_proximity != null) {
            fields[x++] = s_proximity.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the RpnCapabilities.
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

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("resultSetAsOperandSupported ");
        str.append(s_resultSetAsOperandSupported);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("restrictionOperandSupported ");
        str.append(s_restrictionOperandSupported);
        outputted++;

        if (s_proximity != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("proximity ");
            str.append(s_proximity);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public ASN1Integer s_operators[]; // optional
    public ASN1Boolean s_resultSetAsOperandSupported;
    public ASN1Boolean s_restrictionOperandSupported;
    public ProximitySupport s_proximity; // optional

} // RpnCapabilities


//EOF
