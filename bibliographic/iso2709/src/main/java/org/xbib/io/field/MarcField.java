package org.xbib.io.field;

import org.xbib.common.Strings;
import org.xbib.marc.label.RecordLabel;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

        Builder value(String value) {
            this.subfields.add(new Subfield(Strings.EMPTY, value));
            return this;
        }

        Builder value(byte[] value) {
            return value(value, StandardCharsets.US_ASCII);
        }

        Builder value(byte[] value, int offset, int size) {
            return value(value, offset, size, StandardCharsets.US_ASCII);
        }

        Builder value(byte[] value, Charset charset) {
            this.subfields.add(new Subfield(Strings.EMPTY, new String(value, 0, value.length, charset)));
            return this;
        }

        Builder value(byte[] value, int offset, int size, Charset charset) {
            this.subfields.add(new Subfield(Strings.EMPTY, new String(value, offset, size, charset)));
            return this;
        }

        Builder subfield(String subfieldId, String value) {
            this.subfields.add(new Subfield(subfieldId, value));
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

    public String getValue() {
        return subfields.isEmpty() ? null : subfields.get(0).getValue();
    }

    public boolean isControl() {
        return control;
    }

    public String toKey() {
        return (tag == null ? Strings.EMPTY : tag )
                + DOLLAR + (indicator == null ? Strings.EMPTY : indicator)
                + DOLLAR + (subfields.isEmpty() ? Strings.EMPTY : subfields.stream().map(Subfield::getId).sorted().collect(Collectors.joining("")));
    }

    private final static String DOLLAR = "$";
}
