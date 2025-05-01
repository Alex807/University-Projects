// requires at least C11
// in Visual Studio it is set from Properties -> C/C++ -> C Language Standard 
#pragma once

#include <stddef.h>
#include <stdbool.h>
#include <stdnoreturn.h>

// prints to stderr a message prefixed with "error: " and exit the program
// the arguments are the same as for printf
noreturn void err(const char *fmt,...);

// allocs memory using malloc
// if succeeds, it returns the allocated memory, else it prints an error message and exit the program
void *safeAlloc(size_t nBytes);

// loads a text file in a dynamically allocated memory and returns it
// on error, prints a message and exit the program
char *loadFile(const char *fileName);

// checks if a file exists and is not a directory
bool isValid(const char *path); 

// returns the absolute path of a file by its name
char* getAbsolutePath(const char *fileName);

// extracts the file name from a path, without the extension 
char* extractFileName(const char *path); 

// removes the extension from a given file name
char* removeExtension(const char *fileName);

// removes the quotes from a string(to can easy input path with "copy as path" from windows) 
void removeQuotes(char *str);

// removes the new line character from a string
void removeNewLine_Character(char *str);

// converts a string to uppercase
void toUpper_String(char *str);

//copies a file from sourcePath to destinationPath(used to make a copy of the source file in the output directory)
int copyFile(const char *sourcePath, const char *destinationPath);
