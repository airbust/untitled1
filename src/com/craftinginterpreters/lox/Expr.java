package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.TokenType.*;

abstract class Expr {
  Type valType;
  Object val;

  static class Assign extends Expr {
    Assign(Type valtype) {
      this.valType = Type.VOID;
    }
  }
  static class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
      this.valType = left.valType;
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }
  static class Call extends Expr {
    Call(Type valtype) {
      this.valType = valtype;
    }
  }
  static class Grouping extends Expr {
    Grouping(Expr expression) {
      this.expression = expression;
      this.valType = expression.valType;
      this.val = expression.val;
    }

    final Expr expression;
  }
  static class Literal extends Expr {
    Literal(Token token) {
      this.val = token.literal;
      if (token.type == UINT || token.type == CHAR)
        this.valType = Type.INT;
      else if (token.type == DOUBLE)
        this.valType = Type.DOUBLE;
      else if (token.type == STRING)
        this.valType = Type.STRING;
    }
  }
  static class Unary extends Expr {
    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
      this.valType = right.valType;
    }

    final Token operator;
    final Expr right;
  }
  static class Variable extends Expr {
    Variable(Token name, Type type) {
      this.name = name;
      this.valType = type;
    }

    final Token name;
  }
}
