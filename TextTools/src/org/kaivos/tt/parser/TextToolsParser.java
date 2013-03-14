package org.kaivos.tt.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
//import java.io.InputStreamReader;

import org.kaivos.sc.TokenScanner;
import org.kaivos.stg.error.SyntaxError;
import org.kaivos.stg.error.UnexpectedTokenSyntaxError;
import org.kaivos.tt.gen.TextGenerator;

public class TextToolsParser {
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Wrong number of arguments!");
			return;
		}
		if (args.length > 0) {
			loop: for (int i = 0; i < args.length; i++) {
				String arg = args[i];
				if (arg.startsWith("-")) {
					switch (arg) {
					case "-h":
					case "--help":
						System.out.println(
									"Usage: tt file             Process file\n" +
									"       tt [-h|--help]      Show this text and exit\n" +
									"       tt [-v|--version]   Show version and exit\n" +
									"       tt [--version-str]  Show version string and exit"
						);
						return;
					case "-v":
					case "--version":
						System.out.println(
								"TextTools (v0.1) - (c) 2013 IH (fergusq) - All rights reserved\n"
						);
					case "--version-str":
						System.out.println(
								"VERSION 0001"		
						);
						return;
					default:
						break loop;
					}
				}
			}
		}
		
		BufferedReader in = null;
		{
			try {
				in = new BufferedReader(new FileReader(new File(args[args.length-1])));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				return;
			}
		}
		
		String textIn = "";
		try {
			while (in.ready())
				textIn += in.readLine() + "\n";
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}

		TokenScanner s = new TokenScanner();
		s.setSpecialTokens(new char[] { ';', ',', ':', '$', '[', ']', '=', '(', ')', '*', '?', '{', '}', '|', '!', '<', '>'});
		s.setBSpecialTokens(new String[] {"::"});
		s.setComments(true);
		s.setPrep(false);
		s.init(textIn);
		// System.out.println(s.getTokenList());
		TextToolsTree.StartTree tree = new TextToolsTree.StartTree();

		try {
			tree.parse(s);
			try {
				in.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			TextGenerator compiler = new TextGenerator();
			try {
				System.out.println(compiler.generate(tree));
			} catch (Exception e) {
				System.err.println("; E: ("+compiler.node+") Internal Compiler Exception");
				e.printStackTrace();
			}
		} catch (UnexpectedTokenSyntaxError e) {

			if (e.getExceptedArray() == null) {
				System.err.println("[" + e.getFile() + ":" + e.getLine()
						+ "] Syntax error on token '" + e.getToken()
						+ "', excepted '" + e.getExcepted() + "'");
				 //e.printStackTrace();
			} else {
				System.err.println("[" + e.getFile() + ":" + e.getLine()
						+ "] Syntax error on token '" + e.getToken()
						+ "', excepted one of:");
				for (String token : e.getExceptedArray()) {
					System.err.println("[Line " + e.getLine() + "] \t\t'"
							+ token + "'");
				}
			}

			System.err.println("[Line " + e.getLine() + "] Line: '"
					+ s.getLine(e.getLine() - 1).trim() + "'");
		} catch (SyntaxError e) {

			System.err.println("[Line " + e.getLine() + "] " + e.getMessage());
			System.err.println("[Line " + e.getLine() + "] \t"
					+ s.getLine(e.getLine() - 1).trim());
		} catch (StackOverflowError e) {
			System.err.println("Stack overflow exception! (" + e.getStackTrace()[0].getMethodName() + ")");
		}

		{
			// SveCodeGenerator.CGStartTree gen = new
			// SveCodeGenerator.CGStartTree(tree);
			// System.out.println(gen.generate(""));
		}
	}
}
