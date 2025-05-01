package org.example;

import org.example.abstractions.FileAnalyzer;
import org.example.abstractions.UMLFormatter;
import org.example.metadata.ClassInfo;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReverseEngineering {
    public static final String YumlSyntax_OUTUT_PATH = "E://Github//University-Projects//Java_projects//ReflexionPattern//outputSyntax//YUML.txt";
    public static final String Yuml_DIAGRAM_GENERATOR_LINK = "https://yuml.me/diagram/scruffy/class/draw";
    public static final String PlantumlSyntax_OUTPUT_PATH = "E://Github//University-Projects//Java_projects//ReflexionPattern//outputSyntax//PLANTUML.txt";
    public static final String Plantuml_DIAGRAM_GENERATOR_LINK = "https://editor.plantuml.com/uml/";

    private FileAnalyzer analyzer;

    public void setAnalyzer(FileAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    public void analyzeFile(String filePath) throws Exception {
        if (analyzer == null) {
            throw new IllegalStateException("No analyzer set");
        }
        analyzer.analyze(filePath);
        System.out.println(analyzer.getAnalysisResult());
    }

    public static void writeToFile(String content, String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("File " + filePath + " does not exist!\n Make sure you update default paths in ReverseEngineering class.");
            System.exit(1);
        }

        try {
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            try {
                writer.print(content);
            } catch (NullPointerException e) {
                writer.print("Process was ended unsuccessfully!");
                e.printStackTrace();

            } finally {
                writer.close(); //always close opened streams
            }
        } catch (IOException e) {
            System.out.print("An error occurred in parsing file process !! \n");
            e.getStackTrace();
        }
    }

    public static String generateYumlSyntax(FileAnalyzer analyzer, Set<String> ignoredClasses) {
        UMLFormatter yumlFormatter = new YumlFormatter();
        UMLDiagramGenerator yumlDiagramGenerator = new UMLDiagramGenerator(
                yumlFormatter,
                ignoredClasses,
                true,
                true,
                false,
                true
        );
        List<ClassInfo> classes = analyzer.getClassInfoList();
        return yumlDiagramGenerator.generateDiagram(classes);
    }

    public static String generatePlantumlSyntax(FileAnalyzer analyzer, Set<String> ignoredClasses) {
        UMLFormatter plantumlFormatter = new PlantUMLFormatter();
        UMLDiagramGenerator yumlDiagramGenerator = new UMLDiagramGenerator(
                plantumlFormatter,
                ignoredClasses,
                true,
                true,
                false,
                true
        );
        List<ClassInfo> classes = analyzer.getClassInfoList();
        return yumlDiagramGenerator.generateDiagram(classes);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java ReverseEngineeringTool <file-path>");
            return;
        }

        try {
            ReverseEngineering tool = new ReverseEngineering();
            String filePath = args[0];

            if (filePath.endsWith(".jar")) {
                tool.setAnalyzer(new JarFileAnalyzer());
            } else {
                System.out.println("Unsupported file format");
                return;
            }

            tool.analyzeFile(filePath);

            Set<String> ignoredClasses = new HashSet<>(); //configure ignored classes in diagrams
            ignoredClasses.add("java.lang.*");
            ignoredClasses.add("java.util.*");

            String yumlOutput = generateYumlSyntax(tool.analyzer, ignoredClasses);
            writeToFile(yumlOutput, YumlSyntax_OUTUT_PATH);

            String plantumlOutput = generatePlantumlSyntax(tool.analyzer, ignoredClasses);
            writeToFile(plantumlOutput, PlantumlSyntax_OUTPUT_PATH);

            System.out.println("Syntax code was generated successfully for both Yuml and PlantUML sites. \nTry syntax at:");
            System.out.println("YUML: " + Yuml_DIAGRAM_GENERATOR_LINK);
            System.out.println("PlantUML: " + Plantuml_DIAGRAM_GENERATOR_LINK);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}