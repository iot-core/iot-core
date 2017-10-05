package iot.core.distribution.deviceregistry;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = {"org.iotbricks", "iot.core"})
public class DeviceRegistryServiceApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(DeviceRegistryServiceApplication.class).run(args);
    }

}