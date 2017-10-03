package org.iotbricks.service.device.registry.inmemory.spring;

import org.iotbricks.service.device.registry.api.DeviceRegistryService;
import org.iotbricks.service.device.registry.inmemory.InMemoryDeviceRegistryService;
import org.iotbricks.service.device.registry.spi.DeviceSchemaValidator;
import org.iotbricks.service.device.registry.spi.spring.DeviceRegistryServiceSpiConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DeviceRegistryServiceSpiConfiguration.class)
public class InMemoryDeviceRegistryServiceConfiguration {

    @ConditionalOnMissingBean
    @Bean DeviceRegistryService deviceRegistryService(DeviceSchemaValidator deviceSchemaValidator) {
        return new InMemoryDeviceRegistryService(deviceSchemaValidator);
    }


}
