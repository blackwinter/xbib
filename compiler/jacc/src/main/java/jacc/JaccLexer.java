package jacc;

import compiler.*;

public class JaccLexer extends SourceLexer
    implements JaccTokens
{

    private int lastLiteral;

    public JaccLexer(Handler handler, Source source)
    {
        super(handler, source);
    }

    public int nextToken()
    {
        do
        {
            skipWhitespace();
            markPosition();
            lexemeText = null;
            switch (c)
            {
            case -1: 
                return token = 0;

            case 58: // ':'
                nextChar();
                return token = 58;

            case 59: // ';'
                nextChar();
                return token = 59;

            case 124: // '|'
                nextChar();
                return token = 124;

            case 60: // '<'
                nextChar();
                return token = 60;

            case 62: // '>'
                nextChar();
                return token = 62;

            case 91: // '['
                nextChar();
                return token = 91;

            case 93: // ']'
                nextChar();
                return token = 93;

            case 46: // '.'
                nextChar();
                return token = 46;

            case 37: // '%'
                if (directive() != -1)
                    return token;
                break;

            case 34: // '"'
                if (string() != -1)
                    return token;
                break;

            case 39: // '\''
                if (literal() != -1)
                    return token;
                break;

            case 123: // '{'
                if (action() != -1)
                    return token;
                break;

            case 47: // '/'
                skipComment();
                break;

            default:
                if (Character.isJavaIdentifierStart((char)c))
                    return identifier();
                if (Character.isDigit((char)c))
                    return number();
                illegalCharacter();
                nextChar();
                break;
            }
        } while (true);
    }

    public String readWholeLine()
    {
        if (line == null)
            return null;
        String s = line;
        if (col > 0)
            s = s.substring(col);
        nextLine();
        return s;
    }

    public String readCodeLine()
    {
        for (; isWhitespace(c); nextChar());
        return readWholeLine();
    }

    private boolean isWhitespace(int i)
    {
        return i == 32 || i == 12;
    }

    private void skipWhitespace()
    {
        for (; isWhitespace(c); nextChar());
        while (c == 10) 
        {
            nextLine();
            while (isWhitespace(c)) 
                nextChar();
        }
    }

    private void skipComment()
    {
        nextChar();
        if (c == 47)
        {
            nextLine();
        } else
        {
            if (c == 42)
            {
                nextChar();
                do
                {
                    if (c == 42)
                    {
                        do
                            nextChar();
                        while (c == 42);
                        if (c == 47)
                        {
                            nextChar();
                            return;
                        }
                    }
                    if (c == -1)
                    {
                        report(new Failure(getPos(), "Unterminated comment"));
                        return;
                    }
                    if (c == 10)
                        nextLine();
                    else
                        nextChar();
                } while (true);
            }
            report(new Failure(getPos(), "Illegal comment format"));
        }
    }

    private int identifier()
    {
        int i = col;
        do
            nextChar();
        while (c != -1 && Character.isJavaIdentifierPart((char)c));
        lexemeText = line.substring(i, col);
        return token = 3;
    }

    private int directive()
    {
        nextChar();
        if (c == 37)
        {
            nextChar();
            return token = 1;
        }
        if (Character.isJavaIdentifierStart((char)c))
        {
            identifier();
            if (lexemeText.equals("token"))
                return token = 8;
            if (lexemeText.equals("type"))
                return token = 9;
            if (lexemeText.equals("prec"))
                return token = 10;
            if (lexemeText.equals("left"))
                return token = 11;
            if (lexemeText.equals("right"))
                return token = 12;
            if (lexemeText.equals("nonassoc"))
                return token = 13;
            if (lexemeText.equals("start"))
                return token = 14;
            if (lexemeText.equals("package"))
                return token = 15;
            if (lexemeText.equals("extends"))
                return token = 18;
            if (lexemeText.equals("implements"))
                return token = 19;
            if (lexemeText.equals("semantic"))
                return token = 20;
            if (lexemeText.equals("get"))
                return token = 21;
            if (lexemeText.equals("next"))
                return token = 22;
            if (lexemeText.equals("class"))
                return token = 16;
            if (lexemeText.equals("interface"))
            {
                return token = 17;
            } else
            {
                report(new Failure(getPos(), "Unrecognized directive"));
                return -1;
            }
        }
        if (c == 123)
        {
            nextChar();
            return code();
        } else
        {
            report(new Failure(getPos(), "Illegal directive syntax"));
            return -1;
        }
    }

    private int code()
    {
        int i = col;
        StringBuffer stringbuffer = null;
        do
        {
            if (c == 37)
            {
                do
                    nextChar();
                while (c == 37);
                if (c == 125)
                {
                    lexemeText = endBuffer(stringbuffer, i, col - 1);
                    nextChar();
                    return token = 2;
                }
            }
            if (c == -1)
            {
                report(new Failure(getPos(), "Code fragment terminator %} not found"));
                lexemeText = endBuffer(stringbuffer, i, col);
                return token = 2;
            }
            if (c == 10)
            {
                if (stringbuffer == null)
                {
                    stringbuffer = new StringBuffer(line.substring(i, col));
                } else
                {
                    stringbuffer.append('\n');
                    stringbuffer.append(line);
                }
                nextLine();
            } else
            {
                nextChar();
            }
        } while (true);
    }

    private String endBuffer(StringBuffer stringbuffer, int i, int j)
    {
        if (stringbuffer == null)
            return line.substring(i, j);
        stringbuffer.append('\n');
        if (line != null)
            stringbuffer.append(line.substring(0, j));
        return stringbuffer.toString();
    }

    public int getLastLiteral()
    {
        return lastLiteral;
    }

    private int number()
    {
        int i = col;
        int j = 0;
        int k = Character.digit((char)c, 10);
        do
        {
            j = 10 * j + k;
            nextChar();
            k = Character.digit((char)c, 10);
        } while (k >= 0);
        lexemeText = line.substring(i, col);
        lastLiteral = j;
        return token = 6;
    }

    private int string()
    {
        nextChar();
        int i = col;
        while (c != 34 && c != 10 && c != -1) 
            if (c == 92)
                escapeChar();
            else
                nextChar();
        lexemeText = line.substring(i, col);
        if (c == 34)
            nextChar();
        else
            report(new Warning(getPos(), "Missing \" on string literal"));
        return token = 5;
    }

    private int literal()
    {
        int i = col;
        nextChar();
        if (c == 92)
            escapeChar();
        else
        if (c != 39 && c != 10 && c != -1)
        {
            lastLiteral = c;
            nextChar();
        } else
        {
            report(new Failure(getPos(), "Illegal character literal syntax"));
            return -1;
        }
        if (c == 39)
            nextChar();
        else
            report(new Warning(getPos(), "Missing ' on character literal"));
        lexemeText = line.substring(i, col);
        return token = 4;
    }

    private void escapeChar()
    {
        nextChar();
        switch (c)
        {
        case 34: // '"'
        case 39: // '\''
        case 92: // '\\'
        case 98: // 'b'
        case 102: // 'f'
        case 110: // 'n'
        case 114: // 'r'
        case 116: // 't'
            lastLiteral = c;
            nextChar();
            return;
        }
        int i = Character.digit((char)c, 8);
        if (i >= 0)
        {
            lastLiteral = 0;
            int j = i >= 4 ? 2 : 3;
            do
            {
                lastLiteral = (lastLiteral << 3) + i;
                nextChar();
                i = Character.digit((char)c, 8);
            } while (i >= 0 && --j > 0);
            return;
        } else
        {
            report(new Failure(getPos(), "Syntax error in escape sequence"));
            return;
        }
    }

    private int action()
    {
        int i = col;
        int j = 0;
        StringBuffer stringbuffer = null;
        do
        {
            if (c == 125)
            {
                if (--j == 0)
                {
                    nextChar();
                    lexemeText = endBuffer(stringbuffer, i, col);
                    return token = 7;
                }
            } else
            if (c == 123)
                j++;
            if (c == -1)
            {
                report(new Failure(getPos(), "Unterminated action"));
                lexemeText = endBuffer(stringbuffer, i, col);
                return token = 7;
            }
            if (c == 10)
            {
                if (stringbuffer == null)
                {
                    stringbuffer = new StringBuffer(line.substring(i, col));
                } else
                {
                    stringbuffer.append('\n');
                    stringbuffer.append(line);
                }
                nextLine();
            } else
            {
                nextChar();
            }
        } while (true);
    }

    private void illegalCharacter()
    {
        report(new Warning(getPos(), "Ignoring illegal character"));
    }
}
