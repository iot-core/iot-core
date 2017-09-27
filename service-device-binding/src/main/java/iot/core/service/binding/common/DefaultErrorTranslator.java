package iot.core.service.binding.common;

import io.glutamate.lang.Exceptions;
import iot.core.utils.binding.ErrorCondition;
import iot.core.utils.binding.ErrorResult;
import iot.core.utils.binding.ErrorTranslator;
import iot.core.utils.binding.RequestException;

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
