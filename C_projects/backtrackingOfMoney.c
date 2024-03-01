#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <math.h>

struct list {
    int cantitateMonede;
    double valoareMoneda;
    struct list *next;
};
typedef struct list *List_t;

List_t make_node(int cantitateMonede, double valoareMoneda) {
    List_t node = malloc(sizeof(struct list));

    if (node == NULL) {
        printf("Error at malloc!\n");
        exit(-1);
    }

    node->cantitateMonede = cantitateMonede;
    node->valoareMoneda = valoareMoneda;
    node->next = NULL;
    return node;
}

List_t insertSort_ByValue(List_t L, int cantitateMonede, double valoareMoneda) {
    List_t node = make_node(cantitateMonede, valoareMoneda);

    if (node == NULL) {
        printf("Error at insert a new node!\n");
        exit(-1);
    }

    if (L == NULL || node->valoareMoneda < L->valoareMoneda) {
        node->next = L;
        L = node;
    } else {
        List_t current = L;

        while (current->next != NULL && node->valoareMoneda > current->next->valoareMoneda) {
            current = current->next;
        }

        node->next = current->next;
        current->next = node;
    }
    return L;
}

int checkInputData(char *string, char *tipDate) {
    char caracter;
    int size = strlen(string);

    if (strcmp(tipDate, "double") == 0) {
        for (int index = 0; index < size; index++) {
            caracter = string[index];
            if (isdigit(caracter) == 0 && caracter != '.' && caracter != '-') {
                printf("\tValoare invalida !!\n");
                return 0;
            }
        }

    } else if (strcmp(tipDate, "int") == 0) {
        for (int index = 0; index < size; index++) {
            caracter = string[index];
            if (isdigit(caracter) == 0 && caracter != '-') {
                printf("\tValoare invalida !!\n");
                return 0;
            }
        }
    }
    return 1;
}

List_t populateList() {
    List_t list = NULL;

    char buffer[201];
    int cantitateMonede;
    double valoareMoneda;
    int index = 1;

    while (1) {
        fflush(stdin);
        int verificareInput;

        do {
            printf("\n%d. Cantitate monede: ", index);
            fgets(buffer, sizeof(buffer), stdin);

            if (strcmp(buffer, "\n") == 0) {
                return list;
            }
            buffer[strcspn(buffer, "\n")] = '\0';
            verificareInput = checkInputData(buffer, "int");

        } while (verificareInput == 0);
        sscanf(buffer, "%d\n", &cantitateMonede);

        do {
            printf("%d. Valoare moneda(RON): ", index);
            fgets(buffer, sizeof(buffer), stdin);

            if (strcmp(buffer, "\n") == 0) {
                printf("\tValoarea monedelor nu poate lipsi !!\n");
                verificareInput = 0;

            } else {
                buffer[strcspn(buffer, "\n")] = '\0';
                verificareInput = checkInputData(buffer, "double");
            }
        } while (verificareInput == 0);
        sscanf(buffer, "%lf\n", &valoareMoneda);

        list = insertSort_ByValue(list, cantitateMonede, valoareMoneda);
        index++;
    }
    return list;
}
//vector de liste List_t X[100];
void free_list(List_t L) {
    if (L == NULL) {
        return;
    }
    free_list(L->next);
    free(L);
}

List_t cumulareMonede_AceeasiValoare(List_t list) {
    if (list == NULL) {
        return NULL;
    }

    List_t copyOfList = NULL;
    double valoareMonedaCurenta = list->valoareMoneda;
    int cantitateMonedaCurenta = list->cantitateMonede;

    for (List_t aux = list->next; aux != NULL; aux = aux->next) {
        if (aux->valoareMoneda == valoareMonedaCurenta) {
            cantitateMonedaCurenta += aux->cantitateMonede;

        } else {
            copyOfList = insertSort_ByValue(copyOfList, cantitateMonedaCurenta, valoareMonedaCurenta); 
            valoareMonedaCurenta = aux->valoareMoneda;
            cantitateMonedaCurenta = aux->cantitateMonede;
        }
    }

    copyOfList = insertSort_ByValue(copyOfList, cantitateMonedaCurenta, valoareMonedaCurenta);
    return copyOfList;
}

int este_solutie(double sumaActuala, double sumaPormoneu) {
    // Verificăm dacă diferența dintre cele două sume este suficient de mică

    double epsilon = 0.001; // ajustam valoarea epsilon în funcție de precizia dorită
    return fabs(sumaActuala - sumaPormoneu) < epsilon;
}

void afiseaza_solutie(List_t currentCombination) {
	List_t temp = currentCombination;
	temp = cumulareMonede_AceeasiValoare(temp);
	printf("Combinatie: ");
	while (temp != NULL) {
		printf("(%d de %g RON) ", temp->cantitateMonede, temp->valoareMoneda); 
		temp = temp->next;
	}
	printf("\n\n"); 

	free_list(temp); //eliberam memoria
	return;
}

void backtracking(List_t head, List_t listaMonedeRamase, double targetSum, List_t currentCombination, double currentSum, int* existaSolutii) {
    // Verificăm dacă am atins suma țintă    
    if (este_solutie(currentSum, targetSum)) {
        *existaSolutii += 1;  //incrementam nr de solutii gasite
		afiseaza_solutie(currentCombination);
    }

	// Explorăm toate posibilitățile cu o lista auxiliara
    List_t temp = head;

    while (temp != NULL) {
        // Verificăm dacă elementul este disponibil pentru a fi adăugat la combinație
        if (currentSum + temp->valoareMoneda <= targetSum && temp->cantitateMonede > 0) {
            // Adăugăm elementul la combinație
            List_t newCombination = make_node(1, temp->valoareMoneda); //adaugam 1 la cantitate pentru a le aduna apoi prin functia "cumulareMonede"
            newCombination->next = currentCombination; 

            // Actualizăm listaMonedeRamase prin scăderea din cantitateMonede
            List_t tempRamase = listaMonedeRamase;
            while (tempRamase != NULL) {
                if (este_solutie(tempRamase->valoareMoneda, temp->valoareMoneda)) {
                    listaMonedeRamase->cantitateMonede = listaMonedeRamase->cantitateMonede - 1;  //actualizam lista monedelor ramase
                    break;
                }
                tempRamase = tempRamase->next;
            }
            // Apelăm recursiv pentru a continua explorarea pe același nivel al listei
            double updateCurrentSum = currentSum + temp->valoareMoneda;
            backtracking(temp, listaMonedeRamase, targetSum, newCombination, updateCurrentSum, existaSolutii);
        }         
        temp = temp->next; 
    }
}

int main(void) {
    double sumaPormoneu;
    printf("Suma din pormoneu(RON): ");
    scanf("%lf", &sumaPormoneu);

    printf("Introduceti numarul si valoarea monedelor: \n");
    List_t lista = populateList();
    lista = cumulareMonede_AceeasiValoare(lista);

    
    double sumaActuala = 0.0;
    // Inițializăm o listă goală pentru a trimite la backtrack

    List_t currentCombination = NULL; //lista in care salvam fiecare solutie gasita iar mai apoi o afisam
    List_t listaMonedeRamase = lista; //avem o lista separata pentru monedele ramase pentru acea combinatie
    int existaSolutii = 0; //folosim pentru a vedea cate solutii exista

    backtracking(lista, listaMonedeRamase, sumaPormoneu, currentCombination, sumaActuala, &existaSolutii);

    if (existaSolutii == 0){ //tiparim un mesaj pentru avertizare
        printf("\tNu exista nicio combinatie pentru monedele introduse !!\n");
    }else { 
        printf("Au fost gasite %d combinatii. \n", existaSolutii);
    }

    free_list(lista);
	free_list(listaMonedeRamase); 
	free_list(currentCombination);
    return 0;
} 