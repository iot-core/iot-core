package org.iotbricks.annotations.processor;

import org.iotbricks.annotations.processor.ServiceMethod.TypeName;
import org.junit.Assert;
import org.junit.Test;

public class ServiceMethodTest {

    @Test
    public void test1() {
        assertTypeName("foo.Bar", "foo", "Bar");
        assertTypeName("foo.baz.Bar", "foo.baz", "Bar");
        assertTypeName("Bar", "", "Bar");
    }

    private static void assertTypeName(final String typeName, final String expectedPackageName,
            final String expectedSimpleName) {
        final TypeName type = new ServiceMethod.TypeName(typeName, null);

        Assert.assertEquals(expectedSimpleName, type.getSimpleName());
        Assert.assertEquals(expectedPackageName, type.getPackageName());
    }
}
