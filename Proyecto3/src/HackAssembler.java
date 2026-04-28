/*********
 * HackAssembler.java – Traductor de assembler Hack a binario (.hack).
 *                      Incluye la extensión del ISA con las instrucciones
 *                      shift left (<<) y shift right (>>).
 *
 *                      Uso:
 *                        java HackAssembler Prog.asm            -> genera Prog.hack
 *                        java HackAssembler -d Prog.hack        -> genera ProgDis.asm
 *
 * Autor 1: Cristian Bolaños 
 * Autor 2: Jeronimo Contreras 
 *********/

import java.io.BufferedReader; // Guardar en memoria partes del archivo
import java.io.BufferedWriter; 
import java.io.FileReader;  // leer el archivo
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HackAssembler {

    // ---------------- Tablas base ----------------

    /** Tablas de códigos binarios para instrucciones C estándar. */
    static final Map<String, String> COMP = new HashMap<>();    // Clave -> valor Implementacion de map hashmap para eficiencia de busqueda de bucket
    static final Map<String, String> DEST = new HashMap<>();
    static final Map<String, String> JUMP = new HashMap<>();

    /** Símbolos predefinidos del Hack. */
    static final Map<String, Integer> PREDEFINED = new HashMap<>();

    static {
        // a=0
        COMP.put("0",   "0101010");
        COMP.put("1",   "0111111");
        COMP.put("-1",  "0111010");
        COMP.put("D",   "0001100");
        COMP.put("A",   "0110000");
        COMP.put("!D",  "0001101");
        COMP.put("!A",  "0110001");
        COMP.put("-D",  "0001111");
        COMP.put("-A",  "0110011");
        COMP.put("D+1", "0011111");
        COMP.put("A+1", "0110111");
        COMP.put("D-1", "0001110");
        COMP.put("A-1", "0110010");
        COMP.put("D+A", "0000010");
        COMP.put("D-A", "0010011");
        COMP.put("A-D", "0000111");
        COMP.put("D&A", "0000000");
        COMP.put("D|A", "0010101");
        // a=1
        COMP.put("M",   "1110000");
        COMP.put("!M",  "1110001");
        COMP.put("-M",  "1110011");
        COMP.put("M+1", "1110111");
        COMP.put("M-1", "1110010");
        COMP.put("D+M", "1000010");
        COMP.put("D-M", "1010011");
        COMP.put("M-D", "1000111");
        COMP.put("D&M", "1000000");
        COMP.put("D|M", "1010101");

        DEST.put("",    "000");
        DEST.put("M",   "001");
        DEST.put("D",   "010");
        DEST.put("MD",  "011");
        DEST.put("A",   "100");
        DEST.put("AM",  "101");
        DEST.put("AD",  "110");
        DEST.put("AMD", "111");

        JUMP.put("",    "000");
        JUMP.put("JGT", "001");
        JUMP.put("JEQ", "010");
        JUMP.put("JGE", "011");
        JUMP.put("JLT", "100");
        JUMP.put("JNE", "101");
        JUMP.put("JLE", "110");
        JUMP.put("JMP", "111");

        PREDEFINED.put("SP",     0);
        PREDEFINED.put("LCL",    1);
        PREDEFINED.put("ARG",    2);
        PREDEFINED.put("THIS",   3);
        PREDEFINED.put("THAT",   4);
        PREDEFINED.put("SCREEN", 16384);
        PREDEFINED.put("KBD",    24576);
        for (int i = 0; i <= 15; i++) PREDEFINED.put("R" + i, i);
    }

    // ---------------- Entrada principal ----------------

    public static void main(String[] args) {
        if (args.length < 1) {                 // Verifica que se ingresen parametro, si el usuario simplemente pone "java -cp out HackAssembler" sacara el mensaje de error y terminara el programa
            System.err.println("Uso: java HackAssembler <archivo.asm> | -d <archivo.hack>");
            System.exit(1);
        }

        try {
            if ("-d".equals(args[0])) {
                if (args.length < 2) {
                    System.err.println("Falta el archivo .hack");
                    System.exit(1);
                }
                HackDisassembler.disassemble(args[1]);
            } else {
                assemble(args[0]);
            }
        } catch (IOException e) {
            System.err.println("Error de E/S: " + e.getMessage());
            System.exit(1);
        }
    }

    // ---------------- Assembler en dos pasadas ----------------

    public static void assemble(String inputPath) throws IOException {
        // Lee y limpia
        List<String> rawLines = readAllLines(inputPath);
        List<String> cleaned = new ArrayList<>();   // una línea por instrucción o etiqueta
        List<Integer> srcLineOf = new ArrayList<>();  // para mensajes de error

        int srcLineNo = 0;
        for (String original : rawLines) {
            srcLineNo++;                            // Cuenta las lineas
            String line = stripCommentsAndSpaces(original); // Le quita comentarios y espacios
            if (line.isEmpty()) continue;           // Se devuelve al for si la cadena despues de quitarle comentarios y espacios quedo vacia y no la agrega a las lineas
            cleaned.add(line);                      // Añade la linea a la lista de cadenas limpias
            srcLineOf.add(srcLineNo);               // Agrega el indice o numero de linea recien procesada a la lista
        }

        // Pasada 1: tabla de símbolos con etiquetas
        SymbolTable st = new SymbolTable();         // Se crea el objeto tabla de simbolos con simbolos predefinidos y metodos que ayudaran
        int romAddr = 0;
        for (int i = 0; i < cleaned.size(); i++) {
            String line = cleaned.get(i);
            if (line.startsWith("(") && line.endsWith(")")) {       // (LOOP) (END)... etc
                String sym = line.substring(1, line.length() - 1).trim();   // se le sacan los parentesis y se eliminan los espacio con trim
                if (sym.isEmpty() || !isValidSymbol(sym)) {                 // Si al sacar parentesis y espacio queda vacio o queda un simbolo invalido es decir que comience con numeros 
                    fail(srcLineOf.get(i), "Etiqueta inválida: " + line);   // Arroja error indicando linea y contenido de la linea y cual es el error
                }
                if (st.contains(sym)) {                                     // Si la tabla contiene el simbolo
                    fail(srcLineOf.get(i), "Etiqueta duplicada: " + sym);   // Arroja error porque no deberia haber duplicidad de etiquetas
                }
                st.addEntry(sym, romAddr);                                  // Si pasa restricciones, se añade simbolo a la tabla y se guarda su direccion en rom
            } else {
                romAddr++;                                                  // Si no es una etiqueta se añaden lineas a la rom normal
            }
        }

        // Pasada 2: traducción
        String outPath = replaceExtension(inputPath, ".hack");
        BufferedWriter out = new BufferedWriter(new FileWriter(outPath));
        try {
            for (int i = 0; i < cleaned.size(); i++) {      
                String line = cleaned.get(i);           //
                int sourceLine = srcLineOf.get(i);
                if (line.startsWith("(")) continue;

                String binary;
                try {
                    if (line.startsWith("@")) {
                        binary = translateA(line.substring(1).trim(), st);
                    } else {
                        binary = translateC(line);
                    }
                } catch (SyntaxException e) {
                    System.err.println("Error en línea " + sourceLine + ": " + e.getMessage());
                    out.close();
                    return;
                }

                out.write(binary);
                out.newLine();
            }
        } finally {
            out.close();
        }
    }

    // ---------------- Traducción A-instruction ----------------

    private static String translateA(String operand, SymbolTable st) throws SyntaxException {
        if (operand.isEmpty()) throw new SyntaxException("A-instruction vacía");
        int value;
        if (Character.isDigit(operand.charAt(0))) {
            try {
                value = Integer.parseInt(operand);
            } catch (NumberFormatException e) {
                throw new SyntaxException("Constante numérica inválida: " + operand);
            }
            if (value < 0 || value > 32767) {
                throw new SyntaxException("Constante fuera de rango [0..32767]: " + operand);
            }
        } else {
            if (!isValidSymbol(operand)) {
                throw new SyntaxException("Símbolo inválido: " + operand);
            }
            if (!st.contains(operand)) {
                st.addVariable(operand);
            }
            value = st.getAddress(operand);
        }
        return "0" + toBinary(value, 15);               // bit mas significativo siempre en 0 el resto el numero convertido a un binario de 15 digitos
    }

    // ---------------- Traducción C-instruction (estándar + shift) ----------------

    private static String translateC(String line) throws SyntaxException {
        // Separa dest, comp, jump
        String dest = "";
        String comp;
        String jump = "";

        String rest = line;
        int eq = rest.indexOf('=');
        if (eq >= 0) {
            dest = rest.substring(0, eq).trim();
            rest = rest.substring(eq + 1).trim();
        }
        int sc = rest.indexOf(';');
        if (sc >= 0) {
            comp = rest.substring(0, sc).trim();
            jump = rest.substring(sc + 1).trim();
        } else {
            comp = rest.trim();
        }

        if (!DEST.containsKey(dest)) {
            throw new SyntaxException("Destino inválido: '" + dest + "'");
        }
        if (!JUMP.containsKey(jump)) {
            throw new SyntaxException("Salto inválido: '" + jump + "'");
        }
        if (comp.isEmpty()) {
            throw new SyntaxException("Falta expresión comp");
        }

        // ---------- ¿es shift? ----------
        // Sintaxis: <operando><<1  ó  <operando>>>1
        String shiftBits = tryShift(comp);
        if (shiftBits != null) {
            // Formato shift: 1 0 1 a c5 c4 c3 c2 c1 c0 d d d j j j
            // shiftBits ya devuelve los 7 bits "a c5 c4 c3 c2 c1 c0"
            return "101" + shiftBits + DEST.get(dest) + JUMP.get(jump);
        }

        // ---------- C estándar ----------
        String compBits = COMP.get(comp);
        if (compBits == null) {
            throw new SyntaxException("Expresión comp desconocida: '" + comp + "'");
        }
        return "111" + compBits + DEST.get(dest) + JUMP.get(jump);
    }

    /**
     * Si 'comp' es una expresión shift ("D<<1", "M>>1", "A<<1", ...) devuelve los 7
     * bits "a c5 c4 c3 c2 c1 c0" correspondientes; si no, devuelve null.
     *
     * Convención:
     *   a  = 0 si el operando es D; 1 si es A o M.
     *   c5 = 0 para <<1, 1 para >>1. Resto de c en 0.
     */
    private static String tryShift(String comp) throws SyntaxException {
        boolean left  = comp.endsWith("<<1");
        boolean right = comp.endsWith(">>1");
        if (!left && !right) return null;

        String operand = comp.substring(0, comp.length() - 3).trim();
        String a;
        switch (operand) {
            case "D": a = "0"; break;
            case "A": a = "1"; break;
            case "M": a = "1"; break;
            default:
                throw new SyntaxException("Operando de shift inválido: '" + operand + "'");
        }
        String c5 = right ? "1" : "0";
        return a + c5 + "00000";
    }

    // ---------------- Utilidades ----------------

    /** Elimina comentarios // y todos los espacios en blanco. */
    static String stripCommentsAndSpaces(String line) {
        int idx = line.indexOf("//");               // Indice de la primera aparicion de //
        if (idx >= 0) line = line.substring(0, idx); // si hay comentarios, es decir si si aparece algun indice para // generar una subcadena que solo abarque desde el 0 hasta donde comienza la cadena
        return line.replaceAll("\\s+", "");     //  \s cualquier espacio en blanco + uno o mas entonces elimina todos los espacios en blanco
    }

    static boolean isValidSymbol(String s) {
        if (s.isEmpty()) return false;
        char first = s.charAt(0);
        if (!(Character.isLetter(first) || first == '_' || first == '.' || first == '$' || first == ':')) {
            return false;
        }
        for (int i = 1; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean ok = Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == '$' || c == ':';
            if (!ok) return false;
        }
        return true;
    }

    static String toBinary(int value, int bits) {
        StringBuilder sb = new StringBuilder();
        for (int i = bits - 1; i >= 0; i--) sb.append((value >> i) & 1);
        return sb.toString();
    }

    static List<String> readAllLines(String path) throws IOException {
        List<String> lines = new ArrayList<>();                 // Lista donde se almacenaran las lineas del archivo
        BufferedReader r = new BufferedReader(new FileReader(path));    
        try {
            String line;
            while ((line = r.readLine()) != null) lines.add(line); // Se verifica que no sea el final del archivo en cada ciclo y se añade la linea del archivo a la lista
        } finally { r.close(); } // libera el búfer en memoria y cierra la conexión con el archivo en disco.
        return lines;   
    }

    static String replaceExtension(String path, String newExt) {
        int dot = path.lastIndexOf('.');
        if (dot < 0) return path + newExt;
        return path.substring(0, dot) + newExt;
    }

    private static void fail(int line, String msg) {            // metodo para errores en lineas, recibe la linea y el mensaje
        System.err.println("Error en línea " + line + ": " + msg); //imprime error en linea y especifica la linea y el mensaje
        System.exit(1);
    }

    // ---------------- Tabla de símbolos ----------------

    static class SymbolTable {
        private final Map<String, Integer> table = new HashMap<>();
        private int nextVar = 16;

        SymbolTable() {
            table.putAll(PREDEFINED);
        }
        boolean contains(String s)         { return table.containsKey(s); }     // Verifica si existe la palabra predefinida
        int getAddress(String s)           { return table.get(s); }             // Duvuelve la posición de memoria de la palabra predefinida
        void addEntry(String s, int addr)  { table.put(s, addr); }              // Si quiere definir la variable con una dirección de memoria especifica
        void addVariable(String s)         { table.put(s, nextVar++); }         // Cuando se declara una variable con nombre no predefinido se asigna autoamticamente en la 17
    }

    // ---------------- Excepción sintáctica ----------------

    static class SyntaxException extends Exception {
        SyntaxException(String m) { super(m); }
    }
}
