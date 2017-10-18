package org.iotbricks.hono.device.registry.client;

import static io.glutamate.util.Optionals.presentAndEqual;
import static org.iotbricks.core.amqp.transport.utils.Properties.status;

import java.util.Map;
import java.util.Optional;

import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.serialization.jackson.JacksonSerializer;
import org.iotbricks.hono.device.registry.client.internal.AbstractHonoClient;
import org.iotbricks.hono.device.registry.client.model.DeviceInformation;

import io.glutamate.util.concurrent.CloseableCompletionStage;
import io.vertx.core.Vertx;

public class AmqpClient extends AbstractHonoClient implements Client {

    public AmqpClient(final Vertx vertx, final String tenant) {
        super(vertx, tenant, JacksonSerializer.json());
    }

    @Override
    public CloseableCompletionStage<DeviceInformation> registerDevice(final String deviceId,
            final Map<String, ?> data) {

        return request("register", data,
                reply -> success(reply, 201, DeviceInformation.class))
                        .applicationProperty("device_id", deviceId)
                        .execute();

    }

    @Override
    public CloseableCompletionStage<DeviceInformation> getDevice(final String deviceId) {

        return request("get",
                reply -> success(reply, 200, DeviceInformation.class))
                        .applicationProperty("device_id", deviceId)
                        .execute();

    }

    private <T> T success(final Message reply, final int successCode, final Class<T> clazz) {

        final Optional<Integer> status = status(reply);

        if (!presentAndEqual(status, successCode)) {
            throw unwrapError(status, reply);
        }

        return this.serializer.decodeString((String) ((AmqpValue) reply.getBody()).getValue(), clazz);
    }

    private static RuntimeException unwrapError(final Optional<Integer> status, final Message reply) {
        return status
                .map(v -> new RuntimeException(String.format("Remote service error: %s", v)))
                .orElseGet(() -> new RuntimeException("Status code missing in response"));
    }

}
