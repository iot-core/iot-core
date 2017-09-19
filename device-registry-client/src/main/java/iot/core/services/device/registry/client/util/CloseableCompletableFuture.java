package iot.core.services.device.registry.client.util;

import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class CloseableCompletableFuture<T> extends CompletableFuture<T> implements CloseableCompletionStage<T> {

    private static class Entry {
        Entry next;
        Runnable runnable;
        boolean closed;
    }

    private final AtomicReference<Entry> closing = new AtomicReference<>(null);

    @Override
    public CloseableCompletableFuture<T> toCompletableFuture() {
        return this;
    }

    public void whenClosed(final Runnable runnable) {
        Objects.requireNonNull(runnable);

        final Entry next = new Entry();
        next.runnable = runnable;

        do {
            next.next = this.closing.get();
            if (next.next != null && next.next.closed) {
                // handle right now
                runnable.run();
                return;
            }

        } while (!this.closing.compareAndSet(next.next, next));
    }

    @Override
    public void close() throws Exception {
        final Entry entry = new Entry();
        entry.closed = true;

        LinkedList<Throwable> errors = null;
        Entry current = this.closing.getAndSet(entry);
        while (current != null && !current.closed) {

            try {
                current.runnable.run();
            } catch (final Throwable e) {
                if (errors == null) {
                    errors = new LinkedList<>();
                }
                errors.add(e);
            }
            current = current.next;

        }

        if (errors != null) {
            final Throwable e = errors.pollFirst();
            final Exception first;
            if (e instanceof Exception) {
                first = (Exception) e;
            } else {
                first = new Exception();
            }

            errors.forEach(first::addSuppressed);
            throw first;
        }
    }

}
