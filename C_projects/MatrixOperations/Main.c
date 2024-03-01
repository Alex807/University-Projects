#include <stdio.h> 
#include <stdlib.h>
#include <time.h>  

#include "library.h"

int main(void)
{
    int matrix_size;
    do
    {
        printf("\nDimensiune matrice(intre 1 si 10): "); 
        scanf("%d", &matrix_size); 

        if (matrix_size<1 || matrix_size>10) {
            printf("Dimensiune INVALIDA !!\n");
        } 
    
    }while (matrix_size<1 || matrix_size>10);

    srand(time(NULL)); //populate matrix

    
    int** matrix=create_matrix(matrix_size);
    populate_matrix(matrix, matrix_size, 5); //last argument is seed for random numbers generator

    char matrix_simbol[] = "A";
    print_matrix(matrix, matrix_size, matrix_simbol); 

    long int determinant_matrix = calculate_matrix_deter(matrix, matrix_size); 
    printf("det(%s) = %ld\n\n", matrix_simbol, determinant_matrix);

    int exit_value=0;//use this variable to check if the program has ended correctly
    if(determinant_matrix == 0)
    { 
        printf("Matricea afisata nu poate fi inversata!\n"); 
        exit_value = -1;

    }
    else 
    {
        double** inverted_matrix=calculate_inverted_matrix(matrix, matrix_size, determinant_matrix);
        print_inverted_matrix(inverted_matrix, matrix_size);
        
        free_inverted_matrix_memory(inverted_matrix, matrix_size); 
    } 

    free_matrix_memory(matrix, matrix_size);

    //now we calculate the multiplication of two matrices
    int** matrix1 = create_matrix(matrix_size);  
    int** matrix2 = create_matrix(matrix_size);

    char simbol_matrix1[] = "A"; 
    char simbol_matrix2[] = "B"; 
    char simbol_matrix_multiplication[] = "AxB";

    populate_matrix(matrix1, matrix_size, 3); //last argument is seed for random numbers generator
    populate_matrix(matrix2, matrix_size, 4); 

    print_matrix(matrix1, matrix_size, simbol_matrix1); 
    print_matrix(matrix2, matrix_size, simbol_matrix2);

    int** result_matrix_multiplication = calculate_multiplication_matrix(matrix1, matrix2, matrix_size);
    print_matrix(result_matrix_multiplication, matrix_size, simbol_matrix_multiplication);

    free_matrix_memory(result_matrix_multiplication, matrix_size);
    free_matrix_memory(matrix1, matrix_size); 
    free_matrix_memory(matrix2, matrix_size); 

    return exit_value;
} 
