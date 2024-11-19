#ifndef __AUXILIAR_LIBRARY_H
#define __AUXILIAR_LIBRARY_H  

#include "library.h"

#define MARGIN_OF_ERROR 0.0001

void generate_matrix(char *prompt, double** matrix);  
double** create_matrix();
int equal_matrixes(double** mat1, double** mat2); 
void copy_matrix_data (double** source, double** destination);
void print_matrix(double** matrix, char* matrix_simbol);
void initialize_matrix_serial_with_value(double** serial_result, int value); 
void initialize_matrix_parallel_with_value(double** parallel_result, int value); 

void validate_result_with_groundtruth_V1_serial (); 
int find_best_block_size(); 
void find_best_serial_parallel_time();

#endif // __AUXILIAR_LIBRARY_H