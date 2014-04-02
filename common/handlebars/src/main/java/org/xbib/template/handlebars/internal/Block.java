
package org.xbib.template.handlebars.internal;

import org.xbib.template.handlebars.HandlebarsContext;
import org.xbib.template.handlebars.Handlebars;
import org.xbib.template.handlebars.Helper;
import org.xbib.template.handlebars.Lambda;
import org.xbib.template.handlebars.Options;
import org.xbib.template.handlebars.TagType;
import org.xbib.template.handlebars.Template;
import org.xbib.template.handlebars.helper.EachHelper;
import org.xbib.template.handlebars.helper.IfHelper;
import org.xbib.template.handlebars.helper.UnlessHelper;
import org.xbib.template.handlebars.helper.WithHelper;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.xbib.template.handlebars.util.Validate.isTrue;
import static org.xbib.template.handlebars.util.Validate.notNull;

/**
 * Blocks render blocks of text one or more times, depending on the value of
 * the key in the current context.
 * A section begins with a pound and ends with a slash. That is, {{#person}}
 * begins a "person" section while {{/person}} ends it.
 * The behavior of the block is determined by the value of the key if the block
 * isn't present.
 */
class Block extends HelperResolver {

    /**
     * The body template.
     */
    private Template body;

    /**
     * The section's name.
     */
    private final String name;

    /**
     * True if it's inverted.
     */
    private final boolean inverted;

    /**
     * Section's description '#' or '^'.
     */
    private final String type;

    /**
     * The start delimiter.
     */
    private String startDelimiter;

    /**
     * The end delimiter.
     */
    private String endDelimiter;

    /**
     * Inverse section for if/else clauses.
     */
    private Template inverse;

    /**
     * The inverse label: 'else' or '^'.
     */
    private String inverseLabel;

    /**
     * Creates a new {@link org.xbib.template.handlebars.internal.Block}.
     *
     * @param handlebars The handlebars object.
     * @param name       The section's name.
     * @param inverted   True if it's inverted.
     * @param params     The parameter list.
     * @param hash       The hash.
     */
    public Block(final Handlebars handlebars, final String name,
                 final boolean inverted, final List<Object> params,
                 final Map<String, Object> hash) {
        super(handlebars);
        this.name = notNull(name, "The name is required.");
        this.inverted = inverted;
        type = inverted ? "^" : "#";
        params(params);
        hash(hash);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void merge(final HandlebarsContext context, final Writer writer) throws IOException {
        if (body == null) {
            return;
        }
        Helper<Object> helper = helper(name);
        Template template = body;
        final Object childContext;
        HandlebarsContext currentScope = context;
        if (helper == null) {
            childContext = transform(context.get(name));
            final String hname;
            if (inverted) {
                hname = UnlessHelper.NAME;
            } else if (childContext instanceof Iterable) {
                hname = EachHelper.NAME;
            } else if (childContext instanceof Boolean) {
                hname = IfHelper.NAME;
            } else if (childContext instanceof Lambda) {
                hname = WithHelper.NAME;
                template = Lambdas
                        .compile(handlebars,
                                (Lambda<Object, Object>) childContext,
                                context, template,
                                startDelimiter, endDelimiter);
            } else {
                hname = WithHelper.NAME;
                currentScope = HandlebarsContext.newContext(context, childContext);
            }
            // A built-in helper might be override it.
            helper = handlebars.helper(hname);
        } else {
            childContext = transform(determineContext(context));
        }
        Options options = new Options.Builder(handlebars, TagType.SECTION, currentScope, template)
                .setInverse(inverse == null ? Template.EMPTY : inverse)
                .setParams(params(currentScope))
                .setHash(hash(context))
                .build();
        options.data(HandlebarsContext.PARAM_SIZE, this.params.size());

        CharSequence result = helper.apply(childContext, options);
        if (result != null && result.length() > 0) {
            writer.append(result);
        }
    }

    /**
     * The section's name.
     *
     * @return The section's name.
     */
    public String name() {
        return name;
    }

    /**
     * True if it's an inverted section.
     *
     * @return True if it's an inverted section.
     */
    public boolean inverted() {
        return inverted;
    }

    /**
     * Set the template body.
     *
     * @param body The template body. Required.
     * @return This section.
     */
    public Block body(final Template body) {
        this.body = notNull(body, "The template's body is required.");
        return this;
    }

    /**
     * Set the inverse template.
     *
     * @param inverseLabel One of 'else' or '^'. Required.
     * @param inverse      The inverse template. Required.
     * @return This section.
     */
    public Template inverse(final String inverseLabel, final Template inverse) {
        notNull(inverseLabel, "The inverseLabel can't be null.");
        isTrue(inverseLabel.equals("^") || inverseLabel.equals("else"),
                "The inverseLabel must be one of '^' or 'else'.");
        this.inverseLabel = inverseLabel;
        this.inverse = notNull(inverse, "The inverse's template is required.");
        return this;
    }

    /**
     * The inverse template for else clauses.
     *
     * @return The inverse template for else clauses.
     */
    public Template inverse() {
        return inverse;
    }

    /**
     * Set the end delimiter.
     *
     * @param endDelimiter The end delimiter.
     * @return This section.
     */
    public Block endDelimiter(final String endDelimiter) {
        this.endDelimiter = endDelimiter;
        return this;
    }

    /**
     * Set the start delimiter.
     *
     * @param startDelimiter The start delimiter.
     * @return This section.
     */
    public Block startDelimiter(final String startDelimiter) {
        this.startDelimiter = startDelimiter;
        return this;
    }

    /**
     * The template's body.
     *
     * @return The template's body.
     */
    public Template body() {
        return body;
    }

    @Override
    public String text() {
        return text(true);
    }

    /**
     * Build a text version of this block.
     *
     * @param complete True if the inner block should be added.
     * @return A string version of this block.
     */
    private String text(final boolean complete) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(startDelimiter).append(type).append(name);
        String params = paramsToString();
        if (params.length() > 0) {
            buffer.append(" ").append(params);
        }
        String hash = hashToString();
        if (hash.length() > 0) {
            buffer.append(" ").append(hash);
        }
        buffer.append(endDelimiter);
        if (complete) {
            buffer.append(body == null ? "" : body.text());
            buffer.append(inverse == null ? "" : "{{" + inverseLabel + "}}" + inverse.text());
        } else {
            buffer.append("\n...\n");
        }
        buffer.append(startDelimiter).append('/').append(name).append(endDelimiter);
        return buffer.toString();
    }

    /**
     * The start delimiter.
     *
     * @return The start delimiter.
     */
    public String startDelimiter() {
        return startDelimiter;
    }

    /**
     * The end delimiter.
     *
     * @return The end delimiter.
     */
    public String endDelimiter() {
        return endDelimiter;
    }

    @Override
    public List<String> collect(final TagType... tagType) {
        if (body != null) {
            Set<String> tagNames = new LinkedHashSet<String>();
            tagNames.addAll(super.collect(tagType));
            tagNames.addAll(body.collect(tagType));
            return new ArrayList<String>(tagNames);
        } else {
            return super.collect(tagType);
        }
    }

    @Override
    protected void collect(final Collection<String> result, final TagType tagType) {
        if (tagType == TagType.SECTION) {
            result.add(name);
        }
    }

    @Override
    public String toString() {
        return text(false);
    }
}
