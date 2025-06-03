package org.example;

import org.example.components.ClassDefinition;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import java.util.Map;
import java.util.Stack;

public class SchemaHandler extends DefaultHandler {
    private final Stack<String> elementStack;
    private final Map<String, ClassDefinition> classDefinitions;
    private ClassDefinition currentClass;
    private boolean insideComplexType = false;

    public SchemaHandler(Stack<String> elementStack, Map<String, ClassDefinition> classDefinitions) {
        this.elementStack = elementStack;
        this.classDefinitions = classDefinitions;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        elementStack.push(qName);

        if (isXSDElement(qName)) {
            handleElement(attributes);
        } else if (isXSDAttribute(qName)) { //attributes and elements are 2 separated tags in input .xsd file
            handleAttribute(attributes);
        } else if (isXSDComplexType(qName)) {
            handleComplexType(attributes);
        }
    }

    private void handleElement(Attributes attributes) {
        String elementName = attributes.getValue("name");
        String elementType = attributes.getValue("type");
        boolean isUnbounded = "unbounded".equals(attributes.getValue("maxOccurs"));

        if (elementName != null) {
            if (insideComplexType && currentClass != null) {
                if (elementType != null) {
                    // Handle element with type reference
                    processElementByType(elementName, elementType, isUnbounded);
                } else {
                    // Create new class for anonymous complex type
                    ClassDefinition nestedClass = new ClassDefinition(capitalize(elementName));
                    classDefinitions.put(elementName, nestedClass);
                    // Add reference to the nested class in current class
                    currentClass.addAttribute(elementName, capitalize(elementName), isUnbounded);
                    currentClass = nestedClass;
                }
            } else if (!insideComplexType) {
                // Root element - create new class
                currentClass = new ClassDefinition(capitalize(elementName));
                classDefinitions.put(elementName, currentClass);
            }
        }
    }

    private void processElementByType(String elementName, String elementType, boolean isUnbounded) {
        if (elementType.contains(":")) { // Handle primitive types
            String primitiveType = convertXSDType(elementType);
            currentClass.addAttribute(elementName, primitiveType, isUnbounded);

        } else { // Handle complex types
            currentClass.addAttribute(elementName, capitalize(elementType), isUnbounded);
        }
    }

    private void handleComplexType(Attributes attributes) { //case when a type contains other class type
        String typeName = attributes.getValue("name");
        insideComplexType = true;

        if (typeName != null) {
            currentClass = new ClassDefinition(capitalize(typeName));
            classDefinitions.put(typeName, currentClass);
        }
    }

    private void handleAttribute(Attributes attributes) {
        if (currentClass != null) {
            String name = attributes.getValue("name");
            String type = attributes.getValue("type");
            if (name != null && type != null) {
                // Attributes are always simple types and cannot be multiple
                currentClass.addAttribute(name, convertXSDType(type), false);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        // check if we're ending an XSD element tag and we have 3 elements in stack(to get parent)
        if (isXSDElement(qName) && elementStack.size() > 2) {
            // get the parent element name from the stack (2 levels up) -> elm. current = size - 1 // def. elem. current = size - 2
            String parentElement = elementStack.get(elementStack.size() - 3);

            // if parent exists in our class definitions, keep the current class the same
            if (classDefinitions.containsKey(parentElement)) {
                currentClass = classDefinitions.get(parentElement);
            }
        }
        // ending a complex type definition
        else if (isXSDComplexType(qName)) {
            // mark that no inside a complex type
            insideComplexType = false;
        }

        // remove the current element
        elementStack.pop();
    }

    private boolean isXSDElement(String qName) {
        return qName.equals("xs:element") || qName.equals("xsd:element");
    }

    private boolean isXSDComplexType(String qName) {
        return qName.equals("xs:complexType") || qName.equals("xsd:complexType");
    }

    private boolean isXSDAttribute(String qName) {
        return (qName.equals("xs:attribute") || qName.equals("xsd:attribute"));
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        if (str.contains(":")) {
            str = str.substring(str.indexOf(":") + 1);
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String convertXSDType(String xsdType) {
        if (xsdType.contains(":")) {
            String type = xsdType.substring(xsdType.indexOf(":") + 1);
            switch (type) {
                case "string":
                    return "String";
                case "integer":
                    return "int";
                case "float":
                    return "float";
                case "double":
                    return "double";
                case "boolean":
                    return "boolean";
                default:
                    return "String";
            }
        }
        return xsdType;
    }
}