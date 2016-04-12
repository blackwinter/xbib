package org.xbib.io.field;

public class FileField extends AbstractSeparable {

    public FileField(String content) {
        super(FieldSeparator.FS, content);
    }
}
