#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <stdbool.h>
#include <string.h>

#include "lexer.h"
#include "parser.h"

Token *iTk;        // the iterator in the tokens list
Token *consumedTk; // the last consumed token

char structNames[256][128]; // Array to store 256 struct names with mak_len(128)(ensure we use 'struct ID' type)
int structCount = 0; // Number of struct names stored

// Forward declarations for all functions(to avoid errors caused by the order of the functions)
bool unit();
bool structDef();
bool varDef();
bool typeBase();
bool arrayDecl();
bool fnDef();
bool fnParam();
bool stmCompound();
bool stm();
bool expr();
bool exprAssign();
bool exprOr();
bool exprAnd();
bool exprEq();
bool exprRel();
bool exprAdd();
bool exprMul();
bool exprCast();
bool exprUnary();
bool exprPostfix();
bool exprPrimary();

void tkerr(const char *fmt,...){
    fprintf(stderr,"[ERROR] In line %d: ", consumedTk->line);
    va_list va;
    va_start(va,fmt);
    vfprintf(stderr,fmt,va);
    va_end(va);
    fprintf(stderr,"\n\n");
    exit(0); //to stop the execution but still run other commands from makefile
}

bool consume(int code){
    if(iTk->code==code){
        consumedTk=iTk;
        iTk=iTk->next;
        return true;
    }
    return false;
}

// check if an ID is a struct and need to be used as 'struct ID' type
bool isStructType(const char *name) {
    for(int i = 0; i < structCount; i++) {
        if(strcmp(structNames[i], name) == 0) {
            return true;
        }
    }
    return false;
}

// typeBase: TYPE_INT | TYPE_DOUBLE | TYPE_CHAR | STRUCT ID 
bool typeBase(){
    // Handle basic types
    if(consume(TYPE_INT) || consume(TYPE_DOUBLE) || consume(TYPE_CHAR)) {
        return true;
    }

    // Handle struct types 
    if(consume(STRUCT)){ 
        consume(ID); // Consume ID after struct
        if(isStructType(consumedTk->text)) { //case when occur new struct definitions 
            return true;

        } else {
            tkerr("ID '%s' is NOT part of a 'struct ID' type!", consumedTk->text); 
            return false;
        }
    }

    // Check if the current token is an ID that might be a struct type used incorrectly
    Token *start = iTk;
    if(consume(ID)){
        // Check if this ID is a known struct type
        if(isStructType(consumedTk->text)) {
            tkerr("Struct ID '%s' must be preceded by 'struct' keyword to can form 'struct ID' type", consumedTk->text);
            iTk = start; // Restore position
            return false;
        }
        // If it's not a struct type, it might be a typedef or other valid ID
        iTk = start; // Restore position
    }

    return false;
}

// arrayDecl: LBRACKET INT? RBRACKET
bool arrayDecl() {
    if(consume(LBRACKET)){
        consume(CONST_INT); // optional INT
        if(consume(RBRACKET)){
            return true;
        }
        tkerr("Missing ] in array declaration");
    }
    return false;
}

// expr: exprAssign
bool expr(){
    if(exprAssign()){
        return true;
    }
    return false;
}

// exprPrimary: ID ( LPAR ( expr ( COMMA expr )* )? RPAR )? | INT | DOUBLE | CHAR | STRING | LPAR expr RPAR
bool exprPrimary(){
    if(consume(ID)){
        if(consume(LPAR)){
            if(expr()){
                for(;;){
                    if(consume(COMMA)){
                        if(expr()){
                            continue;
                        }
                        tkerr("Invalid expression after ,");
                    }
                    break;
                }
            }
            if(consume(RPAR)){
                return true;
            }
            tkerr("Missing )");
        }
        return true;
    }
    if(consume(CONST_INT)){
        return true;
    }
    if(consume(CONST_DOUBLE)){
        return true;
    }
    if(consume(CONST_CHAR)){
        return true;
    }
    if(consume(STRING)){
        return true;
    }
    if(consume(LPAR)){
        if(expr()){
            if(consume(RPAR)){
                return true;
            }
            tkerr("Missing )");
        }
        tkerr("Invalid expression after (");
    }
    return false;
}

// exprPostfix: exprPostfix LBRACKET expr RBRACKET | exprPostfix DOT ID | exprPrimary
// Removing left recursion: exprPostfix: exprPrimary exprPostfixPrime
// exprPostfixPrime: LBRACKET expr RBRACKET exprPostfixPrime | DOT ID exprPostfixPrime | ε
bool exprPostfix(){
    if(exprPrimary()){
        for(;;){
            if(consume(LBRACKET)){
                if(expr()){
                    if(consume(RBRACKET)){
                        continue;
                    }
                    tkerr("Missing ]");
                }
                tkerr("Invalid expression after [");
            }
            if(consume(DOT)){
                if(consume(ID)){
                    continue;
                }
                tkerr("Missing ID after .");
            }
            break;
        }
        return true;
    }
    return false;
}

// exprUnary: (SUB | NOT) exprUnary | exprPostfix
bool exprUnary(){
    if(consume(SUB) || consume(NOT)){
        if(exprUnary()){
            return true;
        }
        tkerr("Invalid unary expression");
    }
    if(exprPostfix()){
        return true;
    }
    return false;
}

// exprCast: LPAR typeBase arrayDecl? RPAR exprCast | exprUnary
bool exprCast(){
    if(consume(LPAR)){
        Token *start = iTk;
        if(typeBase()){
            arrayDecl(); // optional
            if(consume(RPAR)){
                if(exprCast()){
                    return true;
                }
                tkerr("Invalid cast expression");
            }
            tkerr("Missing )");
        }
        iTk = start; // undo typeBase
        iTk = consumedTk; // restore LPAR
    }
    if(exprUnary()){
        return true;
    }
    return false;
}

// exprMul: exprMul (MUL | DIV) exprCast | exprCast
// Removing left recursion: exprMul: exprCast exprMulPrime
// exprMulPrime: (MUL | DIV) exprCast exprMulPrime | ε
bool exprMul(){
    if(exprCast()){
        for(;;){
            if(consume(MUL) || consume(DIV)){
                if(exprCast()){
                    continue;
                }
                tkerr("Invalid multiplicative expression");
            }
            break;
        }
        return true;
    }
    return false;
}

// exprAdd: exprAdd (ADD | SUB) exprMul | exprMul
// Removing left recursion: exprAdd: exprMul exprAddPrime
// exprAddPrime: (ADD | SUB) exprMul exprAddPrime | ε
bool exprAdd(){
    if(exprMul()){
        for(;;){
            if(consume(ADD) || consume(SUB)){
                if(exprMul()){
                    continue;
                }
                tkerr("Invalid additive expression");
            }
            break;
        }
        return true;
    }
    return false;
}

// exprRel: exprRel (LESS | LESSEQ | GREATER | GREATEREQ) exprAdd | exprAdd
// Removing left recursion: exprRel: exprAdd exprRelPrime
// exprRelPrime: (LESS | LESSEQ | GREATER | GREATEREQ) exprAdd exprRelPrime | ε
bool exprRel(){
    if(!exprAdd()){
        // Check if we're looking at a relational operator without a left operand
        if(iTk->code == LESS || iTk->code == LESSEQ || 
           iTk->code == GREATER || iTk->code == GREATEREQ){
            switch(iTk->code){
                case LESS:
                    tkerr("Missing left operand before '<' operator");
                    break;
                case LESSEQ:
                    tkerr("Missing left operand before '<=' operator");
                    break;
                case GREATER:
                    tkerr("Missing left operand before '>' operator");
                    break;
                case GREATEREQ:
                    tkerr("Missing left operand before '>=' operator");
                    break;
            }
        }
        return false;
    }

    for(;;){
        Token *start = iTk;  // Save position before consuming operator
        
        // Try to consume each relational operator individually
        if(consume(LESS)){
            if(!exprAdd()){
                tkerr("Missing right operand after '<' operator");
                return false;
            }
            continue;
        }
        
        if(consume(LESSEQ)){
            if(!exprAdd()){
                tkerr("Missing right operand after '<=' operator");
                return false;
            }
            continue;
        }
        
        if(consume(GREATER)){
            if(!exprAdd()){
                tkerr("Missing right operand after '>' operator");
                return false;
            }
            continue;
        }
        
        if(consume(GREATEREQ)){
            if(!exprAdd()){
                tkerr("Missing right operand after '>=' operator");
                return false;
            }
            continue;
        }
        
        // If we get here, no relational operator was found
        iTk = start;  // Restore position
        break;
    }
    
    return true;
}

// exprEq: exprEq (EQUAL | NOTEQ) exprRel | exprRel
// Removing left recursion: exprEq: exprRel exprEqPrime
// exprEqPrime: (EQUAL | NOTEQ) exprRel exprEqPrime | ε
bool exprEq(){
    if(exprRel()){
        for(;;){
            if(consume(EQUAL) || consume(NOTEQ)){
                if(exprRel()){
                    continue;
                }
                tkerr("Invalid equality expression");
            }
            break;
        }
        return true;
    }
    return false;
}

// exprAnd: exprAnd AND exprEq | exprEq
// Removing left recursion: exprAnd: exprEq exprAndPrime
// exprAndPrime: AND exprEq exprAndPrime | ε
bool exprAnd(){
    if(exprEq()){
        for(;;){
            if(consume(AND)){
                if(exprEq()){
                    continue;
                }
                tkerr("Invalid AND expression");
            }
            break;
        }
        return true;
    }
    return false;
}

// exprOr: exprOr OR exprAnd | exprAnd
// Removing left recursion: exprOr: exprAnd exprOrPrime
// exprOrPrime: OR exprAnd exprOrPrime | ε
bool exprOr(){
    if(exprAnd()){
        for(;;){
            if(consume(OR)){
                if(exprAnd()){
                    continue;
                }
                tkerr("invalid OR expression");
            }
            break;
        }
        return true;
    }
    return false;
}

// exprAssign: exprUnary ASSIGN exprAssign | exprOr
bool exprAssign(){
    Token *start = iTk;
    if(exprUnary()){
        if(consume(ASSIGN)){
            if(exprAssign()){
                return true;
            }
            tkerr("Invalid assignment expression");
        }
        iTk = start; // undo exprUnary
    }
    if(exprOr()){
        return true;
    }
    return false;
}

// stm: stmCompound | IF LPAR expr RPAR stm (ELSE stm)? | WHILE LPAR expr RPAR stm | RETURN expr? SEMICOLON | expr? SEMICOLON
bool stm(){
    if(stmCompound()){
        return true;
    }
    if(consume(IF)){
        if(consume(LPAR)){
            if(expr()){
                if(consume(RPAR)){
                    if(stm()){
                        if(consume(ELSE)){
                            if(stm()){
                                return true;
                            }
                            tkerr("Invalid statement after ELSE");
                        }
                        return true;
                    }
                    tkerr("Invalid statement after IF");
                }
                tkerr("Missing )");
            }
            tkerr("Invalid expression after (");
        }
        tkerr("Missing (");
    }
    if(consume(WHILE)){
        if(consume(LPAR)){
            if(expr()){
                if(consume(RPAR)){
                    if(stm()){
                        return true;
                    }
                    tkerr("Invalid statement after 'while'");
                }
                tkerr("Missing ) after 'while'");
            }
            tkerr("Invalid expression after (");
        }
        tkerr("Missing ( after 'while' declaration");
    }
    if(consume(RETURN)){
        expr(); // optional
        if(consume(SEMICOLON)){
            return true;
        }
        tkerr("Missing ; after 'return' statement");
    }
    expr(); // optional
    if(consume(SEMICOLON)){
        return true;
    }
    return false;
}

// varDef: typeBase ID arrayDecl? (ASSIGN expr)? SEMICOLON
bool varDef() {
    Token *start = iTk;
    if (typeBase()) {
        if (consume(ID)) {
             // If the next token is LPAR, it's actually a function definition
            if (iTk->code == LPAR) {
                iTk = start;  // Restore token position
                return false;  // Let fnDef() handle it
            }

            arrayDecl(); // check for array declaration
            
            // Check for initialization with ASSIGN
            if (consume(ASSIGN)) {
                // Handle initialization expression
                if (!expr()) {
                    tkerr("Missing expression after '=' in variable initialization");
                }
            }
            
            if (consume(SEMICOLON)) {
                return true;
            }
            tkerr("Missing ; after variable definition");
        }
        iTk = start; // undo typeBase
    }
    return false;
}

// fnParam: typeBase ID arrayDecl?
bool fnParam(){
    if(typeBase()){
        if(consume(ID)){
            arrayDecl(); // optional
            return true;
        }
        tkerr("Missing ID for function parameter");
    } 

    return false;
}

// fnDef: (typeBase | VOID) ID LPAR (fnParam (COMMA fnParam)*)? RPAR stmCompound
bool fnDef(){
    Token *start = iTk;
    
    // First check if this might be a function without return type
    if(iTk->code == ID && iTk->next && iTk->next->code == LPAR) {
        // We found a function name followed by (, but no return type
        tkerr("Missing 'return type' in function declaration '%s'", iTk->text);
        return false;
    }

    // Normal function definition parsing
    if(typeBase() || consume(TYPE_VOID)){
        if(consume(ID)){
            Token *fnName = consumedTk; // Store function name for error messages
            if(consume(LPAR)){
                // Parse first parameter if it exists
                if(fnParam()){
                    for(;;){
                        // After a parameter, we must have either a comma or right paren
                        Token *ahead = iTk;
                        // If we see another typeBase without a comma, that's an error
                        if(ahead->code == TYPE_INT || ahead->code == TYPE_DOUBLE || 
                           ahead->code == TYPE_CHAR || ahead->code == STRUCT) {
                            tkerr("Missing ',' between parameters in function '%s'", fnName->text);
                            return false;
                        }
                        
                        if(consume(COMMA)){
                            if(fnParam()){
                                continue;
                            }
                            tkerr("Missing/Invalid 'type' for parameter '%s' in function '%s'", consumedTk->text, fnName->text);
                        }
                        break;
                    }
                }
                if(consume(RPAR)){
                    // After right parenthesis, we must have a left brace
                    if(iTk->code != LACC) {
                        tkerr("Missing '{' in function '%s' body", fnName->text);
                        return false;
                    }
                    if(!stmCompound()){
                        tkerr("Missing '}' in function '%s' body", fnName->text);
                        return false;
                    }
                    return true;
                }
                tkerr("Missing ')' in function '%s' declaration", fnName->text);
            }
        }
        iTk = start; // undo typeBase or VOID
    }
    return false;
}

// stmCompound: LACC (varDef | stm)* RACC
bool stmCompound(){
    if(consume(LACC)){  // Ensure function starts with {
        for(;;){
            if(varDef()){ }  // Parse variable definitions
            else if(stm()){ }  // Parse statements
            else break;  // If neither, exit loop
        }
        if(consume(RACC)){  // Ensure function ends with }
            return true;
        }
    }
    return false;
}

// structDef: STRUCT ID LACC varDef* RACC (ID)? SEMICOLON
bool structDef(){
    Token *start = iTk; // Save the starting position
    
    if(consume(STRUCT)){
        if(consume(ID)){
            Token *structNameToken = consumedTk; // Store the struct name token
            
            // Check if it's a struct definition with body (must have LACC)
            if(consume(LACC)){
                for(;;){
                    if(varDef()){}
                    else break; // If neither, exit loop
                }
                if(consume(RACC)){
                    // Optional type name after the struct definition
                    consume(ID); // optional type name
                    
                    // Semicolon is required
                    if(consume(SEMICOLON)){
                        // Add the struct name to our list of known struct types
                        if(structNameToken) {
                            strncpy(structNames[structCount], structNameToken->text, 63);
                            structNames[structCount][63] = '\0'; // Ensure null termination
                            structCount++;
                        }
                        return true;
                    }
                    tkerr("Missing ; after struct definition");
                }
                tkerr("Missing } after struct definition");
            
            } else {
                if(iTk->code == LBRACKET || iTk->code == ID) {
                    // This looks like 'struct Point p' OR 'struct Point points[10];' 
                    iTk = start;
                    return false; // Let varDef handle it

                } else {
                    // Missing { in struct definition
                    tkerr("Missing { for struct '%s' definition", structNameToken->text);
                    return false;
                }
            }
        }
        tkerr("Missing ID after 'struct' keyword at declaration");
    }
    return false;
}

// unit: (structDef | fnDef | varDef)* END
bool unit(){
    for(;;){
        if(structDef()){}
        else if(fnDef()){}
        else if(varDef()){}
        else break;
    }

    if(consume(END)){
        return true;
    } 
    
    return false;
}

void parse(Token *tokens){
    iTk=tokens;
    if(!unit())tkerr("Invalid statement at the end of the line");
    printf("[SUCCESS] Sintactic analysis done successfully!\n");
}