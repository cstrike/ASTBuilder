package bx.ast.xml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.eclipse.jdt.core.dom.*;

public class XMLBuilder extends ASTVisitor {
	public Document document;
	public Map<ASTNode, Element> elements;

	public XMLBuilder(){
		super();
		document = DocumentHelper.createDocument();
		elements = new HashMap<ASTNode, Element>();
	}

	@Override
	public boolean visit(CompilationUnit node){
		//Add root to the elements. If the node is not root, it has already been added.
		Element compilationUnitElement = document.addElement(node.getClass().getSimpleName());
		elements.put(node, compilationUnitElement);

		//Add the children of the node to elements.
		PackageDeclaration packageDeclaration = (PackageDeclaration) node.getStructuralProperty(CompilationUnit.PACKAGE_PROPERTY);
		if (packageDeclaration != null){
			Element packageDeclarationElement = compilationUnitElement.addElement(packageDeclaration.getClass().getSimpleName());
			elements.put(packageDeclaration, packageDeclarationElement);
		}

		List<ImportDeclaration> importDeclarationList = (List<ImportDeclaration>) node.getStructuralProperty(CompilationUnit.IMPORTS_PROPERTY);
		Iterator<ImportDeclaration> itImports = importDeclarationList.iterator();
		while(itImports.hasNext()){
			ImportDeclaration importDeclaration = itImports.next();
			Element importDeclarationElement = compilationUnitElement.addElement(importDeclaration.getClass().getSimpleName());
			elements.put(importDeclaration, importDeclarationElement);
		}

		List<TypeDeclaration> typeDeclarationList = (List<TypeDeclaration>) node.getStructuralProperty(CompilationUnit.TYPES_PROPERTY);
		Iterator<TypeDeclaration> itTypes = typeDeclarationList.iterator();
		while(itTypes.hasNext()){
			TypeDeclaration typeDeclaration = itTypes.next();
			Element typeDeclarationElement = compilationUnitElement.addElement(typeDeclaration.getClass().getSimpleName());
			elements.put(typeDeclaration, typeDeclarationElement);
		}

		return super.visit(node);		
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		Element packageDeclarationElement = elements.get(node);
		if(packageDeclarationElement != null){
			Name name=(Name)node.getStructuralProperty(PackageDeclaration.NAME_PROPERTY);
			Element nameElement = packageDeclarationElement.addElement("Name");
			elements.put(name,nameElement);
			nameVisit(name,nameElement);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		Element importDeclarationElement = elements.get(node);
		if(importDeclarationElement != null){
			Name name=(Name)node.getStructuralProperty(PackageDeclaration.NAME_PROPERTY);
			Element nameElement = importDeclarationElement.addElement("Name");
			elements.put(name,nameElement);
			nameVisit(name,nameElement);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		Element typeDeclaration = elements.get(node);
		//get modifiers
		List<Modifier> modifierList = (List<Modifier>) node.getStructuralProperty(TypeDeclaration.MODIFIERS2_PROPERTY);
		if(modifierList != null){
			Iterator<Modifier> itMod = modifierList.iterator();
			while(itMod.hasNext()){
				Modifier modifier = itMod.next();
				Element modifierElement = typeDeclaration.addElement(modifier.getClass().getSimpleName());
				elements.put(modifier, modifierElement);
			}
		}

		//get simpleName
		SimpleName sname = (SimpleName)node.getStructuralProperty(TypeDeclaration.NAME_PROPERTY);
		Element snameElement = typeDeclaration.addElement(sname.getClass().getSimpleName());
		elements.put(sname, snameElement);

		//get type parameters
		List<TypeParameter> typeParameterList = (List<TypeParameter>)node.getStructuralProperty(TypeDeclaration.TYPE_PARAMETERS_PROPERTY);
		Iterator<TypeParameter> itTPM = typeParameterList.iterator();
		while(itTPM.hasNext()){
			TypeParameter tpm = itTPM.next();
			Element tpmElement = typeDeclaration.addElement(tpm.getClass().getSimpleName());
			elements.put(tpm, tpmElement);
		}

		//get super class
		Type type = (Type)node.getStructuralProperty(TypeDeclaration.SUPERCLASS_TYPE_PROPERTY);
		if(type != null){
			System.out.println(type.getClass().getSimpleName());
			Element typeElement = typeDeclaration.addElement("ExtendsType").addElement("Type");
			elements.put(type, typeElement);
			tpVisit(type, typeElement);
		}

		//get implement interfaces
		List<Type> typeList = (List<Type>)node.getStructuralProperty(TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY);
		if(typeList != null){
			Iterator<Type> itTP = typeList.iterator();
			while(itTP.hasNext()){
				Type tp = itTP.next();
				Element tpElement =  typeDeclaration.addElement("implementsType").addElement("Type");
				elements.put(tp,tpElement);
				tpVisit(tp, tpElement);
			}
		}

		//get body
		List<BodyDeclaration> bodyDeclarationList = (List<BodyDeclaration>) node.getStructuralProperty(TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		Iterator<BodyDeclaration> itBody = bodyDeclarationList.iterator();
		while(itBody.hasNext()){
			BodyDeclaration body = itBody.next();
			Element bodyElement = typeDeclaration.addElement(body.getClass().getSimpleName());
			elements.put(body, bodyElement);
		}
		return super.visit(node);
	}

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
			Element qnameElement = nameElement.addElement(qname.getClass().getSimpleName());
			elements.put(qname, qnameElement);
		}else{
			SimpleName sname = (SimpleName)name;
			Element snameElement = nameElement.addElement(sname.getClass().getSimpleName());
			elements.put(sname, snameElement);
		}
	}

	@Override
	public boolean visit(QualifiedName node) {
		Element qnameElement = elements.get(node);
		if(qnameElement != null){
			//get the Name part
			Name name = (Name)node.getStructuralProperty(QualifiedName.QUALIFIER_PROPERTY);
			Element nameElement = qnameElement.addElement("Name");
			elements.put(name, nameElement);
			nameVisit(name,nameElement);

			//get the SimpleName part
			SimpleName sname = (SimpleName)node.getStructuralProperty(QualifiedName.NAME_PROPERTY);
			Element snameElement = qnameElement.addElement(sname.getClass().getSimpleName());
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
		element.addElement("Identifier").setText(text);
	}

	@Override
	public boolean visit(TypeParameter node) {
		Element tpmElement = elements.get(node);
		if(tpmElement != null){
			//get SimpleName
			SimpleName sname= (SimpleName)node.getStructuralProperty(TypeParameter.NAME_PROPERTY);
			Element snameElement = tpmElement.addElement(sname.getClass().getSimpleName());
			elements.put(sname, snameElement);
			//get Type Bounds list
			List<Type> typeList = (List<Type>)node.getStructuralProperty(TypeParameter.TYPE_BOUNDS_PROPERTY);
			if(typeList != null){
				Iterator<Type> itTP = typeList.iterator();
				while(itTP.hasNext()){
					Type tp = itTP.next();
					Element tpElement =  tpmElement.addElement("TypeBound").addElement("Type");
					elements.put(tp,tpElement);
					tpVisit(tp, tpElement);
				}
			}
		}
		return super.visit(node);
	}

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
			Element tpElement = typeElement.addElement(tp.getClass().getSimpleName());
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
		// TODO Auto-generated method stub
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
			Element nameElement = stpElement.addElement("Name");
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
