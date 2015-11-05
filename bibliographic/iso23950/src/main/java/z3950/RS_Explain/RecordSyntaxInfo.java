package z3950.RS_Explain;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1ObjectIdentifier;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.v3.InternationalString;

/**
 * Class for representing a <code>RecordSyntaxInfo</code> from <code>RecordSyntax-explain</code>
 *
 * <pre>
 * RecordSyntaxInfo ::=
 * SEQUENCE {
 *   commonInfo [0] IMPLICIT CommonInfo OPTIONAL
 *   recordSyntax [1] IMPLICIT OBJECT IDENTIFIER
 *   name [2] IMPLICIT InternationalString
 *   transferSyntaxes [3] IMPLICIT SEQUENCE OF OBJECT IDENTIFIER OPTIONAL
 *   description [4] IMPLICIT HumanString OPTIONAL
 *   asn1Module [5] IMPLICIT InternationalString OPTIONAL
 *   abstractStructure [6] IMPLICIT SEQUENCE OF ElementInfo OPTIONAL
 * }
 * </pre>
 *
 */
public final class RecordSyntaxInfo extends ASN1Any {
    /**
     * Default constructor for a RecordSyntaxInfo.
     */

    public RecordSyntaxInfo() {
    }



    /**
     * Constructor for a RecordSyntaxInfo from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public RecordSyntaxInfo(BEREncoding ber, boolean check_tag)
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
        // RecordSyntaxInfo should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("RecordSyntaxInfo: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Decoding: commonInfo [0] IMPLICIT CommonInfo OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("RecordSyntaxInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 0 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_commonInfo = new CommonInfo(p, false);
            part++;
        }

        // Decoding: recordSyntax [1] IMPLICIT OBJECT IDENTIFIER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("RecordSyntaxInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 1 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("RecordSyntaxInfo: bad tag in s_recordSyntax\n");
        }

        s_recordSyntax = new ASN1ObjectIdentifier(p, false);
        part++;

        // Decoding: name [2] IMPLICIT InternationalString

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("RecordSyntaxInfo: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 2 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("RecordSyntaxInfo: bad tag in s_name\n");
        }

        s_name = new InternationalString(p, false);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_transferSyntaxes = null;
        s_description = null;
        s_asn1Module = null;
        s_abstractStructure = null;

        // Decoding: transferSyntaxes [3] IMPLICIT SEQUENCE OF OBJECT IDENTIFIER OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 3 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_transferSyntaxes = new ASN1ObjectIdentifier[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_transferSyntaxes[n] = new ASN1ObjectIdentifier(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Decoding: description [4] IMPLICIT HumanString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 4 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_description = new HumanString(p, false);
            part++;
        }

        // Decoding: asn1Module [5] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 5 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_asn1Module = new InternationalString(p, false);
            part++;
        }

        // Decoding: abstractStructure [6] IMPLICIT SEQUENCE OF ElementInfo OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 6 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            try {
                BERConstructed cons = (BERConstructed) p;
                int parts = cons.number_components();
                s_abstractStructure = new ElementInfo[parts];
                int n;
                for (n = 0; n < parts; n++) {
                    s_abstractStructure[n] = new ElementInfo(cons.elementAt(n), true);
                }
            } catch (ClassCastException e) {
                throw new ASN1EncodingException("Bad BER");
            }
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("RecordSyntaxInfo: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the RecordSyntaxInfo.
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
     * Returns a BER encoding of RecordSyntaxInfo, implicitly tagged.
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
        if (s_commonInfo != null) {
            num_fields++;
        }
        if (s_transferSyntaxes != null) {
            num_fields++;
        }
        if (s_description != null) {
            num_fields++;
        }
        if (s_asn1Module != null) {
            num_fields++;
        }
        if (s_abstractStructure != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding f2[];
        int p;

        // Encoding s_commonInfo: CommonInfo OPTIONAL

        if (s_commonInfo != null) {
            fields[x++] = s_commonInfo.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 0);
        }

        // Encoding s_recordSyntax: OBJECT IDENTIFIER

        fields[x++] = s_recordSyntax.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 1);

        // Encoding s_name: InternationalString

        fields[x++] = s_name.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);

        // Encoding s_transferSyntaxes: SEQUENCE OF OPTIONAL

        if (s_transferSyntaxes != null) {
            f2 = new BEREncoding[s_transferSyntaxes.length];

            for (p = 0; p < s_transferSyntaxes.length; p++) {
                f2[p] = s_transferSyntaxes[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 3, f2);
        }

        // Encoding s_description: HumanString OPTIONAL

        if (s_description != null) {
            fields[x++] = s_description.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);
        }

        // Encoding s_asn1Module: InternationalString OPTIONAL

        if (s_asn1Module != null) {
            fields[x++] = s_asn1Module.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 5);
        }

        // Encoding s_abstractStructure: SEQUENCE OF OPTIONAL

        if (s_abstractStructure != null) {
            f2 = new BEREncoding[s_abstractStructure.length];

            for (p = 0; p < s_abstractStructure.length; p++) {
                f2[p] = s_abstractStructure[p].ber_encode();
            }

            fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 6, f2);
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the RecordSyntaxInfo.
     */

    public String
    toString() {
        int p;
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
        str.append("recordSyntax ");
        str.append(s_recordSyntax);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("name ");
        str.append(s_name);
        outputted++;

        if (s_transferSyntaxes != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("transferSyntaxes ");
            str.append("{");
            for (p = 0; p < s_transferSyntaxes.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_transferSyntaxes[p]);
            }
            str.append("}");
            outputted++;
        }

        if (s_description != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("description ");
            str.append(s_description);
            outputted++;
        }

        if (s_asn1Module != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("asn1Module ");
            str.append(s_asn1Module);
            outputted++;
        }

        if (s_abstractStructure != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("abstractStructure ");
            str.append("{");
            for (p = 0; p < s_abstractStructure.length; p++) {
                if (p != 0) {
                    str.append(", ");
                }
                str.append(s_abstractStructure[p]);
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

    public CommonInfo s_commonInfo; // optional
    public ASN1ObjectIdentifier s_recordSyntax;
    public InternationalString s_name;
    public ASN1ObjectIdentifier s_transferSyntaxes[]; // optional
    public HumanString s_description; // optional
    public InternationalString s_asn1Module; // optional
    public ElementInfo s_abstractStructure[]; // optional

}