package z3950.ElementSpec;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;
import z3950.RS_generic.Variant;



/**
 * Class for representing a <code>ElementRequest_compositeElement</code> from <code>ElementSpecificationFormat-eSpec-1</code>
 * <p/>
 * <pre>
 * ElementRequest_compositeElement ::=
 * SEQUENCE {
 *   elementList [1] EXPLICIT ElementRequest_compositeElement_elementList
 *   deliveryTag [2] IMPLICIT TagPath
 *   variantRequest [3] IMPLICIT Variant OPTIONAL
 * }
 * </pre>
 *
 */



public final class ElementRequest_compositeElement extends ASN1Any {

    /**
     * Default constructor for a ElementRequest_compositeElement.
     */

    public ElementRequest_compositeElement() {
    }



    /**
     * Constructor for a ElementRequest_compositeElement from a BER encoding.
     *
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public ElementRequest_compositeElement(BEREncoding ber, boolean check_tag)
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
        // ElementRequest_compositeElement should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("ElementRequest_compositeElement: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;
        BERConstructed tagged;

        // Decoding: elementList [1] EXPLICIT ElementRequest_compositeElement_elementList

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ElementRequest_compositeElement: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 1 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("ElementRequest_compositeElement: bad tag in s_elementList\n");
        }

        try {
            tagged = (BERConstructed) p;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("ElementRequest_compositeElement: bad BER encoding: s_elementList tag bad\n");
        }
        if (tagged.number_components() != 1) {
            throw new ASN1EncodingException
                    ("ElementRequest_compositeElement: bad BER encoding: s_elementList tag bad\n");
        }

        s_elementList = new ElementRequest_compositeElement_elementList(tagged.elementAt(0), true);
        part++;

        // Decoding: deliveryTag [2] IMPLICIT TagPath

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ElementRequest_compositeElement: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 2 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("ElementRequest_compositeElement: bad tag in s_deliveryTag\n");
        }

        s_deliveryTag = new TagPath(p, false);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_variantRequest = null;

        // Decoding: variantRequest [3] IMPLICIT Variant OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 3 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_variantRequest = new Variant(p, false);
            part++;
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("ElementRequest_compositeElement: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }



    /**
     * Returns a BER encoding of the ElementRequest_compositeElement.
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
     * Returns a BER encoding of ElementRequest_compositeElement, implicitly tagged.
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
        if (s_variantRequest != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;
        BEREncoding enc[];

        // Encoding s_elementList: ElementRequest_compositeElement_elementList

        enc = new BEREncoding[1];
        enc[0] = s_elementList.ber_encode();
        fields[x++] = new BERConstructed(BEREncoding.CONTEXT_SPECIFIC_TAG, 1, enc);

        // Encoding s_deliveryTag: TagPath

        fields[x++] = s_deliveryTag.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 2);

        // Encoding s_variantRequest: Variant OPTIONAL

        if (s_variantRequest != null) {
            fields[x++] = s_variantRequest.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);
        }

        return new BERConstructed(tag_type, tag, fields);
    }



    /**
     * Returns a new String object containing a text representing
     * of the ElementRequest_compositeElement.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        str.append("elementList ");
        str.append(s_elementList);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("deliveryTag ");
        str.append(s_deliveryTag);
        outputted++;

        if (s_variantRequest != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("variantRequest ");
            str.append(s_variantRequest);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public ElementRequest_compositeElement_elementList s_elementList;
    public TagPath s_deliveryTag;
    public Variant s_variantRequest; // optional

}