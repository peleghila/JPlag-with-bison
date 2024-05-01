package de.jplag.bison;

import de.jplag.ParsingException;
import de.jplag.Token;
import de.jplag.bison.grammar.BisonLexer;
import de.jplag.bison.grammar.BisonParser;
import de.jplag.bison.grammar.CPP14Lexer;
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
            "%{\n" +
            "  static void print_token (yytoken_kind_t token, YYSTYPE val);\n" +
            "%}\n" +
            "\n" +
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
            "            };\n" +
            "        ;";
    @Test
    void testParseBison() throws IOException, ParsingException {
        //de.jplag.text.ParserAdapter textParser = new de.jplag.text.ParserAdapter();
        BisonParserAdapter parser = new BisonParserAdapter();

        File filePath = File.createTempFile("content","ypp");
        Files.writeString(filePath.toPath(), simple_bison);
        //List<Token> textTokensForPositions = textParser.parse(Set.of(filePath));
        List<Token> tokens = parser.scan(Set.of(filePath));
        System.out.println(tokens);
    }

    @Test
    void compareBisonToText() throws Exception {
        //de.jplag.text.ParserAdapter textParser = new de.jplag.text.ParserAdapter();
        BisonParserAdapter parser = new BisonParserAdapter();

        File filePath = new File("parser.ypp");
        //List<Token> textTokensForPositions = textParser.parse(Set.of(filePath));
        List<Token> tokens = parser.scan(Set.of(filePath));

        System.out.println(tokens);
    }

    @Test
    void testRawString() throws Exception {
        CPP14Lexer lexer = new CPP14Lexer(CharStreams.fromString("R\"\"\"(((\n" +
                "declare i32 @printf(i8*, ...)\n" +
                "declare void @exit(i32)\n" +
                "@.int_specifier = constant [4 x i8] c\"%d\\0A\\00\"\n" +
                "@.str_specifier = constant [4 x i8] c\"%s\\0A\\00\"\n" +
                "\n" +
                "define void @printi(i32) {\n" +
                "    %spec_ptr = getelementptr [4 x i8], [4 x i8]* @.int_specifier, i32 0, i32 0\n" +
                "    call i32 (i8*, ...) @printf(i8* %spec_ptr, i32 %0)\n" +
                "    ret void\n" +
                "}\n" +
                "\n" +
                "define void @print(i8*) {\n" +
                "    %spec_ptr = getelementptr [4 x i8], [4 x i8]* @.str_specifier, i32 0, i32 0\n" +
                "    call i32 (i8*, ...) @printf(i8* %spec_ptr, i8* %0)\n" +
                "    ret void\n" +
                "}\n" +
                ")))\"\"\"" +
                "R\"(x = \"\"\\y\"\")\";"));
        CommonTokenStream src = new CommonTokenStream(lexer);
        src.fill();
        System.out.println(src.getTokens());
    }

}
