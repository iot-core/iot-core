package org.iotbricks.annotations;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Compiler annotation to create an AMQP transport alongside the Client.
 * <p>
 * This annotation is being processed by the Java compiler using annotation
 * processing. It requires the dependency
 * <code>org.iotbricks:iotbricks-core-annotations-processor</code> to be
 * accessible by the Java compiler.
 * <p>
 * The annotation also requires the annotation {@link Client} to be present
 * on the same package as well.
 */
@Retention(SOURCE)
@Target(PACKAGE)
public @interface AmqpTransport {
    String value();
}
