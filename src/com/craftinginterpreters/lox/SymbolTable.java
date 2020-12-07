package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

class SymbolTable {
    private static SymbolTable symbolTable = null;
    private Map<String, Object> globalVars = new HashMap<>();
    private Map<String, Object> globalConstVars = new HashMap<>();
    private Map<String, Object> vars = new HashMap<>();
    private Map<String, Object> constVars = new HashMap<>();

    private SymbolTable() {}
    public static SymbolTable getInstance() {
        if (symbolTable == null)
            symbolTable = new SymbolTable();
        return symbolTable;
    }

    TokenType getType(String name) {
        if (((Object) vars.get(name)) instanceof Long)
            return UINT;
        if (((Object) vars.get(name)) instanceof Double)
            return DOUBLE;
        if (((Object) constVars.get(name)) instanceof Long)
            return UINT;
        if (((Object) constVars.get(name)) instanceof Double)
            return DOUBLE;
        return null;
    }

    boolean isConstVar(String name) {
        return constVars.containsKey(name);
    }

    boolean isUninitializedVar(String name) {
        return vars.get(name) == null;
    }

    boolean isGlobalVar(String name) {
        return globalVars.containsKey(name);
    }

    boolean isGlobalConstVar(String name) {
        return globalConstVars.containsKey(name);
    }

    void addGlobalInitializedVar(String name, Object value) {
        if (globalVars.containsKey(name))
            throw new RuntimeException();
        if (globalConstVars.containsKey(name))
            throw new RuntimeException();
        globalVars.put(name, value);
    }

    void addGlobalUninitializedVar(String name) {
        if (globalVars.containsKey(name))
            throw new RuntimeException();
        if (globalConstVars.containsKey(name))
            throw new RuntimeException();
        globalVars.put(name, null);
    }

    void addGlobalConstVar(String name, Object value) {
        if (globalVars.containsKey(name))
            throw new RuntimeException();
        if (globalConstVars.containsKey(name))
            throw new RuntimeException();
        globalConstVars.put(name, value);
    }

    void checkDuplicate(String name) {
        if (vars.containsKey(name))
            throw new RuntimeException();
        if (constVars.containsKey(name))
            throw new RuntimeException();
    }

    void addInitializedVar(String name, Object value) {
        checkDuplicate(name);
        vars.put(name, value);
    }

    void addUninitializedVar(String name) {
        checkDuplicate(name);
        vars.put(name, null);
    }

    void addConstVar(String name, Object value) {
        checkDuplicate(name);
        constVars.put(name, value);
    }
}
