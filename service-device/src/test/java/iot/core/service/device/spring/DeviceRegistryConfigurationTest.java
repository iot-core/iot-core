package iot.core.service.device.spring;

import iot.core.service.device.Device;
import iot.core.service.device.DeviceRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DeviceRegistryConfigurationTest {

    @Autowired
    DeviceRegistry deviceRegistry;

    @Test
    public void shouldStartDeviceRegistry() {
        assertThat(deviceRegistry).isNotNull();
    }

    @Test
    public void shouldLoadDevice() {
        Device device = new Device();
        device.setDeviceId(randomUUID().toString());
        deviceRegistry.create(device);

        Optional<Device> loadedDevice = deviceRegistry.findById(device.getDeviceId());

        assertThat(loadedDevice.get()).isNotNull();
    }


}
