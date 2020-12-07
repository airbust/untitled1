package com.craftinginterpreters.lox;

public enum Instruction {
    push(0x01),
    popn(0x03),
    loca(0x0a),
    arga(0x0b),
    globa(0x0c),
    load(0x13),
    store(0x17),
    stackalloc(0x1a),
    add(0x20),
    sub(0x21),
    mul(0x22),
    div(0x23),
    not(0x2e),
    cmp(0x30),
    neg(0x34),
    setLt(0x39),
    setGt(0x3a),
    br(0x41),
    brTrue(0x43),
    call(0x48),
    ret(0x49),
    callname(0x4a);

    private Integer num;

    Instruction(Integer num) {
        this.num = num;
    }

    public Integer getNum() {
        return num;
    }
}
