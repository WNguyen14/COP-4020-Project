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
		IN_STRINGLIT,
		HAVE_GT,
		HAVE_LT,
		HAVE_BANG,
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
								state = State.HAVE_MINUS;
								characters += ch;
								pos++;
							}
					case '*' ->
							{
								Token newToken = new Token(Kind.TIMES, Character.toString(ch) ,new IToken.SourceLocation(line, col), 1);
								tokens.add(newToken);
								pos++;
								col++;
							}
					//start here
					case '&' ->
							{
								Token newToken = new Token(Kind.AND, Character.toString(ch) ,new IToken.SourceLocation(line, col), 1);
								tokens.add(newToken);
								pos++;
								col++;
							}
					case '!' ->
							{
								state = State.HAVE_BANG;
								characters += ch;
								pos++;
							}
					case ',' ->
							{
								Token newToken = new Token(Kind.COMMA, Character.toString(ch) ,new IToken.SourceLocation(line, col), 1);
								tokens.add(newToken);
								pos++;
								col++;
							}
					case '/' ->
							{
								Token newToken = new Token(Kind.DIV, Character.toString(ch) ,new IToken.SourceLocation(line, col), 1);
								tokens.add(newToken);
								pos++;
								col++;
							}
					case '>' ->
							{
								state = State.HAVE_GT;
								characters += ch;
								pos++;
							}
					case '<' ->
							{
								state = State.HAVE_LT;
								characters += ch;
								pos++;
							}
					case '(' ->
							{
								Token newToken = new Token(Kind.LPAREN, Character.toString(ch) ,new IToken.SourceLocation(line, col), 1);
								tokens.add(newToken);
								pos++;
								col++;
							}
					case '[' ->
							{
								Token newToken = new Token(Kind.LSQUARE, Character.toString(ch) ,new IToken.SourceLocation(line, col), 1);
								tokens.add(newToken);
								pos++;
								col++;
							}
					case '%' ->
							{
								Token newToken = new Token(Kind.MOD, Character.toString(ch) ,new IToken.SourceLocation(line, col), 1);
								tokens.add(newToken);
								pos++;
								col++;
							}
					case '|' ->
							{
								Token newToken = new Token(Kind.OR, Character.toString(ch) ,new IToken.SourceLocation(line, col), 1);
								tokens.add(newToken);
								pos++;
								col++;
							}
					case '^' ->
							{
								Token newToken = new Token(Kind.RETURN, Character.toString(ch) ,new IToken.SourceLocation(line, col), 1);
								tokens.add(newToken);
								pos++;
								col++;
							}
					case ')' ->
							{
								Token newToken = new Token(Kind.RPAREN, Character.toString(ch) ,new IToken.SourceLocation(line, col), 1);
								tokens.add(newToken);
								pos++;
								col++;
							}
					case ']' ->
							{
								Token newToken = new Token(Kind.RSQUARE, Character.toString(ch) ,new IToken.SourceLocation(line, col), 1);
								tokens.add(newToken);
								pos++;
								col++;
							}
					case ';' ->
							{
								Token newToken = new Token(Kind.SEMI, Character.toString(ch) ,new IToken.SourceLocation(line, col), 1);
								tokens.add(newToken);
								pos++;
								col++;
							}
					//end here
					case '=' ->
							{
								state = State.HAVE_EQ;
								characters += ch;
								pos++;
							}
					case '#' ->
							{
								while(ch != '\n' && pos < input.length())
								{
									pos++;
									ch = chars [pos];
								}
							}
					case '"' ->
							{
								state = State.IN_STRINGLIT;
								kind = Kind.STRING_LIT;
								characters += (ch);
								pos++;
							}
					default ->
							{
								if ( ('a' <= ch) && (ch <= 'z') || ('A' <= ch) && (ch <= 'Z')  || ch == '_' || ch == '$') {
									state = State.IN_IDENT;
									if(pos == (input.length()-1) )
									{
										characters+=(ch);
										checkIdentifier(characters, line, col);
										col+= characters.length();
										state = State.START;
										pos++;
									}
								}
								else if (('0' <= ch) && (ch <= '9'))
								{
									state = State.IN_NUM;
									if(pos == (input.length()-1) ) {
										characters += ch;
										try {
											Integer.parseInt(characters);
											Token newToken = new Token(Kind.INT_LIT, characters, new IToken.SourceLocation(line, col), characters.length());
											tokens.add(newToken);
											col += characters.length();
											state = State.START;
											pos++;
										} catch (Exception NumberFormatException) {
											Token newToken = new Token(Kind.ERROR, characters, new IToken.SourceLocation(line, col), characters.length());
											tokens.add(newToken);
											col += characters.length();
											state = State.START;
											pos++;
											return;
										}
									}
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
			else if (state == State.HAVE_MINUS)
			{
				switch (ch)
				{
					case '>' ->
							{
								characters += ch;
								Token newToken = new Token(Kind.RARROW, characters, new IToken.SourceLocation(line, col), 2);
								tokens.add(newToken);
								pos++;
								col += 2;
								state = State.START;
							}
					default->
							{
								Token newToken = new Token(Kind.MINUS, characters, new IToken.SourceLocation(line, col), 1);
								tokens.add(newToken);
								col++;
								state = State.START;
							}
				}
			}
			else if (state == State.HAVE_BANG)
			{
				switch (ch)
				{
					case '=' ->
							{
								characters += ch;
								Token newToken = new Token(Kind.NOT_EQUALS, characters, new IToken.SourceLocation(line, col), 2);
								tokens.add(newToken);
								pos++;
								col += 2;
								state = State.START;
							}
					default->
							{
								Token newToken = new Token(Kind.BANG, characters, new IToken.SourceLocation(line, col), 1);
								tokens.add(newToken);
								col++;
								state = State.START;
							}
				}
			}
			else if (state == State.HAVE_GT)
			{
				switch (ch)
				{
					case '=' ->
							{
								characters += ch;
								Token newToken = new Token(Kind.GE, characters, new IToken.SourceLocation(line, col), 2);
								tokens.add(newToken);
								pos++;
								col += 2;
								state = State.START;
							}
					case '>' ->
							{
								characters += ch;
								Token newToken = new Token(Kind.RANGLE, characters, new IToken.SourceLocation(line, col), 2);
								tokens.add(newToken);
								pos++;
								col += 2;
								state = State.START;
							}
					default->
							{
								Token newToken = new Token(Kind.GT, characters, new IToken.SourceLocation(line, col), 1);
								tokens.add(newToken);
								col++;
								state = State.START;
							}
				}
			}
			else if (state == State.HAVE_LT)
			{
				switch (ch)
				{
					case '=' ->
							{
								characters += ch;
								Token newToken = new Token(Kind.LE, characters, new IToken.SourceLocation(line, col), 2);
								tokens.add(newToken);
								pos++;
								col += 2;
								state = State.START;
							}
					case '<' ->
							{
								characters += ch;
								Token newToken = new Token(Kind.LANGLE, characters, new IToken.SourceLocation(line, col), 2);
								tokens.add(newToken);
								pos++;
								col += 2;
								state = State.START;
							}
					case '-' ->
							{
								characters += ch;
								Token newToken = new Token(Kind.LARROW, characters, new IToken.SourceLocation(line, col), 2);
								tokens.add(newToken);
								pos++;
								col += 2;
								state = State.START;
							}
					default->
							{
								Token newToken = new Token(Kind.LT, characters, new IToken.SourceLocation(line, col), 1);
								tokens.add(newToken);
								col++;
								state = State.START;
							}
				}
			}
			else if (state == State.IN_STRINGLIT)
			{
				if (ch == '\"')
				{
					pos++;
					characters += (ch);
					Token newToken = new Token(Kind.STRING_LIT, characters, new IToken.SourceLocation(line, col), characters.length());
					tokens.add(newToken);
					col += characters.length();
					state = State.START;
				}
				else if (ch == '\\')
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
									Token newToken = new Token(Kind.ERROR, characters, new IToken.SourceLocation(line, col), characters.length());
									tokens.add(newToken);
									col+=characters.length();
									state = State.START;
								}

					}

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
					pos++;
					characters+=(ch);
					if(pos == (input.length()-1) )
					{
						checkIdentifier(characters, line, col);
						col+= characters.length();
						state = State.START;
					}
				}
				else
				{
					checkIdentifier(characters, line, col);
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
		if(characters.length() != 0)
		{
			tokens.add(new Token(Kind.ERROR, input, new IToken.SourceLocation(line,col), characters.length()));
		}
		tokens.add(new Token(Kind.EOF, input, new IToken.SourceLocation(line,col), 0));
	}
	public void checkIdentifier (String input, int line, int col)
	{
		switch(input)
		{
			case "string", "int", "float", "boolean", "color", "image", "void" ->
					{
						Token newToken = new Token(Kind.TYPE, input, new IToken.SourceLocation(line, col), input.length());
						tokens.add(newToken);
					}
			case "getWidth", "getHeight" ->
					{
						Token newToken = new Token(Kind.IMAGE_OP, input, new IToken.SourceLocation(line, col), input.length());
						tokens.add(newToken);
					}
			case "getRed", "getGreen", "getBlue" ->
					{
						Token newToken = new Token(Kind.COLOR_OP, input, new IToken.SourceLocation(line, col), input.length());
						tokens.add(newToken);
					}
			case "true", "false" ->
					{
						Token newToken = new Token(Kind.BOOLEAN_LIT, input, new IToken.SourceLocation(line, col), input.length());
						tokens.add(newToken);
					}
			case  "if" ->
					{
						Token newToken = new Token(Kind.KW_IF, input, new IToken.SourceLocation(line, col), input.length());
						tokens.add(newToken);
					}
			case  "else" ->
					{
						Token newToken = new Token(Kind.KW_ELSE, input, new IToken.SourceLocation(line, col), input.length());
						tokens.add(newToken);
					}
			case  "fi" ->
					{
						Token newToken = new Token(Kind.KW_FI, input, new IToken.SourceLocation(line, col), input.length());
						tokens.add(newToken);
					}
			case  "write" ->
					{
						Token newToken = new Token(Kind.KW_WRITE, input, new IToken.SourceLocation(line, col), input.length());
						tokens.add(newToken);
					}
			case  "console" ->
					{
						Token newToken = new Token(Kind.KW_CONSOLE, input, new IToken.SourceLocation(line, col), input.length());
						tokens.add(newToken);
					}
			case  "BLACK", "BLUE", "CYAN", "DARK_GRAY", "GRAY", "GREEN", "LIGHT_GRAY", "MAGENTA", "ORANGE", "PINK", "RED", "WHITE", "YELLOW" ->
					{
						Token newToken = new Token(Kind.COLOR_CONST, input, new IToken.SourceLocation(line, col), input.length());
						tokens.add(newToken);
					}
			default ->
					{
						Token newToken = new Token(Kind.IDENT, input, new IToken.SourceLocation(line, col), input.length());
						tokens.add(newToken);
					}
		}
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