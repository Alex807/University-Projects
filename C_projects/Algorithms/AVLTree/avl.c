#include <stdio.h>
#include <stdlib.h>

#include "avl.h"

TNodAVL* insertEchilibrat(TCheie x, TNodAVL *p, int *h)
{
    TNodAVL *p1, *p2;

    if(!p) //cheia nu e in arbore; se insereaza
    {
        p = malloc(sizeof(TNodAVL));
        p->cheie = x;
        p->ech = 0; //nodul nou inserat creaza un subarbore unitar, care este mereu echilibrat
        p->stg = p->dr = NULL; //noul nod este frunza
        *h = TRUE;
        return p;
    }

    if (x < p->cheie)
    {
        //daca cheia de inserat < cheia curenta,
        //se insereaza in subarborele stang
        p->stg = insertEchilibrat(x, p->stg, h);
        if (*h == TRUE) //ramura stinga a crescut in inaltime
        {
            switch (p->ech)
            {
                case 1:          //subarborele era dezechilibrat in dreapta
                    p->ech = 0;  //adaugare in subarborele stang => echilibru
                    *h = FALSE;
                    break;
                case 0:          //subarborele era in echilibru
                    p->ech = -1; //dezechilibrat cu un nivel in stanga
                    break;
                case -1: //subarborele era dezechilibrat in stanga cu un nivel,
                         //acum e cu doua nivele => reechilibrare
                    p1 = p->stg;
                    if (p1->ech == -1) //cazul 1 stanga, dezechilibru la fiu in stanga
                    {
                        /*Se efectueaza o rotatie la dreapta a subarborelui p1, astfel:
                           - fiul drept al nodului p1 este "decuplat" de arbore;
                           - nodul p devine fiul drept al nodului p1;
                           - nodul "decuplat" devine fiul stang al nodului p.
                        */
                        p->stg = p1->dr;
                        p1->dr = p;
                        p->ech = 0;
                        p = p1;
                    }
                    else // cazul 2 stanga, dezechilibru la fiu in dreapta
                    {
                        /*Se efectueaza o rotatie la stanga a subarborelui p2, astfel:
                           - fiul stanga al nodului p2 este "decuplat" de arbore;
                           - nodul p2->stg devine fiul drept al nodului p1;
                           - nodul p1 devine fiul stang al nodului "decuplat";
                           - nodul "decuplat" p2 devine fiul stang al nodului p.

                           Se efectueaza o rotatie la dreapta a subarborelui p2, astfel:
                           - fiul stanga al nodului p (p2) este "decuplat" de arbore;
                           - nodul p2->dr devine fiul stang al nodului p;
                           - nodul p devine fiul drept al nodului "decuplat" p2.
                        */
                        p2 = p1->dr;
                        p1->dr = p2->stg;
                        p2->stg = p1;
                        p->stg = p2->dr;
                        p2->dr = p;

                        if (p2->ech == -1)
                            p->ech = +1;
                        else
                            p->ech = 0;

                        if (p2->ech == +1)
                            p1->ech = -1;
                        else
                            p1->ech = 0;

                        p = p2;
                    } // caz 2 stinga
                    p->ech = 0;
                    *h = FALSE; //nu s-a schimbat diferenta de nivel
                    break;
            }// switch
        }
    }
    else
        if (x > p->cheie)
        {
            p->dr = insertEchilibrat(x, (TNodAVL *)p->dr, h);
            if (*h) //ramura dreapta a crescut in inaltime
                switch (p->ech)
                {
                    case -1:        //subarborele era dezechilibrat in stanga
                        p->ech = 0; //adaugare in subarborele drept => echilibru
                        *h = FALSE;
                        break;
                    case 0:          //subarborele era in echilibru
                        p->ech = +1; //dezechilibrat cu un nivel in stanga
                        break;
                    case +1: //subarborele era dezechilibrat in dreapta cu un nivel,
                             //acum e cu doua nivele => reechilibrare
                        p1 = p->dr;
                        if (p1->ech == 1) // cazul 1 dreapta
                        {
                            /*Se efectueaza o rotatie la stanga a subarborelui p1, astfel:
                               - nodul p1->stg este "decuplat" de arbore;
                               - nodul p devine fiul stang al nodului p1;
                               - nodul "decuplat" devine fiul drept al nodului p.
                            */
                            p->dr = p1->stg;
                            p1->stg = p;
                            p->ech = 0;
                            p = p1;
                        }
                        else // cazul 2 dreapta
                        {
                            /*Se efectueaza o rotatie la dreapta a subarborelui p2, astfel:
                               - nodul p2->dr este "decuplat" de arbore;
                               - nodul p1 devine fiul drept al nodului p2;
                               - nodul p2 devine fiul drept al nodului p
                               - nodul "decuplat" devine fiul stang al nodului p1.

                              Se efectueaza o rotatie la stanga a subarborelui p2, astfel:
                               - nodul p2->stg este "decuplat" de arbore;
                               - nodul p devine fiul stang al nodului p2;
                               - nodul "decuplat" devine fiul drept al nodului p.
                            */
                            p2 = p1->stg;
                            p1->stg = p2->dr;
                            p2->dr = p1;
                            p->dr = p2->stg;
                            p2->stg = p;

                            if (p2->ech == 1)
                                p->ech = -1;
                            else
                                p->ech = 0;

                            if (p2->ech == -1)
                                p1->ech = +1;
                            else
                                p1->ech = 0;

                            p = p2;
                        } // caz 2 dreapta
                        p->ech = 0;
                        *h = FALSE;
                } // switch
        }
    return p;
}

TNodAVL* adauga(TNodAVL *r, TCheie k)
{
    int h = FALSE;
    return insertEchilibrat(k, r, &h);
}

TNodAVL* echilibru1(TNodAVL *p, int *h)
{
    TNodAVL *p1, *p2;
    int e1, e2; // din valorile (-1,0,+1);
    // h=adevarat,ramura stinga a devenit mai mica
    switch (p->ech)
    {
        case -1:
            p->ech = 0;
            break;
        case 0:
            p->ech = +1;
            *h = FALSE;
            break;
        case +1: // reechilibrare
            p1 = p->dr;
            e1 = p1->ech;
            if (e1 >= 0)
            {
                // cazul 1 dreapta
                /*Se efectueaza o rotatie la stanga a subarborelui p1, astfel:
                   - fiul stang al nodului p1 este "decuplat" de arbore;
                   - nodul p devine fiul stang al nodului p1;
                   - nodul "decuplat" devine fiul drept al nodului p.
                */
                p->dr = p1->stg;
                p1->stg = p;

                if (e1 == 0) //daca subarborele stang p1 era in echilibru
                {
                    p->ech = +1;
                    p1->ech = -1;
                    *h = FALSE;
                }
                else //daca subarborele stang p1 era in dezechilibru in dreapta cu un nivel
                {
                    p->ech = 0;
                    p1->ech = 0;
                }

                p = p1;
            }
            else
            {
                // cazul 2 dreapta
                /*Se efectueaza o rotatie la stanga a subarborelui p2, astfel:
                   - fiul drept al nodului p2 este "decuplat" de arbore;
                   - nodul p1 devine fiul drept al nodului p2;
                   - nodul "decuplat" devine fiul stang al nodului p1;
                   - nodul p2 devine fiul drept al nodului p.

                  Se efectueaza o rotatie la stanga a subarborelui p2, astfel:
                   - fiul stang al nodului p2 este "decuplat" de arbore;
                   - nodul p devine fiul stang al nodului p2;
                   - nodul "decuplat" devine fiul drept al nodului p.
                */
                p2 = p1->stg;
                e2 = p2->ech;
                p1->stg = p2->dr;
                p2->dr = p1;
                p->dr = p2->stg;
                p2->stg = p;

                if (e2 == +1)
                    p->ech = -1;
                else

                    p->ech = 0;
                if (e2 == -1)
                    p1->ech = +1;
                else
                    p1->ech = 0;

                p = p2;
                p2->ech = 0;
            }
            break;
    } //switch
    return p;
}

TNodAVL* echilibru2(TNodAVL *p, int *h)
{
    TNodAVL *p1, *p2;
    int e1, e2; //(-1,0,+1);
    // h=adevarat,ramura dreapta a devenit mai mica
    switch (p->ech)
    {
        case +1:
            p->ech = 0;
            break;
        case 0:
            p->ech = -1;
            *h = FALSE;
            break;
        case -1: // reechilibrare
            p1 = p->stg;
            e1 = p1->ech;
            if (e1 <= 0)
            {
                // cazul 1 stinga
                /*Se efectueaza o rotatie la dreapta a subarborelui p1, astfel:
                   - fiul drept al nodului p1 este "decuplat" de arbore;
                   - nodul p devine fiul drept al nodului p1;
                   - nodul "decuplat" devine fiul stang al nodului p.
                */
                p->stg = p1->dr;
                p1->dr = p;
                if (e1 == 0)
                {
                    p->ech = -1;
                    p1->ech = +1;
                    *h = FALSE;
                }
                else
                {
                    p->ech  = 0;
                    p1->ech = 0;
                }
                p = p1;
            }
            else
            {
                // cazul 2 stinga
                /*Se efectueaza o rotatie la stanga a subarborelui p2, astfel:
                   - fiul stang al nodului p2 este "decuplat" de arbore;
                   - nodul p1 devine fiul stang al nodului p2;
                   - nodul "decuplat" devine fiul drept al nodului p1;
                   - nodul p2 devine fiul stang al nodului p.

                  Se efectueaza o rotatie la dreapta a subarborelui p2, astfel:
                   - fiul drept al nodului p2 este "decuplat" de arbore;
                   - nodul p devine fiul drept al nodului p2;
                   - nodul "decuplat" devine fiul stang al nodului p.
                */
                p2 = p1->dr;
                e2 = p2->ech;
                p1->dr = p2->stg;
                p2->stg = p1;
                p->stg = p2->dr;
                p2->dr = p;

                if (e2 == -1)
                    p->ech = +1;
                else
                    p->ech = 0;

                if (e2 == +1)
                    p1->ech = -1;
                else
                    p1->ech = 0;

                p = p2;
                p2->ech = 0;
            }
            break;
    } // switch
    return p;
}

TNodAVL* suprima(TNodAVL *r, TNodAVL *q, int *h)
{
    // h=false
    if (r->dr) //daca inca exista un fiu drept al nodului
    {
        r->dr = suprima(r->dr, q, h);
        if (*h)
            r = echilibru2(r, h);
    }
    else // cel mai din dreapta nod din subarborele stang al nodului q
    {
        q->cheie = r->cheie;
        r = r->stg;
        *h = TRUE;
    }
    return r;
}

TNodAVL* suprimaEchilibrat(TCheie x, TNodAVL *p, int *h)
{
    TNodAVL *q;
    // h=fals
    if (!p)
    {
        // cheia nu e IN arbore
        *h = FALSE;
        return NULL;
    }

    if (x < p->cheie)
    {
        p->stg = suprimaEchilibrat(x, p->stg, h);
        if (*h)
            p = echilibru1(p, h); //reechilibrare subarbore stang
    }
    else
        if (x > p->cheie)
        {
            p->dr = suprimaEchilibrat(x, p->dr, h);
            if (*h)
                p = echilibru2(p, h); //reechilibrare subarbore drept
        }
        else  //suprima p
        {
            q = p;
            if (!q->dr) //exista doar fiul stang
            {
                p = q->stg;
                *h = TRUE;
            }
            else
                if (!q->stg) //exista doar fiul drept
                {
                    p = q->dr;
                    *h = TRUE;
                }
                else //daca nodul suprimat are doi fii, acesta se inlocuieste
                     //cu un nod din subarborele stang
                {
                    q->stg = suprima(q->stg, q, h);
                    if (*h)
                        p = echilibru1(p, h);
                }
        }
    return p;
}

TNodAVL* sterge(TNodAVL *r, TCheie k)
{
    int h = FALSE;
    return suprimaEchilibrat(k, r, &h);
}

void inordine(TNodAVL *r)
{
    if(!r)
        return;
    inordine(r->stg);
    printf("%d ", r->cheie);;
    inordine(r->dr);
}

void preordine(TNodAVL *r)
{
    if(!r)
        return;
    printf("%d ", r->cheie);
    preordine(r->stg);
    preordine(r->dr);
}

void postordine(TNodAVL *r)
{
	if(!r)
        return;
    postordine(r->stg);
    postordine(r->dr);
    printf("%d ", r->cheie);
}

int inaltime(TNodAVL *r)
{
    if (!r)
        return 0;
    return (MAX(inaltime(r->stg), inaltime(r->dr))) + 1;
}

void distruge(TNodAVL *r)
{
    if(!r)
        return;
    distruge(r->stg);
    distruge(r->dr);
    free(r);
}

void tipar_arbore(TNodAVL *r, int h)
{
    //arborele este tiparit rotit cu 90 de grade la dreapta
    int i;
    if(!r)
        return;
    tipar_arbore(r->stg, h - 1);
    for (i = 1; i < h; i++)
        printf("%4s", " ");
    printf("%4d\n", r->cheie);
    tipar_arbore(r->dr, h - 1);
}

void tiparArbore(TNodAVL *r)
{
	printf("\n");
	tipar_arbore(r, inaltime(r));
}
