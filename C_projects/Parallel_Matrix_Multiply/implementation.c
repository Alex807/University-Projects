#include <stdio.h> 
#include <stdlib.h> 
#include <omp.h>

#include "library.h"

double *A = NULL; //global variables that are only ONCE created in this source file, with 'create_all_matrices' function
double *B = NULL; //this are just references, initialized with NULL, to be used in other source files
double *serial_result = NULL;
double *parallel_result = NULL; //alloc matrix as arrays for better memory locality(we have a big chunk ALL TOGETHER)

void create_all_matrices() {
    A = (double*)malloc(MATRIX_SIZE * MATRIX_SIZE * sizeof(double));
    B = (double*)malloc(MATRIX_SIZE * MATRIX_SIZE * sizeof(double));
    serial_result = (double*)malloc(MATRIX_SIZE * MATRIX_SIZE * sizeof(double));
    parallel_result = (double*)malloc(MATRIX_SIZE * MATRIX_SIZE * sizeof(double));

    if (A == NULL || B == NULL || serial_result == NULL || parallel_result == NULL) {
        printf("Memory allocation failed !\n");
        exit(1);
    }
}

void free_matrices() {
    free(A);
    free(B);
    free(serial_result);
    free(parallel_result);
}

void matrix_multiplication_serial_V1() { 
    int size = MATRIX_SIZE; //use to less words in indexing a matrix element
    for (int i = 0; i < MATRIX_SIZE; i++)  {
        for (int j = 0; j < MATRIX_SIZE; j++) {
            
            serial_result[i * size + j] = 0; //reset 'serial_result' after each iteration to prevent accumulation in global matrix
            for (int k = 0; k < MATRIX_SIZE; k++) {
                serial_result[i * size + j] += A[i * size + k] * B[k * size + j]; 
            }
        } 
    }
}

void matrix_multiplication_parallel_V1(int nr_of_threads, int chunk) { 
    int i, j, k;
    double temp; 
    int size = MATRIX_SIZE;
#pragma omp parallel num_threads(nr_of_threads), default(none), private(i, j, k, temp), shared(A, B, parallel_result, chunk)  
    { 
#pragma omp for schedule(static, chunk)  collapse(2)
        for (i = 0; i < MATRIX_SIZE; i++) {
            for (j = 0; j < MATRIX_SIZE; j++) {

                temp = 0; //use a temporary variable to avoit acces to the global matrix at every step of 'k loop'
                for (k = 0; k < MATRIX_SIZE; k++) {
                    temp += A[i * size + k] * B[k * size + j];
                }
                parallel_result[i * size + j] = temp; 
            } 
        }
    }
}

void matrix_multiplication_serial_V2() {
    double aik;
    int size = MATRIX_SIZE;

    for (int i = 0; i < MATRIX_SIZE; i++) {
        for (int k = 0; k < MATRIX_SIZE; k++) {
            
            aik = A[i * size + k];
            for (int j = 0; j < MATRIX_SIZE; j++) {
                serial_result[i * size + j] += aik * B[k * size + j];
            }
        } 
    }
}

void matrix_multiplication_parallel_V2(int nr_of_threads, int chunk) {
    int i, j, k;
    double aik;
    int size = MATRIX_SIZE;
    
#pragma omp parallel num_threads(nr_of_threads), default(none), private(i, j, k, aik), shared(A, B, parallel_result, chunk)
    {
#pragma omp for schedule(static, chunk) collapse(2) 
        for (i = 0; i < MATRIX_SIZE; i++) {
            for (k = 0; k < MATRIX_SIZE; k++) {
                
                aik = A[i * size + k];
                for (j = 0; j < MATRIX_SIZE; j++) {
                    parallel_result[i * size + j] += aik * B[k * size + j];
                }
            } 
        }
    }
}

void matrix_multiplication_serial_V3() { 
    double sum;
    int size = MATRIX_SIZE; 

    for (int j = 0; j < MATRIX_SIZE; j++) {
        for (int i = 0; i < MATRIX_SIZE; i++) {
            
            sum = 0; //reset after each iteration
            for (int k = 0; k < MATRIX_SIZE; k++) {
                sum += A[i * size + k] * B[k * size + j];
            }
            serial_result[i * size + j] = sum;
        }
    }
} 

void matrix_multiplication_parallel_V3(int nr_of_threads, int chunk) { 
    int i, j, k;
    double sum;
    int size = MATRIX_SIZE;

    #pragma omp parallel num_threads(nr_of_threads), default(none), private(i, j, k, sum), shared(A, B, parallel_result, chunk)
    {
        #pragma omp for schedule(static, chunk) collapse(2)
        for (j = 0; j < MATRIX_SIZE; j++) {
            for (i = 0; i < MATRIX_SIZE; i++) {
                
                sum = 0;  //reset  'sum' after each iteration
                for (k = 0; k < MATRIX_SIZE; k++) {
                    sum += A[i * size + k] * B[k * size + j];
                }
                parallel_result[i * size + j] = sum;  
            }
        }
    }
}

void matrix_multiplication_serial_V4() { 
    double bjk; 
    int size = MATRIX_SIZE;

    for (int j = 0; j < MATRIX_SIZE; j++) {
        for (int k = 0; k < MATRIX_SIZE; k++) {
            
            bjk = B[k * size + j];
            for (int i = 0; i < MATRIX_SIZE; i++) {
                serial_result[i * size + j] += A[i * size +k] * bjk;
            }
        }
    }
}

void matrix_multiplication_parallel_V4(int nr_of_threads, int chunk) { 
    int i, j, k;
    double bjk;
    int size  = MATRIX_SIZE;

    #pragma omp parallel num_threads(nr_of_threads), default(none), private(i, j, k, bjk), shared(A, B, parallel_result, chunk)
    {
        #pragma omp for schedule(static, chunk) collapse(2) 
        for (j = 0; j < MATRIX_SIZE; j++) {
            for (k = 0; k < MATRIX_SIZE; k++) {
                
                bjk = B[k * size + j];  
                for (i = 0; i < MATRIX_SIZE; i++) { 
                    parallel_result[i * size + j] += A[i * size + k] * bjk;  
                }
            }
        }
    }  
} 

void matrix_multiplication_serial_V5() { 
    double aik; 
    int size = MATRIX_SIZE;

    for (int k = 0; k < MATRIX_SIZE; k++) {
        for (int i = 0; i < MATRIX_SIZE; i++) {
            
            aik = A[i * size + k];
            for (int j = 0; j < MATRIX_SIZE; j++) {
                serial_result[i * size + j] += aik * B[k * size + j];
            }
        }
    }
}

void matrix_multiplication_parallel_V5(int nr_of_threads, int chunk) {
    int i, j, k;
    double aik;
    int size = MATRIX_SIZE;

#pragma omp parallel num_threads(nr_of_threads) default(none) private(i, j, k, aik) shared(A, B, parallel_result, chunk)
    { //use 'collapse' directive to combine 2 loops into one big flattened loop, just makes a better distribution of work to threads
#pragma omp for schedule(static, chunk) collapse(2) 
        for (k = 0; k < MATRIX_SIZE; k++) {
            for (i = 0; i < MATRIX_SIZE; i++) {
                
                aik = A[i * size + k];
                for (j = 0; j < MATRIX_SIZE; j++) {
                    parallel_result[i * size + j] += aik * B[k * size + j];
                }
            }
        }
    }
}

void matrix_multiplication_serial_V6() { 
    double bjk; 
    int size = MATRIX_SIZE;

    for (int k = 0; k < MATRIX_SIZE; k++) {
        for (int j = 0; j < MATRIX_SIZE; j++) {
            
            bjk = B[k * size + j];
            for (int i = 0; i < MATRIX_SIZE; i++) {
                serial_result[i * size + j] += A[i * size + k] * bjk;
            }
        }
    }
} 

void matrix_multiplication_parallel_V6(int nr_of_threads, int chunk) { 
    int i, j, k;
    double bjk;
    int size = MATRIX_SIZE;

    #pragma omp parallel num_threads(nr_of_threads), default(none), private(i, j, k, bjk), shared(A, B, parallel_result, chunk)
    {
        #pragma omp for schedule(static, chunk) collapse(2) 
        for (k = 0; k < MATRIX_SIZE; k++) {
            for (j = 0; j < MATRIX_SIZE; j++) {
                
                bjk = B[k * size + j];
                for (i = 0; i < MATRIX_SIZE; i++) {
                    parallel_result[i * size + j] += A[i * size + k] * bjk;
                }
            }
        }
    } 
}

void block_matrix_multiplication_serial(int block_size) {
    int i, j, k, ii, jj, kk; 
    int size = MATRIX_SIZE;

    // we need to process each block
    for (ii = 0; ii < MATRIX_SIZE; ii += block_size) {
        for (jj = 0; jj < MATRIX_SIZE; jj += block_size) {
            for (kk = 0; kk < MATRIX_SIZE; kk += block_size) {
                
                // process each element, even if it is not a full block (check 'for condition' to understand more)
                for (i = ii; i < ii + block_size && i < MATRIX_SIZE; i++) {
                    for (j = jj; j < jj + block_size && j < MATRIX_SIZE; j++) {
                        for (k = kk; k < kk + block_size && k < MATRIX_SIZE; k++) {
                            serial_result[i * size + j] += A[i * size + k] * B[k * size + j];
                        }
                    }
                }
            }
        }
    }
}

void block_matrix_multiplication_parallel(int block_size) {
    int i, j, k, ii, jj, kk; 
    int size = MATRIX_SIZE;

    #pragma omp parallel for private(i, j, k, ii, jj, kk) collapse(2) \
                             num_threads(NUMBER_OF_THREADS) schedule(static, CHUNK_SIZE) 
    
    for (ii = 0; ii < MATRIX_SIZE; ii += block_size) {
        for (jj = 0; jj < MATRIX_SIZE; jj += block_size) {
            for (kk = 0; kk < MATRIX_SIZE; kk += block_size) {
                
                for (i = ii; i < ii + block_size && i < MATRIX_SIZE; i++) {
                    for (j = jj; j < jj + block_size && j < MATRIX_SIZE; j++) {
                        for (k = kk; k < kk + block_size && k < MATRIX_SIZE; k++) {
                            parallel_result[i * size + j] += A[i * size + k] * B[k * size + j];
                        }
                    }
                }
            }
        }
    } 
}