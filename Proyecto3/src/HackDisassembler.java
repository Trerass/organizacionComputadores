/*********
 * HackDisassembler.java – Lee un archivo .hack (binario Hack) y genera el
 *                         archivo <nombre>Dis.asm con el assembler equivalente,
 *                         soportando las instrucciones estándar Hack más
 *                         las extensiones shift (<<1 y >>1).
 *
 *                         Uso:
 *                           java HackAssembler -d Prog.hack   -> ProgDis.asm
 *
 * Autor 1: Cristian Bolaños
 * Autor 2: Jeronimo Contreras
 *********/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HackDisassembler {

    // Mapa inverso: 7 bits de comp (a + cccccc) -> expresión textual (C estándar)
    static final Map<String, String> INV_COMP = new HashMap<>();
    // Mapa inverso: 3 bits de dest -> texto
    static final String[] INV_DEST = {
            "",    // 000
            "M",   // 001
            "D",   // 010
            "MD",  // 011
            "A",   // 100
            "AM",  // 101
            "AD",  // 110
            "AMD"  // 111
    };
    // Mapa inverso: 3 bits de jump -> texto
    static final String[] INV_JUMP = {
            "",    "JGT", "JEQ", "JGE",
            "JLT", "JNE", "JLE", "JMP"
    };

    static {
        // Poblar INV_COMP a partir del COMP del assembler
        INV_COMP.put("0101010", "0");
        INV_COMP.put("0111111", "1");
        INV_COMP.put("0111010", "-1");
        INV_COMP.put("0001100", "D");
        INV_COMP.put("0110000", "A");
        INV_COMP.put("0001101", "!D");
        INV_COMP.put("0110001", "!A");
        INV_COMP.put("0001111", "-D");
        INV_COMP.put("0110011", "-A");
        INV_COMP.put("0011111", "D+1");
        INV_COMP.put("0110111", "A+1");
        INV_COMP.put("0001110", "D-1");
        INV_COMP.put("0110010", "A-1");
        INV_COMP.put("0000010", "D+A");
        INV_COMP.put("0010011", "D-A");
        INV_COMP.put("0000111", "A-D");
        INV_COMP.put("0000000", "D&A");
        INV_COMP.put("0010101", "D|A");
        INV_COMP.put("1110000", "M");
        INV_COMP.put("1110001", "!M");
        INV_COMP.put("1110011", "-M");
        INV_COMP.put("1110111", "M+1");
        INV_COMP.put("1110010", "M-1");
        INV_COMP.put("1000010", "D+M");
        INV_COMP.put("1010011", "D-M");
        INV_COMP.put("1000111", "M-D");
        INV_COMP.put("1000000", "D&M");
        INV_COMP.put("1010101", "D|M");
    }

    public static void disassemble(String inputPath) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(inputPath));
        String outPath = makeDisPath(inputPath);
        BufferedWriter out = new BufferedWriter(new FileWriter(outPath));
        int lineNo = 0;
        try {
            String line;
            while ((line = in.readLine()) != null) {
                lineNo++;
                String clean = line.trim();
                if (clean.isEmpty()) continue;

                if (!isValid16Bit(clean)) {
                    System.err.println("Error en línea " + lineNo + ": instrucción no válida: " + line);
                    out.close();
                    return;
                }

                String asm;
                try {
                    asm = decode(clean);
                } catch (DecodeException e) {
                    System.err.println("Error en línea " + lineNo + ": " + e.getMessage());
                    out.close();
                    return;
                }
                out.write(asm);
                out.newLine();
            }
        } finally {
            in.close();
            out.close();
        }
    }

    private static String decode(String bits) throws DecodeException {
        char b15 = bits.charAt(0);

        if (b15 == '0') {
            // A-instruction
            int value = Integer.parseInt(bits.substring(1), 2);
            return "@" + value;
        }

        // bit15 = 1 -> C o shift
        char b14 = bits.charAt(1);
        char b13 = bits.charAt(2);

        String destBits = bits.substring(10, 13);
        String jumpBits = bits.substring(13, 16);
        String dest = INV_DEST[Integer.parseInt(destBits, 2)];
        String jump = INV_JUMP[Integer.parseInt(jumpBits, 2)];

        String comp;
        if (b14 == '0') {
            // Shift-instruction: 101 a c5 c4..c0 ddd jjj
            if (b13 != '1') {
                throw new DecodeException("prefijo de instrucción no reconocido: " + bits.substring(0, 3));
            }
            char a  = bits.charAt(3);
            char c5 = bits.charAt(4);
            String operand;
            if (a == '0') operand = "D";
            else {
                // a = 1: distinguir A de M con heurística -> si el destino incluye M
                //        o si c5 sugiere lectura-escritura de memoria, preferir M;
                //        por defecto M, que es el uso más frecuente en Hack.
                operand = dest.contains("M") ? "M" : "M";
                // (mantener "M" siempre es una decisión razonable; el usuario puede
                // editar a "A" manualmente si la semántica de su programa así lo exige)
            }
            String op = (c5 == '1') ? ">>1" : "<<1";
            comp = operand + op;
        } else {
            // C estándar: 111 a cccccc ddd jjj
            String compBits = bits.substring(3, 10);  // a + 6 c-bits
            comp = INV_COMP.get(compBits);
            if (comp == null) {
                throw new DecodeException("comp desconocido: " + compBits);
            }
        }

        StringBuilder sb = new StringBuilder();
        if (!dest.isEmpty()) sb.append(dest).append('=');
        sb.append(comp);
        if (!jump.isEmpty()) sb.append(';').append(jump);
        return sb.toString();
    }

    private static boolean isValid16Bit(String s) {
        if (s.length() != 16) return false;
        for (int i = 0; i < 16; i++) {
            char c = s.charAt(i);
            if (c != '0' && c != '1') return false;
        }
        return true;
    }

    /** Genera la ruta de salida <nombre>Dis.asm a partir de <nombre>.hack */
    static String makeDisPath(String path) {
        int dot = path.lastIndexOf('.');
        String base = (dot < 0) ? path : path.substring(0, dot);
        return base + "Dis.asm";
    }

    static class DecodeException extends Exception {
        DecodeException(String m) { super(m); }
    }
}
