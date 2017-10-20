package org.iotbricks.core.amqp.transport.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

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
    private final AmqpTransportContext<RQ> context;

    public Buffer(final int limit, final AmqpTransportContext<RQ> context) {
        this.limit = limit <= 0 ? Integer.MAX_VALUE : limit;
        this.context = context;
    }

    public void append(final RequestInstance<?, RQ> request) {

        final String address = request.getAddress();

        final ProtonSender sender = this.context.requestSender(request);

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
            if (address.equals(request.getAddress())) {
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
            if (address.equals(request.getAddress())) {
                result.add(request);
                i.remove();
            }
        }

        result.forEach(consumer);
    }

    /**
     * Flush requests, sending to target address
     *
     * @param address
     */
    public void flush(final String address) {
        logger.debug("Flushing buffer to address: {}", address);

        if (address == null) {
            return;
        }

        final Iterator<RequestInstance<?, RQ>> i = this.requests.iterator();

        while (i.hasNext()) {
            final RequestInstance<?, RQ> request = i.next();

            // TODO: improve access by address

            if (!address.equals(request.getAddress())) {
                continue;
            }

            logger.debug("Try acquiring sender: {}", request);

            final ProtonSender sender = this.context.requestSender(request);
            if (sender == null) {
                logger.debug("Sender not ready");
                return;
            }

            if (sender != null) {
                logger.trace("Sender ready ... sending request");
                i.remove();
                this.context.sendRequest(sender, request);
            }

        }
    }

    public void flush(final Consumer<RequestInstance<?, RQ>> consumer) {
        final Set<RequestInstance<?, RQ>> requests = this.requests;
        this.requests = new LinkedHashSet<>();
        requests.forEach(consumer);
    }

    public Collection<RequestInstance<?, RQ>> getRequests() {
        return Collections.unmodifiableCollection(this.requests);
    }

}