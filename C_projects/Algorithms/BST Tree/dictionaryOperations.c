#include <stdlib.h> 
#include <stdio.h> 
#include <string.h> 
#include <ctype.h>

#define NUMAR_LITERE 26

struct NodArboreDeRegasire { 
    struct NodArboreDeRegasire* alfabet[NUMAR_LITERE + 1]; // punem '+1' pentru a retine si caracterul de sfarsit cuv 
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
        int index = toupper(*cuvant) - 'A'; //ne asiguram ca folosim cuvinte doar cu litera mare

        if (aux->alfabet[index] == NULL) { 
            aux->alfabet[index] = creeazaNod();
        }
        aux = aux->alfabet[index]; 
        cuvant++; //mutam referinta la urmatoarea litera din cuvant
    }
    aux->esteFinalCuvant = 1;
}

int cautaCuvant(NodArboreDeRegasire* radacina, char* cuvant) {  
    if (radacina == NULL || cuvant == NULL) { 
        printf ("Referintele date sunt NULE! \n"); 
        return;
    }
    NodArboreDeRegasire* aux = radacina; 
   
    while (*cuvant != '\0') { 
        int index = toupper(*cuvant) - 'A'; //ne asiguram ca folosim cuvinte doar cu litera mare

        if (aux->alfabet[index] == NULL) { 
            return 0;
        }
        aux = aux->alfabet[index];
        cuvant++;
    }
    return aux->esteFinalCuvant;
}

int main(void) { 
    NodArboreDeRegasire* radacina = creeazaNod();

    insereazaCuvant(radacina, "MARCEl"); 

    printf("Cuvant cautat: MaRCEL - %s", cautaCuvant(radacina, "MaRCEL") ? "Gasit\n" : "Nu a fost gasit\n");
}