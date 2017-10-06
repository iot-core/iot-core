package org.iotbricks.core.utils.binding;

import com.google.common.base.MoreObjects;

public class ErrorResult {
    private ErrorCondition condition;
    private String message;

    /**
     * Default constructor for data mapping frameworks (e.g. Jackson).
     */
    @SuppressWarnings("unused")
    private ErrorResult() {
    }

    public ErrorResult(final ErrorCondition condition, final String message) {
        this.condition = condition;
        this.message = message;
    }

    public ErrorCondition getCondition() {
        return this.condition;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("condition", this.condition)
                .add("message", this.message)
                .toString();
    }
}
