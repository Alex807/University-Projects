dosseg
.model small
.stack 100h
.data
    A DB 0           ; Primul numar
    B DB 0           ; Al doilea numar
    OP DB 0          ; Operatorul (1-4 sau 5 pentru exit)
    RESULT DB 0      ; Rezultatul operatiei
    MSG_OP DB "Alegeti operatia (1.Adunare, 2.Scadere, 3.Inmultire, 4.Impartire(DIV), 5.EXIT): $"
    MSG1 DB "Introduceti primul numar (0-9): $"
    MSG2 DB "Introduceti al doilea numar (0-9): $"
    MSG_RESULT DB "Rezultatul este: $"
    DIV_ERROR DB "Eroare: Impartire la zero este ilegala $", 0 
    TOO_BIG_MSG DB "Eroare: Rezultatul DIN PACATE poate aveam MAXIM o cifra $", 0
    GOODBYE_MSG DB "Zii frumoasa, la revedere! $", 0  ; Mesaj de la revedere
    newline DB 13, 10, "$"  ; Cod ASCII pentru newline în DOS

.code
new_line proc
    MOV AH, 9
    MOV DX, OFFSET newline
    INT 21H
    RET
new_line endp

main proc
    MOV AX, @data
    MOV DS, AX

main_loop:
    ; Citirea operatiei
    MOV AH, 9
    MOV DX, OFFSET MSG_OP
    INT 21H
    MOV AH, 1
    INT 21H
    SUB AL, '0'       ; Conversie din ASCII în număr
    MOV OP, AL        ; Stocăm operația (1-5)

    CALL new_line 

    ; Verifică dacă utilizatorul vrea să iasă
    CMP OP, 5
    JE END_PROGRAM    ; Daca OP este 5, terminam execuția

    ; Citirea primului numar
    MOV AH, 9
    MOV DX, OFFSET MSG1
    INT 21H
    MOV AH, 1
    INT 21H
    SUB AL, '0'
    MOV A, AL
    CALL new_line

    ; Citirea celui de-al doilea numar
    MOV AH, 9
    MOV DX, OFFSET MSG2
    INT 21H
    MOV AH, 1
    INT 21H
    SUB AL, '0'
    MOV B, AL
    CALL new_line

    ; Executia operatiei
    MOV AL, A
    MOV BL, B
    
    CMP OP, 1        ; Suma
    JE ADD_OPERATION
    CMP OP, 2        ; Diferenta
    JE SUB_OPERATION
    CMP OP, 3        ; Produs
    JE MUL_OPERATION
    CMP OP, 4        ; Impartire
    JE DIV_OPERATION

END_PROGRAM: ;mutam aici procedura pentru a evita jump-uri prea mari si erorile lor
    ; Afișează mesajul de la revedere
    MOV AH, 9
    MOV DX, OFFSET GOODBYE_MSG
    INT 21H

    ; Termină programul
    MOV AH, 4Ch
    INT 21H

ADD_OPERATION:
    ADD AL, BL
    MOV RESULT, AL
    JMP DISPLAY_RESULT
    JMP main_loop    ; Revenim la buclă dacă operația nu este validă

SUB_OPERATION:
    SUB AL, BL
    MOV RESULT, AL
    JMP DISPLAY_RESULT
    JMP main_loop    ; Revenim la buclă dacă operația nu este validă

MUL_OPERATION:
    MOV AH, 0
    MUL BL
    MOV RESULT, AL
    JMP DISPLAY_RESULT
    JMP main_loop    ; Revenim la buclă dacă operația nu este validă

DIV_OPERATION:
    CMP BL, 0
    JE DISPLAY_DIV_ERROR
    MOV AH, 0
    DIV BL
    MOV RESULT, AL
    JMP DISPLAY_RESULT
    JMP main_loop    ; Revenim la buclă dacă operația nu este validă

DISPLAY_DIV_ERROR:
    MOV AH, 9
    MOV DX, OFFSET DIV_ERROR
    INT 21H
    JMP main_loop   ; Revenim la buclă pentru a continua

DISPLAY_RESULT:
    CMP  RESULT, 9
    JG TOO_BIG
    
    ; Afișează mesajul “Rezultatul este:” 
    MOV AH, 9
    MOV DX, OFFSET MSG_RESULT
    INT 21H

    ; Convertim rezultatul în ASCII și îl afișăm
    ADD RESULT, '0'
    MOV DL, RESULT
    MOV AH, 2
    INT 21H

    CALL new_line  ; Adăugăm o linie nouă
    JMP main_loop  ; Revenim la buclă pentru a continua

TOO_BIG:
    ; Afișăm un mesaj de eroare 
    MOV AH, 9
    MOV DX, OFFSET TOO_BIG_MSG
    INT 21H

    CALL new_line  ; Adăugăm o linie nouă
    JMP main_loop  ; Revenim la buclă pentru a continua

main endp
end main
