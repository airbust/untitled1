package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*; 

class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 1;
  private static final Map<String, TokenType> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put("fn",       FN);
    keywords.put("let",      LET);
    keywords.put("const",    CONST);
    keywords.put("as",       AS);
    keywords.put("while",    WHILE);
    keywords.put("if",       IF);
    keywords.put("else",     ELSE);
    keywords.put("return",   RETURN);
    keywords.put("break",    BREAK);
    keywords.put("continue", CONTINUE);
  }
  Scanner(String source) {
    this.source = source;
  }
  List<Token> scanTokens() {
    while (!isAtEnd()) {
      // We are at the beginning of the next lexeme.
      start = current;
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }
  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(': addToken(LEFT_PAREN); break;
      case ')': addToken(RIGHT_PAREN); break;
      case '{': addToken(LEFT_BRACE); break;
      case '}': addToken(RIGHT_BRACE); break;
      case ',': addToken(COMMA); break;
      case ':': addToken(COLON); break;
      case '-':
        addToken(match('>') ? ARROW : MINUS);
        break;
      case '+': addToken(PLUS); break;
      case ';': addToken(SEMICOLON); break;
      case '*': addToken(MUL); break;
      case '!':
        if (match('=')) {
          addToken(BANG_EQUAL);
        } else {
          Lox.error(line, "Unexpected character.");
        }
        break;
      case '=':
        addToken(match('=') ? EQUAL_EQUAL : EQUAL);
        break;
      case '<':
        addToken(match('=') ? LESS_EQUAL : LESS);
        break;
      case '>':
        addToken(match('=') ? GREATER_EQUAL : GREATER);
        break;
      case '/':
        if (match('/')) {
          // A comment goes until the end of the line.
          while (peek() != '\n' && !isAtEnd()) advance();
        } else {
          addToken(DIV);
        }
        break;

      case ' ':
      case '\r':
      case '\t':
        // Ignore whitespace.
        break;

      case '\n':
        line++;
        break;

      case '"': string(); break;

      case '\'': character(); break;

      default:
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        } else {
          Lox.error(line, "Unexpected character.");
        }
        break;
    }
  }
  private void identifier() {
    while (isAlphaNumeric(peek())) advance();

    String text = source.substring(start, current);
    TokenType type = keywords.get(text);
    if (type == null) type = IDENTIFIER;
    addToken(type);
  }
  private void number() {
    // Look for a fractional part.
      while (isDigit(peek())) advance();
      if (peek() == '.' && isDigit(peekNext())) {
        advance();
        while (isDigit(peek())) advance();
        int pos = current;
        if (peek() == 'e' || peek() == 'E') {
          advance();
          if (peek() == '+' || peek() == '-')
            advance();
          if (isDigit(peek())) {
            while (isDigit(peek())) advance();
          } else
            current = pos;
        }
        addToken(DOUBLE,
                Double.parseDouble(source.substring(start, current)));
      } else {
        addToken(UINT,
                Long.parseLong(source.substring(start, current)));
      }
  }
  private void string() {
    StringBuilder sb = new StringBuilder();
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\\') {
        if (isEscape(peekNext())) {
          sb.append(getEscape(peekNext()));
          advance(); advance();
        } else {
          Lox.error(line, "string literal err");
          return;
        }
      } else {
        sb.append(peek());
        advance();
      }
    }

    if (isAtEnd()) {
      Lox.error(line, "Unterminated string.");
      return;
    }

    // The closing ".
    advance();

    // Trim the surrounding quotes.
    addToken(STRING, sb.toString());
  }
  private void character() {
    char value = 0;
    if (isCharRegular(peek())) {
      value = peek();
      advance();
    } else {
      if (peek() == '\\' && isEscape(peekNext())) {
        value = getEscape(peekNext());
        advance(); advance();
      }
    }
    if (match('\'')) {
      addToken(CHAR, value);
    } else {
      Lox.error(line, "char literal err");
    }
  }
  private boolean match(char expected) {
    if (isAtEnd()) return false;
    if (source.charAt(current) != expected) return false;

    current++;
    return true;
  }
  private char peek() {
    if (isAtEnd()) return '\0';
    return source.charAt(current);
  }
  private char peekNext() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  }
  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') ||
            (c >= 'A' && c <= 'Z') ||
            c == '_';
  }
  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }
  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }
  private boolean isEscape(char c) {
    return c == '\\' || c == '"' || c == '\'' || c == 'n' || c == 'r' || c == 't';
  }
  private char getEscape(char c) {
    switch (c) {
      case '\\': return '\\';
      case '"': return '"';
      case '\'': return '\'';
      case 'n': return '\n';
      case 'r': return '\r';
      case 't': return '\t';
      default: Lox.error(line, "getEscape err");
    }
    return 0;
  }
  private boolean isCharRegular(char c) {
    return c != '\'' && c != '\\';
  }
  private boolean isAtEnd() {
    return current >= source.length();
  }
  private char advance() {
    current++;
    return source.charAt(current - 1);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }
}