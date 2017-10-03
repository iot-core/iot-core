package iot.core.distribution.deviceregistry;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = {"org.iotbricks", "iot.core"})
public class DeviceRegistryApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(DeviceRegistryApplication.class).run(args);
    }

}