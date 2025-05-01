#include <stdio.h>
#include <stdlib.h>

#define N 1          // Gradul minim al arborelui B
#define NN (2 * N)   // Numărul maxim de chei într-o pagină

typedef struct Pagina Pagina;
struct Nod {
    int cheie;                    // Valoarea nodului
    Pagina *subpgDreapta;         // Pointer către subpagina dreapta
};

struct Pagina {
    int nrNoduri;                 // Totalul de noduri din pagină
    struct Nod vector[NN + 1];    // Toate nodurile din pagina curentă (+1 pentru overflow temporar în timpul scindării)
    Pagina *subpgStanga;          // subpgStanga pentru cheile mai mici decât prima cheie din pagină
};

// Funcție de creare a unei noi pagini
Pagina *creeazaPagina() {
    Pagina *nouaPagina = (Pagina *)malloc(sizeof(Pagina));
    if (!nouaPagina) {
        printf("Eroare la alocarea memoriei pentru pagina!\n");
        exit(1);
    }
    nouaPagina->nrNoduri = 0;
    nouaPagina->subpgStanga = NULL;
    for (int i = 0; i <= NN; i++) {
        nouaPagina->vector[i].subpgDreapta = NULL;
    }
    return nouaPagina;
}

void insereazaInPagina(Pagina *pagina, int cheie, int *cheiePromovata, Pagina **subpgDreapta) {
    int i;
    *cheiePromovata = -1; // Initializare cu nimic de promovat
    *subpgDreapta = NULL;

    // Găsim poziția unde să inserăm cheia
    for (i = pagina->nrNoduri - 1; i >= 0 && cheie < pagina->vector[i].cheie; i--) {
        pagina->vector[i + 1] = pagina->vector[i]; // Mutăm nodurile la dreapta
    }

    // Inserăm cheia
    pagina->vector[i + 1].cheie = cheie;
    pagina->vector[i + 1].subpgDreapta = *subpgDreapta; // Setăm subpgDreapta din dreapta
    pagina->nrNoduri++;

    // Dacă pagina este plină, trebuie să o scindăm
    if (pagina->nrNoduri > NN) {
        Pagina *nouaPagina = creeazaPagina();
        int mid = NN / 2; // Midpoint pentru scindare

        // Mutăm cheile și subpaginile după mijloc în noua pagină
        nouaPagina->subpgStanga = pagina->vector[mid + 1].subpgDreapta; // Actualizare pentru subpgStanga a noii pagini
        pagina->vector[mid].subpgDreapta = NULL;  // Actualizăm referința 

        for (int j = mid + 1; j < pagina->nrNoduri; j++) {
            nouaPagina->vector[j - (mid + 1)] = pagina->vector[j];
            nouaPagina->vector[j - (mid + 1)].subpgDreapta = pagina->vector[j].subpgDreapta;
        }
        nouaPagina->nrNoduri = pagina->nrNoduri - mid - 1;
        pagina->nrNoduri = mid;

        // Setăm cheia de promovat și subpagina dreapta
        *cheiePromovata = pagina->vector[mid].cheie;
        *subpgDreapta = nouaPagina;
    }
}

Pagina* inserareInArbore(Pagina* pagina, int cheie, int *cheiePromovata, Pagina **subpgDreapta) {
    if (pagina == NULL) {
        *cheiePromovata = cheie;
        *subpgDreapta = NULL;
        return NULL;
    }

    // Dacă pagina este o pagină frunză
    if (pagina->subpgStanga == NULL) {
        insereazaInPagina(pagina, cheie, cheiePromovata, subpgDreapta);
        return pagina;
    }

    // Găsim subpagina corectă
    int i;
    for (i = pagina->nrNoduri; i >= 1 && cheie < pagina->vector[i - 1].cheie; i--);

    // Inserăm cheia în subpagina corectă
    inserareInArbore(i == 0 ? pagina->subpgStanga : pagina->vector[i - 1].subpgDreapta, cheie, cheiePromovata, subpgDreapta);

    if (*cheiePromovata != -1) {
        // Dacă o cheie trebuie promovată
        int totalNoduri = pagina->nrNoduri;
        if (totalNoduri < NN) {
            // Inserăm cheia promovată
            for (int j = totalNoduri; j > i; j--) {
                pagina->vector[j] = pagina->vector[j - 1];
                pagina->vector[j].subpgDreapta = pagina->vector[j - 1].subpgDreapta;
            }
            pagina->vector[i].cheie = *cheiePromovata;
            pagina->vector[i].subpgDreapta = *subpgDreapta;
            pagina->nrNoduri++;
            *cheiePromovata = -1;
            *subpgDreapta = NULL;
        } else {
            // Trebuie să scindăm pagina
            Pagina *nouaPagina = creeazaPagina();
            int mid = NN / 2;

            // Inserăm cheia promovată în pagina curentă
            for (int j = totalNoduri; j > i; j--) {
                pagina->vector[j] = pagina->vector[j - 1];
                pagina->vector[j].subpgDreapta = pagina->vector[j - 1].subpgDreapta;
            }
            pagina->vector[i].cheie = *cheiePromovata;
            pagina->vector[i].subpgDreapta = *subpgDreapta;
            pagina->nrNoduri++;

            // Setăm noua subpagina stânga pentru noua pagină
            nouaPagina->subpgStanga = pagina->vector[mid].subpgDreapta;

            // Mutăm cheile și subpaginile în noua pagină
            for (int j = mid + 1; j < pagina->nrNoduri; j++) {
                nouaPagina->vector[j - (mid + 1)] = pagina->vector[j];
                nouaPagina->vector[j - (mid + 1)].subpgDreapta = pagina->vector[j].subpgDreapta;
            }
            nouaPagina->nrNoduri = pagina->nrNoduri - (mid + 1);

            // Actualizăm numărul de noduri pentru pagina originală
            pagina->nrNoduri = mid;

            // Setăm cheia de promovat
            *cheiePromovata = pagina->vector[mid].cheie;

            // Setăm noua subpagina dreapta
            *subpgDreapta = nouaPagina;

            // // Optional: resetăm cheia promovată în pagina originală
            pagina->vector[mid].cheie = 0; // Sau un alt mod de a semnaliza că această poziție nu mai este validă
            pagina->vector[mid].subpgDreapta = NULL; // Pentru siguranță
        }
    } else {
        *subpgDreapta = NULL;
    }

    return pagina;
}

// Funcție pentru inserare în arborele B și gestionarea rădăcinii
Pagina* inserare(Pagina* radacina, int cheie) {
    int cheiePromovata;
    Pagina *subpgDreapta = NULL;

    radacina = inserareInArbore(radacina, cheie, &cheiePromovata, &subpgDreapta);

    // Dacă o cheie a fost promovată, trebuie să creăm o nouă rădăcină
    if (cheiePromovata != -1) {
        Pagina* nouaRadacina = creeazaPagina();
        nouaRadacina->vector[0].cheie = cheiePromovata;
        nouaRadacina->vector[0].subpgDreapta = subpgDreapta;
        nouaRadacina->nrNoduri = 1;
        nouaRadacina->subpgStanga = radacina;
        return nouaRadacina;
    }

    return radacina;
}

// Funcție de afișare a arborelui
void afisareArbore(Pagina *radacina) {
    if (radacina == NULL) return;

    Pagina *coada[100];
    int niveluri[100];
    int inceput = 0, sfarsit = 0;
    
    coada[sfarsit] = radacina;
    niveluri[sfarsit] = 0;
    sfarsit++;
    
    int nivelCurent = 0;
    
    while (inceput < sfarsit) {
        Pagina *paginaCurenta = coada[inceput];
        int nivel = niveluri[inceput];
        inceput++;
        
        if (nivel > nivelCurent) {
            printf("\n");
            nivelCurent = nivel;
        }
        
        printf("[");
        for (int i = 0; i < paginaCurenta->nrNoduri; i++) {
            printf("%d", paginaCurenta->vector[i].cheie);
            if (i < paginaCurenta->nrNoduri - 1) {
                printf(", ");
            }
        }
        printf("] ");
        
        if (paginaCurenta->subpgStanga) {
            coada[sfarsit] = paginaCurenta->subpgStanga;
            niveluri[sfarsit] = nivel + 1;
            sfarsit++;
        }
        for (int i = 0; i < paginaCurenta->nrNoduri; i++) {
            if (paginaCurenta->vector[i].subpgDreapta) {
                coada[sfarsit] = paginaCurenta->vector[i].subpgDreapta;
                niveluri[sfarsit] = nivel + 1;
                sfarsit++;
            }
        }
    }
    
    printf("\n");
}


void afisareVecini(Pagina* paginaParinte, int cheieCautata) { 
    if (paginaParinte == NULL) { 
        printf ("Cheia %d se afla in pagina radacina !\n", cheieCautata); 
        return;
    }
    printf ("\nCheia cautat este <%d>\n", cheieCautata);

    int size = paginaParinte->nrNoduri;
    int i;
    for (i = size - 1; i >= 0; i--) { 
        int nodCurent = paginaParinte->vector[i].cheie;

        if (nodCurent < cheieCautata) { 
            printf ("Vecin superior STANG: %d\n", nodCurent); 
            break;
        }
    }

    if (i == -1) { // am parcurs tot si NU am gasit cv mai MIC decat cheiaCautat 
        printf ("NU avem vecin superior STANG !\n");
    }

    if (i < (size - 1) ) { //sa mai fie elemente in dreapta paginii parinte(vecinSTANG sa nu fie maxDRP in pagina)
        printf ("Vecin superior DREPT: %d\n", paginaParinte->vector[i+1].cheie);
    
    } else  { 
        printf ("NU avem vecin superior DREPT !\n");
    }
}

void cautaPaginaCheie(Pagina* radacina, int cheieCautata, Pagina* paginaParinte) { //folosim atributul 'paginaParinte' pentru a putea folosi recursivitatea in cautarea paginii ce contine  cheia
    if (radacina == NULL) { 
        printf("Cheia <%d> NU exista in arboreleB !\n",  cheieCautata); 
        return;
    }
    int i = 0; //cautam cel mai mare dar mai mic nod decat cheiaCautata
    int nrNoduri = radacina->nrNoduri;
    while (i < nrNoduri && cheieCautata > radacina->vector[i].cheie) { 
        i++;
    }

   //obligatoriu prima clauza de if pentru a verifica daca ai gasit deja cheia in aceasta pagina
    if (cheieCautata == radacina->vector[i].cheie) { //cheia cautata a fost gasita in aceasta pagina si transmitem pagina parinte
        afisareVecini(paginaParinte, cheieCautata);

    } else if (i == 0) { //cheia cautata este mai mica si decat ce mai mica cheie din paginaCurenta
        cautaPaginaCheie(radacina->subpgStanga, cheieCautata, radacina); //cautam in partea stanga a paginii curente
   
    }else { 
        cautaPaginaCheie(radacina->vector[i - 1].subpgDreapta, cheieCautata, radacina);
    }

}


int main(void) {
    Pagina *radacina = NULL;

    // Inserăm chei
    radacina = inserare(radacina, 10);
    radacina = inserare(radacina, 20);
    radacina = inserare(radacina, 5);
    radacina = inserare(radacina, 6);
    radacina = inserare(radacina, 12);
    radacina = inserare(radacina, 30);
    radacina = inserare(radacina, 7);
    radacina = inserare(radacina, 17); 
    radacina = inserare(radacina, 18);
    // radacina = inserare(radacina, 40);
    // radacina = inserare(radacina, 13);
    // radacina = inserare(radacina, 14);
    // radacina = inserare(radacina, 16);
    // radacina = inserare(radacina, 50);
    // radacina = inserare(radacina, 60);
    // radacina = inserare(radacina, 70);
    // radacina = inserare(radacina, 8);
    // radacina = inserare(radacina, 9);
    // radacina = inserare(radacina, 80);
    // radacina = inserare(radacina, 90);
    // radacina = inserare(radacina, 100);
    // radacina = inserare(radacina, 110);
    // radacina = inserare(radacina, 120);
    // radacina = inserare(radacina, 130);
    // radacina = inserare(radacina, 140);
    // radacina = inserare(radacina, 150);
    // radacina = inserare(radacina, 160);

    // Afișăm arborele
    printf("Structura arborelui B:\n");
    afisareArbore(radacina);

    Pagina* paginaParinte = NULL;
    cautaPaginaCheie(radacina, 10, paginaParinte);
    return 0;
}
