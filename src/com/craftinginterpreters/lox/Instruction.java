package com.craftinginterpreters.lox;

class Instruction {
    private InstructionType instructionType;
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
    }
    Instruction(InstructionType instructionType, long i64) {
        this.instructionType = instructionType;
        this.i64 = i64;
    }
    Instruction(InstructionType instructionType, double f64) {
        this.instructionType = instructionType;
        this.f64 = f64;
    }

    int getOp() {
        return instructionType.getNum();
    }

    @Override
    public String toString() {
       return instructionType.toString() + " " + i64;
    }
}
