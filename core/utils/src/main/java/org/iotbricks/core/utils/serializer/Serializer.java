package org.iotbricks.core.utils.serializer;

public interface Serializer {

    /**
     * Get the content type as per RFC-2046 (MIME type)
     *
     * @return the content type
     */
    public String getContentType();

}
