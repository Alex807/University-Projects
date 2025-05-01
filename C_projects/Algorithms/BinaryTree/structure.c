#include <stdio.h>
#include <stdlib.h>

// Structura pentru un nod al arborelui binar
struct Nod {
    int valoare;
    struct Nod *stanga;
    struct Nod *dreapta;
};
typedef struct Nod* Nod;

// Funcție pentru a crea un nou nod
Nod creeazaNod(int valoare) {
    Nod nou = (Nod)malloc(sizeof(struct Nod));
    nou->valoare = valoare;
    nou->stanga = NULL;
    nou->dreapta = NULL;
    return nou;
}

// Funcție pentru a insera un nou nod în arbore, respectând regula arborelui binar
Nod insereazaNod(Nod radacina, int valoare) {
    // Dacă arborele este gol, nodul devine rădăcina
    if (radacina == NULL) {
        return creeazaNod(valoare);
    }

    // Parcurgem arborele și inserăm nodul în locul corespunzător
    if (valoare < radacina->valoare) {
        radacina->stanga = insereazaNod(radacina->stanga, valoare);
    } else {
        radacina->dreapta = insereazaNod(radacina->dreapta, valoare);
    }

    return radacina;
}

// Funcție pentru găsirea strămoșului comun cel mai recent, excluzând nodurile date
void gasesteStramosComun(Nod radacina, int nod1, int nod2) {

    while (radacina != NULL) {
        Nod ultimStramosComun = radacina;

        if (nod1 < radacina->valoare && nod2 < radacina->valoare) {
            radacina = radacina->stanga;
        }

        else if (nod1 > radacina->valoare && nod2 > radacina->valoare) {
            radacina = radacina->dreapta;
        }
        
        // Dacă nodurile se află pe ramuri diferite, am găsit strămoșul comun
        else {
            printf("Stramosul comun cel mai recent este: %d\n", ultimStramosComun->valoare);
            return;
        }
    }
}

// Main pentru testare
int main() {
    Nod radacina = NULL;

    // Adăugăm nodurile automat în arbore
    radacina = insereazaNod(radacina, 5);
    radacina = insereazaNod(radacina, 3);
    radacina = insereazaNod(radacina, 8);
    radacina = insereazaNod(radacina, 2);
    radacina = insereazaNod(radacina, 4);
    radacina = insereazaNod(radacina, 7);
    radacina = insereazaNod(radacina, 9);
    radacina = insereazaNod(radacina, 1);
    radacina = insereazaNod(radacina, 6);

    // Testăm funcția de găsire a strămoșului comun
    gasesteStramosComun(radacina, 6, 3);

    return 0;
}