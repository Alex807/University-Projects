package org.example;

import org.example.abstractions.FileAnalyzer;
import org.example.metadata.*;

import java.io.File;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFileAnalyzer implements FileAnalyzer {
    private final List<ClassInfo> classInfoList = new ArrayList<>();

    @Override
    public void analyze(String filePath) throws Exception {
        JarFile jarFile = new JarFile(filePath);
        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{
                        new File(filePath).toURI().toURL()
                });

        for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".class")) {
                String className = entry.getName().replace('/', '.').replace(".class", "");
                try {
                    Class<?> clazz = classLoader.loadClass(className);
                    analyzeClass(clazz);
                } catch (ClassNotFoundException e) {
                    System.err.println("Could not load class: " + className);
                }
            }
        }
        classLoader.close(); //close the stream
        jarFile.close();
    }

    private void analyzeClass(Class<?> clazz) {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassName(clazz.getName());

        // Analyze superclass
        if (clazz.getSuperclass() != null) {
            classInfo.setSuperClassName(clazz.getSuperclass().getName());
        }

        // Analyze interfaces
        Class<?>[] interfaces = clazz.getInterfaces();
        boolean isInterface = clazz.isInterface();
        classInfo.setIsInterface(isInterface);
        List<String> interfaceNames = new ArrayList<>();
        for (Class<?> iface : interfaces) {
            interfaceNames.add(iface.getName());
        }
        classInfo.setInterfaces(interfaceNames);

        // Analyze fields
        List<FieldInfo> fieldInfos = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            FieldInfo fieldInfo = new FieldInfo();
            fieldInfo.setName(field.getName());

            Type type = field.getGenericType();
            if (type instanceof ParameterizedType) { //case for an ArrayList <T>
                fieldInfo.setType(type.getTypeName());

            } else if (field.getType().isArray()) { //case for Array[] of an TYPE
                Class<?> elementsType = field.getType().getComponentType();
                fieldInfo.setType(elementsType.getName() + "[]");

            } else { //Primitive types
                fieldInfo.setType(field.getType().getName());
            }

            fieldInfo.setType(field.getGenericType().getTypeName()); //for parametrized types
            fieldInfo.setModifiers(field.getModifiers());
            fieldInfos.add(fieldInfo);
        }
        classInfo.setFields(fieldInfos);

        // Analyze methods
        List<MethodInfo> methodInfos = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            MethodInfo methodInfo = new MethodInfo();
            methodInfo.setName(method.getName());
            methodInfo.setReturnType(method.getReturnType().getName());

            List<String> paramTypes = new ArrayList<>();
            for (Class<?> paramType : method.getParameterTypes()) {
                paramTypes.add(paramType.getName());
            }
            methodInfo.setParameterTypes(paramTypes);
            methodInfo.setModifiers(method.getModifiers());
            methodInfos.add(methodInfo);
        }
        classInfo.setMethods(methodInfos);

        classInfoList.add(classInfo);
    }

    @Override
    public String getAnalysisResult() {
        StringBuilder result = new StringBuilder();
        for (ClassInfo classInfo : classInfoList) {
            result.append("Class: ").append(classInfo.getClassName()).append("\n");

            String superclassName = classInfo.getSuperClassName();
            result.append("Super Class: ");
            if (superclassName != null && !superclassName.equals("java.lang.Object")) {
                result.append(superclassName);
            }
            result.append("\n");

            result.append("Interfaces: ").append(String.join(", ", classInfo.getInterfaces())).append("\n");

            result.append("Fields:\n");
            for (FieldInfo field : classInfo.getFields()) {
                result.append("\t").append(Modifier.toString(field.getModifiers()))
                        .append(" ").append(field.getType())
                        .append(" ").append(field.getName()).append("\n");
            }

            result.append("Methods:\n");
            for (MethodInfo method : classInfo.getMethods()) {
                result.append("\t").append(Modifier.toString(method.getModifiers()))
                        .append(" ").append(method.getReturnType())
                        .append(" ").append(method.getName())
                        .append("(").append(String.join(", ", method.getParameterTypes())).append(")\n");
            }
            result.append("\n");
        }
        return result.toString();
    }

    public List<ClassInfo> getClassInfoList() {

        return classInfoList;
    }
}
