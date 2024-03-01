#ifndef __LIBRARY_C 
#define __LIBRARY_C 

long int calculate_matrix_deter(int** matrix, int matrix_size); 
//subfunctions for calculate_matrix_deter
void populate_matrix(int** matrix, int matrix_size, int seed); 
int** create_matrix(int matrix_size); 
void print_matrix(int** matrix, int matrix_size, char* matrix_simbol); 
void free_matrix_memory(int** matrix, int matrix_size);
int matrix_deter_size2(int** matrix); 
int matrix_deter_size3(int** matrix); 
long int matrix_deter_higher_order(int** matrix, int matrix_size); 
//



double** calculate_inverted_matrix(int** matrix, int matrix_size, long int determinant);

double** create_inverted_matrix(int matrix_size);
void print_inverted_matrix(double** matrix, int matrix_size); 
void free_inverted_matrix_memory(double** matrix, int matrix_size); 
int** calculate_transposed_matrix(int** matrix, int matrix_size);
int calculate_determinant_croped_matrix(int** matrix, int index_row_croped, int index_col_croped, int matrix_size);
int** calculate_adjunct_matrix(int** matrix, int matrix_size);  




int** calculate_multiplication_matrix(int** matrix1, int** matrix2, int matrix_size); 
int calculate_multiplication_matrix_element(int** matrix1, int** matrix2, int index_row_used, int index_col_used, int matrix_size);

#endif