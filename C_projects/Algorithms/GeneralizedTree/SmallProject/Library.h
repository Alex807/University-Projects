#ifndef __LIBRARY_C 
#define __LIBRARY_C 
#include <stdio.h> //used to can return an 'FILE*'

typedef struct Person personType;

FILE* createBinaryFile(char* fileName);
personType* readDataFrom_Stdin(int* index); 
personType* readDataFrom_ResourceFile(int* index, char* resourceFile);
void writeDataTo_BinaryFile(personType* personsArray, int numbersOfPersons, FILE* file);
personType* readDataFrom_BinaryFile(int numbersOfPersons, FILE* binaryFile);
void printArray(personType* personsArray, int numbersOfPersons);
FILE* moveCursorOfFile_AtAnyPosition(FILE* fileName, int positionToStart); 

//-----------------------------------------------------------BinaryTree functions 

typedef struct Node* nodeType;

nodeType createNode(int key, personType personData); 
int searchKey_IfExists(int key, int* keysArray, int numberOfKeys); 
int checkIf_IsInt(char* string); 
int* provideKeysOfNodes(int* numberOfKeys); 
nodeType insert(nodeType root, int key, personType personData); 
nodeType createBinaryTree(int* keysArray, int numberOfKeys, personType* personArray, int numberOfPersons); 
void inOrderTraversal(nodeType root); 
void preOrderTraversal(nodeType root); 
void postOrderTraversal(nodeType root); 
void freeTree(nodeType root); 
nodeType searchNodeByKey(nodeType root, int key); 

#endif