package org.xbib.charset;

import java.io.CharConversionException;

public class ByteToCharUSM94 extends LocalByteConverter {

// USM-94 To UNICODE Character Mapping
//  Basic and Extended Latin Character Sets (ASCII and ANSEL)
//  Greek Symbols
//  Latin Subscript characters
//  Latin Superscript characters
//  Basic Hebrew Character Set
//  Basic and Extended Cyrillic Character Sets
//  Basic and Extended Arabic Character Sets
//  EACC (CJK)

    private static final char[] ascii = {
            0x0000,    // 00 NULL
            0x0001,    // 01 START OF HEADING
            0x0002,    // 02 START OF TEXT
            0x0003,    // 03 END OF TEXT
            0x0004,    // 04 END OF TRANSMISSION
            0x0005,    // 05 ENQUIRY
            0x0006,    // 06 ACKNOWLEDGE
            0x0007,    // 07 BELL
            0x0008,    // 08 BACKSPACE
            0x0009,    // 09 HORIZONTAL TABULATION
            0x000A,    // 0A LINE FEED
            0x000B,    // 0B VERTICAL TABULATION
            0x000C,    // 0C FORM FEED
            0x200D,    // 0D ZERO WIDTH JOINER
            0x200C,    // 0E ZERO WIDTH NON-JOINER
            0x000F,    // 0F SHIFT IN
            0x0010,    // 10 DATA LINK ESCAPE
            0x0011,    // 11 DEVICE CONTROL ONE
            0x0012,    // 12 DEVICE CONTROL TWO
            0x0013,    // 13 DEVICE CONTROL THREE
            0x0014,    // 14 DEVICE CONTROL FOUR
            0x0015,    // 15 NEGATIVE ACKNOWLEDGE
            0x0016,    // 16 SYNCHRONOUS IDLE
            0x0017,    // 17 END OF TRANSMISSION BLOCK
            0x0018,    // 18 CANCEL
            0x0019,    // 19 END OF MEDIUM
            0x001A,    // 1A SUBSTITUTE
            0x001B,    // 1B ESCAPE
            0x001C,    // 1C FILE SEPARATOR
            0x001D,    // 1D GROUP SEPARATOR
            0x001E,    // 1E RECORD SEPARATOR
            0x001F,    // 1F UNIT SEPARATOR
            0x0020,    // 20 SPACE
            0x0021,    // 21 EXCLAMATION MARK
            0x0022,    // 22 QUOTATION MARK
            0x0023,    // 23 NUMBER SIGN
            0x0024,    // 24 DOLLAR SIGN
            0x0025,    // 25 PERCENT SIGN
            0x0026,    // 26 AMPERSAND
            0x0027,    // 27 APOSTROPHE
            0x0028,    // 28 LEFT PARENTHESIS
            0x0029,    // 29 RIGHT PARENTHESIS
            0x002A,    // 2A ASTERISK
            0x002B,    // 2B PLUS SIGN
            0x002C,    // 2C COMMA
            0x002D,    // 2D HYPHEN-MINUS
            0x002E,    // 2E FULL STOP
            0x002F,    // 2F SOLIDUS
            0x0030,    // 30 DIGIT ZERO
            0x0031,    // 31 DIGIT ONE
            0x0032,    // 32 DIGIT TWO
            0x0033,    // 33 DIGIT THREE
            0x0034,    // 34 DIGIT FOUR
            0x0035,    // 35 DIGIT FIVE
            0x0036,    // 36 DIGIT SIX
            0x0037,    // 37 DIGIT SEVEN
            0x0038,    // 38 DIGIT EIGHT
            0x0039,    // 39 DIGIT NINE
            0x003A,    // 3A COLON
            0x003B,    // 3B SEMICOLON
            0x003C,    // 3C LESS-THAN SIGN
            0x003D,    // 3D EQUALS SIGN
            0x003E,    // 3E GREATER-THAN SIGN
            0x003F,    // 3F QUESTION MARK
            0x0040,    // COMMERCIAL AT
            0x0041,    // CAPITAL A
            0x0042,    // CAPITAL B
            0x0043,    // CAPITAL C
            0x0044,    // CAPITAL D
            0x0045,    // CAPITAL E
            0x0046,    // CAPITAL F
            0x0047,    // CAPITAL G
            0x0048,    // CAPITAL H
            0x0049,    // CAPITAL I
            0x004A,    // CAPITAL J
            0x004B,    // CAPITAL K
            0x004C,    // CAPITAL L
            0x004D,    // CAPITAL M
            0x004E,    // CAPITAL N
            0x004F,    // CAPITAL O
            0x0050,    // CAPITAL P
            0x0051,    // CAPITAL Q
            0x0052,    // CAPITAL R
            0x0053,    // CAPITAL S
            0x0054,    // CAPITAL T
            0x0055,    // CAPITAL U
            0x0056,    // CAPITAL V
            0x0057,    // CAPITAL W
            0x0058,    // CAPITAL X
            0x0059,    // CAPITAL Y
            0x005A,    // CAPITAL Z
            0x005B,    // LEFT SQUARE BRACKET
            0x005C,    // REVERSE SOLIDUS
            0x005D,    // RIGHT SQUARE BRACKET
            0x005E,    // CIRCUMFLEX ACCENT (SPACING)
            0x005F,    // LOW LINE
            0x0060,    // GRAVE (SPACING)
            0x0061,    // SMALL A
            0x0062,    // SMALL B
            0x0063,    // SMALL C
            0x0064,    // SMALL D
            0x0065,    // SMALL E
            0x0066,    // SMALL F
            0x0067,    // SMALL G
            0x0068,    // SMALL H
            0x0069,    // SMALL I
            0x006A,    // SMALL J
            0x006B,    // SMALL K
            0x006C,    // SMALL L
            0x006D,    // SMALL M
            0x006E,    // SMALL N
            0x006F,    // SMALL O
            0x0070,    // SMALL P
            0x0071,    // SMALL Q
            0x0072,    // SMALL R
            0x0073,    // SMALL S
            0x0074,    // SMALL T
            0x0075,    // SMALL U
            0x0076,    // SMALL V
            0x0077,    // SMALL W
            0x0078,    // SMALL X
            0x0079,    // SMALL Y
            0x007A,    // SMALL Z
            0x007B,    // LEFT CURLY BRACKET
            0x007C,    // VERTICAL LINE
            0x007D,    // RIGHT CURLY BRACKET
            0x007E,    // TILDE (SPACING)
            0x007F     // DELETE
    };
    private static final char[] ansel = {
            0x0080,    // RESERVED CONTROL CODE
            0x0081,    // RESERVED CONTROL CODE
            0x0082,    // RESERVED CONTROL CODE
            0x0083,    // RESERVED CONTROL CODE
            0x0084,    // RESERVED CONTROL CODE
            0x0085,    // RESERVED CONTROL CODE
            0x0086,    // RESERVED CONTROL CODE
            0x0087,    // RESERVED CONTROL CODE
            0x0088,    // RESERVED CONTROL CODE
            0x0089,    // RESERVED CONTROL CODE
            0x008A,    // RESERVED CONTROL CODE
            0x008B,    // RESERVED CONTROL CODE
            0x008C,    // RESERVED CONTROL CODE
            0x200D,    // 8D ZERO WIDTH JOINER
            0x200C,    // 8E ZERO WIDTH NON-JOINER
            0x008F,    // RESERVED CONTROL CODE
            0x0090,    // RESERVED CONTROL CODE
            0x0091,    // RESERVED CONTROL CODE
            0x0092,    // RESERVED CONTROL CODE
            0x0093,    // RESERVED CONTROL CODE
            0x0094,    // RESERVED CONTROL CODE
            0x0095,    // RESERVED CONTROL CODE
            0x0096,    // RESERVED CONTROL CODE
            0x0097,    // RESERVED CONTROL CODE
            0x0098,    // RESERVED CONTROL CODE
            0x0099,    // RESERVED CONTROL CODE
            0x009A,    // RESERVED CONTROL CODE
            0x009B,    // RESERVED CONTROL CODE
            0x009C,    // RESERVED CONTROL CODE
            0x009D,    // RESERVED CONTROL CODE
            0x009E,    // RESERVED CONTROL CODE
            0x009F,    // RESERVED CONTROL CODE
            0x0020,    // A0 REPLACEMENT CHARACTER
            0x0141,    // A1 CAPITAL L WITH STROKE
            0x00D8,    // A2 CAPITAL O WITH STROKE
            0x0110,    // A3 CAPITAL D WITH STROKE
            0x00DE,    // A4 CAPITAL THORN
            0x00C6,    // A5 CAPITAL LIGATURE AE
            0x0152,    // A6 CAPITAL LIGATURE OE
            0x02B9,    // A7 MODIFIER PRIME
            0x00B7,    // A8 MIDDLE DOT
            0x266D,    // A9 MUSIC FLAT SIGN
            0x00AE,    // AA REGISTERED SIGN
            0x00B1,    // AB PLUS-MINUS SIGN
            0x01A0,    // AC CAPITAL O HORN
            0x01AF,    // AD CAPITAL U HORN
            0x02BE,    // AE MODIFIER RIGHT HALF RING
            0xFFFD,    // AF REPLACEMENT CHARACTER
            0x02BB,    // B0 MODIFIER LETTER TURNED COMMA
            0x0142,    // B1 SMALL L WITH STROKE
            0x00F8,    // B2 SMALL O WITH STROKE
            0x0111,    // B3 SMALL D WITH STROKE
            0x00FE,    // B4 SMALL THORN
            0x00E6,    // B5 SMALL LIGATURE AE
            0x0153,    // B6 SMALL LIGATURE OE
            0x02BA,    // B7 MODIFIER DOUBLE PRIME
            0x0131,    // B8 SMALL DOTLESS I
            0x00A3,    // B9 POUND SIGN
            0x00F0,    // BA SMALL ETH
            0xFFFD,    // BB REPLACEMENT CHARACTER
            0x01A1,    // BC SMALL O HORN
            0x01B0,    // BD SMALL U HORN
            0x2113,    // BE is an illegal OCLC MARC character -> SCRIPT SMALL L
            0xFFFD,    // BF REPLACEMENT CHARACTER
            0x00B0,    // C0 DEGREE SIGN
            0x2113,    // C1 SCRIPT SMALL L
            0x2117,    // C2 SOUND RECORDING COPYRIGHT
            0x00A9,    // C3 COPYRIGHT SIGN
            0x266F,    // C4 MUSIC SHARP SIGN
            0x00BF,    // C5 INVERTED QUESTION MARK
            0x00A1,    // C6 INVERTED EXCLAMATION MARK
            0x20AC,    // C7 EURO SIGN
            0xFFFD,    // C8 LATIN SMALL LETTER SHARP S (ESSZETT)
            0x00DF,    // C9 REPLACEMENT CHARACTER
            0xFFFD,    // CA REPLACEMENT CHARACTER
            0xFFFD,    // CB REPLACEMENT CHARACTER
            0xFFFD,    // CC REPLACEMENT CHARACTER
            0xFFFD,    // CD REPLACEMENT CHARACTER
            0xFFFD,    // CE REPLACEMENT CHARACTER
            0xFFFD,    // CF REPLACEMENT CHARACTER
            0xFFFD,    // D0 REPLACEMENT CHARACTER
            0xFFFD,    // D1 REPLACEMENT CHARACTER
            0xFFFD,    // D2 REPLACEMENT CHARACTER
            0xFFFD,    // D3 REPLACEMENT CHARACTER
            0xFFFD,    // D4 REPLACEMENT CHARACTER
            0xFFFD,    // D5 REPLACEMENT CHARACTER
            0xFFFD,    // D6 REPLACEMENT CHARACTER
            0xFFFD,    // D7 REPLACEMENT CHARACTER
            0xFFFD,    // D8 REPLACEMENT CHARACTER
            0xFFFD,    // D9 REPLACEMENT CHARACTER
            0xFFFD,    // DA REPLACEMENT CHARACTER
            0xFFFD,    // DB REPLACEMENT CHARACTER
            0xFFFD,    // DC REPLACEMENT CHARACTER
            0xFFFD,    // DD REPLACEMENT CHARACTER
            0xFFFD,    // DE REPLACEMENT CHARACTER
            0xFFFD,    // DF REPLACEMENT CHARACTER
            0x0309,    // E0 COMBINING HOOK ABOVE
            0x0300,    // E1 COMBINING GRAVE ACCENT
            0x0301,    // E2 COMBINING ACUTE ACCENT
            0x0302,    // E3 COMBINING CIRCUMFLEX ACCENT
            0x0303,    // E4 COMBINING TILDE
            0x0304,    // E5 COMBINING MACRON
            0x0306,    // E6 COMBINING BREVE
            0x0307,    // E7 COMBINING DOT ABOVE
            0x0308,    // E8 COMBINING DIAERESIS
            0x030C,    // E9 COMBINING CARON
            0x030A,    // EA COMBINING RING ABOVE
            0xFE20,    // EB COMBINING LIGATURE LEFT HALF
            0xFE21,    // EC COMBINING LIGATURE RIGHT HALF
            0x0315,    // ED COMBINING COMMA ABOVE RIGHT
            0x030B,    // EE COMBINING DOUBLE ACUTE ACCENT
            0x0310,    // EF COMBINING CANDRABINDU
            0x0327,    // F0 COMBINING CEDILLA
            0x0328,    // F1 COMBINING OGONEK
            0x0323,    // F2 COMBINING DOT BELOW
            0x0324,    // F3 COMBINING DOUBLE DOT BELOW
            0x0325,    // F4 COMBINING RING BELOW
            0x0333,    // F5 COMBINING DOUBLE UNDERSCORE
            0x0332,    // F6 COMBINING UNDERSCORE
            0x0326,    // F7 COMBINING COMMA BELOW
            0x031C,    // F8 COMBINING LEFT HALF RING BELOW
            0x032E,    // F9 COMBINING BREVE BELOW
            0xFE22,    // FA COMBINING DOUBLE TILDE LEFT HALF
            0xFE23,    // FB COMBINING DOUBLE TILDE RIGHT HALF
            0xFFFD,    // FC REPLACEMENT CHARACTER
            0xFFFD,    // FD REPLACEMENT CHARACTER
            0x0313,    // FE COMBINING COMMA ABOVE
            0xFFFD     // FF REPLACEMENT CHARACTER
    };
    private static final char[] greek = {
            0xFFFD,    // 00 REPLACEMENT CHARACTER
            0xFFFD,    // 01 REPLACEMENT CHARACTER
            0xFFFD,    // 02 REPLACEMENT CHARACTER
            0xFFFD,    // 03 REPLACEMENT CHARACTER
            0xFFFD,    // 04 REPLACEMENT CHARACTER
            0xFFFD,    // 05 REPLACEMENT CHARACTER
            0xFFFD,    // 06 REPLACEMENT CHARACTER
            0xFFFD,    // 07 REPLACEMENT CHARACTER
            0xFFFD,    // 08 REPLACEMENT CHARACTER
            0xFFFD,    // 09 REPLACEMENT CHARACTER
            0xFFFD,    // 0A REPLACEMENT CHARACTER
            0xFFFD,    // 0B REPLACEMENT CHARACTER
            0xFFFD,    // 0C REPLACEMENT CHARACTER
            0x200D,    // 0D ZERO WIDTH JOINER
            0x200C,    // 0E ZERO WIDTH NON-JOINER
            0xFFFD,    // 0F REPLACEMENT CHARACTER
            0xFFFD,    // 10 REPLACEMENT CHARACTER
            0xFFFD,    // 11 REPLACEMENT CHARACTER
            0xFFFD,    // 12 REPLACEMENT CHARACTER
            0xFFFD,    // 13 REPLACEMENT CHARACTER
            0xFFFD,    // 14 REPLACEMENT CHARACTER
            0xFFFD,    // 15 REPLACEMENT CHARACTER
            0xFFFD,    // 16 REPLACEMENT CHARACTER
            0xFFFD,    // 17 REPLACEMENT CHARACTER
            0xFFFD,    // 18 REPLACEMENT CHARACTER
            0xFFFD,    // 19 REPLACEMENT CHARACTER
            0xFFFD,    // 1A REPLACEMENT CHARACTER
            0xFFFD,    // 1B REPLACEMENT CHARACTER
            0xFFFD,    // 1C REPLACEMENT CHARACTER
            0xFFFD,    // 1D REPLACEMENT CHARACTER
            0xFFFD,    // 1E REPLACEMENT CHARACTER
            0xFFFD,    // 1F REPLACEMENT CHARACTER
            0x0020,    // 20 REPLACEMENT CHARACTER
            0xFFFD,    // 21 REPLACEMENT CHARACTER
            0xFFFD,    // 22 REPLACEMENT CHARACTER
            0xFFFD,    // 23 REPLACEMENT CHARACTER
            0xFFFD,    // 24 REPLACEMENT CHARACTER
            0xFFFD,    // 25 REPLACEMENT CHARACTER
            0xFFFD,    // 26 REPLACEMENT CHARACTER
            0xFFFD,    // 27 REPLACEMENT CHARACTER
            0xFFFD,    // 28 REPLACEMENT CHARACTER
            0xFFFD,    // 29 REPLACEMENT CHARACTER
            0xFFFD,    // 2A REPLACEMENT CHARACTER
            0xFFFD,    // 2B REPLACEMENT CHARACTER
            0xFFFD,    // 2C REPLACEMENT CHARACTER
            0xFFFD,    // 2D REPLACEMENT CHARACTER
            0xFFFD,    // 2E REPLACEMENT CHARACTER
            0xFFFD,    // 2F REPLACEMENT CHARACTER
            0xFFFD,    // 30 REPLACEMENT CHARACTER
            0xFFFD,    // 31 REPLACEMENT CHARACTER
            0xFFFD,    // 32 REPLACEMENT CHARACTER
            0xFFFD,    // 33 REPLACEMENT CHARACTER
            0xFFFD,    // 34 REPLACEMENT CHARACTER
            0xFFFD,    // 35 REPLACEMENT CHARACTER
            0xFFFD,    // 36 REPLACEMENT CHARACTER
            0xFFFD,    // 37 REPLACEMENT CHARACTER
            0xFFFD,    // 38 REPLACEMENT CHARACTER
            0xFFFD,    // 39 REPLACEMENT CHARACTER
            0xFFFD,    // 3A REPLACEMENT CHARACTER
            0xFFFD,    // 3B REPLACEMENT CHARACTER
            0xFFFD,    // 3C REPLACEMENT CHARACTER
            0xFFFD,    // 3D REPLACEMENT CHARACTER
            0xFFFD,    // 3E REPLACEMENT CHARACTER
            0xFFFD,    // 3F REPLACEMENT CHARACTER
            0xFFFD,    // 40 REPLACEMENT CHARACTER
            0xFFFD,    // 41 REPLACEMENT CHARACTER
            0xFFFD,    // 42 REPLACEMENT CHARACTER
            0xFFFD,    // 43 REPLACEMENT CHARACTER
            0xFFFD,    // 44 REPLACEMENT CHARACTER
            0xFFFD,    // 45 REPLACEMENT CHARACTER
            0xFFFD,    // 46 REPLACEMENT CHARACTER
            0xFFFD,    // 47 REPLACEMENT CHARACTER
            0xFFFD,    // 48 REPLACEMENT CHARACTER
            0xFFFD,    // 49 REPLACEMENT CHARACTER
            0xFFFD,    // 4A REPLACEMENT CHARACTER
            0xFFFD,    // 4B REPLACEMENT CHARACTER
            0xFFFD,    // 4C REPLACEMENT CHARACTER
            0xFFFD,    // 4D REPLACEMENT CHARACTER
            0xFFFD,    // 4E REPLACEMENT CHARACTER
            0xFFFD,    // 4R REPLACEMENT CHARACTER
            0xFFFD,    // 50 REPLACEMENT CHARACTER
            0xFFFD,    // 51 REPLACEMENT CHARACTER
            0xFFFD,    // 52 REPLACEMENT CHARACTER
            0xFFFD,    // 53 REPLACEMENT CHARACTER
            0xFFFD,    // 54 REPLACEMENT CHARACTER
            0xFFFD,    // 55 REPLACEMENT CHARACTER
            0xFFFD,    // 56 REPLACEMENT CHARACTER
            0xFFFD,    // 57 REPLACEMENT CHARACTER
            0xFFFD,    // 58 REPLACEMENT CHARACTER
            0xFFFD,    // 59 REPLACEMENT CHARACTER
            0xFFFD,    // 5A REPLACEMENT CHARACTER
            0xFFFD,    // 5B REPLACEMENT CHARACTER
            0xFFFD,    // 5C REPLACEMENT CHARACTER
            0xFFFD,    // 5D REPLACEMENT CHARACTER
            0xFFFD,    // 5E REPLACEMENT CHARACTER
            0xFFFD,    // 5F REPLACEMENT CHARACTER
            0xFFFD,    // 60 REPLACEMENT CHARACTER
            0x03B1,    // 61 GREEK SMALL ALPHA
            0x03B2,    // 62 GREEK SMALL BETA
            0x03B3,    // 63 GREEK SMALL GAMMA
            0xFFFD,    // 64 REPLACEMENT CHARACTER
            0xFFFD,    // 65 REPLACEMENT CHARACTER
            0xFFFD,    // 66 REPLACEMENT CHARACTER
            0xFFFD,    // 67 REPLACEMENT CHARACTER
            0xFFFD,    // 68 REPLACEMENT CHARACTER
            0xFFFD,    // 69 REPLACEMENT CHARACTER
            0xFFFD,    // 6A REPLACEMENT CHARACTER
            0xFFFD,    // 6B REPLACEMENT CHARACTER
            0xFFFD,    // 6C REPLACEMENT CHARACTER
            0xFFFD,    // 6D REPLACEMENT CHARACTER
            0xFFFD,    // 6E REPLACEMENT CHARACTER
            0xFFFD,    // 6F REPLACEMENT CHARACTER
            0xFFFD,    // 70 REPLACEMENT CHARACTER
            0xFFFD,    // 71 REPLACEMENT CHARACTER
            0xFFFD,    // 72 REPLACEMENT CHARACTER
            0xFFFD,    // 73 REPLACEMENT CHARACTER
            0xFFFD,    // 74 REPLACEMENT CHARACTER
            0xFFFD,    // 75 REPLACEMENT CHARACTER
            0xFFFD,    // 76 REPLACEMENT CHARACTER
            0xFFFD,    // 77 REPLACEMENT CHARACTER
            0xFFFD,    // 78 REPLACEMENT CHARACTER
            0xFFFD,    // 79 REPLACEMENT CHARACTER
            0xFFFD,    // 7A REPLACEMENT CHARACTER
            0xFFFD,    // 7B REPLACEMENT CHARACTER
            0xFFFD,    // 7C REPLACEMENT CHARACTER
            0xFFFD,    // 7D REPLACEMENT CHARACTER
            0xFFFD,    // 7E REPLACEMENT CHARACTER
            0xFFFD,    // 7F REPLACEMENT CHARACTER
    };
    private static final char[] subscript = {
            0xFFFD,    // 00 REPLACEMENT CHARACTER
            0xFFFD,    // 01 REPLACEMENT CHARACTER
            0xFFFD,    // 02 REPLACEMENT CHARACTER
            0xFFFD,    // 03 REPLACEMENT CHARACTER
            0xFFFD,    // 04 REPLACEMENT CHARACTER
            0xFFFD,    // 05 REPLACEMENT CHARACTER
            0xFFFD,    // 06 REPLACEMENT CHARACTER
            0xFFFD,    // 07 REPLACEMENT CHARACTER
            0xFFFD,    // 08 REPLACEMENT CHARACTER
            0xFFFD,    // 09 REPLACEMENT CHARACTER
            0xFFFD,    // 0A REPLACEMENT CHARACTER
            0xFFFD,    // 0B REPLACEMENT CHARACTER
            0xFFFD,    // 0C REPLACEMENT CHARACTER
            0x200D,    // 0D ZERO WIDTH JOINER
            0x200C,    // 0E ZERO WIDTH NON-JOINER
            0xFFFD,    // 0F REPLACEMENT CHARACTER
            0xFFFD,    // 10 REPLACEMENT CHARACTER
            0xFFFD,    // 11 REPLACEMENT CHARACTER
            0xFFFD,    // 12 REPLACEMENT CHARACTER
            0xFFFD,    // 13 REPLACEMENT CHARACTER
            0xFFFD,    // 14 REPLACEMENT CHARACTER
            0xFFFD,    // 15 REPLACEMENT CHARACTER
            0xFFFD,    // 16 REPLACEMENT CHARACTER
            0xFFFD,    // 17 REPLACEMENT CHARACTER
            0xFFFD,    // 18 REPLACEMENT CHARACTER
            0xFFFD,    // 19 REPLACEMENT CHARACTER
            0xFFFD,    // 1A REPLACEMENT CHARACTER
            0xFFFD,    // 1B REPLACEMENT CHARACTER
            0xFFFD,    // 1C REPLACEMENT CHARACTER
            0xFFFD,    // 1D REPLACEMENT CHARACTER
            0xFFFD,    // 1E REPLACEMENT CHARACTER
            0xFFFD,    // 1F REPLACEMENT CHARACTER
            0x0020,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0x208D,    // SUBSCRIPT LEFT PARENTHESIS
            0x208E,    // SUBSCRIPT RIGHT PARENTHESIS
            0xFFFD,    // REPLACEMENT CHARACTER
            0x208A,    // SUBSCRIPT PLUS SIGN
            0xFFFD,    // REPLACEMENT CHARACTER
            0x208B,    // SUBSCRIPT MINUS SIGN
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0x2080,    // SUBSCRIPT ZERO
            0x2081,    // SUBSCRIPT ONE
            0x2082,    // SUBSCRIPT TWO
            0x2083,    // SUBSCRIPT THREE
            0x2084,    // SUBSCRIPT FOUR
            0x2085,    // SUBSCRIPT FIVE
            0x2086,    // SUBSCRIPT SIX
            0x2087,    // SUBSCRIPT SEVEN
            0x2088,    // SUBSCRIPT EIGHT
            0x2089,    // SUBSCRIPT NINE
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
    };
    private static final char[] superscript = {
            0xFFFD,    // 00 REPLACEMENT CHARACTER
            0xFFFD,    // 01 REPLACEMENT CHARACTER
            0xFFFD,    // 02 REPLACEMENT CHARACTER
            0xFFFD,    // 03 REPLACEMENT CHARACTER
            0xFFFD,    // 04 REPLACEMENT CHARACTER
            0xFFFD,    // 05 REPLACEMENT CHARACTER
            0xFFFD,    // 06 REPLACEMENT CHARACTER
            0xFFFD,    // 07 REPLACEMENT CHARACTER
            0xFFFD,    // 08 REPLACEMENT CHARACTER
            0xFFFD,    // 09 REPLACEMENT CHARACTER
            0xFFFD,    // 0A REPLACEMENT CHARACTER
            0xFFFD,    // 0B REPLACEMENT CHARACTER
            0xFFFD,    // 0C REPLACEMENT CHARACTER
            0x200D,    // 0D ZERO WIDTH JOINER
            0x200C,    // 0E ZERO WIDTH NON-JOINER
            0xFFFD,    // 0F REPLACEMENT CHARACTER
            0xFFFD,    // 10 REPLACEMENT CHARACTER
            0xFFFD,    // 11 REPLACEMENT CHARACTER
            0xFFFD,    // 12 REPLACEMENT CHARACTER
            0xFFFD,    // 13 REPLACEMENT CHARACTER
            0xFFFD,    // 14 REPLACEMENT CHARACTER
            0xFFFD,    // 15 REPLACEMENT CHARACTER
            0xFFFD,    // 16 REPLACEMENT CHARACTER
            0xFFFD,    // 17 REPLACEMENT CHARACTER
            0xFFFD,    // 18 REPLACEMENT CHARACTER
            0xFFFD,    // 19 REPLACEMENT CHARACTER
            0xFFFD,    // 1A REPLACEMENT CHARACTER
            0xFFFD,    // 1B REPLACEMENT CHARACTER
            0xFFFD,    // 1C REPLACEMENT CHARACTER
            0xFFFD,    // 1D REPLACEMENT CHARACTER
            0xFFFD,    // 1E REPLACEMENT CHARACTER
            0xFFFD,    // 1F REPLACEMENT CHARACTER
            0x0020,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0x207D,    // SUPERSCRIPT LEFT PARENTHESIS
            0x207E,    // SUPERSCRIPT RIGHT PARENTHESIS
            0xFFFD,    // REPLACEMENT CHARACTER
            0x207A,    // SUPERSCRIPT PLUS SIGN
            0xFFFD,    // REPLACEMENT CHARACTER
            0x207B,    // SUPERSCRIPT MINUS SIGN
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0x2070,    // SUPERSCRIPT ZERO
            0x00B9,    // SUPERSCRIPT ONE
            0x00B2,    // SUPERSCRIPT TWO
            0x00B3,    // SUPERSCRIPT THREE
            0x2074,    // SUPERSCRIPT FOUR
            0x2075,    // SUPERSCRIPT FIVE
            0x2076,    // SUPERSCRIPT SIX
            0x2077,    // SUPERSCRIPT SEVEN
            0x2078,    // SUPERSCRIPT EIGHT
            0x2079,    // SUPERSCRIPT NINE
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
            0xFFFD,    // REPLACEMENT CHARACTER
    };
    private static final char[] hebrew = {
            0xFFFD,    // 00 REPLACEMENT CHARACTER
            0xFFFD,    // 01 REPLACEMENT CHARACTER
            0xFFFD,    // 02 REPLACEMENT CHARACTER
            0xFFFD,    // 03 REPLACEMENT CHARACTER
            0xFFFD,    // 04 REPLACEMENT CHARACTER
            0xFFFD,    // 05 REPLACEMENT CHARACTER
            0xFFFD,    // 06 REPLACEMENT CHARACTER
            0xFFFD,    // 07 REPLACEMENT CHARACTER
            0xFFFD,    // 08 REPLACEMENT CHARACTER
            0xFFFD,    // 09 REPLACEMENT CHARACTER
            0xFFFD,    // 0A REPLACEMENT CHARACTER
            0xFFFD,    // 0B REPLACEMENT CHARACTER
            0xFFFD,    // 0C REPLACEMENT CHARACTER
            0x200D,    // 0D ZERO WIDTH JOINER
            0x200C,    // 0E ZERO WIDTH NON-JOINER
            0xFFFD,    // 0F REPLACEMENT CHARACTER
            0xFFFD,    // 10 REPLACEMENT CHARACTER
            0xFFFD,    // 11 REPLACEMENT CHARACTER
            0xFFFD,    // 12 REPLACEMENT CHARACTER
            0xFFFD,    // 13 REPLACEMENT CHARACTER
            0xFFFD,    // 14 REPLACEMENT CHARACTER
            0xFFFD,    // 15 REPLACEMENT CHARACTER
            0xFFFD,    // 16 REPLACEMENT CHARACTER
            0xFFFD,    // 17 REPLACEMENT CHARACTER
            0xFFFD,    // 18 REPLACEMENT CHARACTER
            0xFFFD,    // 19 REPLACEMENT CHARACTER
            0xFFFD,    // 1A REPLACEMENT CHARACTER
            0xFFFD,    // 1B REPLACEMENT CHARACTER
            0xFFFD,    // 1C REPLACEMENT CHARACTER
            0xFFFD,    // 1D REPLACEMENT CHARACTER
            0xFFFD,    // 1E REPLACEMENT CHARACTER
            0xFFFD,    // 1F REPLACEMENT CHARACTER
            0x0020,    // 20 SPACE
            0x0021,    // 21 EXCLAMATION MARK
            0x05F4,    // 22 PUNCT GERSHAYIM
            0x0023,    // 23 NUMBER SIGN
            0x0024,    // 24 DOLLAR SIGN
            0x0025,    // 25 PERCENT SIGN
            0x0026,    // 26 AMPERSAND
            0x05F3,    // 27 PUNCT GERESH
            0x0028,    // 28 LEFT PARENTHESIS
            0x0029,    // 29 RIGHT PARENTHESIS
            0x002A,    // 2A ASTERISK
            0x002B,    // 2B PLUS SIGN
            0x002C,    // 2C COMMA
            0x05BE,    // 2D PUNCT MAQAF
            0x002E,    // 2E FULL STOP
            0x002F,    // 2F SOLIDUS
            0x0030,    // 30 DIGIT ZERO
            0x0031,    // 31 DIGIT ONE
            0x0032,    // 32 DIGIT TWO
            0x0033,    // 33 DIGIT THREE
            0x0034,    // 34 DIGIT FOUR
            0x0035,    // 35 DIGIT FIVE
            0x0036,    // 36 DIGIT SIX
            0x0037,    // 37 DIGIT SEVEN
            0x0038,    // 38 DIGIT EIGHT
            0x0039,    // 39 DIGIT NINE
            0x003A,    // 3A COLON
            0x003B,    // 3B SEMICOLON
            0x003C,    // 3C LESS-THAN SIGN
            0x003D,    // 3D EQUALS SIGN
            0x003E,    // 3E GREATER-THAN SIGN
            0x003F,    // 3F QUESTION MARK
            0x05B7,    // 40 POINT PATAH
            0x05B8,    // 41 POINT QAMATS
            0x05B6,    // 42 POINT SEGOL
            0x05B5,    // 43 POINT TSERE
            0x05B4,    // 44 POINT HIRIQ
            0x05B9,    // 45 POINT HOLAM
            0x05BB,    // 46 POINT QUBUTS
            0x05B0,    // 47 POINT SHEVA
            0x05B2,    // 48 POINT HATAF PATAH
            0x05B3,    // 49 POINT HATAF QAMATS
            0x05B1,    // 4A POINT HATAF SEGOL
            0x05BC,    // 4B POINT DAGESH OR MAPIQ
            0x05BF,    // 4C POINT RAFE
            0x05C1,    // 4D POINT SHIN DOT
            0xFB1E,    // 4E POINT JUDEO-SPANISH VARIKA
            0xFFFD,    // 4F REPLACEMENT CHARACTER
            0xFFFD,    // 50 REPLACEMENT CHARACTER
            0xFFFD,    // 51 REPLACEMENT CHARACTER
            0xFFFD,    // 52 REPLACEMENT CHARACTER
            0xFFFD,    // 53 REPLACEMENT CHARACTER
            0xFFFD,    // 54 REPLACEMENT CHARACTER
            0xFFFD,    // 55 REPLACEMENT CHARACTER
            0xFFFD,    // 56 REPLACEMENT CHARACTER
            0xFFFD,    // 57 REPLACEMENT CHARACTER
            0xFFFD,    // 58 REPLACEMENT CHARACTER
            0xFFFD,    // 59 REPLACEMENT CHARACTER
            0xFFFD,    // 5A REPLACEMENT CHARACTER
            0x005B,    // 5B LEFT SQUARE BRACKET
            0xFFFD,    // 5C REPLACEMENT CHARACTER
            0x005D,    // 5D RIGHT SQUARE BRACKET
            0xFFFD,    // 5E REPLACEMENT CHARACTER
            0xFFFD,    // 5F REPLACEMENT CHARACTER
            0x05D0,    // 60 ALEF
            0x05D1,    // 61 BET
            0x05D2,    // 62 GIMEL
            0x05D3,    // 63 DALET
            0x05D4,    // 64 HE
            0x05D5,    // 65 VAV
            0x05D6,    // 66 ZAYIN
            0x05D7,    // 67 HET
            0x05D8,    // 68 TET
            0x05D9,    // 69 YOD
            0x05DA,    // 6A FINAL KAF
            0x05DB,    // 6B KAF
            0x05DC,    // 6C LAMED
            0x05DD,    // 6D FINAL MEM
            0x05DE,    // 6E MEM
            0x05DF,    // 6F FINAL NUN
            0x05E0,    // 70 NUN
            0x05E1,    // 71 SAMEKH
            0x05E2,    // 72 AYIN
            0x05E3,    // 73 FINAL PE
            0x05E4,    // 74 PE
            0x05E5,    // 75 FINAL TSADI
            0x05E6,    // 76 TSADI
            0x05E7,    // 77 KOF
            0x05E8,    // 78 RESH
            0x05E9,    // 79 SHIN
            0x05EA,    // 7A TAV
            0x05F0,    // 7B LIGATURE YIDDISH DOUBLE VAV
            0x05F1,    // 7C LIGATURE YIDDISH VAV YOD
            0x05F2,    // 7D LIGATURE YIDDISH DOUBLE YOD
            0xFFFD,    // 7E REPLACEMENT CHARACTER
            0xFFFD,    // 7F REPLACEMENT CHARACTER
    };
    private static final char[] cyrillic = {
            0xFFFD,    // 00 REPLACEMENT CHARACTER
            0xFFFD,    // 01 REPLACEMENT CHARACTER
            0xFFFD,    // 02 REPLACEMENT CHARACTER
            0xFFFD,    // 03 REPLACEMENT CHARACTER
            0xFFFD,    // 04 REPLACEMENT CHARACTER
            0xFFFD,    // 05 REPLACEMENT CHARACTER
            0xFFFD,    // 06 REPLACEMENT CHARACTER
            0xFFFD,    // 07 REPLACEMENT CHARACTER
            0xFFFD,    // 08 REPLACEMENT CHARACTER
            0xFFFD,    // 09 REPLACEMENT CHARACTER
            0xFFFD,    // 0A REPLACEMENT CHARACTER
            0xFFFD,    // 0B REPLACEMENT CHARACTER
            0xFFFD,    // 0C REPLACEMENT CHARACTER
            0x200D,    // 0D ZERO WIDTH JOINER
            0x200C,    // 0E ZERO WIDTH NON-JOINER
            0xFFFD,    // 0F REPLACEMENT CHARACTER
            0xFFFD,    // 10 REPLACEMENT CHARACTER
            0xFFFD,    // 11 REPLACEMENT CHARACTER
            0xFFFD,    // 12 REPLACEMENT CHARACTER
            0xFFFD,    // 13 REPLACEMENT CHARACTER
            0xFFFD,    // 14 REPLACEMENT CHARACTER
            0xFFFD,    // 15 REPLACEMENT CHARACTER
            0xFFFD,    // 16 REPLACEMENT CHARACTER
            0xFFFD,    // 17 REPLACEMENT CHARACTER
            0xFFFD,    // 18 REPLACEMENT CHARACTER
            0xFFFD,    // 19 REPLACEMENT CHARACTER
            0xFFFD,    // 1A REPLACEMENT CHARACTER
            0xFFFD,    // 1B REPLACEMENT CHARACTER
            0xFFFD,    // 1C REPLACEMENT CHARACTER
            0xFFFD,    // 1D REPLACEMENT CHARACTER
            0xFFFD,    // 1E REPLACEMENT CHARACTER
            0xFFFD,    // 1F REPLACEMENT CHARACTER
            0x0020,    // 20 SPACE
            0x0021,    // 21 EXCLAMATION MARK
            0x0022,    // 22 QUOTATION MARK
            0x0023,    // 23 NUMBER SIGN
            0x0024,    // 24 DOLLAR SIGN
            0x0025,    // 25 PERCENT SIGN
            0x0026,    // 26 AMPERSAND
            0x0027,    // 27 APOSTROPHE
            0x0028,    // 28 LEFT PARENTHESIS
            0x0029,    // 29 RIGHT PARENTHESIS
            0x002A,    // 2A ASTERISK
            0x002B,    // 2B PLUS SIGN
            0x002C,    // 2C COMMA
            0x002D,    // 2D HYPHEN-MINUS
            0x002E,    // 2E FULL STOP
            0x002F,    // 2F SOLIDUS
            0x0030,    // 30 DIGIT ZERO
            0x0031,    // 31 DIGIT ONE
            0x0032,    // 32 DIGIT TWO
            0x0033,    // 33 DIGIT THREE
            0x0034,    // 34 DIGIT FOUR
            0x0035,    // 35 DIGIT FIVE
            0x0036,    // 36 DIGIT SIX
            0x0037,    // 37 DIGIT SEVEN
            0x0038,    // 38 DIGIT EIGHT
            0x0039,    // 39 DIGIT NINE
            0x003A,    // 3A COLON
            0x003B,    // 3B SEMICOLON
            0x003C,    // 3C LESS-THAN SIGN
            0x003D,    // 3D EQUALS SIGN
            0x003E,    // 3E GREATER-THAN SIGN
            0x003F,    // 3F QUESTION MARK
            0x044E,    // 40 SMALL YU
            0x0430,    // 41 SMALL A
            0x0431,    // 42 SMALL BE
            0x0446,    // 43 SMALL TSE
            0x0434,    // 44 SMALL DE
            0x0435,    // 45 SMALL IE
            0x0444,    // 46 SMALL EF
            0x0433,    // 47 SMALL GHE
            0x0445,    // 48 SMALL HA
            0x0438,    // 49 SMALL I
            0x0439,    // 4A SMALL SHORT I
            0x043A,    // 4B SMALL KA
            0x043B,    // 4C SMALL EL
            0x043C,    // 4D SMALL EM
            0x043D,    // 4E SMALL EN
            0x043E,    // 4F SMALL O
            0x043F,    // 50 SMALL PE
            0x044F,    // 51 SMALL YA
            0x0440,    // 52 SMALL ER
            0x0441,    // 53 SMALL ES
            0x0442,    // 54 SMALL TE
            0x0443,    // 55 SMALL U
            0x0436,    // 56 SMALL ZHE
            0x0432,    // 57 SMALL VE
            0x044C,    // 58 SMALL SOFT SIGN
            0x044B,    // 59 SMALL YERU
            0x0437,    // 5A SMALL ZE
            0x0448,    // 5B SMALL SHA
            0x044D,    // 5C SMALL E
            0x0449,    // 5D SMALL SHCHA
            0x0447,    // 5E SMALL CHE
            0x044A,    // 5F SMALL HARD SIGN
            0x042E,    // 60 CAPITAL YU
            0x0410,    // 61 CAPITAL A
            0x0411,    // 62 CAPITAL BE
            0x0426,    // 63 CAPITAL TSE
            0x0414,    // 64 CAPITAL DE
            0x0415,    // 65 CAPITAL IE
            0x0424,    // 66 CAPITAL EF
            0x0413,    // 67 CAPITAL GHE
            0x0425,    // 68 CAPITAL HA
            0x0418,    // 69 CAPITAL I
            0x0419,    // 6A CAPITAL SHORT I
            0x041A,    // 6B CAPITAL KA
            0x041B,    // 6C CAPITAL EL
            0x041C,    // 6D CAPITAL EM
            0x041D,    // 6E CAPITAL EN
            0x041E,    // 6F CAPITAL O
            0x041F,    // 70 CAPITAL PE
            0x042F,    // 71 CAPITAL YA
            0x0420,    // 72 CAPITAL ER
            0x0421,    // 73 CAPITAL ES
            0x0422,    // 74 CAPITAL TE
            0x0423,    // 75 CAPITAL U
            0x0416,    // 76 CAPITAL ZHE
            0x0412,    // 77 CAPITAL VE
            0x042C,    // 78 CAPITAL SOFT SIGN
            0x042B,    // 79 CAPITAL YERU
            0x0417,    // 7A CAPITAL ZE
            0x0428,    // 7B CAPITAL SHA
            0x042D,    // 7C CAPITAL E
            0x0429,    // 7D CAPITAL SHCHA
            0x0427,    // 7E CAPITAL CHE
            0xFFFD     // 7F REPLACEMENT CHARACTER
    };
    private static final char[] extendedCyrillic = {
            0xFFFD,    // 80 REPLACEMENT CHARACTER
            0xFFFD,    // 81 REPLACEMENT CHARACTER
            0xFFFD,    // 82 REPLACEMENT CHARACTER
            0xFFFD,    // 83 REPLACEMENT CHARACTER
            0xFFFD,    // 84 REPLACEMENT CHARACTER
            0xFFFD,    // 85 REPLACEMENT CHARACTER
            0xFFFD,    // 86 REPLACEMENT CHARACTER
            0xFFFD,    // 87 REPLACEMENT CHARACTER
            0xFFFD,    // 88 REPLACEMENT CHARACTER
            0xFFFD,    // 89 REPLACEMENT CHARACTER
            0xFFFD,    // 8A REPLACEMENT CHARACTER
            0xFFFD,    // 8B REPLACEMENT CHARACTER
            0xFFFD,    // 8C REPLACEMENT CHARACTER
            0x200D,    // 8D Zero-Width Joiner
            0x200E,    // 8E Zero-Width Non-Joiner
            0xFFFD,    // 8F REPLACEMENT CHARACTER
            0xFFFD,    // 90 REPLACEMENT CHARACTER
            0xFFFD,    // 91 REPLACEMENT CHARACTER
            0xFFFD,    // 92 REPLACEMENT CHARACTER
            0xFFFD,    // 93 REPLACEMENT CHARACTER
            0xFFFD,    // 94 REPLACEMENT CHARACTER
            0xFFFD,    // 95 REPLACEMENT CHARACTER
            0xFFFD,    // 96 REPLACEMENT CHARACTER
            0xFFFD,    // 97 REPLACEMENT CHARACTER
            0xFFFD,    // 98 REPLACEMENT CHARACTER
            0xFFFD,    // 99 REPLACEMENT CHARACTER
            0xFFFD,    // 9A REPLACEMENT CHARACTER
            0xFFFD,    // 9B REPLACEMENT CHARACTER
            0xFFFD,    // 9C REPLACEMENT CHARACTER
            0xFFFD,    // 9D REPLACEMENT CHARACTER
            0xFFFD,    // 9E REPLACEMENT CHARACTER
            0xFFFD,    // 9F REPLACEMENT CHARACTER
            0x0020,    // A0 REPLACEMENT CHARACTER
            0xFFFD,    // A1 REPLACEMENT CHARACTER
            0xFFFD,    // A2 REPLACEMENT CHARACTER
            0xFFFD,    // A3 REPLACEMENT CHARACTER
            0xFFFD,    // A4 REPLACEMENT CHARACTER
            0xFFFD,    // A5 REPLACEMENT CHARACTER
            0xFFFD,    // A6 REPLACEMENT CHARACTER
            0xFFFD,    // A7 REPLACEMENT CHARACTER
            0xFFFD,    // A8 REPLACEMENT CHARACTER
            0xFFFD,    // A9 REPLACEMENT CHARACTER
            0xFFFD,    // AA REPLACEMENT CHARACTER
            0xFFFD,    // AB REPLACEMENT CHARACTER
            0xFFFD,    // AC REPLACEMENT CHARACTER
            0xFFFD,    // AD REPLACEMENT CHARACTER
            0xFFFD,    // AE REPLACEMENT CHARACTER
            0xFFFD,    // AF REPLACEMENT CHARACTER
            0xFFFD,    // B0 REPLACEMENT CHARACTER
            0xFFFD,    // B1 REPLACEMENT CHARACTER
            0xFFFD,    // B2 REPLACEMENT CHARACTER
            0xFFFD,    // B3 REPLACEMENT CHARACTER
            0xFFFD,    // B4 REPLACEMENT CHARACTER
            0xFFFD,    // B5 REPLACEMENT CHARACTER
            0xFFFD,    // B6 REPLACEMENT CHARACTER
            0xFFFD,    // B7 REPLACEMENT CHARACTER
            0xFFFD,    // B8 REPLACEMENT CHARACTER
            0xFFFD,    // B9 REPLACEMENT CHARACTER
            0xFFFD,    // BA REPLACEMENT CHARACTER
            0xFFFD,    // BB REPLACEMENT CHARACTER
            0xFFFD,    // BC REPLACEMENT CHARACTER
            0xFFFD,    // BD REPLACEMENT CHARACTER
            0xFFFD,    // BE REPLACEMENT CHARACTER
            0xFFFD,    // BF REPLACEMENT CHARACTER
            0x0491,    // C0 SMALL GHE WITH UPTURN
            0x0452,    // C1 SMALL DJE (SERBIAN)
            0x0453,    // C2 SMALL GJE
            0x0454,    // C3 SMALL UKRAINIAN IE
            0x0451,    // C4 SMALL IO
            0x0455,    // C5 SMALL DZE
            0x0456,    // C6 SMALL BYELORUSSIAN-UKRANIAN I
            0x0457,    // C7 SMALL YI (UKRAINIAN)
            0x0458,    // C8 SMALL JE
            0x0459,    // C9 SMALL LJE
            0x045A,    // CA SMALL NJE
            0x045B,    // CB SMALL TSHE (SERBIAN)
            0x045C,    // CC SMALL KJE
            0x045E,    // CD SMALL SHORT U (BYELORUSSIAN)
            0x045F,    // CE SMALL DZHE
            0xFFFD,    // CF REPLACEMENT CHARACTER
            0x0463,    // D0 SMALL YAT
            0x0473,    // D1 SMALL FITA
            0x0475,    // D2 SMALL IZHITSA
            0x046B,    // D3 SMALL BIG YUS
            0xFFFD,    // D4 REPLACEMENT CHARACTER
            0xFFFD,    // D5 REPLACEMENT CHARACTER
            0xFFFD,    // D6 REPLACEMENT CHARACTER
            0xFFFD,    // D7 REPLACEMENT CHARACTER
            0xFFFD,    // D8 REPLACEMENT CHARACTER
            0xFFFD,    // D9 REPLACEMENT CHARACTER
            0xFFFD,    // DA REPLACEMENT CHARACTER
            0x005B,    // DB LEFT SQUARE BRACKET
            0xFFFD,    // DC REPLACEMENT CHARACTER
            0x005D,    // DD RIGHT SQUARE BRACKET
            0xFFFD,    // DE REPLACEMENT CHARACTER
            0x005F,    // DF LOW LINE
            0x0490,    // E0 CAPITAL GHE WITH UPTURN
            0x0402,    // E1 CAPITAL DJE (SERBIAN)
            0x0403,    // E2 CAPITAL GJE
            0x0404,    // E3 CAPITAL UKRANIAN IE
            0x0401,    // E4 CAPITAL IO
            0x0405,    // E5 CAPITAL DZE
            0x0406,    // E6 CAPITAL BYELORUSSIAN-UKRAINIAN I
            0x0407,    // E7 CAPITAL YI (UKRAINIAN)
            0x0408,    // E8 CAPITAL JE
            0x0409,    // E9 CAPITAL LJE
            0x040A,    // EA CAPITAL NJE
            0x040B,    // EB CAPITAL TSHE (SERBIAN)
            0x040C,    // EC CAPITAL KJE
            0x040E,    // ED CAPITAL SHORT U (BYELORUSSIAN)
            0x040F,    // EE CAPITAL DZHE
            0x042A,    // EF CAPITAL HARD SIGN
            0x0462,    // F0 CAPITAL YAT
            0x0472,    // F1 CAPITAL FITA
            0x0474,    // F2 CAPITAL IZHITSA
            0x046A,    // F3 CAPITAL BIG YUS
            0xFFFD,    // F4 REPLACEMENT CHARACTER
            0xFFFD,    // F5 REPLACEMENT CHARACTER
            0xFFFD,    // F6 REPLACEMENT CHARACTER
            0xFFFD,    // F7 REPLACEMENT CHARACTER
            0xFFFD,    // F8 REPLACEMENT CHARACTER
            0xFFFD,    // F9 REPLACEMENT CHARACTER
            0xFFFD,    // FA REPLACEMENT CHARACTER
            0xFFFD,    // FB REPLACEMENT CHARACTER
            0xFFFD,    // FC REPLACEMENT CHARACTER
            0xFFFD,    // FD REPLACEMENT CHARACTER
            0xFFFD,    // FE REPLACEMENT CHARACTER
            0xFFFD     // FF REPLACEMENT CHARACTER
    };
    private static final char[] arabic = {
            0xFFFD,    // 00 REPLACEMENT CHARACTER
            0xFFFD,    // 01 REPLACEMENT CHARACTER
            0xFFFD,    // 02 REPLACEMENT CHARACTER
            0xFFFD,    // 03 REPLACEMENT CHARACTER
            0xFFFD,    // 04 REPLACEMENT CHARACTER
            0xFFFD,    // 05 REPLACEMENT CHARACTER
            0xFFFD,    // 06 REPLACEMENT CHARACTER
            0xFFFD,    // 07 REPLACEMENT CHARACTER
            0xFFFD,    // 08 REPLACEMENT CHARACTER
            0xFFFD,    // 09 REPLACEMENT CHARACTER
            0xFFFD,    // 0A REPLACEMENT CHARACTER
            0xFFFD,    // 0B REPLACEMENT CHARACTER
            0xFFFD,    // 0C REPLACEMENT CHARACTER
            0x200D,    // 0D Zero-Width Joiner
            0x200E,    // 0E Zero-Width Non-Joiner
            0xFFFD,    // 0F REPLACEMENT CHARACTER
            0xFFFD,    // 10 REPLACEMENT CHARACTER
            0xFFFD,    // 11 REPLACEMENT CHARACTER
            0xFFFD,    // 12 REPLACEMENT CHARACTER
            0xFFFD,    // 13 REPLACEMENT CHARACTER
            0xFFFD,    // 14 REPLACEMENT CHARACTER
            0xFFFD,    // 15 REPLACEMENT CHARACTER
            0xFFFD,    // 16 REPLACEMENT CHARACTER
            0xFFFD,    // 17 REPLACEMENT CHARACTER
            0xFFFD,    // 18 REPLACEMENT CHARACTER
            0xFFFD,    // 19 REPLACEMENT CHARACTER
            0xFFFD,    // 1A REPLACEMENT CHARACTER
            0xFFFD,    // 1B REPLACEMENT CHARACTER
            0xFFFD,    // 1C REPLACEMENT CHARACTER
            0xFFFD,    // 1D REPLACEMENT CHARACTER
            0xFFFD,    // 1E REPLACEMENT CHARACTER
            0xFFFD,    // 1F REPLACEMENT CHARACTER
            0x0020,    // 20 SPACE
            0x0021,    // 21 EXCLAMATION MARK
            0x0022,    // 22 QUOTATION MARK
            0x0023,    // 23 NUMBER SIGN
            0x0024,    // 24 DOLLAR SIGN
            0x066A,    // 25 ARABIC PERCENT SIGN
            0x0026,    // 26 AMPERSAND
            0x0027,    // 27 APOSTROPHE
            0x0028,    // 28 LEFT (OPEN) PARENTHESIS
            0x0029,    // 29 RIGHT (CLOSE) PARENTHESIS
            0x066D,    // 2A ARABIC FIVE POINTED STAR (ASTERISK)
            0x002B,    // 2B PLUS SIGN
            0x060C,    // 2C ARABIC COMMA
            0x002D,    // 2D HYPHEN-MINUS
            0x002E,    // 2E FULL STOP (PERIOD)
            0x002F,    // 2F SOLIDUS (SLASH)
            0x0660,    // 30 DIGIT ZERO
            0x0661,    // 31 DIGIT ONE
            0x0662,    // 32 DIGIT TWO
            0x0663,    // 33 DIGIT THREE
            0x0664,    // 34 DIGIT FOUR
            0x0665,    // 35 DIGIT FIVE
            0x0666,    // 36 DIGIT SIX
            0x0667,    // 37 DIGIT SEVEN
            0x0668,    // 38 DIGIT EIGHT
            0x0669,    // 39 DIGIT NINE
            0x003A,    // 3A COLON
            0x061B,    // 3B ARABIC SEMICOLON
            0x003C,    // 3C LESS-THAN SIGN (OPEN ANGLE BRACKET)
            0x003D,    // 3D EQUALS SIGN
            0x003E,    // 3E GREATER-THAN SIGN (CLOSE ANGLE BRACKET)
            0x061F,    // 3F ARABIC QUESTION MARK
            0xFFFD,    // 40 REPLACEMENT CHARACTER
            0x0621,    // 41 HAMZAH
            0x0622,    // 42 ALEF WITH MADDA ABOVE
            0x0623,    // 43 ALEF WITH HAMZA ABOVE
            0x0624,    // 44 WAW WITH HAMZA ABOVE
            0x0625,    // 45 ALEF WITH HAMZA BELOW
            0x0626,    // 46 YEH WITH HAMZA ABOVE
            0x0627,    // 47 ALEF
            0x0628,    // 48 BEH
            0x0629,    // 49 TEH MARBUTA
            0x062A,    // 4A TEH
            0x062B,    // 4B THEH
            0x062C,    // 4C JEEM
            0x062D,    // 4D HAH
            0x062E,    // 4E KHAH
            0x062F,    // 4F DAL
            0x0630,    // 50 THAL
            0x0631,    // 51 REH
            0x0632,    // 52 ZAIN
            0x0633,    // 53 SEEN
            0x0634,    // 54 SHEEN
            0x0635,    // 55 SAD
            0x0636,    // 56 DAD
            0x0637,    // 57 TAH
            0x0638,    // 58 ZAH
            0x0639,    // 59 AIN
            0x063A,    // 5A GHAIN
            0x005B,    // 5B LEFT (OPENING) SQUARE BRACKET
            0xFFFD,    // 5C REPLACEMENT CHARACTER
            0x005D,    // 5D RIGHT (CLOSING) SQUARE BRACKET
            0xFFFD,    // 5E REPLACEMENT CHARACTER
            0xFFFD,    // 5F REPLACEMENT CHARACTER
            0x0640,    // 60 TATWEEL
            0x0641,    // 61 FEH
            0x0642,    // 62 QAF
            0x0643,    // 63 KAF
            0x0644,    // 64 LAM
            0x0645,    // 65 MEEM
            0x0646,    // 66 NOON
            0x0647,    // 67 HEH
            0x0648,    // 68 WAW
            0x0649,    // 69 ALEF MAKSURA
            0x064A,    // 6A YEH
            0x064B,    // 6B FATHATAN
            0x064C,    // 6C DAMMATAN
            0x064D,    // 6D KASRATAN
            0x064E,    // 6E FATHA
            0x064F,    // 6F DAMMA
            0x0650,    // 70 KASRA
            0x0651,    // 71 SHADDA
            0x0652,    // 72 SUKUN
            0x0671,    // 73 ALEF WASLA
            0x0670,    // 74 SUPERSCRIPT ALEF
            0xFFFD,    // 75 REPLACEMENT CHARACTER
            0xFFFD,    // 76 REPLACEMENT CHARACTER
            0xFFFD,    // 77 REPLACEMENT CHARACTER
            0x066C,    // 78 ARABIC THOUSANDS SEPARATOR
            0x201D,    // 79 RIGHT DOUBLE QUOTATION MARK
            0x201C,    // 7A LEFT DOUBLE QUOTATION MARK
            0xFFFD,    // 7B REPLACEMENT CHARACTER
            0xFFFD,    // 7C REPLACEMENT CHARACTER
            0xFFFD,    // 7D REPLACEMENT CHARACTER
            0xFFFD,    // 7E REPLACEMENT CHARACTER
            0xFFFD     // 7F REPLACEMENT CHARACTER
    };
    private static final char[] extendedArabic = {
            0xFFFD,    // 80 REPLACEMENT CHARACTER
            0xFFFD,    // 81 REPLACEMENT CHARACTER
            0xFFFD,    // 82 REPLACEMENT CHARACTER
            0xFFFD,    // 83 REPLACEMENT CHARACTER
            0xFFFD,    // 84 REPLACEMENT CHARACTER
            0xFFFD,    // 85 REPLACEMENT CHARACTER
            0xFFFD,    // 86 REPLACEMENT CHARACTER
            0xFFFD,    // 87 REPLACEMENT CHARACTER
            0xFFFD,    // 88 REPLACEMENT CHARACTER
            0xFFFD,    // 89 REPLACEMENT CHARACTER
            0xFFFD,    // 8A REPLACEMENT CHARACTER
            0xFFFD,    // 8B REPLACEMENT CHARACTER
            0xFFFD,    // 8C REPLACEMENT CHARACTER
            0x200D,    // 8D Zero-Width Joiner
            0x200E,    // 8E Zero-Width Non-Joiner
            0xFFFD,    // 8F REPLACEMENT CHARACTER
            0xFFFD,    // 90 REPLACEMENT CHARACTER
            0xFFFD,    // 91 REPLACEMENT CHARACTER
            0xFFFD,    // 92 REPLACEMENT CHARACTER
            0xFFFD,    // 93 REPLACEMENT CHARACTER
            0xFFFD,    // 94 REPLACEMENT CHARACTER
            0xFFFD,    // 95 REPLACEMENT CHARACTER
            0xFFFD,    // 96 REPLACEMENT CHARACTER
            0xFFFD,    // 97 REPLACEMENT CHARACTER
            0xFFFD,    // 98 REPLACEMENT CHARACTER
            0xFFFD,    // 99 REPLACEMENT CHARACTER
            0xFFFD,    // 9A REPLACEMENT CHARACTER
            0xFFFD,    // 9B REPLACEMENT CHARACTER
            0xFFFD,    // 9C REPLACEMENT CHARACTER
            0xFFFD,    // 9D REPLACEMENT CHARACTER
            0xFFFD,    // 9E REPLACEMENT CHARACTER
            0xFFFD,    // 9F REPLACEMENT CHARACTER
            0x0020,    // A0 REPLACEMENT CHARACTER
            0x06FD,    // A1 SIGN SINDI AMPERSAND
            0x0672,    // A2 ALEF WITH WAVY HAMZA ABOVE
            0x0673,    // A3 ALEF WITH WAVY HAMZA BELOW
            0x0679,    // A4 TTEH
            0x067A,    // A5 TTEHEH
            0x067B,    // A6 BEEH
            0x067C,    // A7 TEH WITH RING
            0x067D,    // A8 TEH WITH THREE DOTS ABOVE
            0x067E,    // A9 PEH
            0x067F,    // AA TEHEH
            0x0680,    // AB BEHEH
            0x0681,    // AC HAH WITH HAMZA ABOVE
            0x0682,    // AD HAH WITH TWO ABOVE DOTS VERTICAL ABOVE
            0x0683,    // AE NYEH
            0x0684,    // AF DYEH
            0x0685,    // B0 HAH WITH THREE DOTS ABOVE
            0x0686,    // B1 TCHEH
            0x06BF,    // B2 TCHEH WITH DOT ABOVE
            0x0687,    // B3 TCHEHEH
            0x0688,    // B4 DDAL
            0x0689,    // B5 DAL WITH RING
            0x068A,    // B6 DAL WITH DOT BELOW
            0x068B,    // B7 DAL WITH DOT BELOW AND SMALL TAH
            0x068C,    // B8 DAHAL
            0x068D,    // B9 DDAHAL
            0x068E,    // BA DUL
            0x068F,    // BB DAL WITH THREE DOTS ABOVE DOWNWARDS
            0x0690,    // BC DAL WITH FOUR DOTS ABOVE
            0x0691,    // BD RREH
            0x0692,    // BE REH WITH SMALL V
            0x0693,    // BF REH WITH RING
            0x0694,    // C0 REH WITH DOT BELOW
            0x0695,    // C1 REH WITH SMALL V BELOW
            0x0696,    // C2 REH WITH DOT BELOW AND DOT ABOVE
            0x0697,    // C3 REH WITH TWO DOTS ABOVE
            0x0698,    // C4 JEH
            0x0699,    // C5 REH WITH FOUR DOTS ABOVE
            0x069A,    // C6 SEEN WITH DOT BELOW AND DOT ABOVE
            0x069B,    // C7 SEEN WITH THREE DOTS BELOW
            0x069C,    // C8 SHEEN WITH THREE DOTS BELOW AND THREE DOTS ABOVE
            0x06FA,    // C9 SHEEN WITH DOT BELOW
            0x069D,    // CA SAD WITH TWO DOTS BELOW
            0x069E,    // CB SAD WITH THREE DOTS ABOVE
            0x06FB,    // CC DAD WITH DOT BELOW
            0x069F,    // CD TAH WITH THREE DOTS ABOVE
            0x06A0,    // CE AIN WITH THREE DOTS ABOVE
            0x06FC,    // CF GHAIN WITH DOT BELOW
            0x06A1,    // D0 DOTLESS FEH
            0x06A2,    // D1 FEH WITH DOT MOVED BELOW
            0x06A3,    // D2 FEH WITH DOT BELOW
            0x06A4,    // D3 VEH
            0x06A5,    // D4 FEH WITH THREE DOTS BELOW
            0x06A6,    // D5 PEHEH
            0x06A7,    // D6 QAF WITH DOT ABOVE
            0x06A8,    // D7 QAF WITH THREE DOTS ABOVE
            0x06A9,    // D8 KEHEH
            0x06AA,    // D9 SWASH KAF
            0x06AB,    // DA KAF WITH RING
            0x06AC,    // DB KAF WITH DOT ABOVE
            0x06AD,    // DC NG
            0x06AE,    // DD KAF WITH THREE DOTS BELOW
            0x06AF,    // DE GAF
            0x06B0,    // DF GAF WITH RING
            0x06B1,    // E0 NGOEH
            0x06B2,    // E1 GAF WITH TWO DOTS BELOW
            0x06B3,    // E2 GUEH
            0x06B4,    // E3 GAF WITH THREE DOTS ABOVE
            0x06B5,    // E4 LAM WITH SMALL V
            0x06B6,    // E5 LAM WITH DOT ABOVE
            0x06B7,    // E6 LAM WITH THREE DOTS ABOVE
            0x06B8,    // E7 LAM WITH THREE DOTS BELOW
            0x06BA,    // E8 NOON GHUNNA
            0x06BB,    // E9 RNOON
            0x06BC,    // EA NOON WITH RING
            0x06BD,    // EB NOON WITH THREE DOTS ABOVE
            0x06B9,    // EC NOON WITH DOT BELOW
            0x06BE,    // ED HEH DOACHASHMEE
            0x06C0,    // EE HEH WITH YEH ABOVE
            0x06C4,    // EF WAW WITH RING
            0x06C5,    // F0 KIRGIZ OE
            0x06C6,    // F1 OE
            0x06CA,    // F2 WAW WITH TWO DOTS ABOVE
            0x06CB,    // F3 VE
            0x06CD,    // F4 YEH WITH TAIL
            0x06CE,    // F5 YEH WITH SMALL V
            0x06D0,    // F6 E
            0x06D2,    // F7 YEH BARREE
            0x06D3,    // F8 YEH BARREE WITH HAMZA ABOVE
            0xFFFD,    // F9 REPLACEMENT CHARACTER
            0xFFFD,    // FA REPLACEMENT CHARACTER
            0xFFFD,    // FB REPLACEMENT CHARACTER
            0xFFFD,    // FC REPLACEMENT CHARACTER
            0x0306,    // FD COMBINING BREVE
            0x030C,    // FE COMBINING CARON
            0xFFFD    // FF REPLACEMENT CHARACTER
    };
    private static final char[][] EACC = new char[256][];
    private static boolean isAsciiDiacritic[];
    private static boolean isAnselDiacritic[];
    private static boolean isGreekDiacritic[];
    private static boolean isSubscriptDiacritic[];
    private static boolean isSuperscriptDiacritic[];
    private static boolean isHebrewDiacritic[];
    private static boolean isCyrillicDiacritic[];
    private static boolean isExtendedCyrillicDiacritic[];
    private static boolean isArabicDiacritic[];
    private static boolean isExtendedArabicDiacritic[];

    static {
        int i;

        isAsciiDiacritic = new boolean[ascii.length];
        isAnselDiacritic = new boolean[ansel.length];
        isGreekDiacritic = new boolean[greek.length];
        isSubscriptDiacritic = new boolean[subscript.length];
        isSuperscriptDiacritic = new boolean[superscript.length];
        isHebrewDiacritic = new boolean[hebrew.length];
        isCyrillicDiacritic = new boolean[cyrillic.length];
        isExtendedCyrillicDiacritic = new boolean[extendedCyrillic.length];
        isArabicDiacritic = new boolean[arabic.length];
        isExtendedArabicDiacritic = new boolean[extendedArabic.length];

        for (i = 0xe0; i <= 0xff; i++) {
            isAnselDiacritic[i - 0x80] = true;
        }

        // hebrew diacritics
        for (i = 0x40; i <= 0x4f; i++) {
            isHebrewDiacritic[i] = true;
        }

        // arabic diacritics
        // -----Original Message-----
        // From:        Smith,Gary
        // Sent:        Wednesday, July 01, 1998 5:22 PM
        // To:  LeVan,Ralph
        // Subject:     RE: USM-94 diacritics
        //
        // It appears that the following are the only true diacritics in
        // the USMARC implementation of Arabic.  I'm basing this on the
        // glyphs displayed in the Unicode Standard.  The same
        // information can be obtained, although with quite a bit more
        // effort, from  UnicodeData-2.1.2.txt at
        // ftp://ftp.unicode.org/Public/2.1-Update/.
        //
        //
        // 6B   FATHATAN              064B   ARABIC FATHATAN
        // 6C   DAMMATAN              064C   ARABIC DAMMATAN
        // 6D   KASRATAN              064D   ARABIC KASRATAN
        // 6E   FATHA                 064E   ARABIC FATHA
        // 6F   DAMMA                 064F   ARABIC DAMMA
        // 70   KASRA                 0650   ARABIC KASRA
        // 71   SHADDA                    0651   ARABIC SHADDA
        // 72   SUKUN                     0652   ARABIC SUKUN
        //
        // 74   SUPERSCRIPT ALEF          0670   ARABIC LETTER
        //                                       SUPERSCRIPT ALEF
        //
        // FD   SHORT E                   0306   COMBINING BREVE
        // FE   SHORT U                   030C   COMBINING CARON
        isArabicDiacritic[0x6B] = true;
        isArabicDiacritic[0x6C] = true;
        isArabicDiacritic[0x6D] = true;
        isArabicDiacritic[0x6E] = true;
        isArabicDiacritic[0x6F] = true;
        isArabicDiacritic[0x70] = true;
        isArabicDiacritic[0x71] = true;
        isArabicDiacritic[0x72] = true;
        isArabicDiacritic[0x74] = true;
        isExtendedArabicDiacritic[0xFD - 0x80] = true;
        isExtendedArabicDiacritic[0xFE - 0x80] = true;

        char[] t;
        EACC[0x21] = EaccTables.initTable21();
        EACC[0x22] = EaccTables.initTable22();
        EACC[0x23] = EaccTables.initTable23();
        EACC[0x27] = EaccTables.initTable27();
        EACC[0x28] = EaccTables.initTable28();
        EACC[0x29] = EaccTables.initTable29();
        EACC[0x2D] = EaccTables.initTable2D();
        EACC[0x2E] = EaccTables.initTable2E();
        EACC[0x2F] = EaccTables.initTable2F();
        EACC[0x33] = EaccTables.initTable33();
        EACC[0x34] = EaccTables.initTable34();
        EACC[0x35] = EaccTables.initTable35();
        EACC[0x39] = EaccTables.initTable39();
        EACC[0x3A] = EaccTables.initTable3A();
        EACC[0x3B] = EaccTables.initTable3B();
        EACC[0x3F] = EaccTables.initTable3F();
        EACC[0x45] = EaccTables.initTable45();
        EACC[0x46] = EaccTables.initTable46();
        EACC[0x47] = EaccTables.initTable47();
        EACC[0x4B] = EaccTables.initTable4B();
        EACC[0x4C] = EaccTables.initTable4C();
        EACC[0x4D] = EaccTables.initTable4D();
        EACC[0x51] = EaccTables.initTable51();
        EACC[0x69] = EaccTables.initTable69();
        EACC[0x6F] = EaccTables.initTable6F();
        EACC[0x70] = EaccTables.initTable70();
    }

    private boolean g0_isDiacritic[], g1_isDiacritic[],
            ignoreRecoverableErrors = false, isDiacritic[], isEACC;
    private byte byteBuf[];
    private char g0_map[], g1_map[], map[];
    private int byteOffset, maxByteOffset, oneByte;
    private String g0_mapName, g1_mapName, mapName;

    public int convert(byte byteBuf[], int startByteOffset, int byteLen,
                       char charBuf[], int startCharOffset, int charLen)
            throws CharConversionException {
        char c, oneChar = '\0';
        int charWhere = -1, difference = 0;
        int byteThree, byteTwo, charOffset, twoBytes;


        this.byteBuf = byteBuf;
        isEACC = false;
        g0_map = ascii;
        g0_mapName = "ascii";
        g0_isDiacritic = isAsciiDiacritic;
        g1_map = ansel;
        g1_mapName = "ansel";
        g1_isDiacritic = isAnselDiacritic;
        maxByteOffset = startByteOffset + byteLen;

        main_loop:
        for (byteOffset = startByteOffset, charOffset = startCharOffset;
             byteOffset < maxByteOffset; byteOffset++, charOffset++) {
            //  Create an unsigned representation of a byte.
            oneByte = byteBuf[byteOffset] & 0xff;
            // Handle escape sequences
            if (oneByte == 0x1b) {
                try {
                    handleEscapeSequence();
                    charOffset--;  // will be reincremented at top of loop
                    continue;
                } catch (CharConversionException e) {
                    if (ignoreRecoverableErrors) {
                        charBuf[charOffset++] = '\ufffd';
                        break main_loop;
                    } else {
                        throw e;
                    }
                }
            }

            // Convert a byte to a Unicode character.
            if (isEACC) { // suck up three bytes for each char
                map = EACC[oneByte];
                if (map == null) {
                    if (ignoreRecoverableErrors) {
                        charBuf[charOffset] = '\ufffd';
                        byteOffset += 2;  // EACC comes in three byte triples
                        continue; // on to next triple
                    }
                    throw new CharConversionException("Unsupported EACC first byte " + Integer.toHexString(oneByte));
                }
                byteTwo = byteBuf[++byteOffset];
                byteThree = byteBuf[++byteOffset];
                if (byteTwo < 0 || byteThree < 0) {
                    if (ignoreRecoverableErrors) {
                        charBuf[charOffset] = '\ufffd';
                        continue; // on to next triple
                    }
                    throw new CharConversionException("Bad trailing EACC bytes " +
                                    Integer.toHexString(oneByte) + " " +
                                    Integer.toHexString(byteTwo) + " " +
                                    Integer.toHexString(byteThree));
                }
                twoBytes = byteTwo * 256 + byteThree;
                try {
                    c = map[twoBytes];
                    if (c == '\0') { // unspecified EACC char in table
                        if (ignoreRecoverableErrors) {
                            charBuf[charOffset] = '\ufffd';
                            continue; // on to next triple
                        }
                        throw new CharConversionException(
                                "Bad trailing EACC bytes " +
                                        Integer.toHexString(oneByte) + " " +
                                        Integer.toHexString(byteTwo) + " " +
                                        Integer.toHexString(byteThree));
                    }
                    charBuf[charOffset] = c;
                } catch (ArrayIndexOutOfBoundsException e) {
                    if (ignoreRecoverableErrors) {
                        charBuf[charOffset] = '\ufffd';
                        continue; // on to next triple
                    }
                    throw new CharConversionException(
                            "Bad trailing EACC bytes " +
                                    Integer.toHexString(oneByte) + " " +
                                    Integer.toHexString(byteTwo) + " " +
                                    Integer.toHexString(byteThree));
                }
                continue;
            }
            charWhere = charOffset;
            if (oneByte > 127) {
                isDiacritic = g1_isDiacritic;
                map = g1_map;
                mapName = g1_mapName;
                oneByte -= 0x80;
                difference = 0x80;
            } else {
                isDiacritic = g0_isDiacritic;
                map = g0_map;
                mapName = g0_mapName;
                difference = 0;
            }
            boolean foundDiacritic = false;
            while (isDiacritic[oneByte]) {
                oneChar = map[oneByte];
                if (oneChar == '\ufffd') {
                    throw new CharConversionException("Unable to translate " +
                            mapName + " character 0x" +
                            Integer.toHexString(oneByte + difference));
                }
                charBuf[++charOffset] = oneChar;
                if (byteOffset + 1 > maxByteOffset) {
                    if (ignoreRecoverableErrors) {
                        charBuf[charWhere] = ' '; // add a space for the
                        continue main_loop;     // diacritic to modify
                    } else {
                        throw new CharConversionException(mapName + " diacritic 0x" +
                                Integer.toHexString(oneByte + difference) +
                                " at end of field");
                    }
                }
                oneByte = (byteBuf[++byteOffset] & 0xff);
                while (oneByte == 0x1b) { // suck up escape sequences
                    try {
                        handleEscapeSequence();
                        oneByte = (byteBuf[++byteOffset] & 0xff);
                    } catch (CharConversionException e) {
                        if (ignoreRecoverableErrors) {
                            charBuf[charOffset] = '\ufffd';
                            break main_loop;
                        } else {
                            throw e;
                        }
                    }
                }
                foundDiacritic = true;
                if (isEACC) {
                    map = EACC[oneByte];
                    if (map == null) {
                        throw new CharConversionException(
                                "Unsupported EACC first byte " +
                                        Integer.toHexString(oneByte));
                    }
                    byteTwo = byteBuf[++byteOffset];
                    byteThree = byteBuf[++byteOffset];
                    if (byteTwo < 0 || byteThree < 0) {
                        throw new CharConversionException(
                                "Bad trailing EACC bytes " +
                                        Integer.toHexString(oneByte) + " " +
                                        Integer.toHexString(byteTwo) + " " +
                                        Integer.toHexString(byteThree));
                    }
                    twoBytes = byteTwo * 256 + byteThree;
                    try {
                        charBuf[charWhere] = map[twoBytes];
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new CharConversionException(
                                "Bad trailing EACC bytes " +
                                        Integer.toHexString(oneByte) + " " +
                                        Integer.toHexString(byteTwo) + " " +
                                        Integer.toHexString(byteThree));
                    }
                    continue main_loop;
                }
                if (oneByte > 127) {
                    isDiacritic = g1_isDiacritic;
                    map = g1_map;
                    mapName = g1_mapName;
                    oneByte -= 0x80;
                    difference = 0x80;
                } else {
                    isDiacritic = g0_isDiacritic;
                    map = g0_map;
                    mapName = g0_mapName;
                    difference = 0;
                }
            }
            if (foundDiacritic) {
                if (oneByte == 0x1d || oneByte == 0x1e || oneByte == 0x1f) {
                    if (ignoreRecoverableErrors) {
                        oneByte = 0x20;  // space
                    } else {
                        throw new CharConversionException(
                                "Diacritic followed by a field or subfield " +
                                        "separator");
                    }
                }
            }

            oneChar = map[oneByte];
            if (oneChar == '\ufffd') {
                throw new CharConversionException("Unable to translate " +
                        mapName + " character 0x" +
                        Integer.toHexString(oneByte + difference));
            }
            charBuf[charWhere] = oneChar;
        }
        return charOffset - startCharOffset;
    }

    public void setIgnoreRecoverableErrors(boolean val) {
        ignoreRecoverableErrors = val;
    }

    private void handleEscapeSequence() throws CharConversionException {
        int saveOne;

        isEACC = false;
        if (byteOffset + 1 == maxByteOffset) {
            throw new CharConversionException("Escape character at end of field");
        }
        oneByte = (byteBuf[++byteOffset] & 0xff);
        switch (oneByte) {  // short escape sequences to g0
            case 's':  // back to default
                g0_map = ascii;
                g0_mapName = "ascii";
                g0_isDiacritic = isAsciiDiacritic;
                break;
            case 'g':  // greek symbols
                g0_map = greek;
                g0_mapName = "greek";
                g0_isDiacritic = isGreekDiacritic;
                break;
            case 'b':  // subscripts
                g0_map = subscript;
                g0_mapName = "subscript";
                g0_isDiacritic = isSubscriptDiacritic;
                break;
            case 'p':  // superscripts
                g0_map = superscript;
                g0_mapName = "superscript";
                g0_isDiacritic = isSuperscriptDiacritic;
                break;
            case '(':  // long escape sequences to g0
            case ',':
                saveOne = oneByte;
                if (byteOffset + 1 == maxByteOffset) {
                    if (ignoreRecoverableErrors) {
                        return;
                    } else {
                        throw new CharConversionException(
                                "Esc-" + (char) oneByte + " at end of field");
                    }
                }
                oneByte = (byteBuf[++byteOffset] & 0xff);
                switch (oneByte) {
                    case 'B':  // ASCII
                        g0_map = ascii;
                        g0_mapName = "ascii";
                        g0_isDiacritic = isAsciiDiacritic;
                        break;
                    case 'S':  // Greek Symbols
                        g0_map = greek;
                        g0_mapName = "greek";
                        g0_isDiacritic = isGreekDiacritic;
                        break;
                    case '2':  // Basic Hebrew
                        g0_map = hebrew;
                        g0_mapName = "hebrew";
                        g0_isDiacritic = isHebrewDiacritic;
                        break;
                    case 'N':  // Basic Cyrillic
                        g0_map = cyrillic;
                        g0_mapName = "cyrillic";
                        g0_isDiacritic = isCyrillicDiacritic;
                        break;
                    case 'Q':  // Extended Cyrillic
                        g0_map = extendedCyrillic;
                        g0_mapName = "extendedCyrillic";
                        g0_isDiacritic = isExtendedCyrillicDiacritic;
                        break;
                    case '3':  // Basic Arabic
                        g0_map = arabic;
                        g0_mapName = "arabic";
                        g0_isDiacritic = isArabicDiacritic;
                        break;
                    case '4':  // Extended Arabic
                        g0_map = extendedArabic;
                        g0_mapName = "extendedArabic";
                        g0_isDiacritic = isExtendedArabicDiacritic;
                        break;
                    default:
                        throw new CharConversionException(
                                "Unsupported USM-94 escape sequence: 'Esc-" + saveOne + "-" +
                                        (char) oneByte + "'");
                }
                break;
            case ')':  // long escape sequences to g0
            case '-':
                saveOne = oneByte;
                if (byteOffset + 1 == maxByteOffset) {
                    if (ignoreRecoverableErrors) {
                        return;
                    } else {
                        throw new CharConversionException(
                                "Esc-" + (char) oneByte + " at end of field");
                    }
                }
                oneByte = (byteBuf[++byteOffset] & 0xff);
                switch (oneByte) {
                    case 'B':  // ASCII
                        g1_map = ansel;
                        g1_mapName = "ansel";
                        g1_isDiacritic = isAnselDiacritic;
                        break;
                    case 'S':  // Greek Symbols
                        g1_map = greek;
                        g1_mapName = "greek";
                        g1_isDiacritic = isGreekDiacritic;
                        break;
                    case '2':  // Basic Hebrew
                        g1_map = hebrew;
                        g1_mapName = "hebrew";
                        g1_isDiacritic = isHebrewDiacritic;
                        break;
                    case 'N':  // Basic Cyrillic
                        g1_map = cyrillic;
                        g1_mapName = "cyrillic";
                        g1_isDiacritic = isCyrillicDiacritic;
                        break;
                    case 'Q':  // Extended Cyrillic
                        g1_map = extendedCyrillic;
                        g1_mapName = "extendedCyrillic";
                        g1_isDiacritic = isExtendedCyrillicDiacritic;
                        break;
                    case '3':  // Basic Arabic
                        g1_map = arabic;
                        g1_mapName = "arabic";
                        g1_isDiacritic = isArabicDiacritic;
                        break;
                    case '4':  // Extended Arabic
                        g1_map = extendedArabic;
                        g1_mapName = "extendedArabic";
                        g1_isDiacritic = isExtendedArabicDiacritic;
                        break;
                    default:
                        throw new CharConversionException(
                                "Unsupported USM-94 escape sequence: 'Esc-" + saveOne + "-" +
                                        (char) oneByte + "'");
                }
                break;
            case '$':
                saveOne = oneByte;
                oneByte = (byteBuf[++byteOffset] & 0xff);
                switch (oneByte) {
                    case '1':  // Chinese Japanese Korean (EACC)
                        isEACC = true;
                        mapName = "EACC";
                        break;
                    default:
                        throw new CharConversionException(
                                "Unsupported USM-94 escape sequence: 'Esc-" + saveOne + "-" +
                                        (char) oneByte + "'");
                }
                break;
            default:
                throw new CharConversionException(
                        "Unsupported USM-94 escape sequence: 'Esc-0x" +
                                Integer.toHexString(oneByte) + "'");
        }
    }


    /*public static void main(String[] args) {
      if(MakeMain) {
        DataDir      rec=null;
        int          i, numrecs=1, recnum, skiprecs=0;
        RecordHandler recordHandler=null;
        String       filename="marc.USMARC", usage;

        usage="usage: java ByteToCharUSM94 -i<input.USMARC> [-n<numrecs>] "+
            "[-s<skipnum>]\n"+
            "\t-i defaults to -imarc.USMARC\n"+
            "\t-n defaults to -n1\n"+
            "\t-s defaults to -s0";

        try {
            recordHandler = RecordHandler.getHandler("USMARC");
        } catch (UnrecognizedRecordHandlerException e) {
            System.out.println("The USMARC RecordHandler requires a byte to "+
            "character converter that it can't find.");
            e.printStackTrace();
        }

        try {
            recordHandler.Input(filename);
        } catch (IOException e) {
            System.out.println("Can't open input file (" + filename +
                    ")!  " + e);
        }

        for(i=0; i<args.length; i++) {
            System.out.println("\t" + args[i]);

            if(args[i].charAt(0)=='-') {
                switch(args[i].charAt(1)) {
                    case 'i':
                        filename=args[i].substring(2);
                        break;

                    case 'n':
                        numrecs=Integer.parseInt(args[i].substring(2));
                        break;

                    case 's':
                        skiprecs=Integer.parseInt(args[i].substring(2));
                        break;

                    default:
                        System.out.println(usage);
                        System.exit(16);
                }
            }
        }

        for(recnum=0; recnum<skiprecs; recnum++) {
            try {
                recordHandler.loadRecord();
            }
            catch(MalformedRecordException e) {
                System.out.println("Malformed Record detected!  "+e);
                e.printStackTrace();
            }
            catch(EOFException e) {
                System.out.println("Unexpected end of input detected!  "+e);
                e.printStackTrace();
            }
            catch(IOException e) {
                System.out.println("IO error reading input!  "+e);
                e.printStackTrace();
            }
        }

        for(i=0; i<numrecs; i++, recnum++) {
            if(i%1000==0)
                System.out.print(i+"\r");
            try {
                rec=recordHandler.getNextRecord();
            }
            catch(EOFException e) {
                System.out.println("Unexpected EndOfFile detected while"+
                    " reading input!");
                e.printStackTrace();
            }
            catch(IOException e) {
                System.out.println("IO error reading input!");
                e.printStackTrace();
            }
            catch(MalformedRecordException e) {
                System.out.println("Bad Record Encountered!");
                e.printStackTrace();
            }
            System.out.println("\n"+rec);
        }
      }
    }*/
}