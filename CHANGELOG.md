# Changelog
 
Todos los cambios relevantes de este proyecto se documentan en este archivo.
 
## [1.0.0] - 2026-04-28
### Added
- `Shifter.hdl`: circuito de desplazamiento de un bit (left/right) con salida `result`.
- `ALU.hdl`: ALU extendida que integra el Shifter cuando `zx=nx=zy=ny=0, no=1`.
- `Memory.hdl`: memoria del computador Hack (RAM16K + Screen + Keyboard).
- `CPU.hdl`: CPU con decodificación de instrucciones tipo A, C estándar y C-shift.
- `Computer.hdl`: integración ROM32K + CPU + Memory.
- `design.txt`: especificación de la codificación binaria de las instrucciones
  shift (`<<1` y `>>1`) usando el prefijo `101`.
- Archivos `.md5` para cada HDL y para `design.txt`.
### Notes
- Las instrucciones shift se codifican como `101 a c5 c4..c0 ddd jjj`,
  preservando compatibilidad total con el ISA Hack original.
- Todas las pruebas unitarias de Nand2Tetris (Shifter, ALU, Memory, CPU,
  Computer) pasan en la plataforma web.
## [0.1.0] - 2026-03-28
### Added
- Estructura inicial del repositorio: `proyecto2/`, `CONTRIBUTORS.md`,
  `CHANGELOG.md`, `LICENSE`, `README.md`.
 
