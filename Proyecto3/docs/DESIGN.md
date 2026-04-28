# DESIGN — HackAssembler / HackDisassembler

## Diagrama de clases (simplificado)

```
          +------------------------+
          |   HackAssembler        |
          +------------------------+
          | + main(args)           |
          | + assemble(path)       |
          | - translateA(...)      |
          | - translateC(...)      |
          | - tryShift(comp)       |
          | - stripCommentsAndSpaces|
          | - isValidSymbol(s)     |
          | - toBinary(v,bits)     |
          | - replaceExtension(..) |
          +-----------+------------+
                      | usa
                      v
          +------------------------+
          |  SymbolTable           |
          +------------------------+
          | - table:Map<S,Integer> |
          | - nextVar:int (=16)    |
          | + contains / getAddr   |
          | + addEntry / addVariable|
          +------------------------+

          +------------------------+
          |  HackDisassembler      |
          +------------------------+
          | + disassemble(path)    |
          | - decode(bits)         |
          | - isValid16Bit(s)      |
          | - makeDisPath(path)    |
          +------------------------+

          +------------------------+
          |  SyntaxException       |   (estática, interna a HackAssembler)
          +------------------------+
          +------------------------+
          |  DecodeException       |   (estática, interna a HackDisassembler)
          +------------------------+
```

## Separación de responsabilidades

- **HackAssembler**: pipeline en dos pasadas.
  - *Pasada 1* (construcción de la tabla de símbolos):
    limpia líneas (comentarios y espacios), registra etiquetas `(XXX)` con su
    dirección de ROM.
  - *Pasada 2* (traducción):
    recorre cada línea activa y emite A-instruction o C-instruction binaria.
    La selección entre C estándar y shift la realiza `tryShift(comp)`.

- **SymbolTable**: encapsula el mapa de símbolos con los predefinidos del Hack
  (R0..R15, SP, LCL, ARG, THIS, THAT, SCREEN, KBD). Las variables nuevas se
  asignan a partir de la dirección 16.

- **HackDisassembler**: decodificador puro. Cada línea de 16 bits se traduce
  mirando primero `bit15`, luego `bit14` para distinguir C estándar de shift.

## Decisiones clave

- El shift se reconoce **suprimiéndolo** del mapa `COMP` del assembler y
  atendiéndolo por una ruta separada (`tryShift`). Así la tabla COMP sigue
  siendo exactamente la del Hack canónico.
- El prefijo binario del shift es `101` (bit15=1, bit14=0, bit13=1). Esto
  garantiza compatibilidad total con todo programa Hack previo.
- Los errores de sintaxis **no matan al proceso con excepción sin control**:
  se imprimen con el número de línea fuente y se cierra el archivo de salida.

## Extensibilidad

- Añadir nuevas operaciones shift (p.ej. `<<2`, rotaciones) sólo requiere
  tocar `tryShift` y el mapeo inverso en `HackDisassembler`.
- Añadir soporte a Maven es trivial: el código no tiene dependencias externas.
