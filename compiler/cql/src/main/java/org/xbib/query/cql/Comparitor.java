package org.xbib.query.cql;

import java.util.HashMap;

/**
 * CQL operators
 */
public enum Comparitor {

    EQUALS("="),
    GREATER(">"),
    GREATER_EQUALS(">="),
    LESS("<"),
    LESS_EQUALS("<="),
    NOT_EQUALS("<>"),
    WITHIN("within"),
    CQLWITHIN("cql.within"),
    ENCLOSES("encloses"),
    CQLENCLOSES("cql.encloses"),
    ADJ("adj"),
    CQLADJ("cql.adj"),
    ALL("all"),
    CQLALL("cql.all"),
    ANY("any"),
    CQLANY("cql.any");
    private static HashMap<String, Comparitor> tokenMap;
    private String token;

    /**
     * Creates a new Operator object.
     *
     * @param token the operator token
     */
    private Comparitor(String token) {
        this.token = token;
        map(token, this);
    }

    /**
     * Map token to operator
     *
     * @param token the token
     * @param op    the operator
     */
    private static void map(String token, Comparitor op) {
        if (tokenMap == null) {
            tokenMap = new HashMap<>();
        }
        tokenMap.put(token, op);
    }

    /**
     * Get token
     *
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * Get operator for token
     *
     * @param token the token
     * @return the operator
     */
    static Comparitor forToken(Object token) {
        return tokenMap.get(token.toString());
    }

    /**
     * Write operator representation
     *
     * @return the operator token
     */
    @Override
    public String toString() {
        return token;
    }
}
