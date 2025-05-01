package org.example;

import org.example.abstractions.RelationType;
import org.example.abstractions.UMLFormatter;
import org.example.metadata.ClassInfo;
import org.example.metadata.FieldInfo;
import org.example.metadata.MethodInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UMLDiagramGenerator {
    private final UMLFormatter formatter;
    private final UMLGeneratorConfig config;
    private Map<String, ClassInfo> classInfoMap; // To look up ClassInfo by class name

    public UMLDiagramGenerator(UMLFormatter formatter,
                               Set<String> ignoredClasses,
                               boolean showAttributes,
                               boolean showMethods,
                               boolean useFullyQualifiedNames,
                               boolean includeTypeParameters) {
        this.formatter = formatter;
        this.config = new UMLGeneratorConfig(useFullyQualifiedNames, showMethods, showAttributes, includeTypeParameters);
        config.setIgnoredClasses(ignoredClasses);
    }

    public String generateDiagram(List<ClassInfo> classes) {
        // Create a map for easy class lookup
        this.classInfoMap = classes.stream()
                .collect(Collectors.toMap(ClassInfo::getClassName, cls -> cls));

        StringBuilder diagram = new StringBuilder();
        diagram.append(formatter.getFileHeader());

        // Format all classes first
        for (ClassInfo classInfo : classes) {
            if (!shouldIgnoreClass(classInfo.getClassName())) {
                String classFormat = formatter.formatClass(classInfo, config);
                if (!classFormat.isEmpty()) {
                    diagram.append(classFormat).append("\n");
                } //to avoid multiple empty new lines
            }
        }

        // Format all relationships
        for (ClassInfo classInfo : classes) {
            if (shouldIgnoreClass(classInfo.getClassName())) {
                continue;
            }

            // Handle superclass relationships (extends)
            String superClassName = classInfo.getSuperClassName();
            if (superClassName != null && !superClassName.equals("java.lang.Object")) {

                ClassInfo superClass = classInfoMap.get(superClassName);
                if (superClass != null && !shouldIgnoreClass(superClassName)) {
                    diagram.append(formatter.formatRelationship(
                            classInfo,
                            superClass,
                            RelationType.EXTENDS,
                            config
                    )).append("\n");
                }
            }

            // Handle interface relationships (extends for interfaces, implements for classes)
            for (String interfaceName : classInfo.getInterfaces()) {
                ClassInfo interfaceInfo = classInfoMap.get(interfaceName);
                if (interfaceInfo != null && !shouldIgnoreClass(interfaceName)) {
                    RelationType relationType = interfaceInfo.isInterface() ?
                            RelationType.EXTENDS :
                            RelationType.IMPLEMENTS;
                    diagram.append(formatter.formatRelationship(
                            classInfo,
                            interfaceInfo,
                            relationType,
                            config
                    )).append("\n");
                }
            }

            // Handle associations (through fields)
            for (FieldInfo field : classInfo.getFields()) {
                String fieldType = field.getType();
                ClassInfo fieldInfo = classInfoMap.get(fieldType);
                if (fieldInfo != null && !shouldIgnoreClass(fieldType)) {
                    diagram.append(formatter.formatRelationship(
                            classInfo,
                            fieldInfo,
                            RelationType.ASSOCIATION,
                            config
                    )).append("\n");
                }
            }

            // Handle dependencies (through method parameters and return types)
            Set<String> dependencies = analyzeDependencies(classInfo);
            for (String dependency : dependencies) {
                ClassInfo dependencyInfo = classInfoMap.get(dependency);
                if (dependencyInfo != null && !shouldIgnoreClass(dependency)) {
                    diagram.append(formatter.formatRelationship(
                            classInfo,
                            dependencyInfo,
                            RelationType.DEPENDENCY,
                            config
                    )).append("\n");
                }
            }
        }

        diagram.append(formatter.getFileFooter());
        return diagram.toString();
    }

    private boolean shouldIgnoreClass(String className) {
        if (className == null || className.isEmpty() || className.equals("java.lang.Object") || config.isAnIgnoredClass(className)) {
            return true;
        }

        // Check if class should be ignored based on patterns
        Set<String> ignoredClasses = config.getIgnoredClasses();
        for (String pattern : ignoredClasses) {
            if (pattern.endsWith(".*")) {
                String packagePattern = pattern.substring(0, pattern.length() - 2);
                if (className.startsWith(packagePattern)) {
                    return true;
                }
            } else if (pattern.equals(className)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> analyzeDependencies(ClassInfo classInfo) {
        Set<String> dependencies = new HashSet<>();
        // Add method return types and parameter types as dependencies
        for (MethodInfo method : classInfo.getMethods()) {
            // Add return type
            if (!method.getReturnType().equals("void")) {
                dependencies.add(method.getReturnType());
            }

            // Add parameter types only for classes
            // Skip parameter dependencies for interface methods
            if (!classInfo.isInterface()) {
                dependencies.addAll(method.getParameterTypes());
            }
        }

        // Remove self-references and already associated types (from fields)
        dependencies.remove(classInfo.getClassName());
        for (FieldInfo field : classInfo.getFields()) {
            dependencies.remove(field.getType());
        }

        return dependencies;
    }
}