package org.iotbricks.core.amqp.transport;

import java.time.Duration;
import java.util.Objects;

import org.iotbricks.core.proton.vertx.serializer.AmqpByteSerializer;
import org.iotbricks.core.proton.vertx.serializer.AmqpSerializer;
import org.iotbricks.core.utils.serializer.ByteSerializer;

public abstract class AbstractAmqpClientBuilder<T extends AbstractAmqpClientBuilder<T>> {

    private AmqpTransport.Builder transport;

    private Duration syncTimeout = Duration.ofSeconds(5);

    protected AbstractAmqpClientBuilder(final AmqpTransport.Builder transport) {
        this.transport = transport;
    }

    protected abstract T builder();

    public T transport(final AmqpTransport.Builder transport) {
        this.transport = transport;
        return builder();
    }

    public AmqpTransport.Builder transport() {
        return this.transport;
    }

    public T hostname(final String hostname) {
        this.transport.hostname(hostname);
        return builder();
    }

    public String hostname() {
        return this.transport.hostname();
    }

    public T username(final String username) {
        this.transport.username(username);
        return builder();
    }

    public String username() {
        return this.transport.username();
    }

    public T password(final String password) {
        this.transport.password(password);
        return builder();
    }

    public String password() {
        return this.transport.password();
    }

    public T port(final int port) {
        this.transport.port(port);
        return builder();
    }

    public int port() {
        return this.transport.port();
    }

    public T syncTimeout(final Duration syncTimeout) {
        this.syncTimeout = syncTimeout;
        return builder();
    }

    public Duration syncTimeout() {
        return this.syncTimeout;
    }

    public T serializer(final AmqpSerializer serializer) {
        Objects.requireNonNull(serializer);
        this.transport.serializer(serializer);
        return builder();
    }

    public T serializer(final ByteSerializer serializer) {
        Objects.requireNonNull(serializer);
        this.transport.serializer(AmqpByteSerializer.of(serializer));
        return builder();
    }

    public AmqpSerializer serializer() {
        return this.transport.serializer();
    }

}