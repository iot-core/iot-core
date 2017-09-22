package iot.core.distribution.deviceregistry;

import iot.core.service.device.AlwaysPassingDeviceSchemaValidator;
import iot.core.service.device.DeviceSchemaValidator;
import iot.core.service.device.binding.spring.DeviceRegistryBindingConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DeviceRegistryApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(DeviceRegistryApplication.class, DeviceRegistryBindingConfiguration.class).run(args);
    }

    @Bean DeviceSchemaValidator deviceSchemaValidator() {
        return new AlwaysPassingDeviceSchemaValidator();
    }

}