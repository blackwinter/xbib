package jacc;

import jacc.grammar.Grammar;
import jacc.grammar.LALRMachine;
import jacc.grammar.LR0Machine;
import jacc.grammar.LookaheadMachine;
import jacc.grammar.SLRMachine;

public class Settings
{

    private int machineType;
    public static final int LR0 = 0;
    public static final int SLR1 = 1;
    public static final int LALR1 = 2;
    private String packageName;
    private String className;
    private String interfaceName;
    private String extendsName;
    private String implementsNames;
    private String typeName;
    private String getToken;
    private String nextToken;
    private String getSemantic;
    private StringBuffer preTextBuffer;
    private StringBuffer postTextBuffer;

    public Settings()
    {
        machineType = 2;
        preTextBuffer = new StringBuffer();
        postTextBuffer = new StringBuffer();
    }

    public void setMachineType(int i)
    {
        machineType = i;
    }

    public int getMachineType()
    {
        return machineType;
    }

    public LookaheadMachine makeMachine(Grammar grammar)
    {
        if (machineType == 0)
            return new LR0Machine(grammar);
        if (machineType == 1)
            return new SLRMachine(grammar);
        else
            return new LALRMachine(grammar);
    }

    public void setPackageName(String s)
    {
        packageName = s;
    }

    public String getPackageName()
    {
        return packageName;
    }

    public void setClassName(String s)
    {
        className = s;
    }

    public String getClassName()
    {
        return className;
    }

    public void setInterfaceName(String s)
    {
        interfaceName = s;
    }

    public String getInterfaceName()
    {
        return interfaceName;
    }

    public void setExtendsName(String s)
    {
        extendsName = s;
    }

    public String getExtendsName()
    {
        return extendsName;
    }

    public void setImplementsNames(String s)
    {
        implementsNames = s;
    }

    public void addImplementsNames(String s)
    {
        if (implementsNames != null)
            implementsNames += ", " + s;
        else
            implementsNames = s;
    }

    public String getImplementsNames()
    {
        return implementsNames;
    }

    public String getTypeName()
    {
        return typeName;
    }

    public void setTypeName(String s)
    {
        typeName = s;
    }

    public String getGetToken()
    {
        return getToken;
    }

    public void setGetToken(String s)
    {
        getToken = s;
    }

    public void setNextToken(String s)
    {
        nextToken = s;
    }

    public String getNextToken()
    {
        return nextToken;
    }

    public void setGetSemantic(String s)
    {
        getSemantic = s;
    }

    public String getGetSemantic()
    {
        return getSemantic;
    }

    public void addPreText(String s)
    {
        preTextBuffer.append(s);
    }

    public String getPreText()
    {
        return preTextBuffer.toString();
    }

    public void addPostText(String s)
    {
        postTextBuffer.append(s);
    }

    public String getPostText()
    {
        return postTextBuffer.toString();
    }

    public void fillBlanks(String s)
    {
        if (getClassName() == null)
            setClassName(s + "Parser");
        if (getInterfaceName() == null)
            setInterfaceName(s + "Tokens");
        if (getTypeName() == null)
            setTypeName("Object");
        if (getInterfaceName() != null)
            addImplementsNames(getInterfaceName());
        if (getGetSemantic() == null)
            setGetSemantic("lexer.getSemantic()");
        if (getGetToken() == null)
            setGetToken("lexer.getToken()");
        if (getNextToken() == null)
            setNextToken("lexer.nextToken()");
    }
}
