#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h> //used for checkIf_IsInt function

#include "Library.h"

#define MAX_NAME_LENGTH 100
#define BINARYFILE_NAME "binaryfile.bin"  
#define COUNT 5

struct Person{
    char name[MAX_NAME_LENGTH];
    int age;
    float note;
}; 

FILE* createBinaryFile(char* fileName) {
    FILE* file; 
    if ((file = fopen(fileName, "wb+")) == NULL) { 
        printf("Error at creating a binary file!\n"); 
        exit(-1); 
    } 
    fseek(file, 0, SEEK_SET); //set cursor at initial position

    return file;
}

personType* readDataFrom_Stdin(int* index) {
    personType* personsArray = NULL; //initialize array 
    if ( (personsArray = calloc(1, sizeof(personType))) == NULL) {  //used calloc instead of malloc for be sure memory is cleared before to be alocated for personData_Array
        printf("Error at inital allocating memory for personData_Array!\n"); 
        exit(-1); 
    }  

    printf("\t!FORMAT CITIRE!   ---->  STOP(ctrl + z + enter) \nNume(string), Varsta(int), Nota(float)\n1. "); 
    personType currentPerson; 
    char line[200]; //used for reading entire line from stdin 

    while ( (fgets(line, 200, stdin)) != NULL) { 
        if ( (sscanf(line, "%[^,], %d, %f\n", currentPerson.name, &currentPerson.age, &currentPerson.note)) != 3) { //format wasn't respected
            printf("Format of a line wasn't respected!\n"); 
            printf("%d. ", *index + 1); 
            continue; //used for skip the current person and read the next one
        }

        personsArray[*index] = currentPerson; 
        (*index)++; 

        //resize the array for the next person
        if ( (personsArray = realloc(personsArray, ((*index) + 1) * sizeof(personType))) == NULL) { //used realloc for each new person readed from resource file
            printf("Error at reallocating memory for %d person!\n", *index);  
            free(personsArray); //free the memory if error is occured
            exit(-1); 
        }
        printf("%d. ", *index + 1);
    }
    return personsArray;
}

personType* readDataFrom_ResourceFile(int* index, char* resourceFile) { 
    FILE* file; 
    if ((file = fopen(resourceFile, "r")) == NULL) { 
        printf("Error at opening resource file!\n"); 
        exit(-1); 
    }

    personType* personsArray = NULL; //initialize array 
    if ( (personsArray = calloc(1, sizeof(personType))) == NULL) {  //used calloc instead of malloc for be sure memory is cleared before to be alocated for personData_Array
        printf("Error at inital allocating memory for personData_Array!\n"); 
        exit(-1); 
    }
     
    personType currentPerson; 
    while ( (fscanf(file, "%[^,], %d, %f\n", currentPerson.name, &currentPerson.age, &currentPerson.note)) != EOF) { //read from resource file each line
        personsArray[*index] = currentPerson; 
        (*index)++; 

        //resize the array for the next person
        if ( (personsArray = realloc(personsArray, ((*index) + 1) * sizeof(personType))) == NULL) { //used realloc for each new person readed from resource file
            printf("Error at reallocating memory for %d person!\n", *index);  
            free(personsArray); //free the memory if error is occured
            exit(-1); 
        }
    } 

    fclose(file);
    return personsArray;
} 

void writeDataTo_BinaryFile(personType* personsArray, int numbersOfPersons, FILE* file) {
    if (file == NULL) { 
        printf("Recieved binary file for writing is NULL!\n"); 
        return; 
    }

    if ( (fwrite(personsArray, sizeof(personType), numbersOfPersons, file)) != numbersOfPersons) { //write all persons from array in binary file
        printf("Error at writing in binary file!\n"); 
        exit(-1); 
    }
    fseek(file, 0, SEEK_SET); //set cursor at initial position

}

personType* readDataFrom_BinaryFile(int numbersOfPersons, FILE* binaryFile) { 
    if (binaryFile == NULL) { 
        printf("Recieved binary file for reading is NULL!\n"); 
        return NULL; //we set an empty array if file is not found 
    }

    personType* personsArray = NULL; //initialize array 
    if ( (personsArray = calloc(numbersOfPersons, sizeof(personType))) == NULL) { 
        printf("Error at inital allocating memory for personData_Array!\n"); 
        return NULL; 
    }

    if ( (fread(personsArray, sizeof(personType), numbersOfPersons, binaryFile)) != numbersOfPersons) { //read all persons from binary file
        printf("Error at reading from binary file!\n");   
        free(personsArray); //free the memory if error is occured
        return NULL; 
    }

    fseek(binaryFile, 0, SEEK_SET); //set cursor at initial position

    return personsArray;
}

void printArray(personType* personsArray, int numbersOfPersons) { 
    printf("\n\tTabel persoane\n");
    
    for (int i = 0; i < numbersOfPersons; i++) {
        printf("Nume: %s, Varsta: %d, Nota: %.2f\n", personsArray[i].name, personsArray[i].age, personsArray[i].note);
    }
}

FILE* moveCursorOfFile_AtAnyPosition(FILE* file, int positionToStart) { 
    if (file == NULL) {
        printf("Recieved binary file for moving cursor is NULL!\n"); 
        return NULL;
    } 

    fseek(file, positionToStart * sizeof(personType), SEEK_SET); //move the cursor at the X person from fileName 
    return file;
} 

//-------------------------------------------------------------------------------------------BinaryTree functions 

struct Node {
    int key; 
    personType personData;
    struct Node* left;
    struct Node* right;
}; 

nodeType createNode(int key, personType personData) {
    nodeType newNode = (nodeType)malloc(sizeof(struct Node));
    if (newNode == NULL) {
        printf("Memory allocation failed to create a node!\n");
        exit(EXIT_FAILURE);
    }
    newNode->key = key;
    newNode->left = NULL; 
    newNode->right = NULL; 
    newNode->personData = personData;

    return newNode;
} 

int searchKey_IfExists(int key, int* keysArray, int numberOfKeys) { 
    for (int index = 0; index < numberOfKeys; index++) { //use linear search because we don't have a sorted array
        if (keysArray[index] == key) { 
            return 0;        //stop searching 
        }
    }
    return 1; 
}

int checkIf_IsInt(char* string) { 
    for (int index = 0; index < strlen(string)-1; index++) { //check if the string is a number
        if (isdigit(string[index]) == 0) { 
            return 0;
        }
    }
    return 1; 
}

int* provideKeysOfNodes(int* numberOfKeys) { 
    int* keysArray = NULL; 
    if ( (keysArray = calloc(1, sizeof(int))) == NULL) { 
        printf("Error at allocating memory for first key !!\n"); 
        free(keysArray); //dealocate for some corupted memory
        return NULL;
    }

    int counter = 0; //use counter to don't use pointer for counting keys
    printf("\n(BINARY TREE)   ONLY TYPE 'INT' KEYS !!  ---->  STOP(CTRL + Z + Enter)\nVal. cheie %d: ", counter+1);
    
    char numberAsString[50]; //read number in string format after check if it is an int  
    while ((fgets(numberAsString, 50, stdin)) != NULL) { //used to read until NULL from user

        if ( (checkIf_IsInt(numberAsString)) == 0) { //check if the number is an int
            printf("Invalid input, isn't an INT !!\nVal. cheie %d: ", counter+1);
            continue;
        } 

        int currentKey = atoi(numberAsString);
        if (searchKey_IfExists(currentKey, keysArray, counter) == 0) { //check if key already exists
            printf("Key already exists in this binary tree !!\nVal. cheie %d: ", counter+1);
            continue;
        }

        keysArray[counter] = currentKey; 
        counter++; 
         
        if ( (keysArray = realloc(keysArray, sizeof(nodeType) * (counter+1)) ) == NULL) { //resize the array for new keys
            printf("Error at reallocating memory for %d key !!\n", counter);  
            free(keysArray); //free the memory if error is occured
            exit(-1); 
        } 
        printf ("Val. cheie %d: ", counter+1); 
    }
    *numberOfKeys = counter; 

    return keysArray;
}


nodeType insert(nodeType root, int key, personType personData) {
    if (root == NULL) {//case when we recieved an empty tree
        return createNode(key, personData);
    }

    if (key < root->key) { //////////////////////////////////////////////////////////////AICI VEZI DUPA CE ORDONEZI ARBORELE/////
        root->left = insert(root->left, key, personData);
    } else if (key > root->key) {
        root->right = insert(root->right, key, personData);
    }

    return root;
} 

nodeType createBinaryTree(int* keysArray, int numberOfKeys, personType* personArray, int numberOfPersons) {  
    //we create nodes equal to minimum size of an array, either keys or persons 
    int treeSize = 0; //numberOfKeys have priority than numberOfPersons
    if (numberOfKeys == numberOfPersons) { 
        treeSize = numberOfKeys;

    } else { 
        treeSize = (numberOfKeys < numberOfPersons)? numberOfKeys : numberOfPersons; //create a tree with all nodes assigned a personData(minimum size)
    }


    if (treeSize == 0) { 
        printf("Recieved size for binaryTree is 0, can not be created !!\n"); 
        return NULL;
    }

    nodeType root = NULL;
    for (int index = 0; index < treeSize; index++) {
        root = insert(root, keysArray[index], personArray[index]);
    }

    return root;
}

void inOrderTraversal(nodeType root) {
    if (root != NULL) {
        inOrderTraversal(root->left);
        printf("%d ", root->key);
        inOrderTraversal(root->right);
    }
} 

void preOrderTraversal(nodeType root) {
    if (root != NULL) {
        printf("%d ", root->key);
        preOrderTraversal(root->left);
        preOrderTraversal(root->right);
    }
} 

void postOrderTraversal(nodeType root) {
    if (root != NULL) {
        postOrderTraversal(root->left);
        postOrderTraversal(root->right);
        printf("%d ", root->key);
    }
}   

void freeTree(nodeType root) {
    if (root != NULL) {
        freeTree(root->left);
        freeTree(root->right);
        free(root);
    }
} 

nodeType searchNodeByKey(nodeType root, int key) {
    if (root == NULL) {
        return NULL;
    }
    if (root->key == key) {
        return root;
    }
    if (key < root->key) {
        return searchNodeByKey(root->left, key);
    } else {
        return searchNodeByKey(root->right, key);
    }
}
