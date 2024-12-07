.model small
.stack 100h

.data
    msg_input db "INPUT sir de caractere: $"
    msg_output db 0Dh, 0Ah, "OUTPUT sirul filtrat este: $"
    filtered db 255 dup(0) 

.code
main proc
    mov ax, @data
    mov ds, ax


    ; afisam mesajul pentru introducerea de input
    lea dx, msg_input
    mov ah, 09h
    int 21h

    ; citim sirul de caractere
    lea di, filtered ; filtram caracterele in acest sir

citire_loop:
    mov ah, 1
    int 21h

    cmp al, 13
    je end_filter

    cmp al, 97
    jl skip_char

    cmp al, 105
    jg skip_char

    mov [di], al
    add di, 1

skip_char:    jmp citire_loop

end_filter:
    lea dx, msg_output ; afisam mesajul pentru output
    mov ah, 09h
    int 21h

    mov byte ptr [di], '$'
    mov ah, 9
    lea dx, filtered
    int 21h

    ; facem intreruperea pentru a termina programul
    mov ax, 4CH
    int 21h
main endp

end main