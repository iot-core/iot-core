package org.iotbricks.core.utils.binding;

@FunctionalInterface
public interface ErrorTranslator {
    public ErrorResult translate(Throwable error);
}
