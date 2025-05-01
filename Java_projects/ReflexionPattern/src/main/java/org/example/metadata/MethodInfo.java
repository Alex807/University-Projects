package org.example.metadata;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class MethodInfo {
    private String name;
    private String returnType;
    private List<String> parameterTypes;
    private int modifiers;

    public MethodInfo() {
        this.parameterTypes = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getReturnType() {

        return returnType;
    }

    public void setReturnType(String returnType) {

        this.returnType = returnType;
    }

    public List<String> getParameterTypes() {

        return parameterTypes;
    }

    public void setParameterTypes(List<String> parameterTypes) {

        this.parameterTypes = parameterTypes;
    }

    public void addParameterType(String parameterType) {
        if (this.parameterTypes == null) {
            this.parameterTypes = new ArrayList<>();
        }
        this.parameterTypes.add(parameterType);
    }

    public int getModifiers() {

        return modifiers;
    }

    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public String toString() {
        return "MethodInfo{" +
                "name='" + name + '\'' +
                ", returnType='" + returnType + '\'' +
                ", parameterTypes=" + parameterTypes +
                ", modifiers=" + Modifier.toString(modifiers) +
                '}';
    }
}
