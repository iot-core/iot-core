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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot configuration for the Device Registry application.
 */
@Configuration
public class CoreAmqpBasedConfig {

    /**
     * Gets properties for configuring
     * {@code CoreAmqpBasedRegistrationConfigProperties} which implements the
     * <em>Device Registration</em> API.
     *
     * @return The properties.
     */
    @Bean
    @ConfigurationProperties(prefix = "core.registry.svc")
    public CoreAmqpBasedRegistrationConfigProperties serviceProperties() {
        return new CoreAmqpBasedRegistrationConfigProperties();
    }

}
