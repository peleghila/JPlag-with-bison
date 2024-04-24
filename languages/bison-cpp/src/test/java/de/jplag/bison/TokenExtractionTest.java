package de.jplag.bison;

import de.jplag.ParsingException;
import de.jplag.Token;
import de.jplag.bison.grammar.BisonLexer;
import de.jplag.bison.grammar.BisonParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TokenExtractionTest {
    String simple_bison = "%{\n" +
            "  #define _GNU_SOURCE\n" +
            "  #include <stdio.h>\n" +
            "  #include \"ptypes.h\"\n" +
            "%}\n" +
            "%start program\n" +
            "\n" +
            "%%\n" +
            "\n" +
            "program :   {\n" +
            "                cout << \"*** RUN ***\" << endl;\n" +
            "                cout << \"Type function with list of parmeters. Parameter list can be empty\" << endl\n" +
            "                     << \"or contain positive integers only. Examples: \" << endl\n" +
            "                     << \" * function()\" << endl\n" +
            "                     << \" * function(1,2,3)\" << endl\n" +
            "                     << \"Terminate listing with ; to see parsed AST\" << endl\n" +
            "                     << \"Terminate parser with Ctrl-D\" << endl;\n" +
            "                \n" +
            "                cout << endl << \"prompt> \";\n" +
            "                \n" +
            "                driver.clear();\n" +
            "            }\n" +
            "        | program command\n" +
            "            {\n" +
            "                const Command &cmd = $2;\n" +
            "                cout << \"command parsed, updating AST\" << endl;\n" +
            "                driver.addCommand(cmd);\n" +
            "                cout << endl << \"prompt> \";\n" +
            "            }\n" +
            "        | program SEMICOLON\n" +
            "            {\n" +
            "                cout << \"*** STOP RUN ***\" << endl;\n" +
            "                cout << driver.str() << endl;\n" +
            "            }\n" +
            "        ;";
    @Test
    void testParseBison() throws IOException, ParsingException {
        BisonParserAdapter parser = new BisonParserAdapter();

        File filePath = File.createTempFile("content","ypp");
        Files.writeString(filePath.toPath(), simple_bison);
        List<Token> tokens = parser.scan(Set.of(filePath));
        System.out.println(tokens);
    }
}
