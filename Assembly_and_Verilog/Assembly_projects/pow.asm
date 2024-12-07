.model small
.stack 100h

.data
    base dw 3           ; baza (16 biti)
    exponent dw 4       ; exponentul (16 biti)
    result dw 1         ; initializam rezultatul cu 1
    msg_result db "Rezultatul este: $"

.code
main proc
    mov AX, @data       ; initializare segment de date
    mov ds, AX

    mov AX, 1           
    mov CX, exponent    ; mutam exponentul in CX

calc_power:
    cmp CX, 0           ; verificam daca CX este 0
    je display_result   ; daca da, afisam rezultatul
    mov BX, base        ; BX = baza (16 biți)
    mul BX              ; inmultim AX cu BX
    loop calc_power     ; decrementam CX si continuam bucla

display_result:
    mov result, AX ; salvam rezultatul 

    lea dx, msg_result ; afisam mesajul
    mov ah, 09h
    int 21h

    mov AX, result      ; mutam rezultatul in AX
    call print_number

    mov AX, 4C00h ; intrerupere pentru a termina programul
    int 21h

main endp

; procedurile de afisare a numarului
print_number proc
    cmp AX, 0
    jne convert_to_text

    mov dl, '0'
    mov ah, 02h
    int 21h
    ret

convert_to_text:
    xor CX, CX
    mov BX, 10

convert_loop:
    xor dx, dx
    div BX              ; AX = AX / BX, DX = rest 
    push dx             ; stocam restul in stiva
    inc CX              ; incrementam CX
    test AX, AX         ; verificam daca mai este ceva de convertit
    jnz convert_loop

print_digits:
    pop dx              
    add dl, '0'         
    mov ah, 02h         
    int 21h             
    loop print_digits   
    ret
print_number endp

end main