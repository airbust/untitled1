package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

class GlobalStrTable {
    private static GlobalStrTable globalStrTable = null;
    private List<String> str;

    private GlobalStrTable() {
        str = new ArrayList<>();
    }

    static GlobalStrTable getInstance() {
        if (globalStrTable == null)
            globalStrTable = new GlobalStrTable();
        return globalStrTable;
    }

    void addStr(String s) {
        str.add(s);
    }
}
