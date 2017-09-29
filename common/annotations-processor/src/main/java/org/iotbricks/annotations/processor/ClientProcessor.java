package org.iotbricks.annotations.processor;

import java.io.IOException;
import java.io.PrintWriter;
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
import javax.lang.model.element.Name;
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

                createAsyncApi((PackageElement) element, element.toString(), serviceName);
                createClient((PackageElement) element, element.toString(), serviceName);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    private void createAsyncApi(final PackageElement element, final String packageName, final String serviceName)
            throws IOException {
        final TypeElement serviceType = this.processingEnv.getElementUtils().getTypeElement(serviceName);

        // FIXME: we should really consider use JDT AST for this

        final String asyncTypeName = fullAsyncName(packageName, serviceType.getSimpleName().toString());

        final JavaFileObject file = this.filer.createSourceFile(asyncTypeName, element);

        final Types types = this.processingEnv.getTypeUtils();

        try (final PrintWriter out = new PrintWriter(file.openWriter())) {
            out.println("package " + packageName + ";");
            out.println();
            out.println("import io.glutamate.util.concurrent.CloseableCompletionStage;");
            out.println();
            out.format("public interface %s {%n", simpleName(asyncTypeName));

            // process direct children

            for (final Element child : serviceType.getEnclosedElements()) {
                if (child.getKind() != ElementKind.METHOD) {
                    // methods only
                    continue;
                }

                final ExecutableElement method = (ExecutableElement) child;

                // return type : Wrap in CloseableCompletableFuture

                out.print("    CloseableCompletionStage<");

                final TypeMirror ret = method.getReturnType();
                if (ret instanceof PrimitiveType) {
                    out.print(types.boxedClass((PrimitiveType) ret));
                } else if (ret.getKind() == TypeKind.VOID) {
                    out.print("Void");
                } else {
                    out.print(ret);
                }

                out.print("> ");

                // method name

                out.print(method.getSimpleName());

                // parameters

                out.print(" (");

                out.print(method.getParameters().stream()
                        .map(parameter -> {
                            return parameter.asType().toString() + " " + evalParameterName(parameter);
                        })
                        .collect(Collectors.joining(", ")));

                out.print(");");

                // finish up

                out.println();
                out.println();
            }

            out.println("}");
        }

    }

    private Name evalParameterName(final VariableElement parameter) {
        // FIXME: we need an alternate way to figure out the parameter name
        return parameter.getSimpleName();
    }

    private static String fullAsyncName(final String packageName, final String simpleSyncName) {
        return packageName + "." + simpleSyncName + "Async";
    }

    private void createClient(final PackageElement element, final String packageName, final String serviceName)
            throws IOException {
        System.out.format("Creating client in %s for service %s%n", packageName, serviceName);

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
