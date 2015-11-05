package org.xbib.query.cql;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * A CQL Term
 */
public class Term extends AbstractNode {

    private static final TimeZone tz = TimeZone.getTimeZone("GMT");
    private static final String ISO_FORMAT_SECONDS = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String ISO_FORMAT_DAYS = "yyyy-MM-dd";

    private String value;
    private Long longvalue;
    private Double doublevalue;
    private Identifier identifier;
    private Date datevalue;
    private SimpleName name;

    public Term(String value) {
        this.value = value;
        try {
            // check for hidden dates. CQL does not support ISO dates.
            this.datevalue = parseDateISO(value);
            this.value = null;
        } catch (Exception e) {

        }
    }

    public Term(Identifier identifier) {
        this.identifier = identifier;
    }

    public Term(SimpleName name) {
        this.name = name;
    }

    public Term(Long value) {
        this.longvalue = value;
    }

    public Term(Double value) {
        this.doublevalue = value;
    }

    /**
     * Set value, useful for inline replacements
     * in spellcheck suggestions
     *
     * @param value the value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * If the value is a String it is embedded in quotation marks.
     * If its a Integer or a Double it is returned without
     * quotation marks.
     *
     * @return the value as String
     */
    public String getValue() {
        return longvalue != null ? Long.toString(longvalue)
                : doublevalue != null ? Double.toString(doublevalue)
                : value != null ? value
                : identifier != null ? identifier.toString()
                : name != null ? name.toString()
                : null;
    }

    public boolean isLong() {
        return longvalue != null;
    }

    public boolean isFloat() {
        return doublevalue != null;
    }

    public boolean isString() {
        return value != null;
    }

    public boolean isName() {
        return name != null;
    }

    public boolean isIdentifier() {
        return identifier != null;
    }

    public boolean isDate() {
        return datevalue != null;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    private Date parseDateISO(String value) {
        if (value == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern(ISO_FORMAT_SECONDS);
        sdf.setTimeZone(tz);
        sdf.setLenient(true);
        try {
            return sdf.parse(value);
        } catch (ParseException pe) {
            // skip
        }
        sdf.applyPattern(ISO_FORMAT_DAYS);
        try {
            return sdf.parse(value);
        } catch (ParseException pe) {
            return null;
        }
    }

    private String formatDateISO(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern(ISO_FORMAT_SECONDS);
        sdf.setTimeZone(tz);
        return sdf.format(date);
    }

    @Override
    public String toString() {
        return longvalue != null ? Long.toString(longvalue)
                : doublevalue != null ? Double.toString(doublevalue)
                : datevalue != null ? formatDateISO(datevalue)
                : value != null ? value.startsWith("\"") && value.endsWith("\"") ? value
                : "\"" + value.replaceAll("\"", "\\\\\"") + "\""
                : identifier != null ? identifier.toString()
                : name != null ? name.toString()
                : null;
    }
}
