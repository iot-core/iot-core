package iotcore.service.device.binding.spring;

import iotcore.service.device.DeviceRegistry;
import iotcore.service.device.binding.DeviceRegistryBinding;
import iotcore.service.device.spring.DeviceRegistryConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DeviceRegistryConfiguration.class)
public class DeviceRegistryBindingConfiguration {

    @Bean(initMethod = "start")
    DeviceRegistryBinding deviceRegistryBinding(DeviceRegistry deviceRegistry) {
        return new DeviceRegistryBinding(deviceRegistry);
    }

}