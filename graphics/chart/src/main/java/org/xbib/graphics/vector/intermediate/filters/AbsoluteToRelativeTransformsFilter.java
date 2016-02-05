package org.xbib.graphics.vector.intermediate.filters;

import org.xbib.graphics.vector.intermediate.commands.AffineTransformCommand;
import org.xbib.graphics.vector.intermediate.commands.Command;
import org.xbib.graphics.vector.intermediate.commands.CreateCommand;
import org.xbib.graphics.vector.intermediate.commands.DisposeCommand;
import org.xbib.graphics.vector.intermediate.commands.SetTransformCommand;
import org.xbib.graphics.vector.intermediate.commands.TransformCommand;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class AbsoluteToRelativeTransformsFilter extends Filter {
    private Stack<AffineTransform> transforms;

    public AbsoluteToRelativeTransformsFilter(Iterable<Command<?>> stream) {
        super(stream);
        transforms = new Stack<AffineTransform>();
    }

    @Override
    public Command<?> next() {
        Command<?> nextCommand = super.next();
        if (nextCommand instanceof AffineTransformCommand) {
            AffineTransformCommand affineTransformCommand = (AffineTransformCommand) nextCommand;
            getCurrentTransform().concatenate(affineTransformCommand.getValue());
        } else if (nextCommand instanceof CreateCommand) {
            AffineTransform newTransform = transforms.isEmpty() ? new AffineTransform() : new AffineTransform(getCurrentTransform());
            transforms.push(newTransform);
        } else if (nextCommand instanceof DisposeCommand) {
            transforms.pop();
        }

        return nextCommand;
    }

    @Override
    protected List<Command<?>> filter(Command<?> command) {
        if (command instanceof SetTransformCommand) {
            SetTransformCommand setTransformCommand = (SetTransformCommand) command;
            AffineTransform absoluteTransform = setTransformCommand.getValue();
            AffineTransform relativeTransform = new AffineTransform();
            try {
                AffineTransform invertedOldTransformation = getCurrentTransform().createInverse();
                relativeTransform.concatenate(invertedOldTransformation);
            } catch (NoninvertibleTransformException e) {
                e.printStackTrace();
            }
            relativeTransform.concatenate(absoluteTransform);
            TransformCommand transformCommand = new TransformCommand(relativeTransform);
            return Arrays.<Command<?>>asList(transformCommand);
        }
        return Arrays.<Command<?>>asList(command);
    }

    private AffineTransform getCurrentTransform() {
        return transforms.peek();
    }
}

