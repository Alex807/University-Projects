package org.example.XMLBinderLibrary;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

public class XMLBinder {

    public static Object createObjectFromXML(String xmlFilePath, Class<?> targetClass) throws Exception {
        File xmlFile = new File(xmlFilePath);
        if (!xmlFile.canRead() || !xmlFile.exists() || !xmlFile.isFile()) {
            throw new IOException("\nXML file is INVALID, check file-path : " + xmlFile.getAbsolutePath());
        }
        // Create DOM parser to can read input file
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        // Create instance of target class
        Object instance = targetClass.getDeclaredConstructor().newInstance(); //create class with reflexion methods

        // Process root element
        Element rootElement = doc.getDocumentElement();
        processElement(rootElement, instance);

        return instance;
    }

    private static void processElement(Element element, Object instance) throws Exception {
        // First handle attributes of current element
        NamedNodeMap attributes = element.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attr = attributes.item(i);
                String fieldName = attr.getNodeName();
                try {
                    Field field = instance.getClass().getField(fieldName);
                    setFieldValue(field, instance, attr.getNodeValue());
                } catch (NoSuchFieldException e) {
                    System.out.println("Warning: Field " + fieldName + " not found in " + instance.getClass().getName());
                }
            }
        }

        // Then handle child elements
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;
                String fieldName = childElement.getTagName();

                try {
                    Field field = instance.getClass().getField(fieldName);

                    if (List.class.isAssignableFrom(field.getType())) {
                        // Handle List fields
                        processListField(field, instance, childElement);
                    } else if (field.getType().getName().startsWith("org.example")) {
                        // Handle complex types (custom classes)
                        Object childInstance = field.getType().getDeclaredConstructor().newInstance();
                        processElement(childElement, childInstance);
                        field.set(instance, childInstance);
                    } else {
                        // Handle simple types(primitives)
                        setFieldValue(field, instance, childElement.getTextContent());
                    }
                } catch (NoSuchFieldException e) {
                    System.out.println("Warning: Field " + fieldName + " not found in " + instance.getClass().getName());
                }
            }
        }
    }

    private static void processListField(Field field, Object instance, Element element) throws Exception {
        // Get the generic type of the List
        ParameterizedType listType = (ParameterizedType) field.getGenericType();
        Class<?> itemClass = (Class<?>) listType.getActualTypeArguments()[0];

        // Get or create the list
        List<Object> list = (List<Object>) field.get(instance);
        if (list == null) {
            list = new ArrayList<>();
            field.set(instance, list);
        }

        // Create and add the new item
        Object item = itemClass.getDeclaredConstructor().newInstance();
        // Process the element's attributes and children
        processElement(element, item);
        list.add(item);
    }

    private static void setFieldValue(Field field, Object instance, String value) throws Exception {
        // Convert string value to appropriate type
        Object convertedValue = convertValue(value, field.getType());
        field.set(instance, convertedValue);
    }

    private static Object convertValue(String value, Class<?> type) {
        if (type == String.class) return value;
        if (type == int.class || type == Integer.class) return Integer.parseInt(value);
        if (type == float.class || type == Float.class) return Float.parseFloat(value);
        if (type == double.class || type == Double.class) return Double.parseDouble(value);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
        return value;
    }

    public static void createXMLFromObject(Object object, String outputXMLFilePath) throws Exception {
        File outputFile = new File(outputXMLFilePath);
        if (!outputFile.getParentFile().exists()) {
            if (outputFile.getParentFile().mkdirs()) {
                System.out.println("Created successfully the output XML file: " + outputFile.getAbsolutePath());
            }
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        // Create root element based on class name
        Element rootElement = doc.createElement(object.getClass().getSimpleName().toLowerCase());
        doc.appendChild(rootElement);

        // Process object fields
        processObject(object, rootElement, doc);

        // Write to file with formatting set-ups
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        //properties to write XML
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outputFile);
        transformer.transform(source, result);
    }

    private static void processObject(Object object, Element parentElement, Document doc) throws Exception {
        Field[] fields = object.getClass().getFields();

        for (Field field : fields) {
            String fieldName = field.getName();
            Object value = field.get(object);

            if (value != null) {
                if (List.class.isAssignableFrom(field.getType())) {
                    // Handle List fields
                    List<?> list = (List<?>) value;
                    for (Object item : list) {
                        Element element = doc.createElement(fieldName);

                        // Check if this is a Book object and handle its ID
                        if (item.getClass().getSimpleName().equals("Book")) {
                            try {
                                Field idField = item.getClass().getDeclaredField("id");
                                idField.setAccessible(true);
                                Object idValue = idField.get(item);
                                if (idValue != null) {
                                    element.setAttribute("id", idValue.toString());
                                }
                            } catch (NoSuchFieldException e) {
                                System.out.println("Warning: No id field found in Book class");
                            }
                        }

                        processComplexType(item, element, doc);
                        parentElement.appendChild(element);
                    }
                } else if (field.getType().getName().startsWith("org.example")) {
                    // Handle complex types
                    Element element = doc.createElement(fieldName);
                    processComplexType(value, element, doc);
                    parentElement.appendChild(element);
                } else {
                    // Handle simple types as element with text content
                    Element element = doc.createElement(fieldName);
                    element.setTextContent(value.toString());
                    parentElement.appendChild(element);
                }
            }
        }
    }

    private static void processComplexType(Object object, Element element, Document doc) throws Exception {
        Field[] fields = object.getClass().getFields();

        for (Field field : fields) {
            String fieldName = field.getName();
            field.setAccessible(true);
            Object value = field.get(object);

            if (value != null) {
                // Skip ID field for Book class as it's handled as an attribute
                if (object.getClass().getSimpleName().equals("Book") && fieldName.equals("id")) {
                    continue;
                }

                if (List.class.isAssignableFrom(field.getType())) {
                    // Handle nested lists
                    List<?> list = (List<?>) value;
                    for (Object item : list) {
                        Element nestedElement = doc.createElement(fieldName);
                        processComplexType(item, nestedElement, doc);
                        element.appendChild(nestedElement);
                    }
                } else if (field.getType().getName().startsWith("org.example")) {
                    // Handle nested complex types
                    Element nestedElement = doc.createElement(fieldName);
                    processComplexType(value, nestedElement, doc);
                    element.appendChild(nestedElement);
                } else {
                    // Create element with text content
                    Element childElement = doc.createElement(fieldName);
                    childElement.setTextContent(value.toString());
                    element.appendChild(childElement);
                }
            }
        }
    }
}