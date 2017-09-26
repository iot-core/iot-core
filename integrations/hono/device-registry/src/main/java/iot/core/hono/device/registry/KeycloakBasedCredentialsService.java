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
import static org.eclipse.hono.util.CredentialsResult.from;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;

import org.eclipse.hono.service.credentials.BaseCredentialsService;
import org.eclipse.hono.util.CredentialsConstants;
import org.eclipse.hono.util.CredentialsResult;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * A credentials service that keeps all data in memory but is backed by a file.
 * <p>
 * On startup this adapter loads all added credentials from a file. On shutdown
 * all credentials kept in memory are written to the file.
 */
@Repository
public final class KeycloakBasedCredentialsService
        extends BaseCredentialsService<KeycloakBasedCredentialsConfigProperties> {

    private Keycloak keycloak;

    @Autowired
    @Override
    public void setConfig(final KeycloakBasedCredentialsConfigProperties configuration) {
        setSpecificConfig(configuration);
    }

    @Override
    protected void doStart(final Future<Void> startFuture) throws Exception {
        this.vertx.executeBlocking(v -> {

            try {
                this.keycloak = KeycloakBuilder.builder()
                        .clientId(getConfig().getClientId())
                        .clientSecret(getConfig().getClientSecret())
                        .password(getConfig().getPassword())
                        .realm(getConfig().getRealm())
                        .serverUrl(getConfig().getServerUrl())
                        .username(getConfig().getUsername())
                        .build();

                v.complete();
            } catch (final Exception e) {
                v.fail(e);
            }

        }, true, startFuture.completer());
    }

    @Override
    protected void doStop(final Future<Void> stopFuture) {
        this.vertx.executeBlocking(v -> {

            try {
                this.keycloak.close();
            } finally {
                v.complete();
            }

        }, true, stopFuture.completer());
    }

    @Override
    public void addCredentials(final String tenantId, final JsonObject credentialsObject,
            final Handler<AsyncResult<CredentialsResult>> resultHandler) {
        resultHandler.handle(Future.failedFuture("Unsupported operation"));
    }

    @Override
    public void updateCredentials(final String tenantId, final JsonObject credentialsObject,
            final Handler<AsyncResult<CredentialsResult>> resultHandler) {
        resultHandler.handle(Future.failedFuture("Unsupported operation"));
    }

    @Override
    public void removeCredentials(final String tenantId, final String deviceId, final String type, final String authId,
            final Handler<AsyncResult<CredentialsResult>> resultHandler) {
        resultHandler.handle(Future.failedFuture("Unsupported operation"));
    }

    @Override
    public void getCredentials(final String tenantId, final String type, final String authId,
            final Handler<AsyncResult<CredentialsResult>> resultHandler) {
        this.vertx.executeBlocking(v -> {

            v.complete(getCredentials(tenantId, type, authId));

        }, true, resultHandler);
    }

    private CredentialsResult getCredentials(final String realm, final String type, final String authId) {

        if (!CredentialsConstants.SECRETS_TYPE_HASHED_PASSWORD.equals(type)) {
            return from(HTTP_NOT_FOUND, null);
        }

        try {
            final UserRepresentation user = this.keycloak
                    .realm(realm)
                    .users()
                    .get(authId)
                    .toRepresentation();

            if (user == null) {
                return CredentialsResult.from(HTTP_NOT_FOUND, null);
            }

            final List<JsonObject> secrets = user
                    .getCredentials()
                    .stream()
                    .filter(cred -> type.equals(cred.getType()))
                    .map(this::mapCredentials)
                    .collect(Collectors.toList());

            final String deviceId = user.getAttributes().get("deviceId").stream().findAny().orElse(null);
            if (deviceId == null) {
                return CredentialsResult.from(HTTP_NOT_FOUND, null);
            }

            return CredentialsResult.from(HTTP_OK,
                    getResultPayload(deviceId, CredentialsConstants.SECRETS_TYPE_HASHED_PASSWORD, authId,
                            user.isEnabled(),
                            new JsonArray(secrets)));
        } catch (final ProcessingException e) {
            if (e.getCause() instanceof NotFoundException) {
                return CredentialsResult.from(HTTP_NOT_FOUND);
            }
            throw e;
        }
    }

    private JsonObject mapCredentials(final CredentialRepresentation credential) {
        return new JsonObject()
                .put(CredentialsConstants.FIELD_SECRETS_PWD_HASH, credential.getValue());
    }

}
