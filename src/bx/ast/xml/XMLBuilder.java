package bx.ast.xml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;

public class XMLBuilder extends ASTVisitor {

	public Document document;
	public Map<ASTNode, Element> elements;

	public XMLBuilder(){
		super();
		document = DocumentHelper.createDocument();
		elements = new HashMap<ASTNode, Element>();
	}

	//Declarations
	@Override
	public boolean visit(CompilationUnit node){
		//Add root to the elements. If the node is not root, it has already been added.
		Element compilationUnitElement = document.addElement("compilationUnit");
		elements.put(node, compilationUnitElement);

		//Add the children of the node to elements.
		PackageDeclaration packageDeclaration = (PackageDeclaration) node.getStructuralProperty(CompilationUnit.PACKAGE_PROPERTY);
		if (packageDeclaration != null){
			Element packageDeclarationElement = compilationUnitElement.addElement("packageDeclaration");
			elements.put(packageDeclaration, packageDeclarationElement);
		}

		List<ImportDeclaration> importDeclarationList = (List<ImportDeclaration>) node.getStructuralProperty(CompilationUnit.IMPORTS_PROPERTY);
		Iterator<ImportDeclaration> itImports = importDeclarationList.iterator();
		while(itImports.hasNext()){
			ImportDeclaration importDeclaration = itImports.next();
			Element importDeclarationElement = compilationUnitElement.addElement("importDeclaration");
			elements.put(importDeclaration, importDeclarationElement);
		}

		List<TypeDeclaration> typeDeclarationList = (List<TypeDeclaration>) node.getStructuralProperty(CompilationUnit.TYPES_PROPERTY);
		Iterator<TypeDeclaration> itTypes = typeDeclarationList.iterator();
		while(itTypes.hasNext()){
			TypeDeclaration typeDeclaration = itTypes.next();
			Element typeDeclarationElement = compilationUnitElement.addElement("typeDeclaration");
			elements.put(typeDeclaration, typeDeclarationElement);
		}

		return super.visit(node);		
	}
	@Override
	public boolean visit(PackageDeclaration node) {
		Element packageDeclarationElement = elements.get(node);
		if(packageDeclarationElement != null){
			Name name=(Name)node.getStructuralProperty(PackageDeclaration.NAME_PROPERTY);
			Element nameElement = packageDeclarationElement.addElement("name");
			elements.put(name,nameElement);
			nameVisit(name,nameElement);
		}
		return super.visit(node);
	}
	@Override
	public boolean visit(ImportDeclaration node) {
		Element importDeclarationElement = elements.get(node);
		if(importDeclarationElement != null){
			importDeclarationElement.addAttribute("onDemand",""+node.isOnDemand());
			importDeclarationElement.addAttribute("static",""+node.isStatic());
			Name name=(Name)node.getStructuralProperty(ImportDeclaration.NAME_PROPERTY);
			Element nameElement = importDeclarationElement.addElement("name");
			elements.put(name,nameElement);
			nameVisit(name,nameElement);
		}
		return super.visit(node);
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(TypeDeclaration node) {
		Element typeDeclaration = elements.get(node);

		typeDeclaration.addAttribute("interface", ""+node.isInterface());

		//get modifiers
		List<Modifier> modifierList = (List<Modifier>) node.getStructuralProperty(TypeDeclaration.MODIFIERS2_PROPERTY);
		if(modifierList != null){
			Iterator<Modifier> itMod = modifierList.iterator();
			while(itMod.hasNext()){
				Modifier modifier = itMod.next();
				Element modifierElement = typeDeclaration.addElement("modifier");
				elements.put(modifier, modifierElement);
			}
		}

		//get simpleName
		SimpleName sname = (SimpleName)node.getStructuralProperty(TypeDeclaration.NAME_PROPERTY);
		Element snameElement = typeDeclaration.addElement("simpleName");
		elements.put(sname, snameElement);

		//get type parameters
		List<TypeParameter> typeParameterList = (List<TypeParameter>)node.getStructuralProperty(TypeDeclaration.TYPE_PARAMETERS_PROPERTY);
		Iterator<TypeParameter> itTPM = typeParameterList.iterator();
		while(itTPM.hasNext()){
			TypeParameter tpm = itTPM.next();
			Element tpmElement = typeDeclaration.addElement("typeParameter");
			elements.put(tpm, tpmElement);
		}

		//get super class
		if(node.isInterface()){
			//for interface, multiple super classes and no implements
			List<Type> itypeList = (List<Type>)node.getStructuralProperty(TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY);
			if(itypeList != null){
				Iterator<Type> itTP = itypeList.iterator();
				while(itTP.hasNext()){
					Type tp = itTP.next();
					Element tpElement =  typeDeclaration.addElement("extendsType").addElement("tp");
					elements.put(tp,tpElement);
					tpVisit(tp, tpElement);
				}
			}
		}else{
			Type type = (Type)node.getStructuralProperty(TypeDeclaration.SUPERCLASS_TYPE_PROPERTY);
			if(type != null){
				Element typeElement = typeDeclaration.addElement("extendsType").addElement("tp");
				elements.put(type, typeElement);
				tpVisit(type, typeElement);
			}

			//get implement interfaces
			List<Type> typeList = (List<Type>)node.getStructuralProperty(TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY);
			if(typeList != null){
				Iterator<Type> itTP = typeList.iterator();
				while(itTP.hasNext()){
					Type tp = itTP.next();
					Element tpElement =  typeDeclaration.addElement("implementsType").addElement("tp");
					elements.put(tp,tpElement);
					tpVisit(tp, tpElement);
				}
			}
		}

		//get body
		List<BodyDeclaration> bodyDeclarationList = (List<BodyDeclaration>) node.getStructuralProperty(TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		Iterator<BodyDeclaration> itBody = bodyDeclarationList.iterator();
		while(itBody.hasNext()){
			BodyDeclaration body = itBody.next();
			Element bodyElement = typeDeclaration.addElement("bodyDeclaration");
			bodyElement.addAttribute("id", "");
			elements.put(body, bodyElement);
			bodyVisit(body, bodyElement);
		}
		return super.visit(node);
	}
	public void bodyVisit(BodyDeclaration body,Element bodyElement){
		BodyDeclaration bd = null;
		if(body instanceof MethodDeclaration){
			bd = (MethodDeclaration)body;
		}else if(body instanceof Initializer){
			bd = (Initializer)body;
		}else if(body instanceof FieldDeclaration){
			bd = (FieldDeclaration)body;
		}else if(body instanceof AbstractTypeDeclaration){
			bd = (AbstractTypeDeclaration)body;
		}else if(body instanceof AnnotationTypeDeclaration){
			bd = (AnnotationTypeDeclaration)body;
		}else if(body instanceof EnumConstantDeclaration){
			bd = (EnumConstantDeclaration)body;
		}else if(body instanceof TypeDeclaration){
			bd = (TypeDeclaration)body;
		}
		if(bd !=null){
			String kind = bd.getClass().getSimpleName().substring(0,1).toLowerCase()+bd.getClass().getSimpleName().substring(1);
			bodyElement.addAttribute("kind", kind);
			Element bdElement = bodyElement.addElement(kind);
			elements.put(bd, bdElement);
		}
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		Element methodDeclarationElement = elements.get(node);
		if(methodDeclarationElement != null){
			methodDeclarationElement.addAttribute("constructor", ""+node.isConstructor());
			methodDeclarationElement.addAttribute("extraDimensions", ""+node.getExtraDimensions());

			//get modifiers
			List<Modifier> modifierList = (List<Modifier>) node.getStructuralProperty(MethodDeclaration.MODIFIERS2_PROPERTY);
			if(modifierList != null){
				Iterator<Modifier> itMod = modifierList.iterator();
				while(itMod.hasNext()){
					Modifier modifier = itMod.next();
					Element modifierElement = methodDeclarationElement.addElement("modifier");
					elements.put(modifier, modifierElement);
				}
			}

			//get simpleName
			SimpleName sname = (SimpleName)node.getStructuralProperty(MethodDeclaration.NAME_PROPERTY);
			Element snameElement = methodDeclarationElement.addElement("simpleName");
			elements.put(sname, snameElement);

			//get type parameters
			List<TypeParameter> typeParameterList = (List<TypeParameter>)node.getStructuralProperty(MethodDeclaration.TYPE_PARAMETERS_PROPERTY);
			Iterator<TypeParameter> itTPM = typeParameterList.iterator();
			while(itTPM.hasNext()){
				TypeParameter tpm = itTPM.next();
				Element tpmElement = methodDeclarationElement.addElement("typeParameter");
				elements.put(tpm, tpmElement);
			}

			//if not constructor, get return type
			if(!node.isConstructor()){
				Type returnType = (Type)node.getStructuralProperty(MethodDeclaration.RETURN_TYPE2_PROPERTY);
				Element rtypeElement = methodDeclarationElement.addElement("returnType").addElement("tp");
				elements.put(returnType, rtypeElement);
				tpVisit(returnType, rtypeElement);
			}

			//formalParameter
			List<SingleVariableDeclaration> fpList = (List<SingleVariableDeclaration>)node.getStructuralProperty(MethodDeclaration.PARAMETERS_PROPERTY);
			Iterator<SingleVariableDeclaration> itFP = fpList.iterator();
			while(itFP.hasNext()){
				SingleVariableDeclaration fp = itFP.next();
				Element fpElement = methodDeclarationElement.addElement("formalParameter").addElement("singleVariableDeclaration");
				elements.put(fp, fpElement);
			}

			//thrownException
			List<Type> exceptionList = (List<Type>)node.getStructuralProperty(MethodDeclaration.THROWN_EXCEPTION_TYPES_PROPERTY);
			Iterator<Type> itException = exceptionList.iterator();
			while(itException.hasNext()){
				Type exception = itException.next();
				Element exceptionElement = methodDeclarationElement.addElement("thrownException").addElement("tp");
				elements.put(exception, exceptionElement);
			}

			//block
			Block block = (Block)node.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
			Element blockElement = methodDeclarationElement.addElement("block");
			elements.put(block, blockElement);
			blockVisit(block, blockElement);
		}
		return super.visit(node);
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(Initializer node) {
		Element initElement = elements.get(node);
		if(initElement != null){
			//get modifiers
			List<Modifier> modifierList = (List<Modifier>) node.getStructuralProperty(Initializer.MODIFIERS2_PROPERTY);
			if(modifierList != null){
				Iterator<Modifier> itMod = modifierList.iterator();
				while(itMod.hasNext()){
					Modifier modifier = itMod.next();
					Element modifierElement = initElement.addElement("modifier");
					elements.put(modifier, modifierElement);
				}
			}

			//block
			Block block = (Block)node.getStructuralProperty(Initializer.BODY_PROPERTY);
			Element blockElement = initElement.addElement("block");
			elements.put(block, blockElement);
			blockVisit(block, blockElement);
		}
		return super.visit(node);
	}
	@Override
	public boolean visit(FieldDeclaration node) {
		Element fieldElement = elements.get(node);
		if(fieldElement != null){
			//get modifiers
			List<Modifier> modifierList = (List<Modifier>) node.getStructuralProperty(FieldDeclaration.MODIFIERS2_PROPERTY);
			if(modifierList != null){
				Iterator<Modifier> itMod = modifierList.iterator();
				while(itMod.hasNext()){
					Modifier modifier = itMod.next();
					Element modifierElement = fieldElement.addElement("modifier");
					elements.put(modifier, modifierElement);
				}
			}

			//get tp
			Type tp = (Type)node.getStructuralProperty(FieldDeclaration.TYPE_PROPERTY);
			Element tpElement = fieldElement.addElement("tp");
			elements.put(tp, tpElement);
			tpVisit(tp, tpElement);

			//get variableDeclarationFragment
			List<VariableDeclarationFragment> vdfList = (List<VariableDeclarationFragment>)node.getStructuralProperty(FieldDeclaration.FRAGMENTS_PROPERTY);
			Iterator<VariableDeclarationFragment> itVDF = vdfList.iterator();
			while(itVDF.hasNext()){
				VariableDeclarationFragment vdf = itVDF.next();
				Element vdfElement = fieldElement.addElement("variableDeclarationFragment");
				elements.put(vdf,vdfElement);
			}
		}
		return super.visit(node);
	}
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		Element vdfElement = elements.get(node);
		if(vdfElement != null){
			vdfElement.addAttribute("extrDimensions", ""+node.getExtraDimensions());

			//get simpleName
			SimpleName sname = (SimpleName)node.getStructuralProperty(VariableDeclarationFragment.NAME_PROPERTY);
			Element snameElement = vdfElement.addElement("simpleName");
			elements.put(sname, snameElement);

			//get Expressions
			Expression exp = (Expression)node.getStructuralProperty(VariableDeclarationFragment.INITIALIZER_PROPERTY);
			Element expElement = vdfElement.addElement("expression");
			elements.put(exp,expElement);
			expVisit(exp, expElement);
		}
		return super.visit(node);
	}
	@Override
	public boolean visit(SingleVariableDeclaration node) {
		Element svdElement = elements.get(node);
		if(svdElement != null){
			svdElement.addAttribute("extrDimentions", ""+node.getExtraDimensions());

			//get modifiers
			List<Modifier> modifierList = (List<Modifier>) node.getStructuralProperty(SingleVariableDeclaration.MODIFIERS2_PROPERTY);
			if(modifierList != null){
				Iterator<Modifier> itMod = modifierList.iterator();
				while(itMod.hasNext()){
					Modifier modifier = itMod.next();
					Element modifierElement = svdElement.addElement("modifier");
					elements.put(modifier, modifierElement);
				}
			}

			//get tp
			Type tp = (Type)node.getStructuralProperty(SingleVariableDeclaration.TYPE_PROPERTY);
			Element tpElement = svdElement.addElement("tp");
			elements.put(tp, tpElement);
			tpVisit(tp, tpElement);

			//get simpleName
			SimpleName sname = (SimpleName)node.getStructuralProperty(SingleVariableDeclaration.NAME_PROPERTY);
			Element snameElement = svdElement.addElement("simpleName");
			elements.put(sname, snameElement);

			//get Expressions
			Expression exp = (Expression)node.getStructuralProperty(SingleVariableDeclaration.INITIALIZER_PROPERTY);
			Element expElement = svdElement.addElement("expression");
			elements.put(exp,expElement);
		}
		return super.visit(node);
	}

	//Statements
	public void blockVisit(Block block,Element blockElement){
		List<Statement> stmtList = block.statements();
		Iterator<Statement> itStmt = stmtList.iterator();
		while(itStmt.hasNext()){
			Statement stmt = itStmt.next();
			Element stmtElement = blockElement.addElement("statement");
			elements.put(stmt, stmtElement);
			stmtVisit(stmt,stmtElement);
		}
	}

	public void stmtVisit(Statement stmt,Element stmtElement){
		Statement myStmt = null;
		if(stmt instanceof AssertStatement){
			myStmt = (AssertStatement)stmt;
		}else if(stmt instanceof Block){
			myStmt = (Block)stmt;
		}else if(stmt instanceof BreakStatement){
			myStmt = (BreakStatement)stmt;
		}else if(stmt instanceof ConstructorInvocation){
			myStmt = (ConstructorInvocation)stmt;
		}else if(stmt instanceof ContinueStatement){
			myStmt = (ContinueStatement)stmt;
		}else if(stmt instanceof DoStatement){
			myStmt = (DoStatement)stmt;
		}else if(stmt instanceof EmptyStatement){
			myStmt = (EmptyStatement)stmt;
		}else if(stmt instanceof ExpressionStatement){
			myStmt = (ExpressionStatement)stmt;
		}else if(stmt instanceof ForStatement){
			myStmt = (ForStatement)stmt;
		}else if(stmt instanceof IfStatement){
			myStmt = (IfStatement)stmt;
		}else if(stmt instanceof LabeledStatement){
			myStmt = (LabeledStatement)stmt;
		}else if(stmt instanceof ReturnStatement){
			myStmt = (ReturnStatement)stmt;
		}else if(stmt instanceof SuperConstructorInvocation){
			myStmt = (SuperConstructorInvocation)stmt;
		}else if(stmt instanceof SwitchCase){
			myStmt = (SwitchCase)stmt;
		}else if(stmt instanceof SwitchStatement){
			myStmt = (SwitchStatement)stmt;
		}else if(stmt instanceof SynchronizedStatement){
			myStmt = (SynchronizedStatement)stmt;
		}else if(stmt instanceof ThrowStatement){
			myStmt = (ThrowStatement)stmt;
		}else if(stmt instanceof  TryStatement){
			myStmt = (TryStatement)stmt;
		}else if(stmt instanceof  TypeDeclarationStatement){
			myStmt = (TypeDeclarationStatement)stmt;
		}else if(stmt instanceof  VariableDeclarationStatement){
			myStmt = (VariableDeclarationStatement)stmt;
		}else if(stmt instanceof  WhileStatement){
			myStmt = (WhileStatement)stmt;
		}else if(stmt instanceof  EnhancedForStatement){
			myStmt = (EnhancedForStatement)stmt;
		}
		if(myStmt != null){
			Element myStmtElement = stmtElement.addElement(myStmt.getClass().getSimpleName().substring(0, 1).toLowerCase()+myStmt.getClass().getSimpleName().substring(1));
			elements.put(myStmt, myStmtElement);
		}
	}

	@Override
	public boolean visit(AssertStatement node) {
		Element astmtElement = elements.get(node);

		//get expression
		Expression exp = node.getExpression();
		Element expElement = astmtElement.addElement("expression");
		elements.put(exp, expElement);
		expVisit(exp, expElement);

		//get message
		Expression msg = node.getMessage();
		Element msgElement = astmtElement.addElement("message").addElement("expression");
		elements.put(msg, msgElement);
		expVisit(msg, msgElement);

		return super.visit(node);
	}

	@Override
	public boolean visit(BreakStatement node) {
		Element bkstmtElement = elements.get(node);
		SimpleName sname = node.getLabel();
		if(sname != null){
			Element snameElement = bkstmtElement.addElement("simpleName");
			elements.put(sname, snameElement);
		}		
		return super.visit(node);
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		Element ciElement = elements.get(node);

		//get TypeArgument
		List<Type> taList = node.typeArguments();
		Iterator<Type> itTa = taList.iterator();
		while(itTa.hasNext()){
			Type ta = itTa.next();
			Element taElement = ciElement.addElement("typeArgument").addElement("tp");
			elements.put(ta,taElement);
		}

		//get Argument
		List<Expression> expList = node.arguments();
		Iterator<Expression> itExp = expList.iterator();
		while(itExp.hasNext()){
			Expression exp = itExp.next();
			Element expElement = ciElement.addElement("argument").addElement("expression");
			elements.put(exp, expElement);
			expVisit(exp, expElement);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(ContinueStatement node) {
		Element cnstmtElement = elements.get(node);
		SimpleName sname = node.getLabel();
		if(sname != null){
			Element snameElement = cnstmtElement.addElement("simpleName");
			elements.put(sname, snameElement);
		}		
		return super.visit(node);
	}

	@Override
	public boolean visit(DoStatement node) {
		Element dostmtElement = elements.get(node);

		Statement stmt = node.getBody();
		Element stmtElement = dostmtElement.addElement("statement");
		elements.put(stmt,stmtElement);
		stmtVisit(stmt, stmtElement);

		Expression exp = node.getExpression();
		Element expElement = dostmtElement.addElement("expression");
		elements.put(exp, expElement);
		expVisit(exp, expElement);

		return super.visit(node);
	}

	@Override
	public boolean visit(EmptyStatement node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		Element expStatement = elements.get(node);
		if(expStatement != null){
			Expression exp = node.getExpression();
			Element expElement = expStatement.addElement("expression");
			elements.put(exp, expElement);
			expVisit(exp, expElement);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(ForStatement node) {
		Element fstmtElement = elements.get(node);
		if(fstmtElement != null){
			//forInit
			List<Expression> initList = node.initializers();
			Iterator<Expression> itInit = initList.iterator();
			while(itInit.hasNext()){
				Expression init = itInit.next();
				Element initElement = fstmtElement.addElement("forInit").addElement("expression");
				elements.put(init, initElement);
				expVisit(init, initElement);
			}

			//expression
			Expression exp = node.getExpression();
			if(exp != null){
				Element expElement = fstmtElement.addElement("expression");
				elements.put(exp, expElement);
				expVisit(exp, expElement);
			}

			//forUpdate
			List<Expression> upList = node.updaters();
			Iterator<Expression> itUp = initList.iterator();
			while(itUp.hasNext()){
				Expression update = itUp.next();
				Element updateElement = fstmtElement.addElement("forUpdate").addElement("expression");
				elements.put(update, updateElement);
				expVisit(update, updateElement);
			}

			//statement
			Statement stmt = node.getBody();
			Element stmtElement = fstmtElement.addElement("statement");
			elements.put(stmt, stmtElement);
			stmtVisit(stmt, stmtElement);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(IfStatement node) {
		Element ifstmtElement = elements.get(node);
		if(ifstmtElement != null){
			Expression exp = node.getExpression();
			Element expElement = ifstmtElement.addElement("expression");
			elements.put(exp, expElement);
			expVisit(exp, expElement);

			Statement thenstmt = node.getThenStatement();
			Element thenElement = ifstmtElement.addElement("thenStatement");
			elements.put(thenstmt, thenElement);
			stmtVisit(thenstmt, thenElement);

			Statement elsestmt = node.getElseStatement();
			Element elseElement = ifstmtElement.addElement("elseStatement");
			elements.put(elsestmt, elseElement);
			stmtVisit(elsestmt, elseElement);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(LabeledStatement node) {
		Element lbstmtElement = elements.get(node);
		if(lbstmtElement != null){
			SimpleName sname = node.getLabel();
			Element snameElement = lbstmtElement.addElement("simpleName");
			elements.put(sname, snameElement);

			Statement stmt = node.getBody();
			Element stmtElement = lbstmtElement.addElement("statement");
			elements.put(stmt, stmtElement);
			stmtVisit(stmt, stmtElement);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(ReturnStatement node) {
		Element returnElement = elements.get(node);
		if(returnElement != null){
			Expression exp = node.getExpression();
			if(exp != null){
				Element expElement = returnElement.addElement("expression");
				elements.put(exp, expElement);
				expVisit(exp, expElement);
			}
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		Element ciElement = elements.get(node);

		//get Expression
		Expression exp2 = node.getExpression();
		Element expElement2 = ciElement.addElement("expression");
		elements.put(exp2,expElement2);
		expVisit(exp2, expElement2);

		//get TypeArgument
		List<Type> taList = node.typeArguments();
		Iterator<Type> itTa = taList.iterator();
		while(itTa.hasNext()){
			Type ta = itTa.next();
			Element taElement = ciElement.addElement("typeArgument").addElement("tp");
			elements.put(ta,taElement);
		}

		//get Argument
		List<Expression> expList = node.arguments();
		Iterator<Expression> itExp = expList.iterator();
		while(itExp.hasNext()){
			Expression exp = itExp.next();
			Element expElement = ciElement.addElement("argument").addElement("expression");
			elements.put(exp, expElement);
			expVisit(exp, expElement);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchCase node) {
		Element swcElement = elements.get(node);
		if(swcElement != null){
			Expression exp = node.getExpression();
			if(exp != null){
				Element expElement = swcElement.addElement("expression");
				elements.put(exp, expElement);
				expVisit(exp, expElement);
			}
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchStatement node) {
		Element swElement = elements.get(node);
		if(swElement != null){
			Expression exp = node.getExpression();
			Element expElement = swElement.addElement("expression");
			elements.put(exp, expElement);
			expVisit(exp, expElement);

			List<Statement> stmtList = node.statements();
			Iterator<Statement> itStmt = stmtList.iterator();
			while(itStmt.hasNext()){
				Statement stmt = itStmt.next();
				if(stmt instanceof SwitchCase){
					SwitchCase swc = (SwitchCase)stmt;
					Element swcElement = swElement.addElement("switchBlock").addElement("switchCase");
					elements.put(swc, swcElement);
				}else{
					Element stmtElement = swElement.addElement("switchBlock").addElement("statement");
					elements.put(stmt, stmtElement);
					stmtVisit(stmt, stmtElement);
				}
			}
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		Element syncElement = elements.get(node);
		if(syncElement != null){
			Expression exp = node.getExpression();
			Element expElement = syncElement.addElement("expression");
			elements.put(exp,expElement);
			expVisit(exp, expElement);

			Block block = node.getBody();
			Element blockElement = syncElement.addElement("block");
			elements.put(block, blockElement);
			blockVisit(block, blockElement);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(ThrowStatement node) {
		Element throwElement = elements.get(node);
		Expression exp = node.getExpression();
		Element expElement = throwElement.addElement("expression");
		elements.put(exp, expElement);
		expVisit(exp, expElement);
		return super.visit(node);
	}

	@Override
	public boolean visit(TryStatement node) {
		Element tryElement = elements.get(node);

		List<VariableDeclarationExpression> vdfList = node.resources();
		if(vdfList != null){
			for(VariableDeclarationExpression vdf : vdfList){
				Element vdfElement = tryElement.addElement("resources").addElement("variableDeclartionExpression");
				elements.put(vdf,vdfElement);
			}
		}

		Block block = node.getBody();
		Element blockElement = tryElement.addElement("block");
		elements.put(block,blockElement);
		blockVisit(block, blockElement);

		List<CatchClause> ccList = node.catchClauses();
		if(ccList != null){
			for(CatchClause cc : ccList){
				Element ccElement = tryElement.addElement("catchClause");
				elements.put(cc, ccElement);
			}
		}

		Block finaly = node.getFinally();
		Element finalyElement = tryElement.addElement("finally").addElement("block");
		elements.put(finaly, finalyElement);
		blockVisit(finaly, finalyElement);

		return super.visit(node);
	}

	@Override
	public boolean visit(CatchClause node) {
		Element ccElement  = elements.get(node);
		SingleVariableDeclaration svd = node.getException();
		Element svdElement = ccElement.addElement("formalParameter").addElement("singleVariableDeclaration");
		elements.put(svd, svdElement);

		Block block = node.getBody();
		Element blockElement = ccElement.addElement("block");
		elements.put(block, blockElement);

		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclarationStatement node) {
		Element tdsElement = elements.get(node);
		AbstractTypeDeclaration atd = node.getDeclaration();
		Element atdElement = null;
		if(atd instanceof TypeDeclaration){
			atd = (TypeDeclaration)atd;
			atdElement = tdsElement.addElement("typeDeclaration");
		}else if(atd instanceof EnumDeclaration){
			//not implemented
			atd = (EnumDeclaration)atd;
			atdElement = tdsElement.addElement("enumDeclartion");
		}
		if(atdElement != null)	elements.put(atd, atdElement);
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		Element vdsElement = elements.get(node);
		if(vdsElement != null){
			List<Modifier> modList = node.modifiers();
			if(modList != null){
				for(Modifier mod : modList){
					Element modElement = vdsElement.addElement("modifier");
					elements.put(mod,modElement);
				}
			}

			Type tp = node.getType();
			Element tpElement = vdsElement.addElement("tp");
			elements.put(tp, tpElement);
			tpVisit(tp, tpElement);

			List<VariableDeclarationFragment> vdfList = node.fragments();
			for(VariableDeclarationFragment vdf : vdfList){
				Element vdfElement = vdsElement.addElement("variableDeclarationFragment");
				elements.put(vdf,vdfElement);
			}
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(WhileStatement node) {
		Element whileElement = elements.get(node);
		Expression exp = node.getExpression();
		Element expElement = whileElement.addElement("expression");
		elements.put(exp, expElement);
		expVisit(exp, expElement);

		Statement stmt = node.getBody();
		Element stmtElement = whileElement.addElement("statement");
		elements.put(stmt, stmtElement);
		stmtVisit(stmt, stmtElement);

		return super.visit(node);
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		Element efsElement = elements.get(node);
		SingleVariableDeclaration svd = node.getParameter();
		Element svdElement = efsElement.addElement("formalParameter").addElement("singleVariableDeclaration");
		elements.put(svd, svdElement);

		Expression exp = node.getExpression();
		Element expElement = efsElement.addElement("expression");
		elements.put(exp, expElement);
		expVisit(exp, expElement);

		Statement stmt = node.getBody();
		Element stmtElement = efsElement.addElement("statement");
		elements.put(stmt, stmtElement);
		stmtVisit(stmt, stmtElement);

		return super.visit(node);
	}



	//expression
	public void expVisit(Expression exp,Element expElement){
		Expression exp2 = null;
		if(exp instanceof ArrayAccess){
			exp2 = (ArrayAccess)exp;
		}else if(exp instanceof ArrayCreation){
			exp2 = (ArrayCreation)exp;
		}else if(exp instanceof ArrayCreation){
			exp2 = (ArrayCreation)exp;
		}
		else if(exp instanceof ArrayInitializer){
			exp2 = (ArrayInitializer)exp;
		}
		else if(exp instanceof Assignment){
			exp2 = (Assignment)exp;
		}
		else if(exp instanceof BooleanLiteral){
			exp2 = (BooleanLiteral)exp;
		}
		else if(exp instanceof CastExpression){
			exp2 = (CastExpression)exp;
		}
		else if(exp instanceof CharacterLiteral){
			exp2 = (CharacterLiteral)exp;
		}
		else if(exp instanceof ClassInstanceCreation){
			exp2 = (ClassInstanceCreation)exp;
		}
		else if(exp instanceof ConditionalExpression){
			exp2 = (ConditionalExpression)exp;
		}
		else if(exp instanceof FieldAccess){
			exp2 = (FieldAccess)exp;
		}
		else if(exp instanceof InfixExpression){
			exp2 = (InfixExpression)exp;
		}
		else if(exp instanceof InstanceofExpression){
			exp2 = (InstanceofExpression)exp;
		}
		else if(exp instanceof  MethodInvocation){
			exp2 = (MethodInvocation)exp;
		}
		else if(exp instanceof   NullLiteral){
			exp2 = (NullLiteral)exp;
		}else if(exp instanceof  NumberLiteral){
			exp2 = (NumberLiteral)exp;
		}else if(exp instanceof  ParenthesizedExpression){
			exp2 = (ParenthesizedExpression)exp;
		}else if(exp instanceof  PostfixExpression){
			exp2 = ( PostfixExpression)exp;
		}else if(exp instanceof  PrefixExpression){
			exp2 = (PrefixExpression)exp;
		}else if(exp instanceof  StringLiteral){
			exp2 = (StringLiteral)exp;
		}else if(exp instanceof  SuperFieldAccess){
			exp2 = (SuperFieldAccess)exp;
		}else if(exp instanceof  SuperMethodInvocation){
			exp2 = (SuperMethodInvocation)exp;
		}
		else if(exp instanceof  ThisExpression){
			exp2 = (ThisExpression)exp;
		}
		else if(exp instanceof  TypeLiteral){
			exp2 = (TypeLiteral)exp;
		}
		else if(exp instanceof  VariableDeclarationExpression){
			exp2 = (VariableDeclarationExpression)exp;
		}

		if(exp2 != null){
			Element myExpElement = expElement.addElement(exp2.getClass().getSimpleName().substring(0, 1).toLowerCase()+exp2.getClass().getSimpleName().substring(1));
			elements.put(exp2, myExpElement);
		}
	}


	@Override
	public boolean visit(ArrayAccess node) {
		Element aaElement = elements.get(node);
		Expression array = node.getArray();
		Element arrayElement = aaElement.addElement("array").addElement("expression");
		elements.put(array, arrayElement);

		Expression index = node .getIndex();
		Element indexElement = aaElement.addElement("index").addElement("expression");
		elements.put(index, indexElement);
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayCreation node) {
		Element aaElement = elements.get(node);
		ArrayType at = node.getType();
		Element atElement = aaElement.addElement("arrayType");
		elements.put(at, atElement);

		List<Expression> expList = node.dimensions();
		if(expList != null){
			for (Expression exp : expList){
				Element expElement = aaElement.addElement("dimensions").addElement("expression");
				elements.put(exp, expElement);
			}
		}

		ArrayInitializer ai = node.getInitializer();
		Element aiElement = aaElement.addElement("arrayInitializer");
		elements.put(ai, aiElement);
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		Element aiElement = elements.get(node);
		List<Expression> expList = node.expressions();
		if(expList != null){
			for (Expression exp : expList){
				Element expElement = aiElement.addElement("expression");
				elements.put(exp, expElement);
			}
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(Assignment node) {
		Element assignElement = elements.get(node);
		if(assignElement != null){
			Expression left = node.getLeftHandSide();
			Element leftElement = assignElement.addElement("leftHandSide").addElement("expression");
			elements.put(left, leftElement);
			expVisit(left, leftElement);

			Expression right = node.getRightHandSide();
			Element rightElement = assignElement.addElement("rightHandSide").addElement("expression");
			elements.put(right, rightElement);
			expVisit(right, rightElement);

			Operator op = node.getOperator();
			Element opElement = assignElement.addElement("assignmentOperator");
			opElement.setText(op.toString());
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		Element boolElement = elements.get(node);
		boolElement.setText(node.toString());
		return super.visit(node);
	}

	@Override
	public boolean visit(CastExpression node) {
		Element castElement = elements.get(node);
		Type tp = node.getType();
		Element tpElement = castElement.addElement("tp");
		tpVisit(tp, tpElement);

		Expression exp = node.getExpression();
		Element expElement = castElement.addElement("expression");
		expVisit(exp, expElement);

		return super.visit(node);
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		Element clElement = elements.get(node);
		clElement.setText(node.getEscapedValue());
		return super.visit(node);
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		Element cicElement = elements.get(node);
		if(cicElement != null){
			Expression exp = node.getExpression();
			Element expElement = cicElement.addElement("expression");
			elements.put(exp, expElement);
			expVisit(exp, expElement);

			//get TypeArgument
			List<Type> taList = node.typeArguments();
			Iterator<Type> itTa = taList.iterator();
			while(itTa.hasNext()){
				Type ta = itTa.next();
				Element taElement = cicElement.addElement("typeArgument").addElement("tp");
				elements.put(ta,taElement);
			}

			//get tp
			Type type = node.getType();
			Element typeElement = cicElement.addElement("tp");
			elements.put(type, typeElement);
			tpVisit(type, typeElement);

			//get Argument
			List<Expression> expList = node.arguments();
			Iterator<Expression> itExp = expList.iterator();
			while(itExp.hasNext()){
				Expression argexp = itExp.next();
				Element argexpElement = cicElement.addElement("argument").addElement("expression");
				elements.put(exp, expElement);
				expVisit(exp, expElement);
			}

			//anonymousClassDeclaration
		}

		return super.visit(node);
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		Element condElement = elements.get(node);
		Expression exp = node.getExpression();
		Element expElement = condElement.addElement("expression");
		elements.put(exp, expElement);
		expVisit(exp, expElement);

		Expression thenexp = node.getThenExpression();
		Element thenexpElement = condElement.addElement("thenExpression").addElement("expression");
		elements.put(thenexp, thenexpElement);
		expVisit(thenexp, thenexpElement);

		Expression elseexp = node.getExpression();
		Element elseexpElement = condElement.addElement("elseExpression").addElement("expression");
		elements.put(elseexp, elseexpElement);
		expVisit(elseexp, elseexpElement);

		return super.visit(node);
	}

	@Override
	public boolean visit(FieldAccess node) {
		Element faElement = elements.get(node);
		Expression exp = node.getExpression();
		Element expElement = faElement.addElement("expression");
		elements.put(exp, expElement);
		expVisit(exp, expElement);

		SimpleName sn = node.getName();
		Element snElement = faElement.addElement("simpleName");
		elements.put(sn, snElement);
		return super.visit(node);
	}

	@Override
	public boolean visit(InfixExpression node) {
		Element infixElement = elements.get(node);
		if(infixElement != null){
			infixElement.addAttribute("hasExtendedOperands", ""+node.hasExtendedOperands());
			Expression left = node.getLeftOperand();
			Element leftElement = infixElement.addElement("leftOperand").addElement("expression");
			elements.put(left, leftElement);
			expVisit(left, leftElement);

			Expression right = node.getRightOperand();
			Element rightElement = infixElement.addElement("rightOperand").addElement("expression");
			elements.put(right, rightElement);
			expVisit(right, rightElement);

			org.eclipse.jdt.core.dom.InfixExpression.Operator op = node.getOperator();
			Element opElement = infixElement.addElement("infixOperator");
			opElement.setText(op.toString());

			if(node.hasExtendedOperands()){
				List<Expression> expList = node.extendedOperands();
				for(Expression exp : expList){
					Element expElement = infixElement.addElement("expression");
					elements.put(exp, expElement);
					expVisit(exp, expElement);
				}
			}
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		Element ioeElement = elements.get(node);
		Expression exp = node.getLeftOperand();
		Element expElement = ioeElement.addElement("expression");
		elements.put(exp, expElement);
		expVisit(exp, expElement);

		Type type = node.getRightOperand();
		Element typeElement = ioeElement.addElement("tp");
		elements.put(type, typeElement);
		tpVisit(type, typeElement);

		return super.visit(node);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		Element cicElement = elements.get(node);
		if(cicElement != null){
			Expression exp = node.getExpression();
			Element expElement = cicElement.addElement("expression");
			elements.put(exp, expElement);
			expVisit(exp, expElement);

			//get TypeArgument
			List<Type> taList = node.typeArguments();
			Iterator<Type> itTa = taList.iterator();
			while(itTa.hasNext()){
				Type ta = itTa.next();
				Element taElement = cicElement.addElement("typeArgument").addElement("tp");
				elements.put(ta,taElement);
			}

			SimpleName sn = node.getName();
			Element snElement = cicElement.addElement("simpleName");
			elements.put(sn, snElement);

			//get Argument
			List<Expression> expList = node.arguments();
			Iterator<Expression> itExp = expList.iterator();
			while(itExp.hasNext()){
				Expression argexp = itExp.next();
				Element argexpElement = cicElement.addElement("argument").addElement("expression");
				elements.put(argexp, argexpElement);
				expVisit(argexp, argexpElement);
			}
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(NullLiteral node) {

		return super.visit(node);
	}

	@Override
	public boolean visit(NumberLiteral node) {
		Element nlElement = elements.get(node);
		nlElement.setText(node.getToken());
		return super.visit(node);
	}

	@Override
	public boolean visit(ParenthesizedExpression node) {
		Element peElement = elements.get(node);
		Expression exp = node.getExpression();
		Element expElement = peElement.addElement("expression");
		elements.put(exp, expElement);
		expVisit(exp, expElement);
		return super.visit(node);
	}

	@Override
	public boolean visit(PostfixExpression node) {
		Element peElement = elements.get(node);
		Expression exp = node.getOperand();
		Element expElement = peElement.addElement("expression");
		elements.put(exp, expElement);
		expVisit(exp, expElement);

		org.eclipse.jdt.core.dom.PostfixExpression.Operator op = node.getOperator();
		Element opElement = peElement.addElement("postfixOperator");
		opElement.setText(op.toString());
		return super.visit(node);
	}

	@Override
	public boolean visit(PrefixExpression node) {
		Element peElement = elements.get(node);
		Expression exp = node.getOperand();
		Element expElement = peElement.addElement("expression");
		elements.put(exp, expElement);
		expVisit(exp, expElement);

		org.eclipse.jdt.core.dom.PrefixExpression.Operator op = node.getOperator();
		Element opElement = peElement.addElement("prefixOperator");
		opElement.setText(op.toString());
		return super.visit(node);
	}

	@Override
	public boolean visit(StringLiteral node) {
		Element elem = elements.get(node);
		elem.setText(node.getEscapedValue());
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		Element elem = elements.get(node);
		Name name = node.getQualifier();
		if(name != null){
			Element nameElement = elem.addElement("name");
			elements.put(name,nameElement);
			nameVisit(name, nameElement);
		}
		SimpleName sn = node.getName();
		Element snElement = elem.addElement("simpleName");
		elements.put(sn, snElement);
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		Element cicElement = elements.get(node);
		Name name = node.getQualifier();
		if(name != null){
			Element nameElement = cicElement.addElement("name");
			elements.put(name,nameElement);
			nameVisit(name, nameElement);
		}

		//get TypeArgument
		List<Type> taList = node.typeArguments();
		Iterator<Type> itTa = taList.iterator();
		while(itTa.hasNext()){
			Type ta = itTa.next();
			Element taElement = cicElement.addElement("typeArgument").addElement("tp");
			elements.put(ta,taElement);
		}

		SimpleName sn = node.getName();
		Element snElement = cicElement.addElement("simpleName");
		elements.put(sn, snElement);

		//get Argument
		List<Expression> expList = node.arguments();
		Iterator<Expression> itExp = expList.iterator();
		while(itExp.hasNext()){
			Expression exp = itExp.next();
			Element expElement = cicElement.addElement("argument").addElement("expression");
			elements.put(exp, expElement);
			expVisit(exp, expElement);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(ThisExpression node) {
		Element elem = elements.get(node);
		Name name = node.getQualifier();
		if(name != null){
			Element nameElement = elem.addElement("name");
			elements.put(name,nameElement);
			nameVisit(name, nameElement);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeLiteral node) {
		Element elem = elements.get(node);
		Type type = node.getType();
		Element typeElement = elem.addElement("tp");
		elements.put(type,typeElement);
		tpVisit(type, typeElement);
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationExpression node) {
		Element vdsElement = elements.get(node);
		if(vdsElement != null){
			List<Modifier> modList = node.modifiers();
			if(modList != null){
				for(Modifier mod : modList){
					Element modElement = vdsElement.addElement("modifier");
					elements.put(mod,modElement);
				}
			}

			Type tp = node.getType();
			Element tpElement = vdsElement.addElement("tp");
			elements.put(tp, tpElement);
			tpVisit(tp, tpElement);

			List<VariableDeclarationFragment> vdfList = node.fragments();
			for(VariableDeclarationFragment vdf : vdfList){
				Element vdfElement = vdsElement.addElement("variableDeclarationFragment");
				elements.put(vdf,vdfElement);
			}
		}
		return super.visit(node);
	}

	//Tokens
	@Override
	public boolean visit(Modifier node) {
		Element modifier= elements.get(node);
		if (modifier != null){
			modifier.setText(node.getKeyword().toString());
		}
		return super.visit(node);
	}

	public void nameVisit(Name name,Element nameElement){
		//Name Element
		if(name.isQualifiedName()){
			QualifiedName qname = (QualifiedName)name;
			Element qnameElement = nameElement.addElement("qualifiedName");
			elements.put(qname, qnameElement);
		}else{
			SimpleName sname = (SimpleName)name;
			Element snameElement = nameElement.addElement("simpleName");
			elements.put(sname, snameElement);
		}
	}

	@Override
	public boolean visit(QualifiedName node) {
		Element qnameElement = elements.get(node);
		if(qnameElement != null){
			//get the Name part
			Name name = (Name)node.getStructuralProperty(QualifiedName.QUALIFIER_PROPERTY);
			Element nameElement = qnameElement.addElement("name");
			elements.put(name, nameElement);
			nameVisit(name,nameElement);

			//get the SimpleName part
			SimpleName sname = (SimpleName)node.getStructuralProperty(QualifiedName.NAME_PROPERTY);
			Element snameElement = qnameElement.addElement("simpleName");
			elements.put(sname, snameElement);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(SimpleName node) {
		Element snameElement = elements.get(node);
		if(snameElement != null){
			String text = node.getIdentifier();
			idVisit(snameElement, text);
		}
		return super.visit(node);
	}

	public void idVisit(Element element,String text){
		//Identifier element
		element.addElement("identifier").setText(text);
	}

	@Override
	public boolean visit(TypeParameter node) {
		Element tpmElement = elements.get(node);
		if(tpmElement != null){
			//get SimpleName
			SimpleName sname= (SimpleName)node.getStructuralProperty(TypeParameter.NAME_PROPERTY);
			Element snameElement = tpmElement.addElement("simpleName");
			elements.put(sname, snameElement);
			//get Type Bounds list
			List<Type> typeList = (List<Type>)node.getStructuralProperty(TypeParameter.TYPE_BOUNDS_PROPERTY);
			if(typeList != null){
				Iterator<Type> itTP = typeList.iterator();
				while(itTP.hasNext()){
					Type tp = itTP.next();
					Element tpElement =  tpmElement.addElement("typeBound").addElement("tp");
					elements.put(tp,tpElement);
					tpVisit(tp, tpElement);
				}
			}
		}
		return super.visit(node);
	}

	//Types
	public void tpVisit(Type type,Element typeElement){
		Type tp = null;
		if(type.isPrimitiveType()){
			tp = (PrimitiveType)type;
		}else if(type.isArrayType()){
			tp = (ArrayType)type;
		}else if(type.isSimpleType()){
			tp = (SimpleType)type;
		}else if(type.isQualifiedType()){
			tp = (QualifiedType)type;
		}else if(type.isParameterizedType()){
			tp = (ParameterizedType)type;
		}else if(type.isWildcardType()){
			tp = (WildcardType)type;
		}
		if(tp !=null){
			Element tpElement = typeElement.addElement(tp.getClass().getSimpleName().substring(0,1).toLowerCase()+tp.getClass().getSimpleName().substring(1)
					);
			elements.put(tp, tpElement);
		}
	}

	@Override
	public boolean visit(ArrayType node) {
		Element elem = elements.get(node);
		elem.addAttribute("dimensions", ""+node.getDimensions());
		Type type = node.getElementType();
		Element typeElement = elem.addElement("tp");
		elements.put(type,typeElement);
		tpVisit(type, typeElement);

		return super.visit(node);
	}

	@Override
	public boolean visit(ParameterizedType node) {
		Element elem = elements.get(node);
		Type type = node.getType();
		Element typeElement = elem.addElement("tp");
		elements.put(type,typeElement);
		tpVisit(type, typeElement);

		//get TypeArgument
		List<Type> taList = node.typeArguments();
		Iterator<Type> itTa = taList.iterator();
		while(itTa.hasNext()){
			Type ta = itTa.next();
			Element taElement = elem.addElement("typeArgument").addElement("tp");
			elements.put(ta,taElement);
		}

		return super.visit(node);
	}

	@Override
	public boolean visit(PrimitiveType node) {
		Element ptypeElement = elements.get(node);
		if(ptypeElement != null){
			ptypeElement.setText(node.getPrimitiveTypeCode().toString());
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(QualifiedType node) {
		Element elem = elements.get(node);
		Type type = node.getQualifier();
		Element typeElement = elem.addElement("tp");
		elements.put(type,typeElement);
		tpVisit(type, typeElement);

		SimpleName sn = node.getName();
		Element snElement = elem.addElement("simpleName");
		elements.put(sn, snElement);


		return super.visit(node);
	}

	@Override
	public boolean visit(SimpleType node) {
		Element stpElement = elements.get(node);
		if(stpElement != null){
			Name name = (Name)node.getStructuralProperty(SimpleType.NAME_PROPERTY);
			Element nameElement = stpElement.addElement("name");
			elements.put(name, nameElement);
			nameVisit(name,nameElement);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(WildcardType node) {
		Element elem = elements.get(node);
		Type type = node.getBound();
		Element typeElement = elem.addElement("tp");
		elements.put(type,typeElement);
		tpVisit(type, typeElement);
		return super.visit(node);
	}

}
