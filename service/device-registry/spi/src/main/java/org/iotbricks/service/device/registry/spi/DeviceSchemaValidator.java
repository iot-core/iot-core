package org.iotbricks.service.device.registry.spi;

import org.iotbricks.service.device.registry.api.Device;

public interface DeviceSchemaValidator {

    void validate(Device device);

}
