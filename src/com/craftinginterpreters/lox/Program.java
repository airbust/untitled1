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

    public void setMagic(int magic) {
        this.magic = magic;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public SymbolTable getGlobals() {
        return globals;
    }

    public void setGlobals(SymbolTable globals) {
        this.globals = globals;
    }

    public FunctionTable getFunctions() {
        return functions;
    }

    public void setFunctions(FunctionTable functions) {
        this.functions = functions;
    }

    public Function get_start() {
        return _start;
    }

    public void set_start(Function _start) {
        this._start = _start;
    }

    public int getNextGlobalOffset() {
        return nextGlobalOffset;
    }

    public void setNextGlobalOffset(int nextGlobalOffset) {
        this.nextGlobalOffset = nextGlobalOffset;
    }
}
