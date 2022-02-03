package edu.ufl.cise.plc;

import edu.ufl.cise.plc.IToken.Kind;

import java.util.ArrayList;

public class Lexer implements ILexer{
	private enum State
	{
		START,
		IN_IDENT,
		HAVE_ZERO,
		HAVE_DOT,
		IN_FLOAT,
		IN_NUM,
		HAVE_EQ,
		HAVE_MINUS
	}
	
	public Lexer(String input)
	{
		int pos = 0;
		int startPos = 0;
		int len = 0;
		ArrayList<Token> tokens = new ArrayList<Token>(); //arraylist to store tokens?
		State state = State.START;
		while (true)
		{
			char[] chars = input.toCharArray();
			char ch = chars [pos];
			if (state == State.START) //START CASE
			{
				startPos = pos;
				switch(ch) //issue: if there is no 0, then we are stuck?
				{
					case ' ', '\t', '\n', '\r' -> 
					{
						pos++;
					}
					case '+' -> 
					{
						Token newToken = new Token(Kind.PLUS, "" ,startPos, 1); 
						//we don't need to put the real input value in the token, we can derive it from startPos and the length
						tokens.add(newToken);
						pos++;
					}
					case '*' -> 
					{
						Token newToken = new Token(Kind.TIMES, "" ,startPos, 1); 
						tokens.add(newToken);
						pos++;
					}
					case '=' ->
					{
						state = State.HAVE_EQ; 
						pos++;
					}
					case '-' ->
					{
						state = State.HAVE_MINUS;
						pos++;
					}
					case '1','2','3','4','5','6','7','8','9' ->
					{
						state = State.IN_NUM;
						pos++;
					}
					case '0' ->
					{
						state = State.HAVE_ZERO;
						pos++;
					}
					case 'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','$','_'  ->
					{
						state = State.IN_IDENT;
						pos++;
					}
					
					case 0 ->
					{
						// instructions say to add an EOF token?
						return;
					}
				
				}
				
			}
			else if (state == State.HAVE_EQ)
			{
				switch (ch)
				{
					case '=' ->
					{
						Token newToken = new Token(Kind.EQUALS, "", startPos, 2);
						tokens.add(newToken);
						pos++;
						state = State.START;
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
				ArrayList<Character> numbers = new ArrayList<Character>(); //store the numbers as we go along in this character array
				switch(ch)
				{
					
					case '0','1','2','3','4','5','6','7','8','9' ->
					{
						tokenPos++;
						pos++;
						numbers.add(ch);
					}
					default ->
					{
						Token newToken = new Token(Kind.INT_LIT,"", tokenPos, pos-tokenPos);
						tokens.add(newToken);
						state = State.START;
					}
				}
			}
			else if (state == State.HAVE_MINUS)
			{
				switch (ch)
				{
				case '>' ->
				{
					Token newToken = new Token(Kind.RARROW, "", startPos, 2);
					tokens.add(newToken);
					pos++;
					state = State.START;

				}
				default ->
				{
					//throw exception
					state = State.START;
					return;
				}
				
				}
				
			}
			else if (state == State.HAVE_ZERO)
			{
				switch(ch)
				{
				case '.' ->
				{
					state = State.HAVE_DOT;
				}
				default ->
				{
					Token newToken = new Token(Kind.INT_LIT, "", startPos, 1);
					tokens.add(newToken);
					pos++;
					state = State.START;

				}
				}
			}
			else if (state == State.HAVE_DOT)
			{
				int tokenPos = 0;
				ArrayList<Character> numbers = new ArrayList<Character>(); //store the numbers as we go along in this character array
				switch (ch)
				{
				case '0','1','2','3','4','5','6','7','8','9' ->
				{
					tokenPos++;
					pos++;
					numbers.add(ch);
				}
				default ->
				{
					Token newToken = new Token(Kind.FLOAT_LIT, "", tokenPos, pos-tokenPos);
					tokens.add(newToken);
					state = State.START;
				}
				}
			}
			else if (state == State.IN_IDENT)
			{
				int tokenPos = 0;
				//ArrayList<Character> numbers = new ArrayList<Character>(); //store the numbers as we go along in this character array
				switch(ch)
				{
					
					case 'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','$','_','0','1','2','3','4','5','6','7','8','9'  ->
					{
						tokenPos++;
						pos++;
						//numbers.add(ch);
					}
					default ->
					{
						Token newToken = new Token(Kind.IDENT,"", tokenPos, pos-tokenPos);
						tokens.add(newToken);
						state = State.START;
					}
				}
			}
			
			
		}
	}
	

}
