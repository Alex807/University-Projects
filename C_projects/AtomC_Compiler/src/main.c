#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <stdbool.h>
#include <windows.h>

#include "lexer.h"
#include "utils.h" 
#include "parser.h"

#define SOURCE_FILE_NAME "testParser.c" 
#define SOURCE_FILE_PATH "E:\\LFTC\\Sintactic-Analyzer\\tests\\TestParser\\testParser.c"

void printBanner() {
    printf("\n");
    printf("\t\t\t\t\t+-------------------------------------------------------+\n");
    printf("\t\t\t\t\t|                                                       |\n");
    printf("\t\t\t\t\t|                    LEXICAL ANALYZER                   |\n");
    printf("\t\t\t\t\t|                                                       |\n");
    printf("\t\t\t\t\t+-------------------------------------------------------+\n");
    printf("\n");
}

int main() {
    // char* fileName = malloc(MAX_FILENAME_SIZE); 
    // char* filePath = malloc(MAX_PATH_SIZE);
    // bool keepExecute = true;
    // char option[10];

    // if (!fileName || !filePath) {
    //     fprintf(stderr, "Memory allocation error!\n");
    //     return EXIT_FAILURE;
    // }
    
    printBanner();
    
    // while (keepExecute) {
    //     printf("  How would you like to provide the source file?\n");
    //     printf("[P] Absolute Path / [N] File Name ");
    //     printf("(P / N)? "); 
        
    //     if (fgets(option, sizeof(option), stdin) == NULL) {
    //         fprintf(stderr, "  [ERROR] Failed to read input!\n");
    //         return EXIT_FAILURE;
    //     }
        
    //     removeNewLine_Character(option);
    //     toUpper_String(option);
        
    //     if (strcmp(option, "P") == 0 || strcmp(option, "PATH") == 0) {
    //         printf("\nEnter absolute path to source file:\n");
    //         printf("  > ");
    //         if (fgets(filePath, MAX_PATH_SIZE, stdin) == NULL) {
    //             fprintf(stderr, "[ERROR] Failed to read input!\n");
    //             continue;
    //         }
            
    //         removeNewLine_Character(filePath); 
    //         removeQuotes(filePath);
            
    //         if (strlen(filePath) == 0) { 
    //             printf("[ERROR] Path cannot be empty. Please try again.\n\n");
    //             continue;
    //         }
            
    //         if (!isValid(filePath)) {
    //             printf("[ERROR] Invalid path: %s\n", filePath);
    //             printf("!! Make sure the file exists and is NOT a directory !!\n\n");
    //             continue;
    //         }
            
    //         fileName = extractFileName(filePath);
    //         keepExecute = false;
            
    //     } else if (strcmp(option, "N") == 0 || strcmp(option, "NAME") == 0) {
    //         printf("\nEnter file name:\n");
    //         printf("  > ");
    //         if (fgets(fileName, MAX_PATH_SIZE, stdin) == NULL) {
    //             fprintf(stderr, "[ERROR] Failed to read input!\n");
    //             continue;
    //         }
            
    //         removeNewLine_Character(fileName);
            
    //         if (strlen(fileName) == 0) {
    //             printf("[ERROR] Filename cannot be empty. Please try again.\n\n");
    //             continue;
    //         }
            
    //         if (!isValid(fileName)) {
    //             printf("[ERROR] File with path: ./%s can NOT be found!!\n", fileName);
    //             printf("!! Make sure file HAS EXTENSION and is located in WORKING DIR !!\n\n");
    //             continue;
    //         }
            
    //         filePath = getAbsolutePath(fileName); //obtains only the relative path, not searching for the file in all computer
    //         fileName = extractFileName(filePath);
    //         keepExecute = false;

    //     } else {
    //         printf("[ERROR] Invalid option. Please enter 'P' or 'N'.\n\n");
    //         continue;
    //     }
    // }
    
    // if (!filePath) {
    //     fprintf(stderr, "[ERROR] Failed to get absolute path\n");
    //     return EXIT_FAILURE;
    // }
    printf("\n[INFO] File found at path: %s\n", SOURCE_FILE_PATH);
    
    printf("[INFO] Processing file: %s\n\n", SOURCE_FILE_NAME);
    
    // Load the file
    char *source = loadFile(SOURCE_FILE_PATH);
    // if (!source) {
    //     fprintf(stderr, "[ERROR] Failed to load file content\n");
    //     free(fileName);
    //     free(filePath);
    //     return EXIT_FAILURE;
    // }
    
    printf("[INFO] Starting lexical analyzer...\n");
    
    // Tokenize the source
    printf("[INFO] Tokenizing source file...\n");
    Token *tokens = tokenize(source);
    // if (!tokens) {
    //     fprintf(stderr, "[ERROR] Failed to tokenize source code\n");
    //     free(source);
    //     free(fileName);
    //     free(filePath);
    //     return EXIT_FAILURE;
    // }
    
    //Print tokens to can see them
    // printf("[INFO] Printing tokens...\n\n"); 
    //printTokens(tokens);

    // Export the tokens
    printf("[INFO] Exporting tokens to output directory: ./output/%s\n", removeExtension(SOURCE_FILE_NAME));
    exportTokens(tokens, SOURCE_FILE_NAME, SOURCE_FILE_PATH);
    printf("[SUCCESS] Lexical analysis done successfully!\n\n");


    //Sintactic Analizer
    printf("[INFO] Starting sintactic analyzer...\n"); //Parse the tokens(Sintactic analysis)
    parse(tokens);
    
    printf("\n[INFO] Cleaning up...\n"); //Clean up
    free(source);
    free(tokens);
    // free(fileName);
    // free(filePath);
    printf("[SUCCESS] Cleaning done successfully!\n\n");

    printf("[SUCCESS] Exit-Code: %d\n", EXIT_SUCCESS);
    return EXIT_SUCCESS;
}