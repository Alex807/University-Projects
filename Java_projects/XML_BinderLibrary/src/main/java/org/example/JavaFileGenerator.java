package org.example;

import org.example.components.AttributeDefinition;
import org.example.components.ClassDefinition;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JavaFileGenerator {
    public static void generateJavaFile(ClassDefinition classDef, String outputDir, String packageName) throws IOException {
        String className = classDef.getClassName();
        StringBuilder code = new StringBuilder();

        code.append("package ")
            .append(Main.DEFAULT_PACKAGE_ROOT) //add the package declaration to can be a fully functional class
            .append(".")
            .append(packageName.toLowerCase())
            .append(";\n\n");

        // Determine if we need List/ArrayList imports
        boolean needsListImports = classDef.getAttributes().stream()
                .anyMatch(AttributeDefinition::isMultiple);

        // Add imports if needed
        if (needsListImports) {
            code.append("import java.util.ArrayList;\n");
            code.append("import java.util.List;\n\n");
        }

        // Add class definition
        code.append("public class ").append(className).append(" {\n");

        // Add all attributes
        for (AttributeDefinition member : classDef.getAttributes()) {
            code.append("    public ");
            if (member.isMultiple()) { //check if it is a list or something
                code.append("List<").append(member.getType()).append("> ")
                        .append(member.getName())
                        .append(" = new ArrayList<").append(member.getType()).append(">();\n");
            } else {
                code.append(member.getType()).append(" ")
                        .append(member.getName()).append(";\n");
            }
        }

        generateGettersAndSetters(code, classDef);

        code.append("}\n");

        // Write to file to the outputDir
        File outputFile = new File(outputDir, className + ".java");
        try (FileWriter writer = new FileWriter(outputFile)) { //try with resources
            writer.write(code.toString());
            System.out.println("Generated: " + outputFile.getAbsolutePath());
        }
    }

    private static void generateGettersAndSetters(StringBuilder code, ClassDefinition classDef) {
        for (AttributeDefinition member : classDef.getAttributes()) {
            String capitalizedName = capitalize(member.getName());

            if (member.isMultiple()) {
                // Getter for list
                code.append("\n    public List<").append(member.getType())
                        .append("> get").append(capitalizedName).append("() {\n")
                        .append("        if (").append(member.getName()).append(" == null) {\n")
                        .append("            ").append(member.getName())
                        .append(" = new ArrayList<>();\n")
                        .append("        }\n")
                        .append("        return ").append(member.getName()).append(";\n")
                        .append("    }\n");

                // Add method for list
                code.append("\n    public void add").append(capitalizedName)
                        .append("(").append(member.getType()).append(" x) {\n")
                        .append("        if (").append(member.getName()).append(" == null) {\n")
                        .append("            ").append(member.getName())
                        .append(" = new ArrayList<>();\n")
                        .append("        }\n")
                        .append("        ").append(member.getName()).append(".add(x);\n")
                        .append("    }\n");
            } else {
                // Regular getter
                code.append("\n    public ").append(member.getType())
                        .append(" get").append(capitalizedName).append("() {\n")
                        .append("        return ").append(member.getName()).append(";\n")
                        .append("    }\n");

                // Regular setter
                code.append("\n    public void set").append(capitalizedName)
                        .append("(").append(member.getType()).append(" value) {\n")
                        .append("        this.").append(member.getName()).append(" = value;\n")
                        .append("    }\n");
            }
        }
    }

    private static String capitalize(String str) { //to make first letter upper-case(Occam style)
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
