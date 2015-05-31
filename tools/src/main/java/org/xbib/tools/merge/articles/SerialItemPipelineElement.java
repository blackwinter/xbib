package org.xbib.tools.merge.articles;

import org.xbib.pipeline.element.PipelineElement;

public class SerialItemPipelineElement implements PipelineElement<SerialItem> {

    private SerialItem serialItem;

    private boolean forced;

    public SerialItemPipelineElement set(SerialItem serialItem) {
        this.serialItem = serialItem;
        return this;
    }

    public SerialItem get() {
        return serialItem;
    }

    public SerialItemPipelineElement setForced(boolean forced) {
        this.forced = forced;
        return this;
    }

    public boolean getForced() {
        return forced;
    }

}
