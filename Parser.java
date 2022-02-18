package edu.ufl.cise.plc;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.*;
import edu.ufl.cise.plc.ast.UnaryExprPostfix;

public class Parser implements IParser {
	
	//"or" "and" broken
	
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
	
	public Expr expr() throws PLCException
	{
		IToken firstToken = t;
		Expr left = null;
		Expr right = null;
		left = term();
		while(isKind(Kind.OR,Kind.GT, Kind.LT, Kind.EQUALS, Kind.NOT_EQUALS, Kind.LE, Kind.GE,Kind.PLUS,Kind.MINUS,Kind.MOD))
		{
			while (isKind(Kind.OR))
			{
				IToken op = t;
				consume();
				right = term();
				left = new BinaryExpr(firstToken, left, op, right);
			}
			
			while(isKind(Kind.GT, Kind.LT, Kind.EQUALS, Kind.NOT_EQUALS, Kind.LE, Kind.GE))
			{
				IToken op = t;
				consume();
				right = term();
				left = new BinaryExpr(firstToken, left, op, right);			
			}
			while(isKind(Kind.PLUS,Kind.MINUS,Kind.MOD))
			{
				IToken op = t;
				consume();
				right = term();
				left = new BinaryExpr(firstToken, left, op, right);
			}
		}
		return left;
	}
	
	public Expr term() throws PLCException
	{
		IToken firstToken = t;
		Expr left = null;
		Expr right = null;
		
		left = factor();
		while (isKind(Kind.AND))
		{
			IToken op = t;
			consume();
			right = factor();
			left = new BinaryExpr(firstToken, left, op, right);
		}
		while(isKind(Kind.TIMES,Kind.DIV, Kind.MOD))
		{
			IToken op = t;
			consume();
			right = factor();
			left = new BinaryExpr(firstToken, left, op ,right);
		}
		return left;
	}
	
	Expr factor()  throws PLCException
	{
		IToken firstToken = t;
		Expr e = null;
		if (isKind(Kind.KW_IF)) //questionable
		{
			Expr condition = null;
			consume();
			if (isKind(Kind.LPAREN))
			{
				consume();
				condition = expr();
				match(Kind.RPAREN);
			}
			else
			{
				throw new SyntaxException("bad if");
			}
			Expr trueCase = expr();
			if (isKind(Kind.KW_ELSE))
			{
				consume();
				Expr falseCase = expr();
				if(isKind(Kind.KW_FI))
				{
					e = new ConditionalExpr(firstToken, condition, trueCase, falseCase);
				}
				else 
				{
					throw new SyntaxException("bad if");
				}
			}
			else
			{
				throw new SyntaxException("bad if");
			}
			consume();
		}
		else if (isKind(Kind.BANG) || isKind(Kind.MINUS) || isKind(Kind.COLOR_OP) || isKind(Kind.IMAGE_OP))
		{
			IToken op = t;
			consume();
			Expr unaryExpression = expr();
			e = new UnaryExpr(firstToken, op, unaryExpression);
		}
		
		else if (isKind(Kind.BOOLEAN_LIT))
		{
			e = new BooleanLitExpr(firstToken);
			consume();
		}
		else if (isKind(Kind.STRING_LIT))
		{
			e = new StringLitExpr(firstToken);
			consume();
		}
		else if(isKind(Kind.INT_LIT))
		{
			e = new IntLitExpr(firstToken);
			consume();
		}
		else if (isKind(Kind.FLOAT_LIT))
		{
			e = new FloatLitExpr(firstToken);
			consume();
		}
		else if (isKind(Kind.IDENT))
		{
			e = new IdentExpr(firstToken);
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
		if (isKind(Kind.LSQUARE))
		{
			consume();
			Expr x = expr();
			match(Kind.COMMA);
			Expr y = expr();
			match(Kind.RSQUARE);
			PixelSelector p = new PixelSelector(firstToken, x, y);
			e = new UnaryExprPostfix(firstToken, e, p);
		}
		return e;
	}
	
	
	public void match(Kind k) throws PLCException
	{
		System.out.println(t.getText());

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
	
	public void consume() throws PLCException
	{
		System.out.println(t.getText());
			this.t = lexer.next();
	}
		
	
	@Override
	public ASTNode parse() throws PLCException
	{
		return expr(); //return some ASTNode
		
	}

}
