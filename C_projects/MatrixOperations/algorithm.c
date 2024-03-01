#include <stdio.h> 
#include <stdlib.h>
#include <time.h> 
#include <math.h> 
#include <string.h>

#include "library.h"

void populate_matrix(int** matrix, int matrix_size, int seed)
{ 
    srand(seed);

    for(int i=0; i<matrix_size; i++)
    { 
        for(int j=0; j<matrix_size; j++)
        { 
            matrix[i][j]=rand()%10; 
        }
    }
}

int** create_matrix(int matrix_size)
{ 
    int **matrix=NULL;
    if( (matrix=(int**)malloc(matrix_size * sizeof(int))) == NULL )
    { 
        perror("Error at malloc!\n"); 
        exit(-1);
    }

    for(int i=0; i<matrix_size; i++)
    {
        if( (matrix[i]=(int*)malloc(matrix_size * sizeof(int))) == NULL )
        { 
            perror("Error at malloc!\n"); 
            exit(-1);
        }
    }

    return matrix;
}

void print_matrix(int** matrix, int matrix_size, char* matrix_simbol)
{ 
    for(int i=0; i<matrix_size; i++)
    { 
        for(int j=0; j<matrix_size; j++)
        { 
            if(i == matrix_size/2 && j==0)
            {
                if(strlen(matrix_simbol) != 1)
                { 
                printf("%s=%3d ", matrix_simbol, matrix[i][j]);
                }
                else 
                { 
                    printf("%s = %3d ", matrix_simbol, matrix[i][j]);
                }
            }
            else if(j == 0)
            { 
                printf("    %3d ", matrix[i][j]);
            }
            else 
            { 
                printf("%3d ", matrix[i][j]);
            }
        }
        printf("\n");
    }
    printf("\n");
}

void free_matrix_memory(int** matrix, int matrix_size)
{ 
    for(int i=0; i<matrix_size; i++)
    { 
        free(matrix[i]);
    }
    free(matrix);
}

int matrix_deter_size2(int** matrix)
{ 
    return (matrix[0][0] * matrix[1][1]) - (matrix[1][0] * matrix[0][1]);
}

int matrix_deter_size3(int** matrix)
{  
    int product_elm_main_diagonal=matrix[0][0] * matrix[1][1] * matrix[2][2];
    int triangle1_main=matrix[1][0] * matrix[2][1] * matrix[0][2]; 
    int triangle2_main=matrix[0][1] * matrix[1][2] * matrix[2][0]; 
    int s1=product_elm_main_diagonal + triangle1_main + triangle2_main; 

    int product_elm_secondary_diagonal=matrix[0][2] * matrix[1][1] * matrix[2][0]; 
    int triangle1_secondary=matrix[0][1] * matrix[1][0] * matrix[2][2]; 
    int triangle2_secondary=matrix[2][1] * matrix[1][2] * matrix[0][0];
    int s2=product_elm_secondary_diagonal + triangle1_secondary + triangle2_secondary; 

    return s1-s2;
}

long int matrix_deter_higher_order(int** matrix, int matrix_size)
{   
    if (matrix_size == 3)
    { 
        return matrix_deter_size3(matrix);
    }
    else
    {
        long int s=0;
        int k=0; 
        int copy_size = matrix_size - 1;

        int** copy=create_matrix(copy_size);

        while (k < matrix_size)
        {
            int x=0, y=0;
            for (int i=1; i<matrix_size; i++)//pleci de la 1 ca sa suprimi direct prima linie din matrice
            { 
                for (int j=0; j<matrix_size; j++)
                { 
                    if (j != k)
                    {
                        copy[x][y]=matrix[i][j]; 
                        y++; 
                        if(y == copy_size)
                        { 
                            x++; 
                            y=0;
                        }
                    }
                }
            }
            
            int product = matrix[0][k] * matrix_deter_higher_order(copy, copy_size);
            if(k % 2 == 0) 
            { 
                s = s + product;
            }
            else 
            { 
                s = s - product;
            }
            
            
            k++;
        }

        free_matrix_memory(copy, copy_size);
        return s;       
    }
}

long int calculate_matrix_deter(int** matrix, int matrix_size)
{ 
    if (matrix_size == 1)
    { 
        return **matrix;
    }
    else if (matrix_size == 2)
    { 
        return matrix_deter_size2(matrix);
    }
    else if (matrix_size == 3)
    {
        return matrix_deter_size3(matrix);
    }
    else 
    { 
        return matrix_deter_higher_order(matrix, matrix_size);
    }
} 
//-----------------------------------------------------------------------------------------
double** create_inverted_matrix(int matrix_size)
{ 
    double **matrix=NULL;
    if( (matrix=(double**)malloc(matrix_size * sizeof(double))) == NULL )
    { 
        perror("Error at malloc!\n"); 
        exit(-1);
    }

    for(int i=0; i<matrix_size; i++)
    {
        if( (matrix[i]=(double*)malloc(matrix_size * sizeof(double))) == NULL )
        { 
            perror("Error at malloc!\n"); 
            exit(-1);
        }
    }

    return matrix;
} 
 
void free_inverted_matrix_memory(double** matrix, int matrix_size) 
{ 
    for(int i=0; i<matrix_size; i++)
    { 
        free(matrix[i]);
    }
    free(matrix);  
} 

void print_inverted_matrix(double** matrix, int matrix_size)
{
    for(int i=0; i<matrix_size; i++)
    { 
        for(int j=0; j<matrix_size; j++)
        { 
            if(i == matrix_size/2 && j==0)
            {
                printf("A(-1) = %7.3lf ", matrix[i][j]);
            }
            else if(j == 0)
            { 
                printf("        %7.3lf ", matrix[i][j]);
            }
            else 
            { 
                printf("%7.3lf ", matrix[i][j]);
            }
        }
        printf("\n\n");
    }
}

int** calculate_transposed_matrix(int** matrix, int matrix_size)
{ 
    int** transposed_matrix = create_matrix(matrix_size); 

    for(int i=0; i<matrix_size; i++)
    { 
        for(int j=0; j<matrix_size; j++)
        { 
            transposed_matrix[j][i] = matrix[i][j];
        }
    }

    return transposed_matrix;
} 

int calculate_determinant_croped_matrix(int** matrix, int index_row_croped, int index_col_croped, int matrix_size) 
{ 
    int croped_matrix_size = matrix_size-1; 
    int** croped_matrix = create_matrix(croped_matrix_size); 

    int x=0, y=0;
    for (int i=0; i<matrix_size; i++)
    { 
        for (int j=0; j<matrix_size; j++)
        { 
            if (i != index_row_croped && j != index_col_croped)
            {
                croped_matrix[x][y]=matrix[i][j]; 
                y++; 

                if(y == croped_matrix_size)
                { 
                    x++; 
                    y=0;
                }
            }
        }
    } 
    int deter_croped_matrix = calculate_matrix_deter(croped_matrix, croped_matrix_size);

    free_matrix_memory(croped_matrix, croped_matrix_size);
    return deter_croped_matrix;
}

int** calculate_adjunct_matrix(int** matrix, int matrix_size) 
{ 
    int** adjunct_matrix = create_matrix(matrix_size);  
    int** transposed_matrix = calculate_transposed_matrix(matrix, matrix_size); 

    for (int i=0; i<matrix_size; i++)
    { 
        for (int j=0; j<matrix_size; j++)
        {
            int exponential_factor = pow(-1,i+j); 
            int deter_of_croped_matrix = calculate_determinant_croped_matrix(transposed_matrix, i, j, matrix_size);

            adjunct_matrix[i][j] = exponential_factor * deter_of_croped_matrix;
        }
    }

    free_matrix_memory(transposed_matrix, matrix_size); 
    return adjunct_matrix;
}

double** calculate_inverted_matrix(int** matrix, int matrix_size, long int matrix_deter)
{
    //double converted_matrix_deter = matrix_deter; //daca nu faci explicit conversie din int la double rezultatul e 0
    double fractioal_deter = 1.0/matrix_deter;

    double** inverted_matrix=create_inverted_matrix(matrix_size);  
    int** adjunct_matrix=calculate_adjunct_matrix(matrix, matrix_size);

    for (int i=0; i<matrix_size; i++)
    { 
        for (int j=0; j<matrix_size; j++)
        { 
            inverted_matrix[i][j] = fractioal_deter * adjunct_matrix[i][j];
        }
    }

    free_matrix_memory(adjunct_matrix, matrix_size);
    return inverted_matrix;
} 
//-----------------------------------------------------------------------------------------------------------PRODUS MATRICI
int calculate_multiplication_matrix_element(int** matrix1, int** matrix2, int index_row_used, int index_col_used, int matrix_size)
{ 
    int result = 0; 

    for (int index=0; index<matrix_size; index++)
    { 
        result += matrix1[index_row_used][index] * matrix2[index][index_col_used];

    }

    return result;
}

int** calculate_multiplication_matrix(int** matrix1, int** matrix2, int matrix_size) 
{ 
    int** result_matrix_multiplication = create_matrix(matrix_size);  
    for (int i =0; i<matrix_size; i++)
    { 
        for (int j=0; j<matrix_size; j++)
        { 
            result_matrix_multiplication[i][j] = calculate_multiplication_matrix_element(matrix1, matrix2, i, j, matrix_size);
        }
    }

    return result_matrix_multiplication;
}