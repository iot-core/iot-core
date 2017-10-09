package org.iotbricks.annotations.processor;

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import org.iotbricks.annotations.LocalTransport;

@SupportedAnnotationTypes("org.iotbricks.annotations.LocalTransport")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class LocalTransportProcessor extends AbstractClientProcessor {

    public LocalTransportProcessor() {
        super(LocalTransport.class);
    }

    @Override
    protected void processClient(final PackageElement packageElement, final TypeElement serviceType,
            final RoundEnvironment roundEnv) throws Exception {

        createLocalTransport(packageElement, serviceType);

    }

    private void createLocalTransport(final PackageElement element,
            final TypeElement serviceType) throws IOException {

        final String serviceClassName = serviceType.getQualifiedName().toString();

        final String name = element.toString() + ".LocalClient";

        final JavaFileObject file = this.filer.createSourceFile(name, element);

        try (final PrintWriter out = new PrintWriter(file.openWriter())) {

            out.format("package %s;%n%n", element);

            out.println("import static io.glutamate.util.concurrent.CloseableCompletionStage.of;");
            out.println();
            out.println("import static java.util.concurrent.CompletableFuture.runAsync;");
            out.println("import static java.util.concurrent.CompletableFuture.supplyAsync;");
            out.println();
            out.println("import java.util.Objects;");
            out.println("import java.util.Optional;");
            out.println("import java.util.concurrent.ExecutorService;");
            out.println("import java.util.concurrent.Executors;");
            out.println();
            out.println("import io.glutamate.util.concurrent.CloseableCompletionStage;");

            out.format("%n%npublic class LocalClient extends AbstractDefaultClient {%n");

            out.format("    private final %s service;%n", serviceClassName);
            out.println("    private final ExecutorService executionService;");

            out.println();

            out.format("    public LocalClient(final %s service) {%n", serviceClassName);
            out.println("        super(null);");
            out.println();
            out.println("        Objects.requireNonNull(service);");
            out.println();
            out.println("        this.service = service;");
            out.println("        this.executionService = Executors.newCachedThreadPool();");
            out.println("    }");

            out.println();

            out.format(
                    "    @Override%n    public void close() throws Exception { super.close(); this.executionService.shutdown(); }%n");

            out.println();

            for (final ServiceMethod method : ServiceMethod.getServiceMethods(this.types, serviceType)) {

                out.format("    @Override%n");
                out.format("    protected CloseableCompletionStage<%s> %s(",
                        method.getReturnType(), method.getInternalName());

                out.print(method.parameterList());

                out.format(") {%n");

                out.append("        ");

                if (method.getReturnType().isVoid()) {
                    out.format("return of(runAsync(() -> this.service.%s(", method.getName());
                } else {
                    out.format("return of(supplyAsync(() -> this.service.%s(", method.getName());
                }

                out.format("%s), this.executionService));%n    }%n%n", method.parameterNames());
            }

            // close class

            out.println();
            out.println("}");
        }

    }
}
