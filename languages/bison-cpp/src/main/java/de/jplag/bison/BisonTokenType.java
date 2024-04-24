package de.jplag.bison;

import de.jplag.TokenType;

public enum BisonTokenType implements TokenType {

    PROLOGUE_BEGIN("PROLOGUE{"),
    PROLOGUE_END("}PROLOGUE"),
    RULES_BEGIN("RULES{"),
    RULES_END("}RULES"),
    EPILOGUE_BEGIN("EPILOGUE{"),
    EPILOGUE_END("}EPILOGUE"),
    CODEBLOCK_BEGIN("RAWCODE{"),
    CODEBLOCK_END("}RAWCODE")
    ;

    private final String description;

    @Override
    public String getDescription() {
        return this.description;
    }

    BisonTokenType(String description) {
        this.description = description;
    }
}
