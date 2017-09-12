package iotcore.service.device.spring;

import iotcore.service.device.DeviceRegistry;
import iotcore.service.device.InMemoryDeviceRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class DeviceRegistryConfiguration {

    @Bean DeviceRegistry deviceRegistry() {
        return new InMemoryDeviceRegistry();
    }

}
