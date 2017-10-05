package iot.core.service.device.binding;

import static org.iotbricks.core.binding.common.NameProvider.serviceName;

import org.iotbricks.common.device.registry.serialization.jackson.JacksonSerializer;
import org.iotbricks.core.binding.common.BeanServiceBinding;
import org.iotbricks.core.binding.proton.ProtonBindingServer;
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
                .binding(BeanServiceBinding.newBinding(deviceRegistryService)
                        .nameProvider(serviceName())
                        .build())
                .serializer(JacksonSerializer.json())
                .build(vertx);

    }

    public void stop() {
        this.server.close();
        vertx.close();
    }

    public static void main(String[] args) {
        new DeviceRegistryBinding(new InMemoryDeviceRegistryService(new AlwaysPassingDeviceSchemaValidator())).start();
    }

}
