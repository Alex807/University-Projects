#include <stdio.h>
#include <stdlib.h> 
#include <string.h> 
#include <math.h>
#include <time.h> 
#include <omp.h>
#include "library.h"

void generate_matrix(char *prompt, double* matrix) {
    srand(time(NULL));
    printf("%s\n", prompt); 
    int size = MATRIX_SIZE; //use to write less into indexing a matrix element
    for (int i = 0; i < MATRIX_SIZE; i++) { 
        for (int j = 0; j < MATRIX_SIZE; j++) { 
            matrix[i + size + j] = rand();
        } 
    }
} 

int equal_matrixes(double* mat1, double* mat2) { 
    int size = MATRIX_SIZE;
    for (int i = 0; i < MATRIX_SIZE; i++) {
        for (int j = 0; j < MATRIX_SIZE; j++) { 
            if (fabs(mat1[i * size + j] - mat2[i * size + j] > MARGIN_OF_ERROR)) {
                return 0;
            } 
        }
    }
    return 1;
}

void copy_matrix_data (double* source, double* destination) { 
    int size = MATRIX_SIZE;
    for (int i = 0; i < MATRIX_SIZE; i++) { 
        for (int j = 0; j < MATRIX_SIZE; j++) { 
            destination[i * size + j] = source[i * size + j];
        }
    }
}

void print_matrix(double* matrix, char* matrix_simbol) { 
    int size = MATRIX_SIZE; 
    for(int i = 0; i < MATRIX_SIZE; i++)
    { 
        for(int j = 0; j < MATRIX_SIZE; j++)
        { 
            if(i == MATRIX_SIZE/2 && j==0)
            {
                if(strlen(matrix_simbol) != 1)
                { 
                printf("%s=%4.1f ", matrix_simbol, matrix[i * size + j]);
                }
                else 
                { 
                    printf("%s = %4.1f ", matrix_simbol, matrix[i * size + j]);
                }
            }
            else if(j == 0)
            { 
                printf("    %4.1f ", matrix[i * size + j]);
            }
            else 
            { 
                printf("%4.1f ", matrix[i * size + j]);
            }
        }
        printf("\n");
    }
    printf("\n\n");
}

void initialize_matrix_serial_with_value(double* serial_result, int value) { 
    int size = MATRIX_SIZE;
    for (int i = 0; i < MATRIX_SIZE; i++) { 
        for (int j = 0; j < MATRIX_SIZE; j++) { 
            serial_result[i * size + j] = value;
        }
    }
}

void initialize_matrix_parallel_with_value(double* parallel_result, int value) { 
    int i, j; 
    int size = MATRIX_SIZE;
    #pragma omp parallel num_threads(NUMBER_OF_THREADS), default(none), private(i, j), shared(parallel_result) 
    {
#pragma omp for schedule(static, CHUNK_SIZE) collapse(2)
    for (int i = 0; i < MATRIX_SIZE; i++) { 
        for (int j = 0; j < MATRIX_SIZE; j++) { 
            parallel_result[i * size + j] = value; 
        } 
    } 
    }
}

double* create_matrix() { 
    double* matrix = NULL; 
    if ( (matrix = (double*)malloc(MATRIX_SIZE * MATRIX_SIZE * sizeof(double))) == NULL) { 
        printf("Memory allocation failed !\n"); 
        exit(1);
    } 
    return matrix;
}

void free_matrix(double* matrix) { 
    free(matrix);
}

void validate_result_with_groundtruth_V1_serial () { 
    printf("\nConsidering V1_serial as ground-truth, check all implemented versions !!\n"); 
    printf("Checking serial versions ... \n");
    
    i_j_k_serial;
    double* ground_truth = create_matrix(); 
    int spoted_errors = 0;

    copy_matrix_data(serial_result, ground_truth);

    i_k_j_serial; 
    if (!equal_matrixes(ground_truth, serial_result)) { 
        printf("V2_serial does NOT multiply matrix correctly !\n"); 
        spoted_errors++;
    }
    
    j_i_k_serial; 
    if (!equal_matrixes(ground_truth, serial_result)) { 
        printf("V3_serial does NOT multiply matrix correctly !\n"); 
        spoted_errors++;
    }

    j_k_i_serial; 
    if (!equal_matrixes(ground_truth, serial_result)) { 
        printf("V4_serial does NOT multiply matrix correctly !\n"); 
        spoted_errors++;
    }

    k_i_j_serial;
    if (!equal_matrixes(ground_truth, serial_result)) { 
        printf("V5_serial does NOT multiply matrix correctly !\n"); 
        spoted_errors++;
    }

    k_j_i_serial;
    if (!equal_matrixes(ground_truth, serial_result)) { 
        printf("V6_serial does NOT multiply matrix correctly !\n"); 
        spoted_errors++;
    }
    
    block_matrix_multiplication_serial(BLOCK_SIZE); 
    if (!equal_matrixes(ground_truth, serial_result)) { 
        printf("Block_serial does NOT multiply matrix correctly !\n"); 
        spoted_errors++;
    }


    printf("Checking parallel versions ... \n");
    j_i_k_parallel; 
    if (!equal_matrixes(ground_truth, parallel_result)) { 
        printf("V3_parallel does NOT multiply matrix correctly !\n"); 
        spoted_errors++;
    } 

    i_j_k_parallel; 
    if (!equal_matrixes(ground_truth, parallel_result)) { 
        printf("V1_parallel does NOT multiply matrix correctly !\n"); 
        spoted_errors++;
    } 

    i_k_j_parallel; 
    if (!equal_matrixes(ground_truth, parallel_result)) { 
        printf("V2_parallel does NOT multiply matrix correctly !\n"); 
        spoted_errors++;
    }

    j_k_i_parallel; 
    if (!equal_matrixes(ground_truth, parallel_result)) { 
        printf("V4_parallel does NOT multiply matrix correctly !\n"); 
        spoted_errors++;
    }

    k_i_j_parallel; 
    if (!equal_matrixes(ground_truth, parallel_result)) { 
        printf("V5_parallel does NOT multiply matrix correctly !\n"); 
        spoted_errors++;
    }

    k_j_i_parallel; 
    if (!equal_matrixes(ground_truth, parallel_result)) { 
        printf("V6_parallel does NOT multiply matrix correctly !\n"); 
        spoted_errors++;
    }

    block_matrix_multiplication_parallel(BLOCK_SIZE); 
    if (!equal_matrixes(ground_truth, parallel_result)) { 
        printf("Block_parallel does NOT multiply matrix correctly !\n"); 
        spoted_errors++;
    }

    if (spoted_errors == 0) { 
        printf("All versions are correctly implemented !!\n\n");
    }

    free_matrix(ground_truth);
}

double measure_serialVersion_executionTime(void (*serial_version)()) { 
    double start, end; //do NOT declare them in header file, because they are not used in other files, are local functions
    start = omp_get_wtime(); 
    serial_version(); 
    end = omp_get_wtime();

    return end - start;
}

double measure_parallelVersion_executionTime(void (*parallel_version)(), int nr_of_threads, int chunk) { 
    double start, end; 
    start = omp_get_wtime(); 
    parallel_version(nr_of_threads, chunk); 
    end = omp_get_wtime(); 

    return end - start;
}

int find_index_of_min_value (double* array, int size) { 
    double min = INFINITY;  
    int index = -1; //this is value that we search, because with it we can acces directly in array value that we need
    for (int i = 0; i < size; i++) { 
        if (array[i] < min) { 
            min = array[i]; 
            index = i;
        }
    }   
    return index; 
}

void print_measurement_info (char* prompt, double executionTime, double ground_truth) { 
    printf ("%s Execution_time = %f sec. , Speed_Up = %.2f\n", prompt, executionTime, ground_truth / executionTime);
}

void find_best_serial_parallel_time () { 
    printf ("Determining the best serial/parellel version.\n");

    char *serial_versions[] = {"i_j_k_serial", "i_k_j_serial", "j_i_k_serial", "j_k_i_serial", "k_i_j_serial", "k_j_i_serial"}; 
    char *parallel_versions[] = {"i_j_k_parallel", "i_k_j_parallel", "j_i_k_parallel", "j_k_i_parallel", "k_i_j_parallel", "k_j_i_parallel"}; 
    double serial_versions_results[6]; 
    double parallel_versions_results[6]; //save all measurements of each versions(V1-V6) 
    
    
    printf("Start calling serial versions ... \n\n"); 
    serial_versions_results[0] = measure_serialVersion_executionTime(matrix_multiplication_serial_V1); 
    double ground_truth = serial_versions_results[0]; //use as refference value for measuring speed-up 
    print_measurement_info("i-j-k serial (ground_truth)", serial_versions_results[0], serial_versions_results[0]);

    serial_versions_results[1] = measure_serialVersion_executionTime(matrix_multiplication_serial_V2);  
    print_measurement_info("i-k-j serial", serial_versions_results[1], ground_truth);
    serial_versions_results[2] = measure_serialVersion_executionTime(matrix_multiplication_serial_V3);  
    print_measurement_info("j-i-k serial", serial_versions_results[2], ground_truth);
    serial_versions_results[3] = measure_serialVersion_executionTime(matrix_multiplication_serial_V4);  
    print_measurement_info("j-k-i serial", serial_versions_results[3], ground_truth);
    serial_versions_results[4] = measure_serialVersion_executionTime(matrix_multiplication_serial_V5);  
    print_measurement_info("k-i-j serial", serial_versions_results[4], ground_truth);
    serial_versions_results[5] = measure_serialVersion_executionTime(matrix_multiplication_serial_V6); 
    print_measurement_info("k-j-i serial", serial_versions_results[5], ground_truth);


    printf("\nStart calling parallel versions ... \n\n"); 

    parallel_versions_results[0] = measure_parallelVersion_executionTime(matrix_multiplication_parallel_V1, NUMBER_OF_THREADS, CHUNK_SIZE);  
    print_measurement_info("i-j-k parallel", parallel_versions_results[0], ground_truth);
    parallel_versions_results[1] = measure_parallelVersion_executionTime(matrix_multiplication_parallel_V2, NUMBER_OF_THREADS, CHUNK_SIZE);  
    print_measurement_info("i-k-j parallel", parallel_versions_results[1], ground_truth);
    parallel_versions_results[2] = measure_parallelVersion_executionTime(matrix_multiplication_parallel_V3, NUMBER_OF_THREADS, CHUNK_SIZE);  
    print_measurement_info("j-i-k parallel", parallel_versions_results[2], ground_truth);
    parallel_versions_results[3] = measure_parallelVersion_executionTime(matrix_multiplication_parallel_V4, NUMBER_OF_THREADS, CHUNK_SIZE);  
    print_measurement_info("j-k-i parallel", parallel_versions_results[3], ground_truth);
    parallel_versions_results[4] = measure_parallelVersion_executionTime(matrix_multiplication_parallel_V5, NUMBER_OF_THREADS, CHUNK_SIZE); 
    print_measurement_info("k-i-j parallel", parallel_versions_results[4], ground_truth);
    parallel_versions_results[5] = measure_parallelVersion_executionTime(matrix_multiplication_parallel_V6, NUMBER_OF_THREADS, CHUNK_SIZE); 
    print_measurement_info("k-j-i parallel", parallel_versions_results[5], ground_truth);

    
    int index_serial = find_index_of_min_value(serial_versions_results, 6);
    int index_parallel = find_index_of_min_value(parallel_versions_results, 6);  

    printf("\nConclusions: \n");
    
    double speed_up_serial = ground_truth / serial_versions_results[index_serial];
    double speed_up_parallel = ground_truth / parallel_versions_results[index_parallel];
    printf("--> best serial version is '%s' with Execution_time = %f sec. , Speed_Up = %.2f\n", serial_versions[index_serial], serial_versions_results[index_serial], speed_up_serial); 
    printf("--> best parallel version is '%s' with Eexecution_time = %f sec. , Speed_Up = %.2f\n\n", parallel_versions[index_parallel], parallel_versions_results[index_parallel], speed_up_parallel);
}

double measure_blockMultiply_executionTime(void (*block_function)(), int arg) { 
    double start, end; 
    start = omp_get_wtime(); 
    block_function(arg); 
    end = omp_get_wtime(); 
    
    return end - start;
}

int find_best_block_size () { 
    printf("Determining the best block size for block matrix multiplication.\n");
    int current_block_size = 4; //search for the best block size, starting from 4 and double it until reaching the matrix size 
    int total_possibilities = (int)(log2((double)MATRIX_SIZE / 4)) + 1; //use a formula to calculate the number of entrys in the array
   
    double block_multiply_serial_results[total_possibilities];   
    double block_multiply_parallel_results[total_possibilities]; 
    int index = 0;
    for (current_block_size = 4; current_block_size <= MATRIX_SIZE; current_block_size *= 2) {  
        printf ("Running implementations for block_size = %d ... \n", current_block_size);
        block_multiply_serial_results[index] = measure_blockMultiply_executionTime(block_matrix_multiplication_serial, current_block_size);  
        block_multiply_parallel_results[index] = measure_blockMultiply_executionTime(block_matrix_multiplication_parallel, current_block_size);

        index++;
    } 

    int index_serial = find_index_of_min_value(block_multiply_serial_results, index); 
    int index_parallel = find_index_of_min_value(block_multiply_parallel_results, index); 

    printf("Conclusions: \n"); 
    int ideal_block_size_serial = (int)pow(2, index_serial + 2); 
    if (index_serial != index_parallel) {  

        int ideal_block_size_parallel = (int)pow(2, index_parallel + 2);    
        printf("--> best block size for serial is %d with Execution_time = %f sec.\n", ideal_block_size_serial, block_multiply_serial_results[index_serial]); 
        printf("--> best block size for parallel is %d with Execution_time = %f sec.\n\n", ideal_block_size_parallel, block_multiply_parallel_results[index_parallel]);
    } 
    else { 
        double average_execution_time = block_multiply_serial_results[index_serial]+ block_multiply_parallel_results[index_parallel] / 2;
        printf("--> best block size for both serial and parallel is %d with average Execution_time = %f sec.\n\n", ideal_block_size_serial, average_execution_time);
    }
    return ideal_block_size_serial;    
}