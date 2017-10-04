package org.iotbricks.core.binding.common;

import java.util.Objects;

import org.iotbricks.core.binding.RequestContext;
import org.iotbricks.core.binding.RequestHandler;
import org.iotbricks.core.binding.RequestProcessor;
import org.iotbricks.core.binding.ResponseContext;
import org.iotbricks.core.binding.ResponseHandler;
import org.iotbricks.core.utils.binding.ErrorResult;
import org.iotbricks.core.utils.binding.ErrorTranslator;

public abstract class AbstractRequestProcessor<C1 extends RequestContext, C2 extends ResponseContext>
        implements RequestProcessor<C1, C2> {

    private final ResponseHandler<? super Object, ? super C1, ? super C2> success;
    private final ResponseHandler<? super ErrorResult, ? super C1, ? super C2> error;
    private ErrorTranslator errorTranslator;
    private final RequestHandler<C1> handler;

    public AbstractRequestProcessor(
            final ResponseHandler<? super Object, ? super C1, ? super C2> success,
            final ResponseHandler<? super ErrorResult, ? super C1, ? super C2> error,
            final ErrorTranslator errorTranslator,
            final RequestHandler<C1> handler) {

        Objects.requireNonNull(success);
        Objects.requireNonNull(error);
        Objects.requireNonNull(errorTranslator);
        Objects.requireNonNull(handler);

        this.success = success;
        this.error = error;
        this.errorTranslator = errorTranslator;
        this.handler = handler;
    }

    @Override
    public void process(final C1 request, final C2 response) {

        try {
            final Object result = handler.process(request);
            success.handle(request, response, result);
        } catch (final Exception e) {
            final ErrorResult errorResult = errorTranslator.translate(e);
            error.handle(request, response, errorResult);
        }

    }

}
