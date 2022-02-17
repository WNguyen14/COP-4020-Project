package edu.ufl.cise.plc;

import java.util.ArrayList;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.*;

public class Parser implements IParser {
	
	IToken t;
	ILexer lexer;


	public Parser(String input)
	{
		this.lexer = CompilerComponentFactory.getLexer(input);
		try {
			this.t = lexer.next();
		} catch (LexicalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	protected boolean isKind(Kind kind)
	{
		return t.getKind() == kind;
	}
	
	protected boolean isKind(Kind...kinds)
	{
		for (Kind k:kinds)
		{
			if (k == t.getKind()) {
				return true;
			}
		}
		return false;
	}
	
	public Expr expr() throws SyntaxException
	{
		IToken firstToken = t;
		Expr left = null;
		Expr right = null;
		left = term();
		while(isKind(Kind.PLUS,Kind.MINUS))
		{
			IToken op = t;
			consume();
			right = term();
			left = new BinaryExpr(firstToken, left, op, right);
		}
		return left;
	}
	
	public Expr term() throws SyntaxException
	{
		IToken firstToken = t;
		Expr left = null;
		Expr right = null;
		
		left = factor();
		while(isKind(Kind.TIMES,Kind.DIV))
		{
			IToken op = t;
			consume();
			right = factor();
			left = new BinaryExpr(firstToken, left, op ,right);
		}
		return left;
	}
	
	Expr factor()  throws SyntaxException
	{
		IToken firstToken = t;
		Expr e = null;
		if(isKind(Kind.INT_LIT))
		{
			e = new IntLitExpr(firstToken);
			consume();
		}
		else if (isKind(Kind.LPAREN))
		{
			consume();
			e = expr();
			match(Kind.RPAREN);
		}
		else
		{
			throw new SyntaxException("Syntax Exception");
		}
		return e;
	}
	
	public void match(Kind k) throws SyntaxException
	{
		if (t.getKind() == k)
		{
			try {
				this.t = lexer.next();
			} catch (LexicalException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
			throw new SyntaxException("Syntax Exception");
	}
	
	public void consume() 
	{
		try {
			this.t = lexer.next();
		} catch (LexicalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	
	@Override
	public ASTNode parse() throws PLCException
	{
		return expr(); //return some ASTNode
		
	}

}
