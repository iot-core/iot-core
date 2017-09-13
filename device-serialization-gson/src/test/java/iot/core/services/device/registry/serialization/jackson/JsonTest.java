package iot.core.services.device.registry.serialization.jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

import iot.core.services.device.registry.serialization.Serializer;
import iotcore.service.device.Device;

public class JsonTest {

    private Serializer serializer;

    @Before
    public void setup() {
        this.serializer = JacksonSerializer.json(true);
    }

    @Test
    public void test1() {
        final Device device1 = new Device();
        device1.setDeviceId("id1");

        final Map<String, Object> data = new HashMap<>();
        data.put("string1", "String 1");
        data.put("long1", Long.MAX_VALUE);
        data.put("double1", Double.MAX_VALUE);
        data.put("instant1", Instant.now().toString());

        device1.setProperties(data);

        final String json = new String (this.serializer.encode(device1), StandardCharsets.UTF_8);
        System.out.println(json);
        final Device device2 = this.serializer.decode(json.getBytes(StandardCharsets.UTF_8), Device.class);

        assertTrue(device1 != device2);

        assertEquals(device1.getDeviceId(), device2.getDeviceId());
        assertEquals(device1.getType(), device2.getType());
        assertEquals(device1.getCreated(), device2.getCreated());
        assertEquals(device1.getUpdated(), device2.getUpdated());

        System.out.println(device1.getProperties());
        System.out.println(device2.getProperties());
        assertTrue(Maps.difference(device1.getProperties(), device2.getProperties()).areEqual());
    }

}
