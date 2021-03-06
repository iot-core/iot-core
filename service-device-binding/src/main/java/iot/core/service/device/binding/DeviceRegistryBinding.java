package iot.core.service.device.binding;

import static io.glutamate.lang.Resource.use;
import static org.iotbricks.core.binding.common.NameProvider.serviceName;

import org.iotbricks.core.binding.amqp.AmqpRejectResponseHandler;
import org.iotbricks.core.binding.amqp.AmqpRequestContext;
import org.iotbricks.core.binding.common.BeanServiceBinding;
import org.iotbricks.core.binding.common.MessageResponseHandler;
import org.iotbricks.core.binding.proton.ProtonBindingServer;
import org.iotbricks.core.binding.proton.ProtonErrorMessageResponseHandler;
import org.iotbricks.core.serialization.jackson.JacksonSerializer;
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

        this.server = ProtonBindingServer.newServer()
                .binding(BeanServiceBinding.newBinding(deviceRegistryService)
                        .nameProvider(serviceName())
                        .build())
                .serializer(JacksonSerializer.json())
                .successHandler(new MessageResponseHandler<>(AmqpRequestContext::getReplyToAddress))
                .errorHandler(new AmqpRejectResponseHandler<>()) // Choose this ...
                .errorHandler(new ProtonErrorMessageResponseHandler()) // ... or this
                .build(use(vertx));
    }

    public void stop() {
        this.server.close();
        vertx.close();
    }

    public static void main(String[] args) {
        new DeviceRegistryBinding(new InMemoryDeviceRegistryService(new AlwaysPassingDeviceSchemaValidator())).start();
    }

}
