package org.xbib.query.cql.elasticsearch.ast;

import org.xbib.query.DateUtil;
import org.xbib.query.QuotedStringTokenizer;
import org.xbib.query.UnterminatedQuotedStringException;
import org.xbib.query.cql.elasticsearch.Visitor;

import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * Elasticsearch query tokens
 */
public class Token implements Node {

    public enum TokenClass {

        NORMAL, ALL, WILDCARD, BOUNDARY, PROTECTED
    }

    private TokenType type;

    private String value;

    private String stringvalue;

    private Boolean booleanvalue;

    private Long longvalue;

    private Double doublevalue;

    private Date datevalue;

    private List<String> values;

    private final EnumSet<TokenClass> tokenClass;

    public Token(String value) {
        this.value = value;
        this.tokenClass = EnumSet.of(TokenClass.NORMAL);
        this.type = TokenType.STRING;
        // if this string is equal to true/false or on/off or yes/no, convert silently to bool
        if (value.equals("true") || value.equals("yes") || value.equals("on")) {
            this.booleanvalue = true;
            this.value = null;
            this.type = TokenType.BOOL;

        } else if (value.equals("false") || value.equals("no") || value.equals("off")) {
            this.booleanvalue = false;
            this.value = null;
            this.type = TokenType.BOOL;

        }
        if (this.value != null) {
            // protected?
            if (value.startsWith("\"") && value.endsWith("\"")) {
                this.stringvalue = value;
                this.value = value.substring(1, value.length() - 1).replaceAll("\\\\\"", "\"");
                this.values = parseQuot(this.value);
                tokenClass.add(TokenClass.PROTECTED);
            }
            // wildcard?
            if (this.value.indexOf('*') >= 0 || this.value.indexOf('?') >= 0) {
                tokenClass.add(TokenClass.WILDCARD);
                // all?
                if (this.value.length() == 1) {
                    tokenClass.add(TokenClass.ALL);
                }
            }
            // prefix?
            if (this.value.length() > 0 && this.value.charAt(0) == '^') {
                tokenClass.add(TokenClass.BOUNDARY);
                this.value = this.value.substring(1);
            }
        }
    }

    public Token(Boolean value) {
        this.booleanvalue = value;
        this.type = TokenType.BOOL;
        this.tokenClass = EnumSet.of(TokenClass.NORMAL);
    }

    public Token(Long value) {
        this.longvalue = value;
        this.type = TokenType.INT;
        this.tokenClass = EnumSet.of(TokenClass.NORMAL);
    }

    public Token(Double value) {
        this.doublevalue = value;
        this.type = TokenType.FLOAT;
        this.tokenClass = EnumSet.of(TokenClass.NORMAL);
    }

    public Token(Date value) {
        this.datevalue = value;
        // this will enforce dates to get formatted as long values (years)
        this.longvalue = Long.parseLong(DateUtil.formatDate(datevalue, "yyyy"));
        this.type = TokenType.DATETIME;
        this.tokenClass = EnumSet.of(TokenClass.NORMAL);
    }

    /**
     * Same as toString(), but ignore stringvalue
     */
    public String getString() {
        StringBuilder sb = new StringBuilder();
        if (booleanvalue != null) {
            sb.append(booleanvalue);
        } else if (longvalue != null) {
            sb.append(longvalue);
        } else if (doublevalue != null) {
            sb.append(doublevalue);
        } else if (datevalue != null) {
            sb.append(DateUtil.formatDateISO(datevalue));
        } else if (value != null) {
            sb.append(value);
        }
        return sb.toString();
    }

    public Boolean getBoolean() {
        return booleanvalue;
    }

    public Long getInteger() {
        return longvalue;
    }

    public Double getFloat() {
        return doublevalue;
    }

    public Date getDate() {
        return datevalue;
    }

    public List<String> getStringList() {
        return values;
    }

    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (booleanvalue != null) {
            sb.append(booleanvalue);
        } else if (longvalue != null) {
            sb.append(longvalue);
        } else if (doublevalue != null) {
            sb.append(doublevalue);
        } else if (datevalue != null) {
            sb.append(DateUtil.formatDateISO(datevalue));
        } else if (stringvalue != null) {
            sb.append(stringvalue);
        } else if (value != null) {
            sb.append(value);
        }
        return sb.toString();
    }

    public boolean isProtected() {
        return tokenClass.contains(TokenClass.PROTECTED);
    }

    public boolean isBoundary() {
        return tokenClass.contains(TokenClass.BOUNDARY);
    }

    public boolean isWildcard() {
        return tokenClass.contains(TokenClass.WILDCARD);
    }

    public boolean isAll() {
        return tokenClass.contains(TokenClass.ALL);
    }

    private List<String> parseQuot(String s) {
        try {
            QuotedStringTokenizer qst = new QuotedStringTokenizer(s, " \t\n\r\f", "\"", '\\', false);
            Iterable<String> iterable = () -> qst;
            Stream<String> stream = StreamSupport.stream(iterable.spliterator(), false);
            return stream.filter(str -> !word.matcher(str).matches()).collect(toList());
        } catch (UnterminatedQuotedStringException e) {
            return Collections.singletonList(s);
        }
    }

    private final static Pattern word = Pattern.compile("[\\P{IsWord}]");
}
