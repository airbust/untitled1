package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

class FunctionTable {
    private int nextFid;
    private List<Function> functions;

    FunctionTable() {
        functions = new ArrayList<>();
        nextFid = 1;
    }

    List<Function> getFunctions() {
        return functions;
    }

    int getNextFid() {
        return nextFid++;
    }

    boolean isDeclared(String name) {
        for (Function fn : functions) {
            if (fn.getName().equals(name))
                return true;
        }
        return false;
    }

    void addFunction(Function fn) {
        if (isDeclared(fn.getName()))
            System.exit(22);
        functions.add(fn);
    }

    Function getFunction(String name) {
        for (Function fn : functions) {
            if (fn.getName().equals(name))
                return fn;
        }
        return null;
    }
}
