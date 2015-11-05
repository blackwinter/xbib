package org.xbib.io.field;

public class RecordField extends AbstractSeparable {

    public RecordField(String content) {
        super(FieldSeparator.RS, content);
    }
}
