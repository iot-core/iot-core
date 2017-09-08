package iot.core.services.device.registry.client;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

import iot.core.services.device.registry.client.internal.AbstractDefaultClient;
import iotcore.service.device.Device;

public class FooBarClient extends AbstractDefaultClient {

    public static class Builder {

        private String endpoint;

        public Builder endpoint ( final String endpoint )   {
            this.endpoint = endpoint;
            return this;
        }

        public String endpoint ()  {
            return this.endpoint;
        }

        public Client build() {
            return new FooBarClient (this.endpoint);
        }
    }

    public static Builder create() {
        return new Builder();
    }

    private FooBarClient(final String endpoint) {
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    protected CompletionStage<Optional<Device>> internalFindById(final String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected CompletionStage<String> internalSave(final Device device) {
        // TODO Auto-generated method stub
        return null;
    }

}
