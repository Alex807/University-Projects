#ifndef AVL_H
#define AVL_H

#define MAX(a,b) (a > b)?a:b

typedef int TCheie;
typedef enum {FALSE=0, TRUE=1} BOOL;

typedef struct TNodAVL {
    TCheie cheie;
    struct TNodAVL *stg, *dr;
    int ech; /* un nod al unui arbore AVL econtine in plus
                fata de un nod dintr-un arbore ABO diferenta de
                inaltime dintre subarborii sai*/
} TNodAVL;

TNodAVL* insertEchilibrat(TCheie x, TNodAVL *p, int *h);
TNodAVL* adauga(TNodAVL *r, TCheie k);
TNodAVL* echilibru1(TNodAVL *p, int *h);
TNodAVL* echilibru2(TNodAVL *p, int *h);
TNodAVL* suprima(TNodAVL *r, TNodAVL *q, int *h);
TNodAVL* suprimaEchilibrat(TCheie x, TNodAVL *p, int *h);
TNodAVL* sterge(TNodAVL *r, TCheie k);
void inordine(TNodAVL *r);
void preordine(TNodAVL *r);
void postordine(TNodAVL *r);
int inaltime(TNodAVL *r);
void distruge(TNodAVL *r);
void tipar_arbore(TNodAVL *r, int h);
void tiparArbore(TNodAVL *r);

#endif // AVL_H
