/**
 * Copyright (c) 2017 Red Hat Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc - initial creation
 */

package iot.core.hono.device.registry;

import org.iotbricks.service.device.registry.api.DeviceRegistryService;
import org.iotbricks.service.device.registry.inmemory.InMemoryDeviceRegistryService;
import org.iotbricks.service.device.registry.spi.AlwaysPassingDeviceSchemaValidator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Spring Boot configuration for the Device Registry application.
 */
@Configuration
@Profile("local")
public class CoreLocalBasedConfig {

    /**
     * Gets properties for configuring
     * {@code CoreAmqpBasedRegistrationConfigProperties} which implements the
     * <em>Device Registration</em> API.
     *
     * @return The properties.
     */
    @Bean
    @ConfigurationProperties(prefix = "core.registry.svc")
    public CoreLocalBasedRegistrationConfigProperties serviceProperties() {
        return new CoreLocalBasedRegistrationConfigProperties();
    }

    @Bean
    public DeviceRegistryService deviceRegistry() {
        return new InMemoryDeviceRegistryService(new AlwaysPassingDeviceSchemaValidator());
    }

}
