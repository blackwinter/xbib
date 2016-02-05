package org.xbib.graphics.vector.intermediate;

import org.xbib.graphics.vector.intermediate.commands.Command;

public interface CommandHandler {
    void handle(Command<?> command);
}

