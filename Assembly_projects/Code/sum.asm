dosseg
.model small ; prin această instrucțiune se selectează modelul de memorie folosit
.stack 100h ;declararea segmentului stivă
.data ; reprezintă secțiunea de declarare a datelor
 A DB 0
 B DB 0
 C DB 0
 X DW 7
 n1 DB "Introduceti primul numar:$" 
 n2 DB "Introduceti al doilea numar:$"
 n3 DB "Introduceti al treilea numar:$"
 result DB "Rezultatul este:$"
.code ; reprezintă secțiunea în care se scrie codul sursă al programului
new_line proc
 MOV AH, 2
 MOV DL, 10 ; valoarea hexa 0x0A (în zecimal 10) corespunde codului ASCII al
;caracterului special LF – line feed, corespunzător lui ‘\n’ (new line character) din C
 INT 21H
 ret
new_line endp
main proc ; proc este cuvântul cheie pentru începerea unei proceduri
 MOV AX, @data ; instrucțiuni implicite pentru încărcarea segmentului de date
 MOV DS, AX
 ; citirea primului număr de la tastatură
 MOV AH, 9
 MOV DX, OFFSET n1
 INT 21h
 MOV AH, 1 ; serviciu de citire asociat întreruperii INT 21H – Keyboard input with echo
 INT 21H
 MOV A, AL
 CALL new_line
; citirea celui de-al doilea număr de la tastatură
 MOV AH, 9
 MOV DX, OFFSET n2
 INT 21h
 MOV AH, 1
 INT 21H
 MOV B, AL
 CALL new_line
; citirea celui de-al treilea număr de la tastatură
 MOV AH, 9
 MOV DX, OFFSET n3
 INT 21h
 MOV AH, 1
 INT 21H
 MOV C, AL
 CALL new_line
; afisarea liniei “Rezultatul este:” pe ecran
 MOV AH, 9
 MOV DX, OFFSET result
 INT 21h
; calculul sumei A + B
 MOV AL, A ; primul număr A este mutat în registrul AL pe 8 biți
 ADD AL, B ; rezultatul sumei A+B se află în registrul AL pe 8 biți
; scăderea lui C din rezultat
 SUB AL, C
; afișarea rezultatului pe ecran
 MOV AH, 2 ; serviciu de afișare asociat întreruperii INT 21H – Display output
 MOV DL, AL ; rezultatul este copiat în registrul DL
 INT 21h
 MOV AH, 4Ch
 INT 21h
main endp ; endp este cuvântul cheie care semnifică finalul unei proceduri
end main