package iot.core.services.device.registry.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class Await {
    private Await() {
    }

    public static <T> T await(final CompletionStage<T> stage, final long timeout, final TimeUnit timeUnit) {

        final CompletableFuture<T> future = stage.toCompletableFuture();

        try {

            if (timeout > 0) {
                return future.get(timeout, timeUnit);
            } else {
                return future.get();
            }

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        } finally {
            // finally cancel, doesn't matter if we are already complete
            future.cancel(false);
        }
    }

}
