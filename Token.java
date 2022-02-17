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
		if (kind == Kind.INT_LIT)
		{
			return parseInt(input);
		}
		else
		{
			//error
			return 0;
		}
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
	
	//stolen from stackoverflow
	public String unescapeJavaString(String st) {

	    StringBuilder sb = new StringBuilder(st.length());

	    for (int i = 0; i < st.length(); i++) {
	        char ch = st.charAt(i);
	        if (ch == '\\') {
	            char nextChar = (i == st.length() - 1) ? '\\' : st
	                    .charAt(i + 1);
	            switch (nextChar) {
	            case '\\':
	                ch = '\\';
	                break;
	            case 'b':
	                ch = '\b';
	                break;
	            case 'f':
	                ch = '\f';
	                break;
	            case 'n':
	                ch = '\n';
	                break;
	            case 'r':
	                ch = '\r';
	                break;
	            case 't':
	                ch = '\t';
	                break;
	            case '\"':
	                ch = '\"';
	                break;
	            case '\'':
	                ch = '\'';
	                break;	            
	            }
	            i++;
	        }
	        sb.append(ch);
	    }
	    return sb.toString();
	}
	
	@Override public String getStringValue() //need to implement delimiters and escape sequences
	{
		String newString = unescapeJavaString(input);
		newString = newString.substring(1,newString.length()-1);
		System.out.println(newString);
		return newString;
	}
	
	
	
	

}
