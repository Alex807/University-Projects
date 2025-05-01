#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <windows.h>
#include <stdbool.h>
#include <sys/stat.h> 
#include <ctype.h>

#include "lexer.h"
#include "utils.h"

void err(const char *fmt,...){
	fprintf(stderr,"[ERROR] ");
	va_list va;
	va_start(va,fmt);
	vfprintf(stderr,fmt,va);
	va_end(va);
	fprintf(stderr,"\n");
	exit(EXIT_FAILURE);
}

void *safeAlloc(size_t nBytes){
	void *p=malloc(nBytes);
	if(!p) err("not enough memory");
	return p;
}

char *loadFile(const char *fileName){
	FILE *fis=fopen(fileName,"rb");
	if(!fis)err("unable to open %s",fileName);
	
	fseek(fis,0,SEEK_END);
	size_t n=(size_t)ftell(fis);
	fseek(fis,0,SEEK_SET);
	char *buf=(char*)safeAlloc((size_t)n+1);
	size_t nRead=fread(buf,sizeof(char),(size_t)n,fis);
	fclose(fis);
	if(n!=nRead)err("cannot read all the content of %s",fileName);
	buf[n]='\0';
	
	return buf;
}

// Function to check if a file exists and is not a directory
bool isValid(const char *path) {
    struct stat path_stat;
    return (stat(path, &path_stat) == 0 && S_ISREG(path_stat.st_mode));
}

char* getAbsolutePath(const char *fileName) {
    char *absolutePath = (char*)malloc(MAX_PATH_SIZE);
    if (!absolutePath) {
        fprintf(stderr, "Memory allocation error\n");
        exit(EXIT_FAILURE);
    }
    
    DWORD result = GetFullPathNameA(fileName, MAX_PATH_SIZE, absolutePath, NULL);
    if (result == 0 || result >= MAX_PATH_SIZE) {
        free(absolutePath);
        return NULL;
    }
    
    return absolutePath;
}

char* extractFileName(const char *absolutePath) {
    char *baseFilename = (char*)malloc(MAX_PATH_SIZE);
    if (!baseFilename) {
        fprintf(stderr, "Memory allocation error in 'extractFileName' function!\n");
        exit(EXIT_FAILURE);
    }
    
    // Find the last slash or backslash
    const char *lastSlash = strrchr(absolutePath, '\\');
    if (!lastSlash) {
        lastSlash = strrchr(absolutePath, '/');
    }
    
    // Get the filename part
    const char *filenameOnly = lastSlash ? lastSlash + 1 : absolutePath;
    strcpy(baseFilename, filenameOnly);
    
    return baseFilename;
}

char* removeExtension(const char *fileName) {
    char *baseFilename = (char*)malloc(MAX_PATH_SIZE);
    if (!baseFilename) {
        fprintf(stderr, "Memory allocation error  in 'removeExtension' function!\n");
        exit(EXIT_FAILURE);
    }

    strcpy(baseFilename, fileName);
    char *quote = strrchr(baseFilename, '.');
    if (quote) {
        *quote = '\0';
    }
    return baseFilename;
}

void removeQuotes(char* filePath) {
    if (!filePath) return;
    
    size_t len = strlen(filePath);
    if (len > 2 && filePath[0] == '\"' && filePath[len-1] == '\"') {
        // Shift everything left by 1 position to remove the first quote
        for (size_t i = 0; i < len - 1; i++) {
            filePath[i] = filePath[i + 1];
        }
        // Remove the last quote by setting the new end to null terminator
        filePath[len - 2] = '\0';
    }
}

void toUpper_String(char *str) {
    char *p = str;
    while (*p) {
        *p = toupper((unsigned char)*p);
        p++;
    }
}

void removeNewLine_Character(char *str) {
    size_t len = strlen(str);
    if (len > 0 && str[len-1] == '\n') {
        str[len-1] = '\0';
    }
}

int copyFile(const char *sourcePath, const char *destinationPath) {
    FILE *sourceFile, *destFile;
    char buffer[4096];
    size_t bytesRead;
    
    // Open source file for reading
    sourceFile = fopen(sourcePath, "rb");
    if (sourceFile == NULL) {
        return -1; // Source file couldn't be opened
    }
    
    // Open destination file for writing
    destFile = fopen(destinationPath, "wb");
    if (destFile == NULL) {
        fclose(sourceFile);
        return -2; // Destination file couldn't be opened
    }
    
    // Copy file contents in chunks
    while ((bytesRead = fread(buffer, 1, sizeof(buffer), sourceFile)) > 0) {
        if (fwrite(buffer, 1, bytesRead, destFile) != bytesRead) {
            fclose(sourceFile);
            fclose(destFile);
            return -3; // Write error
        }
    }
    
    // Close files
    fclose(sourceFile);
    fclose(destFile);
    
    return 0; // Success
}
