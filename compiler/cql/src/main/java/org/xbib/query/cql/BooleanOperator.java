package org.xbib.query.cql;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract syntax tree of CQL - boolean operator enumeration
 */
public enum BooleanOperator {

    AND("and"),
    OR("or"),
    NOT("not"),
    PROX("prox");
    /**
     * token/operator map
     */
    private static Map<String, BooleanOperator> tokenMap;
    /**
     * operator/token map
     */
    private static Map<BooleanOperator, String> opMap;
    private String token;

    /**
     * Creates a new Operator object.
     *
     * @param token the operator token
     */
    BooleanOperator(String token) {
        this.token = token;
        map(token, this);
    }

    /**
     * Map token to operator
     *
     * @param token the token
     * @param op    the operator
     */
    private static void map(String token, BooleanOperator op) {
        if (tokenMap == null) {
            tokenMap = new HashMap<>();
        }
        tokenMap.put(token, op);
        if (opMap == null) {
            opMap = new HashMap<>();
        }
        opMap.put(op, token);
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
    static BooleanOperator forToken(Object token) {
        return tokenMap.get(token.toString().toLowerCase());
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
