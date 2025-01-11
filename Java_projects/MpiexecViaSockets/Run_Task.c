#include <stdio.h> 
#include <stdlib.h> 
 
int main(int argc, char *argv[]) {
    if (argc != 2) {
        printf(" [ERROR] Task_Usage: %s <who_port/host_executed>\n", argv[0]); 
        return 1; 
    }

    printf("Execution was done successfully via [%s] port !!\n", argv[1]); 
    return 0; 
}