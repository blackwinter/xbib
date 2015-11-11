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
package asn1;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * This class represents a BER (Basic Encoding Rules) encoded ASN.1 object.
 * This is an abstract base class from which there are two specific
 * representations are used: primitive and constructed. This superclass
 * is tightly coupled with its subclasses: BERPrimitive and BERConstructed.
 * <p/>
 * <p/>
 * The BER encoding is described in
 * <p/>
 * <em>Information technology -
 * Open Systems Interconnection -
 * Specification of basic encoding rules for Abstract Syntax Notation
 * One (ASN.1)</em>
 * AS 3626-1991
 * ISO/IEC 8825:1990
 *
 * @see asn1.BERPrimitive
 * @see asn1.BERConstructed
 */

public abstract class BEREncoding {
    /**
     * Constant for indicating UNIVERSAL tag type. The value matches
     * the BER bit encoding. Universal tags are for types defined in
     * the ASN.1 standard.
     */
    public static final int UNIVERSAL_TAG = 0x00;

    /**
     * Constant for indicating APPLICATION tag type. The value matches
     * the BER bit encoding. APPLICATION tags are globally unique to an
     * application.
     */
    public static final int APPLICATION_TAG = 0x40;

    /**
     * Constant for indicating CONTEXT SPECIFIC tag type. The value matches
     * the BER bit encoding. CONTEXT SPECIFIC tags are used in applications,
     * but do not have to be globally unique.
     */
    public static final int CONTEXT_SPECIFIC_TAG = 0x80;

    /**
     * Constant for indicating PRIVATE tag type. The value matches
     * the BER bit encoding.
     */
    public static final int PRIVATE_TAG = 0xC0;

    private static final int MAX_BER_SIZE = 65536 * 4;

    /**
     * Outputs the BER object to an OutputStream. This method should work
     * with any OutputStream, whether it is from a socket, file, etc.
     * <p/>
     * <p/>
     * Note: the output is not flushed, so you <strong>must</strong>  explicitly
     * flush the output stream after calling this method to ensure that
     * the data has been written out.
     *
     * @throws java.io.IOException On output I/O error
     * @param    dest - the OutputStream to write the encoding to.
     */

    public abstract void
    output(OutputStream dest) throws java.io.IOException;

    /**
     * Returns the BER encoded object as an array of bytes. This routine
     * may be of use if you want to use the encoding rather than sending
     * it off. If you want to just output it, it is more efficient to
     * use the output method.
     */

    public byte[]
    encoding_get() {
        byte result[] = new byte[i_total_length];
        i_encoding_get(0, result);
        return result;
    }

    /**
     * Method to examine the tag type of the BER encoded ASN.1 object.
     */

    public int
    tag_type_get() {
        return i_tag_type;
    }

    /**
     * Method to examine the tag number of the BER encoded ASN.1 object.
     */

    public int
    tag_get() {
        return i_tag;
    }

    /**
     * Returns the total number of bytes the encoding occupies.
     */

    public int total_length() {
        return i_total_length;
    }


    /**
     * The public wrapping for doInput() method.
     *
     * @param    src - the InputStream to read the raw BER from.
     * @returns Returns the next complete BEREncoding object read
     * in from the input stream. Returns null if the
     * end has been reached.
     * @exception ASN1Exception If data does not represent a BER encoding
     * @exception java.io.IOException On input I/O error
     */

    public static BEREncoding input(InputStream src) throws ASN1Exception, java.io.IOException {
        int numBytesRead[] = new int[1];

        numBytesRead[0] = 0;

        return doInput(src, numBytesRead);
    }


    /**
     * Constructs a complete BER encoding object from octets read in from
     * an InputStream.
     * <p/>
     * This routine handles all forms of encoding, including the
     * indefite-length method. The length is always known with this
     * class. With indefinite-length encodings,
     * the end-of-contents octets are not included in the returned
     * object (i.e. the returned the raw BER is converted to an object
     * which is in the definite-length form).
     *
     * @param numBytesRead - a counter for all read bytes.
     * @param    src - the InputStream to read the raw BER from.
     * @returns Returns the next complete BEREncoding object read
     * in from the input stream. Returns null if the
     * end has been reached.
     * @exception ASN1Exception If data does not represent a BER encoding
     * @exception java.io.IOException On input I/O error
     */

    protected static BEREncoding doInput(InputStream src, int numBytesRead[]) throws ASN1Exception, java.io.IOException {
        // Read in the identifier octets
        int octet = src.read(); // first octet
        if (octet < 0) {
            return null; // end of source reached, return sentinel indicator
        }

        numBytesRead[0]++; // icrements bytes read count

        int tag_type = (octet & 0xC0); // bits 8 and 7 indicate tag type

        boolean is_cons = false; // is constructed
        if ((octet & 0x20) != 0) // bit 6 indicates primitive/constructed
        {
            is_cons = true;
        }

        int tag = octet & 0x1F; // bits 5-1 is (possible) the tag number
        if (tag == 0x1F) {
            // Multiple octet form of tag, need to examine following bytes
            // Tag value in base-128, bytes have MSB set except for last byte

            tag = 0;
            do {
                octet = src.read();
                if (octet < 0) {
                    throw new ASN1EncodingException("Unexpected end in BER encoding");
                }

                numBytesRead[0]++; // icrements bytes read count

                tag <<= 7;
                tag |= (octet & 0x7F);
            } while ((octet & 0x80) != 0);
        }

        // Read in length octets

        int length;

        octet = src.read(); // first octet of length octets
        if (octet < 0) {
            throw new ASN1EncodingException("Unexpected end in BER encoding");
        }

        numBytesRead[0]++; // icrements bytes read count

        if ((octet & 0x80) != 0) {
            if ((octet & 0x7f) == 0) {
                // Indefinite length
                length = -1;
                if (!is_cons) {
                    throw new ASN1EncodingException("BER encoding corrupted primitive");
                }

            } else {
                // Long form of length, number of octets byte plus tag in base-256
                if (4 < (octet & 0x7f)) // Can't handle huge numbers!
                {
                    throw new ASN1EncodingException("BER encoding too long");
                }

                length = 0;

                for (int num_bytes = (octet & 0x7f); 0 < num_bytes; num_bytes--) {
                    octet = src.read();
                    if (octet < 0) {
                        throw new ASN1EncodingException("Unexpected end in BER encoding");
                    }

                    numBytesRead[0]++; // icrements bytes read count

                    length <<= 8;
                    length |= (octet & 0xff);
                }

                if (length < 0 || MAX_BER_SIZE < length) {
                    throw new ASN1EncodingException("BER encoding too long");
                }
            }
        } else {
            // Short form of length, tag in lower 7 bits
            length = (octet & 0x7F);
        }

        // Read in the content octets

        if (!is_cons) {
            // Primitive

            // Indefinite length on a primitive BER
            // Might be able to handle this, but not in current implementation.
            if (length < 0) {
                throw new ASN1EncodingException("Indefinite length primitive BER");
            }

            int contents[] = new int[length];

            for (int x = 0; x < length; x++) {
                octet = src.read();
                if (octet < 0) {
                    throw new ASN1EncodingException("Unexpected end in BER encoding");
                }

                numBytesRead[0]++; // icrements bytes read count

                contents[x] = octet;
            }
            return new BERPrimitive(tag_type, tag, contents);
        } else {
            // Constructed

            ArrayList chunks = new ArrayList(); // dynamic storage for parts
            int total_read = 0;


            if (0 <= length) {
                // Definite length, get all encoded components
                while (total_read < length) {
                    int current_read = numBytesRead[0]; // num bytes read until now
                    BEREncoding chunk = BEREncoding.doInput(src, numBytesRead); // recursive call
                    if (chunk == null) {
                        throw new ASN1EncodingException("Unexpected end in BER encoding");
                    }

                    chunks.add(chunk);
                    //total_read += chunk.i_total_length; // BUG
                    total_read += numBytesRead[0] - current_read; // adds chunk number bytes read
                }
            } else {

                // Indefinite length, need to keep reading until end-of-contents octets
                while (true) {
                    BEREncoding chunk = BEREncoding.doInput(src, numBytesRead); // recursive call
                    if (chunk == null) {
                        throw new ASN1EncodingException("Unexpected end in BER encoding");
                    }


                    if (chunk.i_tag == 0 && chunk.i_tag_type == BEREncoding.UNIVERSAL_TAG && chunk.i_total_length == 2) {
                        break; // end-of-contents marker reached, stop reading
                    } else {
                        chunks.add(chunk); // add to chunks
                    }
                }
            }


            // Take the chunks and put them in an array of BEREncoding

            int num_elements = chunks.size();
            BEREncoding parts[] = new BEREncoding[num_elements];
            for (int x = 0; x < num_elements; x++) {
                parts[x] = ((BEREncoding) chunks.get(x));
            }

            return new BERConstructed(tag_type, tag, parts);
        }
    }



  /* This is the initialization method used by the subclasses. 
   * The length must be the total length of the encoding of the 
   * contents (i.e. not including the identifier or length encodings).
   *
   * @param tag_type The tag type.
   * @see asn1.BEREncoding.UNIVERSAL_TAG
   * @see asn1.BEREncoding.APPLICATION_TAG
   * @see asn1.BEREncoding.CONTEXT_SPECIFIC_TAG
   * @see asn1.BEREncoding.PRIVATE_TAG
   * @param is_constructed True if constructed, or false if primitive.
   * @param tag The tag number.
   * @exception ASN1Exception if tag or tag type is invalid
   */

    protected void
    init(int tag_type, boolean is_constructed, int tag, int length)
            throws ASN1Exception {
        make_identifier(tag_type, is_constructed, tag);
        make_length(length);

        i_total_length = (identifierEncoding.length +
                lengthEncoding.length +
                length);
    }

  /*
   * This is a protected routine used for outputting an array of
   * integers, interpreted as bytes, to an OutputStream. It is used
   * by the superclasses to implement the "output" method.
   */

    protected void
    output_bytes(int data[], OutputStream dest)
            throws java.io.IOException {
        for (int index = 0; index < data.length; index++) {
            dest.write(data[index]);
        }
    }

  /*
   * This is a protected method used to output the encoded identifier
   * and length octets to an OutputStream. It is used by the superclasses
   * to implement the "output" method.
   */

    protected void
    output_head(OutputStream dest) throws java.io.IOException {
        output_bytes(identifierEncoding, dest);
        output_bytes(lengthEncoding, dest);
    }

  /*
   * Internal protected method fills in the data array (starting from index
   * position offset) with the encoding for the identifier and length.
   * This is used by the superclasses to implement the "encoding_get"
   * method.
   */

    protected int
    i_get_head(int offset, byte data[]) {
        for (int n = 0; n < identifierEncoding.length; n++) {
            data[offset++] = (byte) identifierEncoding[n];
        }

        for (int n = 0; n < lengthEncoding.length; n++) {
            data[offset++] = (byte) lengthEncoding[n];
        }

        return offset;
    }

  /*
   * This is an abstract method used for implementing the "get_encoding"
   * method. This method places the bytes of the encoding into the data
   * array (as bytes), starting at offset into the array. The
   * offset of the last element used plus one is returned.
   */

    protected abstract int
    i_encoding_get(int offset, byte data[]);

    /**
     * This private method encodes the identifier octets. When a BER
     * object is created, this method should be used to set up the encoding
     * of the identifier, called via the "init" method.
     * <p/>
     * This method sets the internal variables "i_tag_type" and "i_tag"
     * so this object can be queried for the tag type and tag value without
     * needing to decode them from the encoding octets.
     *
     * @param    tag_type is the tag type of the object, which
     * must be one of the special value defined in ASN1_Any
     * @param    is_constructed is a boolean flag: true indicating the
     * contents is constructed, or false indicating it is primitive.
     * @param    tag is the value of the tag, which must be non-negative.
     * @exception ASN1Exception when the tag_type is improper, or
     * the tag value is negative.
     */

    private void
    make_identifier(int tag_type, boolean is_constructed, int tag)
            throws ASN1Exception {
        int b;

        if ((tag_type & ~0x00C0) != 0) {
            throw new ASN1Exception("Invalid ASN.1 tag type");
        }
        if (tag < 0) {
            throw new ASN1Exception("ASN.1 tag value is negative");
        }

        // bits 8 and 7 specify the tag type
        i_tag_type = (tag_type & 0xC0);
        b = i_tag_type;

        // set bit-6 if structure rather than primitive
        if (is_constructed) {
            b |= 0x20;
        }

        i_tag = tag;
        if (tag <= 30) {
            // Single octet form
            // bits 5-1 contain tag number

            b |= (tag & 0x1F);

            identifierEncoding = new int[1];
            identifierEncoding[0] = b;
        } else {
            // Multiple octet form
            b |= 0x1F; // bits 5-1 all set to 1's

            // Rest in base 128, with bit-8 set except for in last byte

            int number_bytes = 1;
            int tmp_tag = tag;
            do {
                number_bytes++;
                tmp_tag >>= 7;
            } while (tmp_tag != 0);

            identifierEncoding = new int[number_bytes];
            identifierEncoding[0] = b; // first octet

            int index = 0;
            for (int digit = number_bytes - 2; 0 <= digit; digit--) {
                identifierEncoding[++index] = (tag >> (digit * 7)) & 0x7f;
                if (digit != 0) {
                    identifierEncoding[index] |= 0x80; // bit-8 set in last byte
                }
            }
        }
    }

    /**
     * This private method encodes the length octets. When a BER object
     * is created, this method should be used to set up the encoding
     * of the identifier. It should be used by calling the "init" method.
     *
     * @param    length is the length value to be encoded. A negative
     * value indicates an "indefinite length".
     */

    private void make_length(int length) // Generates a BER encoded stream representing the length.
    {
        if (length < 0) {
            lengthEncoding = new int[1];
            lengthEncoding[0] = 0x80; // indefinite length

        } else if (length < 128) {
            // Short form: one octet

            lengthEncoding = new int[1];
            lengthEncoding[0] = length;

        } else {
            // Long form : two to 127 octets

            int count = 0;
            int shifted = length;
            while (shifted != 0) {
                count++;
                shifted >>= 8;
            }

            lengthEncoding = new int[count + 1];
            lengthEncoding[0] = (count | 0x80); // first: bit-8 set, rest = num bytes

            // remaining octets: length in base 256 (most significant digit first)
            int index = 0;
            while (0 < count) {
                count--;
                int digit = (length >> (count * 8)) & 0xff;
                lengthEncoding[++index] = digit;
            }
        }
    }

    /**
     * Storage for the identifier octets. This variable is set up by
     * calling the make_identifer method.
     * The octets are internally stored as int[] for efficiency over byte[].
     */

    private int identifierEncoding[];

    /**
     * The tag type of this BER encoded object. This value must be
     * the same as that encoded in the identiferEncoding.
     * <p/>
     * <p/>
     * This is an internal member. You should not use this.
     */

    protected int i_tag_type;

    /**
     * The tag number of this BER encoded object. This value must be
     * the same as that encoded in the identiferEncoding.
     * <p/>
     * <p/>
     * This is an internal member. You should not use this.
     */

    protected int i_tag;

    /**
     * Storage for the length encoding octets. This will be set up by
     * calling the make_length method.
     * The octets are internally stored as int[] for efficiency over byte[].
     */

    private int lengthEncoding[];

    /**
     * The total length of this BER object (the identifier octets, plus
     * length octets, plus content octects). This variable must be
     * set up before this object is used (using the init method).
     * <p/>
     * <p/>
     * This is an internal member. You should not use this.
     */

    protected int i_total_length;

}