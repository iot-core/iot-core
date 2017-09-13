package iotcore.service.device.spring;

import iotcore.service.device.DeviceRegistry;
import iotcore.service.device.DeviceSchemaValidator;
import iotcore.service.device.InMemoryDeviceRegistry;
import iotcore.service.device.MockDeviceSchemaValidator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DeviceRegistryConfiguration {

    @Bean DeviceRegistry deviceRegistry(DeviceSchemaValidator deviceSchemaValidator) {
        return new InMemoryDeviceRegistry(deviceSchemaValidator);
    }

    @ConditionalOnMissingBean
    @Bean DeviceSchemaValidator deviceSchemaValidator() {
        return new MockDeviceSchemaValidator();
    }


}
