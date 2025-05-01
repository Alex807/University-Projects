package org.example.abstractions;

import org.example.metadata.ClassInfo;

import java.util.List;

// Base interface for file analyzers
public interface FileAnalyzer {
    void analyze(String filePath) throws Exception;
    String getAnalysisResult();
    List<ClassInfo> getClassInfoList();
}
