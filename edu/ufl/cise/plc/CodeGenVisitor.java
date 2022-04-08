package edu.ufl.cise.plc;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
		  StringBuilder sb = (StringBuilder) arg;
		  StringBuilder test = new StringBuilder();

		  sb.append("( " + unaryExpression.getOp().getText()+ unaryExpression.getExpr().visit(this, test)+ " )");
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
			binaryExpr.getLeft().visit(this, sb);
			sb.append(binaryExpr.getOp().getText());
			binaryExpr.getRight().visit(this, sb);
			sb.append(")");
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
		// TODO Auto-generated method stub
		return null;
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
		  sb.append(assignmentStatement.getName() + " = ");
		  assignmentStatement.getExpr().visit(this, sb);
		  sb.append(";\n");
		  return sb;
	}

	@Override
	public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
		StringBuilder sb = (StringBuilder) arg;
		sb.append("ConsoleIO.console.println(");
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
		//imports go here
		for(ASTNode node : program.getDecsAndStatements()) {
			if (node instanceof ReadStatement) {
				code.append("import edu.ufl.cise.plc.runtime.ConsoleIO;\n");
			}
		}
		code.append("public class " + program.getName() + " {\n");
		if (program.getReturnType() == Type.STRING) {
			code.append("\tpublic static String"+ " apply(");

		}
		else {
		code.append("\tpublic static " + program.getReturnType().toString().toLowerCase()+ " apply(");
		}
		for (int i = 0; i < program.getParams().size(); i++) {
			code.append(isString(program.getParams().get(i).getType()) + " " + (program.getParams().get(i).getName()));
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
		  
		  sb.append(isString(declaration.getType()) + " ");
		  sb.append(declaration.getName());
		  Expr expr = declaration.getExpr();
		  if (expr != null) {
			  sb.append(" = ");
			  expr.visit(this, sb);
		  }
		  sb.append(";\n");
		  
		  return sb;
	}

	@Override
	public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String isString(Type type) {
		if (type == Type.STRING) {
			return "String";
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
