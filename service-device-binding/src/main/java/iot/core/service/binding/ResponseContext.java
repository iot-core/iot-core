package iot.core.service.binding;

public interface ResponseContext {
    public void sendMessage(String address, Object value);
}
