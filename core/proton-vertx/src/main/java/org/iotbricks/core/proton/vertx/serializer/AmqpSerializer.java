package org.iotbricks.core.proton.vertx.serializer;

import org.apache.qpid.proton.amqp.messaging.Section;

/**
 * Serialize an object into an AMQP section.
 */
public interface AmqpSerializer {
    public Section encode(Object object);

    public Section encode(Object... objects);

    public <T> T decode(Section section, Class<T> clazz);

    public Object[] decode(Section section, Class<?>... clazzes);

    /**
     * Get the content type as per RFC-2046 (MIME type)
     * 
     * @return the content type
     */
    public String getContentType();
}
