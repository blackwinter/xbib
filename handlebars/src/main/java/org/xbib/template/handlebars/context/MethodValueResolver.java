
package org.xbib.template.handlebars.context;

import org.xbib.template.handlebars.ValueResolver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A specialization of {@link org.xbib.template.handlebars.context.MemberValueResolver} with lookup and invocation
 * support for {@link java.lang.reflect.Method}.
 * It matches a public method.
 */
public class MethodValueResolver extends MemberValueResolver<Method> {

    /**
     * The default instance.
     */
    public static final ValueResolver INSTANCE = new MethodValueResolver();

    @Override
    public boolean matches(final Method method, final String name) {
        return isPublic(method) && method.getName().equals(name) && method.getParameterTypes().length == 0;
    }

    @Override
    protected Object invokeMember(final Method member, final Object context) {
        try {
            return member.invoke(context);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new IllegalStateException("Execution of '" + member.getName() + "' failed", cause);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(
                    "Could not access method:  '" + member.getName() + "'", ex);
        }
    }

    @Override
    protected Set<Method> members(final Class<?> clazz) {
        Set<Method> members = new LinkedHashSet<Method>();
        members(clazz, members);
        return members;
    }

    /**
     * Collect all the method from the given class.
     *
     * @param clazz   The base class.
     * @param members The members result set.
     */
    protected void members(final Class<?> clazz, final Set<Method> members) {
        if (clazz != Object.class) {
            // Keep backing up the inheritance hierarchy.
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (matches(method, memberName(method))) {
                    members.add(method);
                }
            }
            if (clazz.getSuperclass() != null) {
                members(clazz.getSuperclass(), members);
            } else if (clazz.isInterface()) {
                for (Class<?> superIfc : clazz.getInterfaces()) {
                    members(superIfc, members);
                }
            }
        }
    }

    @Override
    protected String memberName(final Method member) {
        return member.getName();
    }
}
