package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

//import com.fasterxml.jackson.annotation.JsonProperty;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import java.io.File;
//import java.io.IOException;
//import java.util.List;
//
//public class Main {
//    // Person class to map JSON data
//    static class Person {
//        @JsonProperty("name")
//        private String name;
//        @JsonProperty("value")
//        private double value;
//
//        // Getters and setters
//        public String getName() { return name; }
//        public void setName(String name) { this.name = name; }
//        public double getValue() { return value; }
//        public void setValue(double value) { this.value = value; }
//
//        @Override
//        public String toString() {
//            return "Person{name='" + name + "', value=" + value + "}";
//        }
//    }
//
//    public static void main(String[] args) throws JsonProcessingException {
//        ObjectMapper mapper = new ObjectMapper();
//        // to pretty print
//        mapper.enable(SerializationFeature.INDENT_OUTPUT);
//        Person p = new Person();
//        p.setName("Ionica");
//        String personAsJson = mapper.writeValueAsString(p);
//        Person p1 = mapper.readValue(personAsJson, Person.class);
//        try {
//            // Read JSON file into List of Person objects
//            List<Person> persons = mapper.readValue(
//                    new File("E:\\JOB\\persons.json"),
//                    mapper.getTypeFactory().constructCollectionType(List.class, Person.class)
//            );
//
//            // Print all persons
//            persons.forEach(System.out::println);
//
//            persons.getFirst().setValue(0.00);
//            persons.getLast().setName("MARCEL");
//
//           // Write back to a new JSON file
//            mapper.writeValue(new File("E:\\JOB\\persons_updated.json"), persons);
//            System.out.println("\nUpdated data has been written to persons_updated.json");
//
//            // Read JSON file into List of Person objects
//            List<Person> persons1 = mapper.readValue(
//                    new File("E:\\JOB\\persons_updated.json"),
//                    mapper.getTypeFactory().constructCollectionType(List.class, Person.class)
//            );
//
//            // Print all persons
//            persons1.forEach(System.out::println);
//
//        } catch (IOException e) {
//            System.err.println("Error reading the file: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//}
public class BankApplication {
    public static void main(String[] args) throws IOException {
        BankApplicationLogic main = new BankApplicationLogic();
        main.run();
    }
}
