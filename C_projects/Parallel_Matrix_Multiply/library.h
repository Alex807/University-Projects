#ifndef __LIBRARY_H
#define __LIBRARY_H 

#define MATRIX_SIZE 2000

#define BLOCK_SIZE 32
void block_matrix_multiplication_serial(int block_size); 
void block_matrix_multiplication_parallel(int block_size); 

extern double *A; //use 'extern' to ONLY declare the variables in the header file, to can access them in other files
extern double *B; // but not create them here, because of multimple definitions in the soruce files
extern double *serial_result;
extern double *parallel_result; 

void create_all_matrices(); //used dinamically allocation to have more memory available for bigger matrices
void free_matrices();


#define NUMBER_OF_THREADS 12
#define CHUNK_SIZE 32 

#include "auxiliarLibrary.h" // include the auxiliar library only AFTER defining common constants 
                            // to avoid possible compilation errors


// declare the functions that call in macros below, because compiler needs to know the functions before calling them
void matrix_multiplication_serial_V1(); 
void matrix_multiplication_parallel_V1(int nr_of_threads, int chunk); 
void matrix_multiplication_serial_V2();
void matrix_multiplication_parallel_V2(int nr_of_threads, int chunk);
void matrix_multiplication_serial_V3();
void matrix_multiplication_parallel_V3(int nr_of_threads, int chunk);
void matrix_multiplication_serial_V4();
void matrix_multiplication_parallel_V4(int nr_of_threads, int chunk); 
void matrix_multiplication_serial_V5();
void matrix_multiplication_parallel_V5(int nr_of_threads, int chunk);
void matrix_multiplication_serial_V6();
void matrix_multiplication_parallel_V6(int nr_of_threads, int chunk);


#define i_j_k_serial matrix_multiplication_serial_V1() 
#define i_j_k_parallel matrix_multiplication_parallel_V1(NUMBER_OF_THREADS, CHUNK_SIZE)

#define i_k_j_serial matrix_multiplication_serial_V2()  
#define i_k_j_parallel matrix_multiplication_parallel_V2(NUMBER_OF_THREADS, CHUNK_SIZE)

#define j_i_k_serial matrix_multiplication_serial_V3()  
#define j_i_k_parallel matrix_multiplication_parallel_V3(NUMBER_OF_THREADS, CHUNK_SIZE)    

#define j_k_i_serial matrix_multiplication_serial_V4()  
#define j_k_i_parallel matrix_multiplication_parallel_V4(NUMBER_OF_THREADS, CHUNK_SIZE)

#define k_i_j_serial matrix_multiplication_serial_V5()
#define k_i_j_parallel matrix_multiplication_parallel_V5(NUMBER_OF_THREADS, CHUNK_SIZE)

#define k_j_i_serial matrix_multiplication_serial_V6()  
#define k_j_i_parallel matrix_multiplication_parallel_V6(NUMBER_OF_THREADS, CHUNK_SIZE) 

#endif // __LIBRARY_H