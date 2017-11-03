package org.iotbricks.service.device.registry.kafka;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.utils.Bytes;
import org.iotbricks.service.device.registry.api.Device;
import org.iotbricks.service.device.registry.api.DeviceRegistryService;

import java.util.Properties;

public class DeviceRegistryServiceEventBusConsumer {

    private final DeviceRegistryService deviceRegistryService;

    public DeviceRegistryServiceEventBusConsumer(DeviceRegistryService deviceRegistryService) {
        this.deviceRegistryService = deviceRegistryService;
    }

    void start() {
        Vertx vertx = Vertx.vertx();

        Properties properties = new Properties();
        properties.put("bootstrap.servers", "localhost:9092");
        properties.put("key.deserializer", StringDeserializer.class.getName());
        properties.put("value.deserializer", BytesDeserializer.class.getName());
        properties.put("group.id", "service.device.registry.events");

        KafkaConsumer.<String, Bytes>create(vertx, properties).handler(event -> {
            String deviceId = event.key();
            if(event.value() == null) {
                deviceRegistryService.remove(deviceId);
                return;
            }
            Device device = Json.decodeValue(new String(event.value().get()), Device.class);
            deviceRegistryService.save(device);
        }).subscribe("service.device.registry");
    }

}