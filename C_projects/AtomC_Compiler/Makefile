SRC_DIR = src/components
INCLUDE_DIR = src/include
MAIN_DIR = src
OBJ_DIR = bin

# @ - silent mode for commands
$(OBJ_DIR)\executable.exe: $(OBJ_DIR)\lexer.o $(OBJ_DIR)\utils.o $(OBJ_DIR)\parser.o $(OBJ_DIR)\main.o
	@gcc $(OBJ_DIR)\lexer.o $(OBJ_DIR)\utils.o $(OBJ_DIR)\parser.o $(OBJ_DIR)\main.o -Wall -o $(OBJ_DIR)\executable.exe
	@$(OBJ_DIR)\executable.exe 
	@del $(OBJ_DIR)\executable.exe
#	@del $(OBJ_DIR)\*.o $(OBJ_DIR)\*.exe (to delete all bin directory files)

$(OBJ_DIR)\lexer.o: $(SRC_DIR)/lexer.c $(INCLUDE_DIR)\lexer.h
	@gcc -c $(SRC_DIR)/lexer.c -I $(INCLUDE_DIR) -o $(OBJ_DIR)\lexer.o

$(OBJ_DIR)\utils.o: $(SRC_DIR)/utils.c $(INCLUDE_DIR)\utils.h
	@gcc -c $(SRC_DIR)/utils.c -I $(INCLUDE_DIR) -o $(OBJ_DIR)\utils.o

$(OBJ_DIR)\parser.o: $(SRC_DIR)/parser.c $(INCLUDE_DIR)\parser.h
	@gcc -c $(SRC_DIR)/parser.c -I $(INCLUDE_DIR) -o $(OBJ_DIR)\parser.o

$(OBJ_DIR)\main.o: $(MAIN_DIR)/main.c 
	@gcc -c $(MAIN_DIR)/main.c -I $(INCLUDE_DIR) -o $(OBJ_DIR)\main.o

clean: 
	del $(OBJ_DIR)\*.o $(OBJ_DIR)\*.exe 