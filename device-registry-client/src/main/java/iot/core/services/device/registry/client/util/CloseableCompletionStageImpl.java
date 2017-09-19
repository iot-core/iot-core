package iot.core.services.device.registry.client.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

final class CloseableCompletionStageImpl<T> implements CloseableCompletionStage<T> {

    private final CompletionStage<T> stage;
    private final AutoCloseable closeHandler;

    public CloseableCompletionStageImpl(final CompletionStage<T> stage, final AutoCloseable closeHandler) {
        this.stage = stage;
        this.closeHandler = closeHandler;
    }

    @Override
    public void close() throws Exception {
        this.closeHandler.close();
    }

    @Override
    public <U> CompletionStage<U> thenApply(final Function<? super T, ? extends U> fn) {
        return this.stage.thenApply(fn);
    }

    @Override
    public <U> CompletionStage<U> thenApplyAsync(final Function<? super T, ? extends U> fn) {
        return this.stage.thenApplyAsync(fn);
    }

    @Override
    public <U> CompletionStage<U> thenApplyAsync(final Function<? super T, ? extends U> fn, final Executor executor) {
        return this.stage.thenApplyAsync(fn, executor);
    }

    @Override
    public CompletionStage<Void> thenAccept(final Consumer<? super T> action) {
        return this.stage.thenAccept(action);
    }

    @Override
    public CompletionStage<Void> thenAcceptAsync(final Consumer<? super T> action) {
        return this.stage.thenAcceptAsync(action);
    }

    @Override
    public CompletionStage<Void> thenAcceptAsync(final Consumer<? super T> action, final Executor executor) {
        return this.stage.thenAcceptAsync(action, executor);
    }

    @Override
    public CompletionStage<Void> thenRun(final Runnable action) {
        return this.stage.thenRun(action);
    }

    @Override
    public CompletionStage<Void> thenRunAsync(final Runnable action) {
        return this.stage.thenRunAsync(action);
    }

    @Override
    public CompletionStage<Void> thenRunAsync(final Runnable action, final Executor executor) {
        return this.stage.thenRunAsync(action, executor);
    }

    @Override
    public <U, V> CompletionStage<V> thenCombine(final CompletionStage<? extends U> other,
            final BiFunction<? super T, ? super U, ? extends V> fn) {
        return this.stage.thenCombine(other, fn);
    }

    @Override
    public <U, V> CompletionStage<V> thenCombineAsync(final CompletionStage<? extends U> other,
            final BiFunction<? super T, ? super U, ? extends V> fn) {
        return this.stage.thenCombineAsync(other, fn);
    }

    @Override
    public <U, V> CompletionStage<V> thenCombineAsync(final CompletionStage<? extends U> other,
            final BiFunction<? super T, ? super U, ? extends V> fn, final Executor executor) {
        return this.stage.thenCombineAsync(other, fn, executor);
    }

    @Override
    public <U> CompletionStage<Void> thenAcceptBoth(final CompletionStage<? extends U> other,
            final BiConsumer<? super T, ? super U> action) {
        return this.stage.thenAcceptBoth(other, action);
    }

    @Override
    public <U> CompletionStage<Void> thenAcceptBothAsync(final CompletionStage<? extends U> other,
            final BiConsumer<? super T, ? super U> action) {
        return this.stage.thenAcceptBothAsync(other, action);
    }

    @Override
    public <U> CompletionStage<Void> thenAcceptBothAsync(final CompletionStage<? extends U> other,
            final BiConsumer<? super T, ? super U> action, final Executor executor) {
        return this.stage.thenAcceptBothAsync(other, action, executor);
    }

    @Override
    public CompletionStage<Void> runAfterBoth(final CompletionStage<?> other, final Runnable action) {
        return this.stage.runAfterBoth(other, action);
    }

    @Override
    public CompletionStage<Void> runAfterBothAsync(final CompletionStage<?> other, final Runnable action) {
        return this.stage.runAfterBothAsync(other, action);
    }

    @Override
    public CompletionStage<Void> runAfterBothAsync(final CompletionStage<?> other, final Runnable action,
            final Executor executor) {
        return this.stage.runAfterBothAsync(other, action, executor);
    }

    @Override
    public <U> CompletionStage<U> applyToEither(final CompletionStage<? extends T> other,
            final Function<? super T, U> fn) {
        return this.stage.applyToEither(other, fn);
    }

    @Override
    public <U> CompletionStage<U> applyToEitherAsync(final CompletionStage<? extends T> other,
            final Function<? super T, U> fn) {
        return this.stage.applyToEitherAsync(other, fn);
    }

    @Override
    public <U> CompletionStage<U> applyToEitherAsync(final CompletionStage<? extends T> other,
            final Function<? super T, U> fn,
            final Executor executor) {
        return this.stage.applyToEitherAsync(other, fn, executor);
    }

    @Override
    public CompletionStage<Void> acceptEither(final CompletionStage<? extends T> other,
            final Consumer<? super T> action) {
        return this.stage.acceptEither(other, action);
    }

    @Override
    public CompletionStage<Void> acceptEitherAsync(final CompletionStage<? extends T> other,
            final Consumer<? super T> action) {
        return this.stage.acceptEitherAsync(other, action);
    }

    @Override
    public CompletionStage<Void> acceptEitherAsync(final CompletionStage<? extends T> other,
            final Consumer<? super T> action,
            final Executor executor) {
        return this.stage.acceptEitherAsync(other, action, executor);
    }

    @Override
    public CompletionStage<Void> runAfterEither(final CompletionStage<?> other, final Runnable action) {
        return this.stage.runAfterEither(other, action);
    }

    @Override
    public CompletionStage<Void> runAfterEitherAsync(final CompletionStage<?> other, final Runnable action) {
        return this.stage.runAfterEitherAsync(other, action);
    }

    @Override
    public CompletionStage<Void> runAfterEitherAsync(final CompletionStage<?> other, final Runnable action,
            final Executor executor) {
        return this.stage.runAfterEitherAsync(other, action, executor);
    }

    @Override
    public <U> CompletionStage<U> thenCompose(final Function<? super T, ? extends CompletionStage<U>> fn) {
        return this.stage.thenCompose(fn);
    }

    @Override
    public <U> CompletionStage<U> thenComposeAsync(final Function<? super T, ? extends CompletionStage<U>> fn) {
        return this.stage.thenComposeAsync(fn);
    }

    @Override
    public <U> CompletionStage<U> thenComposeAsync(final Function<? super T, ? extends CompletionStage<U>> fn,
            final Executor executor) {
        return this.stage.thenComposeAsync(fn, executor);
    }

    @Override
    public CompletionStage<T> exceptionally(final Function<Throwable, ? extends T> fn) {
        return this.stage.exceptionally(fn);
    }

    @Override
    public CompletionStage<T> whenComplete(final BiConsumer<? super T, ? super Throwable> action) {
        return this.stage.whenComplete(action);
    }

    @Override
    public CompletionStage<T> whenCompleteAsync(final BiConsumer<? super T, ? super Throwable> action) {
        return this.stage.whenCompleteAsync(action);
    }

    @Override
    public CompletionStage<T> whenCompleteAsync(final BiConsumer<? super T, ? super Throwable> action,
            final Executor executor) {
        return this.stage.whenCompleteAsync(action, executor);
    }

    @Override
    public <U> CompletionStage<U> handle(final BiFunction<? super T, Throwable, ? extends U> fn) {
        return this.stage.handle(fn);
    }

    @Override
    public <U> CompletionStage<U> handleAsync(final BiFunction<? super T, Throwable, ? extends U> fn) {
        return this.stage.handleAsync(fn);
    }

    @Override
    public <U> CompletionStage<U> handleAsync(final BiFunction<? super T, Throwable, ? extends U> fn,
            final Executor executor) {
        return this.stage.handleAsync(fn, executor);
    }

    @Override
    public CompletableFuture<T> toCompletableFuture() {
        return this.stage.toCompletableFuture();
    }

}
