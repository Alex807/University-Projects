executable: implementation.o auxiliarFunctions.o Main.o  
	gcc implementation.o auxiliarFunctions.o Main.o -Wall -fopenmp -lm -o executable
	./executable  
	make clean

implementation.o: implementation.c library.h auxiliarLibrary.h
	gcc -c implementation.c 

auxiliarFunctions.o: auxiliarFunctions.c auxiliarLibrary.h
	gcc -c auxiliarFunctions.c

Main.o: Main.c
	gcc -c Main.c 

clean: 
	rm *.o executable