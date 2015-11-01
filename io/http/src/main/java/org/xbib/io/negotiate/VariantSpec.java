package org.xbib.io.negotiate;

import java.util.ArrayList;
import java.util.List;

public class VariantSpec {

    private MediaRangeSpec type;

    private List<MediaRangeSpec> aliases = new ArrayList<MediaRangeSpec>();

    private boolean isDefault = false;

    public VariantSpec(String mediaType) {
        type = MediaRangeSpec.parseType(mediaType);
    }

    public VariantSpec addAliasMediaType(String mediaType) {
        aliases.add(MediaRangeSpec.parseType(mediaType));
        return this;
    }

    public void makeDefault() {
        isDefault = true;
    }

    public MediaRangeSpec getMediaType() {
        return type;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public List<MediaRangeSpec> getAliases() {
        return aliases;
    }
}