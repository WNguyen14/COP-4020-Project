package edu.ufl.cise.plc;

import java.lang.reflect.Type;

import static java.lang.Integer.parseInt;

public class Token implements IToken {
	
	final Kind kind;
	final String input;
	final SourceLocation pos;
	final int length;
	
	public Token(Kind kind, String input, SourceLocation pos, int length)
	{
		this.kind = kind;
		this.input = input;
		this.pos = pos;
		this.length = length;
		
		
	}
	
	@Override public Kind getKind() 
	{
		return kind;
	}
	
	@Override public String getText()
	{
		return input;
	}
	
	@Override public SourceLocation getSourceLocation() {return pos;}
	
	@Override public int getIntValue()
	{
		return parseInt(input);
	}
	
	@Override public float getFloatValue()
	{
		if (kind == Kind.FLOAT_LIT)
		{
			return Float.parseFloat(input);
		}
		else
		{
			//error
			return 0;
		}
	}
	
	@Override public boolean getBooleanValue()
	{
		if (kind == Kind.INT_LIT)
		{
			return Boolean.parseBoolean(input);
		}
		else
		{
			//error
			return false;
		}
	}
	
	@Override public String getStringValue() //need to implement delimiters and escape sequences
	{
		return input;
	}
	
	
	

}
