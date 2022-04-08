package edu.ufl.cise.plc;

import java.util.ArrayList;
import java.util.List;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.*;
import edu.ufl.cise.plc.ast.Types.Type;

public class Parser implements IParser {
	
	//"or" "and" broken
	
	IToken t;
	ILexer lexer;

	List<ASTNode> decsAndStatements = new ArrayList<ASTNode>();
	List<NameDef> listNameDef = new ArrayList<NameDef>();
	
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
	
	
	public Program prog() throws PLCException
	{
		IToken firstToken = t;
		Type returnType;
		String name;
		
		if(isKind(Kind.KW_VOID))
		{
			returnType = Type.VOID;
		}
		else{
			try {
				Type.toType(t.getText());
			}
			catch(IllegalArgumentException e)
			{
				throw new SyntaxException("invalid character name");
			}
			returnType = Type.toType(t.getText());
		}
		consume();
		if (t.getKind() != Kind.IDENT)
		{
			throw new SyntaxException("invalid character name");
		}
		name = t.getText();
		consume();
		match(Kind.LPAREN);
		if(t.getKind() != Kind.RPAREN)
		{
			IToken t2 = t;
			consume();
			try {
				Type.toType(t2.getText());
			}
			catch (Exception IllegalArgumentException){
				throw new SyntaxException("bad comma");
			}
			if(Type.toType(t2.getText()) == Type.VOID)
			{
				throw new SyntaxException("void param");
			}
			if (t.getKind() == Kind.LSQUARE)
			{
				IToken lsquare = t;
				match(Kind.LSQUARE);
				Expr width = expr();
				match(Kind.COMMA);
				Expr height = expr();
				match(Kind.RSQUARE);
				NameDefWithDim n = new NameDefWithDim(t, t2.getText(), t.getText(), new Dimension(lsquare, width, height));
				consume();
				listNameDef.add(n);
			}
			else {
			listNameDef.add(new NameDef(t, t2.getText(), t.getText()));
			consume();
			}
			while(t.getKind() == Kind.COMMA)
			{
				consume();
				t2 = t;
				try {
					Type.toType(t2.getText());
				}
				catch (Exception IllegalArgumentException){
					throw new SyntaxException("bad comma");
				}
				if(Type.toType(t2.getText()) == Type.VOID)
				{
					throw new SyntaxException("void param");
				}
				consume();
				if (t.getKind() == Kind.LSQUARE)
				{
					IToken lsquare = t;
					match(Kind.LSQUARE);
					Expr width = expr();
					match(Kind.COMMA);
					Expr height = expr();
					match(Kind.RSQUARE);
					NameDefWithDim n = new NameDefWithDim(t, t2.getText(), t.getText(), new Dimension(lsquare, width, height));
					consume();
					listNameDef.add(n);
				}
				else {
				listNameDef.add(new NameDef(t, t2.getText(), t.getText()));
				consume();
				}
			}
		}
		match(Kind.RPAREN);
		
		
		while(t.getKind() != Kind.EOF) {
			try {
			if (Type.toType(t.getText()) == Type.VOID || Type.toType(t.getText()) == Type.CONSOLE) 
				{
					throw new SyntaxException("void name/console name");
				}
			}
			catch (IllegalArgumentException e){
			}
			if(t.getKind() == Kind.IDENT || t.getKind() == Kind.KW_WRITE || t.getKind() == Kind.RETURN)
			  { 
				  if(t.getKind() == Kind.RETURN) 
				  { 
					  ReturnStatement r;
					  firstToken = t; 
					  consume();
					  Expr e = expr();
					  r = new ReturnStatement(firstToken, e);
					  decsAndStatements.add(r);
				  }
				  else if (t.getKind() == Kind.KW_WRITE)
				  {
					  WriteStatement w;
					  firstToken = t;
					  consume();
					  Expr source = expr();
					  match(Kind.RARROW);
					  Expr dest = expr();
					  w = new WriteStatement(firstToken, source, dest);
					  decsAndStatements.add(w);
				  }
				  else if (t.getKind() == Kind.IDENT)
				  {
					  firstToken = t;
					  consume();
					  PixelSelector p = null;
					  if (isKind(Kind.LSQUARE))
					{
						consume();
						Expr x = expr();
						match(Kind.COMMA);
						Expr y = expr();
						match(Kind.RSQUARE);
						p = new PixelSelector(firstToken, x, y);
						
					}
					if (t.getKind() == Kind.ASSIGN)
					{
						consume();
						Expr e = expr();
						AssignmentStatement a = new AssignmentStatement(firstToken, firstToken.getText(), p, e);
						decsAndStatements.add(a);
					}
					else if (t.getKind() == Kind.LARROW)
					{
						consume();
						Expr e = expr();
						ReadStatement r = new ReadStatement(firstToken, firstToken.getText(), p, e);
						decsAndStatements.add(r);
					}						
					}
					  

				  match(Kind.SEMI);
			  }
			else {
			IToken t2 = t;
			consume();
			if (t.getKind() == Kind.IDENT)
			{
				NameDef n = new NameDef(t, t2.getText(), t.getText());
				consume();
				if(t.getKind() != Kind.SEMI)
				{
					IToken var = t;
					consume();
					decsAndStatements.add(new VarDeclaration(t2, n, var, expr()));
				}
				else {
				decsAndStatements.add(new VarDeclaration(t2, n, null, null));
				}
			}
			else
			{
				IToken lsquare = t;
				match(Kind.LSQUARE);
				Expr width = expr();
				match(Kind.COMMA);
				Expr height = expr();
				match(Kind.RSQUARE);
				NameDefWithDim n = new NameDefWithDim(t, t2.getText(), t.getText(), new Dimension(lsquare, width, height));
				consume();
				if(t.getKind() != Kind.SEMI)
				{
					IToken var = t;
					consume();
					decsAndStatements.add(new VarDeclaration(t2, n, var, expr()));
				}
				else {
				decsAndStatements.add(new VarDeclaration(t2, n, null, null));
				}
			}
			
			match(Kind.SEMI);
			}
		}
		return new Program(firstToken, returnType, name, listNameDef, decsAndStatements);
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
			e.setType(Type.BOOLEAN);
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
		else if (isKind(Kind.COLOR_CONST))
		{
			e = new ColorConstExpr(firstToken);
			consume();
		}
		else if (isKind(Kind.LANGLE))
		{
			consume();
			Expr red = expr();
			match(Kind.COMMA);
			Expr green = expr();
			match(Kind.COMMA);
			Expr blue = expr();
			e = new ColorExpr(firstToken, red, green, blue);
			match(Kind.RANGLE);
		}
		else if (isKind(Kind.KW_CONSOLE))
		{
			e = new ConsoleExpr(firstToken);
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
		
		return prog(); //return some ASTNode
		
	}

}
