#include <stdio.h> 
#include <stdlib.h>  
#include <string.h>
#include <ctype.h>
#include <math.h>

#define allowed_characters_Octal "01234567" 

int validate_input_data(char* number, int type_number_base)
{ 
    switch(type_number_base)
    { 
        case(1): 
        { 
            for(int index=0; index<strlen(number); index++)
            { 
                if ( isdigit(number[index]) == 0)
                { 
                    return 1;
                }
            }
            return 0;

        }   

        case(2): 
        { 
            for(int index=0; index<strlen(number); index++)
            { 
                if ( (isxdigit(number[index])) == 0)  //isxdigit checks if character is allowed in hexazecimal base
                { 
                    return 1;
                }
            }
            return 0;

        } 
        
        case(3): 
        { 
            for(int index=0; index<strlen(number); index++)
            { 
                if ( number[index] != '0' && number[index] != '1')
                { 
                    return 1;
                }
            }
            return 0;
        } 
        
        case(4): 
        { 
            for(int index=0; index<strlen(number); index++)
            { 
                if ( (strchr(allowed_characters_Octal, number[index])) == NULL )
                { 
                    return 1;
                }
            }
            return 0;
        } 

        default: 
        {
            return 1; //arrive here just if character is NOT allowed for neither base
        }
    }
}  

int select_and_validate_numeration_base(void)
{ 
    int type_numeration_base;

    printf("\nAlegeti baza de numeratie:\n1.Zecimal\n2.Hexazecimal\n3.Binar\n4.Octal\nOPTIUNE: ");  
    scanf("%d", &type_numeration_base); 

    while(type_numeration_base<1 || type_numeration_base>4)
    { 
        printf("\nOPTIUNE INVALIDA! Alegeti alta baza de numeratie:\n1.Zecimal\n2.Hexazecimal\n3.Binar\n4.Octal\nOPTIUNE: ");
        scanf("%d", &type_numeration_base);
    } 
    getchar(); // used for remove character '\n' from input buffer(remain from 'scanf' )

    return type_numeration_base;
}

char* read_number(int type_numeration_base)
{
    char* number=NULL; 

    if ( (number=(char*)malloc(sizeof(char))) == NULL)
    { 
        perror("Error at first allocation for readind number!\n"); 
        exit(-1);
    }

    int increase_memory=2, index; 
    char character_of_number;  

    printf("Numarul: "); 

    for (index=0; (character_of_number=getchar() ) != '\n'; index++)
    {   
        number[index] = character_of_number; 

        if ( (number = (char*)realloc(number, increase_memory * sizeof(char))) == NULL)
        { 
            printf("Error at %d reallocation for readind number!\n", index); 
            exit(-1);
        }

        increase_memory++;
        
    }
    number[index] = '\0';   //must be use for sign the end of string 

    if( validate_input_data(number, type_numeration_base) != 0)
    { 
        free(number);
        printf("\nNUMARUL CONTINE CARACTERE INVALIDE!\n");

        read_number(type_numeration_base);
    }

    return number;
}

char* invert_string(char* string, int index)
{
    char temp;
    for (int i = 0, j = index - 1; i < j; i++, j--)
    {
        temp = string[i];
        string[i] = string[j];
        string[j] = temp;
    }

    return string;
}

char* decimal_to_binar_or_octal(int decimal_number, int divisor) 
{ 
    char* string_number=NULL; 
    if ( (string_number = (char*)malloc(sizeof(char))) == NULL)
    { 
        perror("Error at first allocation for converting decimal!\n"); 
        exit(-1);
    } 

    int increase_memory=2, index=0;  
    while (decimal_number > 0)
    { 
        int digit = decimal_number % divisor;
        
        string_number[index] = '0' + digit; 

        index++; 
        decimal_number = decimal_number / divisor;

        if ((string_number = (char*)realloc(string_number, increase_memory * sizeof(char))) == NULL)
        { 
            printf("Error at %d reallocation for rewrite number!\n", index); 
            exit(-1);
        }
        
        increase_memory++;
    }
    string_number[index] = '\0';   // Adaugăm terminatorul de șir

    string_number = invert_string(string_number, index);

    return string_number;
} 

char* decimal_to_hexadecimal(int decimal_number)
{ 
    char* hexadecimal_number=NULL; 
    if ( (hexadecimal_number = (char*)malloc(sizeof(char))) == NULL)
    { 
        perror("Error at first allocation for converting decimal-hexa!\n"); 
        exit(-1);
    }

    int increase_memory=2, index=0; 
    while (decimal_number > 0)
    { 
        int digit = decimal_number % 16;
        
        if (digit < 10)
        {
            hexadecimal_number[index] = '0' + digit;  //convert from digit to character of that digit
        }
        else
        {
            hexadecimal_number[index] = 'A' + (digit - 10);  //convert from result in digits to letter 
        }

        index++; 
        decimal_number = decimal_number / 16;

        if ((hexadecimal_number = (char*)realloc(hexadecimal_number, increase_memory * sizeof(char))) == NULL)
        { 
            printf("Error at %d reallocation for hexadecimal number!\n", index); 
            exit(-1);
        }
        
        increase_memory++;
    }
    hexadecimal_number[index] = '\0';   // must be at the end of string


    hexadecimal_number=invert_string(hexadecimal_number, index);  //invert string to have right order of digits 

    return hexadecimal_number;

}

void decimal_to_other_bases(char* number)
{ 
//-------------------------------------------------------------HEXADECIMAL
    int decimal_number = atoi(number);

    char* hexadecimal_number = decimal_to_hexadecimal(decimal_number);

    printf("2.HEXAZECIMAL:  %s\n", hexadecimal_number);
    free(hexadecimal_number); 
//---------------------------------------------------------BINAR 
    char* binar_number = decimal_to_binar_or_octal(decimal_number, 2); 
    printf("3.BINAR:\t%s\n", binar_number);

    free(binar_number);
//---------------------------------------------------OCTAL 
    char* octal_number = decimal_to_binar_or_octal(decimal_number, 8);
    printf("4.OCTAL:\t%s\n", octal_number);

    free(octal_number);
}

int hexadecimal_to_decimal(char* number)
{ 
    int decimal_number=0, digit;  
    int exponent = strlen(number)-1;  //used for raise 16 with pow function

    for(int index=0; index<strlen(number); index++)
    {
        char character = number[index];

        if ( isdigit(character) != 0 )
        { 
            digit = character - '0'; //convert from character digit in integer  
        }
        else 
        { 
            digit = ( toupper(character) - 'A' ) + 10;   //used for convert letters from hexa in integers
        } 
        int value = digit * pow(16, exponent);
        decimal_number +=  value; 
        exponent--;
    }

    return decimal_number;
}

void hexadecimal_to_other_bases(char* number)
{
//--------------------------------------------DECIMAL 
    int decimal_number = hexadecimal_to_decimal(number);
    printf("\n1.ZECIMAL:\t%d\n", decimal_number);
//---------------------------------------------HEXADECIMAL 

    printf("2.HEXAZECIMAL:  %s\n", number); 

//---------------------------------------------BINAR 
    char* binar_number = decimal_to_binar_or_octal(decimal_number, 2); 
    printf("3.BINAR:\t%s\n", binar_number);

    free(binar_number);
//----------------------------------------------OCTAL
    char* octal_number = decimal_to_binar_or_octal(decimal_number, 8);
    printf("4.OCTAL:\t%s\n", octal_number);

    free(octal_number);
}

int binar_or_octal_to_decimal(char* string_number, int divisor)
{ 
    int decimal_number = 0;
    int exponent = strlen(string_number) - 1; 

    for (int index=0; index<strlen(string_number); index++)
    {   
        char character = string_number[index];
        int digit = character - '0';  //used for make character in integer 

        int value = digit * pow(divisor, exponent);
        decimal_number +=  value; 
        exponent--;
    }

    return decimal_number;
}

void binar_to_other_bases(char* binar_number)
{ 
//-----------------------------------------------------DECIMAL 
    int decimal_number = binar_or_octal_to_decimal(binar_number, 2); 
    printf("\n1.ZECIMAL:\t%d\n", decimal_number);
//---------------------------------------------------HEXADECIMAL
    char* hexadecimal_number = decimal_to_hexadecimal(decimal_number); 
    printf("2.HEXAZECIMAL:  %s\n", hexadecimal_number);

    free(hexadecimal_number);
//----------------------------------------------------BINAR
    printf("3.BINAR:\t%s\n", binar_number); 
//-----------------------------------------------------OCTAL 
    char* octal_number = decimal_to_binar_or_octal(decimal_number, 8);
    printf("4.OCTAL:\t%s\n", octal_number);

    free(octal_number);
} 

void octal_to_other_bases(char* octal_number)
{ 
//-----------------------------------------------------DECIMAL 
    int decimal_number = binar_or_octal_to_decimal(octal_number, 8); 
    printf("\n1.ZECIMAL:\t%d\n", decimal_number);
//---------------------------------------------------HEXADECIMAL
    char* hexadecimal_number = decimal_to_hexadecimal(decimal_number); 
    printf("2.HEXAZECIMAL:  %s\n", hexadecimal_number);

    free(hexadecimal_number);
//----------------------------------------------------BINAR
    char* binar_number = decimal_to_binar_or_octal(decimal_number, 2);
    printf("3.BINAR:\t%s\n", binar_number); 

    free(binar_number);
//-----------------------------------------------------OCTAL 
    printf("4.OCTAL:\t%s\n", octal_number);
}

void convert_number_to_other_bases(char* number, int type_numeration_base)
{ 
    switch(type_numeration_base)
    { 
        case(1): //case for a number in decimal base
        { 
            int decimal_number = atoi(number);
            printf("\n1.ZECIMAL:\t%d\n", decimal_number);  

            decimal_to_other_bases(number); 
            break;
        }   

        case(2): //case for a number in hexadecimal base
        { 
            hexadecimal_to_other_bases(number);
            break;
        } 
        
        case(3): //case for a number in binar base
        { 
            binar_to_other_bases(number); 
            break;
        }
        
        case(4): //case for a number in octal base
        { 
            octal_to_other_bases(number);
            break;
        } 
    }
}

int main(void)
{ //1.Decimal  2.Hexadecimal  3.Binar  4.Octal
    int type_numeration_base = select_and_validate_numeration_base();
    char* number = read_number(type_numeration_base);  

    convert_number_to_other_bases(number, type_numeration_base);
    
    free(number);
    return 0;
}