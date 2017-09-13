package iot.core.services.device.registry.serialization.jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

        final String json = this.serializer.encode(device1);
        System.out.println(json);
        final Device device2 = this.serializer.decode(json);

        assertTrue(device1 != device2);
        assertEquals(device1.getDeviceId(), device2.getDeviceId());

        System.out.println(device1.getProperties());
        System.out.println(device2.getProperties());
        assertTrue(Maps.difference(device1.getProperties(), device2.getProperties()).areEqual());
    }

}
