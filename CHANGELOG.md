# Changelog
 
Todos los cambios relevantes de este proyecto se documentan en este archivo.
 
## [1.0.0] - 2026-04-29
### Added
- `HackAssembler.java`: traductor de assembler Hack a binario (`.asm` → `.hack`).
  - Pipeline en dos pasadas (tabla de símbolos + traducción).
  - Soporte completo de instrucciones tipo A y tipo C estándar.
  - Soporte de las nuevas instrucciones shift (`<<1`, `>>1`) introducidas
    en el proyecto 2, codificadas con prefijo `101`.
  - Tabla de símbolos con predefinidos Hack (`R0..R15`, `SP`, `LCL`, `ARG`,
    `THIS`, `THAT`, `SCREEN`, `KBD`) y asignación automática de variables
    desde la dirección 16.
  - Manejo de errores con mensaje y número de línea fuente.
- `HackDisassembler.java`: clase complementaria invocada con el flag `-d`
  (`.hack` → `<nombre>Dis.asm`).
  - Decodifica los tres formatos: A-instruction, C estándar, C-shift.
- `HackAssemblerTest.java`: pruebas unitarias sin frameworks externos.
  - Cubre A-instructions, C estándar, saltos, shifts left/right, símbolos
    y etiquetas, ida y vuelta (assembler → disassembler) y errores
    controlados.
- Documentación en `docs/`:
  - `API.md` — referencia de clases y métodos públicos.
  - `DESIGN.md` — diagrama de clases y decisiones de diseño.
  - `USER_GUIDE.md` — instrucciones de compilación, ejecución y mensajes
    de error.
- Casos de uso en `test_cases/`: `Suma`, `MulPor4`, `DivPor2`, `Contador`,
  `ShiftMixto` (cada uno con su `.asm` y su `.hack` de referencia).
- Archivos `.md5` de cada fuente Java.
### Notes
- El disassembler **no** restaura nombres simbólicos ni etiquetas: esa
  información se pierde durante el ensamblado.
- En el modo shift con `a=1`, el disassembler reconstruye `M` por
  convención (es el uso predominante); el estudiante puede editar
  manualmente a `A` si la semántica original lo requería.
## [0.1.0] - 2026-04-15
### Added
- Estructura inicial del repositorio: `proyecto3/HackAssembler/` con
  carpetas `src/`, `test/`, `docs/`, más `CONTRIBUTORS.md`,
  `CHANGELOG.md`, `LICENSE` y `README.md`.

