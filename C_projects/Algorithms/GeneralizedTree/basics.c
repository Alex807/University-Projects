#include <stdio.h>
#include <stdlib.h>

struct Nod {
    char Nume[30];
    struct Nod *Prim_fiu;
    struct Nod *Frate_drept;
    struct Nod *Tata;
};
typedef struct Nod* Nod;

void inserareNod(Nod parinte, Nod copil) {
    if (parinte == NULL || copil == NULL) {
        fprintf(stderr, "Eroare: Nodul parinte sau nodul copil este NULL.\n");
        return;
    }

    if (parinte->Prim_fiu == NULL) {
        parinte->Prim_fiu = copil;
    } else {
        Nod curent = parinte->Prim_fiu;

        while (curent->Frate_drept != NULL) {
            curent = curent->Frate_drept;
        }
        curent->Frate_drept = copil;
    }
    copil->Tata = parinte;
}

Nod cautareNod(Nod radacina, char nume[]) {
    if (radacina == NULL) {
        return NULL;
    }

    if (strcmp(radacina->Nume, nume) == 0) {
        return radacina;
    }

    Nod *rezultat = NULL;

    // Cautam in primul fiu si apoi in toti fratii urmatori
    if ((rezultat = cautareNod(radacina->Prim_fiu, nume)) != NULL) { //functia are o complexitate de O(n)
        return rezultat;
    }

    if ((rezultat = cautareNod(radacina->Frate_drept, nume)) != NULL) {
        return rezultat;
    }
    return NULL; // Nodul nu a fost gasit
}