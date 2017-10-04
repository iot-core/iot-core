package org.iotbricks.core.utils.address;

public interface AddressProvider {

    String requestAddress(String service);

    String replyAddress(String service, String replyTo);

}
