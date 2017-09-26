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

import io.vertx.core.Vertx;
import iot.core.services.device.registry.client.Client;

public interface ClientBuilding {

    public Client createClient(Vertx vertx);

}
