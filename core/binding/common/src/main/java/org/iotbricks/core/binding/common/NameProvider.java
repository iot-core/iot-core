package org.iotbricks.core.binding.common;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Named;

import org.iotbricks.annotations.ServiceName;

import io.glutamate.lang.Annotations;
import io.glutamate.lang.Annotations.ScanMode;

@FunctionalInterface
public interface NameProvider {
    /**
     * Provide a service name from the bean and service class
     *
     * @param bean
     *            The service bean instance
     * @param serviceClass
     *            The service class
     * @return The service name to use, must not return null {@code null}
     */
    public String provideName(Object bean, Class<?> serviceClass);

    /**
     * Use the value of an annotation as service name.
     *
     * @param annotationClass
     *            The annotation to scan for. e.g. {@code ServiceClass.class}
     * @param nameExtractor
     *            The function to extract the name from the annotation instance.
     *            e.g. {@code ServiceClass::value}
     * @param <A>
     *            The annotation type
     * @return the new name provider
     */
    public static <A extends Annotation> NameProvider namedBy(final Class<A> annotationClass,
            final Function<A, String> nameExtractor) {

        Objects.requireNonNull(annotationClass);
        Objects.requireNonNull(nameExtractor);

        return (bean, serviceClass) -> {

            final Optional<A> annotation = Annotations.scanFor(annotationClass, serviceClass, ScanMode.BREADTH);

            if (!annotation.isPresent()) {
                throw new IllegalStateException(
                        "Service class is missing annotation @" + annotationClass.getName());
            }

            return nameExtractor.apply(annotation.get());
        };
    }

    /**
     * Extract the service name from the {@link ServiceName} annotation.
     *
     * @return the new name provider
     */
    public static NameProvider serviceName() {
        return namedBy(ServiceName.class, ServiceName::value);
    }

    /**
     * Extract the service name from the {@link Named} annotation.
     *
     * @return the new name provider
     */
    public static NameProvider named() {
        return namedBy(Named.class, Named::value);
    }

    public static NameProvider name(final String name) {
        return (bean, serviceClass) -> name;
    }
}