package org.xbib.jacc;

interface JaccTokens {
    int ERROR = -1;
    int ENDINPUT = 0;
    int MARK = 1;
    int CODE = 2;
    int IDENT = 3;
    int CHARLIT = 4;
    int STRLIT = 5;
    int INTLIT = 6;
    int ACTION = 7;
    int TOKEN = 8;
    int TYPE = 9;
    int PREC = 10;
    int LEFT = 11;
    int RIGHT = 12;
    int NONASSOC = 13;
    int START = 14;
    int PACKAGE = 15;
    int CLASS = 16;
    int INTERFACE = 17;
    int EXTENDS = 18;
    int IMPLEMENTS = 19;
    int SEMANTIC = 20;
    int GETTOKEN = 21;
    int NEXTTOKEN = 22;
    int COLON = 58;
    int SEMI = 59;
    int BAR = 124;
    int TOPEN = 60;
    int TCLOSE = 62;
    int BOPEN = 91;
    int BCLOSE = 93;
    int DOT = 46;
}
