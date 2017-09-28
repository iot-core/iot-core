package iot.core.service.device.spring;

import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import iot.core.service.device.AlwaysPassingDeviceSchemaValidator;
import iot.core.service.device.DeviceSchemaValidator;
import iot.core.service.device.MongoDeviceRegistry;
import org.iotbricks.service.device.registry.api.DeviceRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DeviceRegistryConfiguration {

    @Bean DeviceRegistry deviceRegistry(IMongodConfig mongodConfig, DeviceSchemaValidator deviceSchemaValidator) {
        MongoClient mongoClient = new MongoClient("localhost", mongodConfig.net().getPort());
        return new MongoDeviceRegistry(mongoClient, deviceSchemaValidator);
    }

    @ConditionalOnMissingBean
    @Bean DeviceSchemaValidator deviceSchemaValidator() {
        return new AlwaysPassingDeviceSchemaValidator();
    }


}
