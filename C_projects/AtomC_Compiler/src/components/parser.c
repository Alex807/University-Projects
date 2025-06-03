#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <stdbool.h>

#include "parser.h"
#include "ad.h"
#include "at.h"
#include "gc.h"
#include "utils.h" 
#include "lexer.h"

// Add this with other global variables
Symbol *owner = NULL;  // Initialize to NULL

Token *iTk;		// the iterator in the tokens list
Token *consumedTk;		// the last consumed token

bool expr();
bool stm();

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

// typeBase: TYPE_INT | TYPE_DOUBLE | TYPE_CHAR | STRUCT ID
bool typeBase(Type *t){
	t->n=-1;
	if(consume(TYPE_INT)){
		t->tb=TB_INT;
		return true;
		}
	if(consume(TYPE_DOUBLE)){
		t->tb=TB_DOUBLE;
		return true;
		}
	if(consume(TYPE_CHAR)){
		t->tb=TB_CHAR;
		return true;
		}
	if(consume(STRUCT)){
		if(consume(ID)){
			Token *tkName = consumedTk;
			t->tb=TB_STRUCT;
			t->s=findSymbol(tkName->text);
			if(!t->s) tkerr("structura nedefinita: %s",tkName->text);
			return true;
		}
	}
		//else tkerr("Missing identifier after \"struct\"");
	return false;
}

//arrayDecl: LBRACKET INT? RBRACKET
bool arrayDecl(Type *t){
	if(consume(LBRACKET)){
		if(consume(CONST_INT)){
			Token *tkSize = consumedTk;
			t->n=tkSize->i;
		}
		else t->n=0;
		if(consume(RBRACKET)){
			return true;
		}
		else tkerr("Missing right bracket in array declaration");
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
					else tkerr("Missing \';\' after struct definition");
				}
				else tkerr("Missing \'}\' in struct definition");
			}
		}
		else tkerr("Expected identifier after keyword \"struct\"");
	}
	iTk=start;
	return false;
}

//fnParam: typeBase ID arrayDecl?
bool fnParam(){
	Type t;
	if(typeBase(&t)){
		if(consume(ID)){
			Token *tkName = consumedTk;
			if(arrayDecl(&t)) t.n=0;
			Symbol *param=findSymbolInDomain(symTable,tkName->text);
			if(param)tkerr("symbol redefinition: %s",tkName->text);
			param=newSymbol(tkName->text,SK_PARAM);
			param->type=t;
			param->owner=owner;
			param->paramIdx=symbolsLen(owner->fn.params);
			// parametrul este adaugat atat la domeniul curent, cat si la parametrii fn
			addSymbolToDomain(symTable,param);
			addSymbolToList(&owner->fn.params,dupSymbol(param));
			return true;
		}
		else tkerr("Expected identifier after base type");
	}
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
		else tkerr("Missing \'}\' at the end of compound statement");
	}
	return false;
}

// fnDef: ( typeBase | VOID ) ID
// 		LPAR ( fnParam ( COMMA fnParam )* )? RPAR
// 		stmCompound
bool fnDef(){
	Token *start=iTk;
	Instr *startInstr=owner?lastInstr(owner->fn.instr):NULL;
	Type t;
	if(typeBase(&t)){
		if(consume(ID)){
			Token *tkName = consumedTk;
			if(consume(LPAR)){
				Symbol *fn=findSymbolInDomain(symTable,tkName->text);
				if(fn)tkerr("symbol redefinition: %s",tkName->text);
				fn=newSymbol(tkName->text,SK_FN);
				fn->type=t;
				addSymbolToDomain(symTable,fn);
				owner=fn;
				pushDomain();

				if(fnParam()){
					while(consume(COMMA)){
						if(fnParam()){}
						else tkerr("Expected function parameter after \',\'");
					}
				}
				if(consume(RPAR)){
					addInstr(&fn->fn.instr,OP_ENTER);
					if(stmCompound(false)){
						fn->fn.instr->arg.i=symbolsLen(fn->fn.locals);
						if(fn->type.tb==TB_VOID) addInstrWithInt(&fn->fn.instr,OP_RET_VOID,symbolsLen(fn->fn.params));
						dropDomain();
						owner=NULL;
						return true;
					}
					else tkerr("Expected statement after function header");
				}
				else tkerr("Missing \')\' in function definition");
			}
		}
		else tkerr("Expected identifier after base type");
	}
	iTk=start;
	if(owner)delInstrAfter(startInstr);
	if(consume(TYPE_VOID)){
		t.tb=TB_VOID;
		if(consume(ID)){
			Token *tkName = consumedTk;
			if(consume(LPAR)){
				Symbol *fn=findSymbolInDomain(symTable,tkName->text);
				if(fn)tkerr("symbol redefinition: %s",tkName->text);
				fn=newSymbol(tkName->text,SK_FN);
				fn->type=t;
				addSymbolToDomain(symTable,fn);
				owner=fn;
				pushDomain();

				if(fnParam()){
					while(consume(COMMA)){
						if(fnParam()){}
						else tkerr("Expected function parameter after \',\'");
					}
				}
				if(consume(RPAR)){
					addInstr(&fn->fn.instr,OP_ENTER);
					if(stmCompound(false)){
						fn->fn.instr->arg.i=symbolsLen(fn->fn.locals);
						if(fn->type.tb==TB_VOID) addInstrWithInt(&fn->fn.instr,OP_RET_VOID,symbolsLen(fn->fn.params));
						dropDomain();
						owner=NULL;
						return true;
					}
					else tkerr("Expected statement after function header");
				}
				else tkerr("Missing \')\' in function definition");
			}
		}
		else tkerr("Expected identifier after void type");
	}
	iTk=start;
	if(owner)delInstrAfter(startInstr);
	return false;
}

// ID-ul trebuie sa existe in TS
// Doar functiile pot fi apelate
// O functie poate fi doar apelata
// Apelul unei functii trebuie sa aiba acelasi numar de argumente ca si numarul de parametri de la definitia ei
// Tipurile argumentelor de la apelul unei functii trebuie sa fie convertibile la tipurile parametrilor functiei
//exprPrimary: ID ( LPAR ( expr ( COMMA expr )* )? RPAR )?
//				| INT | DOUBLE | CHAR | STRING | LPAR expr RPAR
bool exprPrimary(Ret *r){
	Token *start=iTk;
	Instr *startInstr=owner?lastInstr(owner->fn.instr):NULL;
	if(consume(ID)){
		Token *tkName = consumedTk;
		Symbol *s=findSymbol(tkName->text);
		if(!s)tkerr("undefined id: %s",tkName->text);
		if(consume(LPAR)){
			if(s->kind!=SK_FN)tkerr("only a function can be called");
			Ret rArg;
			Symbol *param=s->fn.params;
			if(expr(&rArg)){
				if(!param)tkerr("too many arguments in function call");
				if(!convTo(&rArg.type,&param->type))tkerr("in call, cannot convert the argument type to the parameter type");
				addRVal(&owner->fn.instr,rArg.lval,&rArg.type);
				insertConvIfNeeded(lastInstr(owner->fn.instr),&rArg.type,&param->type);
				param=param->next;
				while(consume(COMMA)){
					if(expr(&rArg)){
						if(!param)tkerr("too many arguments in function call");
						if(!convTo(&rArg.type,&param->type))tkerr("in call, cannot convert the argument type to the parameter type");
						addRVal(&owner->fn.instr,rArg.lval,&rArg.type);
						insertConvIfNeeded(lastInstr(owner->fn.instr),&rArg.type,&param->type);
						param=param->next;
					}
					else tkerr("Expected expression after \',\'");
				}
			}
			if(consume(RPAR)){
				if(param)tkerr("too few arguments in function call");
				*r=(Ret){s->type,false,true};
				if(s->fn.extFnPtr){
					addInstr(&owner->fn.instr,OP_CALL_EXT)->arg.extFnPtr=s->fn.extFnPtr;
				}else{
					addInstr(&owner->fn.instr,OP_CALL)->arg.instr=s->fn.instr;
				}
			}
			else tkerr("Missing \')\' in function call");
		}
		else{
			if(s->kind==SK_FN)tkerr("a function can only be called");
			*r=(Ret){s->type,true,s->type.n>=0};
			if(s->kind==SK_VAR){
				if(s->owner==NULL){ // global variables
					addInstr(&owner->fn.instr,OP_ADDR)->arg.p=s->varMem;
				}else{ // local variables
					switch(s->type.tb){
						case TB_INT:addInstrWithInt(&owner->fn.instr,OP_FPADDR_I,s->varIdx+1);break;
						case TB_DOUBLE:addInstrWithInt(&owner->fn.instr,OP_FPADDR_D,s->varIdx+1);break;
						default: break;
					}
				}
			}
			if(s->kind==SK_PARAM){
				switch(s->type.tb){
					case TB_INT:
						addInstrWithInt(&owner->fn.instr,OP_FPADDR_I,s->paramIdx-symbolsLen(s->owner->fn.params)-1); break;
					case TB_DOUBLE:
						addInstrWithInt(&owner->fn.instr,OP_FPADDR_D,s->paramIdx-symbolsLen(s->owner->fn.params)-1); break;
					default: break;
				}
			}
		}
		return true;
	}
	if(consume(CONST_INT)){
		Token *ct = consumedTk;
		*r=(Ret){{TB_INT,NULL,-1},false,true};
		addInstrWithInt(&owner->fn.instr,OP_PUSH_I,ct->i);
		return true;
	} 
	if(consume(CONST_DOUBLE)){
		Token *ct = consumedTk;
		*r=(Ret){{TB_DOUBLE,NULL,-1},false,true};
		addInstrWithDouble(&owner->fn.instr,OP_PUSH_D,ct->d);
		return true;
	}
	if(consume(CONST_CHAR)){
		//Token *ct = consumedTk;
		*r=(Ret){{TB_CHAR,NULL,-1},false,true};
		return true;
	}
	if(consume(STRING)){
		//Token *ct = consumedTk;
		*r=(Ret){{TB_CHAR,NULL,0},false,true};
		return true;
	}
	if(consume(LPAR)){
		if(expr(r)){
			if(consume(RPAR)){
				return true;
			}
			else tkerr("Missing \')\' after expression");
		}
		//else tkerr("Expected expression after \'(\'");
	}
	iTk=start;
	if(owner)delInstrAfter(startInstr);
	return false;
}

// Doar un array poate fi indexat
// Indexul in array trebuie sa fie convertibil la int
// Operatorul de selectie a unui camp de structura se poate aplica doar structurilor
// Campul unei structuri trebuie sa existe
//exprPostfixPrim: LBRACKET expr RBRACKET exprPostfixPrim
//					| DOT ID exprPostfixPrim
//					| ε
bool exprPostfixPrim(Ret *r){
	Token *start=iTk;
	if(consume(LBRACKET)){
		Ret idx;
		if(expr(&idx)){
			if(consume(RBRACKET)){
				if(r->type.n<0)tkerr("only an array can be indexed");
				Type tInt={TB_INT,NULL,-1};
				if(!convTo(&idx.type,&tInt))tkerr("the index is not convertible to int");
				r->type.n=-1;
				r->lval=true;
				r->ct=false;
				if(exprPostfixPrim(r)){
					return true;
				}
			}
			else tkerr("Missing \']\' in array indexation");
		}
		else tkerr("Expected expression in array indexation");
	}
	iTk=start;
	if(consume(DOT)){
		if(consume(ID)){
			Token *tkName = consumedTk;
			if(r->type.tb!=TB_STRUCT)tkerr("a field can only be selected from a struct");
			Symbol *s=findSymbolInList(r->type.s->structMembers,tkName->text);
			if(!s)tkerr("the structure %s does not have a field %s",r->type.s->name,tkName->text);
			*r=(Ret){s->type,true,s->type.n>=0};
			if(exprPostfixPrim(r)){
				return true;
			}
		}
		else tkerr("Expected identifier after after dot operator");
	}
	iTk=start;
	return true;
}

//exprPostfix: exprPrimary exprPostfixPrim
bool exprPostfix(Ret *r){
	if(exprPrimary(r)){
		if(exprPostfixPrim(r)){
			return true;
		}
	}
	return false;
}

// Minus unar si Not trebuie sa aiba un operand scalar
// Rezultatul lui Not este un int
//exprUnary: ( SUB | NOT ) exprUnary | exprPostfix
bool exprUnary(Ret *r){
	if(consume(SUB)){
		if(exprUnary(r)){
			if(!canBeScalar(r))tkerr("unary - must have a scalar operand");
			r->lval=false;
			r->ct=true;
			return true;
		}
		else tkerr("Missing operand after negation");
	}
	if(consume(NOT)){
		if(exprUnary(r)){
			if(!canBeScalar(r))tkerr("unary ! must have a scalar operand");
			r->lval=false;
			r->ct=true;
			return true;
		}
		else tkerr("Missing operand after logical negation");
	}
	if(exprPostfix(r)){
		return true;
	}
	return false;
}

// Structurile nu se pot converti
// Tipul la care se converteste nu poate fi structura
// Un array se poate converti doar la alt array
// Un scalar se poate converti doar la alt scalar
//exprCast: LPAR typeBase arrayDecl? RPAR exprCast | exprUnary
bool exprCast(Ret *r){
	Token *start=iTk;
	if(consume(LPAR)){
		Type t;Ret op;
		if(typeBase(&t)){
			arrayDecl(&t);
			if(consume(RPAR)){
				if(exprCast(&op)){
					if(t.tb==TB_STRUCT)tkerr("cannot convert to a struct type");
					if(op.type.tb==TB_STRUCT)tkerr("cannot convert a struct");
					if(op.type.n>=0&&t.n<0)tkerr("an array can be converted only to another array");
					if(op.type.n<0&&t.n>=0)tkerr("a scalar can be converted only to another scalar");
					*r=(Ret){t,false,true};
					return true;
				}
			}
			else tkerr("Missing \')\' in cast operation");
		}
	}
	iTk=start;
	if(exprUnary(r)){
		return true;
	}
	return false;
}

// Ambii operanzi trebuie sa fie scalari si sa nu fie structuri
//exprMulPrim: ( MUL | DIV ) exprCast exprMulPrim | ε
bool exprMulPrim(Ret *r){
	Token *op;
	if(consume(MUL)){
		op = consumedTk;
		Ret right;
		Instr *lastLeft=lastInstr(owner->fn.instr);
		addRVal(&owner->fn.instr,r->lval,&r->type);
		if(exprCast(&right)){
			Type tDst;
			if(!arithTypeTo(&r->type,&right.type,&tDst))tkerr("invalid operand type for *");
			addRVal(&owner->fn.instr,right.lval,&right.type);
			insertConvIfNeeded(lastLeft,&r->type,&tDst);
			insertConvIfNeeded(lastInstr(owner->fn.instr),&right.type,&tDst);
			switch(op->code){
				case MUL:
					switch(tDst.tb){
						case TB_INT:addInstr(&owner->fn.instr,OP_MUL_I);break;
						case TB_DOUBLE:addInstr(&owner->fn.instr,OP_MUL_D);break;
						default: break;
					}
					break;
				case DIV:
					switch(tDst.tb){
						case TB_INT:addInstr(&owner->fn.instr,OP_DIV_I);break;
						case TB_DOUBLE:addInstr(&owner->fn.instr,OP_DIV_D);break;
						default: break;
					}
					break;
				default: break;
			}
			*r=(Ret){tDst,false,true};
			if(exprMulPrim(r)){
				return true;
			}
		}
		else tkerr("Missing right operand after \'*\' operator");
	}
	if(consume(DIV)){
		op = consumedTk;
		Ret right;
		Instr *lastLeft=lastInstr(owner->fn.instr);
		addRVal(&owner->fn.instr,r->lval,&r->type);
		if(exprCast(&right)){
			Type tDst;
			if(!arithTypeTo(&r->type,&right.type,&tDst))tkerr("invalid operand type for /");
			addRVal(&owner->fn.instr,right.lval,&right.type);
			insertConvIfNeeded(lastLeft,&r->type,&tDst);
			insertConvIfNeeded(lastInstr(owner->fn.instr),&right.type,&tDst);
			switch(op->code){
				case MUL:
					switch(tDst.tb){
						case TB_INT:addInstr(&owner->fn.instr,OP_MUL_I);break;
						case TB_DOUBLE:addInstr(&owner->fn.instr,OP_MUL_D);break;
						default: break;
					}
					break;
				case DIV:
					switch(tDst.tb){
						case TB_INT:addInstr(&owner->fn.instr,OP_DIV_I);break;
						case TB_DOUBLE:addInstr(&owner->fn.instr,OP_DIV_D);break;
						default: break;
					}
					break;
				default: break;
			}
			*r=(Ret){tDst,false,true};
			if(exprMulPrim(r)){
				return true;
			}
		}
		else tkerr("Missing right operand after \'/\' operator");
	}
	return true;
}

//exprMul: exprCast exprMulPrim
bool exprMul(Ret *r){
	if(exprCast(r)){
		if(exprMulPrim(r)){
			return true;
		}
	}
	return false;
}

// Ambii operanzi trebuie sa fie scalari si sa nu fie structuri
//exprAddPrim: ( ADD | SUB ) exprMul exprAddPrim | e
bool exprAddPrim(Ret *r){
	Token *op;
	if(consume(ADD)){
		op = consumedTk;
		Ret right;
		Instr *lastLeft=lastInstr(owner->fn.instr);
		addRVal(&owner->fn.instr,r->lval,&r->type);
		if(exprMul(&right)){
			Type tDst;
			if(!arithTypeTo(&r->type,&right.type,&tDst))tkerr("invalid operand type for +");
			addRVal(&owner->fn.instr,right.lval,&right.type);
			insertConvIfNeeded(lastLeft,&r->type,&tDst);
			insertConvIfNeeded(lastInstr(owner->fn.instr),&right.type,&tDst);
			switch(op->code){
				case ADD:
					switch(tDst.tb){
						case TB_INT:addInstr(&owner->fn.instr,OP_ADD_I);break;
						case TB_DOUBLE:addInstr(&owner->fn.instr,OP_ADD_D);break;
						default: break;
					}
					break;
				case SUB:
					switch(tDst.tb){
						case TB_INT:addInstr(&owner->fn.instr,OP_SUB_I);break;
						case TB_DOUBLE:addInstr(&owner->fn.instr,OP_SUB_D);break;
						default: break;
					}
					break;
				default: break;
			}
			*r=(Ret){tDst,false,true};
			if(exprAddPrim(r)){
				return true;
			}
		}
		else tkerr("Missing right operand after \'+\' operator");
	}
	if(consume(SUB)){
		op = consumedTk;
		Ret right;
		Instr *lastLeft=lastInstr(owner->fn.instr);
		addRVal(&owner->fn.instr,r->lval,&r->type);
		if(exprMul(&right)){
			Type tDst;
			if(!arithTypeTo(&r->type,&right.type,&tDst))tkerr("invalid operand type for -");
			addRVal(&owner->fn.instr,right.lval,&right.type);
			insertConvIfNeeded(lastLeft,&r->type,&tDst);
			insertConvIfNeeded(lastInstr(owner->fn.instr),&right.type,&tDst);
			switch(op->code){
				case ADD:
					switch(tDst.tb){
						case TB_INT:addInstr(&owner->fn.instr,OP_ADD_I);break;
						case TB_DOUBLE:addInstr(&owner->fn.instr,OP_ADD_D);break;
						default: break;
					}
					break;
				case SUB:
					switch(tDst.tb){
						case TB_INT:addInstr(&owner->fn.instr,OP_SUB_I);break;
						case TB_DOUBLE:addInstr(&owner->fn.instr,OP_SUB_D);break;
						default: break;
					}
					break;
				default: break;
			}
			*r=(Ret){tDst,false,true};
			if(exprAddPrim(r)){
				return true;
			}
		}
		else tkerr("Missing right operand after \'-\' operator");
	}
	return true;
}

//exprAdd: exprMul exprAddPrim
bool exprAdd(Ret *r){
	if(exprMul(r)){
		if(exprAddPrim(r)){
			return true;
		}
	}
	return false;
}

// Ambii operanzi trebuie sa fie scalari si sa nu fie structuri
// Rezultatul este un int
//exprRelPrim: ( LESS | LESSEQ | GREATER | GREATEREQ ) exprAdd exprRelPrim | ε
bool exprRelPrim(Ret *r){
	Token *op;
	if(consume(LESS)){
		op = consumedTk;
		Ret right;
		Instr *lastLeft=lastInstr(owner->fn.instr);
		addRVal(&owner->fn.instr,r->lval,&r->type);
		if(exprAdd(&right)){
			Type tDst;
			if(!arithTypeTo(&r->type,&right.type,&tDst))tkerr("invalid operand type for <");
			addRVal(&owner->fn.instr,right.lval,&right.type);
			insertConvIfNeeded(lastLeft,&r->type,&tDst);
			insertConvIfNeeded(lastInstr(owner->fn.instr),&right.type,&tDst);
			switch(op->code){
				case LESS:
					switch(tDst.tb){
						case TB_INT:addInstr(&owner->fn.instr,OP_LESS_I);break;
						case TB_DOUBLE:addInstr(&owner->fn.instr,OP_LESS_D);break;
						default: break;
					}
					break;
				default: break;
			}
			*r=(Ret){{TB_INT,NULL,-1},false,true};
			if(exprRelPrim(r)){
				return true;
			}
		}
		else tkerr("Missing right operand after \'<\' operator");
	}
	if(consume(LESSEQ)){
		op = consumedTk;
		Ret right;
		Instr *lastLeft=lastInstr(owner->fn.instr);
		addRVal(&owner->fn.instr,r->lval,&r->type);
		if(exprAdd(&right)){
			Type tDst;
			if(!arithTypeTo(&r->type,&right.type,&tDst))tkerr("invalid operand type for <=");
			addRVal(&owner->fn.instr,right.lval,&right.type);
			insertConvIfNeeded(lastLeft,&r->type,&tDst);
			insertConvIfNeeded(lastInstr(owner->fn.instr),&right.type,&tDst);
			switch(op->code){
				case LESS:
					switch(tDst.tb){
						case TB_INT:addInstr(&owner->fn.instr,OP_LESS_I);break;
						case TB_DOUBLE:addInstr(&owner->fn.instr,OP_LESS_D);break;
						default: break;
					}
					break;
				default: break;
			}
			*r=(Ret){{TB_INT,NULL,-1},false,true};
			if(exprRelPrim(r)){
				return true;
			}
		}
		else tkerr("Missing right operand after \'<=\' operator");
	}
	if(consume(GREATER)){
		op = consumedTk;
		Ret right;
		Instr *lastLeft=lastInstr(owner->fn.instr);
		addRVal(&owner->fn.instr,r->lval,&r->type);
		if(exprAdd(&right)){
			Type tDst;
			if(!arithTypeTo(&r->type,&right.type,&tDst))tkerr("invalid operand type for >");
			addRVal(&owner->fn.instr,right.lval,&right.type);
			insertConvIfNeeded(lastLeft,&r->type,&tDst);
			insertConvIfNeeded(lastInstr(owner->fn.instr),&right.type,&tDst);
			switch(op->code){
				case LESS:
					switch(tDst.tb){
						case TB_INT:addInstr(&owner->fn.instr,OP_LESS_I);break;
						case TB_DOUBLE:addInstr(&owner->fn.instr,OP_LESS_D);break;
						default: break;
					}
					break;
				default: break;
			}
			*r=(Ret){{TB_INT,NULL,-1},false,true};
			if(exprRelPrim(r)){
				return true;
			}
		}
		else tkerr("Missing right operand after \'>\' operator");
	}
	if(consume(GREATEREQ)){
		op = consumedTk;
		Ret right;
		Instr *lastLeft=lastInstr(owner->fn.instr);
		addRVal(&owner->fn.instr,r->lval,&r->type);
		if(exprAdd(&right)){
			Type tDst;
			if(!arithTypeTo(&r->type,&right.type,&tDst))tkerr("invalid operand type for >=");
			addRVal(&owner->fn.instr,right.lval,&right.type);
			insertConvIfNeeded(lastLeft,&r->type,&tDst);
			insertConvIfNeeded(lastInstr(owner->fn.instr),&right.type,&tDst);
			switch(op->code){
				case LESS:
					switch(tDst.tb){
						case TB_INT:addInstr(&owner->fn.instr,OP_LESS_I);break;
						case TB_DOUBLE:addInstr(&owner->fn.instr,OP_LESS_D);break;
						default: break;
					}
					break;
				default: break;
			}
			*r=(Ret){{TB_INT,NULL,-1},false,true};
			if(exprRelPrim(r)){
				return true;
			}
		}
		else tkerr("Missing right operand after \'>=\' operator");
	}
	return true;
}

//exprRel: exprAdd exprRelPrim
bool exprRel(Ret *r){
	if(exprAdd(r)){
		if(exprRelPrim(r)){
			return true;
		}
	}
	return false;
}

// Ambii operanzi trebuie sa fie scalari si sa nu fie structuri
// Rezultatul este un int
//exprEqPrim: ( EQUAL | NOTEQ ) exprRel exprEqPrim | ε
bool exprEqPrim(Ret *r){
	if(consume(EQUAL)){
		Ret right;
		if(exprRel(&right)){
			Type tDst;
			if(!arithTypeTo(&r->type,&right.type,&tDst))tkerr("invalid operand type for ==");
			*r=(Ret){{TB_INT,NULL,-1},false,true};
			if(exprEqPrim(r)){
				return true;
			}
		}
		else tkerr("Missing right operand after \'==\' operator");
	}
	if(consume(NOTEQ)){
		Ret right;
		if(exprRel(&right)){
			Type tDst;
			if(!arithTypeTo(&r->type,&right.type,&tDst))tkerr("invalid operand type for !=");
			*r=(Ret){{TB_INT,NULL,-1},false,true};
			if(exprEqPrim(r)){
				return true;
			}
		}
		else tkerr("Missing right operand after \'!=\' operator");
	}
	return true;
}

//exprEq: exprRel exprEqPrim
bool exprEq(Ret *r){
	if(exprRel(r)){
		if(exprEqPrim(r)){
			return true;
		}
	}
	return false;
}

// Ambii operanzi trebuie sa fie scalari si sa nu fie structuri
// Rezultatul este un int
//exprAndPrim: AND exprEq exprAndPrim | ε
bool exprAndPrim(Ret* r){
	if(consume(AND)){
		Ret right;
		if(exprEq(&right)){
			Type tDst;
			if(!arithTypeTo(&r->type,&right.type,&tDst))tkerr("invalid operand type for &&");
			*r=(Ret){{TB_INT,NULL,-1},false,true};
			if(exprAndPrim(r)){
				return true;
			}
		}
		else tkerr("Missing right operand after \'&&\' operator");
	}
	return true;
}

//exprAnd: exprEq exprAndPrim
bool exprAnd(Ret* r){
	if(exprEq(r)){
		if(exprAndPrim(r)){
			return true;
		}
	}
	return false;
}

// Ambii operanzi trebuie sa fie scalari si sa nu fie structuri
// Rezultatul este un int
//exprOrPrim: OR exprAnd exprOrPrim | ε
bool exprOrPrim(Ret* r){
	if(consume(OR)){
		Ret right;
		if(exprAnd(&right)){
			Type tDst;
			if(!arithTypeTo(&r->type,&right.type,&tDst))tkerr("invalid operand type for ||");
			*r=(Ret){{TB_INT,NULL,-1},false,true};
			if(exprOrPrim(r)){
				return true;
			}
		}
		else tkerr("Missing right operand after \'||\' operator");
	}
	return true;
}

//exprOr: exprAnd exprOrPrim
bool exprOr(Ret *r){
	if(exprAnd(r)){
		if(exprOrPrim(r)){
			return true;
		}
	}
	return false;
}

// Destinatia trebuie sa fie left-value
// Destinatia nu trebuie sa fie constanta
// Ambii operanzi trebuie sa fie scalari
// Sursa trebuie sa fie convertibila la destinatie
// Tipul rezultat este tipul sursei
//exprAssign: exprUnary ASSIGN exprAssign | exprOr
bool exprAssign(Ret *r){
	Token *start=iTk;
	Instr *startInstr=owner?lastInstr(owner->fn.instr):NULL;
	Ret rDst;
	if(exprUnary(&rDst)){
		if(consume(ASSIGN)){
			if(exprAssign(r)){
				if(!rDst.lval)tkerr("the assign destination must be a left-value");
				if(rDst.ct)tkerr("the assign destination cannot be constant");
				if(!canBeScalar(&rDst))tkerr("the assign destination must be scalar");
				if(!canBeScalar(r))tkerr("the assign source must be scalar");
				if(!convTo(&r->type,&rDst.type))tkerr("the assign source cannot be converted to destination");
				r->lval=false;
				r->ct=true;
				addRVal(&owner->fn.instr,r->lval,&r->type);
				insertConvIfNeeded(lastInstr(owner->fn.instr),&r->type,&rDst.type);
				switch(rDst.type.tb){
					case TB_INT:addInstr(&owner->fn.instr,OP_STORE_I);break;
					case TB_DOUBLE:addInstr(&owner->fn.instr,OP_STORE_D);break;
					default: break;
				}
				return true;
			}
			else tkerr("Missing right operand in assign operation");
		}
	}
	iTk=start;
	if(owner)delInstrAfter(startInstr);
	if(exprOr(r)){
		return true;
	}
	return false;
}

//expr: exprAssign
bool expr(Ret *r){
	if(exprAssign(r)){
		return true;
	}
	return false;
}

// IF - conditia trebuie sa fie scalar
// WHILE - conditia trebuie sa fie scalar
// RETURN - expresia trebuie sa fie scalar
// RETURN - functiile void nu pot returna o valoare
// RETURN - functiile non-void trebuie sa aiba o expresie returnata, a carei tip sa fie convertibil la tipul returnat de functie
/*stm: stmCompound
	| IF LPAR expr RPAR stm ( ELSE stm )?
	| WHILE LPAR expr RPAR stm
	| RETURN expr? SEMICOLON
	| expr? SEMICOLON*/
bool stm(){
	Token *start=iTk;
	Instr *startInstr=owner?lastInstr(owner->fn.instr):NULL;
	Ret rCond,rExpr;
	if(stmCompound(true)) return true;
	if(consume(IF)){
		if(consume(LPAR)){
			if(expr(&rCond)){
				if(!canBeScalar(&rCond))tkerr("the if condition must be a scalar value");
				if(consume(RPAR)){
					addRVal(&owner->fn.instr,rCond.lval,&rCond.type);
					Type intType={TB_INT,NULL,-1};
					insertConvIfNeeded(lastInstr(owner->fn.instr),&rCond.type,&intType);
					Instr *ifJF=addInstr(&owner->fn.instr,OP_JF);
					if(stm()){
						if(consume(ELSE)){
							Instr *ifJMP=addInstr(&owner->fn.instr,OP_JMP);
							ifJF->arg.instr=addInstr(&owner->fn.instr,OP_NOP);
							if(stm()){
								ifJMP->arg.instr=addInstr(&owner->fn.instr,OP_NOP);
							}
							else tkerr("Expected expected statement after else");
						}
						else ifJF->arg.instr=addInstr(&owner->fn.instr,OP_NOP);
						return true;
					}
					else tkerr("Expected expected statement after if");
				}
				else tkerr("Expected \')\' after if condition");
			}
			else tkerr("Expected expression inside of if condition");
		}
		else tkerr("Expected \'(\' after if keyword");
	}
	if(consume(WHILE)){
		Instr *beforeWhileCond=lastInstr(owner->fn.instr);
		if(consume(LPAR)){
			if(expr(&rCond)){
				if(!canBeScalar(&rCond))tkerr("the while condition must be a scalar value");
				if(consume(RPAR)){
					addRVal(&owner->fn.instr,rCond.lval,&rCond.type);
					Type intType={TB_INT,NULL,-1};
					insertConvIfNeeded(lastInstr(owner->fn.instr),&rCond.type,&intType);
					Instr *whileJF=addInstr(&owner->fn.instr,OP_JF);
					if(stm()){
						addInstr(&owner->fn.instr,OP_JMP)->arg.instr=beforeWhileCond->next;
						whileJF->arg.instr=addInstr(&owner->fn.instr,OP_NOP);
						return true;
					}
					else tkerr("Expected expected statement after while");
				}
				else tkerr("Expected \')\' after while condition");
			}
			else tkerr("Expected expression inside of while condition");
		}
		else tkerr("Expected \'(\' after while keyword");
	}
	if(consume(RETURN)){
		if(expr(&rExpr)){
			if(owner->type.tb==TB_VOID)tkerr("a void function cannot return a value");
			if(!canBeScalar(&rExpr))tkerr("the return value must be a scalar value");
			if(!convTo(&rExpr.type,&owner->type))tkerr("cannot convert the return expression type to the function return type");
			addRVal(&owner->fn.instr,rExpr.lval,&rExpr.type);
			insertConvIfNeeded(lastInstr(owner->fn.instr),&rExpr.type,&owner->type);
			addInstrWithInt(&owner->fn.instr,OP_RET,symbolsLen(owner->fn.params));
		}
		else{
			if(owner->type.tb!=TB_VOID)tkerr("a non-void function must return a value");
			addInstr(&owner->fn.instr,OP_RET_VOID);
		}
		if(consume(SEMICOLON)){
			return true;
		}
		else tkerr("Expected \';\' at the end of return");
	}
	if(expr(&rExpr)){
		if(rExpr.type.tb!=TB_VOID)addInstr(&owner->fn.instr,OP_DROP);
	}
	if(consume(SEMICOLON)){
		return true;
	}
	iTk=start;
	if(owner)delInstrAfter(startInstr);
	return false;

}

// unit: ( structDef | fnDef | varDef )* END
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
