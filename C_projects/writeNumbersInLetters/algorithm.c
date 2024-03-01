#include<stdio.h>  
#include<stdlib.h>
#include<string.h> 
#define PASS 200

#include "library.h" 

int count_digits(long long int number)
{ 
    int count=0;

    if(number == 0)
    { 
        return 1;
    } 
 
    while(number != 0)
    { 
        count++; 
        number=number/10;
    }  
     
    return count;
} 
 
int* create_vector(long long int number, int *size)
{      
    unsigned digits; 
    digits=count_digits(number);
    *size=digits;     //add 0 to the initial number

    while( (*size) % 3 != 0)
    { 
        (*size)++;     // we use adress memory of size to modify it
    }

    int *v; // create the vector of digits
    if( (v=(int*)calloc((*size),sizeof(int))) == NULL)
    { 
        printf("Error at malloc!\n"); 
        exit(-1);
    }

    int index=0;
    while(number != 0)
    { 
        v[index]=number%10; 
        number=number/10; 
        index++;
    }  

    int counter_zero; 
    counter_zero=*size-digits; 
    while(counter_zero != 0)
    { 
        v[index]=0; 
        counter_zero--; 
        index++;
    }

    return v;
}

char* according(char* string, int option, char* rank)
{   
    if( strcmp(rank, "miliarde") == 0) 
    { 
        char copy[100];  
        strncpy(copy, string, strlen(string)-4); //just 4 for removing "unu " or "doi " 
        if(option == 1) 
        {   
            strcat(copy, "un miliard ");
            strcpy(string, copy); 
            strcpy(copy, "");   //cleaning 
        } 
        else 
        { 
            strcat(copy, "doua miliarde ");
            strcpy(string, copy); 
            strcpy(copy, "");   //cleaning 
        }
    } 

    if( strcmp(rank, "milioane") == 0) 
    { 
        char copy[300];  
        strncpy(copy, string, strlen(string)-4); //just 4 for removing "unu " or "doi " 
        if(option == 1) 
        {   
            strcat(copy, "un milion ");
            strcpy(string, copy); 
            strcpy(copy, "");   //cleaning 
        } 
        else 
        { 
            strcat(copy, "doua milioane ");
            strcpy(string, copy); 
            strcpy(copy, "");   //cleaning 
        }
    } 

    if( strcmp(rank, "mii") == 0) 
    {  
        char copy[500];  
        strncpy(copy, string, strlen(string)-4); //just 4 for removing "unu " or "doi " 
        if(option == 1) 
        {   
            strcat(copy, "o mie ");
            strcpy(string, copy); 
            strcpy(copy, "");   //cleaning 
        } 
        else 
        { 
            strcat(copy, "doua mii ");
            strcpy(string, copy); 
            strcpy(copy, "");   //cleaning 
        }
    } 
    return string;
}

char* translate(int *array)
{ 
    static char s[1000];   //must be use for prevent from heap memory in .bss

    for(int i=0; i<3; i++)
    {   
        if(i == 0)      //SUTE
        {
            switch (array[i])
            { 
            case(0): 
                strcat(s, ""); 
                break;
            case(1):
                strcat(s, "o suta ");
                break;    
            case(2): 
                strcat(s, "doua sute ");
                break;
            case(3): 
                strcat(s, "trei sute ");
                break;
            case(4): 
                strcat(s, "patru sute ");
                break;
            case(5): 
                strcat(s, "cinci sute ");
                break;
            case(6): 
                strcat(s, "sase sute ");
                break;
            case(7): 
                strcat(s, "sapte sute ");
                break;
            case(8): 
                strcat(s, "opt sute ");
                break;
            case(9): 
                strcat(s, "noua sute ");
                break;
            default:
                printf("Error!\n");
                exit(-1);
                
            }  
        } 

        if(i == 1 && array[1] != 1) 
        {                     // CAZ GENERAL ZECI
            switch (array[i])
            { 
            case(0): 
                strcat(s, ""); 
                break;    
            case(2): 
                strcat(s, "doua zeci ");
                break;
            case(3): 
                strcat(s, "trei zeci ");
                break;
            case(4): 
                strcat(s, "patru zeci ");
                break;
            case(5): 
                strcat(s, "cinci zeci ");
                break;
            case(6): 
                strcat(s, "sase zeci ");
                break;
            case(7): 
                strcat(s, "sapte zeci ");
                break;
            case(8): 
                strcat(s, "opt zeci ");
                break;
            case(9): 
                strcat(s, "noua zeci ");
                break;
            default:
                printf("Error!\n");
                exit(-1);
                
            }  
        } 
        else if(i == 1 && array[i] == 1) 
        {                      //CAZ EXCEPTIE ZECI
            switch (array[i+1])
            { 
            case(0): 
                strcat(s, "zece "); 
                break;
            case(1):
                strcat(s, "unu spre zece ");
                break;    
            case(2): 
                strcat(s, "doi spre zece ");
                break;
            case(3): 
                strcat(s, "trei spre zece ");
                break;
            case(4): 
                strcat(s, "patru spre zece ");
                break;
            case(5): 
                strcat(s, "cinci spre zece ");
                break;
            case(6): 
                strcat(s, "sase spre zece ");
                break;
            case(7): 
                strcat(s, "sapte spre zece ");
                break;
            case(8): 
                strcat(s, "opt spre zece ");
                break;
            case(9): 
                strcat(s, "noua spre zece ");
                break;
            default:
                printf("Error!\n");
                exit(-1);
                
            }   
            break;
        } 

        if(i == 2)
        {  
            if(array[i-2] == array[i-1] && array[i-1] == 0)
            {                         //CAZ EXCEPTIE UNITATI 
                switch (array[i])
                { 
                case(0): 
                    strcat(s, ""); 
                    break;  
                case(1): 
                    strcat(s, "unu ") ; 
                    break; 
                case(2): 
                    strcat(s, "doi ");
                    break;
                case(3): 
                    strcat(s, "trei ");
                    break;
                case(4): 
                    strcat(s, "patru ");
                    break;
                case(5): 
                    strcat(s, "cinci ");
                    break;
                case(6): 
                    strcat(s, "sase ");
                    break;
                case(7): 
                    strcat(s, "sapte ");
                    break;
                case(8): 
                    strcat(s, "opt ");
                    break;
                case(9): 
                    strcat(s, "noua ");
                    break;
                default:
                    printf("Error!\n");
                    exit(-1);
                    
                }   
                return s;  //this case is over here
            }  
            else if(array[0] != 0 && array[1] == 0)
            {                         //CAZ EXCEPTIE UNITATI 
                switch (array[i])
                { 
                case(0): 
                    strcat(s, ""); 
                    break;  
                case(1): 
                    strcat(s, "unu") ; 
                    break; 
                case(2): 
                    strcat(s, "doua ");
                    break;
                case(3): 
                    strcat(s, "trei ");
                    break;
                case(4): 
                    strcat(s, "patru ");
                    break;
                case(5): 
                    strcat(s, "cinci ");
                    break;
                case(6): 
                    strcat(s, "sase ");
                    break;
                case(7): 
                    strcat(s, "sapte ");
                    break;
                case(8): 
                    strcat(s, "opt ");
                    break;
                case(9): 
                    strcat(s, "noua ");
                    break;
                default:
                    printf("Error!\n");
                    exit(-1);
                    
                }  
            }  
            else 
            { 
                switch (array[i])
                {                  //CAZ GENERAL UNITATI
                case(0): 
                    strcat(s, ""); 
                    break;  
                case(1): 
                    strcat(s, "si unu ") ; 
                    break; 
                case(2): 
                    strcat(s, "si doi ");
                    break;
                case(3): 
                    strcat(s, "si trei ");
                    break;
                case(4): 
                    strcat(s, "si patru ");
                    break;
                case(5): 
                    strcat(s, "si cinci ");
                    break;
                case(6): 
                    strcat(s, "si sase ");
                    break;
                case(7): 
                    strcat(s, "si sapte ");
                    break;
                case(8): 
                    strcat(s, "si opt ");
                    break;
                case(9): 
                    strcat(s, "si noua ");
                    break;
                default:
                    printf("Error!\n");
                    exit(-1);
                    
                } 
            }
        }
    } 
    return s;
} 

char* rewrite(long long int number)
{ 
    int size;
    int *array=create_vector(number, &size); 
    
     
    char *string; 
    if( (string=(char*)calloc(PASS, sizeof(char))) == NULL)
    { 
        printf("Error at malloc!\n"); 
        exit(-1); 
    } 

    if(size == 3) 
    {                                     //case for number 0 only
        if(array[0]==array[1] && array[2]==array[1] && array[2] == 0)
        { 
            strcat(string, "zero "); 
            free(array); 
            return string;
        }
    }

    int i, increase=2;
    for(i=size-1; i>=0; i=i-3)
    { 
        int aux[3]; 
        aux[0]=array[i];  
        aux[1]=array[i-1];
        aux[2]=array[i-2]; 

        char *chunk=translate(aux);
        strcat(string, chunk); 
        strcpy(chunk, ""); //for clearing last chunk

        string=realloc(string, increase*PASS*sizeof(char)); 
        increase++;

        if(i==11)         //FIX DEZACORDS
        {    
            if(aux[0]==aux[1] && aux[1]==0 && aux[2]==1)
            {   
                strcpy(string, according(string, 1, "miliarde"));
            } 
            else if(aux[0]==aux[1] && aux[1]==0 && aux[2]==2)
            { 
                strcpy(string, according(string, 2, "miliarde")); 
            }
            else
            { 
                strcat(string, "miliarde ");
            }
        } 
        if(i==8)
        {   
            if(aux[0]==aux[1] && aux[1]==0 && aux[2]==1)
            { 
                strcpy(string, according(string, 1, "milioane"));
            }  
            else if(aux[0]==aux[1] && aux[1]==0 && aux[2]==2)
            { 
                strcpy(string, according(string, 2, "milioane"));
            } 
            else if(aux[0]==aux[1] && aux[1]==aux[2] && aux[2]==0) 
            {
                strcat(string, ""); 
            } 
            else 
            { 
                strcat(string, "milioane ");
            }
        } 
        if (i==5)
        {     
            if(aux[0]==aux[1] && aux[1]==0 && aux[2]==1)
            {    
                strcpy(string, according(string, 1, "mii"));  
            }  
            else if(aux[0]==aux[1] && aux[1]==0 && aux[2]==2)
            { 
                strcpy(string, according(string, 2, "mii"));
            } 
            else if(aux[0]==aux[1] && aux[1]==aux[2] && aux[2]==0)
            {
                strcat(string, "");
            }  
            else 
            { 
                strcat(string, "mii ");
            }    
        } 
        
    }

    free(array); 
    return string;
}