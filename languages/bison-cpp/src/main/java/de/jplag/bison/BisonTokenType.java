package de.jplag.bison;

import de.jplag.TokenType;

public enum BisonTokenType implements TokenType {

    PROLOGUE_BEGIN("PROLOGUE{"),
    PROLOGUE_END("}PROLOGUE"),
//    GRAMMAR_BEGIN("GRAMMAR{"),
//    GRAMMAR_END("}GRAMMAR"),
    GDECL("GRAMMAR_DECL"),
    RULES_BEGIN("RULES{"),
    RULES_END("}RULES"),
    RHS_BEGIN("RULE_RHS{"),
    RHS_END("}RULE_RHS"),
    ACTION_BLOCK_BEGIN("ACTION_BLOCK{"),
    ACTION_BLOCK_END("}ACTION_BLOCK"),
    EPILOGUE_BEGIN("EPILOGUE{"),
    EPILOGUE_END("}EPILOGUE"),
    CODEBLOCK_BEGIN("RAWCODE{"),
    CODEBLOCK_END("}RAWCODE"),
    TOKEN("TOKEN"),
    LEFT("LEFT"),
    RIGHT("RIGHT"),
    NONASSOC("NONASSOC"),
    PRECEDENCE("PRECEDENCE")
    ;

    public static record SYMBOL(String description) implements TokenType {

        public SYMBOL(String description) {
            this.description = description.toLowerCase();
        }
        @Override
        public String getDescription() { return this.description; }

    }

    private final String description;

    @Override
    public String getDescription() {
        return this.description;
    }

    BisonTokenType(String description) {
        this.description = description;
    }
}
