package iot.core.utils.address;

public class DefaultAddressProvider implements AddressProvider {

    private static final String ADDRESS_PATH_DELIMITER = ".";

    @Override
    public String requestAddress(final String service) {
        return service;
    }

    @Override
    public String replyAddress(final String service, final String replyTo) {
        return String.join(ADDRESS_PATH_DELIMITER, service, "reply", replyTo);
    }

}
