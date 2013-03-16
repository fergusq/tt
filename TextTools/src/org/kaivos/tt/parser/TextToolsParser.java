/*
 * Text Tools Text generator
 * Copyright (C) 2013 IH (fergusq)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kaivos.tt.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
//import java.io.InputStreamReader;

import org.kaivos.sc.TokenScanner;
import org.kaivos.stg.error.SyntaxError;
import org.kaivos.stg.error.UnexpectedTokenSyntaxError;
import org.kaivos.tt.gen.ImpossibleException;
import org.kaivos.tt.gen.TextGenerator;

public class TextToolsParser {
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Wrong number of arguments!");
			return;
		}
		String seed = null;
		
		class Var<K, V> {
			public Var(K n, V v) {
				name = n; value = v;
			}
			public K name;
			public V value;
		}
		
		ArrayList<Var<String, String>> vars = new ArrayList<>();
		if (args.length > 0) {
			loop: for (int i = 0; i < args.length; i++) {
				String arg = args[i];
				if (arg.startsWith("-")) {
					switch (arg) {
					case "-h":
					case "--help":
						System.out.println(
									"Usage: tt [-s seed] [--seed seed]      \n" +
									"[-g var value] [--global var value]    \n" +
									"                           Process file\n" +
									"       tt [-h|--help]      Show this text and exit\n" +
									"       tt [-v|--version]   Show version and exit\n" +
									"       tt [--version-str]  Show version string and exit"
						);
						return;
					case "-v":
					case "--version":
						System.out.println(
								"TextTools (v0.1) - Copyright (C) 2013 IH (fergusq)"
						);
					case "--version-str":
						System.out.println(
								"VERSION 0001"		
						);
						return;
					case "-s":
					case "--seed":
						seed = args[++i];
						break;
					case "-g":
					case "--global":
						vars.add(new Var<>(args[++i], args[++i]));
						break;
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
		s.setSpecialTokens(new char[] { ';', ',', ':', '$', '[', ']', '=', '(', ')', '*', '?', '{', '}', '|', '!', '.', '<', '>'});
		s.setBSpecialTokens(new String[] {"::", "=>", "?>", ">>"});
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
			
			for (Var<String, String> v : vars) {
				compiler.vars.put(v.name, v.value);
			}
			
			try {
				System.out.println(compiler.generate(tree));
			} catch (ImpossibleException e) {
				System.err.println("; E: ("+compiler.node+") It's impossible to generate text! There's error in " + args[args.length-1]);
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
