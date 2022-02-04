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
		HAVE_MINUS, IN_STRINGLIT,
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
								characters += ch;
								pos++;
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
								state = State.IN_STRINGLIT;
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
								else if (('1' <= ch) && (ch <= '9'))
								{
									state = State.IN_NUM;
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
			else if (state == State.IN_STRINGLIT)
			{
				characters += (ch);
				pos++;
				ch = chars [pos];
				if (ch == '\\')
				{
					pos++;
					characters += (ch);
					ch = chars [pos];
					switch (ch)
					{
						case 'b', 't', 'n', 'f', 'r', '"', '\'', '\\' ->
								{
									pos++;
									characters += (ch);
								}
						default ->
								{
									//error token
								}

					}

				}
				else if (ch == '\"')
				{
					pos++;
					characters += (ch);
					Token newToken = new Token(Kind.STRING_LIT, characters, new IToken.SourceLocation(line, col), characters.length());
					tokens.add(newToken);
					col += characters.length();
					state = State.START;
				}
				else
				{
					pos++;
					characters += (ch);
				}
			}
			else if (state == State.HAVE_EQ)
			{
				switch (ch)
				{
					case '=' ->
							{
								characters += ch;
								Token newToken = new Token(Kind.EQUALS, characters, new IToken.SourceLocation(line, col), 2);
								tokens.add(newToken);
								pos++;
								col += 2;
								state = State.START;
							}
					default->
							{
								Token newToken = new Token(Kind.ASSIGN, characters, new IToken.SourceLocation(line, col), 1);
								tokens.add(newToken);
								col++;
								state = State.START;
							}
				}
			}
			else if (state == State.IN_NUM)
			{
				switch(ch)
				{

					case '0','1','2','3','4','5','6','7','8','9' ->
							{
								pos++;
								characters+=(ch);
							}
					case '.' ->
							{
								pos++;
								characters+=(ch);
								state = State.HAVE_DOT;
							}
					default ->
							{
								try
								{
									Integer.parseInt(characters);
									Token newToken = new Token(Kind.INT_LIT, characters, new IToken.SourceLocation(line, col), characters.length());
									tokens.add(newToken);
									col+=characters.length();
									state = State.START;
								}
								catch(Exception NumberFormatException)
								{
									Token newToken = new Token(Kind.ERROR, characters, new IToken.SourceLocation(line, col), characters.length());
									tokens.add(newToken);
									col+=characters.length();
									state = State.START;
									return;
								}
							}
				}


			}
			else if (state == State.IN_IDENT)
			{
				if ( (('a' <= ch) && (ch <= 'z')) || (('A' <= ch) && (ch <= 'Z'))  || ch == '_' || ch == '$' || (('0' <= ch) && (ch <= '9')))
				{
					System.out.println(ch);
					pos++;
					characters+=(ch);
				}
				else
				{
					Token newToken = new Token(Kind.IDENT, characters, new IToken.SourceLocation(line, col), characters.length());
					tokens.add(newToken);
					col+= characters.length();

					state = State.START;
				}

			}
			else if (state == State.HAVE_DOT)
			{
				switch(ch) {

					case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
						pos++;
						characters += (ch);
					}
					default -> {
						try {
							Float.parseFloat(characters);
							Token newToken = new Token(Kind.FLOAT_LIT, characters, new IToken.SourceLocation(line, col), characters.length());
							tokens.add(newToken);
							col += characters.length();
							state = State.START;
						} catch (Exception NumberFormatException) {
							Token newToken = new Token(Kind.ERROR, characters, new IToken.SourceLocation(line, col), characters.length());
							tokens.add(newToken);
							col += characters.length();
							state = State.START;
							return;
						}
					}
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
			throw new LexicalException("lexical exception");
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