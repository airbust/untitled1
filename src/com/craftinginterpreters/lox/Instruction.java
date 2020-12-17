package com.craftinginterpreters.lox;

class Instruction {
    private InstructionType instructionType;
    private int size;
    long i64 = Long.MIN_VALUE;
    double f64 = Double.MAX_VALUE;

    boolean isBreak() {
        return i64 == Long.MAX_VALUE;
    }

    boolean isFloat() {
        return i64 == Long.MIN_VALUE && f64 != Double.MAX_VALUE;
    }

    Instruction(InstructionType instructionType) {
        this.instructionType = instructionType;
        this.size = 0;
    }
    Instruction(InstructionType instructionType, long i64) {
        this.instructionType = instructionType;
        this.i64 = i64;
        this.size = 4;
    }
    Instruction(InstructionType instructionType, double f64) {
        this.instructionType = instructionType;
        this.f64 = f64;
        this.size = 8;
    }

    int getSize() {
        return size;
    }

    int getOp() {
        return instructionType.ordinal();
    }

    @Override
    public String toString() {
       return instructionType.toString() + " " + i64;
    }
}
