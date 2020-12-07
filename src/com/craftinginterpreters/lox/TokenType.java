package com.craftinginterpreters.lox;

enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, MINUS, PLUS, SEMICOLON, MUL, DIV, COLON,

    // One or two character tokens.
    BANG_EQUAL, EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL, ARROW,

    // Literals.
    IDENTIFIER, STRING, CHAR, UINT, DOUBLE,

    // Keywords.
    FN, LET, CONST, AS, WHILE, IF, ELSE, RETURN, BREAK, CONTINUE,

    EOF,
}