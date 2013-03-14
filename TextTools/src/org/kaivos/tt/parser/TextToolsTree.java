package org.kaivos.tt.parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.kaivos.parsertools.ParserTree;
import org.kaivos.sc.TokenScanner;
import org.kaivos.stg.error.SyntaxError;

public class TextToolsTree extends ParserTree {
	
	public @interface CompilerInfo {}
	
	/*
	 * Start = {
	 * 		NODE*
	 * }
	 */
	public static class StartTree extends TreeNode {
		
		public ArrayList<NodeTree> nodes = new ArrayList<NodeTree>();
		public ArrayList<CommandTree> cmds = new ArrayList<>();
		public ArrayList<GlobalTree> globals = new ArrayList<>();
		
		@Override
		public void parse(TokenScanner s) throws SyntaxError {
			while (!seek(s).equals("<EOF>")) {
				if (seek(s).equals("!")) {
					CommandTree t = new CommandTree();
					t.parse(s);
					cmds.add(t);
				} else if (seek(s).equals("$")) {
					GlobalTree t = new GlobalTree();
					t.parse(s);
					globals.add(t);
				} else {
					NodeTree t = new NodeTree();
					t.parse(s);
					nodes.add(t);
				}
			}
			accept("<EOF>", s);
			
		}

		@Override
		public String generate(String a) {
			return null;
		}
		
	}

	/*
	 * Command = {
	 * 		"!" NAME ARG* ";"
	 * }
	 */
	public static class CommandTree extends TreeNode {
		
		public String name;
		public ArrayList<String> args = new ArrayList<>();
		
		@Override
		public void parse(TokenScanner s) throws SyntaxError {
			accept("!", s);
			
			name = next(s);
			
			while (!seek(s).equals(";")) {
				args.add(next(s));
			}
			
			accept(";", s);
			
		}

		@Override
		public String generate(String a) {
			return null;
		}
		
	}
	
	/*
	 * Global = {
	 * 		"$" NAME "=" VALUE ";"
	 * }
	 */
	public static class GlobalTree extends TreeNode {
		
		public String name;
		public ValueTree val;
		
		@Override
		public void parse(TokenScanner s) throws SyntaxError {
			accept("$", s);
			
			name = next(s);
			
			accept("=", s);
			
			val = new ValueTree();
			val.parse(s);
			
			if (seek(s).equals(";")) accept(";", s);
			
		}

		@Override
		public String generate(String a) {
			return null;
		}
		
	}
	
	/*
	 * Node = {
	 * 		NAME ":"
	 * 		(LIST ("," LIST)*)?
	 * 		";"
	 * }
	 */
	public static class NodeTree extends TreeNode {
		
		public String name;
		public ArrayList<ListTree> lists = new ArrayList<ListTree>();
		
		@CompilerInfo public ArrayList<HashMap<String, String>> properties = new ArrayList<>();
		
		@Override
		public void parse(TokenScanner s) throws SyntaxError {
			name = next(s);
			accept(":", s);
			while (true) {
				ListTree t = new ListTree();
				t.parse(s);
				lists.add(t);
				if (seek(s).equals(",")) {
					accept(",", s);
					continue;
				}
				if (seek(s).equals("|")) {
					accept("|", s);
					continue;
				}
				else break;
			}
			accept(";", s);
			
		}

		@Override
		public String generate(String a) {
			return null;
		}
		
	}
	
	/*
	 * List = {
	 * 		VALUE*
	 * }
	 */
	public static class ListTree extends TreeNode {
		
		public ArrayList<ValueTree> values = new ArrayList<ValueTree>();
		
		@Override
		public void parse(TokenScanner s) throws SyntaxError {
			while (!seek(s).equals(",") && !seek(s).equals("|") && !seek(s).equals(";") && !seek(s).equals(")")) {
				ValueTree t = new ValueTree();
				t.parse(s);
				values.add(t);
			}

		}

		@Override
		public String generate(String a) {
			return null;
		}
		
	}
	
	/*
	 * InnerNode = {
	 * 		(LIST ("," LIST)*)?
	 * }
	 */
	public static class InnerNodeTree extends TreeNode {
		
		public ArrayList<ListTree> lists = new ArrayList<ListTree>();
		
		@Override
		public void parse(TokenScanner s) throws SyntaxError {
			while (true) {
				ListTree t = new ListTree();
				t.parse(s);
				lists.add(t);
				if (seek(s).equals(",")) {
					accept(",", s);
					continue;
				}
				if (seek(s).equals("|")) {
					accept("|", s);
					continue;
				}
				else break;
			}
		}

		@Override
		public String generate(String a) {
			return null;
		}
		
	}
	
	/*
	 * Value = {
	 * 		STRING | NAME | "[" NAME ("::" NAME)? "]" | NAME "=" VALUE | 
	 * }
	 */
	public static class ValueTree extends TreeNode {
		
		public String val, var, property, how_many;
		
		public int tstart, tend;
		
		public ValueTree val2;
		public InnerNodeTree list;
		
		public boolean returnsString = true;
		
		@Override
		public void parse(TokenScanner s) throws SyntaxError {
			
			if (seek(s).equals("{")) {
				accept("{", s);
				returnsString = false;
			}
			
			val = next(s);
			if (val.equals("$")) {
				var = next(s);
				if (seek(s).equals("=")) {
					accept("=", s);
					val2 = new ValueTree();
					val2.parse(s);
				}
			}
			else if (val.equals("[")) {
				var = next(s);
				if (seek(s).equals("::")) {
					accept("::", s);
					property = next(s);
				} else property = "name";
				accept("]", s);
			}
			else if (val.equals("(")) {
				list = new InnerNodeTree();
				list.parse(s);
				accept(")", s);
			}
			else if (seek(s).equals("=")) {
				accept("=", s);
				val2 = new ValueTree();
				val2.parse(s);
			}
			
			if (seek(s).equals("{") || seek(s).equals("?") || seek(s).equals("*")) {
				how_many = accept(new String[] {"{", "?", "*"}, s);
				
				if (how_many.equals("{")) {
					int i = Integer.parseInt(next(s));
					if (!seek(s).equals(",")) {
						tstart = 1;
						tend = i;
					} else {
						tstart = i;
						accept(",", s);
						tend = Integer.parseInt(next(s));
					}
					accept("}", s);
				}
			}
			
			if (!returnsString) accept("}", s);
		}

		@Override
		public String generate(String a) {
			return null;
		}
		
	}
}
