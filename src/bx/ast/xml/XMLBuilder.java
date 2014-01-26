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

	//Tokens





	//Modifier Visit Blocks
	@Override
	public boolean visit(Modifier node) {
		Element modifier= elements.get(node);
		if (modifier != null){
			modifier.setText(node.getKeyword().toString());
		}
		return super.visit(node);
	}

	//Name Visit Blocks
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
	//Type Visit Blocks
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
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	@Override
	public boolean visit(ParameterizedType node) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return super.visit(node);
	}

}
