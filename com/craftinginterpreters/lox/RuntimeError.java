package com.craftinginterpreters.lox;

class RuntimeError extends RuntimeException {
  final Token token;

  RunTimeError(Token token, String message) {
    super(message);
    this.token = token;
  }
}
