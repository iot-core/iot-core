package org.iotbricks.core.binding.common;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.iotbricks.core.binding.ServiceBinding;
import org.iotbricks.core.binding.common.SimpleVerbRequestHandler.VerbHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BeanServiceBinding extends DefaultServiceBinding {

    private static final Logger logger = LoggerFactory.getLogger(BeanServiceBinding.class);

    public static class Builder {

        private final Object bean;
        private final Class<?> serviceClass;
        private NameProvider nameProvider;

        private Builder(final Object bean) {
            this.bean = bean;
            this.serviceClass = bean.getClass();
        }

        /**
         * Use the provided name as service name.
         *
         * @param name
         *            the name to use
         * @return the builder instance
         */
        public Builder name(final String name) {
            Objects.requireNonNull(name);
            this.nameProvider = NameProvider.name(name);
            return this;
        }

        /**
         * Set the name provider.
         *
         * @param nameProvider
         *            the name provider to use
         * @return the builder instance
         */
        public Builder nameProvider(final NameProvider nameProvider) {
            Objects.requireNonNull(nameProvider);
            this.nameProvider = nameProvider;
            return this;
        }

        public ServiceBinding build() {
            Objects.requireNonNull(this.nameProvider, "'nameProvider' must be set");
            return of(this.nameProvider.provideName(this.bean, this.serviceClass), this.bean, this.serviceClass);
        }

    }

    public static Builder newBinding(final Object bean) {
        Objects.requireNonNull(bean);

        return new Builder(bean);
    }

    private BeanServiceBinding(final String serviceName, final Map<String, VerbHandler> verbMap) {
        super(serviceName, new SimpleVerbRequestHandler(verbMap));
    }

    private static ServiceBinding of(final String serviceName, final Object bean, final Class<?> serviceClazz) {

        final Map<String, VerbHandler> verbMap = new HashMap<>();

        try {
            final Lookup lookup = MethodHandles.publicLookup();
            final Method[] methods = serviceClazz.getMethods();

            for (final Method method : methods) {

                final MethodHandle handle = lookup
                        .unreflect(method)
                        .bindTo(bean);

                verbMap.put(method.getName(), createVerbHandler(method, handle));

            }
        } catch (final Exception e) {
            throw new RuntimeException("Failed to bind", e);
        }

        return new DefaultServiceBinding(serviceName, new SimpleVerbRequestHandler(verbMap));
    }

    private static VerbHandler createVerbHandler(final Method method, final MethodHandle handle) {

        final Parameter[] parameters = method.getParameters();

        final Class<?>[] parameterTypes = new Class<?>[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterTypes[i] = parameters[i].getType();
        }

        return request -> {
            final Object[] arguments = request.decodeRequest(parameterTypes);

            logger.trace("Service method arguments: {}", new Object[] { arguments });

            try {
                return handle.invokeWithArguments(arguments);
            } catch (final Exception e) {
                logger.info("Service method invocation failed", e);
                throw e;
            } catch (final Throwable e) {
                logger.info("Service method invocation failed", e);
                throw new InvocationTargetException(e);
            }
        };
    }

}
