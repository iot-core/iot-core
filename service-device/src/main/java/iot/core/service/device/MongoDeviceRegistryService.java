package iot.core.service.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.iotbricks.service.device.registry.api.Device;
import org.iotbricks.service.device.registry.api.DeviceRegistryService;

import java.util.Map;
import java.util.Optional;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class MongoDeviceRegistryService implements DeviceRegistryService {

    private final MongoClient mongo;

    private final DeviceSchemaValidator schemaValidator;

    private final ObjectMapper objectMapper = new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

    public MongoDeviceRegistryService(MongoClient mongo, DeviceSchemaValidator schemaValidator) {
        this.mongo = mongo;
        this.schemaValidator = schemaValidator;
    }

    @Override public String create(Device device) {
        schemaValidator.validate(device);

        if(deviceExists(device.getDeviceId())) {
            throw new IllegalArgumentException("Device with given ID already exists.");
        }

        Document deviceDocument = new Document(deviceToMap(device));
        devices().insertOne(deviceDocument);

        return device.getDeviceId();
    }

    @Override public void update(Device device) {
        schemaValidator.validate(device);

        if(!deviceExists(device.getDeviceId())) {
            throw new IllegalArgumentException("Device with given ID does not exist.");
        }

        Document mongoDevice = new Document(deviceToMap(device));
        BasicDBObject deviceQuery = new BasicDBObject();
        deviceQuery.put("deviceId", device.getDeviceId());
        devices().replaceOne(deviceQuery, mongoDevice);
    }

    @Override public String save(Device device) {
        schemaValidator.validate(device);

        if(deviceExists(device.getDeviceId())) {
            update(device);
            return device.getDeviceId();
        } else {
            return create(device);
        }
    }

    @Override public Optional<Device> findById(String deviceId) {
        BasicDBObject deviceQuery = new BasicDBObject();
        deviceQuery.put("deviceId", deviceId);
        MongoCursor<Document> collection = devices().find(deviceQuery).iterator();
        return collection.hasNext() ? Optional.of(objectMapper.convertValue(collection.next(), Device.class)) : Optional.empty();
    }

    // Helpers

    @SuppressWarnings("unchecked")
    private Map<String, Object> deviceToMap(Device device) {
        return objectMapper.convertValue(device, Map.class);
    }

    private MongoCollection<Document> devices() {
        return mongo.getDatabase("device-registry").getCollection("devices");
    }

    private boolean deviceExists(String deviceId) {
        BasicDBObject deviceQuery = new BasicDBObject();
        deviceQuery.put("deviceId", deviceId);
        return devices().find(deviceQuery).iterator().hasNext();
    }

}
