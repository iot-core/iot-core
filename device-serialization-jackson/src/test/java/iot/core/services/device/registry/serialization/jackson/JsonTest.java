package iot.core.services.device.registry.serialization.jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

import iot.core.service.device.Device;
import iot.core.services.device.registry.serialization.ByteSerializer;

public class JsonTest {

    private ByteSerializer serializer;

    @Before
    public void setup() {
        this.serializer = JacksonSerializer.json();
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

        assertDevice(device1, this.serializer.decode(this.serializer.encode(device1), Device.class));

        assertDevice(device1, this.serializer.decode(encode(device1, null), Device.class));
        assertDevice(device1,
                this.serializer.decode(encode(device1, ByteBuffer.allocateDirect(64 * 1024)), Device.class));

        assertDevice(device1,
                this.serializer.decode(encode(device1, ByteBuffer.allocate(1)), Device.class));

    }

    private ByteBuffer encode(final Object value, final ByteBuffer buffer) {
        final ByteBuffer buffer1 = this.serializer.encode(value, buffer);
        buffer1.flip();
        return buffer1;
    }

    private void assertDevice(final Device expected, final Device actual) {
        assertTrue(expected != actual);

        assertEquals(expected.getDeviceId(), actual.getDeviceId());
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getCreated(), actual.getCreated());
        assertEquals(expected.getUpdated(), actual.getUpdated());

        assertTrue(Maps.difference(expected.getProperties(), actual.getProperties()).areEqual());
    }

}
