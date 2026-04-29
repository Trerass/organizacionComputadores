# USER GUIDE - HackAssembler

Esta guia asume que los comandos se ejecutan desde la carpeta raiz:

```powershell
C:\Users\JERONIMO\OrgComp2>
```

Si estas en otra carpeta, primero ejecuta:

```powershell
cd C:\Users\JERONIMO\OrgComp2
```

## 1. Requisitos

- JDK 11 o superior.
- No requiere librerias externas.

Para verificar que Java y `javac` esten instalados:

```powershell
java -version
javac -version
```

## 2. Compilar el proyecto

Desde `C:\Users\JERONIMO\OrgComp2`:

```powershell
javac -d out Proyecto3\src\HackAssembler.java Proyecto3\src\HackDisassembler.java
```

Esto crea o actualiza la carpeta `out` con los archivos `.class`.

## 3. Ensamblar archivos `.asm`

Los casos de prueba estan en:

```text
Proyecto3\test_cases
```

Por eso, al ejecutar el ensamblador debes pasar la ruta completa del archivo `.asm`.

### Ensamblar `Suma.asm`

```powershell
java -cp out HackAssembler Proyecto3\test_cases\Suma.asm
```

Genera:

```text
Proyecto3\test_cases\Suma.hack
```

Para ver el resultado:

```powershell
Get-Content Proyecto3\test_cases\Suma.hack
```

### Ensamblar otros casos

```powershell
java -cp out HackAssembler Proyecto3\test_cases\Contador.asm
java -cp out HackAssembler Proyecto3\test_cases\DivPor2.asm
java -cp out HackAssembler Proyecto3\test_cases\MulPor4.asm
java -cp out HackAssembler Proyecto3\test_cases\ShiftMixto.asm
```

Cada comando genera un `.hack` con el mismo nombre en `Proyecto3\test_cases`.

## 4. Desensamblar archivos `.hack`

Para desensamblar se usa el mismo programa, pero agregando la opcion `-d`.

### Desensamblar `Suma.hack`

```powershell
java -cp out HackAssembler -d Proyecto3\test_cases\Suma.hack
```

Genera:

```text
Proyecto3\test_cases\SumaDis.asm
```

Para ver el resultado:

```powershell
Get-Content Proyecto3\test_cases\SumaDis.asm
```

### Desensamblar otros casos

```powershell
java -cp out HackAssembler -d Proyecto3\test_cases\Contador.hack
java -cp out HackAssembler -d Proyecto3\test_cases\DivPor2.hack
java -cp out HackAssembler -d Proyecto3\test_cases\MulPor4.hack
java -cp out HackAssembler -d Proyecto3\test_cases\ShiftMixto.hack
```

Cada comando genera un archivo terminado en `Dis.asm`, por ejemplo:

```text
MulPor4Dis.asm
ShiftMixtoDis.asm
```

El desensamblador no recupera nombres simbolicos ni etiquetas, porque esa informacion se pierde al ensamblar. Por eso puede mostrar direcciones numericas.

## 5. Ejecutar pruebas

Primero compila el codigo fuente y el archivo de pruebas:

```powershell
javac -d out Proyecto3\src\HackAssembler.java Proyecto3\src\HackDisassembler.java Proyecto3\test\HackAssemblerTest.java
```

Luego ejecuta las pruebas:

```powershell
java -cp out HackAssemblerTest
```

La salida esperada termina con algo parecido a:

```text
Pasaron: N   Fallaron: 0
```

## 6. Flujo completo recomendado

Ejemplo completo con `Suma.asm`:

```powershell
cd C:\Users\JERONIMO\OrgComp2
javac -d out Proyecto3\src\HackAssembler.java Proyecto3\src\HackDisassembler.java
java -cp out HackAssembler Proyecto3\test_cases\Suma.asm
Get-Content Proyecto3\test_cases\Suma.hack
java -cp out HackAssembler -d Proyecto3\test_cases\Suma.hack
Get-Content Proyecto3\test_cases\SumaDis.asm
```

## 7. Mensajes de error comunes

| Mensaje | Causa |
|---|---|
| `El sistema no puede encontrar el archivo especificado` | La ruta del archivo `.asm` o `.hack` no existe desde la carpeta actual. Usa rutas como `Proyecto3\test_cases\Suma.asm`. |
| `Etiqueta invalida` | Una etiqueta contiene caracteres no permitidos. |
| `Etiqueta duplicada` | La misma etiqueta aparece mas de una vez. |
| `Destino invalido` | La parte izquierda de `=` no es valida. |
| `Salto invalido` | La parte despues de `;` no es un salto valido. |
| `Expresion comp desconocida` | La parte de computo no coincide con una instruccion Hack valida. |
| `Operando de shift invalido` | El shift usa un operando distinto de `D`, `A` o `M`. |
| `Constante fuera de rango` | Se uso `@n` con `n < 0` o `n > 32767`. |
