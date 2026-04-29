/*********
 * HackAssemblerTest.java – Pruebas unitarias básicas para HackAssembler
 *                          y HackDisassembler. Se ejecuta sin frameworks
 *                          externos (sólo JDK). Cada test imprime PASS/FAIL.
 *
 *                          Uso:
 *                            javac -d out src/*.java test/HackAssemblerTest.java
 *                            java  -cp out HackAssemblerTest
 *
 * Autor 1: Cristian Bolaños
 * Autor 2: Jeronimo Contreras
 *********/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HackAssemblerTest {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) throws IOException {
        testAInstruction();
        testCInstructionBasic();
        testJump();
        testShiftLeftD();
        testShiftRightM();
        testSymbolsAndLabels();

        System.out.println("----------------------------------");
        System.out.println("Pasaron: " + passed + "   Fallaron: " + failed);
        if (failed > 0) System.exit(1);
    }

    // --------- helpers ---------
    static void check(String name, boolean cond, String detail) {
        if (cond) { System.out.println("[PASS] " + name); passed++; }
        else      { System.out.println("[FAIL] " + name + "  -> " + detail); failed++; }
    }

    static File writeTmp(String name, String content) throws IOException {
        File f = File.createTempFile(name, ""); 
        BufferedWriter w = new BufferedWriter(new FileWriter(f));
        w.write(content);
        w.close();
        return f;
    }

    static List<String> readLines(String path) throws IOException {
        List<String> out = new ArrayList<>();
        BufferedReader r = new BufferedReader(new FileReader(path));
        String line;
        while ((line = r.readLine()) != null) out.add(line);
        r.close();
        return out;
    }

    // --------- tests ---------

    static void testAInstruction() throws IOException {
        File in = writeTmp("pruebitaA", "@5\n@100\n");
        HackAssembler.assemble(in.getAbsolutePath());
        List<String> out = readLines(in.getAbsolutePath().replace(in.getName(), in.getName()).replaceAll("$", "")
                .replaceAll("\\..*$", "") + ".hack");
        // fallback: reconstruir ruta como hace assemble()
        String outPath = HackAssembler.replaceExtension(in.getAbsolutePath(), ".hack");
        out = readLines(outPath);
        check("A-instruction @5",   out.get(0).equals("0000000000000101"), out.get(0));
        check("A-instruction @100", out.get(1).equals("0000000001100100"), out.get(1));
    }

    static void testCInstructionBasic() throws IOException {
        File in = writeTmp("pruebitaC1", "D=A\nM=D+1\n");
        HackAssembler.assemble(in.getAbsolutePath());
        List<String> out = readLines(HackAssembler.replaceExtension(in.getAbsolutePath(), ".hack"));
        // D=A   -> 111 0110000 010 000 = 1110110000010000
        check("D=A",   out.get(0).equals("1110110000010000"), out.get(0));
        // M=D+1 -> 111 0011111 001 000 = 1110011111001000
        check("M=D+1", out.get(1).equals("1110011111001000"), out.get(1));
    }

    static void testJump() throws IOException {
        File in = writeTmp("pruebitaJ", "0;JMP\n");
        HackAssembler.assemble(in.getAbsolutePath());
        List<String> out = readLines(HackAssembler.replaceExtension(in.getAbsolutePath(), ".hack"));
        // 0;JMP -> 111 0101010 000 111
        check("0;JMP", out.get(0).equals("1110101010000111"), out.get(0));
    }

    static void testShiftLeftD() throws IOException {
        File in = writeTmp("pruebitaSL", "D=D<<1\n");
        HackAssembler.assemble(in.getAbsolutePath());
        List<String> out = readLines(HackAssembler.replaceExtension(in.getAbsolutePath(), ".hack"));
        // 101 0 0 00000 010 000
        check("D=D<<1", out.get(0).equals("1010000000010000"), out.get(0));
    }

    static void testShiftRightM() throws IOException {
        File in = writeTmp("pruebitaSR", "AM=D>>1\n");
        HackAssembler.assemble(in.getAbsolutePath());
        List<String> out = readLines(HackAssembler.replaceExtension(in.getAbsolutePath(), ".hack"));
        // 101 0 1 00000 101 000
        check("AM=D>>1", out.get(0).equals("1010100000101000"), out.get(0));
    }

    static void testSymbolsAndLabels() throws IOException {
        String prog = "@i\nM=1\n(LOOP)\n@LOOP\n0;JMP\n";
        File in = writeTmp("sym", prog);
        HackAssembler.assemble(in.getAbsolutePath());
        List<String> out = readLines(HackAssembler.replaceExtension(in.getAbsolutePath(), ".hack"));
        // @i  -> i asignado a 16 -> 0000000000010000
        check("@i en 16",   out.get(0).equals("0000000000010000"), out.get(0));
        // M=1 -> 1110111111001000
        check("M=1",         out.get(1).equals("1110111111001000"), out.get(1));
        // @LOOP apunta a ROM[2] -> 0000000000000010
        check("@LOOP==2",   out.get(2).equals("0000000000000010"), out.get(2));
        // 0;JMP
        check("0;JMP sym",  out.get(3).equals("1110101010000111"), out.get(3));
    }
}

