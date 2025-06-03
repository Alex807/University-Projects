package org.example;

import org.example.XMLBinderLibrary.BookShopProcessing;
import org.example.XMLBinderLibrary.DotProcessing;
import org.example.XMLBinderLibrary.XMLBinder;
import org.example.outputclasses.dots.*;
import org.example.outputclasses.bookshop.*;

public class Main {
    public static final String DEFAULT_PACKAGE_ROOT = "org.example.outputclasses"; //use "." for paths in Class.forName(path to the src)
    private final String XSDSchemaPath;
    private final String outputDirPath;
    private final String inputXMLFilePath;
    private final String processedObjectType;

    public Main(String inputPath, String outputPath, String inputFile, String objectType) {
        this.XSDSchemaPath = inputPath;
        this.outputDirPath = outputPath;
        this.inputXMLFilePath = inputFile;
        if (objectType.endsWith(".class"))  processedObjectType = objectType.replace(".class", "");
        else processedObjectType = objectType;
    }

    public void writeJavaFilesFromSchema() {
        SchemaToClass generator = new SchemaToClass();
        generator.generateClasses(XSDSchemaPath, outputDirPath);
    }

    public Object XMLToObject() throws Exception {
        Class<?> type;
        String qualifiedClassName = String.format("%s.%s.%s", DEFAULT_PACKAGE_ROOT, processedObjectType.toLowerCase(), processedObjectType);
        try {
            type = Class.forName(qualifiedClassName); //we retain the type of class that we use

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found: " + qualifiedClassName, e);
        }

        return XMLBinder.createObjectFromXML(inputXMLFilePath, type);
    }

    public void ObjectToXML(Object object) throws Exception {
        XMLBinder.createXMLFromObject(object, inputXMLFilePath); //we want to rewrite the input file, so give it as output
    }

    public void printObjectState(Object result) {
        if (result instanceof BookShop bookShop) { //we do a CAST inside if
            BookShopProcessing.printState(bookShop);
        } else if (result instanceof Dots dots) {
            DotProcessing.printState(dots);
        } else {
            System.out.println("Unsupported object type to be printed: " + result.getClass().getName());
        }
    }

    public void modifyObjectState(Object result) {
        if (result instanceof BookShop bookShop) { //we do a CAST inside if
            BookShopProcessing.modifyState(bookShop);
        } else if (result instanceof Dots dots) {
            DotProcessing.modifyState(dots);
        } else {
            System.out.println("Unsupported object type to make modifies: " + result.getClass().getName());
        }
    }


    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: java Main <schema_file_path> <output_directory> <xml_file_path> <Type.class>");
            return;
        }

        Main generator = new Main(args[0], args[1], args[2], args[3]); //2 mains
//        generator.writeJavaFilesFromSchema();

        try {
            Object result = generator.XMLToObject();
            generator.printObjectState(result);
            generator.modifyObjectState(result);
            generator.ObjectToXML(result);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("\nAn exception occurred in Main class with message: " + e.getMessage());
        }
    }
}