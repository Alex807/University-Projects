#include <stdio.h>
#include <stdlib.h>

struct Nod {
    int valoare;
    struct Nod *stanga;
    struct Nod *dreapta;
};
typedef struct Nod* Nod;

Nod cautareNod(Nod radacina, int valoare) { //avem o complexitate de O(n) deoarece parcurgem arborele in intregime, nefiiind un arbore echilibrat
    if (radacina == NULL || radacina->valoare == valoare) {
        return radacina; // returnam radacina daca arborele este gol sau daca valoarea a fost gasita
    }

    if (valoare < radacina->valoare) {
        return cautareNod(radacina->stanga, valoare); // mergem in subarborele stang
    }
    return cautareNod(radacina->dreapta, valoare); // mergem in subarborele drept
}

// cautam nodul cel mai mic din arbore
Nod gasireMinim(Nod radacina) {
    Nod curent = radacina;

    while (curent == NULL && curent->stanga != NULL) {
        curent = curent->stanga;
    }
    return curent;
}

void parcurgereInOrdine(Nod radacina) {
    if (radacina != NULL) {
        parcurgereInOrdine(radacina->stanga); // Parcurgem subarborele stang
        printf("%d ", radacina->valoare); // Procesam nodul curent
        parcurgereInOrdine(radacina->dreapta); // Parcurgem subarborele drept
    }
}