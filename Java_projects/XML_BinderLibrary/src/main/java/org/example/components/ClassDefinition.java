package org.example.components;

import java.util.ArrayList;
import java.util.List;

public class ClassDefinition {
    private final String className;
    private List<AttributeDefinition> attributes = new ArrayList<>();

    public ClassDefinition(String className) {
        this.className = className;
    }

    public void addAttribute(String name, String type, boolean isMultiple) {
        attributes.add(new AttributeDefinition(name, type, isMultiple));
    }

    // Getters
    public String getClassName() { return className; }
    public List<AttributeDefinition> getAttributes() { return attributes; }
}