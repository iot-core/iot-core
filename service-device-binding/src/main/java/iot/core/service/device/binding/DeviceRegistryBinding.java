package iot.core.service.device.binding;

import static org.iotbricks.core.utils.binding.ErrorCondition.DECODE_ERROR;

import java.util.Optional;

import org.iotbricks.common.device.registry.serialization.jackson.JacksonSerializer;
import org.iotbricks.core.binding.proton.ProtonBindingServer;
import org.iotbricks.core.binding.proton.ProtonRequestContext;
import org.iotbricks.core.binding.proton.ProtonServiceBinding;
import org.iotbricks.core.utils.binding.RequestException;
import org.iotbricks.service.device.registry.api.Device;
import org.iotbricks.service.device.registry.api.DeviceRegistryService;
import org.iotbricks.service.device.registry.inmemory.InMemoryDeviceRegistryService;
import org.iotbricks.service.device.registry.spi.AlwaysPassingDeviceSchemaValidator;

import io.vertx.core.Vertx;

public class DeviceRegistryBinding {

    private final DeviceRegistryService deviceRegistryService;

    private Vertx vertx;

    private ProtonBindingServer server;

    public DeviceRegistryBinding(final DeviceRegistryService deviceRegistryService) {
        this.deviceRegistryService = deviceRegistryService;
    }

    public void start() {
        this.vertx = Vertx.vertx();

        this.server = ProtonBindingServer.newBinding()
                .binding(new ProtonServiceBinding("device", this::processRequest))
                .serializer(JacksonSerializer.json())
                .build(vertx);

    }

    public void stop() {
        this.server.close();
        vertx.close();
    }

    private Object processRequest(final ProtonRequestContext context) throws Exception {

        final Optional<String> verb = context.getVerb();

        if (!verb.isPresent()) {
            throw new RequestException(DECODE_ERROR, "Verb missing");
        }

        switch (verb.get()) {
        case "create": {
            Device device = context.decodeRequest(Device.class);
            return deviceRegistryService.create(device);
        }
        case "save": {
            Device device = context.decodeRequest(Device.class);
            return deviceRegistryService.save(device);
        }
        case "update": {
            Device device = context.decodeRequest(Device.class);
            deviceRegistryService.update(device);
            return null;
        }
        case "findById": {
            String deviceId = context.decodeRequest(String.class);
            return deviceRegistryService.findById(deviceId);
        }
        default:
            throw new RequestException(DECODE_ERROR, String.format("Unsupported verb: %s", verb));
        }
    }

    public static void main(String[] args) {
        new DeviceRegistryBinding(new InMemoryDeviceRegistryService(new AlwaysPassingDeviceSchemaValidator())).start();
    }

}
