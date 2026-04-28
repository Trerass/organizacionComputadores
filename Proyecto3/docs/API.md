# API — HackAssembler / HackDisassembler

## `HackAssembler`

### `static void main(String[] args)`
Punto de entrada. Dos formas de uso:
- `java HackAssembler <archivo.asm>` → ensambla y genera `<archivo>.hack`.
- `java HackAssembler -d <archivo.hack>` → desensambla y genera `<archivo>Dis.asm`.

### `static void assemble(String inputPath)`
Ensambla el `.asm` de la ruta indicada en dos pasadas:
1. Construye la tabla de símbolos con las etiquetas `(...)`.
2. Traduce cada instrucción a su cadena de 16 bits.
Si detecta un error, imprime en `stderr` la línea fuente afectada y detiene el
proceso cerrando el archivo de salida.

### `static String replaceExtension(String path, String newExt)`
Reemplaza la extensión del path por `newExt`. Utilizada para derivar
`.hack` desde `.asm`.

### `SymbolTable` (clase interna estática)
| Método | Descripción |
|---|---|
| `boolean contains(String s)` | ¿Existe el símbolo? |
| `int getAddress(String s)` | Dirección asociada al símbolo. |
| `void addEntry(String s, int addr)` | Añade una etiqueta con su ROM. |
| `void addVariable(String s)` | Añade una variable; asigna siguiente RAM libre. |

### `SyntaxException` (clase interna estática)
Se lanza desde `translateA` / `translateC` / `tryShift` cuando una línea no
se puede traducir. Es capturada por `assemble` para reportar la línea fuente.

## `HackDisassembler`

### `static void disassemble(String inputPath)`
Lee un `.hack`, valida que cada línea sea 16 caracteres `0|1` y emite
`<nombre>Dis.asm`. Maneja tres formatos:
- `0...` → `@<valor>`
- `111…` → C estándar (consulta `INV_COMP`).
- `101…` → shift (decodifica `a` y `c5` para reconstruir `<op><<1|>>1`).

### `static String makeDisPath(String path)`
Deriva `ProgDis.asm` a partir de `Prog.hack`.

### `DecodeException` (clase interna estática)
Se lanza cuando una línea de 16 bits no es decodificable. Gestionada por
`disassemble` con el mismo patrón de "reporte + cierre".

## Tablas

| Mapa | Contenido |
|---|---|
| `HackAssembler.COMP` | 28 entradas estándar Hack (`0`, `1`, …, `D|M`). |
| `HackAssembler.DEST` | 8 combinaciones (`""` → `AMD`). |
| `HackAssembler.JUMP` | 8 combinaciones (`""` → `JMP`). |
| `HackAssembler.PREDEFINED` | 23 símbolos (`R0..R15`, `SP`, `LCL`, `ARG`, `THIS`, `THAT`, `SCREEN`, `KBD`). |
| `HackDisassembler.INV_COMP` | inverso de `COMP` (28 entradas). |
| `HackDisassembler.INV_DEST` / `INV_JUMP` | arrays de 8 strings. |
