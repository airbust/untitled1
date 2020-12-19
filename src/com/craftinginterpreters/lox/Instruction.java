package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.InstructionType.*;

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

    boolean isJmp() {
        return instructionType == br || instructionType == brFalse
                || instructionType == brTrue || instructionType == ret
                || instructionType == call || instructionType == callname;
    }

    boolean isUnconditionalJmp() {
        return instructionType == br || instructionType == ret;
    }

    boolean isBr() {
        return instructionType == br || instructionType == brTrue || instructionType == brFalse;
    }

    @Override
    public String toString() {
        if (i64 == Long.MIN_VALUE && f64 == Double.MAX_VALUE)
            return instructionType.toString();
        else if (i64 != Long.MIN_VALUE)
            return instructionType.toString() + " " + i64;
        else
            return instructionType.toString() + " " + f64;
    }
}
