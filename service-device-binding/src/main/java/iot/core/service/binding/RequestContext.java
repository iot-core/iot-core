package iot.core.service.binding;

import java.util.Optional;

public interface RequestContext {
    public Optional<String> getVerb();

    public <T> T decodeRequest(Class<T> clazz);
}
