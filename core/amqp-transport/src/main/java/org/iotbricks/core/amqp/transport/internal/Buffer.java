package org.iotbricks.core.amqp.transport.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.iotbricks.core.amqp.transport.RequestInstance;
import org.iotbricks.core.amqp.transport.proton.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.proton.ProtonSender;

/**
 * A buffer for {@link RequestInstance}s.
 */
public class Buffer<RQ extends Request> {

    private static final Logger logger = LoggerFactory.getLogger(Buffer.class);

    private Set<RequestInstance<?, RQ>> requests = new LinkedHashSet<>();

    private final int limit;
    private final Function<RQ, String> addressProvider;
    private final AmqpTransportContext<RQ> context;

    public Buffer(final int limit, final Function<RQ, String> addressProvider, final AmqpTransportContext<RQ> context) {
        this.limit = limit <= 0 ? Integer.MAX_VALUE : limit;
        this.addressProvider = addressProvider;
        this.context = context;
    }

    private String address(final RequestInstance<?, RQ> request) {
        return this.addressProvider.apply(request.getRequest());
    }

    public void append(final RequestInstance<?, RQ> request) {

        final String address = address(request);

        final ProtonSender sender = this.context.requestSender(address);

        if (sender != null) {
            logger.debug("Sender is available - {} -> {}", address, sender);
            this.context.sendRequest(sender, request);
            return;
        }

        logger.debug("Waiting for sender: {}", address);

        if (this.requests.size() < this.limit) {
            this.requests.add(request);
        } else {
            request.fail("Local send buffer is full");
        }
    }

    public void remove(final RequestInstance<?, RQ> request) {
        this.requests.remove(request);
    }

    public RequestInstance<?, RQ> poll(final String address) {
        final Iterator<RequestInstance<?, RQ>> i = this.requests.iterator();
        while (i.hasNext()) {
            final RequestInstance<?, RQ> request = i.next();
            if (address.equals(address(request))) {
                i.remove();
                return request;
            }
        }

        return null;
    }

    public void flush(final String address, final Consumer<RequestInstance<?, RQ>> consumer) {
        if (address == null) {
            flush(consumer);
        }

        // FIXME: this needs to be improved

        final List<RequestInstance<?, RQ>> result = new ArrayList<>();

        final Iterator<RequestInstance<?, RQ>> i = this.requests.iterator();
        while (i.hasNext()) {
            final RequestInstance<?, RQ> request = i.next();
            if (address.equals(address(request))) {
                result.add(request);
                i.remove();
            }
        }

        result.forEach(consumer);
    }

    public void flush(final Consumer<RequestInstance<?, RQ>> consumer) {
        final Set<RequestInstance<?, RQ>> requests = this.requests;
        this.requests = new LinkedHashSet<>();
        requests.forEach(consumer);
    }

    public Set<String> getAddresses() {
        // FIXME: this needs to be improved
        return this.requests
                .stream()
                .map(this::address)
                .collect(Collectors.toSet());
    }
}