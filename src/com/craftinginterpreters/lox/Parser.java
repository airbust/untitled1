package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;
    private SymbolTable symbolTable = SymbolTable.getInstance();

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

 /*  Expr parse() {
        return expression();
    }*/

    private Expr expression() {
        return assignment();
    }

    private Stmt declaration() {
        if (match(FN)) return function("function");
        if (match(LET)) return varDeclaration();
        if (match(CONST)) return constDeclaration();

        return statement();
    /*    try {
            if (match(LET)) return varDeclaration();
            if (match(CONST)) return constDeclaration();

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }*/
    }

    private Stmt statement() {
        if (match(IF)) return ifStatement();
       // if (match(PRINT)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());
        if (match(SEMICOLON)) return new Stmt.Empty();

        return expressionStatement();
    }

    private Stmt ifStatement() {
        Expr condition = expression();
        if (peek().type == LEFT_BRACE) {
            Stmt thenBranch = statement();
            Stmt elseBranch = null;
            if (match(ELSE)) {
                if (peek().type == LEFT_BRACE) {
                    elseBranch = statement();
                } else {
                    if (match(IF)) {
                        return ifStatement();
                    } else {
                        throw error(peek(), "Expect '{' or 'if'");
                    }
                }
            }
            return new Stmt.If(condition, thenBranch, elseBranch);
        }
        throw error(peek(), "Expect '{'");
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt whileStatement() {
        Expr condition = expression();
        if (peek().type == LEFT_BRACE) {
            Stmt body = statement();
            return new Stmt.While(condition, body);
        }
        throw error(peek(), "Expect '{'");
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");
        consume(COLON, "Expect ':'");
        Token type = consume(IDENTIFIER, "Expect type");
        if (!type.lexeme.equals("int") && !type.lexeme.equals("double"))
            throw error(previous(), "Type must be int or double");
        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
            symbolTable.addInitializedVar(name.lexeme, initializer.val);
        } else {
            symbolTable.addUninitializedVar(name.lexeme);
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt constDeclaration() {
        Token name = consume(IDENTIFIER, "Expect const name.");
        consume(COLON, "Expect ':'");
        Token type = consume(IDENTIFIER, "Expect type");
        if (!type.lexeme.equals("int") && !type.lexeme.equals("double"))
            throw error(previous(), "Type must be int or double");
        consume(EQUAL, "Expect '='");
        Expr initializer = expression();
        symbolTable.addConstVar(name.lexeme, initializer.val);
        consume(SEMICOLON, "Expect ';' after const declaration.");
        return new Stmt.Const(name, initializer);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
        if (symbolTable.isGlobalConstVar(name.lexeme) || symbolTable.isGlobalVar(name.lexeme))
            throw error(name, "function name exists or same with global var");
        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    throw error(peek(), "Can't have more than 255 parameters.");
                }

                parameters.add(
                        consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");
        consume(ARROW, "Expect '->' after ')'");
        Token ty = consume(IDENTIFIER, "Expect ty after '->'");
        if (!ty.lexeme.equals("void") && !ty.lexeme.equals("int") && !ty.lexeme.equals("double"))
            throw error(previous(), "ty must be void or int or double");

        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = function_block(ty);
        symbolTable.addGlobalConstVar(name.lexeme, name.lexeme);
        return new Stmt.Function(name, parameters, body);
    }

    private List<Stmt> function_block(Token type) {
        List<Stmt> statements = new ArrayList<>();
        String ty = type.lexeme;
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            Stmt stmt = declaration();
            if (stmt instanceof Stmt.Return) {
                if (!(ty.equals("void") && ((Stmt.Return) stmt).value == null)
                && !(ty.equals("int") && (((Stmt.Return) stmt).value.valType == UINT))
                && !(ty.equals("double") && (((Stmt.Return) stmt).value.valType == DOUBLE))) {
                    throw error(type, "return type not matched");
                }
            }
            statements.add(stmt);
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Expr assignment() {
        Expr expr = comparison();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.ConstVariable) {
                throw error(((Expr.ConstVariable) expr).name, "value of const cannot be changed");
            }
            throw error(peek(), "assignment err");
           // error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL, EQUAL_EQUAL, BANG_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            if (expr.valType != right.valType)
                throw error(peek(), "comparison type err");
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            if (expr.valType != right.valType)
                throw error(peek(), "term type err");
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = as();

        while (match(MUL, DIV)) {
            Token operator = previous();
            Expr right = unary();
            if (expr.valType != right.valType)
                throw error(peek(), "factor type err");
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr as() {
        Expr expr = unary();

        if (match(AS)) {
            if (check(IDENTIFIER)) {
                String ty = peek().lexeme;
                if (ty.equals("int")) {
                    expr.valType = UINT;
                } else {
                    if (ty.equals("double")) {
                        expr.valType = DOUBLE;
                    } else {
                        throw error(peek(), "as_expr ty must be int or double");
                    }
                }
                return expr;
            }
        }

        return expr;
    }

    private Expr unary() {
        if (match(MINUS)) {
            Token operator = previous();
            Expr right = unary();
            if (right.valType != UINT)
                throw error(peek(), "unary type err");
            return new Expr.Unary(operator, right);
        }

        return call();
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    throw error(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN,
                "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
    }

    private Expr call() {
        Expr expr = primary();

        if (match(LEFT_PAREN)) {
            expr = finishCall(expr);
        }

        return expr;
    }

    private Expr primary() {
        if (match(UINT, DOUBLE, STRING, CHAR)) {
            return new Expr.Literal(previous());
        }

        if (match(IDENTIFIER)) {
            String name = previous().lexeme;
            if (symbolTable.isConstVar(name))
                return new Expr.ConstVariable(previous(), symbolTable.getType(name));
            if (symbolTable.isUninitializedVar(name))
                throw error(previous(), "uninitialized var cannot be used");
            return new Expr.Variable(previous(), symbolTable.getType(name));
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
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

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case FN:
                case LET:
                case CONST:
                case WHILE:
                case IF:
              //  case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}