package com.craftinginterpreters.lox;

class Program {
    private int magic = 0x72303b3e;
    private int version = 0x00000001;
    private SymbolTable globals;
    private FunctionTable functions;
    private Function _start;
    private int nextGlobalOffset;

    Program() {
        globals = new SymbolTable();
        functions = new FunctionTable();
        _start = new Function();

        _start.setFid(0);
        _start.setLocaSlots(0);
        _start.setParamSlots(0);
        _start.setReturnSlots(0);
        _start.setReturnType(Type.VOID);
        _start.setName("_start");

        Variable fn_name = new Variable();
        fn_name.setConst(true);
        fn_name.setKind(Kind.GLOBAL);
        fn_name.setName("");
        fn_name.setType(Type.STRING);
        fn_name.setAddr(nextGlobalOffset++);
        fn_name.setSize(6);
        fn_name.setValue("_start");
        globals.addVar(fn_name);
    }

    Function getFunction(String name) {
        return functions.getFunction(name);
    }

    public int getMagic() {
        return magic;
    }

    public int getVersion() {
        return version;
    }

    public SymbolTable getGlobals() {
        return globals;
    }

    public FunctionTable getFunctions() {
        return functions;
    }

    public Function get_start() {
        return _start;
    }

    public int getNextGlobalOffset() {
        return nextGlobalOffset;
    }
}
