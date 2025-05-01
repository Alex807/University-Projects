#include <stdlib.h>
#include <stdio.h>
#include <string.h> 
#include <stdbool.h>

#define MAX_OF_NODES 50
#define INFINIT 99999

struct Graph { 
    int countOfNodes; 
    int nodesArray[MAX_OF_NODES];
    int matrix[MAX_OF_NODES][MAX_OF_NODES];
}; 
typedef struct Graph* DirectedGraphType; 

DirectedGraphType createGraph() {
    DirectedGraphType graph = (DirectedGraphType)malloc(sizeof(struct Graph));
    if (graph ==  NULL) { 
        printf("Error at creating graph !\n"); 
        return NULL;
    }
    graph->countOfNodes = 0; //initialize the total number of nodes with 0
    return graph;
}

void initializeMatrixForNode(DirectedGraphType graph, int node) { 
    int size = graph->countOfNodes;
    for (int i = 0; i < size; i++) { 
        graph->matrix[i][node] = 0; 
        graph->matrix[node][i] = 0; 
    }
}

void addNode(DirectedGraphType graph, int node) {
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


void addEdge(DirectedGraphType graph, int srcNode, int destNode, int weight) {
    int size = graph->countOfNodes; 
    if (srcNode >= size || destNode >= size) { 
        printf("Edge has nodes that are NOT in graph ! Edge %d - %d was not added!\n", srcNode, destNode); 
        return;
    } 

    graph->matrix[srcNode][destNode] = weight;
}

void suppressNode(DirectedGraphType graph, int node) { 
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

void suppressEdge(DirectedGraphType graph, int srcNode, int destNode) {
    int size = graph->countOfNodes; 
    if (srcNode >= size || destNode >= size) { 
        printf("Edge has nodes that are NOT in graph ! Edge %d - %d is not existing!\n", srcNode, destNode); 
        return;
    } 
    graph->matrix[srcNode][destNode] = 0;
}

void printGraph(DirectedGraphType graph) {
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

void printRoute(int* route, int size, int totalCost) { 
    for (int i = 0; i < size; i++) { 
        printf ("%d  ", route[i]);
    }
    printf("COST: %d\n", totalCost);
}

void getInvertedRoute(int* t, int startNode, int destNode, int* route, int* size) { 
    int auxiliarNode = destNode; 
    int index = 0;

    while (auxiliarNode != startNode) { 
        route[index] = auxiliarNode; 
        auxiliarNode = t[auxiliarNode]; //mergem la parintele nodului dest, parcurgi invers drumul 
        index++;
    }
    route[index] = startNode; //la final sa fie si oct de start adaugat
    index++;
    *size = index;
}

void reverseRouteArray(int* array, int size) { 
    int aux;
    int middle = size / 2;
    for (int i = 0; i < middle; i++) { 
        aux = array[size - i - 1]; 
        array[size - i - 1] = array[i]; 
        array[i] = aux; 
    }
}

void dijkstra(DirectedGraphType graph, int startNode) { 
    int size = graph->countOfNodes;
    if (startNode >= size) { 
        printf("StartNode for DIJKSTRA function is NOT in graph!\n"); 
        return;
    } 

    int d[size];  //retinem distantele aici
    int t[size];  //retinem nodul anterior parcurs pentru a ajunge aici 
    int visited[size]; //setam daca un nod a fost deja vizitat sau NU

    int externalDegree = 0; //numaram cate muchii ce pleaca din nodul de start avem(pt a nu fi izolat)
    for (int i = 0; i < size; i++) { 

       if (graph->matrix[startNode][i] == 0) {
            d[i] = INFINIT; // Inițializăm cu VALOARE MAX dacă nu este o muchie directă
        } else {
            d[i] = graph->matrix[startNode][i]; // Ponderea pentru muchiile directe
            externalDegree++;
        }

        t[i] = startNode;   
        visited[i] = 0;
    }

    if (externalDegree == 0) { //caz pentru noduri izolate
        printf("StartNode has NO edge to another node !\n"); 
        return;
    }

    d[startNode] = 0; // Distanța până la startNode este 0
    visited[startNode] = 1; //setam ca fiind vizitat 

    for (int pas = 0; pas < size - 1; pas++) { 
        int minPondere = INFINIT; 
        int min_i = -1; 

        for (int i = 0; i < size; i++) { //gasim nodul cu dist MINIMA de pe randul din tabel
            
            if (visited[i] == 0 && d[i] < minPondere) { 
                minPondere = d[i]; 
                min_i = i;
            }
        }
 
        // Dacă nu putem găsi un nod următor, ieșim din buclă
        if (min_i == -1)  break; // Dacă nu mai există noduri de procesat

        visited[min_i] = 1; //actualizam aici ca sa ajungem in acel nod

        for (int i = 0; i < size; i++) { 
            if (graph->matrix[min_i][i] != 0 && visited[i] == 0) { //cautam in toate muchiile sale spre noduri NEVIZITATE
               
                if ( (d[min_i] + graph->matrix[min_i][i]) < d[i]) { //actualizam tabelul daca gasim un drum mai EFICIENT
                    d[i] = d[min_i] + graph->matrix[min_i][i]; 
                    t[i] = min_i;
                }
            }
        }
    }

    int route[size];
    for (int i = 0; i < size; i++) { 
        int routeSize = 0;
        int destNode = i; //aflam traseul pentru fiecare nod

        if (d[destNode] == INFINIT) { 
            printf ("No path from %d to %d !\n", startNode, destNode); 
            continue;
        }
        
        if (destNode == startNode) continue; //ignoram traseul startNode - startNode

        getInvertedRoute(t, startNode, destNode, route, &routeSize);
        reverseRouteArray(route, routeSize);

        int routeCost = d[destNode];
        printRoute(route, routeSize, routeCost);
    }
    
}

void addAdminData (DirectedGraphType graph) { 
    addNode(graph, 0);
    addNode(graph, 1);
    addNode(graph, 2);
    addNode(graph, 3);
    addNode(graph, 4); 
    addNode(graph, 5);
    addNode(graph, 6);
    addNode(graph, 7);
    addNode(graph, 8);

    addEdge(graph, 0, 1, 10);  
    addEdge(graph, 0, 2, 30); 
    addEdge(graph, 0, 3, 5);
    addEdge(graph, 1, 3, 3); 
    addEdge(graph, 3, 2, 5);
    addEdge(graph, 3, 4, 3);  
    addEdge(graph, 3, 5, 5);
    addEdge(graph, 3, 6, 1); 
    addEdge(graph, 3, 8, 3); 
    addEdge(graph, 4, 1, 1);
    addEdge(graph, 4, 6, 3); 
    addEdge(graph, 5, 2, 7);
    addEdge(graph, 5, 7, 2); 
    addEdge(graph, 6, 8, 1); 
    addEdge(graph, 7, 3, 1);
    addEdge(graph, 7, 2, 1); 
    addEdge(graph, 8, 7, 1); 
}

int main(void) { 
    bool value = true;

    DirectedGraphType graph = createGraph(); 
    if (value) { 
        addAdminData(graph);
    } else { 
        //preiei datele de intrare din alta parte
    }

    printGraph(graph); 

    int startNode = 3; 
    dijkstra(graph, startNode);

    free(graph);
    return 0;
}

