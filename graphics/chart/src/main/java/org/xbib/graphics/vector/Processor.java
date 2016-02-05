package org.xbib.graphics.vector;

import org.xbib.graphics.vector.intermediate.commands.Command;
import org.xbib.graphics.vector.util.PageSize;

public interface Processor {
    Document process(Iterable<Command<?>> commands, PageSize pageSize);
}

