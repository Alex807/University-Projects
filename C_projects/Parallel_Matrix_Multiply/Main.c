#include <stdio.h> 
#include <stdlib.h>

#include "library.h"

int main() {  
    create_all_matrices();
    generate_matrix("Generating matrix A ...", A);
    generate_matrix("Generating matrix B ...", B); 


    validate_result_with_groundtruth_V1_serial(); 

    find_best_serial_parallel_time();

    find_best_block_size();

    free_matrices();
    return 0;  
}   
