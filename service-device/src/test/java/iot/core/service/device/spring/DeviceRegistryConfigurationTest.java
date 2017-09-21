package iot.core.service.device.spring;

import org.junit.Test;
import org.springframework.boot.SpringApplication;

import iot.core.service.device.DeviceRegistry;
import iot.core.service.device.spring.DeviceRegistryConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

public class DeviceRegistryConfigurationTest {

    @Test
    public void shouldStartDeviceRegistry() {
        DeviceRegistry deviceRegistry = new SpringApplication(DeviceRegistryConfiguration.class).run().getBean(DeviceRegistry.class);
        assertThat(deviceRegistry).isNotNull();
    }


}
