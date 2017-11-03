package org.iotbricks.service.device.registry.kafka;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.proton.ProtonClient;
import io.vertx.proton.ProtonConnection;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Bytes;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.iotbricks.service.device.registry.api.Device;

import java.util.Properties;

public class DeviceRegistryServiceEventBusProducer {

    void start() {
        Vertx vertx = Vertx.vertx();

        Properties properties = new Properties();
        properties.put("bootstrap.servers", "localhost:9092");
        properties.put("key.serializer", StringSerializer.class.getName());
        properties.put("value.serializer", BytesSerializer.class.getName());
        properties.put("group.id", "service.device.registry.events");

        KafkaProducer kafkaProducer = KafkaProducer.create(vertx, properties);

        ProtonClient.create(vertx).connect("localhost", 5672, res -> {
            ProtonConnection connection = res.result();
            producerConnectionHandler(connection, kafkaProducer);
        });
    }

    private static void producerConnectionHandler(ProtonConnection connection, KafkaProducer kafkaProducer) {
        connection.open().createReceiver("service.device.registry").handler((delivery, msg) -> {
            String subject = msg.getSubject();
            if (subject.equals("save")) {
                Section body = msg.getBody();
                if (body instanceof AmqpValue) {
                    String content = (String) ((AmqpValue) body).getValue();
                    Device device = Json.decodeValue(content, Device.class);
                    String deviceId = device.getDeviceId();
                    kafkaProducer.write(KafkaProducerRecord.create("service.device.registry", deviceId, new Bytes(content.getBytes())));
                }
            } else if (subject.equals("remove")) {
                Section body = msg.getBody();
                if (body instanceof AmqpValue) {
                    String deviceId = (String) ((AmqpValue) body).getValue();
                    kafkaProducer.write(KafkaProducerRecord.create("service.device.registry", deviceId, null));
                }
            }
        }).open();
    }

}
