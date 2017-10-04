package org.iotbricks.core.utils.client;

import java.time.Duration;

import io.glutamate.util.concurrent.Await;
import io.glutamate.util.concurrent.CloseableCompletionStage;

public class AbstractSyncWrapper {

    private Duration timeout;

    public AbstractSyncWrapper(final Duration timeout) {
        this.timeout = timeout;
    }

    protected <T> T await(final CloseableCompletionStage<T> stage) {
        try {
            return Await.await(stage, this.timeout);
        } finally {
            try {
                stage.close();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}