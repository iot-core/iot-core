package org.iotbricks.core.binding.common;

import static org.iotbricks.core.utils.binding.ErrorCondition.DECODE_ERROR;
import static org.iotbricks.core.utils.binding.ErrorCondition.NOT_IMPLEMENTED;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.iotbricks.core.binding.RequestContext;
import org.iotbricks.core.binding.RequestHandler;
import org.iotbricks.core.utils.binding.RequestException;

public class SimpleVerbRequestHandler implements RequestHandler<RequestContext> {

    @FunctionalInterface
    public interface VerbHandler {
        public Object process(RequestContext context) throws Exception;
    }

    private final Map<String, VerbHandler> verbMap;

    public SimpleVerbRequestHandler(final Map<String, VerbHandler> verbMap) {
        Objects.requireNonNull(verbMap);
        this.verbMap = new HashMap<>(verbMap);
    }

    @Override
    public Object process(final RequestContext context) throws Exception {

        final Optional<String> verb = context.getVerb();

        if (!verb.isPresent()) {
            verbNotPresent(context);
            return null;
        }

        final VerbHandler handler = this.verbMap.get(verb.get());

        if (handler == null) {
            noHandler(context);
            return null;
        }

        return handler.process(context);
    }

    private void noHandler(final RequestContext context) {
        throw new RequestException(NOT_IMPLEMENTED, "Verb unknown");
    }

    protected void verbNotPresent(final RequestContext context) {
        throw new RequestException(DECODE_ERROR, "Verb missing");
    }

}
