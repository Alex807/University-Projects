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

void afiseazaCuvintePrefix(NodArboreDeRegasire* nod, char* buffer, int nivel) { 
    if (nod->esteFinalCuvant == 1) { //printam cuvantul dupa ce adaugam terminatorul
        buffer[nivel] = '\0'; //se termina cuvantul
        printf("%s \n", buffer);
    }

    for (int i = 0; i < NUMAR_LITERE; i++) {
        
        if (nod->alfabet[i] != NULL) { 
            buffer[nivel] = 'A' + i;

            afiseazaCuvintePrefix(nod->alfabet[i], buffer, nivel + 1);
        }
    }
}

void cautaDupaPrefix(NodArboreDeRegasire* radacina, char* prefix)  { 
    if (radacina == NULL || prefix == NULL) { 
        printf ("Referintele date sunt NULE! \n"); 
        return;
    }
    NodArboreDeRegasire* aux = radacina; 
    char buffer[200]; 
    int nivel = 0;

    while (*prefix != '\0' && aux != NULL) {
        int index = toupper(*prefix) - 'A'; 

        aux = aux->alfabet[index]; 
        buffer[nivel] = toupper(*prefix);

        nivel++; 
        prefix++;
    }

    if (aux != NULL) { 
        afiseazaCuvintePrefix(aux, buffer, nivel);
    
    } else { 
        printf ("Nu exista cuv cu prefixul '%s'\n", prefix);
    }
}

int main(void) { 
    NodArboreDeRegasire* radacina = creeazaNod(); 

    insereazaCuvant(radacina, "MARCEL"); 
    insereazaCuvant(radacina, "MARE"); 
    insereazaCuvant(radacina, "MARINAR"); 
    insereazaCuvant(radacina, "ELENA"); 
    insereazaCuvant(radacina, "ELEFANT"); 
    insereazaCuvant(radacina, "PORC"); 

    //printf ("PORC: %s", cautaCuvant(radacina, "PORc") ? "Gasit\n" : "Nu a fost gasit\n");

    cautaDupaPrefix(radacina, "ELE");
}