#include <stdio.h>  
#include<stdlib.h>
#include<time.h> 

#include "library.h" 

int main(void)
{   //use this code to generate a randomly large number

    // srand(time(NULL));
    // long long int number;  
    // number=rand(); 
    // printf("Number in digits: \t%lld\n", number);

    long long int number; 
    printf("\nNumber in digits: \t"); 
    scanf("%lld", &number);

    char *result = NULL; //initialize the pointer to NULL
    result=rewrite(number);
    printf("Number in caracters: %s\n", result);
     
    free(result);
    return 0; 
}