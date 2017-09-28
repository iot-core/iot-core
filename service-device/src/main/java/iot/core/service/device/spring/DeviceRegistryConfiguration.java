package iot.core.service.device.spring;

import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import iot.core.service.device.AlwaysPassingDeviceSchemaValidator;
import iot.core.service.device.DeviceSchemaValidator;
import iot.core.service.device.MongoDeviceRegistryService;
import org.iotbricks.service.device.registry.api.DeviceRegistryService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DeviceRegistryConfiguration {

    @Bean DeviceRegistryService deviceRegistry(IMongodConfig mongodConfig, DeviceSchemaValidator deviceSchemaValidator) {
        MongoClient mongoClient = new MongoClient("localhost", mongodConfig.net().getPort());
        return new MongoDeviceRegistryService(mongoClient, deviceSchemaValidator);
    }

    @ConditionalOnMissingBean
    @Bean DeviceSchemaValidator deviceSchemaValidator() {
        return new AlwaysPassingDeviceSchemaValidator();
    }


}
