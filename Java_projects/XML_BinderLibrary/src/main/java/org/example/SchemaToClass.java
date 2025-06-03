package org.example;

import org.example.components.ClassDefinition;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SchemaToClass {
    private final Stack<String> elementStack = new Stack<>();
    private final Map<String, ClassDefinition> classDefinitions = new HashMap<>();

    private File validateOutputPath(String path) {
        // Create output directory if it doesn't exist
        File outputDir = new File(path);
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) { //if the dir does not exist, we create it
                System.err.println("Failed to create output directory: " + outputDir);
            }
        }
        return outputDir;
    }

    public void generateClasses(String schemaFile, String outputDirPath) {
        File outputDir = validateOutputPath(outputDirPath);

        try {
            // Copy schema .xsd(input) to output directory
            File sourceSchema = new File(schemaFile);
            File destSchema = new File(outputDir, sourceSchema.getName()); //represent the copy of the input, not the dir itself
            if (!destSchema.exists()) { //if NOT exists a copy of schema
                java.nio.file.Files.copy(sourceSchema.toPath(), destSchema.toPath()); //copy the .xsd input to the output dir
            }

            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(sourceSchema, new SchemaHandler(elementStack, classDefinitions));

            //Package for generated .java files
            String schema = sourceSchema.getName();
            String schemaName = schema.substring(0, schema.indexOf(".")); //to remove .xsd extension

            // Generate Java files
            for (ClassDefinition classDef : classDefinitions.values()) {
                JavaFileGenerator.generateJavaFile(classDef, outputDir.getAbsolutePath(), schemaName);
            }
            System.out.println("Classes generated successfully in: " + outputDir.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error in 'generateClasses' method: " + e.getMessage());
        }
    }
}