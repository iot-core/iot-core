package org.iotbricks.hono.device.registry.client;

import static io.glutamate.lang.Exceptions.wrap;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import org.iotbricks.hono.device.registry.client.model.DeviceInformation;

import io.glutamate.time.Durations;
import io.glutamate.util.concurrent.CloseableCompletionStage;

public interface Client extends AutoCloseable {
    public CloseableCompletionStage<DeviceInformation> registerDevice(String deviceId, Map<String, ?> data);

    public CloseableCompletionStage<DeviceInformation> getDevice(String deviceId);

    public static <T> T sync(final CloseableCompletionStage<T> stage, final Duration timeout) {
        final CompletableFuture<T> future = stage.toCompletableFuture();
        try {
            if (timeout != null) {
                return wrap(() -> Durations.map(timeout, future::get));
            } else {
                return wrap((Callable<T>) future::get);
            }
        } finally {
            wrap(() -> stage.close());
        }
    }
}