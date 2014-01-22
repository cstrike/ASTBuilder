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
		
		List<ImportDeclaration> importDeclarationList = (List<ImportDeclaration>) node.getStructuralProperty(node.IMPORTS_PROPERTY);
		Iterator<ImportDeclaration> itImports = importDeclarationList.iterator();
		while(itImports.hasNext()){
			ImportDeclaration importDeclaration = itImports.next();
			Element importDeclarationElement = compilationUnitElement.addElement(importDeclaration.getClass().getSimpleName());
			elements.put(importDeclaration, importDeclarationElement);
		}
		
		List<TypeDeclaration> typeDeclarationList = (List<TypeDeclaration>) node.getStructuralProperty(node.TYPES_PROPERTY);
		Iterator<TypeDeclaration> itTypes = typeDeclarationList.iterator();
		while(itTypes.hasNext()){
			TypeDeclaration typeDeclaration = itTypes.next();
			Element typeDeclarationElement = compilationUnitElement.addElement(typeDeclaration.getClass().getSimpleName());
			elements.put(typeDeclaration, typeDeclarationElement);
		}
		
		return super.visit(node);		
	}

}
