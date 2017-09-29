@Client(DeviceRegistryService.class)
@AmqpTransport("device")
package iot.core.services.device.registry.client;

import org.iotbricks.annotations.AmqpTransport;
import org.iotbricks.annotations.Client;
import org.iotbricks.service.device.registry.api.DeviceRegistryService;
