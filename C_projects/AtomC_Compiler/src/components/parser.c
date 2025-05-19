#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <stdbool.h>
#include <string.h>

#include "lexer.h"
#include "parser.h"
#include "utils.h"
#include "ad.h" 
#include "at.h" 
#include "vm.h"

// Add this with other global variables
Symbol *owner = NULL;  // Initialize to NULL

Token *iTk;        // the iterator in the tokens list
Token *consumedTk; // the last consumed token

// Forward declarations for all functions(to avoid errors caused by the order of the functions)
bool unit();
bool structDef();
bool varDef();
bool typeBase(Type *t);
bool arrayDecl(Type *t);
bool fnDef();
bool fnParam();
bool stmCompound(bool value);
bool stm();
bool expr(Ret *r);
bool exprAssign(Ret *r);
bool exprOr(Ret *r);
bool exprAnd(Ret *r);
bool exprEq(Ret *r);
bool exprRel(Ret *r);
bool exprAdd(Ret *r);
bool exprMul(Ret *r);
bool exprCast(Ret *r);
bool exprUnary(Ret *r);
bool exprPostfix(Ret *r);
bool exprPrimary(Ret *r);

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

bool typeBase(Type *t) {
    t->n = -1;  // not an array by default
    t->s = NULL;  // not a struct by default
  
    if(consume(TYPE_INT)) {
        t->tb = TB_INT;
        return true;
    }
    if(consume(TYPE_DOUBLE)) {
        t->tb = TB_DOUBLE;
        return true;
    }
    if(consume(TYPE_CHAR)) {
        t->tb = TB_CHAR;
        return true;
    }
    if(consume(STRUCT)) {
        if(consume(ID)) {
            // Find the struct in symbol table
            Symbol *s = findSymbol(consumedTk->text);
            if(!s || s->kind != SK_STRUCT) {
                tkerr("undefined struct: %s", consumedTk->text);
                return false;
            }
            t->tb = TB_STRUCT;
            t->s = s;
            return true;
        }
        tkerr("missing struct name");
    }
    return false;
}

bool arrayDecl(Type *t) {
    if(consume(LBRACKET)) {
        if(consume(CONST_INT)) {  // Changed from TYPE_INT to CONST_INT
            t->n = consumedTk->i;  // Get the integer value
            if(t->n <= 0) {
                tkerr("array size must be positive");
                return false;
            }
        } else {
            t->n = 0;  // For unspecified size
        }
        if(consume(RBRACKET)) {
            return true;
        }
        tkerr("Missing ] in array declaration");
        return false;
    }
    return false;
}

bool expr(Ret *r) {
    // Pass the Ret structure to exprAssign to collect type information
    if(exprAssign(r)){
        return true;
    }
    return false;
}

// exprPrimary: ID ( LPAR ( expr ( COMMA expr )* )? RPAR )? | INT | DOUBLE | CHAR | STRING | LPAR expr RPAR
bool exprPrimary(Ret *r) {
    if(consume(ID)) {
        Token *tkName = consumedTk;  // store the ID token
      
        // Check if ID exists in symbol table
        Symbol *s = findSymbol(tkName->text);
        if(!s) {
            tkerr("undefined id: %s", tkName->text);
        }
      
        // Function call
        if(consume(LPAR)) {
            // Check if it's actually a function
            if(s->kind != SK_FN) {
                tkerr("only a function can be called");
            }
          
            Ret rArg;  // for argument type checking
            Symbol *param = s->fn.params;  // current parameter being checked
          
            // Process arguments
            if(expr(&rArg)) {  // first argument
                // Check first argument
                if(!param) {
                    tkerr("too many arguments in function call");
                }
                if(!convTo(&rArg.type, &param->type)) {
                    tkerr("in call, cannot convert the argument type to the parameter type");
                }
                param = param->next;
              
                // Process remaining arguments
                for(;;) {
                    if(consume(COMMA)) {
                        if(expr(&rArg)) {
                            // Check each argument
                            if(!param) {
                                tkerr("too many arguments in function call");
                            }
                            if(!convTo(&rArg.type, &param->type)) {
                                tkerr("in call, cannot convert the argument type to the parameter type");
                            }
                            param = param->next;
                            continue;
                        }
                        tkerr("Invalid expression after ,");
                    }
                    break;
                }
            }
          
            if(consume(RPAR)) {
                // Check if we have unused parameters
                if(param) {
                    tkerr("too few arguments in function call");
                }
              
                // Set return type info
                r->type = s->type;
                r->lval = false;
                r->ct = true;
              
                return true;
            }
            tkerr("Missing )");
        } else {
            // Non-function identifier
            if(s->kind == SK_FN) {
                tkerr("a function can only be called");
            }
          
            // Set identifier type info
            r->type = s->type;
            r->lval = true;
            r->ct = s->type.n >= 0;  // constant if it's an array
          
            return true;
        }
    }
  
    // Constants
    if(consume(CONST_INT)) {
        *r = (Ret){{TB_INT, NULL, -1}, false, true};
        return true;
    }
    if(consume(CONST_DOUBLE)) {
        *r = (Ret){{TB_DOUBLE, NULL, -1}, false, true};
        return true;
    }
    if(consume(CONST_CHAR)) {
        *r = (Ret){{TB_CHAR, NULL, -1}, false, true};
        return true;
    }
    if(consume(STRING)) {
        *r = (Ret){{TB_CHAR, NULL, 0}, false, true};  // array of char
        return true;
    }
  
    // Parenthesized expression
    if(consume(LPAR)) {
        if(expr(r)) {  // type info will be set by expr
            if(consume(RPAR)) {
                return true;
            }
            tkerr("Missing )");
        }
        tkerr("Invalid expression after (");
    }
  
    return false;
}

// exprPostfix: exprPostfix LBRACKET expr RBRACKET | exprPostfix DOT ID | exprPrimary
bool exprPostfix(Ret *r) {
    if(exprPrimary(r)) {  // r will contain the primary expression's type info
        for(;;) {
            // Array indexing
            if(consume(LBRACKET)) {
                Ret idx;  // index expression type info
                if(expr(&idx)) {
                    if(consume(RBRACKET)) {
                        // Type analysis for array indexing
                        if(r->type.n < 0) {
                            tkerr("only an array can be indexed");
                        }
                      
                        Type tInt = {TB_INT, NULL, -1};
                        if(!convTo(&idx.type, &tInt)) {
                            tkerr("the index is not convertible to int");
                        }
                      
                        // Modify type info for the result
                        // After indexing, it's no longer an array
                        r->type.n = -1;
                        r->lval = true;
                        r->ct = false;
                      
                        continue;
                    }
                    tkerr("Missing ]");
                }
                tkerr("Invalid expression after [");
            }
          
            // Structure field access
            if(consume(DOT)) {
                Token *tkName = iTk;  // store the field name token
                if(consume(ID)) {
                    // Type analysis for structure field access
                    if(r->type.tb != TB_STRUCT) {
                        tkerr("a field can only be selected from a struct");
                    }
                  
                    // Find the field in the structure
                    Symbol *s = findSymbolInList(r->type.s->structMembers, tkName->text);
                    if(!s) {
                        tkerr("the structure %s does not have a field %s",
                              r->type.s->name, tkName->text);
                    }
                  
                    // Set the type info for the field
                    r->type = s->type;
                    r->lval = true;
                    r->ct = s->type.n >= 0;  // constant if it's an array
                  
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
bool exprUnary(Ret *r) {
    Token *op = iTk;  // store the operator token for distinguishing between SUB and NOT
  
    if(consume(SUB) || consume(NOT)) {
        if(exprUnary(r)) {  // r will contain the operand's type info
            // Type analysis for unary operations
            if(!canBeScalar(r)) {
                tkerr("unary - or ! must have a scalar operand");
            }
          
            // Set result properties
            r->lval = false;
            r->ct = true;
          
            // For NOT operator, result is always int
            if(op->code == NOT) {
                r->type.tb = TB_INT;
                r->type.n = -1;
                r->type.s = NULL;
            }
            // For SUB operator, keep the original type but ensure it's not an lvalue
          
            return true;
        }
        tkerr("Invalid unary expression");
    }
  
    if(exprPostfix(r)) {  // r will contain the postfix expression's type info
        return true;
    }
  
    return false;
}

//exprCast: LPAR typeBase arrayDecl? RPAR exprCast | exprUnary
bool exprCast(Ret *r) {
    Token *start = iTk;

    // Try exprUnary first
    if(exprUnary(r)) {
        return true;
    }

    // Try cast operation
    if(consume(LPAR)) {
        Type t;       // type to cast to
        Ret op;      // operand being cast
      
        if(typeBase(&t)) {
            // Optional array declaration
            arrayDecl(&t);
          
            if(consume(RPAR)) {
                if(exprCast(&op)) {  // get the operand's type info
                    // Type analysis for cast operation
                  
                    // Check if casting to struct
                    if(t.tb == TB_STRUCT) {
                        tkerr("cannot convert to a struct type");
                    }
                  
                    // Check if casting from struct
                    if(op.type.tb == TB_STRUCT) {
                        tkerr("cannot convert a struct");
                    }
                  
                    // Check array to non-array conversion
                    if(op.type.n >= 0 && t.n < 0) {
                        tkerr("an array can be converted only to another array");
                    }
                  
                    // Check scalar to array conversion
                    if(op.type.n < 0 && t.n >= 0) {
                        tkerr("a scalar can be converted only to another scalar");
                    }
                  
                    // Set the result type to the cast type
                    r->type = t;
                    r->lval = false;
                    r->ct = true;
                  
                    return true;
                }
            } else {
                tkerr("Missing \')\' in cast operation");
            }
        }
    }
  
    iTk = start;
    return false;
}

// exprMul: exprMul (MUL | DIV) exprCast | exprCast
bool exprMul(Ret *r) {
    // Start with exprCast
    if(exprCast(r)) {
        for(;;) {
            Token *op = iTk;  // store the operator token
            if(consume(MUL) || consume(DIV)) {
                Ret right;  // store type info for right operand
                if(exprCast(&right)) {
                    // Type analysis for multiplicative operations
                    Type tDst;
                    if(!arithTypeTo(&r->type, &right.type, &tDst)) {
                        tkerr("invalid operand type for * or /");
                    }
                  
                    // Set result type to the destination type
                    // Similar to addition/subtraction, we keep the arithmetic result type
                    r->type = tDst;
                    r->lval = false;
                    r->ct = true;
                  
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
bool exprAdd(Ret *r) {
    // Start with exprMul
    if(exprMul(r)) {
        for(;;) {
            Token *op = iTk;  // store the operator token
            if(consume(ADD) || consume(SUB)) {
                Ret right;  // store type info for right operand
                if(exprMul(&right)) {
                    // Type analysis for additive operations
                    Type tDst;
                    if(!arithTypeTo(&r->type, &right.type, &tDst)) {
                        tkerr("invalid operand type for + or -");
                    }
                  
                    // Set result type to the destination type
                    // Note: This is different from logical/relational operations
                    // as we keep the arithmetic result type
                    r->type = tDst;
                    r->lval = false;
                    r->ct = true;
                  
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
bool exprRel(Ret *r) {
    // Start with exprAdd
    if(!exprAdd(r)) {
        // Check if we're looking at a relational operator without a left operand
        if(iTk->code == LESS || iTk->code == LESSEQ || 
           iTk->code == GREATER || iTk->code == GREATEREQ) {
            switch(iTk->code) {
                case LESS:
                    tkerr("Missing left operand before '<' operator");
                    break;
                case LESSEQ:
                    tkerr("Missing left operand before '<=' operator");
                    break;
                case GREATER:
                    tkerr("Missing right operand before '>' operator");
                    break;
                case GREATEREQ:
                    tkerr("Missing right operand before '>=' operator");
                    break;
            }
        }
        return false;
    }

    for(;;) {
        Token *start = iTk;  // Save position before consuming operator
        Token *op = iTk;     // Store the operator for error messages
      
        // Try to consume any relational operator
        if(consume(LESS) || consume(LESSEQ) || 
           consume(GREATER) || consume(GREATEREQ)) {
          
            Ret right;  // store type info for right operand
            if(exprAdd(&right)) {
                // Type analysis for relational operations
                Type tDst;
                if(!arithTypeTo(&r->type, &right.type, &tDst)) {
                    tkerr("invalid operand type for <, <=, >, >=");
                }
              
                // Set result type to int (boolean result)
                r->type.tb = TB_INT;
                r->type.s = NULL;
                r->type.n = -1;
                r->lval = false;
                r->ct = true;
              
                continue;
            }
          
            // Error messages for missing right operand
            switch(op->code) {
                case LESS:
                    tkerr("Missing right operand after '<' operator");
                    break;
                case LESSEQ:
                    tkerr("Missing right operand after '<=' operator");
                    break;
                case GREATER:
                    tkerr("Missing right operand after '>' operator");
                    break;
                case GREATEREQ:
                    tkerr("Missing right operand after '>=' operator");
                    break;
            }
            return false;
        }
      
        // If we get here, no relational operator was found
        iTk = start;  // Restore position
        break;
    }
  
    return true;
}

// exprEq: exprEq (EQUAL | NOTEQ) exprRel | exprRel
bool exprEq(Ret *r) {
    // Start with exprRel
    if(exprRel(r)) {
        for(;;) {
            Token *op = iTk;  // store the operator token
            if(consume(EQUAL) || consume(NOTEQ)) {
                Ret right;  // store type info for right operand
                if(exprRel(&right)) {
                    // Type analysis for equality operations
                    Type tDst;
                    if(!arithTypeTo(&r->type, &right.type, &tDst)) {
                        tkerr("invalid operand type for == or !=");
                    }
                  
                    // Set result type to int (boolean result)
                    r->type.tb = TB_INT;
                    r->type.s = NULL;
                    r->type.n = -1;
                    r->lval = false;
                    r->ct = true;
                  
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
bool exprAnd(Ret *r) {
    // Start with exprEq
    if(exprEq(r)) {
        for(;;) {
            if(consume(AND)) {
                Ret right;  // store type info for right operand
                if(exprEq(&right)) {
                    // Type analysis for AND operation
                    Type tDst;
                    if(!arithTypeTo(&r->type, &right.type, &tDst)) {
                        tkerr("invalid operand type for &&");
                    }
                  
                    // Set result type to int (boolean result)
                    r->type.tb = TB_INT;
                    r->type.s = NULL;
                    r->type.n = -1;
                    r->lval = false;
                    r->ct = true;
                  
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
// exprOrPrime: OR exprAnd exprOrPrime 
bool exprOr(Ret *r) {

    if(exprAnd(r)) {
        for(;;) {
            if(consume(OR)) {
                Ret right;  // store type info for right operand
                if(exprAnd(&right)) {
                    // Type analysis for OR operation
                    Type tDst;
                    if(!arithTypeTo(&r->type, &right.type, &tDst)) {
                        tkerr("invalid operand type for ||");
                    }
                  
                    // Set result type to int (boolean result)
                    r->type.tb = TB_INT;
                    r->type.n = -1;
                    r->type.s = NULL;
                    r->lval = false;
                    r->ct = true;
                  
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
bool exprAssign(Ret *r) {
    Token *start = iTk;
    Ret rDst;  // destination expression type info

    if(exprUnary(&rDst)) {
        if(consume(ASSIGN)) {
            if(exprAssign(r)) {  // source expression type info stored in r
                // Type analysis for assignment
                if(!rDst.lval) {
                    tkerr("the assign destination must be a left-value");
                }
                if(rDst.ct) {
                    tkerr("the assign destination cannot be constant");
                }
                if(!canBeScalar(&rDst)) {
                    tkerr("the assign destination must be scalar");
                }
                if(!canBeScalar(r)) {
                    tkerr("the assign source must be scalar");
                }
                if(!convTo(&r->type, &rDst.type)) {
                    tkerr("the assign source cannot be converted to destination");
                }
              
                // Set the properties of the result
                r->lval = false;
                r->ct = true;
              
                return true;
            }
            tkerr("Invalid assignment expression");
        }
        iTk = start;  // undo exprUnary
    }

    // If not an assignment, try exprOr
    if(exprOr(r)) {  // type info will be stored in r
        return true;
    }
  
    return false;
}

// stm: stmCompound | IF LPAR expr RPAR stm (ELSE stm)? | WHILE LPAR expr RPAR stm | RETURN expr? SEMICOLON | expr? SEMICOLON
bool stm() {
    Ret rCond, rExpr;

    if(stmCompound(true)){
        return true;
    }

    if(consume(IF)){
        if(consume(LPAR)){
            if(expr(&rCond)){
                if(!canBeScalar(&rCond)) {
                    tkerr("the if condition must be a scalar value");
                }
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
            if(expr(&rCond)){
                if(!canBeScalar(&rCond)) {
                    tkerr("the while condition must be a scalar value");
                }
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
        bool hasExpr = expr(&rExpr);  // optional expression
        if(hasExpr) {
            if(owner->type.tb == TB_VOID) {
                tkerr("a void function cannot return a value");
            }
            if(!canBeScalar(&rExpr)) {
                tkerr("the return value must be a scalar value");
            }
            if(!convTo(&rExpr.type, &owner->type)) {
                tkerr("cannot convert the return expression type to the function return type");
            }
        } else {
            if(owner->type.tb != TB_VOID) {
                tkerr("a non-void function must return a value");
            }
        }
        if(consume(SEMICOLON)){
            return true;
        }
        tkerr("Missing ; after 'return' statement");
    }

    bool hasExpr = expr(&rExpr);  // optional expression
    if(consume(SEMICOLON)){
        return true;
    }
    return false;
}

//varDef: typeBase ID arrayDecl? SEMICOLON
bool varDef(){
	Token *start=iTk;
	Type t;
	if(typeBase(&t)){
		if(consume(ID)){
			Token *tkName = consumedTk;
			if(arrayDecl(&t)){
				if(t.n==0)tkerr("a vector variable must have a specified dimension");
			}

			if(consume(SEMICOLON)){
				Symbol *var=findSymbolInDomain(symTable,tkName->text);
				if(var)tkerr("symbol redefinition: %s",tkName->text);
				var=newSymbol(tkName->text,SK_VAR);
				var->type=t;
				var->owner=owner;
				addSymbolToDomain(symTable,var);
				if(owner){
					switch(owner->kind){
						case SK_FN:
							var->varIdx=symbolsLen(owner->fn.locals);
							addSymbolToList(&owner->fn.locals,dupSymbol(var));
							break;
						case SK_STRUCT:
							var->varIdx=typeSize(&owner->type);
							addSymbolToList(&owner->structMembers,dupSymbol(var));
							break;
						default: break;
					}
				}else{
					var->varMem=safeAlloc(typeSize(&t));
				}
				return true;
			}
			else tkerr("Expected \';\' after variable declaration");
		}
		else tkerr("Missing identifier after type base");
	}
	iTk=start;
	return false;
}

bool fnParam() {
    Type t;

    if(typeBase(&t)) {
        if(consume(ID)) {
            Token *tkName = consumedTk;

            if(arrayDecl(&t)) {
                t.n = 0;  // array without dimension for parameters
            }

            // Use addFnParam which handles all the parameter setup
            addFnParam(owner, tkName->text, t);
            return true;
        }
        tkerr("Missing ID for function parameter");
    }
    return false;
}

bool fnDef() {
    Token *start = iTk;
    Type t;  // Type information for the function
  
    // Check for function without return type
    if(iTk->code == ID && iTk->next && iTk->next->code == LPAR) {
        tkerr("Missing 'return type' in function declaration '%s'", iTk->text);
        return false;
    }

    // Get return type
    if(typeBase(&t)) {  // Pass type by reference
        // typeBase already filled t
    } else if(consume(TYPE_VOID)) {
        t.tb = TB_VOID;
        t.n = -1;  // not an array
        t.s = NULL;  // not a struct
    } else {
        return false;
    }

    if(consume(ID)) {
        Token *tkName = consumedTk;  // Store function name token

        if(consume(LPAR)) {
            // Check for function redefinition
            Symbol *fn = findSymbolInDomain(symTable, tkName->text);
            if(fn) {
                tkerr("symbol redefinition: %s", tkName->text);
                return false;
            }

            // Create new function symbol
            fn = newSymbol(tkName->text, SK_FN);
            fn->type = t;
            addSymbolToDomain(symTable, fn);
          
            // Set function as current owner and create new domain for parameters
            owner = fn;
            pushDomain();

            // Parse parameters
            if(fnParam()) {
                for(;;) {
                    Token *ahead = iTk;
                    // Check for missing comma between parameters
                    if(ahead->code == TYPE_INT || ahead->code == TYPE_DOUBLE || 
                       ahead->code == TYPE_CHAR || ahead->code == STRUCT) {
                        tkerr("Missing ',' between parameters in function '%s'", tkName->text);
                        dropDomain();  // Clean up domain on error
                        owner = NULL;
                        return false;
                    }
                  
                    if(consume(COMMA)) {
                        if(fnParam()) {
                            continue;
                        }
                        tkerr("Missing/Invalid 'type' for parameter in function '%s'", tkName->text);
                        dropDomain();  // Clean up domain on error
                        owner = NULL;
                        return false;
                    }
                    break;
                }
            }

            if(consume(RPAR)) {
                // Check for function body
                if(iTk->code != LACC) {
                    tkerr("Missing '{' in function '%s' body", tkName->text);
                    dropDomain();  // Clean up domain on error
                    owner = NULL;
                    return false;
                }

                // Parse function body
                // Note: false parameter indicates not to create a new domain
                if(!stmCompound(false)) {
                    tkerr("Missing '}' in function '%s' body", tkName->text);
                    dropDomain();  // Clean up domain on error
                    owner = NULL;
                    return false;
                }

                // Successfully parsed function, clean up domain
                dropDomain();
                owner = NULL;
                return true;
            }
          
            // Clean up on error
            dropDomain();
            owner = NULL;
            tkerr("Missing ')' in function '%s' declaration", tkName->text);
        }
    }
  
    iTk = start;
    return false;
}

//stmCompound: LACC ( varDef | stm )* RACC
bool stmCompound(bool newDomain){
	if(consume(LACC)){
		if(newDomain)pushDomain();
		while(varDef() || stm()){}
		if(consume(RACC)){
			if(newDomain)dropDomain();
			return true;
		}
		else tkerr("Missing } at the end of compound statement");
	}
	return false;
}

//structDef: STRUCT ID LACC varDef* RACC SEMICOLON
bool structDef(){
	Token *start=iTk;
	if(consume(STRUCT)){
		if(consume(ID)){

			Token *tkName = consumedTk;

			if(consume(LACC)){
				Symbol *s=findSymbolInDomain(symTable,tkName->text);
				if(s)tkerr("symbol redefinition: %s",tkName->text);
				
                s=addSymbolToDomain(symTable,newSymbol(tkName->text,SK_STRUCT));
				s->type.tb=TB_STRUCT;
				s->type.s=s;
				s->type.n=-1;
				
                pushDomain();
				owner=s;

				while(varDef()){}
				if(consume(RACC)){
					if(consume(SEMICOLON)){
						owner=NULL;
						dropDomain();
						return true;
					}
					else tkerr("Missing ; after struct definition");
				}
				else tkerr("Missing } in struct definition");
			}
		}
		else tkerr("Expected identifier after keyword 'struct'");
	}
	iTk=start;
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

void parse(Token *tokens) {
    iTk = tokens;

    if(!unit()) {
        tkerr("Invalid statement at the end of the line");
    }
    
    printf("\n\n[SUCCESS] Compilation was done successfully!\n");
}