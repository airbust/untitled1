package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

class SymbolTable {
    private SymbolTable parent;
    private List<Variable> table = new ArrayList<>();

    SymbolTable() {}

    SymbolTable(SymbolTable parent) {
        this.parent = parent;
    }

    void clear() {
        table.clear();
    }

    SymbolTable getParent() {
        return parent;
    }

    List<Variable> getTable() {
        return table;
    }

    boolean isDeclared(String name) {
        if (name.equals(""))
            return false;
        for (Variable var : table) {
            if (var.getName().equals(name))
                return true;
        }
        return false;
    }

    boolean isGlobalTable() {
        return parent == null;
    }

    SymbolTable getGlobal() {
        if (parent == null)
            return this;
        return parent.getGlobal();
    }

    Variable getVar(String name) {
        if (name.equals(""))
            return null;
        for (Variable var : table) {
            if (var.getName().equals(name))
                return var;
        }
        if (parent == null)
            return null;
        return parent.getVar(name);
    }

    void addVar(Variable var) {
        if (isDeclared(var.getName()))
            System.exit(64);
        table.add(var);
    }
}
