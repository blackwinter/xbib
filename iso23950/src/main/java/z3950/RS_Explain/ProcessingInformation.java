package z3950.RS_Explain;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1External;
import asn1.ASN1Integer;
import asn1.ASN1ObjectIdentifier;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.DatabaseName;
import z3950.v3.InternationalString;

/**
 * Class for representing a <code>ProcessingInformation</code> from <code>RecordSyntax-explain</code>
 * <pre>
 * ProcessingInformation ::=
 * SEQUENCE {
 *   commonInfo [0] IMPLICIT CommonInfo OPTIONAL
 *   databaseName [1] IMPLICIT DatabaseName
 *   processingContext [2] IMPLICIT INTEGER
 *   name [3] IMPLICIT InternationalString
 *   oid [4] IMPLICIT OBJECT IDENTIFIER
 *   description [5] IMPLICIT HumanString OPTIONAL
 *   instructions [6] IMPLICIT EXTERNAL OPTIONAL
 * }
 * </pre>
 *
 */
public final class ProcessingInformation extends ASN1Any {
    /**
     * Default constructor for a ProcessingInformation.
     */

    public ProcessingInformation() {
    }



    /**
     * Constructor for a ProcessingInformation from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public ProcessingInformation(BEREncoding ber, boolean check_tag)
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
        // ProcessingInformation should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("ProcessingInformation: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Decoding: commonInfo [0] IMPLICIT CommonInfo OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ProcessingInformation: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 0 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_commonInfo = new CommonInfo(p, false);
            part++;
        }

        // Decoding: databaseName [1] IMPLICIT DatabaseName

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ProcessingInformation: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 1 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("ProcessingInformation: bad tag in s_databaseName\n");
        }

        s_databaseName = new DatabaseName(p, false);
        part++;

        // Decoding: processingContext [2] IMPLICIT INTEGER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ProcessingInformation: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 2 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("ProcessingInformation: bad tag in s_processingContext\n");
        }

        s_processingContext = new ASN1Integer(p, false);
        part++;

        // Decoding: name [3] IMPLICIT InternationalString

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ProcessingInformation: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 3 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("ProcessingInformation: bad tag in s_name\n");
        }

        s_name = new InternationalString(p, false);
        part++;

        // Decoding: oid [4] IMPLICIT OBJECT IDENTIFIER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ProcessingInformation: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 4 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("ProcessingInformation: bad tag in s_oid\n");
        }

        s_oid = new ASN1ObjectIdentifier(p, false);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_description = null;
        s_instructions = null;

        // Decoding: description [5] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 5 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_description = new HumanString(p, false);
            part++;
        }

        // Decoding: instructions [6] IMPLICIT EXTERNAL OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 6 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_instructions = new ASN1External(p, false);
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("ProcessingInformation: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the ProcessingInformation.
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
     * Returns a BER encoding of ProcessingInformation, implicitly tagged.
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

        int num_fields = 4; // number of mandatories
        if (s_commonInfo != null) {
            num_fields++;
        }
        if (s_description != null) {
            num_fields++;
        }
        if (s_instructions != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;

        // Encoding s_commonInfo: CommonInfo OPTIONAL

        if (s_commonInfo != null) {
            fields[x++] = s_commonInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 0);
        }

        // Encoding s_databaseName: DatabaseName

        fields[x++] = s_databaseName.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);

        // Encoding s_processingContext: INTEGER

        fields[x++] = s_processingContext.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);

        // Encoding s_name: InternationalString

        fields[x++] = s_name.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);

        // Encoding s_oid: OBJECT IDENTIFIER

        fields[x++] = s_oid.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);

        // Encoding s_description: HumanString OPTIONAL

        if (s_description != null) {
            fields[x++] = s_description.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 5);
        }

        // Encoding s_instructions: EXTERNAL OPTIONAL

        if (s_instructions != null) {
            fields[x++] = s_instructions.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 6);
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the ProcessingInformation.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        if (s_commonInfo != null) {
            str.append("commonInfo ");
            str.append(s_commonInfo);
            outputted++;
        }

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("databaseName ");
        str.append(s_databaseName);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("processingContext ");
        str.append(s_processingContext);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("name ");
        str.append(s_name);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("oid ");
        str.append(s_oid);
        outputted++;

        if (s_description != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("description ");
            str.append(s_description);
            outputted++;
        }

        if (s_instructions != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("instructions ");
            str.append(s_instructions);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public CommonInfo s_commonInfo; // optional
    public DatabaseName s_databaseName;
    public ASN1Integer s_processingContext;
    public InternationalString s_name;
    public ASN1ObjectIdentifier s_oid;
    public HumanString s_description; // optional
    public ASN1External s_instructions; // optional


/*
 * Enumerated constants for class.
 */

    // Enumerated constants for processingContext
    public static final int E_access = 0;
    public static final int E_search = 1;
    public static final int E_retrieval = 2;
    public static final int E_record_presentation = 3;
    public static final int E_record_handling = 4;

}