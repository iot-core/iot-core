package org.iotbricks.core.binding;

public interface ResponseContext {
    public void sendMessage(String address, Object value);
}
