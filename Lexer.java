package edu.ufl.cise.plc;

import edu.ufl.cise.plc.IToken.Kind;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Lexer implements ILexer{
	private ArrayList<IToken> tokens = new ArrayList<>();
	private Kind kind;
	private int internalPos = 0;
	private enum State
	{
		START,
		IN_IDENT,
		HAVE_ZERO,
		HAVE_DOT,
		IN_FLOAT,
		IN_NUM,
		HAVE_EQ,
		HAVE_MINUS,
	}
	
	private String characters = "";

	public Lexer(String input)
	{
		int pos = 0;
		int startPos = 0;
		int line = 0;
		int col = 0;
		State state = State.START;
		//note for adding numbers
		while (pos < input.length())
		{
			char[] chars = input.toCharArray();
			char ch = chars [pos];
			if (state == State.START) //START CASE
			{
				characters = ""; 
				startPos = pos;
				switch(ch) //issue: if there is no 0, then we are stuck?
				{
					case '\n' ->
					{
						line++;
						col = 0;
						pos++;
					}
					case ' ', '\t', '\r' ->
					{
						pos++;
						col++;
					}
					case '+' ->
					{
						Token newToken = new Token(Kind.PLUS, Character.toString(ch) ,new IToken.SourceLocation(line, col), 1);
						tokens.add(newToken);
						pos++;
						col++;
					}
					case '-' ->
					{
						Token newToken = new Token(Kind.MINUS, Character.toString(ch) ,new IToken.SourceLocation(line, col), 1); 
						tokens.add(newToken);
						pos++;
						col++;
					}
					case '*' ->
					{
						Token newToken = new Token(Kind.TIMES, Character.toString(ch) ,new IToken.SourceLocation(line, col), 1);
						tokens.add(newToken);
						pos++;
						col++;
					}
					case '=' ->
					{
						state = State.HAVE_EQ;
						pos++;
						col++;
					}
					case '#' ->
					{
						while(ch != '\n')
						{
							pos++;
							col++;
							ch = chars [pos];
						}
					}
					case '"' ->
							{
								state = State.IN_IDENT;
								kind = Kind.STRING_LIT;
							}
					case 0 ->
					{
						// instructions say to add an EOF token?
						return;
					}
					default ->
					{
						if ( ('a' <= ch) && (ch <= 'z') || ('A' <= ch) && (ch <= 'Z')  || ch == '_' || ch == '$') {
							state = State.IN_IDENT;
						}
						else
						{
							Token newToken = new Token(Kind.ERROR, Character.toString(ch), new IToken.SourceLocation(line, col), 1);
							tokens.add(newToken);
							pos++;
							state = State.START;
						}
					}
				}

			}
			else if (state == State.HAVE_EQ)
			{
				switch (ch)
				{
					case '=' ->
					{
						Token newToken = new Token(Kind.EQUALS, Character.toString(ch), new IToken.SourceLocation(line, col), 2);
						tokens.add(newToken);
						pos++;
						col++;
					}
					default->
					{
						//throw an exception or something?
						return;
					}
				}
			}
			else if (state == State.IN_NUM)
			{
				int tokenPos = 0;
				switch(ch)
				{

					case '0','1','2','3','4','5','6','7','8','9' ->
					{
						tokenPos++;
						pos++;
						col++;
						characters+=(ch);
					}
					default ->
					{
						Token newToken = new Token(Kind.INT_LIT, characters, new IToken.SourceLocation(line, col), pos-tokenPos);
						tokens.add(newToken);
						state = State.START;
					}
				}


			}
			else if (state == State.IN_IDENT)
			{
				int tokenPos = 0;
				if ( ('a' <= ch) && (ch <= 'z') || ('A' <= ch) && (ch <= 'Z')  || ch == '_' || ch == '$' || ('0' <= ch) && (ch <= '9'))
				{
					System.out.println(ch);
					tokenPos++;
					pos++;
					col++;
					characters+=(ch);
				}
				else
				{
					Token newToken = new Token(Kind.IDENT, characters, new IToken.SourceLocation(line, col), pos-tokenPos);
					tokens.add(newToken);
					state = State.START;
				}

			}
		}
		tokens.add(new Token(Kind.EOF, input, new IToken.SourceLocation(line,col), 0));
	}
	@Override
	public IToken next() throws LexicalException
	{
		IToken nextToken = tokens.get(internalPos++);
		if (nextToken.getKind() == Kind.ERROR)
		{
			throw new LexicalException("illegal character");
		}
		else
		{
			return nextToken;
		}
		
	}

	@Override
	public IToken peek() throws LexicalException {
		return tokens.get(internalPos);
	}


}
