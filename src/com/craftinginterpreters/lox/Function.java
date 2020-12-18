package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

class Function {
    private int fid;
    private long fname;
    private int returnSlots;
    private int paramSlots;
    private int locaSlots;
    private List<Instruction> instructionList = new ArrayList<>();
    private String name;
    private Type returnType = Type.VOID;
    private List<Type> paramTypes = new ArrayList<>();

    int addInstruction(Instruction instruction) {
        instructionList.add(instruction);
        return instructionList.size() - 1;
    }

    void addParamType(Type type) {
        paramTypes.add(type);
        paramSlots++;
    }

    Instruction getInstruction(int id) {
        return instructionList.get(id);
    }

    int nextLoca() {
        return locaSlots++;
    }

    Type getReturnType() {
        return returnType;
    }

    String getName() {
        return name;
    }

    int getFid() {
        return fid;
    }

    int getInstructionCount() {
        return instructionList.size();
    }

    List<Instruction> getInstructionList() {
        return instructionList;
    }

    int getReturnSlots() {
        return returnSlots;
    }

    int getParamSlots() {
        return paramSlots;
    }

    int getLocaSlots() {
        return locaSlots;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }

    public void setReturnSlots(int returnSlots) {
        this.returnSlots = returnSlots;
    }

    public void setParamSlots(int paramSlots) {
        this.paramSlots = paramSlots;
    }

    public void setLocaSlots(int locaSlots) {
        this.locaSlots = locaSlots;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public void setFname(long fname) {
        this.fname = fname;
    }
}
