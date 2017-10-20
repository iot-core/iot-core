package org.iotbricks.hono.device.registry.client;

import java.util.Map;

import org.iotbricks.hono.device.registry.client.model.DeviceInformation;

import io.glutamate.util.concurrent.CloseableCompletionStage;

public interface Client extends AutoCloseable {
    public CloseableCompletionStage<DeviceInformation> registerDevice(String deviceId, Map<String, ?> data);

    public CloseableCompletionStage<DeviceInformation> getDevice(String deviceId);
}