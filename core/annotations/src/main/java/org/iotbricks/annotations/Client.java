package org.iotbricks.annotations;

import static java.lang.annotation.ElementType.PACKAGE;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Compiler annotation to create the core client interfaces and glue code.
 * <p>
 * This annotation is being processed by the Java compiler using annotation
 * processing. It requires the dependency
 * <code>org.iotbricks:iotbricks-core-annotations-processor</code> to be
 * accessible by the Java compiler.
 */
@Retention(SOURCE)
@Target(PACKAGE)
public @interface Client {
    Class<?> value();
}
