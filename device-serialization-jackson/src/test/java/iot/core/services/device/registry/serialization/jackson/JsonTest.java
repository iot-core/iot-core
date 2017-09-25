package iot.core.services.device.registry.serialization.jackson;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
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
    public void test1() throws IOException {
        final Device device1 = new Device();
        device1.setDeviceId("id1");

        final Map<String, Object> data = new HashMap<>();
        data.put("string1", "String 1");
        data.put("long1", Long.MAX_VALUE);
        data.put("double1", Double.MAX_VALUE);
        data.put("instant1", Instant.now().toString());

        device1.setProperties(data);

        this.serializer.encodeTo(device1, System.out);
        System.out.println();
        this.serializer.encodeTo(this.serializer.decode(this.serializer.encode(device1), Device.class), System.out);
        System.out.println();

        assertDevice(device1, this.serializer.decode(this.serializer.encode(device1), Device.class));

        assertDevice(device1, this.serializer.decode(encode(device1, null), Device.class));
        assertDevice(device1,
                this.serializer.decode(encode(device1, ByteBuffer.allocateDirect(64 * 1024)), Device.class));

        assertDevice(device1,
                this.serializer.decode(encode(device1, ByteBuffer.allocate(1)), Device.class));

    }

    @Test
    public void testOptional1() {
        final String result = this.serializer.decode(this.serializer.encode(Optional.<String>empty()), String.class);
        Assert.assertNull(result);
    }

    @Test
    public void testOptional2() {
        final String result = this.serializer.decode(this.serializer.encode(Optional.<String>of("FooBar")),
                String.class);
        Assert.assertEquals("FooBar", result);
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

    @Test
    public void testEnsureNotClosed() throws IOException {
        final Device device = new Device();

        final AtomicBoolean closeCheck = new AtomicBoolean();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final FilterOutputStream assertOut = new FilterOutputStream(out) {
            @Override
            public void close() throws IOException {
                closeCheck.set(true);
                super.close();
            }
        };

        this.serializer.encodeTo(device, assertOut);
        assertFalse(closeCheck.get());
        final byte[] array1 = out.toByteArray();

        this.serializer.encodeTo(device, assertOut);
        assertFalse(closeCheck.get());
        final byte[] array2 = out.toByteArray();

        Assert.assertTrue(array2.length > array1.length);
        assertThat(array1, not(equalTo(array2)));
    }

}
