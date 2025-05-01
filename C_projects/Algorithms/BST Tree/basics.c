#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "ArboreRegasire.h"
void Initializare(NodArboreDeRegasire *Nod)
{
	//ATENTIE, aceasta functie trebuie apelata cu *Nod deja alocat
	char c;
	for (c = 'A'; c <= '['; c++)
		Nod->alfabet[c - 'A'] = NULL;
}

void Atribuie(NodArboreDeRegasire *Nod, char c, NodArboreDeRegasire *p)
{
	Nod->alfabet[c - 'A'] = p;
}

NodArboreDeRegasire *Valoare(NodArboreDeRegasire *Nod, char c)
{
	return Nod->alfabet[c - 'A'];
}

void NodNou(NodArboreDeRegasire *Nod, char c)
{
	Nod->alfabet[c - 'A'] = (NodArboreDeRegasire *)malloc(sizeof(struct NodArboreDeRegasire));
	Initializare(Nod->alfabet[c - 'A']);
}

void Adauga(char *x, NodArboreDeRegasire *cuvinte)  //x=cuvantul, cuvinte=radacina arborelui de regasire

{

	unsigned i;

	NodArboreDeRegasire *t;

	t = cuvinte;

	for (i = 0; i < strlen(x);i++) 
	{

		if (Valoare(t, x[i]) == NULL) //nodul curent nu are fiu pentru caracterul x[i], deci se creaza unul nou

			NodNou(t, x[i]);

		t = Valoare(t, x[i]); //avansez in arborele de regasire

	}
	Atribuie(t, '[', t); //se face o bucla pentru '[', pentru a marca un nod terminal

}
void Parcurge(NodArboreDeRegasire *Nod, char cuv[], int niv) //nodul curent, un buffer pentru construirea cuvantului, nivelul curent
{ 
	char c;
	for (c = 'A'; c <= 'Z'; c++)		//pentru toate literele considerate
	{
		if (Valoare(Nod, c))			//daca exista legatura in dictionar
		{
			cuv[niv] = c;				//adaug litera gasita in buffer
			Parcurge(Valoare(Nod, c),cuv,niv+1); //avansez 
		}
	}
	if (Valoare(Nod, '['))			//exista terminator=>am gasit un cuvant intreg
	{
		cuv[niv] = '\0';	
		printf("%s\n", cuv);		//prelucrez datele
	}
}

