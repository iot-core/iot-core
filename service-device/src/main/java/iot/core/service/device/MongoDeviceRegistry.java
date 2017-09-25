package iot.core.service.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;

import java.util.Map;
import java.util.Optional;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class MongoDeviceRegistry implements DeviceRegistry {

    private final Mongo mongo;

    private final DeviceSchemaValidator schemaValidator;

    private final ObjectMapper objectMapper = new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

    public MongoDeviceRegistry(Mongo mongo, DeviceSchemaValidator schemaValidator) {
        this.mongo = mongo;
        this.schemaValidator = schemaValidator;
    }

    @Override public String create(Device device) {
        schemaValidator.validate(device);

        BasicDBObject deviceQuery = new BasicDBObject();
        deviceQuery.put("deviceId", device.getDeviceId());
        boolean deviceExists = devices().find(deviceQuery).hasNext();
        if(deviceExists) {
            throw new IllegalArgumentException("Device with given ID already exists.");
        }

        BasicDBObject mongoDevice = new BasicDBObject();
        mongoDevice.putAll(objectMapper.convertValue(device, Map.class));
        devices().save(mongoDevice);

        return device.getDeviceId();
    }

    @Override public void update(Device device) {
        schemaValidator.validate(device);

        BasicDBObject deviceQuery = new BasicDBObject();
        deviceQuery.put("deviceId", device.getDeviceId());
        boolean deviceExists = devices().find(deviceQuery).hasNext();
        if(!deviceExists) {
            throw new IllegalArgumentException("Device with given ID does not exist.");
        }

        BasicDBObject mongoDevice = new BasicDBObject();
        mongoDevice.putAll(objectMapper.convertValue(device, Map.class));
        devices().save(mongoDevice);
    }

    @Override public String save(Device device) {
        schemaValidator.validate(device);

        BasicDBObject deviceQuery = new BasicDBObject();
        deviceQuery.put("deviceId", device.getDeviceId());
        boolean deviceExists = devices().find(deviceQuery).hasNext();
        if(deviceExists) {
            update(device);
            return device.getDeviceId();
        } else {
            return create(device);
        }
    }

    @Override public Optional<Device> findById(String deviceId) {
        BasicDBObject deviceQuery = new BasicDBObject();
        deviceQuery.put("deviceId", deviceId);
        DBCursor collection = devices().find(deviceQuery);
        return collection.hasNext() ? Optional.of(objectMapper.convertValue(collection.next(), Device.class)) : Optional.empty();
    }

    // Helpers

    private DBCollection devices() {
        return mongo.getDB("device-registry").getCollection("devices");
    }

}
