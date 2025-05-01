#include <stdio.h>


#include "avl.h"

int main()
{
    int i, n = 7;

    TNodAVL *radacina;

    radacina = NULL; //initializare arbore
    for(i = 1; i <= n; i++)
        radacina = adauga(radacina, i);
    tiparArbore(radacina);

    return 0;
}
