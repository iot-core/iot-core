package org.iotbricks.annotations.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public class ServiceMethod {

    public static class TypeName {
        private final String type;
        private final TypeMirror typeMirror;

        public TypeName(final String fullQualifiedName, final TypeMirror typeMirror) {

            Objects.requireNonNull(fullQualifiedName);
            if (fullQualifiedName.isEmpty()) {
                throw new IllegalArgumentException("Type name must not be empty");
            }

            this.type = fullQualifiedName;
            this.typeMirror = typeMirror;
        }

        public String getName() {
            return this.type;
        }

        public TypeMirror getTypeMirror() {
            return this.typeMirror;
        }

        public String getSimpleName() {
            final String[] toks = this.type.split("\\.");
            return toks[toks.length - 1];
        }

        public String getPackageName() {
            final int idx = this.type.lastIndexOf('.');
            if (idx >= 0) {
                return this.type.substring(0, idx);
            } else {
                return "";
            }
        }

        @Override
        public String toString() {
            return this.type;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (this.type == null ? 0 : this.type.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TypeName other = (TypeName) obj;
            if (this.type == null) {
                if (other.type != null) {
                    return false;
                }
            } else if (!this.type.equals(other.type)) {
                return false;
            }
            return true;
        }

    }

    public static class Parameter {
        private final TypeName type;
        private final String name;

        public Parameter(final TypeName type, final String name) {
            this.type = type;
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public TypeName getType() {
            return this.type;
        }

        @Override
        public String toString() {
            return String.format("%s %s", this.type, this.name);
        }
    }

    private final String name;
    private final TypeName returnType;
    private final List<Parameter> parameters;

    public ServiceMethod(final String name, final TypeName returnType, final List<Parameter> parameters) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(returnType);
        Objects.requireNonNull(parameters);

        this.name = name;
        this.returnType = returnType;
        this.parameters = Collections.unmodifiableList(parameters);
    }

    public String getName() {
        return this.name;
    }

    public String getInternalName() {
        return "internal" + Character.toUpperCase(this.name.charAt(0)) + this.name.substring(1);
    }

    public List<Parameter> getParameters() {
        return this.parameters;
    }

    public TypeName getReturnType() {
        return this.returnType;
    }

    public String parameterList() {
        return this.parameters.stream().map(Object::toString).collect(Collectors.joining(", "));
    }

    public String parameterNames() {
        return this.parameters.stream().map(Parameter::getName).collect(Collectors.joining(", "));
    }

    public static List<ServiceMethod> getServiceMethods(final Types types, final Element serviceType) {

        final List<ServiceMethod> result = new ArrayList<>();

        for (final Element child : serviceType.getEnclosedElements()) {
            if (child.getKind() != ElementKind.METHOD) {
                // methods only
                continue;
            }

            final ExecutableElement method = (ExecutableElement) child;

            final TypeName returnType;

            final TypeMirror ret = method.getReturnType();
            if (ret.getKind().isPrimitive()) {
                returnType = new TypeName(types.boxedClass((PrimitiveType) ret).toString(), ret);
            } else if (ret.getKind() == TypeKind.VOID) {
                returnType = new TypeName(Void.class.getName(), ret);
            } else {
                returnType = new TypeName(ret.toString(), ret);
            }

            final List<Parameter> parameters = new ArrayList<>(method.getParameters().size());

            for (final VariableElement parameter : method.getParameters()) {
                parameters.add(new Parameter(new TypeName(parameter.asType().toString(), parameter.asType()),
                        evalParameterName(parameter)));
            }

            result.add(new ServiceMethod(method.getSimpleName().toString(), returnType, parameters));
        }

        return result;
    }

    private static String evalParameterName(final VariableElement parameter) {
        // FIXME: we need an alternate way to figure out the parameter name
        return parameter.getSimpleName().toString();
    }
}
