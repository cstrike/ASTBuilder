package bx.ast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class ASTBuilder {
	public CompilationUnit astRootNode;
	
	private static CompilationUnit parse(String content) {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);     //to parse compilation unit 
        parser.setSource(content.toCharArray());          //content is a string which stores the java source 
        parser.setResolveBindings(true); 
        CompilationUnit result = (CompilationUnit) parser.createAST(null);
        return result;
    }
	
	public ASTBuilder(String content){
		astRootNode = parse(content);		
	}
}
