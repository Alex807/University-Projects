#include <stdio.h> 
#include <stdlib.h> 
#include <ctype.h>
#include <string.h>

struct list{
    short int status;  //we use ( 1-busy ) and ( 0-free )
    int sizeInBits; //store the total size of node in bits
    char description[300];
    long int memoryAdress; //use long int for more space to save big positive numbers(in hexa are all positive)
    unsigned int uniqueID;
    struct list* next;
};
typedef struct list ListNode_t; 
typedef ListNode_t* List_t;

List_t make_node(unsigned int uniqueID, long int memoryAdress, char* description, int sizeInBits, short int status){
    List_t node = NULL;
    node = malloc(sizeof(struct list)); 

    if(node == NULL)
    { 
        printf("Error at malloc!\n"); 
        exit(-1);
    } 

    node->uniqueID = uniqueID; 
    node->memoryAdress = memoryAdress; 
    strcpy(node->description, description); 
    node->sizeInBits = sizeInBits; 
    node->status = status;
    node->next = NULL;

    return node;
} 

List_t insertSort_ByMemoryAdress(List_t L, unsigned int uniqueID, long int memoryAdress, char* description, int sizeInBits, short int status){
    List_t node = make_node(uniqueID, memoryAdress, description, sizeInBits, status);

    if (node == NULL) { 
        printf("Error at insert a new node!\n"); 
        exit(-1);
    } 

    if (L == NULL || memoryAdress < L->memoryAdress) { //punem semnul invers pt ca se schimba cand adaugam nodul la lista
        // Adaugă la începutul listei sau când memoryAdress este mai mic decât primul element
        node->next = L;
        L = node;
    } else {
        List_t current = L;

        while (current->next != NULL && memoryAdress > (current->next->memoryAdress) ) {
            current = current->next;
        }

        // Adaugă în mijlocul listei sau la sfârșit
        node->next = current->next;
        current->next = node;
    }
    return L;
} 

List_t insertSort_BySize(List_t L, List_t node){
    if (node == NULL) { 
        printf("Error at insert a new node!\n"); 
        exit(-1);
    } 

    if (L == NULL || node->sizeInBits < L->sizeInBits) { 
        // Adaugă la începutul listei sau când memoryAdress este mai mic decât primul element
        node->next = L;
        L = node;
    } else {
        List_t current = L;

        while (current->next != NULL && node->sizeInBits > (current->next->sizeInBits) ) {
            current = current->next;
        }

        // Adaugă în mijlocul listei sau la sfârșit
        node->next = current->next;
        current->next = node;
    }
    return L;
}

void print_list(List_t list){
    if (list == NULL){ 
        printf("Lista este momentan goala !\n");
        return;  //exit function
    }

    List_t copyOfList; 
    printf("\nID_unic \t Stare_resursa\n");

    for(copyOfList=list; copyOfList != NULL; copyOfList=copyOfList->next){ 
        if (copyOfList->status == 0){ //check if busy

            printf("  %x   \t\t   liber \n", copyOfList->uniqueID);
        }else if ( copyOfList->status == 1) { 

            printf("  %x   \t\t   ocupat \n", copyOfList->uniqueID);
        }
    }
    
    printf("\n");
}

char findSeparator_UsedInLine(char* line){ 
    char character;
    for (int index = 0; index < sizeof(line); index++){ //verify each character of line until find used separator
        character = line[index]; 

        if ( isdigit(character)== 0 && isalpha(character) == 0 ) { //check every character until find separator(is juct a character)
            return character;
        }
    }
    //end of for means no separator used in this line
    printf("Separatorul nu a putut fi gasit!\n"); 
    exit(-1);
}

List_t populateList(List_t list, char* fileName){ 
    FILE *file = NULL;
    if ((file = fopen(fileName, "r")) == NULL) {
        printf("Eroare la deschiderea fisierului!\n");
        exit(-1);
    }  

    unsigned int uniqueID; 
    long int memoryAdress; 
    int sizeInBits;
    char description[300], line[500];

    while ( (fgets(line, sizeof(line), file) != NULL) ){ 
        char separatorInFile = findSeparator_UsedInLine(line); //find separator for each line
        char* findedQuotes = strchr(line, '\"'); //search quotes to know exactly the format for each specific line

        if (findedQuotes != NULL){ //quotes are spoted in this line(used for any separator)
            if( (sscanf(line, "%x%c%ld%c\"%299[^\"]\"%c%d", &uniqueID, &separatorInFile, &memoryAdress, &separatorInFile, description, &separatorInFile, &sizeInBits)) == 7) { 
                list = insertSort_ByMemoryAdress(list, uniqueID, memoryAdress, description, sizeInBits, 0);
           
            }else{ 
                printf("Eroare la citirea unei linii din fisier ce contine \"  \"  !\n");
                exit(-1);
            }

        }

        if (separatorInFile == ',' && findedQuotes == NULL){ //quotes don't use in this specific line and separator is ','
            if ( (sscanf(line, "%x,%ld,%299[^,],%d", &uniqueID, &memoryAdress, description, &sizeInBits) ) == 4) { 
                list = insertSort_ByMemoryAdress(list, uniqueID, memoryAdress, description, sizeInBits, 0); //set free in first instance
            
            }else{ 
                printf("Eroare la citirea unei linii din fisier ce au separator ' , ' \n");
                exit(-1);
            }
        }
        
        if (separatorInFile == ';' && findedQuotes == NULL){ //quotes don't use in this specific line and separator is ';'
            if( (sscanf(line, "%x;%ld;%299[^;];%d", &uniqueID, &memoryAdress, description, &sizeInBits)) == 4) { 
                list = insertSort_ByMemoryAdress(list, uniqueID, memoryAdress, description, sizeInBits, 0); //set free in first instance
            
            }else{ 
                printf("Eroare la citirea unei linii din fisier ce au separator ' ; ' \n");
                exit(-1);
            }
        }
    }
    fclose(file); //close file after finish all reading operations
    return list;
}

void free_list(List_t L){ 
    if(L == NULL){ 
        return;
    } 
    free_list(L->next);  //use recursion for freeing all nods
    free(L);   
}

List_t copyNode(List_t originalNode) {//make a new node with same info for another list
    List_t newNode = make_node(originalNode->uniqueID, originalNode->memoryAdress, originalNode->description, originalNode->sizeInBits, originalNode->status);
    
    return newNode;
}

List_t findResource(List_t list, int resourceSize){ 
    List_t copyOfList = NULL; //we store in another list all free resources
    List_t resource = NULL;

    for(List_t aux = list; aux != NULL; aux = aux->next){ 
        if ( (aux->status == 0) && (aux->sizeInBits >= resourceSize) ) { //we add in list only available and bigger resources
            resource = copyNode(aux); //create new location in memory with info from 'aux' node
            copyOfList = insertSort_BySize(copyOfList, resource); //we create a new list of suitable nodes
        }
    } 

    if (copyOfList == NULL){ //case for doesn't find any resouce available and copyList is empty
        return NULL; //end function
    }

    resource = copyOfList;//best resource finded is first node of copyList because is created with 'insertSort_bySize'

    for(List_t aux = list; aux != NULL; aux = aux->next){ //search again in original list for set finded resoruce on busy
        if (aux->uniqueID == resource->uniqueID ) { //identify node by uniqueID(must be different from each node)
            aux->status = 1; //set resource busy
            resource = aux;  //save node of original list to can free copyOfList
        }
    }

    free_list(copyOfList); //remove from sistem
    return resource;
}

List_t deallocateResource(List_t list, unsigned int resourceUniqueID){ 
    for (List_t aux = list; aux != NULL; aux = aux->next){ 
        if (aux->uniqueID == resourceUniqueID){
            aux->status = 0; //deallocate finded node    1(busy)  0(free)

            printf("Dealocare realizata cu succes !!\n");
            return list; //stop searching after one finding(just one element has recieved 'uniqueID')
        }
    }//end of for means no resource with this 'uniqueID' exist in our list

    printf("Resursa cu ID_unic '%x' NU a fost gasita !!\n", resourceUniqueID); 
    return list; //we make no changes in our list in this case
}

void checkMemoryZone(List_t list){ 
    if (list == NULL){ 
        printf("Lista este momentan goala !\n");
        return;  //exit function
    }

    for(List_t aux = list; aux->next != NULL; aux = aux->next){//stop for early for can acces a forward node in if
        long int currentMemoryAdress = aux->memoryAdress;
        int currentSizeInBits = aux->sizeInBits;
        
        if ( (currentMemoryAdress + currentSizeInBits) != (aux->next)->memoryAdress ) { 
            printf("Zona de memorie NU este continua !!\n");
            return; //exit function if even one single node is not continous with previous  
        }

    }//end of for means all nodes are next to eachother
    printf("Zona de memorie este continua !!\n");
}

int main(int argc, char** argv){ 
    if (argc != 2){ //check if the command in console contain fileName
        printf("Numar invalid de argumente !\n"); 
        exit(-1);
    }

    char* fileName = argv[1]; //we rename the input data from keyboard of fileName that we read
    List_t list = NULL; 
    int optionForMenu;
    do{ 
        List_t allocatedResource = NULL;
        char menuOptions[] = "\tINTERFATA PROGRAM \n1. Initializare \n2. Alocare Resursa \n3. Dealocare Resursa \n4. Afisare Stare Resurse \n5. Eliberare Memorie \n6. Stare zona de memorie \n7. Iesire Program";
        printf("%s\nOPTIUNE: ", menuOptions);
        scanf("%d", &optionForMenu);

        switch (optionForMenu)
        {
        case(1):{//Initializare
            list = populateList(list, fileName);
            break;
        }
        case(2):{//Alocare Resursa
            int resourceSize;
            printf("Dimensiune resursa(in biti): ");
            scanf("%d", &resourceSize);

            //we return resource with sizeInBits >= resourceSize
            allocatedResource = findResource(list, resourceSize);
            if (allocatedResource != NULL ){ 
                printf("Alocare resursa reusita !!\n");
                printf("ID_unic: %x   Dimensiune: %d\n", allocatedResource->uniqueID, allocatedResource->sizeInBits);

            }else{ 
                printf("Nicio resursa NU a putut fi alocata !!\n");
            }
            break;
        }
        case(3):{//Dealocare Resursa
            unsigned int resourceUniqueID;
            printf("ID_unic resursa dealocata: "); 
            scanf("%x", &resourceUniqueID);
            
            list = deallocateResource(list, resourceUniqueID);
            break;
        }
        case(4):{//Afisare stare resurse
            print_list(list);
            break;
        }
        case(5):{//Eliberare memorie
            free_list(list);
            list = NULL;

            printf("Lista a fost golita cu succes !\n");
            break;
        }
        case(6):{//Afisare stare zona memorie
            checkMemoryZone(list); //print at console if list has a continous memory zone
            break;
        }
        default://comands who are not recognized
            if (optionForMenu != 7){ //7 used only for exit
                printf("   COMANDA INVALIDA !\n");
                break;
            }
        }
        printf("\n");
    }while(optionForMenu != 7); //exit program option
    printf("Programul a fost inchis. Pe curand !!\n");

    free_list(list); //in case user don't use free option last  
    return 0;
}