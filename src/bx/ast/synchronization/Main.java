package bx.ast.synchronization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.dom4j.io.XMLWriter;

import bx.ast.ASTBuilder;
import bx.ast.xml.XMLBuilder;

public class Main {
	public static void main(String[] args){
		String fileName = "./test/Test.java";
		ASTBuilder ast = new ASTBuilder(readFile(fileName));
		XMLBuilder xml = new XMLBuilder();
		ast.astRootNode.accept(xml);
		try {
			XMLWriter output = new XMLWriter(new FileWriter(new File("testExample.xml")));
			output.write(xml.document);
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static String readFile(String fileName) {
		String result = "";
        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                //result += tempString;
            	result = result + tempString + '\n';
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return result;
    }
}
