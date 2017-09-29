package org.iotbricks.service.device.registry.spi.spring;

import org.iotbricks.service.device.registry.spi.AlwaysPassingDeviceSchemaValidator;
import org.iotbricks.service.device.registry.spi.DeviceSchemaValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeviceRegistryServiceSpiConfiguration {

    @ConditionalOnMissingBean
    @Bean DeviceSchemaValidator deviceSchemaValidator() {
        return new AlwaysPassingDeviceSchemaValidator();
    }

}