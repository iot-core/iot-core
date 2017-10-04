@Client(DeviceRegistryService.class)
@AmqpTransport("device")
package org.iotbricks.client.device.registry;

import org.iotbricks.annotations.AmqpTransport;
import org.iotbricks.annotations.Client;
import org.iotbricks.service.device.registry.api.DeviceRegistryService;
