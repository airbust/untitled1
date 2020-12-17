package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.InstructionType.*;

import java.util.List;

class Instruction {
    private InstructionType instructionType;
    private int size;
    long i64;
    private double f64;

    boolean isBreak() {
        return i64 == Long.MAX_VALUE;
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
}
