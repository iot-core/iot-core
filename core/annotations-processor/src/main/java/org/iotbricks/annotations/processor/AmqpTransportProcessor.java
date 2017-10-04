package org.iotbricks.annotations.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

import org.iotbricks.annotations.AmqpTransport;

@SupportedAnnotationTypes("org.iotbricks.annotations.AmqpTransport")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AmqpTransportProcessor extends AbstractClientProcessor {

    public AmqpTransportProcessor() {
        super(AmqpTransport.class);
    }

    @Override
    protected void processClient(final PackageElement packageElement, final TypeElement serviceType,
            final RoundEnvironment roundEnv)
            throws Exception {

        createAmqpTransport(packageElement, serviceType);

    }

    private void createAmqpTransport(final PackageElement element,
            final TypeElement serviceType) throws IOException {

        final AmqpTransport transport = element.getAnnotation(AmqpTransport.class);

        String serviceTopic = transport.value();
        if (serviceTopic == null || serviceTopic.isEmpty()) {
            serviceTopic = serviceType.getQualifiedName().toString();
        }

        final String name = element.toString() + ".AmqpClient";

        final JavaFileObject file = this.filer.createSourceFile(name, element);

        try (final PrintWriter out = new PrintWriter(file.openWriter())) {

            out.format("package %s;%n%n", element);

            out.println("import static java.util.Objects.requireNonNull;");
            out.println();
            out.println("import java.time.Duration;");
            out.println();
            out.println("import io.glutamate.util.concurrent.CloseableCompletionStage;");
            out.println("import io.vertx.core.Vertx;");
            out.println();
            out.println("import iot.core.amqp.transport.AmqpTransport;");
            out.println("import iot.core.services.device.registry.client.internal.AbstractAmqpClientBuilder;");
            out.println();

            out.println("public class AmqpClient extends AbstractDefaultClient {");
            out.println();

            out.println("    public static final class Builder extends AbstractAmqpClientBuilder<Builder> {");
            out.println("        public Builder(final AmqpTransport.Builder builder) { super(builder); }");
            out.println("        @Override protected Builder builder() { return this; }");
            out.println("            public Client build(final Vertx vertx) {");
            out.println(
                    "                return new AmqpClient(vertx, new AmqpTransport.Builder(transport()), syncTimeout());");
            out.println("            }");
            out.println("    }");

            out.println();

            out.println("    public static Builder newClient() { return new Builder(AmqpTransport.newTransport()); }");
            out.println(
                    "    public static Builder newClient(final AmqpTransport.Builder transport) { return new Builder(requireNonNull(transport)); }");
            out.println();
            out.println("    private final AmqpTransport transport;");

            out.println();
            out.println(
                    "    private AmqpClient(final Vertx vertx, final AmqpTransport.Builder transport, final Duration syncTimeout) {");
            out.println("        super(syncTimeout);");
            out.println("        this.transport = transport.build(vertx);");
            out.println("    }");

            out.println();
            out.println(
                    "    @Override public void close() throws Exception { this.transport.close(); super.close(); }");
            out.println();

            for (final ServiceMethod method : ServiceMethod.getServiceMethods(this.types, serviceType)) {

                // @Override protected CloseableCompletionStage<Void> internalUpdate(final
                // Device device) { return this.transport.request("device", "update", device,
                // this.transport.ignoreBody()); }

                out.format("    @Override%n");
                out.format("    protected CloseableCompletionStage<%s> %s(",
                        method.getReturnType(), method.getInternalName());

                out.print(method.parameterList());

                out.format(") { return this.transport.request(\"%s\", \"%s\", ",
                        serviceTopic, method.getName());

                // parameters

                if (method.getParameters().size() > 1) {
                    out.append("new Object[]{").append(method.parameterNames()).append("}");
                } else {
                    out.print(method.parameterNames());
                }

                // return type

                out.print(", ");

                final TypeMirror ret = method.getReturnType().getTypeMirror();

                if (ret.getKind().equals(TypeKind.VOID)) {
                    out.format("this.transport.ignoreBody()");
                } else if (this.types.erasure(ret).toString().equals(Optional.class.getName())) {
                    final TypeMirror valueType = ((DeclaredType) ret).getTypeArguments().get(0);
                    out.format("this.transport.bodyAsOptional(%s.class)", valueType);
                } else {
                    out.format("this.transport.bodyAs(%s.class)", ret);
                }

                // finish

                out.println("); }");
                out.println();
            }

            out.println("}");
        }

    }
}
