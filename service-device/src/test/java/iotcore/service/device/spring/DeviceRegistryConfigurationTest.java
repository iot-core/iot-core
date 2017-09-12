package iotcore.service.device.spring;

import iotcore.service.device.DeviceRegistry;
import org.junit.Test;
import org.springframework.boot.SpringApplication;

import static org.assertj.core.api.Assertions.assertThat;

public class DeviceRegistryConfigurationTest {

    @Test
    public void shouldStartDeviceRegistry() {
        DeviceRegistry deviceRegistry = new SpringApplication(DeviceRegistryConfiguration.class).run().getBean(DeviceRegistry.class);
        assertThat(deviceRegistry).isNotNull();
    }


}
