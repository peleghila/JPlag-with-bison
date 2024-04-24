package de.jplag.bison;

import de.jplag.TokenType;
import de.jplag.bison.grammar.BisonLexer;
import de.jplag.bison.grammar.BisonParser;
import de.jplag.bison.grammar.BisonParserBaseListener;
import de.jplag.cpp2.CPPParserAdapter;
import de.jplag.cpp2.CPPTokenListener;
import de.jplag.cpp2.grammar.CPP14Lexer;
import de.jplag.cpp2.grammar.CPP14Parser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Deque;

import static de.jplag.bison.BisonTokenType.*;

public class BisonTokenListener extends BisonParserBaseListener {
    private final BisonParserAdapter parser;
    private final Deque<TokenType> trackedState = new ArrayDeque<>();
    private Token lastElseToken;

    /**
     * Constructs a new token listener.
     * @param parser the adapter to pass extracted tokens to.
     */
    public BisonTokenListener(BisonParserAdapter parser) {
        this.parser = parser;
    }


    @Override public void enterPrologue_declarations(BisonParser.Prologue_declarationsContext ctx) {
        addEnter(PROLOGUE_BEGIN,ctx.getStart());
    }
    @Override public void exitPrologue_declarations(BisonParser.Prologue_declarationsContext ctx) {
        addExit(PROLOGUE_END, ctx.getStop());
    }

    @Override public void enterBison_grammar(BisonParser.Bison_grammarContext ctx) {
        addEnter(RULES_BEGIN,ctx.getStart());
    }
    @Override public void exitBison_grammar(BisonParser.Bison_grammarContext ctx) {
        addExit(RULES_END,ctx.getStop());
    }

    @Override public void enterEpilogue_opt(BisonParser.Epilogue_optContext ctx) {
        addEnter(EPILOGUE_BEGIN,ctx.getStart());
        //if (ctx.EPILOGUE() != null) {
            //visit terminal will handle the code
        //}
    }
    @Override public void exitEpilogue_opt(BisonParser.Epilogue_optContext ctx) {
        addExit(EPILOGUE_END,ctx.getStop());
    }

    @Override public void visitTerminal(TerminalNode node) {
        switch (node.getSymbol().getType()){
            case BisonLexer.PROLOGUE:
                String code = node.getText().substring(2,
                        node.getText().length() - 2);
                addEnter(CODEBLOCK_BEGIN,node.getSymbol());
                doCppCode(code,node.getSymbol().getLine(),node.getSymbol().getCharPositionInLine() + 2);
                addExitAfter(CODEBLOCK_END,node.getSymbol());
                break;
            case BisonLexer.EPILOGUE:
                doCppCode(node.getText(),node.getSymbol().getLine(),node.getSymbol().getCharPositionInLine());
        }
    }

    private class AdjustedCPPParserAdapter extends CPPParserAdapter {
        int firstLineOffset;
        int firstLine;
        BisonParserAdapter parser;
        AdjustedCPPParserAdapter(int firstLine,int firstLineOffset, BisonParserAdapter parser) {
            this.firstLine = firstLine;
            this.firstLineOffset = firstLineOffset;
            this.parser = parser;
        }

        @Override
        public void addToken(TokenType type, int column, int line, int length) {
            parser.addToken(type,
                    line == 1 ? column + this.firstLineOffset : column,
                    line + this.firstLine - 1,
                    length);
        }
    }

    private void doCppCode(String cppCode,int line,int col) {
        //Bison preprocessing for $
        CPP14Lexer lexer = new CPP14Lexer(CharStreams.fromString(cppCode));
        // create a buffer of tokens pulled from the lexer
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        CPP14Parser parser = new CPP14Parser(tokenStream);
        CPP14Parser.TranslationUnitContext translationUnit = parser.translationUnit();

        ParseTreeWalker.DEFAULT.walk(new CPPTokenListener(new AdjustedCPPParserAdapter(line,col,this.parser)), translationUnit);
    }

    private void addEnter(TokenType type, Token token) {
        addTokenWithLength(type, token, token.getText().length());
    }

    private void addExit(TokenType type, Token token) {
        addTokenWithLength(type, token, 1);
    }

    private void addExitAfter(TokenType type, Token token) {
        String [] lines = token.getText().split("\\r\\n|\\r|\\n");
        int line = token.getLine() + lines.length - 1;
        int column = lines.length == 1 ?
                token.getCharPositionInLine() + lines[0].length() :
                lines[lines.length - 1].length();
        this.parser.addToken(type,column,line,0);
    }

    private void addTokenWithLength(TokenType type, Token token, int length) {
        int column = token.getCharPositionInLine() + 1;
        this.parser.addToken(type, column, token.getLine(), length);
    }
}
