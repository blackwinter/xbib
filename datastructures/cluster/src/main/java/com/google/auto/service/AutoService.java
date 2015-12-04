package com.google.auto.service;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * An annotation for service providers as described in {@link java.util.ServiceLoader}. The
 * {@link com.google.auto.service.processor.AutoServiceProcessor} generates the configuration files which allows service
 * providers
 * to be loaded with {@link java.util.ServiceLoader#load(Class)}.
 *
 * <p>Service providers assert that they conform to the service provider specification.
 * Specifically, they must:
 * <ul>
 * <li>be a non-inner, non-anonymous, concrete class
 * <li>have a publicly accessible no-arg constructor
 * <li>implement the interface type returned by {@code value()}
 * </ul>
 */
@Documented
@Target(TYPE)
public @interface AutoService {
    /**
     * Returns the interface implemented by this service provider.
     */
    Class<?> value();
}
