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

import static edu.ufl.cise.plc.ast.Types.Type.*;

public class TypeCheckVisitor implements ASTVisitor {
	
	SymbolTable symbolTable = new SymbolTable();  
	Program root;
	
	record Pair<T0,T1>(T0 t0, T1 t1){};  //may be useful for constructing lookup tables.
	
	private void check(boolean condition, ASTNode node, String message) throws TypeCheckException {
		if (!condition) {
			throw new TypeCheckException(message, node.getSourceLoc());
		}
	}
	
	//The type of a BooleanLitExpr is always BOOLEAN.  
	//Set the type in AST Node for later passes (code generation)
	//Return the type for convenience in this visitor.  
	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
		booleanLitExpr.setType(Type.BOOLEAN);
		return Type.BOOLEAN;
	}

	@Override
	public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
		  stringLitExpr.setType(Type.STRING);
		    return Type.STRING;
	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
		intLitExpr.setType(Type.INT);
	    return Type.INT;
	}

	@Override
	public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
		floatLitExpr.setType(Type.FLOAT);
		return Type.FLOAT;
	}

	@Override
	public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
		colorConstExpr.setType(Type.COLOR);
		return Type.COLOR;
	}

	@Override
	public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
		consoleExpr.setType(Type.CONSOLE);
		return Type.CONSOLE;
	}
	
	//Visits the child expressions to get their type (and ensure they are correctly typed)
	//then checks the given conditions.
	@Override
	public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
		Type redType = (Type) colorExpr.getRed().visit(this, arg);
		Type greenType = (Type) colorExpr.getGreen().visit(this, arg);
		Type blueType = (Type) colorExpr.getBlue().visit(this, arg);
		check(redType == greenType && redType == blueType, colorExpr, "color components must have same type");
		check(redType == Type.INT || redType == Type.FLOAT, colorExpr, "color component type must be int or float");
		Type exprType = (redType == Type.INT) ? Type.COLOR : Type.COLORFLOAT;
		colorExpr.setType(exprType);
		return exprType;
	}	

	
	
	//Maps forms a lookup table that maps an operator expression pair into result type.  
	//This more convenient than a long chain of if-else statements. 
	//Given combinations are legal; if the operator expression pair is not in the map, it is an error. 
	Map<Pair<Kind,Type>, Type> unaryExprs = Map.of(
			new Pair<Kind,Type>(Kind.BANG,BOOLEAN), BOOLEAN,
			new Pair<Kind,Type>(Kind.MINUS, FLOAT), FLOAT,
			new Pair<Kind,Type>(Kind.MINUS, INT),INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,INT), INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,COLOR), INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,IMAGE), IMAGE,
			new Pair<Kind,Type>(Kind.IMAGE_OP,IMAGE), INT
			);
	
	//Visits the child expression to get the type, then uses the above table to determine the result type
	//and check that this node represents a legal combination of operator and expression type. 
	@Override
	public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws Exception {
		// !, -, getRed, getGreen, getBlue
		Kind op = unaryExpr.getOp().getKind();
		Type exprType = (Type) unaryExpr.getExpr().visit(this, arg);
		//Use the lookup table above to both check for a legal combination of operator and expression, and to get result type.
		Type resultType = unaryExprs.get(new Pair<Kind,Type>(op,exprType));
		check(resultType != null, unaryExpr, "incompatible types for unaryExpr");
		//Save the type of the unary expression in the AST node for use in code generation later. 
		unaryExpr.setType(resultType);
		//return the type for convenience in this visitor.
		return resultType;
	}


	//This method has several cases. Work incrementally and test as you go. 
	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
		Kind op = binaryExpr.getOp().getKind();
		Type leftType = (Type) binaryExpr.getLeft().visit(this, arg);
		Type rightType = (Type) binaryExpr.getRight().visit(this, arg);
		Type resultType = null;
		switch(op) {//AND, OR, PLUS, MINUS, TIMES, DIV, MOD, EQUALS, NOT_EQUALS, LT, LE, GT,GE 
		case AND,OR -> {
			if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) resultType = Type.BOOLEAN;
		}
		case EQUALS,NOT_EQUALS -> {
		check(leftType == rightType, binaryExpr, "incompatible types for comparison");
		resultType = Type.BOOLEAN;
		}
		case PLUS -> {
		if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
		else if (leftType == Type.FLOAT && rightType == Type.FLOAT) resultType = Type.FLOAT;
		else if (leftType == Type.COLOR && rightType == Type.COLOR) resultType = Type.COLOR;
		else if (leftType == Type.COLORFLOAT && rightType == Type.COLORFLOAT) resultType = Type.COLORFLOAT;
		else if (leftType == Type.IMAGE && rightType == Type.IMAGE) resultType = Type.IMAGE;
		else if (leftType == Type.STRING && rightType == Type.STRING) resultType = Type.STRING;
		else if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) resultType = Type.BOOLEAN;
		else if (leftType == Type.INT && rightType == Type.FLOAT) {
			binaryExpr.getLeft().setCoerceTo(FLOAT);
			resultType = Type.FLOAT;
		}
		else if (leftType == Type.FLOAT && rightType == Type.INT) {
			binaryExpr.getRight().setCoerceTo(FLOAT);
			resultType = Type.FLOAT;
		}
		else check(false, binaryExpr, "incompatible types for operator");
		}
		case  MINUS -> {
		if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
		else if (leftType == Type.FLOAT && rightType == Type.FLOAT) resultType = Type.FLOAT;
		else if (leftType == Type.COLOR && rightType == Type.COLOR) resultType = Type.COLOR;
		else if (leftType == Type.COLORFLOAT && rightType == Type.COLORFLOAT) resultType = Type.COLORFLOAT;
		else if (leftType == Type.IMAGE && rightType == Type.IMAGE) resultType = Type.IMAGE;
		else if (leftType == Type.STRING && rightType == Type.STRING) resultType = Type.STRING;
		else if (leftType == Type.INT && rightType == Type.FLOAT) {
			binaryExpr.getLeft().setCoerceTo(FLOAT);
			resultType = Type.FLOAT;
		}
		else if (leftType == Type.FLOAT && rightType == Type.INT) {
			binaryExpr.getRight().setCoerceTo(FLOAT);
			resultType = Type.FLOAT;
		}
		else check(false, binaryExpr, "incompatible types for operator");
		}
		case TIMES -> {
		if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
		else if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) resultType = Type.BOOLEAN;
		else if (leftType == Type.FLOAT && rightType == Type.FLOAT) resultType = Type.FLOAT;
		else if (leftType == Type.COLOR && rightType == Type.COLOR) resultType = Type.COLOR;
		else if (leftType == Type.IMAGE && rightType == Type.INT) resultType = Type.IMAGE;
		else if (leftType == Type.COLORFLOAT && rightType == Type.COLORFLOAT) resultType = Type.COLORFLOAT;
		else if (leftType == Type.IMAGE && rightType == Type.IMAGE) resultType = Type.IMAGE;
		else if (leftType == Type.STRING && rightType == Type.STRING) resultType = Type.STRING;
		else if (leftType == Type.IMAGE && rightType == Type.FLOAT) resultType = Type.IMAGE;
		else if (leftType == Type.INT && rightType == Type.COLOR) resultType = Type.COLOR;
		else if (leftType == Type.FLOAT && rightType == Type.COLOR) resultType = Type.COLORFLOAT;
		else if (leftType == Type.COLOR && rightType == Type.FLOAT) resultType = Type.COLORFLOAT;
		else if (leftType == Type.FLOAT && rightType == Type.INT) resultType = Type.FLOAT;
		else if (leftType == Type.COLOR && rightType == Type.INT) resultType = Type.COLOR;

		else check(false, binaryExpr, "incompatible types for operator");
		}
		case DIV -> {
			if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
			else if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) resultType = Type.BOOLEAN;
			else if (leftType == Type.FLOAT && rightType == Type.FLOAT) resultType = Type.FLOAT;
			else if (leftType == Type.COLOR && rightType == Type.COLOR) resultType = Type.COLOR;
			else if (leftType == Type.IMAGE && rightType == Type.INT) resultType = Type.IMAGE;
			else if (leftType == Type.COLORFLOAT && rightType == Type.COLORFLOAT) resultType = Type.COLORFLOAT;
			else if (leftType == Type.IMAGE && rightType == Type.IMAGE) resultType = Type.IMAGE;
			else if (leftType == Type.STRING && rightType == Type.STRING) resultType = Type.STRING;
			else if (leftType == Type.IMAGE && rightType == Type.FLOAT) resultType = Type.IMAGE;
			else if (leftType == Type.INT && rightType == Type.COLOR) resultType = Type.COLOR;
			else if (leftType == Type.FLOAT && rightType == Type.COLOR) resultType = Type.COLORFLOAT;
			else if (leftType == Type.COLOR && rightType == Type.FLOAT) resultType = Type.COLORFLOAT;
			else if (leftType == Type.FLOAT && rightType == Type.INT) resultType = Type.FLOAT;
			else if (leftType == Type.COLOR && rightType == Type.INT) resultType = Type.COLOR;
		else if (leftType == Type.INT && rightType == Type.FLOAT) {
			binaryExpr.getLeft().setCoerceTo(FLOAT);
			resultType = Type.FLOAT;
		}
		else if (leftType == Type.FLOAT && rightType == Type.INT) {
			binaryExpr.getRight().setCoerceTo(FLOAT);
			resultType = Type.FLOAT;
		}
		else check(false, binaryExpr, "incompatible types for operator");
		}
		case MOD ->
		{
			if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
			else if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) resultType = Type.BOOLEAN;
			else if (leftType == Type.FLOAT && rightType == Type.FLOAT) resultType = Type.FLOAT;
			else if (leftType == Type.COLOR && rightType == Type.COLOR) resultType = Type.COLOR;
			else if (leftType == Type.IMAGE && rightType == Type.INT) resultType = Type.IMAGE;
			else if (leftType == Type.COLORFLOAT && rightType == Type.COLORFLOAT) resultType = Type.COLORFLOAT;
			else if (leftType == Type.IMAGE && rightType == Type.IMAGE) resultType = Type.IMAGE;
			else if (leftType == Type.STRING && rightType == Type.STRING) resultType = Type.STRING;
			else if (leftType == Type.IMAGE && rightType == Type.FLOAT) resultType = Type.IMAGE;
			else if (leftType == Type.INT && rightType == Type.COLOR) resultType = Type.COLOR;
			else if (leftType == Type.FLOAT && rightType == Type.COLOR) resultType = Type.COLORFLOAT;
			else if (leftType == Type.COLOR && rightType == Type.FLOAT) resultType = Type.COLORFLOAT;
			else if (leftType == Type.FLOAT && rightType == Type.INT) resultType = Type.FLOAT;
			else if (leftType == Type.COLOR && rightType == Type.INT) resultType = Type.COLOR;
		}
		case LT, LE, GT, GE -> {
		if (leftType == rightType) resultType = Type.BOOLEAN;
		else if (leftType == Type.INT && rightType == Type.FLOAT) {
			binaryExpr.getLeft().setCoerceTo(FLOAT);
			resultType = Type.BOOLEAN;
		}
		else if (leftType == Type.FLOAT && rightType == Type.INT) {
			binaryExpr.getRight().setCoerceTo(FLOAT);
			resultType = Type.BOOLEAN;
		}
		else check(false, binaryExpr, "incompatible types for operator");
		}
		default -> {
		throw new Exception("compiler error");
		}
		}
		binaryExpr.setType(resultType);
		return resultType;
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
		String name = identExpr.getText();
		Declaration dec = symbolTable.lookup(name);
		check(dec != null, identExpr, "undefined identifier " + name);
		check(dec.isInitialized(), identExpr, "using uninitialized variable");
		identExpr.setDec(dec);  //save declaration--will be useful later. 
		Type type = dec.getType();
		identExpr.setType(type);
		return type;
	}

	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
//		if ((conditionalExpr.getTrueCase().getType() == conditionalExpr.getFalseCase().getType()) && conditionalExpr.getTrueCase().getType() == Type.BOOLEAN
//				&& (conditionalExpr.getCondition() instanceof BooleanLitExpr)) {
//			return conditionalExpr.getTrueCase().getType();
//		}
//		throw new TypeCheckException("bad conditional");
	
//			Expr e = new BooleanLitExpr(conditionalExpr.getCondition().getFirstToken());
//			e.setType(BOOLEAN);
//			conditionalExpr = new ConditionalExpr(conditionalExpr.getCondition().getFirstToken(), e, conditionalExpr.getTrueCase(), conditionalExpr.getFalseCase());
		
//			String name = conditionalExpr.getCondition().getText();
//			Declaration dec = symbolTable.lookup(name);
//			if (dec != null) {
//				Type type = dec.getType();
//				conditionalExpr.getCondition().setType(type);
//				check(dec.getType() == Type.BOOLEAN, conditionalExpr, "type must be boolean");
//			}
//			else {
//				check(conditionalExpr.getCondition() instanceof BooleanLitExpr, conditionalExpr, "type must be boolean"); 
//			}
//		Type trueCaseType = (Type) conditionalExpr.getTrueCase().visit(this,null);
//		Type falseCaseType = (Type) conditionalExpr.getFalseCase().visit(this,null);
//
//		check(trueCaseType == falseCaseType, conditionalExpr, "type must be the same");
//		return trueCaseType;
		
		Type conditionType =(Type) conditionalExpr.getCondition().visit(this, null);
		check(conditionType == Type.BOOLEAN, conditionalExpr, "must be boolean");
		Type trueCaseType = (Type) conditionalExpr.getTrueCase().visit(this,null);
		Type falseCaseType = (Type) conditionalExpr.getFalseCase().visit(this,null);
		check(trueCaseType == falseCaseType, conditionalExpr, "type must be the same");
		conditionalExpr.setType(falseCaseType);
		return falseCaseType;
	}

	@Override
	public Object visitDimension(Dimension dimension, Object arg) throws Exception {
		
		Type widthType = (Type)dimension.getWidth().visit(this, arg);
		check(widthType == Type.INT, dimension, "width not int");
		Type heightType = (Type)dimension.getHeight().visit(this, arg);
		check(heightType == Type.INT, dimension, "height not int");
		return null;
	}

	@Override
	//This method can only be used to check PixelSelector objects on the right hand side of an assignment. 
	//Either modify to pass in context info and add code to handle both cases, or when on left side
	//of assignment, check fields from parent assignment statement.
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		Type xType = (Type) pixelSelector.getX().visit(this, arg);
		check(xType == Type.INT, pixelSelector.getX(), "only ints as pixel selector components");
		Type yType = (Type) pixelSelector.getY().visit(this, arg);
		check(yType == Type.INT, pixelSelector.getY(), "only ints as pixel selector components");
		return null;
	}

	@Override
	//This method several cases--you don't have to implement them all at once.
	//Work incrementally and systematically, testing as you go.  
	public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
		if (assignmentStatement.getExpr() instanceof ColorExpr) {
			assignmentStatement.getExpr().setType(Type.IMAGE);
		}
		Declaration target = symbolTable.lookup(assignmentStatement.getName());
		assignmentStatement.setTargetDec(target);
		Type targetType = target.getType();
		target.setInitialized(true);
		if (targetType == Type.IMAGE && assignmentStatement.getSelector() != null) {
			NameDef namedefX = new NameDef(assignmentStatement.getFirstToken(), "int",assignmentStatement.getSelector().getX().getText());
			namedefX.setInitialized(true);
			NameDef namedefY = new NameDef(assignmentStatement.getFirstToken(), "int" ,assignmentStatement.getSelector().getY().getText());
			namedefY.setInitialized(true);
			symbolTable.insert(assignmentStatement.getSelector().getX().getText(), namedefX);
			symbolTable.insert(assignmentStatement.getSelector().getY().getText(), namedefY);
		}
		
		Type sourceType = (Type) assignmentStatement.getExpr().visit(this, arg);
		
		if (targetType==Type.IMAGE && assignmentStatement.getSelector() != null) {
			symbolTable.remove(assignmentStatement.getSelector().getX().getText());
			symbolTable.remove(assignmentStatement.getSelector().getY().getText());
		}
		
		if (targetType == Type.IMAGE && assignmentStatement.getSelector() == null) {
			if (targetType == Type.INT) {
				assignmentStatement.getExpr().setCoerceTo(COLOR);
			}
			else if (targetType == Type.FLOAT) {
				assignmentStatement.getExpr().setCoerceTo(COLORFLOAT);
			}
			check(assignmentCompatible(sourceType, targetType, "w/o pixel"), assignmentStatement, "w/o pixel"); 

		}
		else {
			PixelSelector pixels = assignmentStatement.getSelector();
		}

		
		//check(sourceType == targetType, assignmentStatement, "incompatible types for comparison");
		
		return null;
		}


	@Override
	public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
		Type sourceType = (Type) writeStatement.getSource().visit(this, arg);
		Type destType = (Type) writeStatement.getDest().visit(this, arg);
		check(destType == Type.STRING || destType == Type.CONSOLE, writeStatement,
				"illegal destination type for write");
		check(sourceType != Type.CONSOLE, writeStatement, "illegal source type for write");
		return null;
	}

	@Override
	public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
		
		  String name = readStatement.getName();
		  Declaration declaration = symbolTable.lookup(name); 
		  readStatement.setTargetDec(declaration);
		  //check(declaration != null, readStatement, "undeclared variable " + name); 
		  Type expressionType= (Type) declaration.getType();
		  Type sourceType = (Type)readStatement.getSource().visit(this,  arg);
		  //check(assignmentCompatible(declaration.getType(), expressionType), readStatement, "incompatible types in assignment");
		  check(readStatement.getSelector() == null, readStatement, "selector");
		  check(sourceType == Type.CONSOLE || sourceType == Type.STRING, readStatement, "string/console");
		  if (sourceType==Type.CONSOLE) {
			  readStatement.getSource().setCoerceTo(expressionType);
		  }
		  readStatement.getTargetDec().setInitialized(true);
		  return expressionType;
		 
	}
	
	private boolean assignmentCompatible(Type targetType, Type rhsType, String typing) {
		switch(typing) {
		case ("varDeclaration") :
			return (targetType == rhsType 
			|| targetType==Type.STRING && rhsType==Type.INT
			|| targetType==Type.STRING && rhsType==Type.BOOLEAN
			|| targetType==Type.IMAGE && rhsType==Type.COLOR
			);
		
		case ("notImage") : 
			return (targetType == rhsType 
			|| targetType==Type.FLOAT && rhsType==Type.INT
			|| targetType==Type.INT && rhsType==Type.FLOAT
			|| targetType==Type.INT && rhsType==Type.COLOR
			|| targetType==Type.COLOR && rhsType==Type.INT
			|| targetType==Type.COLOR && rhsType==Type.IMAGE
			);
		case ("w/o pixel") :
			return (targetType == rhsType 
			|| targetType==Type.IMAGE && rhsType==Type.COLOR
			|| targetType==Type.IMAGE && rhsType==Type.COLORFLOAT
			);
		case ("readStatement") :
			return (targetType==rhsType || rhsType==Type.STRING || rhsType==Type.CONSOLE);
			
		default: 
			return false;
			}
	}
	
	@Override
	public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
		String name = declaration.getName();
		boolean inserted = symbolTable.insert(name,declaration);
		check(inserted, declaration, "variable " + name + "already declared");
		Expr e = declaration.getExpr();
		if (e != null) {
			Type initializerType = (Type) e.visit(this,arg);
			if (initializerType == Type.INT && declaration.getType() == Type.FLOAT) {
				declaration.getExpr().setCoerceTo(FLOAT);
				check(assignmentCompatible(declaration.getType(), declaration.getExpr().getCoerceTo(), "varDeclaration"),declaration, 
						"type of expression and declared type do not match");
			}
			else if (declaration.getType() == Type.COLOR || declaration.getType() == Type.IMAGE 
					) {
				Type t = declaration.getType();

				check(assignmentCompatible(declaration.getType(), initializerType, "readStatement"), declaration, "rhs must be console or string");
			}
			
			else {
				Type t = declaration.getType();
				check(assignmentCompatible(declaration.getType(), initializerType, "varDeclaration"),declaration, 
						"type of expression and declared type do not match");
			}
			declaration.setInitialized(true);
			
		}
		else {
			declaration.getNameDef().visit(this, arg);
			if (declaration.getNameDef().getType() == Type.IMAGE) {
				check(declaration.getNameDef() instanceof NameDefWithDim, declaration, "not namedefwithdim" );
			}
		}
		return declaration.getType();
	}


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {	
		
		List<NameDef> parameters = program.getParams();
		for (NameDef param : parameters) {
			//check(symbolTable.insert(param.getName(), param), param, "namedef");
			check(!(param instanceof NameDefWithDim), param, "checking dimension");
			param.visit(this, arg);
			param.setInitialized(true);
		}
		
		//Save root of AST so return type can be accessed in return statements
		root = program;
		
		//Check declarations and statements
		List<ASTNode> decsAndStatements = program.getDecsAndStatements();
		
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}
		return program;
	}

	@Override
	public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {
		String name = nameDef.getName();
		//check(symbolTable.insert(name, nameDef), nameDef, "name should match");
		symbolTable.insert(name, nameDef);
		return null;
	}

	@Override
	public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
		check(nameDefWithDim.getType() == Type.IMAGE, nameDefWithDim, "dimensions should be image");
		Dimension x = nameDefWithDim.getDim();
		String name = nameDefWithDim.getName();
		check(!(symbolTable.insert(name, nameDefWithDim)), nameDefWithDim, "name should match dim");
		//symbolTable.insert(name, nameDefWithDim);
		x.visit(this, arg);
//check(symbolTable.insert(nameDefWithDim.getName(), nameDefWithDim), nameDefWithDim, "namedefwithdim");
		return null;
	}
 
	@Override
	public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
		Type returnType = root.getReturnType();  //This is why we save program in visitProgram.
		Type expressionType = (Type) returnStatement.getExpr().visit(this, arg);
		check(returnType == expressionType, returnStatement, "return statement with invalid type");
		returnStatement.getExpr().setType(expressionType);
		return returnStatement.getExpr().getType();
	}

	@Override
	public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
		Type expType = (Type) unaryExprPostfix.getExpr().visit(this, arg);
		check(expType == Type.IMAGE, unaryExprPostfix, "pixel selector can only be applied to image");
		unaryExprPostfix.getSelector().visit(this, arg);
		unaryExprPostfix.setType(Type.INT);
		unaryExprPostfix.setCoerceTo(COLOR);
		return Type.COLOR;
	}
	

}
