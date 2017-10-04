package org.iotbricks.core.utils.address;

import org.iotbricks.core.utils.address.AddressProvider;
import org.iotbricks.core.utils.address.DefaultAddressProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DefaultAddressProviderTest {

    private AddressProvider addressProvider;

    @Before
    public void setup() {
        this.addressProvider = DefaultAddressProvider.instance();
    }

    @Test
    public void test1() {
        Assert.assertEquals("service1", addressProvider.requestAddress("service1"));
    }

    @Test
    public void test2() {
        Assert.assertEquals("service1.reply.1234", addressProvider.replyAddress("service1", "1234"));
    }
}
