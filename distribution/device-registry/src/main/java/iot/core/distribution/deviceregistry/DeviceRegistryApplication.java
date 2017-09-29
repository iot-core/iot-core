package iot.core.distribution.deviceregistry;

import iot.core.service.device.binding.spring.DeviceRegistryBindingConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class DeviceRegistryApplication {

    private DeviceRegistryApplication() {
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(DeviceRegistryApplication.class, DeviceRegistryBindingConfiguration.class).run(args);
    }

}