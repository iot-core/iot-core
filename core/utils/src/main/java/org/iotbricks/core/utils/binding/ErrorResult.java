package org.iotbricks.core.utils.binding;

import com.google.common.base.MoreObjects;

public class ErrorResult {
    private final ErrorCondition condition;
    private final String message;

    public ErrorResult(final ErrorCondition condition, final String message) {
        this.condition = condition;
        this.message = message;
    }

    public ErrorCondition getCondition() {
        return condition;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("condition", this.condition)
                .add("message", this.message)
                .toString();
    }
}
