package iot.core.services.device.registry.client.util;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class CloseableCompletableFutureTest {

    @Test
    public void test1() throws Exception {
        final AtomicInteger triggered = new AtomicInteger();

        final CloseableCompletableFuture<?> future = new CloseableCompletableFuture<>();
        future.whenClosed(triggered::incrementAndGet);
        future.close();

        assertEquals(1, triggered.get());
    }

    @Test
    public void test2() throws Exception {
        final AtomicInteger triggered = new AtomicInteger();

        final CloseableCompletableFuture<?> future = new CloseableCompletableFuture<>();
        future.whenClosed(triggered::incrementAndGet);
        future.close();
        future.whenClosed(triggered::incrementAndGet);

        assertEquals(2, triggered.get());
    }

    @Test
    public void testDoubleClose1() throws Exception {
        final AtomicInteger triggered = new AtomicInteger();

        final CloseableCompletableFuture<?> future = new CloseableCompletableFuture<>();
        future.whenClosed(triggered::incrementAndGet);

        future.close();
        future.close();

        assertEquals(1, triggered.get());
    }
}
