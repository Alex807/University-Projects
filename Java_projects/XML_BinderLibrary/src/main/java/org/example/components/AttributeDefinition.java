package org.example.components;

public class AttributeDefinition {
    private final String name;
    private final String type;
    private final boolean isMultiple;

    public AttributeDefinition(String name, String type, boolean isMultiple) {
        this.name = name;
        this.type = type;
        this.isMultiple = isMultiple;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public boolean isMultiple() { return isMultiple; }
}