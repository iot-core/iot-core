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

import org.eclipse.hono.config.SignatureSupportingConfigProperties;
import org.eclipse.hono.deviceregistry.SignatureSupporting;

import io.vertx.core.Vertx;
import iot.core.services.device.registry.client.AmqpClient;
import iot.core.services.device.registry.client.Client;

public final class CoreBasedRegistrationConfigProperties implements SignatureSupporting, ClientBuilding {

    private final SignatureSupportingConfigProperties registrationAssertionProperties = new SignatureSupportingConfigProperties();

    private String hostname = "localhost";

    private int port = 5672;

    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    @Override
    public SignatureSupportingConfigProperties getSigning() {
        return this.registrationAssertionProperties;
    }

    @Override
    public Client createClient(final Vertx vertx) {
        return AmqpClient.create()
                .hostname(this.hostname)
                .port(this.port)
                .build(vertx);
    }

}
