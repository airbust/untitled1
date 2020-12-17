package com.craftinginterpreters.lox;

enum InstructionType {
    nop(0x00),
    push(0x01),
    pop(0x02),
    popn(0x03),
    dup(0x04),
    loca(0x0a),
    arga(0x0b),
    globa(0x0c),
    load64(0x13),
    store64(0x17),
    alloc(0x18),
    free(0x19),
    stackalloc(0x1a),
    addi(0x20),
    subi(0x21),
    muli(0x22),
    divi(0x23),
    addf(0x24),
    subf(0x25),
    mulf(0x26),
    divf(0x27),
    divu(0x28),
    shl(0x29),
    shr(0x2a),
    and(0x2b),
    or(0x2c),
    not(0x2e),
    cmpi(0x30),
    cmpu(0x32),
    cmpf(0x32),
    negi(0x34),
    negf(0x35),
    itof(0x36),
    ftoi(0x37),
    shrl(0x38),
    setLt(0x39),
    setGt(0x3a),
    br(0x41),
    brFalse(0x42),
    brTrue(0x43),
    call(0x48),
    ret(0x49),
    callname(0x4a),
    scani(0x50),
    scanc(0x51),
    scanf(0x52),
    printi(0x54),
    printc(0x55),
    printf(0x56),
    prints(0x57),
    println(0x58),
    panic(0xfe);

    private int num;

    InstructionType(int num) {
        this.num = num;
    }

    int getNum() {
        return num;
    }
}
