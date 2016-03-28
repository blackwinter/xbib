package org.xbib.io.field;

import org.xbib.common.Strings;
import org.xbib.marc.label.RecordLabel;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class MarcField implements Comparable<MarcField> {

    private final String tag;
    private final String indicator;
    private final List<Subfield> subfields;
    private final boolean control;

    @Override
    public int compareTo(MarcField o) {
        return 0;
    }

    static class Builder {
        String tag;
        String indicator;
        List<Subfield> subfields;

        Builder() {
            this.subfields = new LinkedList<>();
        }

        Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        Builder indicator(String indicator) {
            this.indicator = indicator;
            return this;
        }

        Builder subfield(String subfieldId, String subfield) {
            this.subfields.add(new Subfield(subfieldId, subfield));
            return this;
        }

        Builder subfield(RecordLabel label, String raw) {
            int pos = 0;
            int subfieldidlen = label.getSubfieldIdentifierLength();
            if (raw.length() >= pos + subfieldidlen) {
                this.subfields.add(new Subfield(raw.substring(pos, pos + subfieldidlen),
                        raw.substring(pos + subfieldidlen)));
            }
            return this;
        }

        Builder field(RecordLabel label, String raw) {
            this.tag = raw.length() > 2 ? raw.substring(0, 3) : "999";
            if (isControl(tag)) {
                if (raw.length() > 3) {
                    this.subfields.add(new Subfield(null, raw.substring(3)));
                }
            } else {
                int pos = 3 + label.getIndicatorLength();
                this.indicator = raw.length() >= pos ? raw.substring(3, pos) : null;
                int subfieldidlen = label.getSubfieldIdentifierLength();
                if (raw.length() >= pos + subfieldidlen) {
                    this.subfields.add(new Subfield(raw.substring(pos, pos + subfieldidlen),
                            raw.substring(pos + subfieldidlen)));
                }
            }
            return this;
        }

        public boolean isControl(String tag) {
            return tag != null && tag.charAt(0) == '0' && tag.charAt(1) == '0';
        }

        MarcField build() {
            return new MarcField(tag, indicator, subfields, isControl(tag));
        }
    }

    static class Subfield {
        String id;
        String value;
        Subfield(String id, String value) {
            this.id = id;
            this.value = value;
        }

        public String getId() {
            return id;
        }

        public String getValue() {
            return value;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private MarcField(String tag, String indicator, List<Subfield> subfields, boolean control) {
        this.tag = tag;
        this.indicator = indicator;
        this.subfields = subfields;
        this.control = control;
    }

    public String getTag() {
        return tag;
    }

    public String getIndicator() {
        return indicator;
    }

    public List<Subfield> getSubfields() {
        return subfields;
    }

    public boolean isControl() {
        return control;
    }

    public String toKey() {
        return (tag != null ? tag : Strings.EMPTY)
                + (indicator != null ? DOLLAR + indicator : Strings.EMPTY)
                + subfields.stream().map(Subfield::getId).collect(Collectors.joining(""));
    }

    private final static String DOLLAR = "$";
}
