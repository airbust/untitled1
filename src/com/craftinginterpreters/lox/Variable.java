package com.craftinginterpreters.lox;

class Variable {
    private boolean isConst;
    private int id;
    private String name;
    private Kind kind;
    private Type type;
    private int size;
    private long addr;
    private String value;

    Variable() {
        this.isConst = false;
    }

    Variable(String name, Kind kind, Type type, int size, long addr, boolean isConst) {
        this.name = name;
        this.kind = kind;
        this.type = type;
        this.isConst = isConst;
        this.size = size;
        this.addr = addr;
    }

    Variable(String name, Kind kind, Type type, long addr, boolean isConst) {
        this.name = name;
        this.kind = kind;
        this.type = type;
        this.isConst = isConst;
        this.addr = addr;
        if (type == Type.VOID) size = 0;
        else size = 8;
    }

    String getValue() {
        return value;
    }

    void setValue(String value) {
        this.value = value;
    }

    boolean isConst() {
        return isConst;
    }

    Type getType() {
        return type;
    }

    Kind getKind() {
        return kind;
    }

    String getName() {
        return name;
    }

    long getAddr() {
        return addr;
    }

    int getSize() {
        return size;
    }

    public void setConst(boolean aConst) {
        isConst = aConst;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setAddr(long addr) {
        this.addr = addr;
    }
}
