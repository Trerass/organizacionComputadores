// Caso 5: Mezcla de shifts sobre M y A
//         RAM[3] = (RAM[3] << 1) y luego A = A >> 1 (inofensivo)
@3
M=M<<1
D=A
A=D>>1
@0
M=D
