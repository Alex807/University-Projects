package org.example.abstractions;

import org.example.UMLGeneratorConfig;
import org.example.metadata.ClassInfo;

public interface UMLFormatter {
    String formatClass(ClassInfo classInfo, UMLGeneratorConfig config);
    String formatRelationship(ClassInfo from, ClassInfo to, RelationType type, UMLGeneratorConfig config);
    String getFileHeader(); //this 2 fields used only in PlantUMLFormatter
    String getFileFooter();
}