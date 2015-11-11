
package org.xbib.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

public class ISO_5428 extends Charset {

    public ISO_5428() {
        super("ISO_5428", null);
    }

    @Override
    public boolean contains(Charset cs) {
        return false;
    }

    @Override
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }

    @Override
    public CharsetEncoder newEncoder() {
        return null;
    }

    private static final class Decoder extends CharsetDecoder {

        Decoder(Charset cs) {
            super(cs, 1.0f, 1.0f);
        }

        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            boolean tonos = false;
            boolean dialitika = false;
            while (in.hasRemaining()) {
                byte b = in.get();
                if (!out.hasRemaining()) {
                    in.position(in.position() - 1);
                    return CoderResult.OVERFLOW;
                }
                if (b == (byte) 0xa2) {
                    tonos = true;
                } else if (b == (byte) 0xa3) {
                    dialitika = true;
                }
                int i = (int) b;
                char c;
                switch (i) {
                    case 0xe1: {
                        /*  alpha small */
                        c = tonos ? '\u03ac' : '\u03b1';
                        break;
                    }
                    case 0xc1: {
                        /*  alpha capital */
                        c = tonos ? '\u0386' : '\u0391';
                        break;
                    }
                    case 0xe2: {
                         /*  Beta small */
                        c = '\u03b2';
                        break;
                    }
                    case 0xc2: {
                        /*  Beta capital */
                        c = '\u0392';
                        break;
                    }
                    case 0xe4: {
                        /*  Gamma small */
                        c = '\u03b3';
                        break;
                    }
                    case 0xc4: {
                        /*  Gamma capital */
                        c = '\u0393';
                        break;
                    }
                    case 0xe5: {
                        /*  Delta small */
                        c = '\u03b4';
                        break;
                    }
                    case 0xc5: {
                        /*  Delta capital */
                        c = '\u0394';
                        break;
                    }
                    case 0xe6: {
                        /*  epsilon small */
                        c = tonos ? '\u03ad' : '\u03b5';
                        break;
                    }
                    case 0xc6: {
                        /*  epsilon capital */
                        c = tonos ? '\u0388' : '\u0395';
                        break;
                    }
                    case 0xe9: {
                        /*  Zeta small */
                        c = '\u03b6';
                        break;
                    }
                    case 0xc9: {
                        /*  Zeta capital */
                        c = '\u0396';
                        break;
                    }
                    case 0xea: {
                        /*  Eta small */
                        c = tonos ? '\u03ae' : '\u03b7';
                        break;
                    }
                    case 0xca: {
                        /*  Eta capital */
                        c = tonos ? '\u0389' : '\u0397';
                        break;
                    }
                    case 0xeb: {
                        /*  Theta small */
                        c = '\u03b8';
                        break;
                    }
                    case 0xcb: {
                        /*  Theta capital */
                        c = '\u0398';
                        break;
                    }
                    case 0xec: {
                        /*  Iota small */
                        if (tonos) {
                            if (dialitika) {
                                c = '\u0390';
                            } else {
                                c = '\u03af';
                            }
                        } else if (dialitika) {
                            c = '\u03ca';
                        } else {
                            c = '\u03b9';
                        }
                        break;
                    }
                    case 0xcc: {
                        /*  Iota capital */
                        if (tonos) {
                            c = '\u038a';
                        } else if (dialitika) {
                            c = '\u03aa';
                        } else {
                            c = '\u0399';
                        }
                        break;
                    }
                    case 0xed: {
                        /*  Kappa small */
                        c = '\u03ba';
                        break;
                    }
                    case 0xcd: {
                        /*  Kappa capital */
                        c = '\u039a';
                        break;
                    }
                    case 0xee: {
                        /*  Lambda small */
                        c = '\u03bb';
                        break;
                    }
                    case 0xce: {
                        /*  Lambda capital */
                        c = '\u039b';
                        break;
                    }
                    case 0xef: {
                        /*  Mu small */
                        c = '\u03bc';
                        break;
                    }
                    case 0xcf:
                        /*  Mu capital */
                        c = '\u039c';
                        break;
                    case 0xf0: {
                        /*  Nu small */
                        c = '\u03bd';
                        break;
                    }
                    case 0xd0: {
                        /*  Nu capital */
                        c = '\u039d';
                        break;
                    }
                    case 0xf1: {
                        /*  Xi small */
                        c = '\u03be';
                        break;
                    }
                    case 0xd1: {
                        /*  Xi capital */
                        c = '\u039e';
                        break;
                    }
                    case 0xf2: {
                        /*  Omicron small */
                        if (tonos) {
                            c = '\u03cc';
                        } else {
                            c = '\u03bf';
                        }
                        break;
                    }
                    case 0xd2: {
                        /*  Omicron capital */
                        if (tonos) {
                            c = '\u038c';
                        } else {
                            c = '\u039f';
                        }
                        break;
                    }
                    case 0xf3: {
                        /*  Pi small */
                        c = '\u03c0';
                        break;
                    }
                    case 0xd3: {
                        /*  Pi capital */
                        c = '\u03a0';
                        break;
                    }
                    case 0xf5: {
                        /*  Rho small */
                        c = '\u03c1';
                        break;
                    }
                    case 0xd5: {
                        /*  Rho capital */
                        c = '\u03a1';
                        break;
                    }
                    case 0xf7: {
                        /*  Sigma small (end of words) */
                        c = '\u03c2';
                        break;
                    }
                    case 0xf6: {
                        /*  Sigma small */
                        c = '\u03c3';
                        break;
                    }
                    case 0xd6: {
                        /*  Sigma capital */
                        c = '\u03a3';
                        break;
                    }
                    case 0xf8: {
                        /*  Tau small */
                        c = '\u03c4';
                        break;
                    }
                    case 0xd8: {
                        /*  Tau capital */
                        c = '\u03a4';
                        break;
                    }
                    case 0xf9: {
                        /*  Upsilon small */
                        if (tonos) {
                            if (dialitika) {
                                c = '\u03b0';
                            } else {
                                c = '\u03cd';
                            }
                        } else if (dialitika) {
                            c = '\u03cb';
                        } else {
                            c = '\u03c5';
                        }
                        break;
                    }
                    case 0xd9: {
                        /*  Upsilon capital */
                        if (tonos) {
                            c = '\u038e';
                        } else if (dialitika) {
                            c = '\u03ab';
                        } else {
                            c = '\u03a5';
                        }
                        break;
                    }
                    case 0xfa: {
                        /*  Phi small */
                        c = '\u03c6';
                        break;
                    }
                    case 0xda: {
                        /*  Phi capital */
                        c = '\u03a6';
                        break;
                    }
                    case 0xfb: {
                        /*  Chi small */
                        c = '\u03c7';
                        break;
                    }
                    case 0xdb: {
                        /*  Chi capital */
                        c = '\u03a7';
                        break;
                    }
                    case 0xfc: {
                        /*  Psi small */
                        c = '\u03c8';
                        break;
                    }
                    case 0xdc: {
                        /*  Psi capital */
                        c = '\u03a8';
                        break;
                    }
                    case 0xfd: {
                        /*  Omega small */
                        if (tonos) {
                            c = '\u03ce';
                        } else {
                            c = '\u03c9';
                        }
                        break;
                    }
                    case 0xdd: {
                        /*  Omega capital */
                        if (tonos) {
                            c = '\u038f';
                        } else {
                            c = '\u03a9';
                        }
                        break;
                    }
                    default: {
                        c = (char) b;
                    }
                }
                out.put(c);
            }

            return CoderResult.UNDERFLOW;
        }
    }
}
