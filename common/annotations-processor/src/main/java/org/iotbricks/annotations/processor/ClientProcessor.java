package org.iotbricks.annotations.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import org.iotbricks.annotations.Client;
import org.iotbricks.annotations.processor.ServiceMethod.Parameter;
import org.iotbricks.annotations.processor.ServiceMethod.TypeName;

@SupportedAnnotationTypes("org.iotbricks.annotations.Client")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ClientProcessor extends AbstractProcessor {

    private Messager messager;
    private Filer filer;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

        try {
            for (final Element element : roundEnv.getElementsAnnotatedWith(Client.class)) {
                final Client client = element.getAnnotation(Client.class);
                final String serviceName = getServiceType(client);
                this.messager.printMessage(Kind.NOTE, String.format("Creating client for: %s", serviceName), element);

                if (!(element instanceof PackageElement)) {
                    throw new IllegalStateException("Annotation must be on package level");
                }

                final PackageElement packageElement = (PackageElement) element;
                if (packageElement.isUnnamed()) {
                    throw new IllegalStateException(
                            "Annotation must be on named package. Unnamed packages are not supported!");
                }

                createAsyncApi(packageElement, element.toString(), serviceName);
                createClient(packageElement, element.toString(), serviceName);
                createSyncWrapper(packageElement, element.toString(), serviceName);
                createAbstractDefaultClient(packageElement, element.toString(), serviceName);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    private List<ServiceMethod> getServiceMethods(final Element serviceType) {

        final Types types = this.processingEnv.getTypeUtils();
        final List<ServiceMethod> result = new ArrayList<>();

        for (final Element child : serviceType.getEnclosedElements()) {
            if (child.getKind() != ElementKind.METHOD) {
                // methods only
                continue;
            }

            final ExecutableElement method = (ExecutableElement) child;

            final TypeName returnType;

            final TypeMirror ret = method.getReturnType();
            if (ret instanceof PrimitiveType) {
                returnType = new TypeName(types.boxedClass((PrimitiveType) ret).toString());
            } else if (ret.getKind() == TypeKind.VOID) {
                returnType = new TypeName("Void");
            } else {
                returnType = new TypeName(ret.toString());
            }

            final List<Parameter> parameters = new ArrayList<>(method.getParameters().size());

            for (final VariableElement parameter : method.getParameters()) {
                parameters.add(new Parameter(new TypeName(parameter.asType().toString()),
                        evalParameterName(parameter)));
            }

            result.add(new ServiceMethod(method.getSimpleName().toString(), returnType, parameters));
        }

        return result;
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

            for (final ServiceMethod method : getServiceMethods(serviceType)) {
                out.format("    CloseableCompletionStage<%s> %s (", method.getReturnType(), method.getName());
                out.print(parameterList(method));
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

            for (final ServiceMethod method : getServiceMethods(serviceType)) {

                // @Override public CloseableCompletionStage<String> save(final Device device) {
                // return internalSave(device); }

                out.format("            @Override public CloseableCompletionStage<%s> %s (",
                        method.getReturnType(), method.getName());
                out.print(method.getParameters().stream().map(Object::toString).collect(Collectors.joining(", ")));
                out.format(") { return %s(", method.getInternalName());
                out.print(parameterNames(method));
                out.format(");}%n");
            }

            out.println("        };");
            out.println("    }");
            out.println();

            for (final ServiceMethod method : getServiceMethods(serviceType)) {
                out.format("    protected abstract CloseableCompletionStage<%s> %s (", method.getReturnType(),
                        method.getInternalName());
                out.print(parameterList(method));
                out.format(");%n");
            }

            out.println("}");
        }

    }

    private String parameterList(final ServiceMethod method) {
        return method.getParameters().stream().map(Object::toString).collect(Collectors.joining(", "));
    }

    private String parameterNames(final ServiceMethod method) {
        return method.getParameters().stream().map(Parameter::getName).collect(Collectors.joining(", "));
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
                    "public class SyncDeviceRegistryServiceWrapper extends iot.core.utils.client.AbstractSyncWrapper implements %s {%n",
                    serviceName);
            out.println();
            out.format("    private final %s async;%n", asyncTypeName);
            out.println();
            out.format(
                    "    public SyncDeviceRegistryServiceWrapper(final %s async, final Duration timeout) { super(timeout); this.async = async; }%n%n",
                    asyncTypeName);

            for (final ServiceMethod method : getServiceMethods(serviceType)) {

                if (!method.getReturnType().getName().equals("Void")) {
                    out.format(
                            "@Override public %4$s %1$s(%2$s) { return await(this.async.%1$s(%3$s)); }%n",
                            method.getName(), parameterList(method), parameterNames(method), method.getReturnType());
                } else {
                    out.format(
                            "@Override public void %1$s(%2$s) { await(this.async.%1$s(%3$s)); }%n",
                            method.getName(), parameterList(method), parameterNames(method));
                }

            }

            out.println("}");
        }

    }

    private String evalParameterName(final VariableElement parameter) {
        // FIXME: we need an alternate way to figure out the parameter name
        return parameter.getSimpleName().toString();
    }

    private static String fullAsyncName(final String packageName, final String simpleSyncName) {
        return packageName + "." + simpleSyncName + "Async";
    }

    private static String simpleName(final String name) {
        final String[] toks = name.split("\\.");
        return toks[toks.length - 1];
    }

    private static String getServiceType(final Client client) {
        try {
            client.value();
            throw new IllegalStateException("Unable to get service type");
        } catch (final MirroredTypeException e) {
            return e.getTypeMirror().toString();
        }
    }

}
