package org.iotbricks.annotations.processor;

import static org.iotbricks.annotations.processor.ServiceMethod.getServiceMethods;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import org.iotbricks.annotations.Client;

@SupportedAnnotationTypes("org.iotbricks.annotations.Client")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ClientProcessor extends AbstractClientProcessor {

    private static final String PKG_CORE_UTILS_CLIENT = "org.iotbricks.core.utils.client";

    public ClientProcessor() {
        super(Client.class);
    }

    @Override
    protected void processClient(final PackageElement packageElement, final TypeElement serviceType,
            final RoundEnvironment roundEnv)
            throws Exception {

        this.messager.printMessage(Kind.NOTE, String.format("Creating client for: %s", serviceType), packageElement);

        createAsyncApi(packageElement, packageElement.toString(), serviceType.getQualifiedName().toString());
        createClient(packageElement, packageElement.toString(), serviceType.getQualifiedName().toString());
        createSyncWrapper(packageElement, packageElement.toString(), serviceType.getQualifiedName().toString());
        createAbstractDefaultClient(packageElement, packageElement.toString(),
                serviceType.getQualifiedName().toString());
    }

    private void createAsyncApi(final PackageElement element, final String packageName, final String serviceName)
            throws IOException {
        final TypeElement serviceType = this.processingEnv.getElementUtils().getTypeElement(serviceName);

        // FIXME: we should really consider use JDT AST for this

        final String asyncTypeName = fullAsyncName(packageName, serviceType.getSimpleName().toString());

        final JavaFileObject file = this.filer.createSourceFile(asyncTypeName, element);

        try (final PrintWriter out = new PrintWriter(file.openWriter())) {
            out.println("package " + packageName + ";");
            out.println();
            out.println("import io.glutamate.util.concurrent.CloseableCompletionStage;");
            out.println();
            out.format("public interface %s {%n", simpleName(asyncTypeName));

            for (final ServiceMethod method : ServiceMethod.getServiceMethods(this.types, serviceType)) {
                out.format("    CloseableCompletionStage<%s> %s (", method.getReturnType(), method.getName());
                out.print(method.parameterList());
                out.format(");%n");
            }

            out.println("}");
        }

    }

    private void createClient(final PackageElement element, final String packageName, final String serviceName)
            throws IOException {

        // FIXME: consider using JDT AST for doing all this

        final String simpleServiceName = simpleName(serviceName);

        final JavaFileObject file = this.filer.createSourceFile(packageName + ".Client", element);
        try (final PrintWriter out = new PrintWriter(file.openWriter())) {
            out.println("package " + packageName + ";");
            out.println();
            out.println("import java.time.Duration;");
            out.println();
            out.println("public interface Client extends AutoCloseable {\n\n" +
                    "    " + serviceName + " sync();\n\n" +
                    "    " + serviceName + " sync(Duration timeout);\n\n" +
                    "    " + fullAsyncName(packageName, simpleServiceName) + " async();\n\n" +
                    "    @Override\n" +
                    "    public default void close() throws Exception {\n" +
                    "    }\n" +
                    "\n" +
                    "}");
        }
    }

    private void createAbstractDefaultClient(final PackageElement element, final String packageName,
            final String serviceName) throws IOException {

        final TypeElement serviceType = this.processingEnv.getElementUtils().getTypeElement(serviceName);

        // FIXME: we should really, really consider use JDT AST for this

        final String name = packageName + ".AbstractDefaultClient";
        final String asyncTypeName = fullAsyncName(packageName, serviceType.getSimpleName().toString());

        final JavaFileObject file = this.filer.createSourceFile(name, element);

        try (final PrintWriter out = new PrintWriter(file.openWriter())) {
            out.println("package " + packageName + ";");
            out.println();
            out.println("import java.time.Duration;");
            out.println();
            out.println("import io.glutamate.util.concurrent.CloseableCompletionStage;");
            out.println();
            out.println("public abstract class AbstractDefaultClient implements Client {");
            out.println();
            out.println("    private final Duration timeout;");
            out.println();
            out.println("    public AbstractDefaultClient(final Duration timeout) { this.timeout = timeout; }");
            out.println();
            out.format(
                    "    @Override public %s sync() { return new SyncDeviceRegistryServiceWrapper(async(), this.timeout); }%n%n",
                    serviceName);

            out.format("    @Override public %s sync(final Duration timeout) {%n", serviceName);
            out.println("        if (timeout == null) { return sync(); }");
            out.println("        return new SyncDeviceRegistryServiceWrapper(async(), timeout);");
            out.println("    }");
            out.println();

            out.format("    @Override public %s async() {%n", asyncTypeName);
            out.format("        return new %s() {%n", asyncTypeName);

            for (final ServiceMethod method : getServiceMethods(this.types, serviceType)) {

                // @Override public CloseableCompletionStage<String> save(final Device device) {
                // return internalSave(device); }

                out.format("            @Override public CloseableCompletionStage<%s> %s (",
                        method.getReturnType(), method.getName());
                out.print(method.getParameters().stream().map(Object::toString).collect(Collectors.joining(", ")));
                out.format(") { return %s(", method.getInternalName());
                out.print(method.parameterNames());
                out.format(");}%n");
            }

            out.println("        };");
            out.println("    }");
            out.println();

            for (final ServiceMethod method : getServiceMethods(this.types, serviceType)) {
                out.format("    protected abstract CloseableCompletionStage<%s> %s (", method.getReturnType(),
                        method.getInternalName());
                out.print(method.parameterList());
                out.format(");%n");
            }

            out.println("}");
        }

    }

    private void createSyncWrapper(final PackageElement element, final String packageName,
            final String serviceName) throws IOException {

        final TypeElement serviceType = this.processingEnv.getElementUtils().getTypeElement(serviceName);

        // FIXME: we should really, really consider use JDT AST for this

        final String name = packageName + ".SyncDeviceRegistryServiceWrapper";
        final String asyncTypeName = fullAsyncName(packageName, serviceType.getSimpleName().toString());

        final JavaFileObject file = this.filer.createSourceFile(name, element);

        try (final PrintWriter out = new PrintWriter(file.openWriter())) {
            out.println("package " + packageName + ";");
            out.println();
            out.println("import java.time.Duration;");
            out.println();
            out.format(
                    "public class SyncDeviceRegistryServiceWrapper extends %s.AbstractSyncWrapper implements %s {%n",
                    PKG_CORE_UTILS_CLIENT, serviceName);
            out.println();
            out.format("    private final %s async;%n", asyncTypeName);
            out.println();
            out.format(
                    "    public SyncDeviceRegistryServiceWrapper(final %s async, final Duration timeout) { super(timeout); this.async = async; }%n%n",
                    asyncTypeName);

            for (final ServiceMethod method : getServiceMethods(this.types, serviceType)) {

                if (!method.getReturnType().getName().equals("java.lang.Void")) {
                    out.format(
                            "    @Override public %4$s %1$s(%2$s) { return await(this.async.%1$s(%3$s)); }%n",
                            method.getName(), method.parameterList(), method.parameterNames(), method.getReturnType());
                } else {
                    out.format(
                            "    @Override public void %1$s(%2$s) { await(this.async.%1$s(%3$s)); }%n",
                            method.getName(), method.parameterList(), method.parameterNames());
                }

            }

            out.println("}");
        }

    }

    private static String fullAsyncName(final String packageName, final String simpleSyncName) {
        return packageName + "." + simpleSyncName + "Async";
    }

    private static String simpleName(final String name) {
        final String[] toks = name.split("\\.");
        return toks[toks.length - 1];
    }

}
