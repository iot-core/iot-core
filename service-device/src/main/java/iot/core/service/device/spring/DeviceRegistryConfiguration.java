package iot.core.service.device.spring;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import iot.core.service.device.DeviceRegistry;
import iot.core.service.device.DeviceSchemaValidator;
import iot.core.service.device.InMemoryDeviceRegistry;
import iot.core.service.device.MockDeviceSchemaValidator;

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
