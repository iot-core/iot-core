package org.iotbricks.core.amqp.transport.client;

import java.util.function.Function;

import org.iotbricks.core.amqp.transport.proton.ProtonTransport;

import io.glutamate.util.concurrent.CloseableCompletionStage;

public class ServiceRequestBuilder<R>
        extends ProtonTransport.RequestBuilderImpl<R, ServiceRequestBuilder<R>, ServiceRequestInformation> {

    private String service;

    public ServiceRequestBuilder(
            final Function<ServiceRequestBuilder<R>, CloseableCompletionStage<R>> executor) {
        super(executor);
    }

    @Override
    public ServiceRequestBuilder<R> builder() {
        return this;
    }

    public ServiceRequestBuilder<R> service(final String service) {
        this.service = service;
        return builder();
    }

    @Override
    public ServiceRequestInformation buildInformation() {
        return new ServiceRequestInformation(this.service);
    }

}