package com.craftinginterpreters.lox;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
  static boolean hadError = false;

  public static void main(String[] args) throws IOException {
    runFile(args[0], args[1]);
  }

  private static void runFile(String path1, String path2) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path1));
    run(new String(bytes, Charset.defaultCharset()), path2);

    // Indicate an error in the exit code.
    if (hadError) System.exit(65);
  }

  private static void run(String source, String dest) throws IOException {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();
    if (hadError) System.exit(65);

    Program program = new Program();
    Parser parser = new Parser(tokens);
    parser.parse(program);

    DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(dest)));
    byte[] res = new Output().gen(program);
    out.write(res);

    Output.printIR(program);
  }

  static void error(int line, String message) {
    report(line, "", message);
  }

  private static void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }

  static void error(Token token, String message) {
    if (token.type == TokenType.EOF) {
      report(token.line, " at end", message);
    } else {
      report(token.line, " at '" + token.lexeme + "'", message);
    }
  }
}