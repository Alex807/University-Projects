#include <stdio.h> 
#include <stdlib.h> 

//graful este unul neorientat, deci in matricea de adiacenta se retine muchiile i-j si j-i cu acelasi cost
typedef struct Graf { 
    int indexNoduri; //memoram cate noduri avem in graf
    int* vectorNoduri;  //retinem toate nodurile din graf in ordine crescatore
    int** matriceAdiacenta;  //matricea de adiacenta a grafului (0 - nu exista muchie) altfel costul muchiei este retinut in matrice) 
}; 
typedef struct Graf* Graf;

void Prim (Graf graf, int start) {  
    int size = graf->indexNoduri; //dimensiunea grafului 
    int* vectorNoduri = graf->vectorNoduri; //vectorul de noduri al grafului 
    int** matrice = graf->matriceAdiacenta; //matricea de adiacenta a grafului

    int vizitat[size]; //vector in care retinem daca un nod a fost vizitat sau nu   0-NEVIZITAT 1-VIZITAT 
    int minPondere = 0, min_i, min_j; //retinem ponderea minima a muchiei si nodurile care o formeaza 
    int costTotal = 0; //retinem costul total al arborelui de acoperire minima 

    int existaConexiune = 0; //retinem daca exista conexiune intre nodul de start si restul nodurilor din graf(prevenim cazul in care algortimul nu poate fi aplicat)
    for (int i = 0; i < size; i++) { 
        if (matrice[start][i] != 0) { //daca exista muchia start-i
            existaConexiune = 1; //marcam ca exista conexiune
        } 
        vizitat[i] = 0; // initializam vectorul de vizitat cu 0 (niciun nod nu a fost vizitat)
    }
    vizitat[start] = 1; //marcam nodul de start ca vizitat 0-NEVIZITAT 1-VIZITAT 


    if (existaConexiune == 0) { //daca nu exista conexiune intre nodul de start si restul nodurilor din graf
        printf("Nu se poate aplica algoritmul Prim deoarece nu exista conexiune intre nodul de start si restul nodurilor din graf\n");
        return; //iesim din functie
    }
    
    for (int pas = 0; pas < size - 1; pas++) {  
        minPondere = 99999; //initializam ponderea minima cu o valoare foarte mare
        min_i = -1; //initializam nodurile cu -1 pentru a fi sigur ca nu exista noduri gasite momentan
        min_j = -1; 

        for (int i = 0; i < size; i++) { 

            for (int j = 0; j < size; j++) {  

                if (matrice[i][j] != 0 && vizitat[i] == 1 && vizitat[j] == 0) { //daca exista muchia i-j si nodul j nu a fost vizitat, DAR ne aflam in nodul i care a fost vizitat
                  
                    if (matrice[i][j] < minPondere) { 
                        minPondere = matrice[i][j]; //retinem ponderea minima a muchiei 
                        min_i = i; //retinem nodul i 
                        min_j = j; //retinem nodul j 
                    }
                    
                }
            }
        }
        costTotal += minPondere; //adaugam la costul total ponderea minima a muchiei 
        vizitat[min_j] = 1; //marcam nodul dest j ca vizitat 0-NEVIZITAT 1-VIZITAT (pentru a nu mai intra in el)

    } //avem complexitatea functiei de O(n^2)
    printf("Costul total al arborelui de acoperire minima este: %d\n", costTotal); //afisam costul total al arborelui de acoperire minima   
}
