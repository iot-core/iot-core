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
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import org.iotbricks.annotations.AmqpTransport;
import org.iotbricks.annotations.ServiceName;

@SupportedAnnotationTypes("org.iotbricks.annotations.AmqpTransport")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AmqpTransportProcessor extends AbstractClientProcessor {

    private static final String PKG_CORE_AMQP_TRANSPORT = "org.iotbricks.core.amqp.transport.client";
    private static final String CLASS_AMQP_TRANSPORT = "AmqpClientTransport";
    private static final String CLASS_ABSTRACT_CLIENT_BUILDER = "AbstractAmqpClientBuilder";

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
            final ServiceName serviceName = serviceType.getAnnotation(ServiceName.class);
            if (serviceName != null) {
                serviceTopic = serviceName.value();
            }
        }
        if (serviceTopic == null || serviceTopic.isEmpty()) {
            serviceTopic = serviceType.getQualifiedName().toString();
        }

        if (serviceTopic == null || serviceTopic.isEmpty()) {
            this.messager.printMessage(Kind.ERROR,
                    "Unable to detect service name. You can fall back setting the value attribute of @"
                            + AmqpTransport.class.getSimpleName(),
                    element);
            throw new IllegalStateException();
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
            out.println("import io.glutamate.lang.Resource;");
            out.println("import io.vertx.core.Vertx;");
            out.println();
            out.format("import %s.%s;%n", PKG_CORE_AMQP_TRANSPORT, CLASS_AMQP_TRANSPORT);
            out.format("import %s.%s;%n", PKG_CORE_AMQP_TRANSPORT, CLASS_ABSTRACT_CLIENT_BUILDER);
            out.println();

            out.println("public class AmqpClient extends AbstractDefaultClient {");
            out.println();

            out.format("    public static final class Builder extends %s<Builder> {%n", CLASS_ABSTRACT_CLIENT_BUILDER);
            out.format("        public Builder(final %s.Builder builder) { super(builder); }%n",
                    CLASS_AMQP_TRANSPORT);
            out.format("        @Override%n        protected Builder builder() { return this; }%n");
            out.println("            public Client build(final Resource<Vertx> vertx) {");
            out.format(
                    "                return new AmqpClient(vertx, %s.newTransport(transport()), syncTimeout());%n",
                    CLASS_AMQP_TRANSPORT);
            out.println("            }");
            out.println("    }");

            out.println();

            out.format("    public static Builder newClient() { return new Builder(%s.newTransport()); }%n",
                    CLASS_AMQP_TRANSPORT);
            out.format(
                    "    public static Builder newClient(final %s.Builder transport) { return new Builder(requireNonNull(transport)); }%n",
                    CLASS_AMQP_TRANSPORT);
            out.println();
            out.format("    private final %s transport;%n", CLASS_AMQP_TRANSPORT);

            out.println();
            out.format(
                    "    private AmqpClient(final Resource<Vertx> vertx, final %s.Builder transport, final Duration syncTimeout) {%n",
                    CLASS_AMQP_TRANSPORT);
            out.println("        super(syncTimeout);");
            out.println("        this.transport = transport.build(vertx);");
            out.println("    }");

            out.println();
            out.format(
                    "    @Override%n    public void close() throws Exception { this.transport.close(); super.close(); }%n");
            out.println();

            for (final ServiceMethod method : ServiceMethod.getServiceMethods(this.types, serviceType)) {

                out.format("    @Override%n");
                out.format("    protected CloseableCompletionStage<%s> %s(",
                        method.getReturnType(), method.getInternalName());

                out.print(method.parameterList());

                out.format(") { return this.transport.request(\"%s\", \"%s\", ",
                        serviceTopic, method.getName());

                // parameters

                out.append("new Object[]{").append(method.parameterNames()).append("}");

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
