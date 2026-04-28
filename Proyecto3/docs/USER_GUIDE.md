# USER GUIDE — HackAssembler

## 1. Requisitos

- JDK 11 o superior (probado con 11, 17 y 21).
- No requiere librerías externas.

## 2. Compilación

### Con javac directo
```bash
mkdir -p out
javac -d out Proyecto3/src/HackAssembler.java Proyecto3/src/HackDisassembler.java
```

### Con Maven (opcional)
Un `pom.xml` mínimo:
```xml
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>eafit.oc</groupId>
  <artifactId>hack-assembler</artifactId>
  <version>1.0</version>
  <build>
    <sourceDirectory>src</sourceDirectory>
    <testSourceDirectory>test</testSourceDirectory>
  </build>
</project>
```
Luego `mvn compile`.

## 3. Ensamblar un `.asm`

```bash
java -cp out HackAssembler Suma.asm
# produce Suma.hack en el mismo directorio
```

### Entrada `Suma.asm`
```
// RAM[2] = RAM[0] + RAM[1]
@0
D=M
@1
D=D+M
@2
M=D
```

### Salida `Suma.hack`
```
0000000000000000
1111110000010000
0000000000000001
1111000010010000
0000000000000010
1110001100001000
```

## 4. Ensamblar con shifts

```
// R2 = R0 << 1
@0
D=M
D=D<<1
@2
M=D
```

## 5. Desensamblar un `.hack`

```bash
java -cp out HackAssembler -d Suma.hack
# produce SumaDis.asm
```

El desensamblador **no recupera** nombres simbólicos ni etiquetas (esa
información se pierde durante el ensamblado), sólo direcciones numéricas.

## 6. Ejecutar los tests

```bash
javac -d out test/HackAssemblerTest.java
java  -cp out HackAssemblerTest
```
Salida esperada (resumen): `Pasaron: N   Fallaron: 0`.

## 7. Mensajes de error

| Mensaje | Causa |
|---|---|
| `Etiqueta inválida: …` | `(XYZ)` contiene caracteres no permitidos. |
| `Etiqueta duplicada: X` | La misma etiqueta aparece dos veces. |
| `Destino inválido: 'XYZ'` | `XYZ=` no está en `{M,D,MD,A,AM,AD,AMD}`. |
| `Salto inválido: 'XYZ'` | `;XYZ` no está en `{JGT,JEQ,JGE,JLT,JNE,JLE,JMP}`. |
| `Expresión comp desconocida: 'XYZ'` | La parte derecha de `=` no coincide con ningún comp ni con un shift. |
| `Operando de shift inválido: 'X'` | `X<<1` o `X>>1` con X distinto de `D`, `A` o `M`. |
| `Constante fuera de rango` | `@n` con `n < 0` o `n > 32767`. |

Al detectar un error, el programa imprime la línea fuente afectada y cierra
el archivo de salida sin borrar lo ya escrito.

## 8. Flujo completo recomendado

1. Escribir `Prog.asm` con instrucciones Hack + shifts.
2. `java HackAssembler Prog.asm` → `Prog.hack`.
3. Cargar `Prog.hack` en `Computer.hdl` (proyecto 2) y ejecutar.
4. (Opcional) `java HackAssembler -d Prog.hack` para inspeccionar la
   decodificación.
