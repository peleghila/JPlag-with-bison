package de.jplag.bison;

import de.jplag.ParsingException;
import de.jplag.TokenType;
import de.jplag.bison.grammar.BisonLexer;
import de.jplag.bison.grammar.BisonParser;
import de.jplag.bison.grammar.BisonParserBaseListener;
import de.jplag.cpp2.CPPParserAdapter;
import de.jplag.cpp2.CPPTokenListener;
import de.jplag.cpp2.grammar.CPP14Lexer;
import de.jplag.cpp2.grammar.CPP14Parser;
//import de.jplag.cpp.CPPScanner;
//import de.jplag.cpp.CPPTokenType;
//import de.jplag.cpp.Scanner;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayDeque;
import java.util.BitSet;
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

    @Override public void enterGrammar_declaration(BisonParser.Grammar_declarationContext ctx) {
        addTokenWithLength(GDECL,ctx.getStart(),ctx.getStart().getText().length());
    }

    @Override public void enterBison_grammar(BisonParser.Bison_grammarContext ctx) {
        //addEnter(GRAMMAR_BEGIN,ctx.getStart());
    }
    @Override public void exitBison_grammar(BisonParser.Bison_grammarContext ctx) {
        //addExit(GRAMMAR_END,ctx.getStop());
    }

    @Override public void enterRules(BisonParser.RulesContext ctx) {
        addEnter(RULES_BEGIN,ctx.getStart());
    }

    @Override public void exitRules(BisonParser.RulesContext ctx) {
        addExit(RULES_END,ctx.getStop());
    }

    @Override public void enterRhs(BisonParser.RhsContext ctx) {

        addEnter(RHS_BEGIN,ctx.getStart());
        trackedState.push(RHS_BEGIN);
    }

    @Override public void exitRhs(BisonParser.RhsContext ctx) {
        assert(trackedState.peek() == RHS_BEGIN);
        addExit(RHS_END,ctx.getStop());
        trackedState.pop();
    }

    @Override public void enterSymbol(BisonParser.SymbolContext ctx) {
        if (trackedState.peek() == RHS_BEGIN) {
            addTokenWithLength(new SYMBOL(ctx.getText()),ctx.getStart(),ctx.getStart().getText().length());
        }
    }

    @Override public void enterSymbol_declaration(BisonParser.Symbol_declarationContext ctx) {
        if (ctx.PERCENT_TOKEN() != null){
            addTokenWithLength(TOKEN,ctx.getStart(),ctx.getStart().getText().length());
        }
    }

    @Override public void enterPrecedence_declarator(BisonParser.Precedence_declaratorContext ctx) {
        TokenType tt = null;
        switch(ctx.getText()) {
            case "%left":
                tt = LEFT;
                break;
            case "%right":
                tt = RIGHT;
                break;
            case "%nonassoc":
                tt = NONASSOC;
                break;
            case "%precedence":
                tt = PRECEDENCE;
                break;
        }
        addTokenWithLength(tt,ctx.getStart(),ctx.getStart().getText().length());
    }

    @Override public void enterActionBlock(BisonParser.ActionBlockContext ctx) {
        addEnter(ACTION_BLOCK_BEGIN,ctx.getStart());
    }

    @Override public void exitActionBlock(BisonParser.ActionBlockContext ctx) {
        addExit(ACTION_BLOCK_END,ctx.getStop());
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
            case BisonLexer.BRACED_CODE:
            case BisonLexer.EPILOGUE:
                doCppCode(node.getText(),node.getSymbol().getLine(),node.getSymbol().getCharPositionInLine());
                break;
        }
    }

//    private class AdjustedCPPParserAdapter extends CPPParserAdapter {
//        int firstLineOffset;
//        int firstLine;
//        BisonParserAdapter parser;
//        AdjustedCPPParserAdapter(int firstLine,int firstLineOffset, BisonParserAdapter parser) {
//            this.firstLine = firstLine;
//            this.firstLineOffset = firstLineOffset;
//            this.parser = parser;
//        }
//
//        @Override
//        public void addToken(TokenType type, int column, int line, int length) {
//            parser.addToken(type,
//                    line == 1 ? column + this.firstLineOffset : column,
//                    line + this.firstLine - 1,
//                    length);
//        }
//    }
    private class AdjustedTokenAdder implements CPP14Scanner.TokenAdder {
            int firstLineOffset;
            int firstLine;

        AdjustedTokenAdder(int firstLine,int firstLineOffset) {
            this.firstLine = firstLine;
            this.firstLineOffset = firstLineOffset;

        }

        @Override
        public void addToken(TokenType type, int column, int line, int length) {
            parser.addToken(type,
                    line == 1 ? column + this.firstLineOffset : column,
                    line + this.firstLine - 1,
                    length);
        }
    }

//    class ScannerAdapter extends Scanner {
//        int firstLineOffset;
//        int firstLine;
//        ScannerAdapter(int firstLine,int firstLineOffset) {
//            this.firstLine = firstLine;
//            this.firstLineOffset = firstLineOffset;
//        }
//        @Override
//        public void add(CPPTokenType type, de.jplag.cpp.Token token) {
//            int length = token.endColumn - token.beginColumn + 1;
//            int column = token.beginLine == 1 ? token.beginColumn + this.firstLineOffset : token.beginColumn;
//            int line =  token.beginLine + this.firstLine - 1;
//            parser.addToken(type,column,line,length);
//        }
//    }

    private void doCppCode(String cppCode,int line,int col) {
        //Bison preprocessing for $
        cppCode = cppCode.replace('$','q');
        //StringReader reader = new StringReader(cppCode);

//        try {
//            CPPScanner.scanIS(
//                new ByteArrayInputStream(cppCode.getBytes("UTF-8")),
//                new ScannerAdapter(line,col),
//                null
//            );
//        } catch (ParsingException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

//        CPP14Lexer lexer = new CPP14Lexer(CharStreams.fromString(cppCode));
//        // create a buffer of tokens pulled from the lexer
//        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
//        CPP14Parser parser = new CPP14Parser(tokenStream);
//        CPP14Parser.TranslationUnitContext translationUnit = parser.translationUnit();
//        if (parser.getNumberOfSyntaxErrors() == 0)
//            ParseTreeWalker.DEFAULT.walk(new CPPTokenListener(new AdjustedCPPParserAdapter(line,col,this.parser)), translationUnit);
//        else {
//            //reset parser and try to get a block
//            tokenStream.reset();
//            parser = new CPP14Parser(tokenStream);
//            CPP14Parser.CompoundStatementContext block = parser.compoundStatement();
//            ParseTreeWalker.DEFAULT.walk(new CPPTokenListener(new AdjustedCPPParserAdapter(line, col, this.parser)), block);
//        }
        new CPP14Scanner().scanIS(CharStreams.fromString(cppCode),new AdjustedTokenAdder(line,col));
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
