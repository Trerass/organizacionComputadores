// Caso 4: Contador descendente con etiqueta
//         Empieza en RAM[0] y decrementa hasta 0
(LOOP)
   @0
   D=M
   @END
   D;JEQ
   @0
   M=M-1
   @LOOP
   0;JMP
(END)
   @END
   0;JMP
