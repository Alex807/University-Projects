#pragma once

enum{
	//identifiers
    ID
    // keywords
    ,TYPE_CHAR, TYPE_DOUBLE, TYPE_INT, ELSE, IF, RETURN, STRUCT, TYPE_VOID, WHILE
	// constants
	,CONST_INT, CONST_CHAR, CONST_DOUBLE, STRING
    // delimiters
    ,DOT, COMMA, SEMICOLON, LPAR, RPAR, LBRACKET, RBRACKET, LACC, RACC, END
    // operators
    ,ADD, SUB, MUL, DIV, AND, OR, NOT, ASSIGN, EQUAL, NOTEQ, LESS, LESSEQ, GREATER, GREATEREQ
	//spaces 
	,SPACE, NEWLINE, TAB, LINECOMMENT, BLOCKCOMMENT
};

typedef struct Token{
	int code;		// ID, TYPE_CHAR, ...
	int line;		// the line from the input file
	union{
		char *text;		// the chars for ID, STRING (dynamically allocated)
		int i;		// the value for INT
		char c;		// the value for CHAR
		double d;		// the value for DOUBLE
		};
	struct Token *next;		// next token in a simple linked list
}Token;

#define MAX_PATH_SIZE 260 
#define MAX_FILENAME_SIZE 50

Token *tokenize(const char *pch);
void exportTokens(const Token *tokens, const char *fileName, const char *filePath); 
void printTokens(const Token *tokens);
