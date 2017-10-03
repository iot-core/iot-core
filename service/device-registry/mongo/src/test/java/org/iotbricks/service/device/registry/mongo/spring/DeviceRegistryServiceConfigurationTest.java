package org.iotbricks.service.device.registry.mongo.spring;

import org.iotbricks.service.device.registry.api.Device;
import org.iotbricks.service.device.registry.api.DeviceRegistryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "iotbricks.mongo.enabled=true")
@SpringBootApplication
public class DeviceRegistryServiceConfigurationTest {

    @Autowired
    DeviceRegistryService deviceRegistryService;

    @Test
    public void shouldStartDeviceRegistry() {
        assertThat(deviceRegistryService).isNotNull();
    }

    @Test
    public void shouldLoadDevice() {
        Device device = new Device();
        device.setDeviceId(randomUUID().toString());
        deviceRegistryService.create(device);

        Optional<Device> loadedDevice = deviceRegistryService.findById(device.getDeviceId());

        assertThat(loadedDevice.get()).isNotNull();
    }

    @Test
    public void shouldUpdateDevice() {
        Device device = new Device();
        device.setDeviceId(randomUUID().toString());
        deviceRegistryService.create(device);

        Optional<Device> loadedDevice = deviceRegistryService.findById(device.getDeviceId());
        loadedDevice.get().setType("newType");
        deviceRegistryService.update(loadedDevice.get());
        loadedDevice = deviceRegistryService.findById(device.getDeviceId());

        assertThat(loadedDevice.get().getType()).isEqualTo("newType");
    }


}
