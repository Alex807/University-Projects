#include <stdio.h> 
#include <stdlib.h>

#define ALPHABET_SIZE 26

struct Nod {
    struct Nod *children[ALPHABET_SIZE];
    int isEndOfWord;
};
typedef struct Nod* Nod;

int countWords(Nod nod) {
    int count = 0;

    if (nod->isEndOfWord) { // verificam daca nodul curent este sfarsitul unui cuvant(caz de baza pentru a opri recursivitatea)
        count++; // daca da, adaugam 1 la contor
    }

    for (int i = 0; i < ALPHABET_SIZE; i++) { // parcurgem fiecare litera din alfabet
        if (nod->children[i] != NULL) { // verificam daca exista copil pentru litera curenta
            count += countWords(nod->children[i]); // recursivitate pentru a numara toate cuvintele din subarborele curent
        }
    }

    return count; // returnam numarul de cuvinte ce incep cu prefixul dat
}

// functia principala pentru numararea cuvintelor cu un anumit prefix în arborele de regasire(aici se retin doar nodurile care incep cu prefixul, urmanand sa fie numarate cu ajutorul functiei auxiliare 'countWordsWithPrefixHelper')
int filterWords_ByPrefix(Nod root, const char *prefix) {
    Nod current = root; // folosim un pointer auxiliar pentru a retine radacina arborelui de regasire

    for (int i = 0; i < strlen(prefix); i++) { // Parcurgem fiecare caracter din prefix

        int index = prefix[i] - 'A'; // Convertim primul caracter din prefix intr-un index (0-25) pentru a accesa copiii nodului curent ce incep cu acel caracter
        if (current->children[index] == NULL) { // verifica daca exista copil pentru litera curenta din prefix
            
            return 0; // daca nu exista prima litera, inseamna ca nu exista niciun cuvant cu prefixul dat, deci returnam 0
        }
        current = current->children[index]; // trecem la urmatorul nod din arbore
    }
    return countWords(current); // apelam functia auxiliara pe setul de date ce incep NUMAI cu prefixul dat
}

int filterWords_BySuffix(Nod root, const char *suffix) {
    Nod current = root; // Folosim un pointer auxiliar pentru a reține rădăcina arborelui de regăsire

    int suffixLen = strlen(suffix);
    int wordCount = 0;

    // Parcurgem arborele pentru a găsi toate cuvintele cu sufixul dat
    for (int i = suffixLen - 1; i >= 0; i--) { // Parcurgem invers fiecare caracter din sufix
        int index = suffix[i] - 'A'; // Convertim caracterul din sufix într-un index (0-25) pentru a accesa copiii nodului curent

        if (current->children[index] == NULL) { // verificam daca exista copil pentru litera curenta din sufix
            return 0; // daca nu exista prima litera, inseamna ca nu exista niciun cuvant cu sufixul dat, deci returnam 0
        }

        current = current->children[index]; // trecem la nodul copil corespunzător caracterului din sufix
    }
}

//NUMARAM CATE CUVINTE INCEP CU UN PREFIX/SUFIX ANUME 
int countWordsFroFile(Nod root, const char *filename) {
    FILE *file = fopen(filename, "r"); // deschidem fisierul in modul de citire

    if (file == NULL) { // verificam daca fisierul a fost deschis cu succes
        printf("Eroare la deschiderea fisierului %s\n", filename);
        return 0;
    }

    char word[100]; // alocam un buffer pentru a citi cuvintele din fisier
    int count = 0; // initializam un contor pentru a numara cuvintele ce incep cu prefixul dat

    while (fscanf(file, "%s", word) != EOF) { // citim fiecare cuvant din fisier
        if (filterWords_ByPrefix(root, word) != 0) { // apelam functia de filtrare pentru a numara cuvintele ce incep cu prefixul dat
            count++; // daca cuvantul incepe cu prefixul dat, adaugam 1 la contor
        }
    }

    fclose(file); // inchidem fisierul
    return count; // returnam numarul de cuvinte ce incep cu prefixul dat
}

//NUMARAM CATE CUVINTE INCEP CU UN PREFIX ANUME citite din fisier
int countWords_andCharacters(Nod nod, int* totalLenght) { //vom numara cate cuvinte avem si vom retine in parametrul 'totalLenght' nr total de caractere ce formeaza fiecare cuvant
    if (nod == NULL) { // caz de baza pentru a opri recursivitatea
        return 0;
    }

    int count = 0; // initializam un contor pentru a numara cuvintele
    if (nod->isEndOfWord) { // verificam daca am ajuns la sfarsitul unui cuvant
        count++; // daca da, adaugam 1 la contor
        *totalLenght += 1; // adaugam 1 la numarul total de caractere ce formeaza cuvantul
    }

    for (int i=0; i<ALPHABET_SIZE; i++) { // parcurgem fiecare litera din alfabet
        
        if (nod->children[i] != NULL) { // verificam daca exista copil pentru litera curenta
            *totalLenght += 1; // adaugam 1 la numarul total de caractere ce formeaza cuvantul
            count += countWords_andCharacters(nod->children[i], totalLenght); // apelam recursivitate pentru a numara cuvintele din subarborele curent
        } 
    }
    return count; // returnam numarul de cuvinte
}

double averageWordLength(Nod root) {
    int totalLenght = 0; // initializam un contor pentru a retine numarul total de caractere ce formeaza cuvintele
    int wordCount = countWords_andCharacters(root, &totalLenght); // apelam functia auxiliara pentru a numara cuvintele si a retine numarul total de caractere ce formeaza cuvintele

    if (wordCount == 0) { // verificam daca avem cuvinte in arbore
        return 0; // daca nu, returnam 0
    }
    return (double)totalLenght / wordCount; // returnam media lungimii cuvintelor
}
