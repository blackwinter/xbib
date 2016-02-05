package org.xbib.time.chronic.handlers;

import org.xbib.time.chronic.tags.Tag;

@SuppressWarnings("unchecked")
public class TagPattern extends HandlerPattern {
    private Class<? extends Tag> tagClass;

    public TagPattern(Class<? extends Tag> tagClass) {
        this(tagClass, false);
    }

    public TagPattern(Class<? extends Tag> tagClass, boolean optional) {
        super(optional);
        this.tagClass = tagClass;
    }

    public Class<? extends Tag> getTagClass() {
        return tagClass;
    }

    @Override
    public String toString() {
        return "[TagPattern: tagClass = " + tagClass + "]";
    }
}
