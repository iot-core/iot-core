package iot.core.services.device.registry.client;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

import iotcore.service.device.AlwaysPassingDeviceSchemaValidator;
import iotcore.service.device.Device;
import iotcore.service.device.InMemoryDeviceRegistry;

public class LocalTest {

    private LocalClient client;

    @Before
    public void setup() {
        this.client = new LocalClient(new InMemoryDeviceRegistry(new AlwaysPassingDeviceSchemaValidator()));
    }

    @After
    public void cleanup() throws Exception {
        this.client.close();
    }

    @Test
    public void test1() {

        final Device device1 = new Device("id1", new Date(), new Date(), "type1", emptyMap());

        this.client.sync().save(device1);

        final Device device2 = this.client.sync().findById("id1").get();

        assertDevice(device1, device2);
    }

    private void assertDevice(final Device expected, final Device actual) {
        assertEquals(expected.getDeviceId(), actual.getDeviceId());
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getCreated(), actual.getCreated());
        assertEquals(expected.getUpdated(), actual.getUpdated());

        assertTrue(Maps.difference(expected.getProperties(), actual.getProperties()).areEqual());
    }

}
