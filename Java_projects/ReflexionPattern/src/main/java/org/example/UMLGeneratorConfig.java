package org.example;

import java.util.HashSet;
import java.util.Set;

public class UMLGeneratorConfig {
    private Set<String> ignoredClasses;
    private boolean useFullyQualifiedNames;
    private boolean showMethods;
    private boolean showAttributes;
    private boolean includeTypeParameters;

    public UMLGeneratorConfig(boolean useFullyQualifiedNames, boolean showMethods, boolean showAttributes, boolean includeTypeParameters) {
        this.ignoredClasses = new HashSet<>();

        // Add default ignored patterns
        ignoredClasses.add("java.lang.*");
        ignoredClasses.add("java.util.*");

        this.useFullyQualifiedNames = useFullyQualifiedNames;
        this.showMethods = showMethods;
        this.showAttributes = showAttributes;
        this.includeTypeParameters = includeTypeParameters;
    }

    public Set<String> getIgnoredClasses() {
        return ignoredClasses;
    }

    public void setIgnoredClasses(Set<String> ignoredClasses) {
        this.ignoredClasses = ignoredClasses;
    }

    public boolean isAnIgnoredClass(String className) {
        return ignoredClasses.contains(className);
    }

    public void addIgnoredClass(String pattern) {
        this.ignoredClasses.add(pattern);
    }

    public boolean UseFullyQualifiedNames() {
        return useFullyQualifiedNames;
    }

    public void setUseFullyQualifiedNames(boolean useFullyQualifiedNames) {
        this.useFullyQualifiedNames = useFullyQualifiedNames;
    }

    public boolean ShowMethods() {
        return showMethods;
    }

    public void setShowMethods(boolean showMethods) {
        this.showMethods = showMethods;
    }

    public boolean ShowAttributes() {
        return showAttributes;
    }

    public void setShowAttributes(boolean showAttributes) {
        this.showAttributes = showAttributes;
    }

    public boolean IncludeTypeParameters() {
        return includeTypeParameters;
    }

    public void setIncludeTypeParameters(boolean includeTypeParameters) {
        this.includeTypeParameters = includeTypeParameters;
    }
}