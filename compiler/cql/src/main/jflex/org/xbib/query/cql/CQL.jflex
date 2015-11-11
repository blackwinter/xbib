package org.xbib.query.cql;

import java.io.IOException;

%%
%class CQLLexer
%implements CQLTokens
%unicode 
%integer
%eofval{ 
    return 0; 
%eofval}
%line
%column

%{
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
    
%}
NL  = \n | \r | \r\n
LPAR = "("
RPAR = ")"
AND = [aA][nN][dD]
OR = [oO][rR]
NOT = [nN][oO][tT]
PROX = [pP][rR][oO][xX]
SORTBY = [sS][oO][rR][tT][bB][yY]
SIMPLESTRING = [^ \t\"()=<>\/]+
QUOTEDSTRING = [^\"]
LT = "<"
GT = ">"
EQ = "="
GE = ">="
LE = "<="
NE = "<>"
EXACT = "=="
NAMEDCOMPARITORS =  [cC][qQ][lL] "." [eE][xX][aA][cC][tT] | [eE][xX][aA][cC][tT] |  [cC][qQ][lL] "."  [wW][iI][tT][hH][iI][nN] | [wW][iI][tT][hH][iI][nN] | [cC][qQ][lL] "." [aA][dD][jJ] | [aA][dD][jJ] | [cC][qQ][lL] "." [aA][lL][lL] | [aA][lL][lL] | [cC][qQ][lL] "." [aA][nN][yY] | [aA][nN][yY] | [cC][qQ][lL] "." [eE][nN][cC][lL][oO][sS][eE][sS] | [eE][nN][cC][lL][oO][sS][eE][sS]
INTEGER = 0 | [1-9][0-9]*
FLOAT = [0-9]+ "." [0-9]+
SLASH = "/"

%state STRING2

%%

<YYINITIAL>\"   {
        yybegin(STRING2); 
        sb.setLength(0);
    }

<STRING2> {
 
\\\" { 
        sb.append("\""); 
    }
{QUOTEDSTRING} {
        sb.append(yytext()); 
    }
\"	            { 
        yybegin(YYINITIAL);
        yylval = sb.toString();
        return QUOTEDSTRING;
    }
}

<YYINITIAL>{NL} { 
        return NL;
    }

<YYINITIAL>" "|\t {
    }

<YYINITIAL>{FLOAT}    {
        yylval = Double.parseDouble(yytext());
        return FLOAT;
    }

<YYINITIAL>{INTEGER}    {
        yylval = Long.parseLong(yytext());
        return INTEGER;
    }

<YYINITIAL>{NAMEDCOMPARITORS}  {
        yylval = yytext();
        return NAMEDCOMPARITORS;
    }	

<YYINITIAL>{GE}  {
        yylval = yytext();
        return GE;
    }

<YYINITIAL>{LE}  {
        yylval = yytext();
        return LE;
    }

<YYINITIAL>{NE}  {
        yylval = yytext();
        return NE;
    }

<YYINITIAL>{EXACT}  {
        yylval = yytext();
        return EXACT;
    }

<YYINITIAL>{GT}  {
        yylval = yytext();
        return GT;
    }

<YYINITIAL>{LT}  {
        yylval = yytext();
        return LT;
    }

<YYINITIAL>{EQ}  {
        yylval = yytext();
        return EQ;
    }

<YYINITIAL>{AND}  {
        yylval = yytext();
        return AND;
    }

<YYINITIAL>{OR}  {
        yylval = yytext();
        return OR;
    }

<YYINITIAL>{NOT}  {
        yylval = yytext();
        return NOT;
    }

<YYINITIAL>{PROX}  {
        yylval = yytext();
        return PROX;
    }

<YYINITIAL>{SORTBY}  {
        yylval = yytext();
        return SORTBY; 
    }

<YYINITIAL>{SIMPLESTRING} {
        yylval = yytext();
        return SIMPLESTRING;
    }

<YYINITIAL>{LPAR} {
        yylval = yytext();
        return LPAR;
    }

<YYINITIAL>{RPAR} {
        yylval = yytext();
        return RPAR;
    }

<YYINITIAL>{SLASH} {
        yylval = yytext();
        return SLASH;
    }
