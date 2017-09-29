package org.iotbricks.service.device.registry.spi;

import org.iotbricks.service.device.registry.api.Device;
import org.junit.Test;

public class AlwaysPassingDeviceSchemaValidatorTest {

    DeviceSchemaValidator validator = new AlwaysPassingDeviceSchemaValidator();

    @Test
    public void shouldPassValidation() {
        validator.validate(new Device());
    }

}
