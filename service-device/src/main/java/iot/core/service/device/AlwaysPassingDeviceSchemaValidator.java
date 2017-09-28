package iot.core.service.device;

import org.iotbricks.service.device.registry.api.Device;

public class AlwaysPassingDeviceSchemaValidator implements DeviceSchemaValidator {

    @Override public void validate(Device device) {
    }

}
