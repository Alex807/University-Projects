package org.example;

import org.example.abstractions.RelationType;
import org.example.abstractions.UMLFormatter;
import org.example.metadata.ClassInfo;
import org.example.metadata.FieldInfo;
import org.example.metadata.MethodInfo;

import java.util.ArrayList;
import java.util.List;

public class YumlFormatter implements UMLFormatter {
    @Override
    public String formatClass(ClassInfo classInfo, UMLGeneratorConfig config) {
        StringBuilder builder = new StringBuilder();
        builder.append("[").append(classInfo.getSimpleName());

        // Format fields if showing attributes
        if (config.ShowAttributes() && !classInfo.getFields().isEmpty()) {
            builder.append("|");
            List<String> formattedFields = new ArrayList<>();
            for (FieldInfo field : classInfo.getFields()) {
                String fieldType = field.getType();
                formattedFields.add("- " + field.getName() + ":" + fieldType);
            }
            // Join fields without trailing semicolon
            builder.append(String.join(";", formattedFields));
        } else {
            builder.append("|"); // Empty fields section
        }

        // Format methods if showing methods
        builder.append("|");
        if (config.ShowMethods()) {
            List<String> formattedMethods = new ArrayList<>();
            for (MethodInfo method : classInfo.getMethods()) {
                formattedMethods.add(method.getName() + "()");
            }
            // Join methods without trailing semicolon
            builder.append(String.join(";", formattedMethods));
        }

        builder.append("]");
        return builder.toString();
    }

    @Override
    public String formatRelationship(ClassInfo from, ClassInfo to, RelationType type, UMLGeneratorConfig config) {
        if (from == null || to == null) {
            return "";
        }

        String fromName = from.getFormattedName(config.UseFullyQualifiedNames(), config.IncludeTypeParameters());
        String toName = to.getFormattedName(config.UseFullyQualifiedNames(), config.IncludeTypeParameters());
        switch (type) {
            case EXTENDS:
                // For interfaces, use dashed line
                if (to.isInterface()) {
                    return String.format("[%s]^-.-[%s]", toName, fromName);
                }
                // For classes, use solid line
                return String.format("[%s]^[%s]", fromName, toName);
            case IMPLEMENTS:
                return String.format("[%s]^-.-[%s]", toName, fromName);
            case ASSOCIATION:
                return String.format("[%s]->[%s]", fromName, toName);
            case DEPENDENCY:
                return String.format("[%s]-.>[%s]", fromName, toName);
            default:
                return "";
        }
    }

    @Override
    public String getFileHeader() {
        return "";
    }

    @Override
    public String getFileFooter() {
        return "";
    }
}
