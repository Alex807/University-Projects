#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <sys/types.h>  
#include <sys/stat.h>   
#include <io.h>

#include "lexer.h"
#include "utils.h"

// Variabile globale
Token *tokens;   // Lista simplu înlănțuită de tokeni
Token *lastTk;   // Ultimul token din listă
int line = 1;    // Linia curentă din fișierul sursă

// Adaugă un token la sfârșitul listei și returnează un pointer la acesta
Token *addTk(int code) {
    Token *tk = safeAlloc(sizeof(Token));
    tk->code = code;
    tk->line = line;
    tk->next = NULL;
    if (lastTk) {
        lastTk->next = tk;
    } else {
        tokens = tk;
    }
    lastTk = tk;
    return tk;
}

char *extract(const char *begin, const char *end) { // Adăugat
    size_t length = end - begin; // Calculăm lungimea subșirului
    char *result = (char *)safeAlloc(length + 1); // Alocăm memorie pentru subșir (+1 pentru '\0')
    strncpy(result, begin, length); // Copiem caracterele din intervalul [begin, end)
    result[length] = '\0'; // Adăugăm terminatorul de șir
    return result; // Returnăm subșirul
}

Token *tokenize(const char *pch) {
    const char *start;
    Token *tk;

    for (;;) {
        switch (*pch) {
            // we skip whitespaces and count lines
            case ' ': case '\t': case '\r': case '\n':
                if (*pch == '\n') line++;
                pch++;
                break;

            case '\0':
                addTk(END);
                return tokens;

            // Delimitators 
            case ',':
                addTk(COMMA);
                pch++;
                break;
            case ';':
                addTk(SEMICOLON);
                pch++;
                break;
            case '(':
                addTk(LPAR);
                pch++;
                break;
            case ')':
                addTk(RPAR);
                pch++;
                break;
            case '{':
                addTk(LACC);
                pch++;
                break;
            case '}':
                addTk(RACC);
                pch++;
                break;
            case '[':
                addTk(LBRACKET);
                pch++;
                break;
            case ']':
                addTk(RBRACKET);
                pch++;
                break;

            // Operators
            case '+':
                addTk(ADD);
                pch++;
                break;
            case '-':
                addTk(SUB);
                pch++;
                break;
            case '*':
                addTk(MUL);
                pch++;
                break;
            case '/':
                if (pch[1] == '/') { // one line comment
                    pch += 2;
                    while (*pch != '\n' && *pch != '\0') pch++;
                } else if (pch[1] == '*') { // multi line comment
                    pch += 2;
                    while (!(*pch == '*' && pch[1] == '/') && *pch != '\0') {
                        if (*pch == '\n') line++;
                        pch++;
                    }
                    if (*pch == '\0') {
                        err("Unterminated comment");
                    }
                    pch += 2; // skip the closing */
                } else { // Operator aritmetic
                    addTk(DIV);
                    pch++;
                }
                break;
            case '.': // doubles like .5 (means 0.5)
                if (isdigit(pch[1])) { 
                    start = pch;
                    pch++; // move past the dot
                    while (isdigit(*pch)) pch++;
                    if (*pch == 'e' || *pch == 'E') { // exponent
                        pch++;
                        if (*pch == '+' || *pch == '-') {
                            pch++; // move past the sign
                            if (!isdigit(*pch)) {
                                err("Invalid double: missing digits after exponent sign");
                            }
                        } else if (!isdigit(*pch)) {
                            err("Invalid double: missing digits in exponent");
                        }
                        while (isdigit(*pch)) pch++;
                    }
                    char *text = extract(start, pch);
                    tk = addTk(CONST_DOUBLE);
                    tk->text = text;
                    tk->d = atof(text); // atof will correctly convert .X to 0.X
                
                } else if (isalpha(pch[1]) || pch[1] == '_') { // Handle struct field access
                    addTk(DOT);
                    pch++;
                } else { // Invalid usage of dot
                    err("Invalid character: %c (%d)", *pch, *pch);
                }
                break;
            case '=':
                if (pch[1] == '=') {
                    addTk(EQUAL);
                    pch += 2;
                } else {
                    addTk(ASSIGN);
                    pch++;
                }
                break;
            case '!':
                if (pch[1] == '=') {
                    addTk(NOTEQ);
                    pch += 2;
                } else {
                    addTk(NOT);
                    pch++;
                }
                break;
            case '<':
                if (pch[1] == '=') {
                    addTk(LESSEQ);
                    pch += 2;
                } else {
                    addTk(LESS);
                    pch++;
                }
                break;
            case '>':
                if (pch[1] == '=') {
                    addTk(GREATEREQ);
                    pch += 2;
                } else {
                    addTk(GREATER);
                    pch++;
                }
                break;
            case '&':
                if (pch[1] == '&') { // logic AND operator
                    addTk(AND);
                    pch += 2;
                } else {
                    err("Invalid character: %c (%d)", *pch, *pch);
                }
                break;
            case '|':
                if (pch[1] == '|') { // logic OR operator
                    addTk(OR);
                    pch += 2;
                } else {
                    err("Invalid character: %c (%d)", *pch, *pch);
                }
                break;

            // string
            case '"':
                start = ++pch;
                while (*pch != '"' && *pch != '\0') {
                    if (*pch == '\n') line++;
                    pch++;
                }
                if (*pch == '\0') {
                    err("Unterminated string: missing closing '\"'");
                }
                char *string = extract(start, pch);
                tk = addTk(STRING);
                tk->text = string;
                pch++;
                break;

            // character
            case '\'':
                start = ++pch;
                if (*pch == '\0' || pch[1] != '\'') {
                    err("Invalid character constant: missing closing '\'' or too many/less characters");
                }
                char c = *pch;
                tk = addTk(CONST_CHAR);
                tk->i = c;
                pch += 2;
                break;

            default:
                if (isalpha(*pch) || *pch == '_') { // identifiers and keywords
                    start = pch++;
                    while (isalnum(*pch) || *pch == '_') pch++;
                    char *text = extract(start, pch);
                    if (strcmp(text, "char") == 0) addTk(TYPE_CHAR);
                    else if (strcmp(text, "double") == 0) addTk(TYPE_DOUBLE);
                    else if (strcmp(text, "else") == 0) addTk(ELSE);
                    else if (strcmp(text, "if") == 0) addTk(IF);
                    else if (strcmp(text, "int") == 0) addTk(TYPE_INT);
                    else if (strcmp(text, "return") == 0) addTk(RETURN);
                    else if (strcmp(text, "struct") == 0) addTk(STRUCT);
                    else if (strcmp(text, "void") == 0) addTk(TYPE_VOID);
                    else if (strcmp(text, "while") == 0) addTk(WHILE);
                    else {
                        tk = addTk(ID);
                        tk->text = text;
                    }
                } else if (isdigit(*pch)) { //constants
                    start = pch;
                    while (isdigit(*pch)) pch++; // read all digits
                    if (*pch == '.') { 
                        pch++;
                        if (!isdigit(*pch)) {
                            err("Invalid double: missing digits after '.'");
                        }
                        while (isdigit(*pch)) pch++;
                        if (*pch == 'e' || *pch == 'E') { // exponent
                            pch++;
                            if (*pch == '+' || *pch == '-') { pch++; // sign is optional
                                pch ++; // move past the sign
                                // Check if there's a digit after the sign
                                if (!isdigit(*pch)) {
                                    err("Invalid double: missing digits after exponent sign");
                                }
                            } else if (!isdigit(*pch)) { // No sign, but still need a digit
                                err("Invalid double: missing digits in exponent");
                            }
                        }
                        while (isdigit(*pch)) pch++;
                        char *text = extract(start, pch);
                        tk = addTk(CONST_DOUBLE);
                        tk->text = text; // keep the text representation
                        tk->d = atof(text); // convert to numeric value

                    } else if (*pch == 'e' || *pch == 'E') { // exponent for double
                        pch++;
                        if (*pch == '+' || *pch == '-') {
                            pch++; // move past the sign
                            if (!isdigit(*pch)) {
                                err("Invalid double: missing digits after exponent sign");
                            }
                            while (isdigit(*pch)) pch++; // Process digits after sign
                        } else if (!isdigit(*pch)) { // No sign, but still need a digit
                            err("Invalid double: missing digits in exponent");
                        } else {
                            while (isdigit(*pch)) pch++; // Process digits (no sign case)
                        }
                        char *text = extract(start, pch);
                        tk = addTk(CONST_DOUBLE);
                        tk->text = text;
                        tk->d = atof(text); // convert to numeric value

                    } else { // integer
                        if (isalpha(*pch)) {
                            err("Invalid integer: unexpected character '%c' after digits", *pch);
                        }
                        char *text = extract(start, pch);
                        tk = addTk(CONST_INT);
                        tk->text = text;
                        tk->i = atoi(text); // convert to numeric value
                    }
                } else {
                    err("Invalid character: %c (%d)", *pch, *pch);
                }
        }
     
    }
}

void exportTokens(const Token *tokens, const char *sourceFileName, const char *sourceFilePath) {
    // Create full path for the subdirectory
    char dirPath[256]; 
    snprintf(dirPath, sizeof(dirPath), "./output/%s", removeExtension(sourceFileName));

    // Create the subdirectory if it doesn't exist + copy the source file to be available in the output directory
    struct stat st = {0};
    if (stat(dirPath, &st) == -1) {
        mkdir(dirPath); 
    }

    // Copy the source file to the output directory(to always be upp to date)
    char copyFilePath[256];
    snprintf(copyFilePath, sizeof(dirPath), "./output/%s/%s", removeExtension(sourceFileName), sourceFileName);
    copyFile(sourceFilePath, copyFilePath);
    
    // Create full path for the output file
    char filePath[512];
    snprintf(filePath, sizeof(filePath), "%s/AtomList-%s.txt", dirPath, removeExtension(sourceFileName));
    
    // Open the file for writing
    FILE *f = fopen(filePath, "w"); 
    if (!f) {
        // If opening failed, check if it's a permission issue
        fprintf(stderr, "Error creating/opening file at '%s': (%s)\n", filePath, strerror(errno));
        exit(-1) ;
    }
    
    // Write tokens to file
    for (const Token *tk = tokens; tk; tk = tk->next) {
        fprintf(f, "Line %d: ", tk->line);
        switch (tk->code) {
            case ID: fprintf(f, "ID [%s]\n", tk->text); break;
            case TYPE_INT: fprintf(f, "TYPE_INT\n"); break;
            case TYPE_CHAR: fprintf(f, "TYPE_CHAR\n"); break;
            case TYPE_DOUBLE: fprintf(f, "TYPE_DOUBLE\n"); break;
            case TYPE_VOID: fprintf(f, "VOID\n"); break;
            case STRUCT: fprintf(f, "STRUCT\n"); break;
            case IF: fprintf(f, "IF\n"); break;
            case ELSE: fprintf(f, "ELSE\n"); break;
            case WHILE: fprintf(f, "WHILE\n"); break;
            case RETURN: fprintf(f, "RETURN\n"); break;
            case CONST_INT: fprintf(f, "INT [%d]\n", tk->i); break;
            case CONST_DOUBLE: fprintf(f, "DOUBLE [%.2f]\n", tk->d); break;
            case CONST_CHAR: fprintf(f, "CHAR [%c]\n", tk->i); break;
            case STRING: fprintf(f, "STRING [%s]\n", tk->text); break;
            case COMMA: fprintf(f, "COMMA\n"); break;
            case SEMICOLON: fprintf(f, "SEMICOLON\n"); break;
            case LPAR: fprintf(f, "LPAR\n"); break;
            case RPAR: fprintf(f, "RPAR\n"); break;
            case LACC: fprintf(f, "LACC\n"); break;
            case RACC: fprintf(f, "RACC\n"); break;
            case LBRACKET: fprintf(f, "LBRACKET\n"); break;
            case RBRACKET: fprintf(f, "RBRACKET\n"); break;
            case ADD: fprintf(f, "ADD\n"); break;
            case SUB: fprintf(f, "SUB\n"); break;
            case MUL: fprintf(f, "MUL\n"); break;
            case DIV: fprintf(f, "DIV\n"); break;
            case DOT: fprintf(f, "DOT\n"); break;
            case AND: fprintf(f, "AND\n"); break;
            case OR: fprintf(f, "OR\n"); break;
            case NOT: fprintf(f, "NOT\n"); break;
            case ASSIGN: fprintf(f, "ASSIGN\n"); break;
            case EQUAL: fprintf(f, "EQUAL\n"); break;
            case NOTEQ: fprintf(f, "NOTEQ\n"); break;
            case LESS: fprintf(f, "LESS\n"); break;
            case LESSEQ: fprintf(f, "LESSEQ\n"); break;
            case GREATER: fprintf(f, "GREATER\n"); break;
            case GREATEREQ: fprintf(f, "GREATEREQ\n"); break;
            case END: fprintf(f, "END"); break;
            default: fprintf(f, "UNKNOWN\n"); break;
        }
    }
    
    fclose(f); 
}

void printTokens(const Token *tokens) {

    for (const Token *tk = tokens; tk; tk = tk->next) {
        printf("Line %d: ", tk->line);
        switch (tk->code) {
            case ID: printf("ID [%s]\n", tk->text); break;
            case TYPE_INT: printf("TYPE_INT\n"); break;
            case TYPE_CHAR: printf("TYPE_CHAR\n"); break;
            case TYPE_DOUBLE: printf("TYPE_DOUBLE\n"); break;
            case TYPE_VOID: printf("VOID\n"); break;
            case STRUCT: printf("STRUCT\n"); break;
            case IF: printf("IF\n"); break;
            case ELSE: printf("ELSE\n"); break;
            case WHILE: printf("WHILE\n"); break;
            case RETURN: printf("RETURN\n"); break;
            case CONST_INT: printf("INT [%d]\n", tk->i); break;
            case CONST_DOUBLE: printf("DOUBLE [%.2f]\n", tk->d); break;
            case CONST_CHAR: printf("CHAR [%c]\n", tk->i); break;
            case STRING: printf("STRING [%s]\n", tk->text); break;
            case COMMA: printf("COMMA\n"); break;
            case SEMICOLON: printf("SEMICOLON\n"); break;
            case LPAR: printf("LPAR\n"); break;
            case RPAR: printf("RPAR\n"); break;
            case LACC: printf("LACC\n"); break;
            case RACC: printf("RACC\n"); break; 
            case LBRACKET: printf("LBRACKET\n"); break;
            case RBRACKET: printf("RBRACKET\n"); break;
            case ADD: printf("ADD\n"); break;
            case SUB: printf("SUB\n"); break;
            case MUL: printf("MUL\n"); break;
            case DIV: printf("DIV\n"); break;
            case DOT: printf("DOT\n"); break;
            case AND: printf("AND\n"); break;
            case OR: printf("OR\n"); break;
            case NOT: printf("NOT\n"); break;
            case ASSIGN: printf("ASSIGN\n"); break;
            case EQUAL: printf("EQUAL\n"); break;
            case NOTEQ: printf("NOTEQ\n"); break;
            case LESS: printf("LESS\n"); break;
            case LESSEQ: printf("LESSEQ\n"); break;
            case GREATER: printf("GREATER\n"); break;
            case GREATEREQ: printf("GREATEREQ\n"); break;
            case END: printf("END\n\n"); break;
            default: printf("UNKNOWN\n"); break;
        }
    }
}