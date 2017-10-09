@Client(DeviceRegistryService.class)
@AmqpTransport("device")
@LocalTransport
package org.iotbricks.client.device.registry;

import org.iotbricks.annotations.AmqpTransport;
import org.iotbricks.annotations.Client;
import org.iotbricks.annotations.LocalTransport;
import org.iotbricks.service.device.registry.api.DeviceRegistryService;
