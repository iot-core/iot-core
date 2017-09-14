package iot.core.services.device.registry.client.util;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

public class AwaitTest {

    @Test
    public void test1() {
        final String result = Await.await(completedFuture("Foo"), 0, null);
        Assert.assertEquals("Foo", result);
    }

    @Test
    public void test2() {
        final String result = Await.await(completedFuture("Foo"), 1, TimeUnit.SECONDS);
        Assert.assertEquals("Foo", result);
    }

    @Test(expected = RuntimeException.class)
    public void test3() {
        Await.await(CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(10_0000);
            } catch (final InterruptedException e) {
            }
        }), 1, TimeUnit.SECONDS);
    }
}
