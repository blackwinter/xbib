
package org.xbib.template.handlebars.helper;

import org.xbib.template.handlebars.Helper;
import org.xbib.template.handlebars.HelperRegistry;
import org.xbib.template.handlebars.internal.Files;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.xbib.template.handlebars.util.Validate.isTrue;
import static org.xbib.template.handlebars.util.Validate.notEmpty;
import static org.xbib.template.handlebars.util.Validate.notNull;

/**
 * Default implementation of {@link HelperRegistry}.
 */
public class DefaultHelperRegistry implements HelperRegistry {

    /**
     * The helper registry.
     */
    private final Map<String, Helper<?>> helpers = new HashMap<String, Helper<?>>();

    public DefaultHelperRegistry(ClassLoader classLoader) {
        registerHelper(WithHelper.NAME, WithHelper.INSTANCE);
        registerHelper(IfHelper.NAME, IfHelper.INSTANCE);
        registerHelper(IfConditionHelper.NAME, IfConditionHelper.INSTANCE);
        registerHelper(SelectHelper.NAME, SelectHelper.INSTANCE);
        registerHelper(EvalHelper.NAME, EvalHelper.INSTANCE);
        registerHelper(UnlessHelper.NAME, UnlessHelper.INSTANCE);
        registerHelper(EachHelper.NAME, EachHelper.INSTANCE);
        registerHelper(EmbeddedHelper.NAME, EmbeddedHelper.INSTANCE);
        registerHelper(BlockHelper.NAME, BlockHelper.INSTANCE);
        registerHelper(PartialHelper.NAME, PartialHelper.INSTANCE);
        registerHelper("i18n", new I18nHelper(classLoader));
        registerHelper("i18nJs", new I18nJSHelper(classLoader));
        registerHelper(PaginationHelper.NAME, PaginationHelper.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C> Helper<C> helper(final String name) {
        notEmpty(name, "A helper's name is required.");
        return (Helper<C>) helpers.get(name);
    }

    @Override
    public <H> HelperRegistry registerHelper(final String name, final Helper<H> helper) {
        notEmpty(name, "A helper's name is required.");
        notNull(helper, "A helper is required.");

        Helper<?> oldHelper = helpers.put(name, helper);
        return this;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public HelperRegistry registerHelpers(final Object helperSource) {
        notNull(helperSource, "The helper source is required.");
        isTrue(!(helperSource instanceof String), "java.lang.String isn't a helper source.");
        try {
            if (helperSource instanceof File) {
                // adjust to File version
                return registerHelpers((File) helperSource);
            } else if (helperSource instanceof URI) {
                // adjust to URI version
                return registerHelpers((URI) helperSource);
            } else if (helperSource instanceof Class) {
                // adjust to Class version
                return registerHelpers((Class) helperSource);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Can't register helpres", ex);
        }
        registerDynamicHelper(helperSource, helperSource.getClass());
        return this;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public HelperRegistry registerHelpers(final Class<?> helperSource) {
        notNull(helperSource, "The helper source is required.");
        if (Enum.class.isAssignableFrom(helperSource)) {
            Enum[] helpers = ((Class<Enum>) helperSource).getEnumConstants();
            for (Enum helper : helpers) {
                isTrue(helper instanceof Helper, "'%s' isn't a helper.", helper.name());
                registerHelper(helper.name(), (Helper) helper);
            }
        } else {
            registerDynamicHelper(null, helperSource);
        }
        return this;
    }

    @Override
    public HelperRegistry registerHelpers(final URI location) throws Exception {
        return registerHelpers(location.getPath(), Files.read(location.toString()));
    }

    @Override
    public HelperRegistry registerHelpers(final File input) throws Exception {
        return registerHelpers(input.getAbsolutePath(), Files.read(input));
    }

    @Override
    public HelperRegistry registerHelpers(final String filename, final Reader source)
            throws Exception {
        return registerHelpers(filename, Files.read(source));
    }

    @Override
    public HelperRegistry registerHelpers(final String filename, final InputStream source)
            throws Exception {
        return registerHelpers(filename, Files.read(source));
    }

    @Override
    public HelperRegistry registerHelpers(final String filename, final String source)
            throws Exception {
        notNull(filename, "The filename is required.");
        notEmpty(source, "The source is required.");
        //handlebarsJs.registerHelpers(filename, source);
        return this;
    }

    @Override
    public Set<Entry<String, Helper<?>>> helpers() {
        return this.helpers.entrySet();
    }

    /**
     * <p>
     * Register all the helper methods for the given helper source.
     * </p>
     *
     * @param source The helper source.
     * @param clazz  The helper source class.
     */
    private void registerDynamicHelper(final Object source, final Class<?> clazz) {
        int size = helpers.size();
        int replaced = 0;
        if (clazz != Object.class) {
            Set<String> overloaded = new HashSet<String>();
            // Keep backing up the inheritance hierarchy.
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                boolean isPublic = Modifier.isPublic(method.getModifiers());
                String helperName = method.getName();
                if (isPublic && CharSequence.class.isAssignableFrom(method.getReturnType())) {
                    boolean isStatic = Modifier.isStatic(method.getModifiers());
                    if (source != null || isStatic) {
                        if (helpers.containsKey(helperName)) {
                            replaced++;
                        }
                        isTrue(overloaded.add(helperName), "name conflict found: " + helperName);
                        registerHelper(helperName, new MethodHelper(method, source));
                    }
                }
            }
        }
        isTrue((size + replaced) != helpers.size(),
                "No helper method was found in: " + clazz.getName());
    }

}
