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
package org.xbib.query.cql;

import java.io.IOException;

class CQLLexer implements CQLTokens {

  /** This character denotes the endStream of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;
  public static final int STRING2 = 2;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0,  0,  1, 1
  };

  /** 
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED = 
    "\11\0\1\20\1\1\2\0\1\2\22\0\1\20\1\0\1\21\5\0"+
    "\1\3\1\4\4\0\1\30\1\40\1\36\11\37\2\0\1\22\1\24"+
    "\1\23\2\0\1\5\1\16\1\25\1\7\1\31\2\0\1\34\1\33"+
    "\1\35\1\0\1\27\1\0\1\6\1\10\1\13\1\26\1\11\1\15"+
    "\1\12\2\0\1\32\1\14\1\17\2\0\1\41\4\0\1\5\1\16"+
    "\1\25\1\7\1\31\2\0\1\34\1\33\1\35\1\0\1\27\1\0"+
    "\1\6\1\10\1\13\1\26\1\11\1\15\1\12\2\0\1\32\1\14"+
    "\1\17\uff86\0";

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\2\0\1\1\2\2\1\3\1\4\5\1\1\5\1\6"+
    "\1\7\1\10\1\11\3\1\2\12\1\13\1\14\1\15"+
    "\1\14\4\1\1\16\2\1\1\17\1\20\1\21\1\22"+
    "\6\1\1\23\1\24\1\25\1\26\6\1\1\27\1\30"+
    "\11\1\1\31\3\1";

  private static int [] zzUnpackAction() {
    int [] result = new int[68];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\42\0\104\0\104\0\146\0\210\0\210\0\252"+
    "\0\314\0\356\0\u0110\0\u0132\0\210\0\210\0\u0154\0\u0176"+
    "\0\u0198\0\u01ba\0\u01dc\0\u01fe\0\u0220\0\u0242\0\210\0\210"+
    "\0\210\0\u0264\0\u0286\0\u02a8\0\u02ca\0\u02ec\0\104\0\u030e"+
    "\0\u0330\0\210\0\210\0\210\0\210\0\u0352\0\u0374\0\u0396"+
    "\0\u03b8\0\u03da\0\u0220\0\210\0\104\0\104\0\104\0\u03fc"+
    "\0\u041e\0\u0440\0\u0462\0\u0484\0\u04a6\0\u03da\0\104\0\u04c8"+
    "\0\u04ea\0\u050c\0\u052e\0\u0550\0\u0572\0\u0594\0\u05b6\0\u05d8"+
    "\0\104\0\u05fa\0\u061c\0\u063e";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[68];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\3"+
    "\1\12\2\3\1\13\1\3\1\14\2\3\1\15\1\16"+
    "\1\17\1\20\1\21\1\22\3\3\1\23\1\24\3\3"+
    "\1\25\1\26\1\27\1\3\21\30\1\31\17\30\1\32"+
    "\3\3\2\0\13\3\5\0\13\3\1\0\2\3\1\4"+
    "\1\3\2\0\13\3\5\0\13\3\1\0\1\3\42\0"+
    "\3\3\2\0\1\3\1\33\1\34\10\3\5\0\2\3"+
    "\1\35\10\3\1\0\4\3\2\0\3\3\1\36\7\3"+
    "\5\0\13\3\1\0\4\3\2\0\4\3\1\37\6\3"+
    "\5\0\13\3\1\0\4\3\2\0\4\3\1\40\6\3"+
    "\5\0\13\3\1\0\4\3\2\0\3\3\1\41\7\3"+
    "\5\0\13\3\1\0\1\3\23\0\1\42\1\43\41\0"+
    "\1\44\41\0\1\45\15\0\3\3\2\0\13\3\5\0"+
    "\1\3\1\46\11\3\1\0\4\3\2\0\1\3\1\47"+
    "\5\3\1\50\3\3\5\0\13\3\1\0\4\3\2\0"+
    "\13\3\5\0\6\3\1\51\4\3\1\0\4\3\2\0"+
    "\13\3\5\0\3\3\1\52\5\3\2\53\1\0\4\3"+
    "\2\0\13\3\5\0\3\3\1\52\5\3\2\26\1\0"+
    "\1\3\21\0\1\54\20\0\3\3\2\0\2\3\1\55"+
    "\7\3\1\56\5\0\13\3\1\0\4\3\2\0\13\3"+
    "\5\0\10\3\1\56\2\3\1\0\4\3\2\0\13\3"+
    "\5\0\2\3\1\56\10\3\1\0\4\3\2\0\5\3"+
    "\1\57\5\3\5\0\13\3\1\0\4\3\2\0\3\3"+
    "\1\60\7\3\5\0\13\3\1\0\4\3\2\0\4\3"+
    "\1\61\6\3\5\0\13\3\1\0\4\3\2\0\13\3"+
    "\5\0\2\3\1\62\10\3\1\0\4\3\2\0\13\3"+
    "\5\0\1\63\12\3\1\0\4\3\2\0\1\64\12\3"+
    "\5\0\13\3\1\0\4\3\2\0\5\3\1\65\5\3"+
    "\5\0\13\3\1\0\4\3\2\0\13\3\5\0\11\3"+
    "\2\66\1\0\4\3\2\0\7\3\1\67\3\3\5\0"+
    "\13\3\1\0\4\3\2\0\5\3\1\70\5\3\5\0"+
    "\13\3\1\0\4\3\2\0\13\3\5\0\3\3\1\71"+
    "\7\3\1\0\4\3\2\0\13\3\5\0\2\3\1\72"+
    "\10\3\1\0\4\3\2\0\13\3\5\0\1\73\12\3"+
    "\1\0\4\3\2\0\13\3\5\0\7\3\1\74\3\3"+
    "\1\0\4\3\2\0\11\3\1\75\1\3\5\0\13\3"+
    "\1\0\4\3\2\0\1\76\12\3\5\0\4\3\1\23"+
    "\1\24\5\3\1\0\4\3\2\0\3\3\1\77\7\3"+
    "\5\0\13\3\1\0\4\3\2\0\5\3\1\56\5\3"+
    "\5\0\13\3\1\0\4\3\2\0\13\3\5\0\6\3"+
    "\1\100\4\3\1\0\4\3\2\0\12\3\1\101\5\0"+
    "\13\3\1\0\4\3\2\0\1\3\1\102\1\34\10\3"+
    "\5\0\2\3\1\35\10\3\1\0\4\3\2\0\10\3"+
    "\1\103\2\3\5\0\13\3\1\0\4\3\2\0\1\3"+
    "\1\56\11\3\5\0\13\3\1\0\4\3\2\0\12\3"+
    "\1\56\5\0\13\3\1\0\4\3\2\0\13\3\5\0"+
    "\4\3\1\104\6\3\1\0\4\3\2\0\10\3\1\56"+
    "\2\3\5\0\13\3\1\0\1\3";

  private static int [] zzUnpackTrans() {
    int [] result = new int[1632];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unkown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\2\0\3\1\2\11\5\1\2\11\10\1\3\11\10\1"+
    "\4\11\6\1\1\11\30\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[68];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private char zzBuffer[] = new char[ZZ_BUFFERSIZE];

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** number of newlines encountered up to the start of the matched text */
  private int yyline;

  /** the number of characters up to the start of the matched text */
  private int yychar;

  /**
   * the number of characters from the last newline up to the start of the 
   * matched text
   */
  private int yycolumn;

  /** 
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean zzEOFDone;

  /* user code: */
    private Object yylval;
    private int token;
    private StringBuilder sb = new StringBuilder();

    public int getToken() {        
        return token;
    }
    
    public int nextToken() {
        try {
            token = yylex();            
            return token;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Object getSemantic() {
        return yylval;
    }
    
    public int getLine() {
        return yyline;
    }
    
    public int getColumn() {
        return yycolumn;
    }
    


  /**
   * Creates a new scanner
   * There is also a java.io.InputStream version of this constructor.
   *
   * @param   in  the java.io.Reader to read input from.
   */
  CQLLexer(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
  CQLLexer(java.io.InputStream in) {
    this(new java.io.InputStreamReader(in));
  }

  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    char [] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 144) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }


  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   * 
   * @exception   IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws IOException {

    /* first: make room (if you can) */
    if (zzStartRead > 0) {
      System.arraycopy(zzBuffer, zzStartRead,
                       zzBuffer, 0,
                       zzEndRead-zzStartRead);

      /* translate stored positions */
      zzEndRead-= zzStartRead;
      zzCurrentPos-= zzStartRead;
      zzMarkedPos-= zzStartRead;
      zzStartRead = 0;
    }

    /* is the buffer big enough? */
    if (zzCurrentPos >= zzBuffer.length) {
      /* if not: blow it up */
      char newBuffer[] = new char[zzCurrentPos*2];
      System.arraycopy(zzBuffer, 0, newBuffer, 0, zzBuffer.length);
      zzBuffer = newBuffer;
    }

    /* finally: fill the buffer with new input */
    int numRead = zzReader.read(zzBuffer, zzEndRead,
                                            zzBuffer.length-zzEndRead);

    if (numRead > 0) {
      zzEndRead+= numRead;
      return false;
    }
    // unlikely but not impossible: read 0 characters, but not at end of stream
    if (numRead == 0) {
      int c = zzReader.read();
      if (c == -1) {
        return true;
      } else {
        zzBuffer[zzEndRead++] = (char) c;
        return false;
      }
    }

	// numRead < 0
    return true;
  }


  /**
   * Closes the input stream.
   */
  public final void yyclose() throws IOException {
    zzAtEOF = true;            /* indicate endStream of file */
    zzEndRead = zzStartRead;  /* invalidate buffer    */

    if (zzReader != null)
      zzReader.close();
  }


  /**
   * Resets the scanner to read from a new input stream.
   * Does not close the old reader.
   *
   * All internal variables are reset, the old input stream
   * <b>cannot</b> be reused (internal buffer is discarded and lost).
   * Lexical state is set to <tt>ZZ_INITIAL</tt>.
   *
   * @param reader   the new input stream
   */
  public final void yyreset(java.io.Reader reader) {
    zzReader = reader;
    zzAtBOL  = true;
    zzAtEOF  = false;
    zzEOFDone = false;
    zzEndRead = zzStartRead = 0;
    zzCurrentPos = zzMarkedPos = 0;
    yyline = yychar = yycolumn = 0;
    zzLexicalState = YYINITIAL;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final String yytext() {
    return new String( zzBuffer, zzStartRead, zzMarkedPos-zzStartRead );
  }


  /**
   * Returns the character at position <tt>pos</tt> from the
   * matched text.
   *
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch.
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer[zzStartRead+pos];
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of
   * yypushback(int) and a match-all fallback rule) this method
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the endStream of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   IOException  if any I/O-Error occurs
   */
  public int yylex() throws IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char [] zzBufferL = zzBuffer;
    char [] zzCMapL = ZZ_CMAP;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      boolean zzR = false;
      for (zzCurrentPosL = zzStartRead; zzCurrentPosL < zzMarkedPosL;
                                                             zzCurrentPosL++) {
        switch (zzBufferL[zzCurrentPosL]) {
        case '\u000B':
        case '\u000C':
        case '\u0085':
        case '\u2028':
        case '\u2029':
          yyline++;
          yycolumn = 0;
          zzR = false;
          break;
        case '\r':
          yyline++;
          yycolumn = 0;
          zzR = true;
          break;
        case '\n':
          if (zzR)
            zzR = false;
          else {
            yyline++;
            yycolumn = 0;
          }
          break;
        default:
          zzR = false;
          yycolumn++;
        }
      }

      if (zzR) {
        // peek one character ahead if it is \n (if we have counted one line too much)
        boolean zzPeek;
        if (zzMarkedPosL < zzEndReadL)
          zzPeek = zzBufferL[zzMarkedPosL] == '\n';
        else if (zzAtEOF)
          zzPeek = false;
        else {
          boolean eof = zzRefill();
          zzEndReadL = zzEndRead;
          zzMarkedPosL = zzMarkedPos;
          zzBufferL = zzBuffer;
          if (eof) 
            zzPeek = false;
          else 
            zzPeek = zzBufferL[zzMarkedPosL] == '\n';
        }
        if (zzPeek) yyline--;
      }
      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;
  
      zzState = ZZ_LEXSTATE[zzLexicalState];


      zzForAction: {
        while (true) {
    
          if (zzCurrentPosL < zzEndReadL)
            zzInput = zzBufferL[zzCurrentPosL++];
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = zzBufferL[zzCurrentPosL++];
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMapL[zzInput] ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          int zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
        case 8: 
          { yylval = yytext();
        return GT;
          }
        case 26: break;
        case 12: 
          { sb.append(yytext());
          }
        case 27: break;
        case 6: 
          { yybegin(STRING2); 
        sb.setLength(0);
          }
        case 28: break;
        case 19: 
          { sb.append("\"");
          }
        case 29: break;
        case 18: 
          { yylval = yytext();
        return EXACT;
          }
        case 30: break;
        case 25: 
          { yylval = yytext();
        return SORTBY;
          }
        case 31: break;
        case 10: 
          { yylval = Long.parseLong(yytext());
        return INTEGER;
          }
        case 32: break;
        case 11: 
          { yylval = yytext();
        return SLASH;
          }
        case 33: break;
        case 24: 
          { yylval = yytext();
        return PROX;
          }
        case 34: break;
        case 23: 
          { yylval = Double.parseDouble(yytext());
        return FLOAT;
          }
        case 35: break;
        case 4: 
          { yylval = yytext();
        return RPAR;
          }
        case 36: break;
        case 1: 
          { yylval = yytext();
        return SIMPLESTRING;
          }
        case 37: break;
        case 16: 
          { yylval = yytext();
        return LE;
          }
        case 38: break;
        case 14: 
          { yylval = yytext();
        return OR;
          }
        case 39: break;
        case 7: 
          { yylval = yytext();
        return LT;
          }
        case 40: break;
        case 3: 
          { yylval = yytext();
        return LPAR;
          }
        case 41: break;
        case 15: 
          { yylval = yytext();
        return NE;
          }
        case 42: break;
        case 21: 
          { yylval = yytext();
        return NAMEDCOMPARITORS;
          }
        case 43: break;
        case 22: 
          { yylval = yytext();
        return NOT;
          }
        case 44: break;
        case 9: 
          { yylval = yytext();
        return EQ;
          }
        case 45: break;
        case 17: 
          { yylval = yytext();
        return GE;
          }
        case 46: break;
        case 20: 
          { yylval = yytext();
        return AND;
          }
        case 47: break;
        case 5: 
          { 
          }
        case 48: break;
        case 13: 
          { yybegin(YYINITIAL);
        yylval = sb.toString();
        return QUOTEDSTRING;
          }
        case 49: break;
        case 2: 
          { return NL;
          }
        case 50: break;
        default: 
          if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
            zzAtEOF = true;
              {     return 0; 
 }
          } 
          else {
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
