package org.example;

import org.example.abstractions.RelationType;
import org.example.abstractions.UMLFormatter;
import org.example.metadata.ClassInfo;
import org.example.metadata.FieldInfo;
import org.example.metadata.MethodInfo;

public class PlantUMLFormatter implements UMLFormatter {
    @Override
    public String formatClass(ClassInfo classInfo, UMLGeneratorConfig config) {
        StringBuilder builder = new StringBuilder();

        // Determine if it's an interface or class
        if (classInfo.isInterface()) {
            builder.append("interface ");
        } else {
            builder.append("class ");
        }

        builder.append(classInfo.getSimpleName()).append("{\n");

        // Add fields if showing attributes
        if (config.ShowAttributes() && !classInfo.getFields().isEmpty()) {
            for (FieldInfo field : classInfo.getFields()) {
                builder.append(" -").append(field.getName())
                        .append(":")
                        .append(field.getType())
                        .append("\n");
            }
        }

        // Add methods if showing methods
        if (config.ShowMethods() && !classInfo.getMethods().isEmpty()) {
            for (MethodInfo method : classInfo.getMethods()) {
                builder.append(" ")
                        .append(method.getName())
                        .append("()\n");
            }
        }

        builder.append("}\n\n");
        return builder.toString();
    }

    @Override
    public String formatRelationship(ClassInfo from, ClassInfo to, RelationType type, UMLGeneratorConfig config) {
        if (from == null || to == null) {
            return "";
        }

        String fromName = from.getSimpleName();
        String toName = to.getSimpleName();

        switch (type) {
            case IMPLEMENTS:
                return String.format("%s <|--- %s\n\n", toName, fromName);
            case ASSOCIATION:
                return String.format("%s ---> %s\n\n", fromName, toName);
            case EXTENDS:
                if (to.isInterface()) {
                    return String.format("%s <|--- %s\n\n", toName, fromName);
                }
                return String.format("%s --|> %s\n\n", fromName, toName);
            default:
                return "";
        }
    }

    @Override
    public String getFileHeader() {
        return "@startuml\n\n";
    }

    @Override
    public String getFileFooter() {
        return "\n\n@enduml";
    }
}