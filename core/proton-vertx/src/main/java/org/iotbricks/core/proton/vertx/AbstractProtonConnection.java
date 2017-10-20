package org.iotbricks.core.proton.vertx;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.proton.ProtonClient;
import io.vertx.proton.ProtonClientOptions;
import io.vertx.proton.ProtonConnection;

/**
 * An abstract class working with proton connections.
 * <p>
 * This class is intended to help build functionality which requires an
 * established AMQP connection. It takes a set of connection parameters and
 * tried to keep an open AMQP connection, reconnecting when necessary.
 */
public abstract class AbstractProtonConnection implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(AbstractProtonConnection.class);

    public abstract static class Builder<C extends AbstractProtonConnection, B extends AbstractProtonConnection.Builder<C, B>> {

        private String hostname = "localhost";
        private int port;
        private String username;
        private String password;
        private String container;
        private Consumer<ProtonClientOptions> protonClientOptions;

        protected Builder() {
            this.hostname = "localhost";
            this.port = 5672;
        }

        protected Builder(final B other) {
            this.hostname = other.hostname();
            this.port = other.port();
            this.username = other.username();
            this.password = other.password();
            this.container = other.container();
            this.protonClientOptions = other.protonClientOptions();
        }

        protected abstract B builder();

        public B hostname(final String hostname) {
            this.hostname = hostname;
            return builder();
        }

        public String hostname() {
            return this.hostname;
        }

        public B port(final int port) {
            this.port = port;
            return builder();
        }

        public int port() {
            return this.port;
        }

        public B username(final String username) {
            this.username = username;
            return builder();
        }

        public String username() {
            return this.username;
        }

        public B password(final String password) {
            this.password = password;
            return builder();
        }

        public String password() {
            return this.password;
        }

        public B container(final String container) {
            this.container = container;
            return builder();
        }

        public String container() {
            return this.container;
        }

        public B protonClientOptions(final Consumer<ProtonClientOptions> protonClientOptions) {
            this.protonClientOptions = protonClientOptions;
            return builder();
        }

        public Consumer<ProtonClientOptions> protonClientOptions() {
            return this.protonClientOptions;
        }

        public abstract C build(final Vertx vertx);

        public void validate() {
            Objects.requireNonNull(this.hostname, "'hostname' must be set");
        }
    }

    private final AtomicBoolean closed = new AtomicBoolean();

    protected final Vertx vertx;
    private final Builder<? extends AbstractProtonConnection, ?> options;

    protected final Context context;

    protected ProtonConnection connection;

    private final ProtonClientOptions protonOptions;

    public AbstractProtonConnection(final Vertx vertx, final Builder<? extends AbstractProtonConnection, ?> options) {
        this.vertx = vertx;
        this.options = options;

        this.context = vertx.getOrCreateContext();

        this.protonOptions = new ProtonClientOptions();

        if (this.options.protonClientOptions != null) {
            this.options.protonClientOptions.accept(this.protonOptions);
        }

    }

    protected void open() {
        this.context.runOnContext(v -> startConnection());
    }

    @Override
    public void close() {
        if (this.closed.compareAndSet(false, true)) {
            logger.debug("Schedule close");
            this.context.runOnContext(v -> performClose());
        }
    }

    /**
     * Return if the transport is marked closed.
     *
     * @return {@code true} if the transport is marked closed, {@code false}
     *         otherwise
     */
    public boolean isClosed() {
        return this.closed.get();
    }

    private void startConnection() {
        logger.trace("Starting connection...");

        if (isClosed()) {
            logger.debug("Starting connection... abort, we are closed!");
            // we are marked closed
            return;
        }

        createConnection(this::handleConnection);
    }

    protected void createConnection(final Handler<AsyncResult<ProtonConnection>> handler) {

        final ProtonClient client = ProtonClient.create(this.vertx);

        client.connect(this.protonOptions, this.options.hostname(), this.options.port(),
                this.options.username(), this.options.password(),
                con -> {

                    logger.debug("Connection -> {}", con);

                    if (con.failed()) {
                        logger.debug("Connection failed", con.cause());
                        handler.handle(con);
                        return;
                    }

                    con.result()
                            .setContainer(this.options.container())
                            .openHandler(opened -> {

                                logger.debug("Open -> {}", opened);
                                handler.handle(opened);

                            }).open();

                });
    }

    protected void handleConnection(final AsyncResult<ProtonConnection> result) {
        if (result.failed()) {
            if (isClosed()) {
                // we are closed, nothing to do
                return;
            }

            // set up timer for re-connect
            this.vertx.setTimer(1_000, timer -> startConnection());
        } else {
            if (isClosed()) {
                // we got marked closed in the meantime
                result.result().close();
                return;
            }

            this.connection = result.result();
            this.connection.disconnectHandler(this::handleDisconnected);

            performEstablished(result.result());
        }
    }

    protected void performEstablished(final ProtonConnection conection) {
    }

    protected void performClose() {
        if (this.connection != null) {
            this.connection.close();
            this.connection = null;
        }
    }

    protected void handleDisconnected(final ProtonConnection connection) {

        logger.debug("Got disconnected: {}", connection);

        this.connection = null;
        if (!isClosed()) {
            startConnection();
        }
    }

}
