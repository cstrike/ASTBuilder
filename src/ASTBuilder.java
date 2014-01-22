import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

public class ASTBuilder {
	public static void main(String[] args){
		String content =  "public class Main {" +
				"public static void main(String[] args) {" +
				"System.out.println(\"Hello World!\");" +
				"} " +
				"public void distinctPrimarySum(String... numbers) {"+
				" List<String> l = Arrays.asList(numbers);"+
				" int sum = l.stream()"+
				" .map(e -> new Integer(e))"+
				" .filter(e -> Primes.isPrime(e))"+
				" .distinct()"+
				" .reduce(0, (x,y) -> x+y); "+
				" System.out.println(\"distinctPrimarySum result is: \" + sum);"+
				"}"+
				"}";
		// Initialize ASTParser
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(content.toCharArray()); 		  //content is a string which stores the java source
		parser.setResolveBindings(true);
		CompilationUnit result = (CompilationUnit) parser.createAST(null);
		
		//show import declarations in order
		List importList =result.imports();  
		System.out.println("import:");
		for(Object obj : importList) {
			ImportDeclaration importDec = (ImportDeclaration)obj;
			System.out.println(importDec.getName());
		}

		//show class name
		List types = result.types();  
		TypeDeclaration typeDec = (TypeDeclaration) types.get(0); 
		System.out.println("className:"+typeDec.getName());

		//show fields
		FieldDeclaration fieldDec[]=typeDec.getFields();
		System.out.println("Fields:");
		for(FieldDeclaration field: fieldDec)
		{
			System.out.println("Field fragment:"+field.fragments());
			System.out.println("Field type:"+field.getType());
		}

		//show methods
		MethodDeclaration methodDec[] = typeDec.getMethods();  
		System.out.println("Method:");  
		for (MethodDeclaration method : methodDec)  
		{  
			//get method name
			SimpleName methodName=method.getName();
			System.out.println("method name:"+methodName);

			//get method parameters
			List param=method.parameters();
			System.out.println("method parameters:"+param);

			//get method return type
			Type returnType=method.getReturnType2();
			System.out.println("method return type:"+returnType);
			//get method body
			Block body=method.getBody();
			List statements=body.statements();   

			//get the statements of the method body
			Iterator iter=statements.iterator();
			while(iter.hasNext())
			{
				//get each statement
				Statement stmt=(Statement)iter.next();

				if(stmt instanceof ExpressionStatement)
				{
					ExpressionStatement expressStmt=(ExpressionStatement) stmt;
					Expression express=expressStmt.getExpression();

					if(express instanceof Assignment)
					{
						Assignment assign=(Assignment)express;
						System.out.println("LHS:"+assign.getLeftHandSide()+"; ");
						System.out.println("Op:"+assign.getOperator()+"; ");
						System.out.println("RHS:"+assign.getRightHandSide());

					}
					else if(express instanceof MethodInvocation)
					{
						MethodInvocation mi=(MethodInvocation) express;
						System.out.println("invocation name:"+mi.getName());
						System.out.println("invocation exp:"+mi.getExpression());
						System.out.println("invocation arg:"+mi.arguments());

					}
					else if(express instanceof LambdaExpression)
					{
						//Never enter this
						LambdaExpression le=(LambdaExpression)express;
						ASTNode astnode = le.getBody();
					}
					System.out.println();

				}

				else if(stmt instanceof IfStatement)
				{
					IfStatement ifstmt=(IfStatement) stmt;
					InfixExpression wex=(InfixExpression) ifstmt.getExpression();
					System.out.println("if-LHS:"+wex.getLeftOperand()+"; ");
					System.out.println("if-op:"+wex.getOperator()+"; ");
					System.out.println("if-RHS:"+wex.getRightOperand());
					System.out.println();
				}

				else if(stmt instanceof VariableDeclarationStatement)
				{
					VariableDeclarationStatement var=(VariableDeclarationStatement) stmt;
					System.out.println("Type of variable:"+var.getType());
					System.out.println("Name of variable:"+var.fragments());
					//should add something to parse lambdaExpression
					List<ASTNode> fragmentList = ((VariableDeclarationStatement) stmt).fragments();
					VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragmentList.get(0);
					Expression exp = fragment.getInitializer();
					MethodInvocation methodInvocation = (MethodInvocation) exp;
					List argList = methodInvocation.arguments();
					for (Object arg : argList){
						if (arg instanceof LambdaExpression){
							System.out.println(arg);
							System.out.println(((LambdaExpression) arg).parameters());
							System.out.println(((LambdaExpression) arg).getBody());
						}
					}
					System.out.println();

				}

				else if(stmt instanceof ReturnStatement)
				{
					ReturnStatement rtstmt=(ReturnStatement) stmt;
					System.out.println("return:"+rtstmt.getExpression());
					System.out.println();
				}

			}
		}
	}
}
