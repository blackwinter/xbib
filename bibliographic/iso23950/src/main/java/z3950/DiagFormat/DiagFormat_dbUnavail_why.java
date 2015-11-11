package z3950.DiagFormat;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Integer;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.InternationalString;



/**
 * Class for representing a <code>DiagFormat_dbUnavail_why</code> from <code>DiagnosticFormatDiag1</code>
 * <p/>
 * <pre>
 * DiagFormat_dbUnavail_why ::=
 * SEQUENCE {
 *   reasonCode [1] IMPLICIT INTEGER OPTIONAL
 *   message [2] IMPLICIT InternationalString OPTIONAL
 * }
 * </pre>
 *
 */



public final class DiagFormat_dbUnavail_why extends ASN1Any {

    /**
     * Default constructor for a DiagFormat_dbUnavail_why.
     */

    public DiagFormat_dbUnavail_why() {
    }



    /**
     * Constructor for a DiagFormat_dbUnavail_why from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public DiagFormat_dbUnavail_why(BEREncoding ber, boolean check_tag)
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
        // DiagFormat_dbUnavail_why should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("Zebulun DiagFormat_dbUnavail_why: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_reasonCode = null;
        s_message = null;

        // Decoding: reasonCode [1] IMPLICIT INTEGER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 1 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_reasonCode = new ASN1Integer(p, false);
            part++;
        }

        // Decoding: message [2] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 2 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_message = new InternationalString(p, false);
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Zebulun DiagFormat_dbUnavail_why: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the DiagFormat_dbUnavail_why.
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
     * Returns a BER encoding of DiagFormat_dbUnavail_why, implicitly tagged.
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
        if (s_reasonCode != null) {
            num_fields++;
        }
        if (s_message != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;

        // Encoding s_reasonCode: INTEGER OPTIONAL

        if (s_reasonCode != null) {
            fields[x++] = s_reasonCode.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
        }

        // Encoding s_message: InternationalString OPTIONAL

        if (s_message != null) {
            fields[x++] = s_message.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the DiagFormat_dbUnavail_why.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        if (s_reasonCode != null) {
            str.append("reasonCode ");
            str.append(s_reasonCode);
            outputted++;
        }

        if (s_message != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("message ");
            str.append(s_message);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public ASN1Integer s_reasonCode; // optional
    public InternationalString s_message; // optional


/*
 * Enumerated constants for class.
 */

    // Enumerated constants for reasonCode
    public static final int E_doesNotExist = 0;
    public static final int E_existsButUnavail = 1;
    public static final int E_locked = 2;
    public static final int E_accessDenied = 3;

} // DiagFormat_dbUnavail_why


//EOF
