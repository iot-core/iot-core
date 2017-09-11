package iot.core.services.device.registry.serialization.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import iot.core.services.device.registry.serialization.Serializer;
import iotcore.service.device.Device;

public class JacksonSerializer implements Serializer {

    public static Serializer json() {
        return json(false);
    }

    public static Serializer json(boolean pretty) {
        return new JacksonSerializer(ObjectMappers.defaultJson(pretty));
    }

    private ObjectMapper mapper;

    private JacksonSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String encode(Device device) {
        if (device != null) {
            try {
                return mapper.writeValueAsString(device);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    @Override
    public Device decode(String value) {
        if (value != null) {
            try {
                return mapper.readerFor(Device.class).readValue(value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

}
