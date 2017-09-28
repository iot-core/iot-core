package iot.core.service.device;

import org.iotbricks.service.device.registry.api.Device;

public class MockDeviceSchemaValidator implements DeviceSchemaValidator {

    @Override public void validate(Device device) {
        if("geoDeviceSchema".equals(device.getType())) {
            if(!device.getProperties().containsKey("lat") || !device.getProperties().containsKey("lng")) {
                throw new RuntimeException();
            }
        } else {
            throw new RuntimeException();
        }
    }

}