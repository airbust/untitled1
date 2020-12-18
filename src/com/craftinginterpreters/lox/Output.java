package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

class Output {
    List<Byte> output = new ArrayList<>();

    static void printIR(Program program) {
        int sum = program.get_start().getInstructionCount();

        System.out.println("_start:\n");
        for (Instruction ins : program.get_start().getInstructionList()) {
            System.out.println(ins);
        }
        System.out.println("-------------------");

        for (Function fn : program.getFunctions().getFunctions()) {
            System.out.println(fn.getName() + ":\n");
            for (Instruction ins : fn.getInstructionList()) {
                System.out.println(ins);
            }
            sum += fn.getInstructionCount();
            System.out.println("-------------------");
        }
        System.out.println(sum);
        System.out.println("success!");
    }

    byte[] gen(Program program) {
        output.addAll(int2bytes(4, program.getMagic()));
        output.addAll(int2bytes(4, program.getVersion()));

        output.addAll(int2bytes(4, program.getGlobals().getTable().size()));
        for (Variable var : program.getGlobals().getTable()) {
            if (var.isConst())
                output.addAll(int2bytes(1, 1));
            else
                output.addAll(int2bytes(1, 0));
            output.addAll(int2bytes(4, var.getSize()));
            if (var.getType() == Type.STRING)
                output.addAll(string2bytes(var.getValue()));
            else
                output.addAll(long2bytes(8, 0));
        }

        output.addAll(int2bytes(4, program.getFunctions().getFunctions().size() + 1));

        genFunction(program.get_start());
        for (Function fn : program.getFunctions().getFunctions()) {
            genFunction(fn);
        }

        byte[] res = new byte[output.size()];
        for (int i = 0; i < output.size(); i++) {
            res[i] = output.get(i);
        }
        return res;
    }

    private void genFunction(Function fn) {
        output.addAll(int2bytes(4, fn.getFid()));
        output.addAll(int2bytes(4, fn.getReturnSlots()));
        output.addAll(int2bytes(4, fn.getParamSlots()));
        output.addAll(int2bytes(4, fn.getLocaSlots()));
        output.addAll(int2bytes(4, fn.getInstructionCount()));

        for (Instruction ins : fn.getInstructionList()) {
            output.addAll(int2bytes(1, ins.getOp()));
            if (ins.isFloat())
                output.addAll(double2Bytes(ins.f64));
            else {
                if (ins.getOp() == 0x01) // push u64
                    output.addAll(long2bytes(8, ins.i64));
                else if (ins.i64 != Long.MIN_VALUE)
                    output.addAll(long2bytes(4, ins.i64));
            }
        }
    }

    private static List<Byte> double2Bytes(double d) {
        long value = Double.doubleToRawLongBits(d);
        List<Byte> byteRet = new ArrayList<>();
        int start = 8 * (8-1);
        for (int i = 0; i < 8; i++) {
            byteRet.add((byte) (( value >> ( start - i * 8 )) & 0xFF ));
        }
        return byteRet;
    }

    private static List<Byte> char2bytes(char value) {
        List<Byte> AB = new ArrayList<>();
        AB.add((byte)(value&0xff));
        return AB;
    }

    private static List<Byte> string2bytes(String valueString) {
        List<Byte> AB = new ArrayList<>();
        for (int i=0;i<valueString.length();i++){
            char ch=valueString.charAt(i);
            AB.add((byte)(ch&0xff));
        }
        return AB;
    }

    private static List<Byte> long2bytes(int length, long target) {
        List<Byte> bytes = new ArrayList<>();
        int start = 8 * (length-1);
        for(int i = 0 ; i < length; i++){
            bytes.add((byte) (( target >> ( start - i * 8 )) & 0xFF ));
        }
        return bytes;
    }

    private static List<Byte> int2bytes(int length, int target){
        List<Byte> bytes = new ArrayList<>();
        int start = 8 * (length-1);
        for(int i = 0 ; i < length; i++){
            bytes.add((byte) (( target >> ( start - i * 8 )) & 0xFF ));
        }
        return bytes;
    }
}
