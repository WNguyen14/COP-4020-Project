package edu.ufl.cise.plc;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.ASTVisitor;
import edu.ufl.cise.plc.ast.AssignmentStatement;
import edu.ufl.cise.plc.ast.BinaryExpr;
import edu.ufl.cise.plc.ast.BooleanLitExpr;
import edu.ufl.cise.plc.ast.ColorConstExpr;
import edu.ufl.cise.plc.ast.ColorExpr;
import edu.ufl.cise.plc.ast.ConditionalExpr;
import edu.ufl.cise.plc.ast.ConsoleExpr;
import edu.ufl.cise.plc.ast.Declaration;
import edu.ufl.cise.plc.ast.Dimension;
import edu.ufl.cise.plc.ast.Expr;
import edu.ufl.cise.plc.ast.FloatLitExpr;
import edu.ufl.cise.plc.ast.IdentExpr;
import edu.ufl.cise.plc.ast.IntLitExpr;
import edu.ufl.cise.plc.ast.NameDef;
import edu.ufl.cise.plc.ast.NameDefWithDim;
import edu.ufl.cise.plc.ast.PixelSelector;
import edu.ufl.cise.plc.ast.Program;
import edu.ufl.cise.plc.ast.ReadStatement;
import edu.ufl.cise.plc.ast.ReturnStatement;
import edu.ufl.cise.plc.ast.StringLitExpr;
import edu.ufl.cise.plc.ast.Types.Type;
import edu.ufl.cise.plc.ast.UnaryExpr;
import edu.ufl.cise.plc.ast.UnaryExprPostfix;
import edu.ufl.cise.plc.ast.VarDeclaration;
import edu.ufl.cise.plc.ast.WriteStatement;

public class CodeGenVisitor implements ASTVisitor {

	private String packageName;
	private StringBuilder code = new StringBuilder();
	public CodeGenVisitor(String packageName) {
		this.packageName = packageName;
		
	}
	
	
	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
		StringBuilder sb = (StringBuilder) arg;
		  sb.append(booleanLitExpr.getText());
		  return sb;
	}

	@Override
	public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
		StringBuilder sb = (StringBuilder) arg;
		  sb.append(stringLitExpr.getText());
		  return sb;
	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
		StringBuilder sb = (StringBuilder) arg;
	  sb.append(intLitExpr.getText());
	  return sb;
	}

	@Override
	public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
		  StringBuilder sb = (StringBuilder) arg;
		  sb.append(floatLitExpr.getText());
		  sb.append("f");
		  return sb;
	}

	@Override
	public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
		StringBuilder sb = (StringBuilder) arg;
		sb.append("ColorTuple.unpack(Color." + colorConstExpr.getText() + ".getRGB())");
		return sb;
	}

	@Override
	public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
		  StringBuilder sb = (StringBuilder) arg;
		  sb.append("(" + boxedType(consoleExpr.getCoerceTo()) + ") ConsoleIO.readValueFromConsole(\"" + consoleExpr.getCoerceTo().toString());
		  sb.append("\", \"Enter integer:\")");
		  return sb;
	}

	@Override
	public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
		StringBuilder sb = (StringBuilder) arg;
		sb.append("new ColorTuple(");
		colorExpr.getRed().visit(this,  sb);
		sb.append(",");
		colorExpr.getGreen().visit(this,  sb);
		sb.append(",");
		colorExpr.getBlue().visit(this,  sb);
		sb.append(")");
		
		return sb;
	}

	@Override
	public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
		  StringBuilder sb = (StringBuilder) arg;
		  StringBuilder test = new StringBuilder();
		  if (unaryExpression.getOp().getText().equals("getRed") 
				  || unaryExpression.getOp().getText().equals("getGreen")
				  || unaryExpression.getOp().getText().equals("getBlue")) {
			  if (unaryExpression.getExpr().getType() == Type.IMAGE && unaryExpression.getOp().getText().equals("getRed")) {
				  sb.append("(ImageOps.extractRed" + "("+ unaryExpression.getExpr().visit(this, test)+ "))");
			  }
			  else if (unaryExpression.getExpr().getType() == Type.IMAGE && unaryExpression.getOp().getText().equals("getGreen")) {
				  sb.append("(ImageOps.extractGreen" + "("+ unaryExpression.getExpr().visit(this, test)+ "))");
			  }
			  else if (unaryExpression.getExpr().getType() == Type.IMAGE && unaryExpression.getOp().getText().equals("getBlue")) {
				  sb.append("(ImageOps.extractBlue" + "("+ unaryExpression.getExpr().visit(this, test)+ "))");
			  }
			  else {
			  sb.append("(ColorTuple." + unaryExpression.getOp().getText()+ "("+ unaryExpression.getExpr().visit(this, test)+ "))");
			  }
		  }
		  else {
		  sb.append("( " + unaryExpression.getOp().getText()+ unaryExpression.getExpr().visit(this, test)+ " )");
		  }
		  return sb;
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
		StringBuilder sb = (StringBuilder) arg;
		Type type = binaryExpr.getType();
		Expr leftExpr = binaryExpr.getLeft();
		Expr rightExpr = binaryExpr.getRight();
		Type leftType = leftExpr.getCoerceTo() != null ? leftExpr.getCoerceTo() : leftExpr.getType();
		Type rightType = rightExpr.getCoerceTo() != null ? rightExpr.getCoerceTo() : rightExpr.getType();
		Kind op = binaryExpr.getOp().getKind();
		if (false) 
		       throw new UnsupportedOperationException("Not implemented");
		else {
			sb.append("(");
			if ((binaryExpr.getLeft().getCoerceTo() == Type.COLOR && binaryExpr.getRight().getCoerceTo() == Type.COLOR ) 
					|| binaryExpr.getLeft().getType() == Type.COLOR && binaryExpr.getRight().getType() == Type.COLOR)
			{
				sb.append("ImageOps.binaryTupleOp(" + binaryExpr.getOp().getKind().toString() + "," + binaryExpr.getLeft().getText() + "," + binaryExpr.getRight().getText() + "))");
				//sb.append("ImageOps.binaryTupleOp(" + binaryExpr.getOp().getKind().toString() +", getColorTuple("+ binaryExpr.getLeft().getText() + ""+ "," + binaryExpr.getRight().getText() + "))");
			}
			 
			else {
				binaryExpr.getLeft().visit(this, sb);
				sb.append(binaryExpr.getOp().getText());
				binaryExpr.getRight().visit(this, sb);
				sb.append(")");
			}
		}
//		if (binaryExpr.getCoerceTo() != type) {
//		   genTypeConversion(type, binaryExpr.getCoerceTo(), sb);
//		}
		return sb;
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
		  StringBuilder sb = (StringBuilder) arg;
		  sb.append(identExpr.getText());
		  return sb;
	}

	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
		StringBuilder sb = (StringBuilder) arg;
		sb.append("(");
		conditionalExpr.getCondition().visit(this,  sb);
		sb.append(")?\n");
		sb.append("(");
		conditionalExpr.getTrueCase().visit(this,  sb);
		sb.append("):\n");
		sb.append("(");
		conditionalExpr.getFalseCase().visit(this,  sb);
		sb.append(")");
		
		return sb;
	}

	@Override
	public Object visitDimension(Dimension dimension, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
		  StringBuilder sb = (StringBuilder) arg;
		  if (assignmentStatement.getExpr().getType() == Type.COLOR || assignmentStatement.getExpr() instanceof ColorExpr || assignmentStatement.getExpr().getType() == Type.INT) {
			  sb.append("for(int ");
			  String x = assignmentStatement.getSelector().getX().getText();
			  String y = assignmentStatement.getSelector().getY().getText();
			  sb.append(x + "=0; " + x+"<" + assignmentStatement.getName() + ".getWidth(); " + x +"++)" + "\n");
			  sb.append("\tfor(int ");
			  sb.append(y + "=0; " + y+"<" + assignmentStatement.getName() + ".getHeight(); " + y +"++)\n");
			  sb.append("\t\tImageOps.setColor(" + assignmentStatement.getName() + "," + x + "," + y + ",");
			  if (assignmentStatement.getExpr() instanceof BinaryExpr) {
				  BinaryExpr binaryExpr = ((BinaryExpr)(assignmentStatement).getExpr());
					sb.append("ImageOps.binaryTupleOp(" + binaryExpr.getOp().getKind().toString() +
							", ImageOps.getColorTuple("+ binaryExpr.getLeft().getText() + ","+ x + "," + y + ")"
							+ ", ImageOps.getColorTuple("+ binaryExpr.getRight().getText() + "," + x + "," + y + ")));\n");

			  }
			  else {
			  assignmentStatement.getExpr().visit(this, sb);
			  sb.append(");\n");
			  }
		  }
		  else {
			  sb.append(assignmentStatement.getName() + " = ");
			  assignmentStatement.getExpr().visit(this, sb);
			  sb.append(";\n");
		  }
		  return sb;
	}

	@Override
	public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
		StringBuilder sb = (StringBuilder) arg;
		if (writeStatement.getSource().getType() == Type.IMAGE) {
			sb.append("ConsoleIO.displayImageOnScreen(");
		}
		else {
			sb.append("ConsoleIO.console.println(");
		}
		Expr expr = writeStatement.getSource();
		expr.visit(this, sb);
		sb.append(");\n");
		return sb;
	}

	@Override
	public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
		  StringBuilder sb = (StringBuilder) arg;
		  sb.append(readStatement.getName() + " = ");
		  Expr expr = readStatement.getSource();
		  expr.visit(this, sb);
		  sb.append(";\n");
		  return sb;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		//program.visit(CompilerComponentFactory.getTypeChecker(), arg);
		code.append("package "+ packageName + ";\n");
		Set<String> imports = new HashSet<String>();
		//imports go here
		for (int i = 0; i < program.getParams().size(); i++) {
			if (program.getParams().get(i).getType() == Type.IMAGE) {
				imports.add("import java.awt.image.BufferedImage");
			}
			if (program.getParams().get(i).getType() == Type.COLOR) {
				imports.add("import edu.ufl.cise.plc.runtime.ColorTuple");
			}
		}
		for(ASTNode node : program.getDecsAndStatements()) {
			if (node instanceof ReadStatement) {
				imports.add("import edu.ufl.cise.plc.runtime.ConsoleIO");
				//code.append("import edu.ufl.cise.plc.runtime.ConsoleIO;\n");
			}
			if (node instanceof AssignmentStatement) {
				if (((AssignmentStatement)node).getExpr().getType() == Type.COLOR) {
					imports.add("import java.awt.Color");
					imports.add("import edu.ufl.cise.plc.runtime.ColorTuple");
					imports.add("import edu.ufl.cise.plc.runtime.ColorTupleFloat");

					imports.add("import edu.ufl.cise.plc.runtime.ImageOps");
					imports.add("import static edu.ufl.cise.plc.runtime.ImageOps.OP.*");

//					code.append("import java.awt.Color;");
//					code.append("import edu.ufl.cise.plc.runtime.ColorTuple;\n");
//					code.append("import edu.ufl.cise.plc.runtime.ImageOps;\n");
				}
				

			}
			if (node instanceof VarDeclaration) {
				if ( ((VarDeclaration)node).getType() == Type.IMAGE) {
					if (((VarDeclaration)node).getExpr() != null && ((VarDeclaration)node).getExpr().getType() == Type.STRING) {
						imports.add("import edu.ufl.cise.plc.runtime.FileURLIO");

						//code.append("import edu.ufl.cise.plc.runtime.FileURLIO;\n");
					}
					if (((VarDeclaration)node).getExpr() instanceof BinaryExpr) {
						imports.add("import static edu.ufl.cise.plc.runtime.ImageOps.OP.*");
						imports.add("import edu.ufl.cise.plc.runtime.ImageOps");

					}

					imports.add("import java.awt.image.BufferedImage");
					imports.add("import java.awt.Color");
					imports.add("import edu.ufl.cise.plc.runtime.ColorTupleFloat");
					imports.add("import edu.ufl.cise.plc.runtime.ColorTuple");

					imports.add("import edu.ufl.cise.plc.runtime.ImageOps");
					imports.add("import static edu.ufl.cise.plc.runtime.ImageOps.OP.*");
					//code.append("import java.awt.image.BufferedImage;\n");
				}
				if ( ((VarDeclaration)node).getType() == 
						Type.COLOR) {
					imports.add("import static edu.ufl.cise.plc.runtime.ImageOps.BoolOP.*");

					imports.add("import java.awt.Color");
					imports.add("import edu.ufl.cise.plc.runtime.ColorTuple");
					imports.add("import edu.ufl.cise.plc.runtime.ColorTupleFloat");

					imports.add("import edu.ufl.cise.plc.runtime.ImageOps");
					imports.add("import static edu.ufl.cise.plc.runtime.ImageOps.OP.*");
				}

			}
			
			if (node instanceof WriteStatement) {
					imports.add("import edu.ufl.cise.plc.runtime.ConsoleIO");
			}
			
		}
		for (String i : imports) {
			code.append(i + ";\n");
		}
		code.append("public class " + program.getName() + " {\n");
		if (program.getReturnType() == Type.STRING) {
			code.append("\tpublic static String"+ " apply(");

		}
		else {
		code.append("\tpublic static " + fixType(program.getReturnType())+ " apply(");
		}
		for (int i = 0; i < program.getParams().size(); i++) {
			code.append(fixType(program.getParams().get(i).getType()) + " " + (program.getParams().get(i).getName()));
			if (i < program.getParams().size() - 1) {
				code.append(" , ");
			}
		}
		code.append("){\n");
		//code.append(decs.getText());
		for (ASTNode node : program.getDecsAndStatements()) {
			code.append(genCode(node, packageName));
		}
		code.append("\n");
	
		code.append("}\n");
		code.append("}");
		return this.code.toString();

		
	}
	
	private String genCode(ASTNode decoratedAST, String packageName) throws Exception {
		  ASTVisitor v = CompilerComponentFactory.getCodeGenerator(packageName);
		  StringBuilder test = new StringBuilder();
		  String code = decoratedAST.visit(v, test).toString();
		  return code;
		}

	@Override
	public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
		  StringBuilder sb = (StringBuilder) arg;
		  Expr expr = returnStatement.getExpr();
		  sb.append("return ");
		  expr.visit(this, sb);
		  sb.append(";");
		  sb.append("\n");
		  return sb;
	}

	@Override
	public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
		  StringBuilder sb = (StringBuilder) arg;
		  
		  sb.append(fixType(declaration.getType()) + " ");
		 
		  sb.append(declaration.getName());
		  Expr expr = declaration.getExpr();
		  if (expr != null) {
			  sb.append(" = ");
			  if (declaration.getType() == Type.IMAGE && declaration.getExpr() instanceof BinaryExpr) {
				  sb.append("ImageOps.binaryImageScalarOp(" + ((BinaryExpr)declaration.getExpr()).getOp().getKind().toString() + "," +  ((BinaryExpr)declaration.getExpr()).getLeft().getText() + "," +  ((BinaryExpr)declaration.getExpr()).getRight().getText() + ")");
			  }
			  else if (declaration.getType() == Type.IMAGE) {
				  
				  if (declaration.getExpr().getType() != Type.IMAGE) {
					  sb.append("FileURLIO.readImage");
					  sb.append("(");
					  expr.visit(this, sb);
					  
							 
					  sb.append(",");
					  if (declaration.getNameDef().getDim() != null) {
						  sb.append(declaration.getNameDef().getDim().getWidth().getText());
						  sb.append(",");
						  sb.append(declaration.getNameDef().getDim().getHeight().getText());				  
	
					  }
					  else {
						  sb.append("null");
						  sb.append(",");
						  sb.append("null");				  
	
					  }
					  sb.append(")");
				  }
				  else {
					  expr.visit(this, sb);
				  }
				  
			  }
			  else {
				  expr.visit(this, sb);
			  }
		  }
		  else if (declaration.getType() == Type.IMAGE) {
			  sb.append(" = ");
			  sb.append("new BufferedImage(");
			  sb.append(declaration.getDim().getWidth().getText());
			  sb.append(",");
			  sb.append(declaration.getDim().getHeight().getText());
			  sb.append(",");
			  sb.append("BufferedImage.TYPE_INT_RGB)");
		  }
		  sb.append(";\n");
		  
		  return sb;
	}

	@Override
	public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
		StringBuilder sb = (StringBuilder) arg;
		sb.append("ImageOps.getColorTuple(" + unaryExprPostfix.getText() 
		+ "," + unaryExprPostfix.getSelector().getX().getText()
		+ "," + unaryExprPostfix.getSelector().getY().getText() + ")");
		return sb;
	}
	
	public String fixType(Type type) {
		if (type == Type.STRING) {
			return "String";
		}
		else if (type == Type.IMAGE) {
			return "BufferedImage";
		}
		else if (type == Type.COLOR) {
			return "ColorTuple";
		}
		else {
			return type.toString().toLowerCase();
		}
	}
	
	public String boxedType(Type type) {
		if (type == Type.INT) {
			return "Integer";
		}
		else if (type == Type.BOOLEAN) {
			return "Boolean";
		}
		else if (type == Type.STRING) {
			return "String";
		}
		else if (type == Type.FLOAT) {
			return "Float";
		}
		else {
			return "boxed type";
		}
	}
	
	

}
