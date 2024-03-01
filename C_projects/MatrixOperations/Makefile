executable: algorithm.o Main.o  
	gcc algorithm.o Main.o -Wall -o executable
	./executable 

algorithm.o: algorithm.c library.h
	gcc -c algorithm.c 

Main.o: Main.c
	gcc -c Main.c 

clean: 
	del *.o executable.exe