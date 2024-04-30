package de.jplag.bison;

import de.jplag.ParsingException;
import de.jplag.Token;
import de.jplag.TokenType;
import de.jplag.bison.grammar.CPP14Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static de.jplag.bison.CPPTokenType.*;

public class CPP14Scanner {
    public interface TokenAdder {
        void addToken(TokenType type, int column, int line, int length);
    }
    public static void scanFile(File file, TokenAdder adder) throws ParsingException {
        try(InputStream input = new FileInputStream(file)) {
            scanIS(CharStreams.fromStream(input),adder);
        } catch (IOException e) {
            System.out.println("C/C++ Scanner: File " + file.getName() + " not found.");
            throw new ParsingException(file, e.getMessage(), e);
        }

    }

    public static void scanIS(CharStream input, TokenAdder adder)  {
        //CPPScanner scanner = new CPPScanner(input, "UTF-8");

        CPP14Lexer lexer = new CPP14Lexer(input);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        tokenStream.fill();
        for(org.antlr.v4.runtime.Token token: tokenStream.getTokens()) {
            TokenType tt = switch (token.getType()) {
                case CPP14Lexer.LeftBrace -> C_BLOCK_BEGIN;
                case CPP14Lexer.RightBrace -> C_BLOCK_END;
                case CPP14Lexer.Question -> C_QUESTIONMARK;
                case CPP14Lexer.Ellipsis -> C_ELLIPSIS;
                case CPP14Lexer.Assign,
                    CPP14Lexer.StarAssign,
                    CPP14Lexer.DivAssign,
                    CPP14Lexer.ModAssign,
                    CPP14Lexer.PlusAssign,
                    CPP14Lexer.MinusAssign,
                    CPP14Lexer.LeftShiftAssign,
                    CPP14Lexer.RightShiftAssign,
                    CPP14Lexer.AndAssign,
                    CPP14Lexer.XorAssign,
                    CPP14Lexer.OrAssign,
                    CPP14Lexer.PlusPlus,
                    CPP14Lexer.MinusMinus-> C_ASSIGN;
                case CPP14Lexer.Auto -> C_AUTO;
                case CPP14Lexer.Break -> C_BREAK;
                case CPP14Lexer.Case -> C_CASE;
                case CPP14Lexer.Catch -> C_CATCH;
                case CPP14Lexer.Char,
                    CPP14Lexer.Char16,
                    CPP14Lexer.Char32-> C_CHAR;
                case CPP14Lexer.Const -> C_CONST;
                case CPP14Lexer.Continue -> C_CONTINUE;
                case CPP14Lexer.Default -> C_DEFAULT;
                case CPP14Lexer.Delete -> C_DELETE;
                case CPP14Lexer.Do -> C_DO;
                case CPP14Lexer.Double -> C_DOUBLE;
                case CPP14Lexer.Else -> C_ELSE;
                case CPP14Lexer.Enum -> C_ENUM;
                case CPP14Lexer.Extern -> C_EXTERN;
                case CPP14Lexer.Float -> C_FLOAT;
                case CPP14Lexer.For -> C_FOR;
                case CPP14Lexer.Friend -> C_FRIEND;
                case CPP14Lexer.Goto -> C_GOTO;
                case CPP14Lexer.If -> C_IF;
                case CPP14Lexer.Inline -> C_INLINE;
                case CPP14Lexer.Int -> C_INT;
                case CPP14Lexer.Long -> C_LONG;
                case CPP14Lexer.New -> C_NEW;
                case CPP14Lexer.Private -> C_PRIVATE;
                case CPP14Lexer.Protected -> C_PROTECTED;
                case CPP14Lexer.Public -> C_PUBLIC;
                case CPP14Lexer.Register -> C_REGISTER;
                case CPP14Lexer.Return -> C_RETURN;
                case CPP14Lexer.Short -> C_SHORT;
                case CPP14Lexer.Signed -> C_SIGNED;
                case CPP14Lexer.Sizeof -> C_SIZEOF;
                case CPP14Lexer.Static -> C_STATIC;
                case CPP14Lexer.Struct -> C_STRUCT;
                case CPP14Lexer.Class -> C_CLASS;
                case CPP14Lexer.Switch -> C_SWITCH;
                case CPP14Lexer.Template -> C_TEMPLATE;
                case CPP14Lexer.This -> C_THIS;
                case CPP14Lexer.Try -> C_TRY;
                case CPP14Lexer.Typedef -> C_TYPEDEF;
                case CPP14Lexer.Union -> C_UNION;
                case CPP14Lexer.Unsigned -> C_UNSIGNED;
                case CPP14Lexer.Virtual -> C_VIRTUAL;
                case CPP14Lexer.Void -> C_VOID;
                case CPP14Lexer.Volatile -> C_VOLATILE;
                case CPP14Lexer.While -> C_WHILE;
                case CPP14Lexer.Operator -> C_OPERATOR;
                case CPP14Lexer.Throw -> C_THROW;
                case CPP14Lexer.Nullptr -> C_NULL;
                default -> null;
            };
            if (tt != null) {
                adder.addToken(tt, token.getCharPositionInLine() + 1,token.getLine(),token.getText().length());
            }
        }

    }

    private File currentFile;

    public List<Token> scan(Set<File> files) throws ParsingException {
        List<Token> tokens = new ArrayList<>();
        TokenAdder adder = new TokenAdder() {
            @Override
            public void addToken(TokenType type, int column, int line, int length) {
                tokens.add(new Token(type,currentFile,line,column,length));
            }
        };
        for (File f : files) {
            this.currentFile = f;
            scanFile(f,adder);
            tokens.add(Token.fileEnd(currentFile));
        }
        return tokens;
    }
}
