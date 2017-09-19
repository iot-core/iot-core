package iot.core.services.device.registry.client.util;

import java.util.Objects;
import java.util.concurrent.CompletionStage;

public interface CloseableCompletionStage<T> extends CompletionStage<T>, AutoCloseable {

    public static <T> CloseableCompletionStage<T> of(final CompletionStage<T> stage, final AutoCloseable closeHandler) {
        Objects.requireNonNull(stage);
        Objects.requireNonNull(closeHandler);

        return new CloseableCompletionStageImpl<>(stage, closeHandler);
    }

    public static <T> CloseableCompletionStage<T> of(final CompletionStage<T> stage) {
        return of(stage, () -> {
        });
    }
}
