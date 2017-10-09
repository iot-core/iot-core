package org.iotbricks.annotations.processor;

import static org.iotbricks.annotations.processor.Clients.getServiceType;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import org.iotbricks.annotations.Client;

public abstract class AbstractClientProcessor extends AbstractProcessor {

    private final Class<? extends Annotation> clazz;

    protected Messager messager;
    protected Filer filer;
    protected Types types;

    public AbstractClientProcessor(final Class<? extends Annotation> clazz) {
        this.clazz = clazz;
    }

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        this.types = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

        for (final Element element : roundEnv.getElementsAnnotatedWith(this.clazz)) {

            final Client client = element.getAnnotation(Client.class);

            if (client == null) {
                this.messager.printMessage(Kind.ERROR,
                        "Package must also be annotated with @" + Client.class.getName()
                                + "  in order to use @" + this.clazz.getName(),
                        element);
                return true;
            }

            if (!(element instanceof PackageElement)) {
                throw new IllegalStateException("Annotation must be on package level");
            }

            final PackageElement packageElement = (PackageElement) element;
            if (packageElement.isUnnamed()) {
                throw new IllegalStateException(
                        "Annotation must be on named package. Unnamed packages are not supported!");
            }

            final TypeMirror service = getServiceType(client);
            final TypeElement serviceType = this.processingEnv.getElementUtils().getTypeElement(service.toString());

            try {
                processClient(packageElement, serviceType, roundEnv);
            } catch (final Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        }

        return true;
    }

    protected abstract void processClient(PackageElement packageElement, TypeElement serviceType,
            RoundEnvironment roundEnv) throws Exception;
}