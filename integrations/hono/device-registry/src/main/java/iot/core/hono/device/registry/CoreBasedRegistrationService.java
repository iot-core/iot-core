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

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.eclipse.hono.util.RegistrationConstants.FIELD_DATA;
import static org.eclipse.hono.util.RegistrationResult.from;
import static org.eclipse.hono.util.RequestResponseApiConstants.FIELD_DEVICE_ID;

import java.net.HttpURLConnection;
import java.util.Date;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.eclipse.hono.service.registration.BaseRegistrationService;
import org.eclipse.hono.util.RegistrationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import iot.core.service.device.Device;
import iot.core.services.device.registry.client.Client;

@Component
public class CoreBasedRegistrationService extends BaseRegistrationService<ClientBuilding> {

    private Client client;

    @Autowired
    @Override
    public void setConfig(final ClientBuilding configuration) {
        setSpecificConfig(configuration);
    }

    @Override
    protected void doStart(final Future<Void> startFuture) {
        try {
            this.client = getConfig().createClient(this.vertx);
            startFuture.complete();
        } catch (final Exception e) {
            startFuture.fail(e);
        }
    }

    @Override
    protected void doStop(final Future<Void> stopFuture) {
        try {
            this.client.close();
            stopFuture.complete();
        } catch (final Exception e) {
            stopFuture.fail(e);
        }
    }

    private String makeId(final String tenantId, final String deviceId) {
        return String.format("%s/%s", tenantId, deviceId);
    }

    private String deviceIdFrom(final String id) {
        if (id == null) {
            return id;
        }

        final String toks[] = id.split("/", 2);
        if (toks.length < 2) {
            return null;
        }

        return toks[1];
    }

    @Override
    public void getDevice(final String tenantId, final String deviceId,
            final Handler<AsyncResult<RegistrationResult>> resultHandler) {

        this.client
                .async()
                .findById(makeId(tenantId, deviceId))
                .thenApply(this::getDeviceResult)
                .whenComplete(completer(resultHandler));

    }

    private RegistrationResult getDeviceResult(final Optional<Device> result) {
        return result
                .map(device -> from(HTTP_OK, toResult(device)))
                .orElse(from(HTTP_NOT_FOUND));
    }

    private JsonObject toResult(final Device device) {
        return new JsonObject()
                .put(FIELD_DEVICE_ID, deviceIdFrom(device.getDeviceId()))
                .put(FIELD_DATA, device.getProperties());
    }

    @Override
    public void addDevice(final String tenantId, final String deviceId, final JsonObject otherKeys,
            final Handler<AsyncResult<RegistrationResult>> resultHandler) {

        this.client
                .async()
                .create(new Device(makeId(tenantId, deviceId), new Date(), new Date(), "hono", otherKeys.getMap()))
                .handle((result, error) -> {
                    if (error == null) {
                        return from(HttpURLConnection.HTTP_CREATED);
                    } else {
                        return from(HttpURLConnection.HTTP_INTERNAL_ERROR);
                    }
                }).whenComplete(completer(resultHandler));

    }

    @Override
    public void updateDevice(final String tenantId, final String deviceId, final JsonObject otherKeys,
            final Handler<AsyncResult<RegistrationResult>> resultHandler) {

    }

    @Override
    public void removeDevice(final String tenantId, final String deviceId,
            final Handler<AsyncResult<RegistrationResult>> resultHandler) {

    }

    private static <T, U extends Throwable> BiConsumer<T, U> completer(
            final Handler<AsyncResult<T>> handler) {

        return (result, error) -> {
            if (error == null) {
                handler.handle(Future.succeededFuture(result));
            } else {
                handler.handle(Future.failedFuture(error));
            }
        };

    }

}
