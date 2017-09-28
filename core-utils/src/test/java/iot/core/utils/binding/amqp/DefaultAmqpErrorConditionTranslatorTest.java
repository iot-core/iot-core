package iot.core.utils.binding.amqp;

import org.junit.Assert;
import org.junit.Test;

import iot.core.utils.binding.ErrorCondition;

public class DefaultAmqpErrorConditionTranslatorTest {

    /**
     * Test if all error conditions are mapped
     */
    @Test
    public void testAllMapped() {
        for (final ErrorCondition ec : ErrorCondition.values()) {
            final String result = DefaultAmqpErrorConditionTranslator.toDefaultAmqp(ec);
            if (ec == ErrorCondition.INTERNAL_ERROR) {
                Assert.assertEquals(DefaultAmqpErrorConditionTranslator.INTERNAL_ERROR, result);
            } else {
                Assert.assertNotEquals(DefaultAmqpErrorConditionTranslator.INTERNAL_ERROR, result);
            }
        }
    }
}
