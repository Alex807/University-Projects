package org.example.metadata;

import java.lang.reflect.Modifier;

public class FieldInfo {
    private String name;
    private String type;
    private int modifiers;

    public FieldInfo() {
    }

    public FieldInfo(String name, String type, int modifiers) {
        this.name = name;
        this.type = type;
        this.modifiers = modifiers;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public int getModifiers() {

        return modifiers;
    }

    public void setModifiers(int modifiers) {

        this.modifiers = modifiers;
    }

    @Override
    public String toString() {
        return "FieldInfo{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", modifiers=" + Modifier.toString(modifiers) +
                '}';
    }
}
