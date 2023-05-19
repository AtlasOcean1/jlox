package com.craftinginterpreters.lox;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

class AstPrinter implements Expr.Visitor<String>,
                            Stmt.Visitor<String> {
  String print(Expr expr) {
    return expr.accept(this);
  }

  public static void main(String[] args) throws IOException {
  String path = "test.lox";
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    String source = new String(bytes, Charset.defaultCharset());
    
    List<Stmt> statements = Lox.getStatements(source);
    
    System.out.println("(ns user)");
    System.out.println("");
    System.out.println(new AstPrinter().print(statements));
  }
  
  String print(List<Stmt> statements) {
    StringBuilder str = new StringBuilder();
    
  try {
    for (Stmt statement : statements) {
    String clojureStr = statement.accept(this);
    str.append(clojureStr);
    }
    
    } catch (RuntimeError error) {
    Lox.runtimeError(error);
    }
    
    return str.toString();
  }

  @Override
  public String visitBinaryExpr(Expr.Binary expr) {
    return parenthesize(expr.operator.lexeme,
                        expr.left, expr.right);
  }

  @Override
  public String visitLiteralExpr(Expr.Literal expr) {
    if (expr.value == null) return "nil";
    return expr.value.toString();
  }

  @Override
  public String visitGroupingExpr(Expr.Grouping expr) {
    return parenthesize("group", expr.expression);
  }

  @Override
  public String visitUnaryExpr(Expr.Unary expr) {
    return parenthesize(expr.operator.lexeme, expr.right);
  }

  @Override
  public String visitAssignExpr(Expr.Assign expr) {
    return parenthesize(expr.name.lexeme, expr.value);
  }

  @Override
  public String visitLogicalExpr(Expr.Logical expr) {
    return parenthesize(expr.operator.lexeme, expr.right, expr.left);
  }

  @Override
  public String visitVariableExpr(Expr.Variable expr) {
    return parenthesize(expr.name.lexeme, expr);
  }

  @Override
  public String visitCallExpr(Expr.Call expr) {
    String paren     = parenthesize(expr.paren.lexeme, expr);
    String callee    = parenthesize("callee", expr.callee);

    StringBuilder str = new StringBuilder();

    try {
    for (Expr argument : expr.arguments) {
      String clojureStr = parenthesize("arguments", argument);
      str.append(clojureStr);
    }
      
    } catch (RuntimeError error) {
      Lox.runtimeError(error);
    }
    
    str.append(paren);
    str.append(callee);
    
    return str.toString();
  }

  @Override
  public String visitGetExpr(Expr.Get expr) {
    String name  = parenthesize("expr.name", expr);
    String object = parenthesize("object", expr.object);

    StringBuilder str = new StringBuilder();

    str.append(name);
    str.append(object);

    return str.toString();
  }

  @Override
  public String visitSetExpr(Expr.Set expr) {
    String object = parenthesize("object", expr.object);
    String name   = parenthesize("expr.name", expr);
    String value  = parenthesize("value", expr.value);

    StringBuilder str = new StringBuilder();
    
    str.append(object);
    str.append(name);
    str.append(value);

    return str.toString();
  }

  @Override
  public String visitSuperExpr(Expr.Super expr) {
    String keyword = parenthesize("expr.keyword", expr);
    String method  = parenthesize("expr.method", expr);

    StringBuilder str = new StringBuilder();

    str.append(keyword);
    str.append(method);

    return str.toString();
  }

  @Override
  public String visitThisExpr(Expr.This expr) {
    return parenthesize(expr.keyword.lexeme, expr);
  }

  @Override
  public String visitBlockStmt(Stmt.Block stmt) {
    StringBuilder str = new StringBuilder();
    
  try {
    for (Stmt statement : stmt.statements) {
    String clojureStr = parenthesize("block", statement);
    str.append(clojureStr);
    }
    
    } catch (RuntimeError error) {
    Lox.runtimeError(error);
    }
    
    return str.toString();
  }

  @Override
  public String visitExpressionStmt(Stmt.Expression stmt) {
    return parenthesize("Expression", stmt.expression);
  }

  @Override
  public String visitIfStmt(Stmt.If stmt) {
    String condition     = parenthesize ("if", stmt.condition);
    String thenbranch    = parenthesize ("then", stmt.thenBranch);
    String elsebranchstr = parenthesize ("else", stmt.elseBranch);

    StringBuilder str = new StringBuilder();
    str.append(condition);
    str.append(thenbranch);
    str.append(elsebranchstr);
    return str.toString();
  }

  @Override
  public String visitPrintStmt(Stmt.Print stmt) {
    return parenthesize("print", stmt.expression);
  }

  @Override
  public String visitVarStmt(Stmt.Var stmt) {
    return parenthesize(stmt.name.lexeme, stmt.initializer);
  }

  @Override
  public String visitWhileStmt(Stmt.While stmt) {
    String condition     = parenthesize ("while", stmt.condition);
    String body          = parenthesize ("body", stmt.body);

    StringBuilder str = new StringBuilder();
    str.append(condition);
    str.append(body);
    return str.toString();
  }

  @Override
  public String visitClassStmt(Stmt.Class stmt) {
    String name       = parenthesize(stmt.name.lexeme, stmt);
    String superclass = parenthesize("superclass", stmt.superclass);

    StringBuilder str = new StringBuilder();

    try {
      for (Stmt statement : stmt.methods) {
      String clojureStr = parenthesize("methods", statement);
      str.append(clojureStr);
      }
      
      } catch (RuntimeError error) {
      Lox.runtimeError(error);
      }
      
    str.append(name);
    str.append(superclass);
    
    return str.toString();
  }

  @Override
  public String visitFunctionStmt(Stmt.Function stmt) {
    String name   = parenthesize("stmt.name", stmt);
    String params = parenthesize("stmt.params", stmt);

    StringBuilder str = new StringBuilder();

    try {
    for (Stmt statement : stmt.body) {
      String clojureStr = parenthesize("body", statement);
      str.append(clojureStr);
    }
      
    } catch (RuntimeError error) {
      Lox.runtimeError(error);
    }

    str.append(name);
    str.append(params);

    return str.toString();
  }

  @Override
  public String visitReturnStmt(Stmt.Return stmt) {
    return parenthesize(stmt.keyword.lexeme, stmt);
  }


  private String parenthesize(String name, Expr... exprs) {
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);
    for (Expr expr : exprs) {
      builder.append(" ");
      builder.append(expr.accept(this));
    }
    builder.append(")");

    return builder.toString();
  }

  private String parenthesize(String name, Stmt... stmts) {
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);
    for (Stmt stmt : stmts) {
      builder.append(" ");
      builder.append(stmt.accept(this));
    }
    builder.append(")");

    return builder.toString();
  }

}
