package iot.core.service.device.binding.spring;

import org.iotbricks.service.device.registry.api.DeviceRegistryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import iot.core.service.device.binding.DeviceRegistryBinding;
import iot.core.service.device.spring.DeviceRegistryConfiguration;

@Configuration
@Import(DeviceRegistryConfiguration.class)
public class DeviceRegistryBindingConfiguration {

    @Bean(initMethod = "start")
    public DeviceRegistryBinding deviceRegistryBinding(DeviceRegistryService deviceRegistryService) {
        return new DeviceRegistryBinding(deviceRegistryService);
    }

}