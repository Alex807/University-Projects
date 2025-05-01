#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#define MAX_OF_NODES 50
#define INFINIT 99999

struct Graph { 
    int countOfNodes; 
    int nodesArray[MAX_OF_NODES];
    int matrix[MAX_OF_NODES][MAX_OF_NODES];
}; 
typedef struct Graph* UndirectedGraphType; 

UndirectedGraphType createGraph() {
    UndirectedGraphType graph = (UndirectedGraphType)malloc(sizeof(struct Graph));
    if (graph ==  NULL) { 
        printf("Error at creating graph !\n"); 
        return NULL;
    }
    graph->countOfNodes = 0; //initialize the total number of nodes with 0
    return graph;
}

void initializeMatrixForNode(UndirectedGraphType graph, int node) { 
    int size = graph->countOfNodes;
    for (int i = 0; i < size; i++) { 
        graph->matrix[i][node] = 0; 
        graph->matrix[node][i] = 0; 
    }
}

void addNode(UndirectedGraphType graph, int node) {
    int size = graph->countOfNodes;
    if (node != size) { 
        printf("Add nodes starting from 0 in ascending way ONLY! Node %d NOT added! \n", node); 
        return;
    }

    graph->nodesArray[node] = node;
    graph->countOfNodes++;  // Increment the number of nodes

    // Update the adjacency matrix for the new node
    initializeMatrixForNode(graph, size);
}


void addEdge(UndirectedGraphType graph, int srcNode, int destNode, int weight) {
    int size = graph->countOfNodes; 
    if (srcNode >= size || destNode >= size) { 
        printf("Edge has nodes that are NOT in graph ! Edge %d - %d was not added!\n", srcNode, destNode); 
        return;
    } 

    graph->matrix[srcNode][destNode] = weight;
    graph->matrix[destNode][srcNode] = weight;
}

void suppressNode(UndirectedGraphType graph, int node) { 
    int size = graph->countOfNodes;  
    if (node >= size) { 
        printf("Node %d is NOT in graph!\n", node);
        return;
    }
    
    int* array = graph->nodesArray;
    int lastNode = size - 1; //schimbam cu ultimul nod pentru a fi mai eficient
    array[node] = array[lastNode];

    //nodul indicat este inlocuit cu ultimul nod
    for(int i = 0; i < size; i++){
        graph->matrix[i][node] = graph->matrix[i][lastNode];
        graph->matrix[node][i] = graph->matrix[lastNode][i];
    }
    // Decrement the count of nodes
    graph->countOfNodes--;
}

void suppressEdge(UndirectedGraphType graph, int srcNode, int destNode) {
    int size = graph->countOfNodes; 
    if (srcNode >= size || destNode >= size) { 
        printf("Edge has nodes that are NOT in graph ! Edge %d - %d is not existing!\n", srcNode, destNode); 
        return;
    } 
    graph->matrix[srcNode][destNode] = 0;
    graph->matrix[destNode][srcNode] = 0;
}

void printGraph(UndirectedGraphType graph) {
    int matrix_size = graph->countOfNodes; 
    if (matrix_size == 0) { 
        printf("Graph is EMPTY !!\n\n");
        return;
    }

    int* nodeArray = graph->nodesArray;
    for (int i=0; i < matrix_size; i++) { //print nodes value for best reading matrix '(i)'
        if (i == 0) { 
            printf("\n\t\t\t  (%2d)     ", nodeArray[i]); //printing the first line of nodes value
        
        } else if (nodeArray[i] >= 9) { 
            printf("(%2d)    ", nodeArray[i]); //lines with 2 digits nodes
        } else {
            printf("(%2d)    ", nodeArray[i]); //lines with 1 digit nodes
        }
    }
    printf("\n");

    for(int i=0; i<matrix_size; i++)
    { 
        for(int j=0; j<matrix_size; j++)
        { 
            if(i == matrix_size/2 && j==0)
            {
                printf("Adiency_Matrix =  (%2d)    %2d   ", nodeArray[i], graph->matrix[i][j]); //printing the middle line of the matrix with "Matrix_Name"(only once happens) 
            }
            else if(j == 0)
            { 
                printf("\t\t  (%2d)    %2d   ", nodeArray[i], graph->matrix[i][j]); //printing only the first element of each line
            }
            else 
            { 
                printf("   %2d   ", graph->matrix[i][j]);  //rest of the line withount first element
            }
        }
        printf("\n\n");
    }
}

void printMST(UndirectedGraphType graph, int parent[], int startNode) {
    int size = graph->countOfNodes;
    printf("Edge \tWeight\n");
    for (int i = 0; i < size; i++) { // Începem de la 1 deoarece 0 este sursa și nu are părinte
        if (i == startNode) continue;

        printf("%d - %d \t%d \n", i, parent[i], graph->matrix[i][parent[i]]);
    }
}

void primMST (UndirectedGraphType graph, int startNode) {  
    int size = graph->countOfNodes; //dimensiunea grafului 
    int parent[size]; //retinem traseul pnetru fiecare nod
    int vizitat[size]; //vector in care retinem daca un nod a fost vizitat sau nu   0-NEVIZITAT 1-VIZITAT 
    int minPondere, min_i, min_j; //retinem ponderea minima a muchiei si nodurile care o formeaza 

    for (int i = 0; i < size; i++) { 
        vizitat[i] = 0; //initializam toate nodurile ca fiind nevizitate
        parent[i] = -1;
    }
    vizitat[startNode] = 1; //marcam nodul de start ca vizitat 0-NEVIZITAT 1-VIZITAT 

    int costTotal = 0; //retinem costul total al arborelui de acoperire minima 
    for (int pas = 0; pas < size - 1; pas++) {  
        minPondere = INFINIT; //initializam ponderea minima cu o valoare foarte mare
        min_i = -1; //initializam nodurile cu -1 pentru a fi sigur ca nu exista noduri gasite momentan
        min_j = -1; 

        for (int i = 0; i < size; i++) { 

            for (int j = 0; j < size; j++) {  

                if (graph->matrix[i][j] != 0 && vizitat[i] == 1 && vizitat[j] == 0) { //daca exista muchia i-j si nodul j nu a fost vizitat, DAR ne aflam in nodul i care a fost vizitat
                  
                    if (graph->matrix[i][j] < minPondere) { 
                        minPondere = graph->matrix[i][j]; //retinem ponderea minima a muchiei  
                        min_i = i; //retinem nodul i 
                        min_j = j; //retinem nodul j 
                    }
                    
                }
            }                    
        }
        if (min_i != -1 && min_j != -1) { //am gasit o muchie valida pt arborele de acop min
            costTotal += minPondere; // Adaugă ponderea minimă la costul total 

            vizitat[min_j] = 1; // Marchez nodul destinație ca vizitat
            parent[min_j] = min_i;
        }

    } //avem complexitatea functiei de O(n^3)
    printMST(graph, parent, startNode);
    printf("TOTAL COST: %d\n", costTotal); //afisam costul total al arborelui de acoperire minima   
}

int main(void) { 
    UndirectedGraphType graph = createGraph(); 
    addNode(graph, 0);
    addNode(graph, 1);
    addNode(graph, 2);
    addNode(graph, 3);
    addNode(graph, 4); 
    addNode(graph, 5);
    addNode(graph, 6);
    addNode(graph, 7);

    addEdge(graph, 0, 2, 1);  
    addEdge(graph, 0, 4, 6); 
    addEdge(graph, 1, 6, 1);
    addEdge(graph, 2, 4, 3); 
    addEdge(graph, 2, 5, 1);  
    addEdge(graph, 2, 6, 9);
    addEdge(graph, 3, 5, 3); 
    addEdge(graph, 3, 7, 9); 
    addEdge(graph, 5, 7, 9);

    printGraph(graph); 

    primMST(graph, 0);

    free(graph);
    return 0;
}

