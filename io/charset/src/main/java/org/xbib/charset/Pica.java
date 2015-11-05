/**
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
package org.xbib.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

import java.util.HashMap;
import java.util.Map;

/**
 * PICA character set implementation.
 *
 * This character set is a modified version of the 'InterMARC' character set
 * and contains 256 tokens.
 *
 * A description can be found at
 * <a href="http://www.pica.nl/ne/docu/dn010/html/t07.shtml">the Pica website.
 *
 */
public class Pica extends Charset {

    private final static HashMap encodeMap = new HashMap(128);
    private final static HashMap decodeMap = new HashMap(128);

    /**
     * Pica character mapping for index subset \u00a0..\u00ff.
     * Pica is equal to US-ASCII but not ISO-8859-1.
     * These are the definitions for Pica characters
     * which are different from ISO-8859-1.
     */
    static {
        Pica.charTable(encodeMap, decodeMap, '\u00a0', '\u00ff',
                new char[]{
                    '\u00a0', '\u0141', '\u00d8', '\u0110', '\u00de', '\u00c6',
                    '\u0152', '\u02b9', '\u00b7', '\u266d', '\u00ae', '\u00b1',
                    '\u01a0', '\u01af', '\u02be', '\u00c5', '\u02bf', '\u0142',
                    '\u00f8', '\u0111', '\u00fe', '\u00e6', '\u0153', '\u02ba',
                    '\u0131', '\u00a3', '\u00f0', '\u03b1', '\u01a1', '\u01b0',
                    '\u00df', '\u00e5', '\u0132', '\u00c4', '\u00d6', '\u00dc',
                    '\u0186', '\u018e', '\u2260', '\u2192', '\u2264', '\u221e',
                    '\u222b', '\u00d7', '\u00a7', '\u22a1', '\u21d4', '\u2265',
                    '\u0133', '\u00e4', '\u00f6', '\u00fc', '\u0254', '\u0258',
                    '\u00bf', '\u00a1', '\u03b2', '\u003f', '\u03b3', '\u03c0',
                    '\u003f', '\u003f', '\u003f', '\u003f', '\u0341', '\u0300',
                    '\u0301', '\u0302', '\u0303', '\u0304', '\u0306', '\u0307',
                    '\u0308', '\u030c', '\u030a', '\ufe20', '\ufe21', '\u0315',
                    '\u030b', '\u0310', '\u0327', '\u0000', '\u0323', '\u0324',
                    '\u0325', '\u0333', '\u0332', '\u003f', '\u031c', '\u032e',
                    '\ufe23', '\ufe22', '\u003f', '\u0000', '\u0313', '\u003f'
                });
    }
    // Handle to the real charset we'll use for transcoding between
    // characters and bytes.  Doing this allows applying the Pica
    // charset to multi-byte charset encodings like UTF-8.
    Charset encodeCharset;

    /**
     * Constructor for the Pica charset.  Call the superclass
     * constructor to pass along the name(s) we'll be known by.
     * Then save a reference to the delegate Charset.
     */
    public Pica() {
        super("PICA", CharsetProvider.aliasesFor("PICA"));
        encodeCharset = Charset.forName("ISO-8859-1");
    }

    /**
     * Fill the conversion tables.
     */
    static void charTable(Map encodeMap, Map decodeMap, char from, char to,
            char[] code) {
        int i = 0;

        for (char c = from; c <= to; c++) {
            if (code[i] != '\u0000') {
                encodeMap.put(Character.valueOf(code[i]), Character.valueOf(c));
                decodeMap.put(Character.valueOf(c), Character.valueOf(code[i]));
            }

            i++;
        }
    }

    /**
     * This method must be implemented by concrete Charsets.  We allow
     * subclasses of the Pica charset.
     */
    public boolean contains(Charset charset) {
        return charset instanceof Pica;
    }

    /**
     * Called by users of this Charset to obtain an encoder.
     * This implementation instantiates an instance of a private class
     * (defined below) and passes it an encoder from the base Charset.
     */
    public CharsetEncoder newEncoder() {
        return new PicaEncoder(this, encodeCharset.newEncoder());
    }

    /**
     * Called by users of this Charset to obtain a decoder.
     * This implementation instantiates an instance of a private class
     * (defined below) and passes it a decoder from the base Charset.
     */
    public CharsetDecoder newDecoder() {
        return new PicaDecoder(this, encodeCharset.newDecoder());
    }

    private static class PicaEncoder extends CharsetEncoder {

        private CharsetEncoder baseEncoder;

        /**
         * Constructor, call the superclass constructor with the
         * Charset object and the encodings sizes from the
         * delegate encoder.
         */
        PicaEncoder(Charset cs, CharsetEncoder baseEncoder) {
            super(cs, baseEncoder.averageBytesPerChar(),
                    baseEncoder.maxBytesPerChar());
            this.baseEncoder = baseEncoder;
        }

        /**
         * Implementation of the encoding loop.  First, we apply
         * the Pica charset mapping to the CharBuffer, then
         * reset the encoder for the base Charset and call it's
         * encode() method to do the actual encoding. The CharBuffer
         * passed in may be read-only or re-used by the caller for
         * other purposes so we duplicate it and apply the Pica
         * encoding to the copy.  We do want to advance the position
         * of the input buffer to reflect the chars consumed.
         */
        protected CoderResult encodeLoop(CharBuffer cb, ByteBuffer bb) {
            CharBuffer tmpcb = CharBuffer.allocate(cb.remaining());

            while (cb.hasRemaining()) {
                tmpcb.put(cb.get());
            }

            tmpcb.rewind();

            for (int pos = tmpcb.position(); pos < tmpcb.limit(); pos++) {
                char c = tmpcb.get(pos);
                Character mapChar = (Character) encodeMap.get(Character.valueOf(c));

                if (mapChar != null) {
                    tmpcb.put(pos, mapChar.charValue());
                }
            }

            baseEncoder.reset();

            CoderResult cr = baseEncoder.encode(tmpcb, bb, true);

            // If error or output overflow, we need to adjust
            // the position of the input buffer to match what
            // was really consumed from the temp buffer.  If
            // underflow (all input consumed) this is a no-op.
            cb.position(cb.position() - tmpcb.remaining());

            return cr;
        }
    }

    /**
     * The decoder implementation for the Pica Charset.
     */
    private static class PicaDecoder extends CharsetDecoder {

        /**
         * Constructor, call the superclass constructor with the
         * Charset object and pass alon the chars/byte values
         * from the delegate decoder.
         */
        PicaDecoder(Charset cs, CharsetDecoder baseDecoder) {
            // base decoder only needed for size hints
            super(cs, baseDecoder.averageCharsPerByte(),
                    baseDecoder.maxCharsPerByte());
        }

        /**
         * Implementation of the decoding loop.
         */
        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            while (in.hasRemaining()) {
                byte b = in.get();

                if (!out.hasRemaining()) {
                    in.position(in.position() - 1);
                    return CoderResult.OVERFLOW;
                }
                char oldChar = (char) (b & 0xFF);
                Character mapChar = (Character) decodeMap.get(Character.valueOf(oldChar));
                out.put(mapChar != null ? mapChar.charValue() : oldChar);
            }
            return CoderResult.UNDERFLOW;
        }
    }
}
/*

De PICA characterset is een enigszins gemodificeerde versie van de INTERMARC characterset.

Deze characterset omvat in totaal 256 tekens.

Kolommen:
(1) = Octaal
(2) = Teken
(3) = Omschrijving

(1)  (2)  (3)

-----------------------------------------------------------------

000-177   Standaards ASCII (eerste groep van 128 tekens)

200-237   niet gebruikt

240     diacritische spatie

241     Poolse L

242     Deense O

243   Ð   Servische D

244   Þ   Thorn (groot)

245   Æ   Ligatuur AE

246   ¼   Ligatuur OE

247   ¢   Cyrillische zachtteken (translitt.)

250   ×   Griekse half-hoge punt

251     Mol

252   ®   Registratie-teken

253   ±   Plusminus

254   O   Vietnamese O-haak

255   U   Vietnamese U-haak

256   ?   Alif

257   Å   Angstrom A

260   `   Ayn

261     Poolse l

262     Deense o

263     Servische d

264   þ   Thorn (klein)

265   æ   Ligatuur ae

266   ½   Ligatuur oe

267     Cyrillische hardteken (translitt.)

270     Turkse i (zonder punt)

271   £   Brits pond-teken

272     Eth

273   a   Alfa

274     Vietnamese o-haak

275     Vietnamese u-haak

276   ß   Duitse dubbele S

277   å   Angstrom a

300     Nederlandse IJ

301   Ä   Umlaut A

302   Ö   Umlaut O

303   Ü   Umlaut U

304     Omgekeerde C

305     Omgekeerde E

306   ¹   Ongelijk-teken

307   ®   Fleche

310   £   Kleiner dan/is-gelijk-teken

311   ¥   Oneindig-teken

312   ò   Integraal-teken

313     Vermenigvuldiging-teken

314   §   Paragraaf

315   Ö   Vierkantswortel-teken

316     Reaction

317   ³   Groter dan/is-gelijk-teken

320     Nederlandse ij

321   ä   Umlaut a

322   ö   Umlaut o

323   ü   Umlaut u

324     Omgekeerde c

325     Omgekeerde e

326   ¿   Spaans omgekeerd vraagteken

327   ¡   Spaans omgekeerd uitroepteken

330   b   Beta

331

332   g   Gamma

333   p   Pi

334

335

336

337

340   `   Vietnamese rijzende toon

341   `   Accent grave (zie ook octaal 140)

342   ?   Accent aigu

343   ?   Accent circonflexe (zie ook 140)

344   ~   Tilde

345   ¯   Bovenstreepje (lang)

346     Bovenstreepje (kort)

347   ×   Punt boven

350   ?   Trema (geen umlaut)

351     Hacek

352   ?   Angstrom

353     Ligatuur links

354     Ligatuur rechts

355   '   Komma als accent (bovenaan)

356   ²   Dubbele aigu

357     Candrabindu

360   ?   Cedille

361     Hoek boven links

362   ¢   Punt als accent (onderaan)

363   ²   Twee punten als accent (onderaan)

364     Cirkeltje onderaan

365     Dubbele onderstreping als accent

366   _   Onderstreping als accent

367     Hoek boven rechts

370     Omgekeerde cedille

371     Upadhmaniya (geen accent)

372     Halve tilde rechts

373     Halve tilde links

374

375

376   ?   Komma rechts (op middelhoogte)

377

 */
