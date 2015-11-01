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

import java.io.Reader;

public class CQLParser implements CQLTokens {
    private int yyss = 100;
    private int yytok;
    private int yysp = 0;
    private int[] yyst;
    protected int yyerrno = (-1);
    private Object[] yysv;
    private Object yyrv;

    public boolean parse() {
        int yyn = 0;
        yysp = 0;
        yyst = new int[yyss];
        yyerrno = (-1);
        yysv = new Object[yyss];
        yytok = (lexer.getToken()
                 );
    loop:
        for (;;) {
            switch (yyn) {
                case 0:
                    yyst[yysp] = 0;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 61:
                    yyn = yys0();
                    continue;

                case 1:
                    yyst[yysp] = 1;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 62:
                    switch (yytok) {
                        case ENDINPUT:
                            yyn = 122;
                            continue;
                    }
                    yyn = 125;
                    continue;

                case 2:
                    yyst[yysp] = 2;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 63:
                    yyn = yys2();
                    continue;

                case 3:
                    yyst[yysp] = 3;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 64:
                    switch (yytok) {
                        case RPAR:
                            yyn = yyerr(0, 125);
                            continue;
                        case SORTBY:
                            yyn = 23;
                            continue;
                        case ENDINPUT:
                            yyn = yyr3();
                            continue;
                    }
                    yyn = 125;
                    continue;

                case 4:
                    yyst[yysp] = 4;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 65:
                    yyn = yys4();
                    continue;

                case 5:
                    yyst[yysp] = 5;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 66:
                    yyn = yys5();
                    continue;

                case 6:
                    yyst[yysp] = 6;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 67:
                    yyn = yys6();
                    continue;

                case 7:
                    yyst[yysp] = 7;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 68:
                    yyn = yys7();
                    continue;

                case 8:
                    yyst[yysp] = 8;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 69:
                    yyn = yys8();
                    continue;

                case 9:
                    yyst[yysp] = 9;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 70:
                    yyn = yys9();
                    continue;

                case 10:
                    yyst[yysp] = 10;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 71:
                    yyn = yys10();
                    continue;

                case 11:
                    yyst[yysp] = 11;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 72:
                    switch (yytok) {
                        case ENDINPUT:
                            yyn = yyr1();
                            continue;
                    }
                    yyn = 125;
                    continue;

                case 12:
                    yyst[yysp] = 12;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 73:
                    yyn = yys12();
                    continue;

                case 13:
                    yyst[yysp] = 13;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 74:
                    yyn = yys13();
                    continue;

                case 14:
                    yyst[yysp] = 14;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 75:
                    yyn = yys14();
                    continue;

                case 15:
                    yyst[yysp] = 15;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 76:
                    yyn = yys15();
                    continue;

                case 16:
                    yyst[yysp] = 16;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 77:
                    yyn = yys16();
                    continue;

                case 17:
                    yyst[yysp] = 17;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 78:
                    yyn = yys17();
                    continue;

                case 18:
                    yyst[yysp] = 18;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 79:
                    yyn = yys18();
                    continue;

                case 19:
                    yyst[yysp] = 19;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 80:
                    yyn = yys19();
                    continue;

                case 20:
                    yyst[yysp] = 20;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 81:
                    yyn = yys20();
                    continue;

                case 21:
                    yyst[yysp] = 21;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 82:
                    yyn = yys21();
                    continue;

                case 22:
                    yyst[yysp] = 22;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 83:
                    switch (yytok) {
                        case error:
                        case NL:
                            yyn = 125;
                            continue;
                    }
                    yyn = yyr46();
                    continue;

                case 23:
                    yyst[yysp] = 23;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 84:
                    switch (yytok) {
                        case SIMPLESTRING:
                            yyn = 22;
                            continue;
                    }
                    yyn = 125;
                    continue;

                case 24:
                    yyst[yysp] = 24;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 85:
                    yyn = yys24();
                    continue;

                case 25:
                    yyst[yysp] = 25;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 86:
                    yyn = yys25();
                    continue;

                case 26:
                    yyst[yysp] = 26;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 87:
                    yyn = yys26();
                    continue;

                case 27:
                    yyst[yysp] = 27;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 88:
                    yyn = yys27();
                    continue;

                case 28:
                    yyst[yysp] = 28;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 89:
                    yyn = yys28();
                    continue;

                case 29:
                    yyst[yysp] = 29;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 90:
                    yyn = yys29();
                    continue;

                case 30:
                    yyst[yysp] = 30;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 91:
                    yyn = yys30();
                    continue;

                case 31:
                    yyst[yysp] = 31;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 92:
                    yyn = yys31();
                    continue;

                case 32:
                    yyst[yysp] = 32;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 93:
                    yyn = yys32();
                    continue;

                case 33:
                    yyst[yysp] = 33;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 94:
                    yyn = yys33();
                    continue;

                case 34:
                    yyst[yysp] = 34;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 95:
                    yyn = yys34();
                    continue;

                case 35:
                    yyst[yysp] = 35;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 96:
                    yyn = yys35();
                    continue;

                case 36:
                    yyst[yysp] = 36;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 97:
                    switch (yytok) {
                        case ENDINPUT:
                        case SORTBY:
                        case RPAR:
                            yyn = yyr8();
                            continue;
                    }
                    yyn = 125;
                    continue;

                case 37:
                    yyst[yysp] = 37;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 98:
                    yyn = yys37();
                    continue;

                case 38:
                    yyst[yysp] = 38;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 99:
                    yyn = yys38();
                    continue;

                case 39:
                    yyst[yysp] = 39;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 100:
                    yyn = yys39();
                    continue;

                case 40:
                    yyst[yysp] = 40;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 101:
                    yyn = yys40();
                    continue;

                case 41:
                    yyst[yysp] = 41;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 102:
                    switch (yytok) {
                        case RPAR:
                            yyn = 53;
                            continue;
                    }
                    yyn = 125;
                    continue;

                case 42:
                    yyst[yysp] = 42;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 103:
                    switch (yytok) {
                        case SLASH:
                            yyn = 48;
                            continue;
                        case ENDINPUT:
                        case SIMPLESTRING:
                            yyn = yyr7();
                            continue;
                    }
                    yyn = 125;
                    continue;

                case 43:
                    yyst[yysp] = 43;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 104:
                    switch (yytok) {
                        case ENDINPUT:
                        case SLASH:
                        case SIMPLESTRING:
                            yyn = yyr39();
                            continue;
                    }
                    yyn = 125;
                    continue;

                case 44:
                    yyst[yysp] = 44;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 105:
                    switch (yytok) {
                        case ENDINPUT:
                        case SIMPLESTRING:
                            yyn = yyr5();
                            continue;
                    }
                    yyn = 125;
                    continue;

                case 45:
                    yyst[yysp] = 45;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 106:
                    switch (yytok) {
                        case SIMPLESTRING:
                            yyn = 22;
                            continue;
                        case ENDINPUT:
                            yyn = yyr2();
                            continue;
                    }
                    yyn = 125;
                    continue;

                case 46:
                    yyst[yysp] = 46;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 107:
                    yyn = yys46();
                    continue;

                case 47:
                    yyst[yysp] = 47;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 108:
                    yyn = yys47();
                    continue;

                case 48:
                    yyst[yysp] = 48;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 109:
                    switch (yytok) {
                        case SIMPLESTRING:
                            yyn = 22;
                            continue;
                    }
                    yyn = 125;
                    continue;

                case 49:
                    yyst[yysp] = 49;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 110:
                    yyn = yys49();
                    continue;

                case 50:
                    yyst[yysp] = 50;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 111:
                    yyn = yys50();
                    continue;

                case 51:
                    yyst[yysp] = 51;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 112:
                    yyn = yys51();
                    continue;

                case 52:
                    yyst[yysp] = 52;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 113:
                    yyn = yys52();
                    continue;

                case 53:
                    yyst[yysp] = 53;
                    yysv[yysp] = (lexer.getSemantic()
                                 );
                    yytok = (lexer.nextToken()
                            );
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 114:
                    yyn = yys53();
                    continue;

                case 54:
                    yyst[yysp] = 54;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 115:
                    switch (yytok) {
                        case SLASH:
                            yyn = 48;
                            continue;
                        case ENDINPUT:
                        case SIMPLESTRING:
                            yyn = yyr6();
                            continue;
                    }
                    yyn = 125;
                    continue;

                case 55:
                    yyst[yysp] = 55;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 116:
                    switch (yytok) {
                        case ENDINPUT:
                        case SIMPLESTRING:
                            yyn = yyr4();
                            continue;
                    }
                    yyn = 125;
                    continue;

                case 56:
                    yyst[yysp] = 56;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 117:
                    yyn = yys56();
                    continue;

                case 57:
                    yyst[yysp] = 57;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 118:
                    yyn = yys57();
                    continue;

                case 58:
                    yyst[yysp] = 58;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 119:
                    yyn = yys58();
                    continue;

                case 59:
                    yyst[yysp] = 59;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 120:
                    yyn = yys59();
                    continue;

                case 60:
                    yyst[yysp] = 60;
                    if (++yysp>=yyst.length) {
                        yyexpand();
                    }
                case 121:
                    yyn = yys60();
                    continue;

                case 122:
                    return true;
                case 123:
                    yyerror("stack overflow");
                case 124:
                    return false;
                case 125:
                    yyerror("syntax error");
                    return false;
            }
        }
    }

    protected void yyexpand() {
        int[] newyyst = new int[2*yyst.length];
        Object[] newyysv = new Object[2*yyst.length];
        for (int i=0; i<yyst.length; i++) {
            newyyst[i] = yyst[i];
            newyysv[i] = yysv[i];
        }
        yyst = newyyst;
        yysv = newyysv;
    }

    private int yys0() {
        switch (yytok) {
            case AND:
                return 13;
            case FLOAT:
                return 14;
            case GT:
                return 15;
            case INTEGER:
                return 16;
            case LPAR:
                return 17;
            case NOT:
                return 18;
            case OR:
                return 19;
            case PROX:
                return 20;
            case QUOTEDSTRING:
                return 21;
            case SIMPLESTRING:
                return 22;
        }
        return 125;
    }

    private int yys2() {
        switch (yytok) {
            case NE:
            case NAMEDCOMPARITORS:
            case GE:
            case NL:
            case EXACT:
            case LE:
            case error:
            case LT:
                return 125;
        }
        return yyr41();
    }

    private int yys4() {
        switch (yytok) {
            case NE:
            case NAMEDCOMPARITORS:
            case GE:
            case NL:
            case EXACT:
            case LE:
            case error:
            case LT:
                return 125;
        }
        return yyr40();
    }

    private int yys5() {
        switch (yytok) {
            case EQ:
                return 28;
            case EXACT:
                return 29;
            case GE:
                return 30;
            case GT:
                return 31;
            case LE:
                return 32;
            case LT:
                return 33;
            case NAMEDCOMPARITORS:
                return 34;
            case NE:
                return 35;
        }
        return 125;
    }

    private int yys6() {
        switch (yytok) {
            case AND:
                return 13;
            case FLOAT:
                return 14;
            case GT:
                return 15;
            case INTEGER:
                return 16;
            case LPAR:
                return 17;
            case NOT:
                return 18;
            case OR:
                return 19;
            case PROX:
                return 20;
            case QUOTEDSTRING:
                return 21;
            case SIMPLESTRING:
                return 22;
        }
        return 125;
    }

    private int yys7() {
        switch (yytok) {
            case NE:
            case NAMEDCOMPARITORS:
            case GE:
            case NL:
            case EXACT:
            case LE:
            case error:
            case LT:
                return 125;
        }
        return yyr45();
    }

    private int yys8() {
        switch (yytok) {
            case AND:
                return 13;
            case NOT:
                return 18;
            case OR:
                return 19;
            case PROX:
                return 20;
            case ENDINPUT:
            case SORTBY:
            case RPAR:
                return yyr9();
        }
        return 125;
    }

    private int yys9() {
        switch (yytok) {
            case OR:
            case NOT:
            case ENDINPUT:
            case SORTBY:
            case RPAR:
            case PROX:
            case AND:
                return yyr13();
        }
        return 125;
    }

    private int yys10() {
        switch (yytok) {
            case FLOAT:
            case QUOTEDSTRING:
            case LPAR:
            case NL:
            case INTEGER:
            case error:
                return 125;
            case SIMPLESTRING:
                return yyerr(2, 125);
            case SLASH:
                return yyerr(3, 125);
            case OR:
            case NOT:
            case ENDINPUT:
            case SORTBY:
            case RPAR:
            case PROX:
            case AND:
                return yyr44();
        }
        return yyr39();
    }

    private int yys12() {
        switch (yytok) {
            case SIMPLESTRING:
                return yyerr(1, 125);
            case OR:
            case NOT:
            case ENDINPUT:
            case SORTBY:
            case RPAR:
            case PROX:
            case AND:
                return yyr22();
        }
        return 125;
    }

    private int yys13() {
        switch (yytok) {
            case NE:
            case NAMEDCOMPARITORS:
            case GE:
            case NL:
            case EXACT:
            case LE:
            case error:
            case LT:
                return 125;
        }
        return yyr16();
    }

    private int yys14() {
        switch (yytok) {
            case NE:
            case NAMEDCOMPARITORS:
            case GE:
            case NL:
            case EXACT:
            case LE:
            case error:
            case LT:
                return 125;
        }
        return yyr43();
    }

    private int yys15() {
        switch (yytok) {
            case AND:
                return 13;
            case FLOAT:
                return 14;
            case INTEGER:
                return 16;
            case NOT:
                return 18;
            case OR:
                return 19;
            case PROX:
                return 20;
            case QUOTEDSTRING:
                return 21;
            case SIMPLESTRING:
                return 22;
        }
        return 125;
    }

    private int yys16() {
        switch (yytok) {
            case NE:
            case NAMEDCOMPARITORS:
            case GE:
            case NL:
            case EXACT:
            case LE:
            case error:
            case LT:
                return 125;
        }
        return yyr42();
    }

    private int yys17() {
        switch (yytok) {
            case AND:
                return 13;
            case FLOAT:
                return 14;
            case GT:
                return 15;
            case INTEGER:
                return 16;
            case LPAR:
                return 17;
            case NOT:
                return 18;
            case OR:
                return 19;
            case PROX:
                return 20;
            case QUOTEDSTRING:
                return 21;
            case SIMPLESTRING:
                return 22;
        }
        return 125;
    }

    private int yys18() {
        switch (yytok) {
            case NE:
            case NAMEDCOMPARITORS:
            case GE:
            case NL:
            case EXACT:
            case LE:
            case error:
            case LT:
                return 125;
        }
        return yyr18();
    }

    private int yys19() {
        switch (yytok) {
            case NE:
            case NAMEDCOMPARITORS:
            case GE:
            case NL:
            case EXACT:
            case LE:
            case error:
            case LT:
                return 125;
        }
        return yyr17();
    }

    private int yys20() {
        switch (yytok) {
            case NE:
            case NAMEDCOMPARITORS:
            case GE:
            case NL:
            case EXACT:
            case LE:
            case error:
            case LT:
                return 125;
        }
        return yyr19();
    }

    private int yys21() {
        switch (yytok) {
            case NE:
            case NAMEDCOMPARITORS:
            case GE:
            case NL:
            case EXACT:
            case LE:
            case error:
            case LT:
                return 125;
        }
        return yyr47();
    }

    private int yys24() {
        switch (yytok) {
            case SLASH:
                return 48;
            case OR:
            case NOT:
            case INTEGER:
            case SIMPLESTRING:
            case QUOTEDSTRING:
            case PROX:
            case FLOAT:
            case AND:
                return yyr24();
        }
        return 125;
    }

    private int yys25() {
        switch (yytok) {
            case OR:
            case NOT:
            case INTEGER:
            case SLASH:
            case SIMPLESTRING:
            case QUOTEDSTRING:
            case PROX:
            case FLOAT:
            case AND:
                return yyr25();
        }
        return 125;
    }

    private int yys26() {
        switch (yytok) {
            case OR:
            case NOT:
            case INTEGER:
            case SLASH:
            case SIMPLESTRING:
            case QUOTEDSTRING:
            case PROX:
            case FLOAT:
            case AND:
                return yyr26();
        }
        return 125;
    }

    private int yys27() {
        switch (yytok) {
            case AND:
                return 13;
            case FLOAT:
                return 14;
            case INTEGER:
                return 16;
            case NOT:
                return 18;
            case OR:
                return 19;
            case PROX:
                return 20;
            case QUOTEDSTRING:
                return 21;
            case SIMPLESTRING:
                return 22;
        }
        return 125;
    }

    private int yys28() {
        switch (yytok) {
            case OR:
            case NOT:
            case INTEGER:
            case SLASH:
            case SIMPLESTRING:
            case QUOTEDSTRING:
            case PROX:
            case FLOAT:
            case AND:
                return yyr27();
        }
        return 125;
    }

    private int yys29() {
        switch (yytok) {
            case OR:
            case NOT:
            case INTEGER:
            case SLASH:
            case SIMPLESTRING:
            case QUOTEDSTRING:
            case PROX:
            case FLOAT:
            case AND:
                return yyr33();
        }
        return 125;
    }

    private int yys30() {
        switch (yytok) {
            case OR:
            case NOT:
            case INTEGER:
            case SLASH:
            case SIMPLESTRING:
            case QUOTEDSTRING:
            case PROX:
            case FLOAT:
            case AND:
                return yyr30();
        }
        return 125;
    }

    private int yys31() {
        switch (yytok) {
            case OR:
            case NOT:
            case INTEGER:
            case SLASH:
            case SIMPLESTRING:
            case QUOTEDSTRING:
            case PROX:
            case FLOAT:
            case AND:
                return yyr29();
        }
        return 125;
    }

    private int yys32() {
        switch (yytok) {
            case OR:
            case NOT:
            case INTEGER:
            case SLASH:
            case SIMPLESTRING:
            case QUOTEDSTRING:
            case PROX:
            case FLOAT:
            case AND:
                return yyr31();
        }
        return 125;
    }

    private int yys33() {
        switch (yytok) {
            case OR:
            case NOT:
            case INTEGER:
            case SLASH:
            case SIMPLESTRING:
            case QUOTEDSTRING:
            case PROX:
            case FLOAT:
            case AND:
                return yyr28();
        }
        return 125;
    }

    private int yys34() {
        switch (yytok) {
            case OR:
            case NOT:
            case INTEGER:
            case SLASH:
            case SIMPLESTRING:
            case QUOTEDSTRING:
            case PROX:
            case FLOAT:
            case AND:
                return yyr34();
        }
        return 125;
    }

    private int yys35() {
        switch (yytok) {
            case OR:
            case NOT:
            case INTEGER:
            case SLASH:
            case SIMPLESTRING:
            case QUOTEDSTRING:
            case PROX:
            case FLOAT:
            case AND:
                return yyr32();
        }
        return 125;
    }

    private int yys37() {
        switch (yytok) {
            case AND:
                return 13;
            case FLOAT:
                return 14;
            case INTEGER:
                return 16;
            case LPAR:
                return 17;
            case NOT:
                return 18;
            case OR:
                return 19;
            case PROX:
                return 20;
            case QUOTEDSTRING:
                return 21;
            case SIMPLESTRING:
                return 22;
        }
        return 125;
    }

    private int yys38() {
        switch (yytok) {
            case SLASH:
                return 48;
            case OR:
            case NOT:
            case INTEGER:
            case SIMPLESTRING:
            case LPAR:
            case QUOTEDSTRING:
            case PROX:
            case FLOAT:
            case AND:
                return yyr15();
        }
        return 125;
    }

    private int yys39() {
        switch (yytok) {
            case NE:
            case NAMEDCOMPARITORS:
            case GE:
            case NL:
            case EXACT:
            case LE:
            case error:
            case LT:
                return 125;
        }
        return yyr44();
    }

    private int yys40() {
        switch (yytok) {
            case EQ:
                return 52;
            case OR:
            case NOT:
            case INTEGER:
            case GT:
            case SIMPLESTRING:
            case LPAR:
            case QUOTEDSTRING:
            case PROX:
            case FLOAT:
            case AND:
                return yyr11();
        }
        return 125;
    }

    private int yys46() {
        switch (yytok) {
            case OR:
            case NOT:
            case INTEGER:
            case ENDINPUT:
            case SLASH:
            case SIMPLESTRING:
            case LPAR:
            case QUOTEDSTRING:
            case PROX:
            case FLOAT:
            case AND:
                return yyr36();
        }
        return 125;
    }

    private int yys47() {
        switch (yytok) {
            case SLASH:
                return 48;
            case OR:
            case NOT:
            case INTEGER:
            case SIMPLESTRING:
            case QUOTEDSTRING:
            case PROX:
            case FLOAT:
            case AND:
                return yyr23();
        }
        return 125;
    }

    private int yys49() {
        switch (yytok) {
            case OR:
            case NOT:
            case ENDINPUT:
            case SORTBY:
            case RPAR:
            case PROX:
            case AND:
                return yyr21();
        }
        return 125;
    }

    private int yys50() {
        switch (yytok) {
            case OR:
            case NOT:
            case ENDINPUT:
            case SORTBY:
            case RPAR:
            case PROX:
            case AND:
                return yyr12();
        }
        return 125;
    }

    private int yys51() {
        switch (yytok) {
            case SLASH:
                return 48;
            case OR:
            case NOT:
            case INTEGER:
            case SIMPLESTRING:
            case LPAR:
            case QUOTEDSTRING:
            case PROX:
            case FLOAT:
            case AND:
                return yyr14();
        }
        return 125;
    }

    private int yys52() {
        switch (yytok) {
            case AND:
                return 13;
            case FLOAT:
                return 14;
            case INTEGER:
                return 16;
            case NOT:
                return 18;
            case OR:
                return 19;
            case PROX:
                return 20;
            case QUOTEDSTRING:
                return 21;
            case SIMPLESTRING:
                return 22;
        }
        return 125;
    }

    private int yys53() {
        switch (yytok) {
            case OR:
            case NOT:
            case ENDINPUT:
            case SORTBY:
            case RPAR:
            case PROX:
            case AND:
                return yyr20();
        }
        return 125;
    }

    private int yys56() {
        switch (yytok) {
            case OR:
            case NOT:
            case INTEGER:
            case ENDINPUT:
            case SLASH:
            case SIMPLESTRING:
            case LPAR:
            case QUOTEDSTRING:
            case PROX:
            case FLOAT:
            case AND:
                return yyr35();
        }
        return 125;
    }

    private int yys57() {
        switch (yytok) {
            case RPAR:
            case NAMEDCOMPARITORS:
            case error:
            case SORTBY:
            case NL:
                return 125;
            case EQ:
                return 28;
            case EXACT:
                return 29;
            case GE:
                return 30;
            case GT:
                return 31;
            case LE:
                return 32;
            case LT:
                return 33;
            case NE:
                return 35;
        }
        return yyr38();
    }

    private int yys58() {
        switch (yytok) {
            case OR:
            case NOT:
            case INTEGER:
            case GT:
            case SIMPLESTRING:
            case LPAR:
            case QUOTEDSTRING:
            case PROX:
            case FLOAT:
            case AND:
                return yyr10();
        }
        return 125;
    }

    private int yys59() {
        switch (yytok) {
            case AND:
                return 13;
            case FLOAT:
                return 14;
            case INTEGER:
                return 16;
            case NOT:
                return 18;
            case OR:
                return 19;
            case PROX:
                return 20;
            case QUOTEDSTRING:
                return 21;
            case SIMPLESTRING:
                return 22;
        }
        return 125;
    }

    private int yys60() {
        switch (yytok) {
            case OR:
            case NOT:
            case INTEGER:
            case ENDINPUT:
            case SLASH:
            case SIMPLESTRING:
            case LPAR:
            case QUOTEDSTRING:
            case PROX:
            case FLOAT:
            case AND:
                return yyr37();
        }
        return 125;
    }

    private int yyr1() { // cql : sortedQuery
        {
        this.cql = ((SortedQuery)yysv[yysp-1]);
        yyrv = this.cql;
    }
        yysv[yysp-=1] = yyrv;
        return 1;
    }

    private int yyr14() { // booleanGroup : boolean modifierList
        {
        yyrv = new BooleanGroup(BooleanOperator.forToken(yysv[yysp-2]), ((ModifierList)yysv[yysp-1]));
    }
        yysv[yysp-=2] = yyrv;
        return 37;
    }

    private int yyr15() { // booleanGroup : boolean
        {
        yyrv = new BooleanGroup(BooleanOperator.forToken(yysv[yysp-1]));
    }
        yysv[yysp-=1] = yyrv;
        return 37;
    }

    private int yyr25() { // comparitor : comparitorSymbol
        {
        yyrv = Comparitor.forToken(yysv[yysp-1]);
    }
        yysv[yysp-=1] = yyrv;
        return 24;
    }

    private int yyr26() { // comparitor : namedComparitor
        {
        yyrv = Comparitor.forToken(yysv[yysp-1]);
    }
        yysv[yysp-=1] = yyrv;
        return 24;
    }

    private int yyr27() { // comparitorSymbol : EQ
        yysp -= 1;
        return yypcomparitorSymbol();
    }

    private int yyr28() { // comparitorSymbol : LT
        yysp -= 1;
        return yypcomparitorSymbol();
    }

    private int yyr29() { // comparitorSymbol : GT
        yysp -= 1;
        return yypcomparitorSymbol();
    }

    private int yyr30() { // comparitorSymbol : GE
        yysp -= 1;
        return yypcomparitorSymbol();
    }

    private int yyr31() { // comparitorSymbol : LE
        yysp -= 1;
        return yypcomparitorSymbol();
    }

    private int yyr32() { // comparitorSymbol : NE
        yysp -= 1;
        return yypcomparitorSymbol();
    }

    private int yyr33() { // comparitorSymbol : EXACT
        yysp -= 1;
        return yypcomparitorSymbol();
    }

    private int yypcomparitorSymbol() {
        switch (yyst[yysp-1]) {
            case 5: return 25;
            default: return 59;
        }
    }

    private int yyr16() { // boolean : AND
        yysp -= 1;
        return yypboolean();
    }

    private int yyr17() { // boolean : OR
        yysp -= 1;
        return yypboolean();
    }

    private int yyr18() { // boolean : NOT
        yysp -= 1;
        return yypboolean();
    }

    private int yyr19() { // boolean : PROX
        yysp -= 1;
        return yypboolean();
    }

    private int yypboolean() {
        switch (yyst[yysp-1]) {
            case 8: return 38;
            default: return 2;
        }
    }

    private int yyr8() { // cqlQuery : prefixAssignment cqlQuery
        {
        yyrv = new Query(((PrefixAssignment)yysv[yysp-2]), ((Query)yysv[yysp-1]));
    }
        yysv[yysp-=2] = yyrv;
        return yypcqlQuery();
    }

    private int yyr9() { // cqlQuery : scopedClause
        {
        yyrv = new Query(((ScopedClause)yysv[yysp-1]));
    }
        yysv[yysp-=1] = yyrv;
        return yypcqlQuery();
    }

    private int yypcqlQuery() {
        switch (yyst[yysp-1]) {
            case 6: return 36;
            case 0: return 3;
            default: return 41;
        }
    }

    private int yyr44() { // identifier : simpleName
        {
        yyrv = new Identifier(((SimpleName)yysv[yysp-1]));
    }
        yysv[yysp-=1] = yyrv;
        return 4;
    }

    private int yyr45() { // identifier : quotedString
        {
        yyrv = new Identifier(((String)yysv[yysp-1]));
    }
        yysv[yysp-=1] = yyrv;
        return 4;
    }

    private int yyr39() { // index : simpleName
        {
        yyrv = new Index(((SimpleName)yysv[yysp-1]));
    }
        yysv[yysp-=1] = yyrv;
        switch (yyst[yysp-1]) {
            case 45: return 42;
            case 23: return 42;
            default: return 5;
        }
    }

    private int yyr37() { // modifier : SLASH simpleName comparitorSymbol term
        {
        yyrv = new Modifier(((SimpleName)yysv[yysp-3]), Comparitor.forToken(yysv[yysp-2]), ((Term)yysv[yysp-1]));
    }
        yysv[yysp-=4] = yyrv;
        return yypmodifier();
    }

    private int yyr38() { // modifier : SLASH simpleName
        {
        yyrv = new Modifier(((SimpleName)yysv[yysp-1]));
    }
        yysv[yysp-=2] = yyrv;
        return yypmodifier();
    }

    private int yypmodifier() {
        switch (yyst[yysp-1]) {
            case 42: return 46;
            case 38: return 46;
            case 24: return 46;
            default: return 56;
        }
    }

    private int yyr35() { // modifierList : modifierList modifier
        {
        yyrv = new ModifierList(((ModifierList)yysv[yysp-2]),((Modifier)yysv[yysp-1]));
    }
        yysv[yysp-=2] = yyrv;
        return yypmodifierList();
    }

    private int yyr36() { // modifierList : modifier
        {
        yyrv = new ModifierList(((Modifier)yysv[yysp-1]));
    }
        yysv[yysp-=1] = yyrv;
        return yypmodifierList();
    }

    private int yypmodifierList() {
        switch (yyst[yysp-1]) {
            case 38: return 51;
            case 24: return 47;
            default: return 54;
        }
    }

    private int yyr34() { // namedComparitor : NAMEDCOMPARITORS
        yysp -= 1;
        return 26;
    }

    private int yyr10() { // prefixAssignment : GT term EQ term
        {
        yyrv = new PrefixAssignment(((Term)yysv[yysp-3]), ((Term)yysv[yysp-1]));
    }
        yysv[yysp-=4] = yyrv;
        return 6;
    }

    private int yyr11() { // prefixAssignment : GT term
        {
        yyrv = new PrefixAssignment(((Term)yysv[yysp-1]));
    }
        yysv[yysp-=2] = yyrv;
        return 6;
    }

    private int yyr47() { // quotedString : QUOTEDSTRING
        {
        yyrv = ((String)yysv[yysp-1]);
    }
        yysv[yysp-=1] = yyrv;
        return 7;
    }

    private int yyr23() { // relation : comparitor modifierList
        {
        yyrv = new Relation(((Comparitor)yysv[yysp-2]), ((ModifierList)yysv[yysp-1]));
    }
        yysv[yysp-=2] = yyrv;
        return 27;
    }

    private int yyr24() { // relation : comparitor
        {
        yyrv = new Relation(((Comparitor)yysv[yysp-1]));
    }
        yysv[yysp-=1] = yyrv;
        return 27;
    }

    private int yyr12() { // scopedClause : scopedClause booleanGroup searchClause
        {
        yyrv = new ScopedClause(((ScopedClause)yysv[yysp-3]), ((BooleanGroup)yysv[yysp-2]), ((SearchClause)yysv[yysp-1]) );
    }
        yysv[yysp-=3] = yyrv;
        return 8;
    }

    private int yyr13() { // scopedClause : searchClause
        {
        yyrv = new ScopedClause(((SearchClause)yysv[yysp-1]));
    }
        yysv[yysp-=1] = yyrv;
        return 8;
    }

    private int yyr20() { // searchClause : LPAR cqlQuery RPAR
        {
        yyrv = new SearchClause(((Query)yysv[yysp-2]));
    }
        yysv[yysp-=3] = yyrv;
        return yypsearchClause();
    }

    private int yyr21() { // searchClause : index relation term
        {
        yyrv = new SearchClause(((Index)yysv[yysp-3]), ((Relation)yysv[yysp-2]), ((Term)yysv[yysp-1]));
    }
        yysv[yysp-=3] = yyrv;
        return yypsearchClause();
    }

    private int yyr22() { // searchClause : term
        {
        yyrv = new SearchClause(((Term)yysv[yysp-1]));
    }
        yysv[yysp-=1] = yyrv;
        return yypsearchClause();
    }

    private int yypsearchClause() {
        switch (yyst[yysp-1]) {
            case 37: return 50;
            default: return 9;
        }
    }

    private int yyr46() { // simpleName : SIMPLESTRING
        {
        yyrv = new SimpleName(((String)yysv[yysp-1]));
    }
        yysv[yysp-=1] = yyrv;
        switch (yyst[yysp-1]) {
            case 48: return 57;
            case 45: return 43;
            case 37: return 10;
            case 23: return 43;
            case 17: return 10;
            case 6: return 10;
            case 0: return 10;
            default: return 39;
        }
    }

    private int yyr6() { // singleSpec : index modifierList
        {
        yyrv = new SingleSpec(((Index)yysv[yysp-2]), ((ModifierList)yysv[yysp-1]));
    }
        yysv[yysp-=2] = yyrv;
        return yypsingleSpec();
    }

    private int yyr7() { // singleSpec : index
        {
        yyrv = new SingleSpec(((Index)yysv[yysp-1]));
    }
        yysv[yysp-=1] = yyrv;
        return yypsingleSpec();
    }

    private int yypsingleSpec() {
        switch (yyst[yysp-1]) {
            case 23: return 44;
            default: return 55;
        }
    }

    private int yyr4() { // sortSpec : sortSpec singleSpec
        {
        yyrv = new SortSpec(((SortSpec)yysv[yysp-2]), ((SingleSpec)yysv[yysp-1]));
    }
        yysv[yysp-=2] = yyrv;
        return 45;
    }

    private int yyr5() { // sortSpec : singleSpec
        {
        yyrv = new SortSpec(((SingleSpec)yysv[yysp-1]));
    }
        yysv[yysp-=1] = yyrv;
        return 45;
    }

    private int yyr2() { // sortedQuery : cqlQuery SORTBY sortSpec
        {
        yyrv = new SortedQuery(((Query)yysv[yysp-3]), ((SortSpec)yysv[yysp-1]));
    }
        yysv[yysp-=3] = yyrv;
        return 11;
    }

    private int yyr3() { // sortedQuery : cqlQuery
        {
        yyrv = new SortedQuery(((Query)yysv[yysp-1]));
    }
        yysv[yysp-=1] = yyrv;
        return 11;
    }

    private int yyr40() { // term : identifier
        {
        yyrv = new Term(((Identifier)yysv[yysp-1]));
    }
        yysv[yysp-=1] = yyrv;
        return yypterm();
    }

    private int yyr41() { // term : boolean
        {
        yyrv = new Term(BooleanOperator.forToken(yysv[yysp-1]).getToken());
    }
        yysv[yysp-=1] = yyrv;
        return yypterm();
    }

    private int yyr42() { // term : INTEGER
        {
        yyrv = new Term(((Long)yysv[yysp-1]));
    }
        yysv[yysp-=1] = yyrv;
        return yypterm();
    }

    private int yyr43() { // term : FLOAT
        {
        yyrv = new Term(((Double)yysv[yysp-1]));
    }
        yysv[yysp-=1] = yyrv;
        return yypterm();
    }

    private int yypterm() {
        switch (yyst[yysp-1]) {
            case 59: return 60;
            case 52: return 58;
            case 27: return 49;
            case 15: return 40;
            default: return 12;
        }
    }

    private int yyerr(int e, int n) {
        yyerrno = e;
        return n;
    }
    protected String[] yyerrmsgs = {
        "unexpected closing parenthesis",
        "unexpected string after term",
        "unexpected string after string",
        "unexpected slash after string"
    };


    private CQLLexer lexer;
        
    private SortedQuery cql;

    public CQLParser(Reader reader) {
        this.lexer = new CQLLexer(reader);
        lexer.nextToken();
    }

    public void yyerror (String error) {
        throw new SyntaxException("CQL error at " 
            + "[" + lexer.getLine() + "," + lexer.getColumn() +"]"
            + ": " + (yyerrno >= 0 ? yyerrmsgs[yyerrno] : error) 
            + ": " + lexer.getSemantic());
    }
    
    public SortedQuery getCQLQuery() {
        return cql;
    }

}
