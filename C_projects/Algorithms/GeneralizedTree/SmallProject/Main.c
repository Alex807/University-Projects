#include <stdio.h>
#include <stdlib.h>
#include <string.h> 
#include "Library.h"

#define RESOURCEFILE_NAME "resources.txt"
#define BINARYFILE_NAME "binaryfile.bin" 

int main(void) { 
    int numberOfPersons = 0; //inital is 0 because no person was readed from resource file
    personType* personsArray = readDataFrom_ResourceFile(&numberOfPersons, RESOURCEFILE_NAME);
    //personType* personsArray = readDataFrom_Stdin(&numberOfPersons);

    FILE* binaryFile = createBinaryFile(BINARYFILE_NAME); 
    writeDataTo_BinaryFile(personsArray, numberOfPersons, binaryFile); 

    int positionToStart = 356; //the position from where to start reading from binary file
    binaryFile = moveCursorOfFile_AtAnyPosition(binaryFile, positionToStart); //an implementation of fseek function  
    numberOfPersons -= positionToStart; //actualized size of persons
    personType* personsFrom_BinaryFile = readDataFrom_BinaryFile(numberOfPersons, binaryFile); //if we use 'moveCursorOfFile_AtAnyPosition' function we read (numersOfPersons - positionToStart) persons from binary file

    //personType* personsFrom_BinaryFile = readDataFrom_BinaryFile(numberOfPersons, binaryFile);
    printArray(personsFrom_BinaryFile, numberOfPersons); 

//----------------------------------binary tree part of Main

    //first key is ALWAYS the root
    int numberOfKeys = 0;
    int* arrayKeys = provideKeysOfNodes(&numberOfKeys);

    nodeType root = createBinaryTree(arrayKeys, numberOfKeys, personsFrom_BinaryFile, numberOfPersons); 

    printf("\nINORDER:  ");
    inOrderTraversal(root);
    printf("\n"); 

    printf("PREORDER: ");
    preOrderTraversal(root);
    printf("\n");

    printf("POSTORDER:"); 
    postOrderTraversal(root);
    printf("\n");

    int searchedNode;
    printf("\nCheie cautata in arbore: "); 
    scanf("%d", &searchedNode);

    if (searchNodeByKey(root, searchedNode) != NULL) {
        printf("Node with key %d exists in the tree!\n", searchedNode);
    } else {
        printf("Node with key %d doesn't exist in the tree!\n", searchedNode);
    }

    free(personsArray);
    free(personsFrom_BinaryFile);
    fclose(binaryFile);

    freeTree(root); 
    free(arrayKeys); 
    return 0;
}
