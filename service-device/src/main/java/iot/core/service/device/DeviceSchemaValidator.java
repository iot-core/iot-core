package iot.core.service.device;

import org.iotbricks.service.device.registry.api.Device;

public interface DeviceSchemaValidator {

    void validate(Device device);

}
