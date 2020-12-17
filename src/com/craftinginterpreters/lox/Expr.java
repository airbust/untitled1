package com.craftinginterpreters.lox;

import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

abstract class Expr {
  Type valType;
  Object val;

  interface Visitor<R> {
    R visitAssignExpr(Assign expr);
    R visitBinaryExpr(Binary expr);
    R visitCallExpr(Call expr);
    R visitGroupingExpr(Grouping expr);
    R visitLiteralExpr(Literal expr);
    R visitUnaryExpr(Unary expr);
    R visitVariableExpr(Variable expr);
    R visitConstVariableExpr(ConstVariable expr);
  }
  static class Assign extends Expr {
    Assign(Type valtype) {
      this.valType = Type.VOID;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignExpr(this);
    }

  }
  static class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
      this.valType = left.valType;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }
  static class Call extends Expr {
    Call(Type valtype) {
      this.valType = valtype;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCallExpr(this);
    }
  }
  static class Grouping extends Expr {
    Grouping(Expr expression) {
      this.expression = expression;
      this.valType = expression.valType;
      this.val = expression.val;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
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

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }

   // final Object value;
  }
  static class Unary extends Expr {
    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
      this.valType = right.valType;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }

    final Token operator;
    final Expr right;
  }
  static class Variable extends Expr {
    Variable(Token name, Type type) {
      this.name = name;
      this.valType = type;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }

    final Token name;
  }
  static class ConstVariable extends Expr {
    ConstVariable(Token name, Type type) {
      this.name = name;
      this.valType = type;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitConstVariableExpr(this);
    }

    final Token name;
  }

  abstract <R> R accept(Visitor<R> visitor);
}
