package org.iotbricks.hono.device.registry.client;

import static java.lang.Integer.parseInt;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.iotbricks.hono.device.registry.client.model.DeviceInformation;

import io.glutamate.util.concurrent.CloseableCompletionStage;
import io.vertx.core.Vertx;
import io.vertx.core.net.PemKeyCertOptions;

public class Main {

    public static void main(final String[] args) throws Exception {

        final String id = "my-id-1";

        final Map<String, Object> data = new HashMap<>();
        data.put("foo", "bar");
        data.put("answer", 42);

        final Vertx vertx = Vertx.vertx();

        final Path certsPath = Paths.get("/home/jreimann/git/hono/example/target/config/hono-demo-certs-jar");

        final PemKeyCertOptions keyCert = new PemKeyCertOptions();
        keyCert.addCertPath(certsPath.resolve("device-registry-cert.pem").toString());
        keyCert.addKeyPath(certsPath.resolve("device-registry-key.pem").toString());

        try (Client client = AmqpClient.newClient()
                .tenant("foo-bar-baz")
                .transport(transport -> {
                    transport
                            .hostname(args[0])
                            .port(parseInt(args[1]))
                            .port(31671)
                            .username("consumer@HONO")
                            .password("verysecret")
                            .protonClientOptions(options -> {
                                options
                                        .addEnabledSaslMechanism("PLAIN")
                                        .setSsl(true)
                                        .setKeyCertOptions(keyCert)
                                        .setSniServerName(args[1])
                                        .setHostnameVerificationAlgorithm("")
                                        .setTrustAll(true);
                            });
                })
                .build(vertx)) {

            try (CloseableCompletionStage<DeviceInformation> request = client.registerDevice(id, data)) {
                final DeviceInformation result = request.toCompletableFuture().get();
                System.out.println(result);
            }
        }
    }
}
