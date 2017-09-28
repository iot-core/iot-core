package iot.core.utils.binding.amqp;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;

import iot.core.utils.binding.ErrorCondition;

public final class DefaultAmqpErrorConditionTranslator implements AmqpErrorConditionTranslator {

    static final String INTERNAL_ERROR = "amqp:internal-error";

    private static final AmqpErrorConditionTranslator INSTANCE = new DefaultAmqpErrorConditionTranslator();

    private final BiMap<ErrorCondition, String> map = createMap();

    private static BiMap<ErrorCondition, String> createMap() {
        final BiMap<ErrorCondition, String> result = HashBiMap.create();

        result.put(ErrorCondition.DECODE_ERROR, "amqp:decode-error");
        result.put(ErrorCondition.ILLEGAL_STATE, "amqp:illegal-state");
        result.put(ErrorCondition.INTERNAL_ERROR, INTERNAL_ERROR);
        result.put(ErrorCondition.NOT_FOUND, "amqp:not-found");
        result.put(ErrorCondition.NOT_IMPLEMENTED, "amqp:not-implemented");

        return ImmutableBiMap.copyOf(result);
    }

    private DefaultAmqpErrorConditionTranslator() {
    }

    public static AmqpErrorConditionTranslator instance() {
        return INSTANCE;
    }

    public static String toDefaultAmqp(final ErrorCondition error) {
        return INSTANCE.toAmqp(error);
    }

    public static ErrorCondition fromDefaultAmqp(final String error) {
        return INSTANCE.fromAmqp(error);
    }

    @Override
    public ErrorCondition fromAmqp(final String errorCondition) {
        return this.map.inverse().getOrDefault(errorCondition, ErrorCondition.INTERNAL_ERROR);
    }

    @Override
    public String toAmqp(final ErrorCondition errorCondition) {
        return this.map.getOrDefault(errorCondition, INTERNAL_ERROR);
    }

}
