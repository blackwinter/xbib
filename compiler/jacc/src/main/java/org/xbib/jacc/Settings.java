package org.xbib.jacc;

import org.xbib.jacc.grammar.Grammar;
import org.xbib.jacc.grammar.LALRMachine;
import org.xbib.jacc.grammar.LR0Machine;
import org.xbib.jacc.grammar.LookaheadMachine;
import org.xbib.jacc.grammar.SLRMachine;

public class Settings {

    private static final int LR0 = 0;
    private static final int SLR1 = 1;
    private static final int LALR1 = 2;
    private int machineType;
    private String packageName;
    private String className;
    private String interfaceName;
    private String extendsName;
    private String implementsNames;
    private String typeName;
    private String getToken;
    private String nextToken;
    private String getSemantic;
    private StringBuilder preTextBuffer;
    private StringBuilder postTextBuffer;

    Settings() {
        machineType = LALR1;
        preTextBuffer = new StringBuilder();
        postTextBuffer = new StringBuilder();
    }

    void setMachineType(int i)
    {
        machineType = i;
    }

    public int getMachineType()
    {
        return machineType;
    }

    LookaheadMachine makeMachine(Grammar grammar) {
        if (machineType == LR0) {
            return new LR0Machine(grammar);
        }
        if (machineType == SLR1) {
            return new SLRMachine(grammar);
        }
        else {
            return new LALRMachine(grammar);
        }
    }

    void setPackageName(String s)
    {
        packageName = s;
    }

    String getPackageName()
    {
        return packageName;
    }

    void setClassName(String s)
    {
        className = s;
    }

    String getClassName()
    {
        return className;
    }

    void setInterfaceName(String s)
    {
        interfaceName = s;
    }

    String getInterfaceName()
    {
        return interfaceName;
    }

    void setExtendsName(String s)
    {
        extendsName = s;
    }

    String getExtendsName()
    {
        return extendsName;
    }

    public void setImplementsNames(String s)
    {
        implementsNames = s;
    }

    void addImplementsNames(String s) {
        if (implementsNames != null) {
            implementsNames += ", " + s;
        }
        else {
            implementsNames = s;
        }
    }

    String getImplementsNames()
    {
        return implementsNames;
    }

    String getTypeName()
    {
        return typeName;
    }

    void setTypeName(String s)
    {
        typeName = s;
    }

    String getGetToken()
    {
        return getToken;
    }

    void setGetToken(String s)
    {
        getToken = s;
    }

    void setNextToken(String s)
    {
        nextToken = s;
    }

    String getNextToken()
    {
        return nextToken;
    }

    void setGetSemantic(String s)
    {
        getSemantic = s;
    }

    String getGetSemantic()
    {
        return getSemantic;
    }

    void addPreText(String s)
    {
        preTextBuffer.append(s);
    }

    String getPreText()
    {
        return preTextBuffer.toString();
    }

    void addPostText(String s)
    {
        postTextBuffer.append(s);
    }

    String getPostText()
    {
        return postTextBuffer.toString();
    }

    void fillBlanks(String s) {
        if (getClassName() == null) {
            setClassName(s + "Parser");
        }
        if (getInterfaceName() == null) {
            setInterfaceName(s + "Tokens");
        }
        if (getTypeName() == null) {
            setTypeName("Object");
        }
        if (getInterfaceName() != null) {
            addImplementsNames(getInterfaceName());
        }
        if (getGetSemantic() == null) {
            setGetSemantic("lexer.getSemantic()");
        }
        if (getGetToken() == null) {
            setGetToken("lexer.getToken()");
        }
        if (getNextToken() == null) {
            setNextToken("lexer.nextToken()");
        }
    }
}
