package org.iotbricks.core.binding.common;

import org.iotbricks.core.utils.binding.ErrorCondition;
import org.iotbricks.core.utils.binding.ErrorResult;
import org.iotbricks.core.utils.binding.ErrorTranslator;
import org.iotbricks.core.utils.binding.RequestException;

import io.glutamate.lang.Exceptions;

public class DefaultErrorTranslator implements ErrorTranslator {

    @Override
    public ErrorResult translate(final Throwable error) {
        final Throwable cause = Exceptions.getCause(error);
        return new ErrorResult(translateCondition(cause), Exceptions.getMessage(cause));
    }

    protected ErrorCondition translateCondition(Throwable error) {
        if (error instanceof RequestException) {
            final ErrorCondition result = ((RequestException) error).getCondition();
            return result != null ? result : ErrorCondition.INTERNAL_ERROR;
        }

        if (error instanceof IllegalArgumentException) {
            return ErrorCondition.ILLEGAL_STATE;
        }

        if (error instanceof UnsupportedOperationException) {
            return ErrorCondition.NOT_IMPLEMENTED;
        }

        return ErrorCondition.INTERNAL_ERROR;
    }

}
