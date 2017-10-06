package org.iotbricks.core.amqp.transport.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.proton.ProtonSender;

/**
 * A buffer for {@link Request}s.
 */
public class Buffer {

    private static final Logger logger = LoggerFactory.getLogger(Buffer.class);

    private Set<Request<?>> requests = new LinkedHashSet<>();

    private final AmqpTransportContext context;
    private final int limit;

    public Buffer(final AmqpTransportContext context, final int limit) {
        this.context = context;
        this.limit = limit <= 0 ? Integer.MAX_VALUE : limit;
    }

    public void append(final Request<?> request) {

        final String service = request.getService();
        final ProtonSender sender = this.context.requestSender(service);

        if (sender != null) {
            logger.debug("Sender is available - {} -> {}", service, sender);
            this.context.sendRequest(sender, request);
            return;
        }

        logger.debug("Waiting for sender: {}", service);

        if (this.requests.size() < this.limit) {
            this.requests.add(request);
        } else {
            request.fail("Local send buffer is full");
        }
    }

    public void remove(final Request<?> request) {
        this.requests.remove(request);
    }

    public Request<?> poll(final String service) {
        final Iterator<Request<?>> i = this.requests.iterator();
        while (i.hasNext()) {
            final Request<?> request = i.next();
            if (service.equals(request.getService())) {
                i.remove();
                return request;
            }
        }

        return null;
    }

    public void flush(final String service, final Consumer<Request<?>> consumer) {
        if (service == null) {
            flush(consumer);
        }

        // FIXME: this needs to be improved

        final List<Request<?>> result = new ArrayList<>();

        final Iterator<Request<?>> i = this.requests.iterator();
        while (i.hasNext()) {
            final Request<?> request = i.next();
            if (service.equals(request.getService())) {
                result.add(request);
                i.remove();
            }
        }

        result.forEach(consumer);
    }

    public void flush(final Consumer<Request<?>> consumer) {
        final Set<Request<?>> requests = this.requests;
        this.requests = new LinkedHashSet<>();
        requests.forEach(consumer);
    }

    public Set<String> getServices() {
        // FIXME: this needs to be improved
        return this.requests
                .stream()
                .map(Request::getService)
                .collect(Collectors.toSet());
    }
}