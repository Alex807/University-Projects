#include <stdlib.h> 
#include <stdio.h> 
#include <string.h> 
#include <ctype.h>

#define NUMAR_LITERE 26

struct NodArboreDeRegasire { 
    struct NodArboreDeRegasire* alfabet[NUMAR_LITERE + 1]; 
    int esteFinalCuvant;
}; 
typedef struct NodArboreDeRegasire NodArboreDeRegasire; 

// Funcție pentru a crea un nou nod al arborelui
NodArboreDeRegasire* creeazaNod() {
    NodArboreDeRegasire *nod = (NodArboreDeRegasire*)malloc(sizeof(NodArboreDeRegasire));
    nod->esteFinalCuvant = 0;
    for (int i = 0; i < NUMAR_LITERE + 1; i++) {
        nod->alfabet[i] = NULL;
    }
    return nod;
}

void insereazaCuvant(NodArboreDeRegasire* radacina, char* cuvant) { 
    if (radacina == NULL || cuvant == NULL) { 
        printf ("Referintele date sunt NULE! \n"); 
        return;
    }

    NodArboreDeRegasire* aux = radacina; 

    while (*cuvant != '\0') { 
        int index = toupper(*cuvant) - 'A'; 

        if (aux->alfabet[index] == NULL) { 
            aux->alfabet[index] = creeazaNod();
        } 
        aux = aux->alfabet[index]; 
        cuvant++;
    }
    aux->esteFinalCuvant = 1;
}

int areSufixul(char* buffer, char* sufix) { 
    int lungimeCuvant = strlen(buffer); 
    int lungimeSufix = strlen(sufix); 

    if (lungimeSufix > lungimeCuvant) return 0; //evitam cazul in care am deplasa spre stanga cuvantul di cond de return

    return strcmp(buffer + (lungimeCuvant - lungimeSufix), sufix) == 0;
}

void cautaDupaSufix(NodArboreDeRegasire* radacina, char* buffer, int nivel, char* sufix) { 
    if (radacina == NULL || sufix == NULL) { 
        printf ("Referintele date sunt NULE! \n"); 
        return;
    }

    if (radacina->esteFinalCuvant == 1) { 
        buffer[nivel] = '\0'; 
        
        if (areSufixul(buffer, sufix)) { 
            printf("%s \n", buffer);
        }
    } 

    for (int i = 0; i < NUMAR_LITERE; i++) { 
       
        if (radacina->alfabet[i] != NULL) { 
            buffer[nivel] = 'A' + i;

            cautaDupaSufix(radacina->alfabet[i], buffer, nivel + 1, sufix);
        }
    }
}

int main(void) { 
    NodArboreDeRegasire* radacina = creeazaNod(); 

    insereazaCuvant(radacina, "MARCEL"); 
    insereazaCuvant(radacina, "MARC"); 
    insereazaCuvant(radacina, "MARINAR"); 
    insereazaCuvant(radacina, "ELENA"); 
    insereazaCuvant(radacina, "ELEFANT"); 
    insereazaCuvant(radacina, "PORC"); 

    char buffer[200]; 
    int nivel = 0;
    cautaDupaSufix(radacina, buffer, nivel, "C"); //adaugat direct de aici campurile pentru a folosi recursivitatea in fct
}