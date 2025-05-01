package org.example;

import java.io.*;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonDataSourceProcessor {
    private final ObjectMapper mapper;
    private final Path jsonFilePath;

    public JsonDataSourceProcessor(Path jsonFilePath, ObjectMapper mapper) {
        this.jsonFilePath = jsonFilePath;
        this.mapper = mapper;
        mapper.enable(SerializationFeature.INDENT_OUTPUT); //for pretty print in json file
        mapper.registerModule(new JavaTimeModule());
    }

    public JsonDataSource read() throws IOException {
        JsonDataSource data;
        File f = jsonFilePath.toFile();
        if (f.exists() && f.length() > 0) {
            try (InputStream in = new FileInputStream(f)) {
                data = mapper.readValue(in, JsonDataSource.class); //needs parent object type for what contains json
            }
        } else {
            data = new JsonDataSource();
        }
        return data;
    }

    public void write(JsonDataSource data) throws IOException{
        File f = jsonFilePath.toFile();
        if (!f.exists()) {
            f.getParentFile().mkdirs();
        }
        try (OutputStream out = new FileOutputStream(f)) {
            mapper.writeValue(out, data);
        }
    }
}
