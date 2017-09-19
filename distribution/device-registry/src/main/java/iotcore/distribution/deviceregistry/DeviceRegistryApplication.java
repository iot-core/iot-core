package iotcore.distribution.deviceregistry;

import iotcore.service.device.AlwaysPassingDeviceSchemaValidator;
import iotcore.service.device.DeviceSchemaValidator;
import iotcore.service.device.binding.spring.DeviceRegistryBindingConfiguration;
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