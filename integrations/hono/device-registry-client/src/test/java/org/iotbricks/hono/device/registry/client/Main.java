package org.iotbricks.hono.device.registry.client;

import static io.glutamate.lang.Resource.manage;
import static io.glutamate.util.concurrent.Await.await;
import static io.vertx.core.Vertx.vertx;
import static java.lang.Integer.parseInt;
import static java.time.Duration.ofSeconds;

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

        final Path certsPath = Paths.get("/home/jreimann/git/hono/example/target/config/hono-demo-certs-jar");

        final PemKeyCertOptions keyCert = new PemKeyCertOptions();
        keyCert.addCertPath(certsPath.resolve("device-registry-cert.pem").toString());
        keyCert.addKeyPath(certsPath.resolve("device-registry-key.pem").toString());

        try (final Client client = AmqpClient.newClient()
                .tenant("foo-bar-baz")
                .transport(transport -> {
                    transport
                            .hostname(args[0])
                            .port(parseInt(args[1]))
                            .port(31671)
                            .username("hono-client@HONO")
                            .password("secret")
                            .container("test-client")
                            .protonClientOptions(options -> {
                                options
                                        .setSsl(true)
                                        .addEnabledSaslMechanism("PLAIN")
                                        .setKeyCertOptions(keyCert)
                                        .setHostnameVerificationAlgorithm("")
                                        .setTrustAll(true);
                            });
                })
                .build(manage(vertx(), Vertx::close))) {

            {
                final DeviceInformation result = await(client.getDevice(id), ofSeconds(15));
                System.out.println("Current device: " + result);
            }

            {
                try (CloseableCompletionStage<DeviceInformation> request = client.registerDevice(id, data)) {
                    final DeviceInformation result = request.toCompletableFuture().get();
                    System.out.println(result);
                }
            }

        }
    }
}
