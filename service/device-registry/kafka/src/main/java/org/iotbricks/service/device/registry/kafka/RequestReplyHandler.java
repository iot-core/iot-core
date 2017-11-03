package org.iotbricks.service.device.registry.kafka;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.proton.ProtonClient;
import io.vertx.proton.ProtonConnection;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.iotbricks.service.device.registry.api.Device;
import org.iotbricks.service.device.registry.api.DeviceRegistryService;

import java.util.Optional;

import static io.vertx.proton.ProtonHelper.message;

public class RequestReplyHandler {

    private final DeviceRegistryService deviceRegistryService;

    public RequestReplyHandler(DeviceRegistryService deviceRegistryService) {
        this.deviceRegistryService = deviceRegistryService;
    }

    void start() {
        Vertx vertx = Vertx.vertx();
        ProtonClient client = ProtonClient.create(vertx);
        client.connect("localhost", 5672, res -> {
            ProtonConnection connection = res.result();
            helloWorldSendAndConsumeExample(connection);
        });
    }

    private void helloWorldSendAndConsumeExample(ProtonConnection connection) {
        connection.open().createReceiver("service.device.registry.requests").handler((delivery, msg) -> {
            String subject = msg.getSubject();
            if (subject.equals("findById")) {
                Section body = msg.getBody();
                if (body instanceof AmqpValue) {
                    String deviceId = (String) ((AmqpValue) body).getValue();
                    Optional<Device> device = deviceRegistryService.findById(deviceId);
                    String response;
                    if(device.isPresent()) {
                        response = Json.encode(device.get());
                    } else {
                        response = "{}";
                    }
                    connection.createSender(msg.getReplyTo()).send(message(response));
                }
            }
        }).open();
    }

}
