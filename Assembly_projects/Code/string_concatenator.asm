.model small
.stack 100h

.data
    msg_input1 db "INPUT primul sir: $"
    msg_input2 db 0Dh, 0Ah, "INPUT al doilea sir: $"
    msg_output db 0Dh, 0Ah, "OUTPUT sirul concatenat: $"
    buffer1 db 255, ?, 255 dup('$') ; buffer pentru primul sir
    buffer2 db 255, ?, 255 dup('$') ; buffer pentru al doilea sir
    result db 255 dup('$')          ; buffer pentru rezultat

.code
main proc
    ; initializam segmentul de date
    mov ax, @data
    mov ds, ax

    ; facem clear la bufferul pentru rezultat
    lea di, result
    mov cx, 255
clear_result:
    mov byte ptr [di], '$'
    inc di
    loop clear_result

    ; citim primul sir
    lea dx, msg_input1
    mov ah, 09h
    int 21h

    lea dx, buffer1
    mov ah, 0Ah
    int 21h

    ; eliminam caracterul newline din primul sir
    lea si, buffer1 + 2
    mov cl, [buffer1+1]
    lea di, buffer1 + 2
    mov ch, 0

remove_trailing1:
    cmp cl, 0
    je remove_done1
    mov al, [si]
    cmp al, 0Dh
    je remove_done1
    mov [di], al
    inc si
    inc di
    dec cl
    jmp remove_trailing1

remove_done1:
    mov byte ptr [di], '$'

    ; citim al doilea sir
    lea dx, msg_input2
    mov ah, 09h
    int 21h

    lea dx, buffer2
    mov ah, 0Ah
    int 21h

    ; eliminam caracterul newline din al doilea sir
    lea si, buffer2 + 2
    mov cl, [buffer2+1]
    lea di, buffer2 + 2
    mov ch, 0

remove_trailing2:
    cmp cl, 0
    je remove_done2
    mov al, [si]
    cmp al, 0Dh
    je remove_done2
    mov [di], al
    inc si
    inc di
    dec cl
    jmp remove_trailing2

remove_done2:
    mov byte ptr [di], '$'

    ; concatenam cele doua siruri
    lea si, buffer1 + 2
    lea di, result

concatenate_first:
    mov al, [si]
    cmp al, '$'
    je concat_second
    mov [di], al
    inc si
    inc di
    jmp concatenate_first

concat_second:
    lea si, buffer2 + 2

concatenate_second:
    mov al, [si]
    cmp al, '$'
    je end_concat
    mov [di], al
    inc si
    inc di
    jmp concatenate_second

end_concat:
    mov byte ptr [di], '$'

    ; tiparim rezultatul 
    lea dx, msg_output
    mov ah, 09h
    int 21h

    lea dx, result
    mov ah, 09h
    int 21h

    mov ah, 4Ch
    int 21h
main endp

end main