/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package z3950.v3;

import asn1.ASN1Any;
import asn1.ASN1EncodingException;
import asn1.ASN1Exception;
import asn1.ASN1External;
import asn1.ASN1Integer;
import asn1.ASN1ObjectIdentifier;
import asn1.ASN1Sequence;
import asn1.BERConstructed;
import asn1.BEREncoding;

/**
 * Class for representing a <code>ExtendedServicesRequest</code> from <code>Z39-50-APDU-1995</code>
 *
 * <pre>
 * ExtendedServicesRequest ::=
 * SEQUENCE {
 *   referenceId ReferenceId OPTIONAL
 *   function [3] IMPLICIT INTEGER
 *   packageType [4] IMPLICIT OBJECT IDENTIFIER
 *   packageName [5] IMPLICIT InternationalString OPTIONAL
 *   userId [6] IMPLICIT InternationalString OPTIONAL
 *   retentionTime [7] IMPLICIT IntUnit OPTIONAL
 *   permissions [8] IMPLICIT Permissions OPTIONAL
 *   description [9] IMPLICIT InternationalString OPTIONAL
 *   taskSpecificParameters [10] IMPLICIT EXTERNAL OPTIONAL
 *   waitAction [11] IMPLICIT INTEGER
 *   elements ElementSetName OPTIONAL
 *   otherInfo OtherInformation OPTIONAL
 * }
 * </pre>
 *
 */

public final class ExtendedServicesRequest extends ASN1Any {

    /**
     * Default constructor for a ExtendedServicesRequest.
     */

    public ExtendedServicesRequest() {
    }

    /**
     * Constructor for a ExtendedServicesRequest from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public ExtendedServicesRequest(BEREncoding ber, boolean check_tag)
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
        // ExtendedServicesRequest should be encoded by a constructed BER

        BERConstructed ber_cons;
        try {
            ber_cons = (BERConstructed) ber;
        } catch (ClassCastException e) {
            throw new ASN1EncodingException
                    ("ExtendedServicesRequest: bad BER form\n");
        }

        // Prepare to decode the components

        int num_parts = ber_cons.number_components();
        int part = 0;
        BEREncoding p;

        // Decoding: referenceId ReferenceId OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ExtendedServicesRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        try {
            s_referenceId = new ReferenceId(p, true);
            part++; // yes, consumed
        } catch (ASN1Exception e) {
            s_referenceId = null; // no, not present
        }

        // Decoding: function [3] IMPLICIT INTEGER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun ExtendedServicesRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 3 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun ExtendedServicesRequest: bad tag in s_function\n");
        }

        s_function = new ASN1Integer(p, false);
        part++;

        // Decoding: packageType [4] IMPLICIT OBJECT IDENTIFIER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("Zebulun ExtendedServicesRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 4 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("Zebulun ExtendedServicesRequest: bad tag in s_packageType\n");
        }

        s_packageType = new ASN1ObjectIdentifier(p, false);
        part++;

        // Decoding: packageName [5] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ExtendedServicesRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 5 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_packageName = new InternationalString(p, false);
            part++;
        }

        // Decoding: userId [6] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ExtendedServicesRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 6 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_userId = new InternationalString(p, false);
            part++;
        }

        // Decoding: retentionTime [7] IMPLICIT IntUnit OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ExtendedServicesRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 7 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_retentionTime = new IntUnit(p, false);
            part++;
        }

        // Decoding: permissions [8] IMPLICIT Permissions OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ExtendedServicesRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 8 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_permissions = new Permissions(p, false);
            part++;
        }

        // Decoding: description [9] IMPLICIT InternationalString OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ExtendedServicesRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 9 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_description = new InternationalString(p, false);
            part++;
        }

        // Decoding: taskSpecificParameters [10] IMPLICIT EXTERNAL OPTIONAL

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ExtendedServicesRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() == 10 &&
                p.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            s_taskSpecificParameters = new ASN1External(p, false);
            part++;
        }

        // Decoding: waitAction [11] IMPLICIT INTEGER

        if (num_parts <= part) {
            // End of record, but still more elements to get
            throw new ASN1Exception("ExtendedServicesRequest: incomplete");
        }
        p = ber_cons.elementAt(part);

        if (p.tag_get() != 11 ||
                p.tag_type_get() != BEREncoding.CONTEXT_SPECIFIC_TAG) {
            throw new ASN1EncodingException
                    ("ExtendedServicesRequest: bad tag in s_waitAction\n");
        }

        s_waitAction = new ASN1Integer(p, false);
        part++;

        // Remaining elements are optional, set variables
        // to null (not present) so can return at end of BER

        s_elements = null;
        s_otherInfo = null;

        // Decoding: elements ElementSetName OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        try {
            s_elements = new ElementSetName(p, true);
            part++; // yes, consumed
        } catch (ASN1Exception e) {
            s_elements = null; // no, not present
        }

        // Decoding: otherInfo OtherInformation OPTIONAL

        if (num_parts <= part) {
            return; // no more data, but ok (rest is optional)
        }
        p = ber_cons.elementAt(part);

        try {
            s_otherInfo = new OtherInformation(p, true);
            part++; // yes, consumed
        } catch (ASN1Exception e) {
            s_otherInfo = null; // no, not present
        }

        // Should not be any more parts

        if (part < num_parts) {
            throw new ASN1Exception("Zebulun ExtendedServicesRequest: bad BER: extra data " + part + "/" + num_parts + " processed");
        }
    }

    /**
     * Returns a BER encoding of the ExtendedServicesRequest.
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
     * Returns a BER encoding of ExtendedServicesRequest, implicitly tagged.
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

        int num_fields = 3; // number of mandatories
        if (s_referenceId != null) {
            num_fields++;
        }
        if (s_packageName != null) {
            num_fields++;
        }
        if (s_userId != null) {
            num_fields++;
        }
        if (s_retentionTime != null) {
            num_fields++;
        }
        if (s_permissions != null) {
            num_fields++;
        }
        if (s_description != null) {
            num_fields++;
        }
        if (s_taskSpecificParameters != null) {
            num_fields++;
        }
        if (s_elements != null) {
            num_fields++;
        }
        if (s_otherInfo != null) {
            num_fields++;
        }

        // Encode it

        BEREncoding fields[] = new BEREncoding[num_fields];
        int x = 0;

        // Encoding s_referenceId: ReferenceId OPTIONAL

        if (s_referenceId != null) {
            fields[x++] = s_referenceId.ber_encode();
        }

        // Encoding s_function: INTEGER

        fields[x++] = s_function.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 3);

        // Encoding s_packageType: OBJECT IDENTIFIER

        fields[x++] = s_packageType.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 4);

        // Encoding s_packageName: InternationalString OPTIONAL

        if (s_packageName != null) {
            fields[x++] = s_packageName.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 5);
        }

        // Encoding s_userId: InternationalString OPTIONAL

        if (s_userId != null) {
            fields[x++] = s_userId.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 6);
        }

        // Encoding s_retentionTime: IntUnit OPTIONAL

        if (s_retentionTime != null) {
            fields[x++] = s_retentionTime.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 7);
        }

        // Encoding s_permissions: Permissions OPTIONAL

        if (s_permissions != null) {
            fields[x++] = s_permissions.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 8);
        }

        // Encoding s_description: InternationalString OPTIONAL

        if (s_description != null) {
            fields[x++] = s_description.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 9);
        }

        // Encoding s_taskSpecificParameters: EXTERNAL OPTIONAL

        if (s_taskSpecificParameters != null) {
            fields[x++] = s_taskSpecificParameters.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 10);
        }

        // Encoding s_waitAction: INTEGER

        fields[x++] = s_waitAction.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 11);

        // Encoding s_elements: ElementSetName OPTIONAL

        if (s_elements != null) {
            fields[x++] = s_elements.ber_encode();
        }

        // Encoding s_otherInfo: OtherInformation OPTIONAL

        if (s_otherInfo != null) {
            fields[x++] = s_otherInfo.ber_encode();
        }

        return new BERConstructed(tag_type, tag, fields);
    }

    /**
     * Returns a new String object containing a text representing
     * of the ExtendedServicesRequest.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");
        int outputted = 0;

        if (s_referenceId != null) {
            str.append("referenceId ");
            str.append(s_referenceId);
            outputted++;
        }

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("function ");
        str.append(s_function);
        outputted++;

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("packageType ");
        str.append(s_packageType);
        outputted++;

        if (s_packageName != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("packageName ");
            str.append(s_packageName);
            outputted++;
        }

        if (s_userId != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("userId ");
            str.append(s_userId);
            outputted++;
        }

        if (s_retentionTime != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("retentionTime ");
            str.append(s_retentionTime);
            outputted++;
        }

        if (s_permissions != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("permissions ");
            str.append(s_permissions);
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

        if (s_taskSpecificParameters != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("taskSpecificParameters ");
            str.append(s_taskSpecificParameters);
            outputted++;
        }

        if (0 < outputted) {
            str.append(", ");
        }
        str.append("waitAction ");
        str.append(s_waitAction);
        outputted++;

        if (s_elements != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("elements ");
            str.append(s_elements);
            outputted++;
        }

        if (s_otherInfo != null) {
            if (0 < outputted) {
                str.append(", ");
            }
            str.append("otherInfo ");
            str.append(s_otherInfo);
            outputted++;
        }

        str.append("}");

        return str.toString();
    }

/*
 * Internal variables for class.
 */

    public ReferenceId s_referenceId; // optional
    public ASN1Integer s_function;
    public ASN1ObjectIdentifier s_packageType;
    public InternationalString s_packageName; // optional
    public InternationalString s_userId; // optional
    public IntUnit s_retentionTime; // optional
    public Permissions s_permissions; // optional
    public InternationalString s_description; // optional
    public ASN1External s_taskSpecificParameters; // optional
    public ASN1Integer s_waitAction;
    public ElementSetName s_elements; // optional
    public OtherInformation s_otherInfo; // optional

/*
 * Enumerated constants for class.
 */

    // Enumerated constants for function
    public static final int E_create = 1;
    public static final int E_delete = 2;
    public static final int E_modify = 3;

    // Enumerated constants for waitAction
    public static final int E_wait = 1;
    public static final int E_waitIfPossible = 2;
    public static final int E_dontWait = 3;
    public static final int E_dontReturnPackage = 4;

}