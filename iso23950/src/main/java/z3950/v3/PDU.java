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
import asn1.BEREncoding;



/**
 * Class for representing a <code>PDU</code> from <code>Z39-50-APDU-1995</code>
 * <p/>
 * <pre>
 * PDU ::=
 * CHOICE {
 *   initRequest [20] IMPLICIT InitializeRequest
 *   initResponse [21] IMPLICIT InitializeResponse
 *   searchRequest [22] IMPLICIT SearchRequest
 *   searchResponse [23] IMPLICIT SearchResponse
 *   presentRequest [24] IMPLICIT PresentRequest
 *   presentResponse [25] IMPLICIT PresentResponse
 *   deleteResultSetRequest [26] IMPLICIT DeleteResultSetRequest
 *   deleteResultSetResponse [27] IMPLICIT DeleteResultSetResponse
 *   accessControlRequest [28] IMPLICIT AccessControlRequest
 *   accessControlResponse [29] IMPLICIT AccessControlResponse
 *   resourceControlRequest [30] IMPLICIT ResourceControlRequest
 *   resourceControlResponse [31] IMPLICIT ResourceControlResponse
 *   triggerResourceControlRequest [32] IMPLICIT TriggerResourceControlRequest
 *   resourceReportRequest [33] IMPLICIT ResourceReportRequest
 *   resourceReportResponse [34] IMPLICIT ResourceReportResponse
 *   scanRequest [35] IMPLICIT ScanRequest
 *   scanResponse [36] IMPLICIT ScanResponse
 *   sortRequest [43] IMPLICIT SortRequest
 *   sortResponse [44] IMPLICIT SortResponse
 *   segmentRequest [45] IMPLICIT Segment
 *   extendedServicesRequest [46] IMPLICIT ExtendedServicesRequest
 *   extendedServicesResponse [47] IMPLICIT ExtendedServicesResponse
 *   close [48] IMPLICIT Close
 * }
 * </pre>
 *
 * @version $Release$ $Date$
 */



public final class PDU extends ASN1Any {

    public final static String VERSION = "Copyright (C) Hoylen Sue, 1998. 199809080315Z";



    /**
     * Default constructor for a PDU.
     */

    public PDU() {
    }



    /**
     * Constructor for a PDU from a BER encoding.
     * <p/>
     *
     * @param ber       the BER encoding.
     * @param check_tag will check tag if true, use false
     *                  if the BER has been implicitly tagged. You should
     *                  usually be passing true.
     * @exception ASN1Exception if the BER encoding is bad.
     */

    public PDU(BEREncoding ber, boolean check_tag)
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

        c_initRequest = null;
        c_initResponse = null;
        c_searchRequest = null;
        c_searchResponse = null;
        c_presentRequest = null;
        c_presentResponse = null;
        c_deleteResultSetRequest = null;
        c_deleteResultSetResponse = null;
        c_accessControlRequest = null;
        c_accessControlResponse = null;
        c_resourceControlRequest = null;
        c_resourceControlResponse = null;
        c_triggerResourceControlRequest = null;
        c_resourceReportRequest = null;
        c_resourceReportResponse = null;
        c_scanRequest = null;
        c_scanResponse = null;
        c_sortRequest = null;
        c_sortResponse = null;
        c_segmentRequest = null;
        c_extendedServicesRequest = null;
        c_extendedServicesResponse = null;
        c_close = null;

        // Try choice initRequest
        if (ber.tag_get() == 20 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_initRequest = new InitializeRequest(ber, false);
            return;
        }

        // Try choice initResponse
        if (ber.tag_get() == 21 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_initResponse = new InitializeResponse(ber, false);
            return;
        }

        // Try choice searchRequest
        if (ber.tag_get() == 22 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_searchRequest = new SearchRequest(ber, false);
            return;
        }

        // Try choice searchResponse
        if (ber.tag_get() == 23 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_searchResponse = new SearchResponse(ber, false);
            return;
        }

        // Try choice presentRequest
        if (ber.tag_get() == 24 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_presentRequest = new PresentRequest(ber, false);
            return;
        }

        // Try choice presentResponse
        if (ber.tag_get() == 25 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_presentResponse = new PresentResponse(ber, false);
            return;
        }

        // Try choice deleteResultSetRequest
        if (ber.tag_get() == 26 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_deleteResultSetRequest = new DeleteResultSetRequest(ber, false);
            return;
        }

        // Try choice deleteResultSetResponse
        if (ber.tag_get() == 27 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_deleteResultSetResponse = new DeleteResultSetResponse(ber, false);
            return;
        }

        // Try choice accessControlRequest
        if (ber.tag_get() == 28 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_accessControlRequest = new AccessControlRequest(ber, false);
            return;
        }

        // Try choice accessControlResponse
        if (ber.tag_get() == 29 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_accessControlResponse = new AccessControlResponse(ber, false);
            return;
        }

        // Try choice resourceControlRequest
        if (ber.tag_get() == 30 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_resourceControlRequest = new ResourceControlRequest(ber, false);
            return;
        }

        // Try choice resourceControlResponse
        if (ber.tag_get() == 31 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_resourceControlResponse = new ResourceControlResponse(ber, false);
            return;
        }

        // Try choice triggerResourceControlRequest
        if (ber.tag_get() == 32 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_triggerResourceControlRequest = new TriggerResourceControlRequest(ber, false);
            return;
        }

        // Try choice resourceReportRequest
        if (ber.tag_get() == 33 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_resourceReportRequest = new ResourceReportRequest(ber, false);
            return;
        }

        // Try choice resourceReportResponse
        if (ber.tag_get() == 34 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_resourceReportResponse = new ResourceReportResponse(ber, false);
            return;
        }

        // Try choice scanRequest
        if (ber.tag_get() == 35 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_scanRequest = new ScanRequest(ber, false);
            return;
        }

        // Try choice scanResponse
        if (ber.tag_get() == 36 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_scanResponse = new ScanResponse(ber, false);
            return;
        }

        // Try choice sortRequest
        if (ber.tag_get() == 43 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_sortRequest = new SortRequest(ber, false);
            return;
        }

        // Try choice sortResponse
        if (ber.tag_get() == 44 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_sortResponse = new SortResponse(ber, false);
            return;
        }

        // Try choice segmentRequest
        if (ber.tag_get() == 45 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_segmentRequest = new Segment(ber, false);
            return;
        }

        // Try choice extendedServicesRequest
        if (ber.tag_get() == 46 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_extendedServicesRequest = new ExtendedServicesRequest(ber, false);
            return;
        }

        // Try choice extendedServicesResponse
        if (ber.tag_get() == 47 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_extendedServicesResponse = new ExtendedServicesResponse(ber, false);
            return;
        }

        // Try choice close
        if (ber.tag_get() == 48 &&
                ber.tag_type_get() == BEREncoding.CONTEXT_SPECIFIC_TAG) {
            c_close = new Close(ber, false);
            return;
        }

        throw new ASN1Exception("Zebulun PDU: bad BER encoding: choice not matched");
    }



    /**
     * Returns a BER encoding of PDU.
     *
     * @return The BER encoding.
     * @exception ASN1Exception Invalid or cannot be encoded.
     */

    public BEREncoding
    ber_encode()
            throws ASN1Exception {
        BEREncoding chosen = null;

        // Encoding choice: c_initRequest
        if (c_initRequest != null) {
            chosen = c_initRequest.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 20);
        }

        // Encoding choice: c_initResponse
        if (c_initResponse != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_initResponse.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 21);
        }

        // Encoding choice: c_searchRequest
        if (c_searchRequest != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_searchRequest.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 22);
        }

        // Encoding choice: c_searchResponse
        if (c_searchResponse != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_searchResponse.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 23);
        }

        // Encoding choice: c_presentRequest
        if (c_presentRequest != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_presentRequest.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 24);
        }

        // Encoding choice: c_presentResponse
        if (c_presentResponse != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_presentResponse.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 25);
        }

        // Encoding choice: c_deleteResultSetRequest
        if (c_deleteResultSetRequest != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_deleteResultSetRequest.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 26);
        }

        // Encoding choice: c_deleteResultSetResponse
        if (c_deleteResultSetResponse != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_deleteResultSetResponse.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 27);
        }

        // Encoding choice: c_accessControlRequest
        if (c_accessControlRequest != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_accessControlRequest.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 28);
        }

        // Encoding choice: c_accessControlResponse
        if (c_accessControlResponse != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_accessControlResponse.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 29);
        }

        // Encoding choice: c_resourceControlRequest
        if (c_resourceControlRequest != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_resourceControlRequest.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 30);
        }

        // Encoding choice: c_resourceControlResponse
        if (c_resourceControlResponse != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_resourceControlResponse.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 31);
        }

        // Encoding choice: c_triggerResourceControlRequest
        if (c_triggerResourceControlRequest != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_triggerResourceControlRequest.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 32);
        }

        // Encoding choice: c_resourceReportRequest
        if (c_resourceReportRequest != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_resourceReportRequest.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 33);
        }

        // Encoding choice: c_resourceReportResponse
        if (c_resourceReportResponse != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_resourceReportResponse.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 34);
        }

        // Encoding choice: c_scanRequest
        if (c_scanRequest != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_scanRequest.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 35);
        }

        // Encoding choice: c_scanResponse
        if (c_scanResponse != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_scanResponse.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 36);
        }

        // Encoding choice: c_sortRequest
        if (c_sortRequest != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_sortRequest.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 43);
        }

        // Encoding choice: c_sortResponse
        if (c_sortResponse != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_sortResponse.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 44);
        }

        // Encoding choice: c_segmentRequest
        if (c_segmentRequest != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_segmentRequest.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 45);
        }

        // Encoding choice: c_extendedServicesRequest
        if (c_extendedServicesRequest != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_extendedServicesRequest.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 46);
        }

        // Encoding choice: c_extendedServicesResponse
        if (c_extendedServicesResponse != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_extendedServicesResponse.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 47);
        }

        // Encoding choice: c_close
        if (c_close != null) {
            if (chosen != null) {
                throw new ASN1Exception("CHOICE multiply set");
            }
            chosen = c_close.ber_encode(BEREncoding.CONTEXT_SPECIFIC_TAG, 48);
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

        throw new ASN1EncodingException("Zebulun PDU: cannot implicitly tag");
    }



    /**
     * Returns a new String object containing a text representing
     * of the PDU.
     */

    public String
    toString() {
        StringBuffer str = new StringBuffer("{");

        boolean found = false;

        if (c_initRequest != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: initRequest> ");
            }
            found = true;
            str.append("initRequest ");
            str.append(c_initRequest);
        }

        if (c_initResponse != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: initResponse> ");
            }
            found = true;
            str.append("initResponse ");
            str.append(c_initResponse);
        }

        if (c_searchRequest != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: searchRequest> ");
            }
            found = true;
            str.append("searchRequest ");
            str.append(c_searchRequest);
        }

        if (c_searchResponse != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: searchResponse> ");
            }
            found = true;
            str.append("searchResponse ");
            str.append(c_searchResponse);
        }

        if (c_presentRequest != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: presentRequest> ");
            }
            found = true;
            str.append("presentRequest ");
            str.append(c_presentRequest);
        }

        if (c_presentResponse != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: presentResponse> ");
            }
            found = true;
            str.append("presentResponse ");
            str.append(c_presentResponse);
        }

        if (c_deleteResultSetRequest != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: deleteResultSetRequest> ");
            }
            found = true;
            str.append("deleteResultSetRequest ");
            str.append(c_deleteResultSetRequest);
        }

        if (c_deleteResultSetResponse != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: deleteResultSetResponse> ");
            }
            found = true;
            str.append("deleteResultSetResponse ");
            str.append(c_deleteResultSetResponse);
        }

        if (c_accessControlRequest != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: accessControlRequest> ");
            }
            found = true;
            str.append("accessControlRequest ");
            str.append(c_accessControlRequest);
        }

        if (c_accessControlResponse != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: accessControlResponse> ");
            }
            found = true;
            str.append("accessControlResponse ");
            str.append(c_accessControlResponse);
        }

        if (c_resourceControlRequest != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: resourceControlRequest> ");
            }
            found = true;
            str.append("resourceControlRequest ");
            str.append(c_resourceControlRequest);
        }

        if (c_resourceControlResponse != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: resourceControlResponse> ");
            }
            found = true;
            str.append("resourceControlResponse ");
            str.append(c_resourceControlResponse);
        }

        if (c_triggerResourceControlRequest != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: triggerResourceControlRequest> ");
            }
            found = true;
            str.append("triggerResourceControlRequest ");
            str.append(c_triggerResourceControlRequest);
        }

        if (c_resourceReportRequest != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: resourceReportRequest> ");
            }
            found = true;
            str.append("resourceReportRequest ");
            str.append(c_resourceReportRequest);
        }

        if (c_resourceReportResponse != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: resourceReportResponse> ");
            }
            found = true;
            str.append("resourceReportResponse ");
            str.append(c_resourceReportResponse);
        }

        if (c_scanRequest != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: scanRequest> ");
            }
            found = true;
            str.append("scanRequest ");
            str.append(c_scanRequest);
        }

        if (c_scanResponse != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: scanResponse> ");
            }
            found = true;
            str.append("scanResponse ");
            str.append(c_scanResponse);
        }

        if (c_sortRequest != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: sortRequest> ");
            }
            found = true;
            str.append("sortRequest ");
            str.append(c_sortRequest);
        }

        if (c_sortResponse != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: sortResponse> ");
            }
            found = true;
            str.append("sortResponse ");
            str.append(c_sortResponse);
        }

        if (c_segmentRequest != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: segmentRequest> ");
            }
            found = true;
            str.append("segmentRequest ");
            str.append(c_segmentRequest);
        }

        if (c_extendedServicesRequest != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: extendedServicesRequest> ");
            }
            found = true;
            str.append("extendedServicesRequest ");
            str.append(c_extendedServicesRequest);
        }

        if (c_extendedServicesResponse != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: extendedServicesResponse> ");
            }
            found = true;
            str.append("extendedServicesResponse ");
            str.append(c_extendedServicesResponse);
        }

        if (c_close != null) {
            if (found) {
                str.append("<ERROR: multiple CHOICE: close> ");
            }
            found = true;
            str.append("close ");
            str.append(c_close);
        }

        str.append("}");

        return str.toString();
    }


/*
 * Internal variables for class.
 */

    public InitializeRequest c_initRequest;
    public InitializeResponse c_initResponse;
    public SearchRequest c_searchRequest;
    public SearchResponse c_searchResponse;
    public PresentRequest c_presentRequest;
    public PresentResponse c_presentResponse;
    public DeleteResultSetRequest c_deleteResultSetRequest;
    public DeleteResultSetResponse c_deleteResultSetResponse;
    public AccessControlRequest c_accessControlRequest;
    public AccessControlResponse c_accessControlResponse;
    public ResourceControlRequest c_resourceControlRequest;
    public ResourceControlResponse c_resourceControlResponse;
    public TriggerResourceControlRequest c_triggerResourceControlRequest;
    public ResourceReportRequest c_resourceReportRequest;
    public ResourceReportResponse c_resourceReportResponse;
    public ScanRequest c_scanRequest;
    public ScanResponse c_scanResponse;
    public SortRequest c_sortRequest;
    public SortResponse c_sortResponse;
    public Segment c_segmentRequest;
    public ExtendedServicesRequest c_extendedServicesRequest;
    public ExtendedServicesResponse c_extendedServicesResponse;
    public Close c_close;

} // PDU


//EOF
