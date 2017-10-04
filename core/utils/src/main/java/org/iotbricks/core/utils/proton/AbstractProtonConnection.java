package org.iotbricks.core.utils.proton;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.proton.ProtonClient;
import io.vertx.proton.ProtonConnection;

public abstract class AbstractProtonConnection implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(AbstractProtonConnection.class);

    public abstract static class Builder<T extends AbstractProtonConnection> {

        private String hostname = "localhost";
        private int port = 5672;
        private String username;
        private String password;
        private String container;

        protected Builder() {
        }

        protected Builder(final Builder<T> other) {
            this.hostname = other.hostname;
            this.port = other.port;
            this.username = other.username;
            this.password = other.password;
            this.container = other.container;
        }

        public Builder<T> hostname(final String hostname) {
            this.hostname = hostname;
            return this;
        }

        public String hostname() {
            return this.hostname;
        }

        public Builder<T> port(final int port) {
            this.port = port;
            return this;
        }

        public int port() {
            return this.port;
        }

        public Builder<T> username(final String username) {
            this.username = username;
            return this;
        }

        public String username() {
            return this.username;
        }

        public Builder<T> password(final String password) {
            this.password = password;
            return this;
        }

        public String password() {
            return this.password;
        }

        public Builder<T> container(final String container) {
            this.container = container;
            return this;
        }

        public String container() {
            return this.container;
        }

        public abstract T build(final Vertx vertx);
    }

    private final AtomicBoolean closed = new AtomicBoolean();

    protected final Vertx vertx;
    private final Builder<? extends AbstractProtonConnection> options;

    protected final Context context;

    protected ProtonConnection connection;

    public AbstractProtonConnection(final Vertx vertx, final Builder<? extends AbstractProtonConnection> options) {
        this.vertx = vertx;
        this.options = options;

        this.context = vertx.getOrCreateContext();
    }

    protected void open() {
        this.context.runOnContext(v -> startConnection());
    }

    @Override
    public void close() {
        if (this.closed.compareAndSet(false, true)) {
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

        client.connect(this.options.hostname(), this.options.port(),
                this.options.username(), this.options.password(),
                con -> {

                    logger.debug("Connection -> {}", con);

                    if (con.failed()) {
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

            performEstablished(result);
        }
    }

    protected void performEstablished(final AsyncResult<ProtonConnection> result) {
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
