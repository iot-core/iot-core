package iot.core.utils.address;

public interface AddressProvider {
    public String requestAddress(String service);

    public String replyAddress(String service, String replyTo);
}
