package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;
import static com.craftinginterpreters.lox.InstructionType.*;

class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;
    private int nextGlobalOffset;
    private SymbolTable symbolTable = new SymbolTable();
    private GlobalStrTable globalStrTable = GlobalStrTable.getInstance();

    Parser(List<Token> tokens, int nextGlobalOffset) {
        this.tokens = tokens;
        this.nextGlobalOffset = nextGlobalOffset;
    }

    void parse(Program program) {
        Function _start = program.get_start();
        SymbolTable globals = program.getGlobals();
        FunctionTable functionTable = program.getFunctions();
        while (!isAtEnd()) {
            declaration(globals, functionTable, _start);
        }
        Function main = program.getFunction("main");
        _start.addInstruction(new Instruction(call, main.getFid()));
    }

    private Expr expression(SymbolTable symbolTable, FunctionTable functionTable, Function fn) {
        return assignment(symbolTable, functionTable, fn);
    }

    private void declaration(SymbolTable symbolTable, FunctionTable functionTable, Function fn) {
        if (match(FN)) {
            function(symbolTable, functionTable);
            return;
        }
        if (match(LET)) {
            varDeclaration(symbolTable, functionTable, fn);
            return;
        }
        if (match(CONST)) {
            constDeclaration(symbolTable, functionTable, fn);
            return;
        }

        statement(symbolTable, functionTable, fn, -1);
    }

    private void statement(SymbolTable symbolTable, FunctionTable functionTable, Function fn, int while_st) {
        if (match(IF)) {
            ifStatement(symbolTable, functionTable, fn, while_st);
            return;
        }
        if (match(RETURN)) {
            returnStatement(symbolTable, functionTable, fn);
            return;
        }
        if (match(BREAK)) {
            if (while_st == -1)
                throw error(peek(), "must break in a while");
            consume(SEMICOLON, "Expect ';'.");
            fn.addInstruction(new Instruction(br, Long.MAX_VALUE));
            return;
        }
        if (match(CONTINUE)) {
            if (while_st == -1)
                throw error(peek(), "must continue in a while");
            int t = fn.getInstructionCount();
            fn.addInstruction(new Instruction(br, while_st - t));
            return;
        }
        if (match(WHILE)) {
            whileStatement(symbolTable, functionTable, fn);
            return;
        }
        if (match(LEFT_BRACE)) {
            SymbolTable newSymbolTable = new SymbolTable(symbolTable);
            block(newSymbolTable, functionTable, fn, while_st);
            return;
        }
        if (match(SEMICOLON)) {
            return;
        }

        expressionStatement(symbolTable, functionTable, fn);
    }

    private void ifStatement(SymbolTable symbolTable, FunctionTable functionTable, Function fn, int while_st) {
        Expr condition = expression(symbolTable, functionTable, fn);
        SymbolTable newSymbolTable = new SymbolTable(symbolTable);
        int id = fn.addInstruction(new Instruction(brFalse, 0));
        if (peek().type == LEFT_BRACE) {
            statement(newSymbolTable, functionTable, fn, while_st);
            newSymbolTable.clear();
            if (match(ELSE)) {
                int id2 = fn.addInstruction(new Instruction(br, 0));
                Instruction jmp = fn.getInstruction(id);
                jmp.i64 = id2 - id;
                if (peek().type == LEFT_BRACE) {
                    statement(newSymbolTable, functionTable, fn, while_st);
                } else if (match(IF)) {
                    ifStatement(symbolTable, functionTable, fn, while_st);
                } else {
                    throw error(peek(), "Expect '{' or 'if'");
                }

                int id3 = fn.addInstruction(new Instruction(nop));
                Instruction jmp2 = fn.getInstruction(id2);
                jmp2.i64 = id3 - id2;
            } else {
                int id4 = fn.addInstruction(new Instruction(nop));
                Instruction jmp = fn.getInstruction(id);
                jmp.i64 = id4 - id;
            }
            return;
        }
        throw error(peek(), "Expect '{'");
    }

    private void returnStatement(SymbolTable symbolTable, FunctionTable functionTable, Function fn) {
        if (fn.getReturnType() != Type.VOID)
            fn.addInstruction(new Instruction(arga, 0));
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression(symbolTable, functionTable, fn);
            if (value.valType != fn.getReturnType())
                throw error(peek(), "function return type err");
            if (fn.getReturnType() != Type.VOID)
                fn.addInstruction(new Instruction(store64));
        } else if (fn.getReturnType() != Type.VOID)
            throw error(peek(), "function return type err");
        fn.addInstruction(new Instruction(ret));
        consume(SEMICOLON, "Expect ';' after return value.");
    }

    private void whileStatement(SymbolTable symbolTable, FunctionTable functionTable, Function fn) {
        int x = fn.addInstruction(new Instruction(nop));
        Expr condition = expression(symbolTable, functionTable, fn);
        fn.addInstruction(new Instruction(brTrue, 1));
        int id = fn.addInstruction(new Instruction(br, 0));
        SymbolTable newSymbolTable = new SymbolTable(symbolTable);
        if (peek().type == LEFT_BRACE) {
            statement(newSymbolTable, functionTable, fn, x);
            int ed = fn.getInstructionCount();
            fn.addInstruction(new Instruction(br, x-ed-1));
            ed = fn.addInstruction(new Instruction(nop));
            Instruction jmp = fn.getInstruction(id);
            jmp.i64 = ed-id;
            List<Instruction> instructions = fn.getInstructionList();
            for (int i = 0; i < instructions.size(); i++) {
                if (instructions.get(i).isBreak()) {
                    Instruction ins = fn.getInstruction(i);
                    ins.i64 = ed-i;
                }
            }
        } else
            throw error(peek(), "Expect '{'");
    }

    private void varDeclaration(SymbolTable symbolTable, FunctionTable functionTable, Function fn) {
        Token name = consume(IDENTIFIER, "Expect variable name.");
        consume(COLON, "Expect ':'");
        Token type = consume(IDENTIFIER, "Expect type");
        Type valtype;
        if (type.lexeme.equals("int"))
            valtype = Type.INT;
        else if (type.lexeme.equals("double"))
            valtype = Type.DOUBLE;
        else
            throw error(previous(), "Type must be int or double");

        Kind kind = symbolTable.isGlobalTable() ? Kind.GLOBAL : Kind.VAR;
        long addr = kind == Kind.GLOBAL ? nextGlobalOffset++ : fn.nextLoca();
        symbolTable.addVar(new Variable(name.lexeme, kind, valtype, addr, false));

        Expr initializer = null;
        if (match(EQUAL)) {
            if (kind == Kind.GLOBAL)
                fn.addInstruction(new Instruction(globa, addr));
            else if (kind == Kind.VAR)
                fn.addInstruction(new Instruction(loca, addr));
            initializer = expression(symbolTable, functionTable, fn);
            if (initializer.valType != valtype)
                throw error(peek(), "let lhs and rhs type not matched");
            fn.addInstruction(new Instruction(store64));
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
    }

    private void constDeclaration(SymbolTable symbolTable, FunctionTable functionTable, Function fn) {
        Token name = consume(IDENTIFIER, "Expect const name.");
        consume(COLON, "Expect ':'");
        Token type = consume(IDENTIFIER, "Expect type");
        Type valtype;
        if (type.lexeme.equals("int"))
            valtype = Type.INT;
        else if (type.lexeme.equals("double"))
            valtype = Type.DOUBLE;
        else
            throw error(previous(), "Type must be int or double");
        consume(EQUAL, "Expect '='");

        Kind kind = symbolTable.isGlobalTable() ? Kind.GLOBAL : Kind.VAR;
        long addr = kind == Kind.GLOBAL ? nextGlobalOffset++ : fn.nextLoca();
        symbolTable.addVar(new Variable(name.lexeme, kind, valtype, addr, true));

        Expr initializer;
        if (kind == Kind.GLOBAL)
            fn.addInstruction(new Instruction(globa, addr));
        else if (kind == Kind.VAR)
            fn.addInstruction(new Instruction(loca, addr));
        initializer = expression(symbolTable, functionTable, fn);
        if (initializer.valType != valtype)
            throw error(peek(), "let lhs and rhs type not matched");
        fn.addInstruction(new Instruction(store64));

        consume(SEMICOLON, "Expect ';' after const declaration.");
    }

    private void expressionStatement(SymbolTable symbolTable, FunctionTable functionTable, Function fn) {
        Expr expr = expression(symbolTable, functionTable, fn);
        consume(SEMICOLON, "Expect ';' after expression.");
    }

    private void function(SymbolTable symbolTable, FunctionTable functionTable) {
        Function function = new Function();
        SymbolTable newSymbolTable = new SymbolTable(symbolTable);
        Token name = consume(IDENTIFIER, "Expect function name.");
        if (functionTable.isDeclared(name.lexeme))
            throw error(name, "function name exists or same with global var");
        function.setName(name.lexeme);
        consume(LEFT_PAREN, "Expect '(' after function name.");
        List<Variable> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                Variable var = new Variable();
                if (match(CONST))
                    var.setConst(true);
                consume(IDENTIFIER, "Expect parameter name.");
                var.setName(previous().lexeme);
                consume(COLON, "Expect ':'");
                Token ty = consume(IDENTIFIER, "Expect ty");
                if (ty.lexeme.equals("int"))
                    var.setType(Type.INT);
                else if (ty.lexeme.equals("double"))
                    var.setType(Type.DOUBLE);
                else
                    throw error(ty, "function param type cannot be void");
                var.setKind(Kind.PARAM);
                parameters.add(var);
                newSymbolTable.addVar(var);
                function.addParamType(var.getType());
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");
        consume(ARROW, "Expect '->' after ')'");
        Token ty = consume(IDENTIFIER, "Expect ty after '->'");
        switch (ty.lexeme) {
            case "void":
                function.setReturnType(Type.VOID);
                function.setReturnSlots(0);
                for (int i = 0; i < parameters.size(); i++) {
                    parameters.get(i).setAddr(i);
                }
                break;

            case "int":
                function.setReturnType(Type.INT);
                function.setReturnSlots(1);
                for (int i = 0; i < parameters.size(); i++) {
                    parameters.get(i).setAddr(i+1);
                }
                break;

            case "double":
                function.setReturnType(Type.DOUBLE);
                function.setReturnSlots(1);
                for (int i = 0; i < parameters.size(); i++) {
                    parameters.get(i).setAddr(i+1);
                }
                break;

            default:
                throw error(previous(), "ty must be void or int or double");
        }
        function.setFid(functionTable.getNextFid());
        consume(LEFT_BRACE, "Expect '{' before function body.");
        block(newSymbolTable, functionTable, function, -1);

     // needed when there is no return in block
        function.addInstruction(new Instruction(ret));
        SymbolTable global = symbolTable.getGlobal();
        Variable fn_name = new Variable();
        fn_name.setName("");
        fn_name.setKind(Kind.GLOBAL);
        fn_name.setType(Type.STRING);
        fn_name.setConst(false);
        fn_name.setSize(name.lexeme.length());
        fn_name.setAddr(nextGlobalOffset++);
        global.addVar(fn_name);
        globalStrTable.addStr(name.lexeme);
        function.setFname(fn_name.getAddr());
        functionTable.addFunction(function);
    }

    private void block(SymbolTable symbolTable, FunctionTable functionTable, Function fn, int while_st) {
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            declaration(symbolTable, functionTable, fn);
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
    }

    private Expr assignment(SymbolTable symbolTable, FunctionTable functionTable, Function fn) {
        if (check(IDENTIFIER)) {
            if (peekNext().type == EQUAL) {
                String name = peek().lexeme;
                Variable l_expr = symbolTable.getVar(name);
                if (l_expr == null)
                    throw error(peek(), "Invalid assignment target.");
                advance(); advance();
                if (l_expr.isConst())
                    throw error(peek(), "const cannot be assigned");
                if (l_expr.getKind() == Kind.GLOBAL)
                    fn.addInstruction(new Instruction(globa, l_expr.getAddr()));
                else if (l_expr.getKind() == Kind.PARAM)
                    fn.addInstruction(new Instruction(arga, l_expr.getAddr()));
                else if (l_expr.getKind() == Kind.VAR)
                    fn.addInstruction(new Instruction(loca, l_expr.getAddr()));
                else
                    throw error(peek(), "assign rhs should not be void");
                Expr value = assignment(symbolTable, functionTable, fn);
                if (l_expr.getType() != value.valType)
                    throw error(peek(), "l_expr and value type not same");
                fn.addInstruction(new Instruction(store64));
                return new Expr.Assign(Type.VOID);
            }
        }

        return comparison(symbolTable, functionTable, fn);
    }

    private Expr comparison(SymbolTable symbolTable, FunctionTable functionTable, Function fn) {
        Expr expr = term(symbolTable, functionTable, fn);

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL, EQUAL_EQUAL, BANG_EQUAL)) {
            Token operator = previous();
            Expr right = term(symbolTable, functionTable, fn);
            if (expr.valType != right.valType)
                throw error(peek(), "comparison type err");
            if (operator.type == LESS_EQUAL) {
                if (right.valType == Type.INT)
                    fn.addInstruction(new Instruction(cmpi));
                else if (right.valType == Type.DOUBLE)
                    fn.addInstruction(new Instruction(cmpf));
                else
                    throw error(previous(), "cmp type error");
                fn.addInstruction(new Instruction(setGt));
                fn.addInstruction(new Instruction(not));
            } else if (operator.type == GREATER_EQUAL) {
                if (right.valType == Type.INT)
                    fn.addInstruction(new Instruction(cmpi));
                else if (right.valType == Type.DOUBLE)
                    fn.addInstruction(new Instruction(cmpf));
                else
                    throw error(previous(), "cmp type error");
                fn.addInstruction(new Instruction(setLt));
                fn.addInstruction(new Instruction(not));
            } else if (operator.type == LESS) {
                if (right.valType == Type.INT)
                    fn.addInstruction(new Instruction(cmpi));
                else if (right.valType == Type.DOUBLE)
                    fn.addInstruction(new Instruction(cmpf));
                else
                    throw error(previous(), "cmp type error");
                fn.addInstruction(new Instruction(setLt));
            } else if (operator.type == GREATER) {
                if (right.valType == Type.INT)
                    fn.addInstruction(new Instruction(cmpi));
                else if (right.valType == Type.DOUBLE)
                    fn.addInstruction(new Instruction(cmpf));
                else
                    throw error(previous(), "cmp type error");
                fn.addInstruction(new Instruction(setGt));
            } else if (operator.type == BANG_EQUAL) {
                if (right.valType == Type.INT)
                    fn.addInstruction(new Instruction(cmpi));
                else if (right.valType == Type.DOUBLE)
                    fn.addInstruction(new Instruction(cmpf));
                else
                    throw error(previous(), "cmp type error");
            } else if (operator.type == EQUAL_EQUAL) {
                if (right.valType == Type.INT)
                    fn.addInstruction(new Instruction(cmpi));
                else if (right.valType == Type.DOUBLE)
                    fn.addInstruction(new Instruction(cmpf));
                else
                    throw error(previous(), "cmp type error");
                fn.addInstruction(new Instruction(not));
            }
            expr = new Expr.Binary(expr, operator, right);
            expr.valType = Type.BOOL;
        }

        return expr;
    }

    private Expr term(SymbolTable symbolTable, FunctionTable functionTable, Function fn) {
        Expr expr = factor(symbolTable, functionTable, fn);

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor(symbolTable, functionTable, fn);
            if (expr.valType != right.valType)
                throw error(peek(), "term type err");
            if (operator.type == PLUS) {
                if (right.valType == Type.INT)
                    fn.addInstruction(new Instruction(addi));
                else if (right.valType == Type.DOUBLE)
                    fn.addInstruction(new Instruction(addf));
                else
                    throw error(peek(), "term type err");
            } else { // MINUS
                if (right.valType == Type.INT)
                    fn.addInstruction(new Instruction(subi));
                else if (right.valType == Type.DOUBLE)
                    fn.addInstruction(new Instruction(subf));
                else
                    throw error(peek(), "term type err");
            }
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor(SymbolTable symbolTable, FunctionTable functionTable, Function fn) {
        Expr expr = as(symbolTable, functionTable, fn);

        while (match(MUL, DIV)) {
            Token operator = previous();
            Expr right = unary(symbolTable, functionTable, fn);
            if (expr.valType != right.valType)
                throw error(peek(), "factor type err");
            if (operator.type == MUL) {
                if (right.valType == Type.INT)
                    fn.addInstruction(new Instruction(muli));
                else if (right.valType == Type.DOUBLE)
                    fn.addInstruction(new Instruction(mulf));
                else
                    throw error(peek(), "factor type err");
            } else { // DIV
                if (right.valType == Type.INT)
                    fn.addInstruction(new Instruction(divi));
                else if (right.valType == Type.DOUBLE)
                    fn.addInstruction(new Instruction(divf));
                else
                    throw error(peek(), "factor type err");
            }
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr as(SymbolTable symbolTable, FunctionTable functionTable, Function fn) {
        Expr expr = unary(symbolTable, functionTable, fn);

        if (match(AS)) {
            if (check(IDENTIFIER)) {
                String ty = peek().lexeme;
                if (ty.equals("int")) {
                    if (expr.valType == Type.DOUBLE)
                        fn.addInstruction(new Instruction(ftoi));
                    expr.valType = Type.INT;
                } else if (ty.equals("double")) {
                    if (expr.valType == Type.INT)
                        fn.addInstruction(new Instruction(itof));
                    expr.valType = Type.DOUBLE;
                } else {
                    throw error(peek(), "as_expr ty must be int or double");
                }
                return expr;
            }
        }

        return expr;
    }

    private Expr unary(SymbolTable symbolTable, FunctionTable functionTable, Function fn) {
        if (match(MINUS)) {
            Token operator = previous();
            Expr right = unary(symbolTable, functionTable, fn);
            if (right.valType != Type.INT && right.valType != Type.DOUBLE)
                throw error(peek(), "unary type err");
            if (right.valType == Type.INT)
                fn.addInstruction(new Instruction(negi));
            else if (right.valType == Type.DOUBLE)
                fn.addInstruction(new Instruction(negf));
            return new Expr.Unary(operator, right);
        }

        return call(symbolTable, functionTable, fn);
    }

    private Expr call(SymbolTable symbolTable, FunctionTable functionTable, Function fn) {
        if (peek().type == IDENTIFIER) {
            String name = peek().lexeme;
            switch (name) {
                case "getint":
                    advance();
                    consume(LEFT_PAREN,
                            "Expect '(' after getint.");
                    consume(RIGHT_PAREN,
                            "Expect ')' after '('.");
                    fn.addInstruction(new Instruction(scani));
                    return new Expr.Call(Type.INT);

                case "getdouble":
                    advance();
                    consume(LEFT_PAREN,
                            "Expect '(' after getdouble.");
                    consume(RIGHT_PAREN,
                            "Expect ')' after '('.");
                    fn.addInstruction(new Instruction(scanf));
                    return new Expr.Call(Type.DOUBLE);

                case "getchar":
                    advance();
                    consume(LEFT_PAREN,
                            "Expect '(' after getdouble.");
                    consume(RIGHT_PAREN,
                            "Expect ')' after '('.");
                    fn.addInstruction(new Instruction(scanc));
                    return new Expr.Call(Type.INT);

                case "putln":
                    advance();
                    consume(LEFT_PAREN,
                            "Expect '(' after getdouble.");
                    consume(RIGHT_PAREN,
                            "Expect ')' after '('.");
                    fn.addInstruction(new Instruction(println));
                    return new Expr.Call(Type.VOID);

                case "putint":
                    advance();
                    consume(LEFT_PAREN,
                            "Expect '(' after putint.");
                    if (expression(symbolTable, functionTable, fn).valType != Type.INT)
                        throw error(previous(), "param not match");
                    consume(RIGHT_PAREN,
                            "Expect ')' after '('.");
                    fn.addInstruction(new Instruction(printi));
                    return new Expr.Call(Type.VOID);

                case "putdouble":
                    advance();
                    consume(LEFT_PAREN,
                            "Expect '(' after putdouble.");
                    if (expression(symbolTable, functionTable, fn).valType != Type.DOUBLE)
                        throw error(previous(), "param not match");
                    consume(RIGHT_PAREN,
                            "Expect ')' after '('.");
                    fn.addInstruction(new Instruction(printf));
                    return new Expr.Call(Type.VOID);

                case "putchar":
                    advance();
                    consume(LEFT_PAREN,
                            "Expect '(' after putchar.");
                    if (expression(symbolTable, functionTable, fn).valType != Type.INT)
                        throw error(previous(), "param not match");
                    consume(RIGHT_PAREN,
                            "Expect ')' after '('.");
                    fn.addInstruction(new Instruction(printc));
                    return new Expr.Call(Type.VOID);

                case "putstr":
                    advance();
                    consume(LEFT_PAREN,
                            "Expect '(' after putchar.");
                    if (peek().type == STRING)
                        primary(symbolTable, functionTable, fn);
                    else if (peek().type != UINT)
                        throw error(peek(), "putstr param must be string or int");
                    consume(RIGHT_PAREN,
                            "Expect ')' after '('.");
                    fn.addInstruction(new Instruction(prints));
                    return new Expr.Call(Type.VOID);
            }

            if (peekNext().type == LEFT_PAREN) {
                advance(); advance();
                Function pfn;
                if (name.equals(fn.getName()))
                    pfn = fn;
                else
                    pfn = functionTable.getFunction(name);
                if (pfn.getReturnType() != Type.VOID)
                    fn.addInstruction(new Instruction(stackalloc, 1));

                List<Expr> arguments = new ArrayList<>();
                if (!check(RIGHT_PAREN)) {
                    do {
                        if (arguments.size() >= 255) {
                            throw error(peek(), "Can't have more than 255 arguments.");
                        }
                        arguments.add(expression(symbolTable, functionTable, fn));
                    } while (match(COMMA));
                }

                consume(RIGHT_PAREN,
                        "Expect ')' after arguments.");

                fn.addInstruction(new Instruction(call, pfn.getFid()));
                return new Expr.Call(pfn.getReturnType());
            }
        }

        return primary(symbolTable, functionTable, fn);
    }

    private Expr primary(SymbolTable symbolTable, FunctionTable functionTable, Function fn) {
        if (match(UINT)) {
            fn.addInstruction(new Instruction(push, (long) previous().literal));
            return new Expr.Literal(previous());
        } else if (match(DOUBLE)) {
            fn.addInstruction(new Instruction(push, (double) previous().literal));
            return new Expr.Literal(previous());
        } else if (match(CHAR)) {
            fn.addInstruction(new Instruction(push, Character.getNumericValue((char) previous().literal)));
            return new Expr.Literal(previous());
        } else if (match(STRING)) {
            Variable var = new Variable("", Kind.GLOBAL, Type.STRING,
                    previous().lexeme.length(), nextGlobalOffset++, true);
            globalStrTable.addStr(previous().lexeme);
            SymbolTable globals = symbolTable.getGlobal();
            globals.addVar(var);
            fn.addInstruction(new Instruction(push, var.getAddr()));
            return new Expr.Literal(previous());
        }

        if (match(IDENTIFIER)) {
            String name = previous().lexeme;
            Variable var = symbolTable.getVar(name);
            if (var == null)
                throw error(previous(), "this var cannot be used");
            Kind kind = var.getKind();
            if (kind == Kind.GLOBAL)
                fn.addInstruction(new Instruction(globa, var.getAddr()));
            else if (kind == Kind.PARAM)
                fn.addInstruction(new Instruction(arga, var.getAddr()));
            else if (kind == Kind.VAR)
                fn.addInstruction(new Instruction(loca, var.getAddr()));
            fn.addInstruction(new Instruction(load64));
            return new Expr.Variable(previous(), var.getType());
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression(symbolTable, functionTable, fn);
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token peekNext() {
        if (current+1 >= tokens.size())
            throw error(previous(), "cannot peeknext");
        return tokens.get(current+1);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }
}