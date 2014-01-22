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
	HashMap<Integer, String> modifierMap;

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
			packageDeclarationElement.addElement("PackageName").addElement("Identifier").setText(node.getName().toString());
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		Element importDeclarationElement = elements.get(node);
		if(importDeclarationElement != null){
			importDeclarationElement.addElement("Identifier").setText(node.getName().toString());
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		Element typeDeclaration = elements.get(node);
		Element clazz;
		if(node.isInterface()){
			clazz = typeDeclaration.addElement("InterfaceDeclaration");
		}else{
			clazz = typeDeclaration.addElement("ClassDeclaration");
		}

		//get modifiers
		List<Modifier> typeDeclarationList = (List<Modifier>) node.getStructuralProperty(TypeDeclaration.MODIFIERS2_PROPERTY);
		Iterator<Modifier> itMod = typeDeclarationList.iterator();
		while(itMod.hasNext()){
			Modifier modifier = itMod.next();
			Element modifierElement = clazz.addElement(modifier.getClass().getSimpleName());
			elements.put(modifier, modifierElement);
		}

		//get class name
		Element identifier = clazz.addElement("identifier");
		identifier.setText(node.getName().toString());

		//get super class
		if(node.getSuperclassType() != null){
			Element typeName = clazz.addElement("super").addElement("classType").addElement("typeName");
			typeName.addElement("identifier").setText(node.getSuperclassType().toString());
		}

		//get body
		List<BodyDeclaration> bodyDeclarationList = (List<BodyDeclaration>) node.getStructuralProperty(TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		Iterator<BodyDeclaration> itBody = bodyDeclarationList.iterator();
		while(itBody.hasNext()){
			BodyDeclaration body = itBody.next();
			Element bodyElement = clazz.addElement(body.getClass().getSimpleName());
			elements.put(body, bodyElement);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(Modifier node) {
		Element modifier= elements.get(node);
		if (modifier != null){
			modifier.setText(node.getKeyword().toString());
		}
		return super.visit(node);
	}

}
