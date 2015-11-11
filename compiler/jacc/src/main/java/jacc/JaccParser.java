package jacc;

import compiler.*;

public class JaccParser extends JaccAbstractParser
    implements JaccTokens
{
    private static class SymList
    {

        JaccSymbol head;
        SymList tail;

        static int length(SymList symlist)
        {
            int i = 0;
            for (; symlist != null; symlist = symlist.tail)
                i++;

            return i;
        }

        static JaccSymbol[] toArray(SymList symlist)
        {
            int i = length(symlist);
            JaccSymbol ajaccsymbol[] = new JaccSymbol[i];
            while (i > 0) 
            {
                ajaccsymbol[--i] = symlist.head;
                symlist = symlist.tail;
            }
            return ajaccsymbol;
        }

        static int[] toIntArray(SymList symlist)
        {
            int i = length(symlist);
            int ai[] = new int[i];
            while (i > 0) 
            {
                ai[--i] = symlist.head.getTokenNo();
                symlist = symlist.tail;
            }
            return ai;
        }

        SymList(JaccSymbol jaccsymbol, SymList symlist)
        {
            head = jaccsymbol;
            tail = symlist;
        }
    }


    private Settings settings;
    private int seqNo;
    private JaccLexer lexer;
    private int precedence;

    public JaccParser(Handler handler, Settings settings1)
    {
        super(handler);
        seqNo = 1;
        precedence = 0;
        settings = settings1;
    }

    public void parse(JaccLexer jacclexer)
    {
        lexer = jacclexer;
        terminals.findOrAdd("error");
        parseDefinitions();
        if (jacclexer.getToken() != 1)
        {
            report(new Failure(jacclexer.getPos(), "Missing grammar"));
        } else
        {
            jacclexer.nextToken();
            parseGrammar();
            String s;
            if (jacclexer.getToken() == 1)
                while ((s = jacclexer.readWholeLine()) != null) 
                {
                    settings.addPostText(s);
                    settings.addPostText("\n");
                }
        }
        jacclexer.close();
    }

    public int[] parseSymbols(JaccLexer jacclexer)
    {
        lexer = jacclexer;
        SymList symlist = null;
        do
        {
            JaccSymbol jaccsymbol = parseDefinedSymbol();
            if (jaccsymbol == null)
            {
                if (jacclexer.getToken() != 0)
                    report(new Warning(jacclexer.getPos(), "Ignoring extra tokens at end of input"));
                jacclexer.close();
                return SymList.toIntArray(symlist);
            }
            symlist = new SymList(jaccsymbol, symlist);
            jacclexer.nextToken();
        } while (true);
    }

    public void parseErrorExamples(JaccLexer jacclexer, JaccJob jaccjob)
    {
        lexer = jacclexer;
        do
        {
            if (jacclexer.getToken() != 5)
                break;
            String s = jacclexer.getLexeme();
            if (jacclexer.nextToken() == 58)
                jacclexer.nextToken();
            else
                report(new Warning(jacclexer.getPos(), "A colon was expected here"));
            int i;
            do
            {
                compiler.Position position = jacclexer.getPos();
                SymList symlist = null;
                JaccSymbol jaccsymbol;
                while ((jaccsymbol = parseDefinedSymbol()) != null) 
                {
                    symlist = new SymList(jaccsymbol, symlist);
                    jacclexer.nextToken();
                }
                int ai[] = SymList.toIntArray(symlist);
                jaccjob.errorExample(position, s, ai);
                i = jacclexer.getToken();
                if (i != 124)
                    break;
                jacclexer.nextToken();
            } while (true);
            if (i != 0)
            {
                if (i != 59)
                {
                    report(new Failure(jacclexer.getPos(), "Unexpected token; a semicolon was expected here"));
                    do
                        i = jacclexer.nextToken();
                    while (i != 59 && i != 0);
                }
                if (i == 59)
                    jacclexer.nextToken();
            }
        } while (true);
        if (jacclexer.getToken() != 0)
            report(new Failure(jacclexer.getPos(), "Unexpected token; ignoring the rest of this file"));
        jacclexer.close();
    }

    private void parseDefinitions()
    {
        boolean flag = false;
        do
        {
            switch (lexer.getToken())
            {
            case 0: // '\0'
            case 1: // '\001'
                return;
            }
            if (parseDefinition())
            {
                flag = false;
            } else
            {
                if (!flag)
                {
                    flag = true;
                    report(new Failure(lexer.getPos(), "Syntax error in definition"));
                }
                lexer.nextToken();
            }
        } while (true);
    }

    private boolean parseDefinition()
    {
        switch (lexer.getToken())
        {
        case 2: // '\002'
            settings.addPreText(lexer.getLexeme());
            lexer.nextToken();
            return true;

        case 8: // '\b'
            parseTokenDefn();
            return true;

        case 9: // '\t'
            parseTypeDefn();
            return true;

        case 11: // '\013'
            parseFixityDefn(Fixity.left(precedence++));
            return true;

        case 13: // '\r'
            parseFixityDefn(Fixity.nonass(precedence++));
            return true;

        case 12: // '\f'
            parseFixityDefn(Fixity.right(precedence++));
            return true;

        case 14: // '\016'
            parseStart();
            return true;

        case 16: // '\020'
            settings.setClassName(parseIdent(lexer.getLexeme(), settings.getClassName()));
            return true;

        case 17: // '\021'
            settings.setInterfaceName(parseIdent(lexer.getLexeme(), settings.getInterfaceName()));
            return true;

        case 15: // '\017'
            settings.setPackageName(parseDefnQualName(lexer.getLexeme(), settings.getPackageName()));
            return true;

        case 18: // '\022'
            settings.setExtendsName(parseDefnQualName(lexer.getLexeme(), settings.getExtendsName()));
            return true;

        case 19: // '\023'
            lexer.nextToken();
            String s = parseQualName();
            if (s != null)
                settings.addImplementsNames(s);
            return true;

        case 20: // '\024'
            settings.setTypeName(parseDefnQualName(lexer.getLexeme(), settings.getTypeName()));
            if (lexer.getToken() == 58)
            {
                settings.setGetSemantic(lexer.readCodeLine());
                lexer.nextToken();
            }
            return true;

        case 21: // '\025'
            settings.setGetToken(lexer.readCodeLine());
            lexer.nextToken();
            return true;

        case 22: // '\026'
            settings.setNextToken(lexer.readCodeLine());
            lexer.nextToken();
            return true;

        case 3: // '\003'
        case 4: // '\004'
        case 5: // '\005'
        case 6: // '\006'
        case 7: // '\007'
        case 10: // '\n'
        default:
            return false;
        }
    }

    private void parseStart()
    {
        compiler.Position position = lexer.getPos();
        lexer.nextToken();
        JaccSymbol jaccsymbol = parseNonterminal();
        if (jaccsymbol == null)
        {
            report(new Failure(position, "Missing start symbol"));
        } else
        {
            if (start == null)
                start = jaccsymbol;
            else
                report(new Failure(position, "Multiple %start definitions are not permitted"));
            lexer.nextToken();
        }
    }

    private String parseIdent(String s, String s1)
    {
        compiler.Position position = lexer.getPos();
        if (lexer.nextToken() != 3)
        {
            report(new Failure(lexer.getPos(), "Syntax error in %" + s + " directive; identifier expected"));
            return s1;
        }
        String s2 = lexer.getLexeme();
        lexer.nextToken();
        if (s2 != null && s1 != null)
        {
            report(new Failure(position, "Multiple %" + s + " definitions are not permitted"));
            s2 = s1;
        }
        return s2;
    }

    private String parseDefnQualName(String s, String s1)
    {
        compiler.Position position = lexer.getPos();
        lexer.nextToken();
        String s2 = parseQualName();
        if (s2 != null && s1 != null)
        {
            report(new Failure(position, "Multiple %" + s + " definitions are not permitted"));
            s2 = s1;
        }
        return s2;
    }

    private void parseTokenDefn()
    {
        compiler.Position position = lexer.getPos();
        String s = optionalType();
        int i = 0;
        do
        {
            JaccSymbol jaccsymbol = parseTerminal();
            if (jaccsymbol == null)
            {
                if (i == 0)
                    report(new Failure(position, "Missing symbols in %token definition"));
                return;
            }
            addType(jaccsymbol, s);
            lexer.nextToken();
            i++;
        } while (true);
    }

    private void parseTypeDefn()
    {
        compiler.Position position = lexer.getPos();
        String s = optionalType();
        int i = 0;
        do
        {
            JaccSymbol jaccsymbol = parseSymbol();
            if (jaccsymbol == null)
            {
                if (i == 0)
                    report(new Failure(position, "Missing symbols in %type definition"));
                return;
            }
            addType(jaccsymbol, s);
            lexer.nextToken();
            i++;
        } while (true);
    }

    private void parseFixityDefn(Fixity fixity)
    {
        compiler.Position position = lexer.getPos();
        String s = optionalType();
        int i = 0;
        do
        {
            JaccSymbol jaccsymbol = parseTerminal();
            if (jaccsymbol == null)
            {
                if (i == 0)
                    report(new Failure(position, "Missing symbols in fixity definition"));
                return;
            }
            addFixity(jaccsymbol, fixity);
            addType(jaccsymbol, s);
            lexer.nextToken();
            i++;
        } while (true);
    }

    private String optionalType()
    {
label0:
        {
            String s;
label1:
            {
                if (lexer.nextToken() != 60)
                    break label0;
                lexer.nextToken();
                s = parseQualName();
                do
                {
                    if (lexer.getToken() != 91)
                        break label1;
                    if (lexer.nextToken() != 93)
                        break;
                    lexer.nextToken();
                    s = s + "[]";
                } while (true);
                report(new Failure(lexer.getPos(), "Missing ']' in array type"));
            }
            if (lexer.getToken() == 62)
                lexer.nextToken();
            else
            if (s != null)
                report(new Failure(lexer.getPos(), "Missing `>' in type specification"));
            return s;
        }
        return null;
    }

    private void addFixity(JaccSymbol jaccsymbol, Fixity fixity)
    {
        if (!jaccsymbol.setFixity(fixity))
            report(new Warning(lexer.getPos(), "Cannot change fixity for " + jaccsymbol.getName()));
    }

    private void addType(JaccSymbol jaccsymbol, String s)
    {
        if (s != null && !jaccsymbol.setType(s))
            report(new Warning(lexer.getPos(), "Cannot change type for " + jaccsymbol.getName()));
    }

    private void parseGrammar()
    {
        JaccSymbol jaccsymbol;
        while ((jaccsymbol = parseLhs()) != null) 
        {
            if (start == null)
                start = jaccsymbol;
            jaccsymbol.addProduction(parseRhs());
            for (; lexer.getToken() == 124; jaccsymbol.addProduction(parseRhs()))
                lexer.nextToken();

            if (lexer.getToken() == 59)
                lexer.nextToken();
            else
                report(new Warning(lexer.getPos(), "Missing ';' at end of rule"));
        }
    }

    private JaccSymbol parseLhs()
    {
        boolean flag = false;
        for (int i = lexer.getToken(); i != 1 && i != 0;)
        {
            JaccSymbol jaccsymbol = parseNonterminal();
            if (jaccsymbol == null)
            {
                if (!flag)
                {
                    if (parseTerminal() != null)
                        report(new Failure(lexer.getPos(), "Terminal symbol used on left hand side of rule"));
                    else
                        report(new Failure(lexer.getPos(), "Missing left hand side in rule"));
                    flag = true;
                }
                i = lexer.nextToken();
            } else
            {
                i = lexer.nextToken();
                if (i != 58)
                {
                    if (!flag)
                        report(new Failure(lexer.getPos(), "Missing colon after left hand side of rule"));
                    flag = true;
                } else
                {
                    lexer.nextToken();
                    return jaccsymbol;
                }
            }
        }

        return null;
    }

    private JaccProd parseRhs()
    {
        Fixity fixity = null;
        SymList symlist = null;
        do
        {
            while (lexer.getToken() == 10) 
            {
                lexer.nextToken();
                JaccSymbol jaccsymbol = parseSymbol();
                if (jaccsymbol == null)
                    report(new Failure(lexer.getPos(), "Missing token for %prec directive"));
                else
                if (jaccsymbol.getFixity() == null)
                {
                    report(new Failure(lexer.getPos(), "Ignoring %prec annotation because no fixity has been specified for " + jaccsymbol.getName()));
                    lexer.nextToken();
                } else
                {
                    if (fixity != null)
                        report(new Warning(lexer.getPos(), "Multiple %prec annotations in production"));
                    fixity = jaccsymbol.getFixity();
                    lexer.nextToken();
                }
            }
            JaccSymbol jaccsymbol1 = parseSymbol();
            if (jaccsymbol1 == null)
                break;
            symlist = new SymList(jaccsymbol1, symlist);
            lexer.nextToken();
        } while (true);
        String s = null;
        compiler.Position position = null;
        if (lexer.getToken() == 7)
        {
            s = lexer.getLexeme();
            position = lexer.getPos();
            lexer.nextToken();
        }
        JaccSymbol ajaccsymbol[] = SymList.toArray(symlist);
        return new JaccProd(fixity, ajaccsymbol, position, s, seqNo++);
    }

    private String parseQualName()
    {
        if (lexer.getToken() != 3)
        {
            report(new Failure(lexer.getPos(), "Syntax error in qualified name; identifier expected"));
            return null;
        }
        StringBuffer stringbuffer = new StringBuffer();
        do
        {
            stringbuffer.append(lexer.getLexeme());
            if (lexer.nextToken() != 46)
                break;
            if (lexer.nextToken() != 3)
            {
                report(new Failure(lexer.getPos(), "Syntax error in qualified name"));
                break;
            }
            stringbuffer.append('.');
        } while (true);
        return stringbuffer.toString();
    }

    private JaccSymbol parseTerminal()
    {
        String s = lexer.getLexeme();
        switch (lexer.getToken())
        {
        case 3: // '\003'
            if (nonterms.find(s) != null)
                return null;
            else
                return terminals.findOrAdd(s);

        case 4: // '\004'
            return literals.findOrAdd(s, lexer.getLastLiteral());
        }
        return null;
    }

    private JaccSymbol parseNonterminal()
    {
        String s = lexer.getLexeme();
        switch (lexer.getToken())
        {
        case 3: // '\003'
            if (terminals.find(s) != null)
                return null;
            else
                return nonterms.findOrAdd(s);
        }
        return null;
    }

    private JaccSymbol parseSymbol()
    {
        String s = lexer.getLexeme();
        switch (lexer.getToken())
        {
        case 3: // '\003'
            JaccSymbol jaccsymbol = null;
            jaccsymbol = terminals.find(s);
            if (jaccsymbol == null)
                jaccsymbol = nonterms.findOrAdd(s);
            return jaccsymbol;

        case 4: // '\004'
            return literals.findOrAdd(s, lexer.getLastLiteral());
        }
        return null;
    }

    private JaccSymbol parseDefinedSymbol()
    {
        String s = lexer.getLexeme();
        switch (lexer.getToken())
        {
        case 3: // '\003'
            JaccSymbol jaccsymbol = nonterms.find(s);
            return jaccsymbol == null ? terminals.find(s) : jaccsymbol;

        case 4: // '\004'
            return literals.find(lexer.getLastLiteral());
        }
        return null;
    }
}
