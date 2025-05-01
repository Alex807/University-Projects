executable: Main.o Algorithm.o 
	gcc Algorithm.o Main.o -Wall -o executable 
	./executable

Algorithm.o: Algorithm.c Library.h
	gcc -c Algorithm.c 

Main.o: Main.c
	gcc -c Main.c 
 
clean: 
	del *.o *.bin executable.exe 