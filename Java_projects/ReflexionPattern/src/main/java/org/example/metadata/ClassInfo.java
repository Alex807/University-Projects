package org.example.metadata;

import java.util.*;
import java.util.stream.Collectors;

public class ClassInfo {
    private String className;
    private String superClassName;
    private String packageName;
    private boolean isInterface;
    private List<String> interfaces;
    private List<FieldInfo> fields;
    private List<MethodInfo> methods;
    private final Map<String, String> typeParameters; // For generic type information

    public ClassInfo() {
        this.interfaces = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.typeParameters = new HashMap<>();
        packageName = "";
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSuperClassName() {
        return superClassName;
    }

    public void setSuperClassName(String superClassName) {
        this.superClassName = superClassName;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<String> interfaces) {
        this.interfaces = interfaces;
    }

    public void setIsInterface(boolean isInterface) {
        this.isInterface = isInterface;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public void addInterface(String interfaceName) {
        if (this.interfaces == null) {
            this.interfaces = new ArrayList<>();
        }
        this.interfaces.add(interfaceName);
    }

    public List<FieldInfo> getFields() {
        return fields;
    }

    public void setFields(List<FieldInfo> fields) {
        this.fields = fields;
    }

    public void addField(FieldInfo field) {
        if (this.fields == null) {
            this.fields = new ArrayList<>();
        }
        this.fields.add(field);
    }

    public List<MethodInfo> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodInfo> methods) {
        this.methods = methods;
    }

    public void addMethod(MethodInfo method) {
        if (this.methods == null) {
            this.methods = new ArrayList<>();
        }
        this.methods.add(method);
    }

    public void addTypeParameter(String paramName, String actualType) {
        typeParameters.put(paramName, actualType);
    }

    public Map<String, String> getTypeParameters() {
        return typeParameters;
    }

    public String getPackageName() {
        if (!packageName.isEmpty()) {
            return packageName;
        }

        int lastDot;
        if (!className.isEmpty()) {
            lastDot = className.lastIndexOf('.');
        } else {
            lastDot = -1;
        }
        if(lastDot > 0) {
            packageName = className.substring(0, lastDot);
        }
        return packageName;
    }

    public String getSimpleName() { //returns only the name of class, without package
        int lastDot;
        if (!className.isEmpty()) {
            lastDot = className.lastIndexOf('.');
        } else {
            lastDot = -1;
        }
        return lastDot > 0 ? className.substring(lastDot + 1) : className;
    }

    public String getFormattedName(boolean useFullyQualifiedNames, boolean includeTypeParameters) {
        StringBuilder formattedName = new StringBuilder();

        // Add package name if using fully qualified names
        if (useFullyQualifiedNames && !packageName.isEmpty()) {
            formattedName.append(packageName).append(".");
        }

        // Add class name
        formattedName.append(className);

        // Add type parameters if present and configured to show them
        if (includeTypeParameters && !typeParameters.isEmpty()) {
            formattedName.append("<");

            // Join all type parameters with commas
            String params = typeParameters.values().stream()
                    .filter(param -> param != null && !param.isEmpty())
                    .collect(Collectors.joining(", "));

            formattedName.append(params);
            formattedName.append(">");
        }

        return formattedName.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ClassInfo{\n");
        sb.append("  className='").append(className).append("'\n");
        sb.append("  superClassName='").append(superClassName).append("'\n");
        sb.append("  interfaces=").append(interfaces).append("\n");
        sb.append("  fields=").append(fields).append("\n");
        sb.append("  methods=").append(methods).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassInfo classInfo = (ClassInfo) o;
        return Objects.equals(className, classInfo.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className);
    }
}
