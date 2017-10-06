package org.iotbricks.service.device.registry.mongo.spring;

import com.mongodb.MongoClient;
import org.iotbricks.service.device.registry.api.DeviceRegistryService;
import org.iotbricks.service.device.registry.mongo.MongoDeviceRegistryService;
import org.iotbricks.service.device.registry.spi.DeviceSchemaValidator;
import org.iotbricks.service.device.registry.spi.spring.DeviceRegistryServiceSpiConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DeviceRegistryServiceSpiConfiguration.class)
public class MongoDeviceRegistryServiceConfiguration {

    @ConditionalOnProperty(name = "iotbricks.mongo.enabled", havingValue = "true")
    @Bean DeviceRegistryService deviceRegistryService(MongoClient mongo, DeviceSchemaValidator deviceSchemaValidator) {
        return new MongoDeviceRegistryService(mongo, deviceSchemaValidator);
    }

    /**
     * If MongoDB configuration is disabled, we don't want Spring Boot to attempt to connect to MongoDB server.
     *
     * @return
     */
    @ConditionalOnProperty(name = "iotbricks.mongo.enabled", havingValue = "false", matchIfMissing = true)
    @Bean MongoClient mongo() {
        return null;
    }

}