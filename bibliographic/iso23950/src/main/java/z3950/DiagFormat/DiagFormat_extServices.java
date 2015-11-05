package z3950.DiagFormat;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Integer;
import asn1.BEREncoding;



/**
 * Class for representing a <code>DiagFormat_extServices</code> from <code>DiagnosticFormatDiag1</code>
 * <p/>
 * <pre>
 * DiagFormat_extServices ::=
 * CHOICE {
 *   req [1] IMPLICIT INTEGER
 *   permission [2] IMPLICIT INTEGER
 *   immediate [3] IMPLICIT INTEGER
 * }
 * </pre>
 *
 */

public final class DiagFormat_extServices extends ASN1Any {

    /**
     * Default constructor for a DiagFormat_extServices.
     */

    public DiagFormat_extServices() {
    }



    /**
     * Constructor for a DiagFormat_extServices from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public DiagFormat_extServices(BEREncoding ber, boolean check_tag)
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

        c_req = null;
        c_permission = null;
        c_immediate = null;

        // Try choice req
        if (ber.tag_get() == 1 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_req = new ASN1Integer(ber, false);
            return;
        }

        // Try choice permission
        if (ber.tag_get() == 2 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_permission = new ASN1Integer(ber, false);
            return;
        }

        // Try choice immediate
        if (ber.tag_get() == 3 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_immediate = new ASN1Integer(ber, false);
            return;
        }

        throw new ASN1Exception("DiagFormat_extServices: bad BER encoding: choice not matched");
    }



    /**
     * Returns a BER encoding of DiagFormat_extServices.
     *
     * @return The BER encoding.
     * @exception ASN1Exception Invalid or cannot be encoded.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        BEREncoding chosen = null;

        // Encoding choice: c_req
        if (c_req != null) {
            chosen = c_req.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);
        }

        // Encoding choice: c_permission
        if (c_permission != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_permission.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);
        }

        // Encoding choice: c_immediate
        if (c_immediate != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_immediate.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
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

        throw new ASN1EncodingException("Zebulun DiagFormat_extServices: cannot implicitly tag");
    }



    /**
     * Returns a new String object containing a text representing
     * of the DiagFormat_extServices.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");

        boolean found = false;

        if (c_req != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: req> ");
            }
            found = true;
            str.append("req ");
            str.append(c_req);
        }

        if (c_permission != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: permission> ");
            }
            found = true;
            str.append("permission ");
            str.append(c_permission);
        }

        if (c_immediate != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: immediate> ");
            }
            found = true;
            str.append("immediate ");
            str.append(c_immediate);
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public ASN1Integer c_req;
    public ASN1Integer c_permission;
    public ASN1Integer c_immediate;


/*
 * Enumerated constants for class.
 */

    // Enumerated constants for req
    public static final int E_nameInUse = 1;
    public static final int E_noSuchName = 2;
    public static final int E_quota = 3;
    public static final int E_type = 4;

    // Enumerated constants for permission
    public static final int E_id = 1;
    public static final int E_modifyDelete = 2;

    // Enumerated constants for immediate
    public static final int E_failed = 1;
    public static final int E_service = 2;
    public static final int E_parameters = 3;

} // DiagFormat_extServices


//EOF
