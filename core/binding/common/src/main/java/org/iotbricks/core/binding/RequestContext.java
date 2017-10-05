package org.iotbricks.core.binding;

import java.util.Optional;

public interface RequestContext {
    public Optional<String> getVerb();

    public Object[] decodeRequest(Class<?>[] parameterTypes);
}
