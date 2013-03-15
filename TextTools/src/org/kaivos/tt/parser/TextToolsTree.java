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
	 * 		NAME ("(" ("$" NAME ("," "$" NAME)*)? ")")? ":"
	 * 		(LIST ("," LIST)*)?
	 * 		";"
	 * }
	 */
	public static class NodeTree extends TreeNode {
		
		public String name;
		public ArrayList<ListTree> lists = new ArrayList<ListTree>();
		
		public boolean isFunc = false;
		public ArrayList<String> params = new ArrayList<>();
		
		@CompilerInfo public ArrayList<HashMap<String, String>> properties = new ArrayList<>();
		
		@Override
		public void parse(TokenScanner s) throws SyntaxError {
			if (isFunc = seek(s).equals(".")) {
				accept(".", s);
			}
			name = next(s);
			if (isFunc) {
				accept("(", s);
				if (!seek(s).equals(")"))
					while (true) {
						accept("$", s);
						params.add(next(s));
	
						if (accept(new String[] { ",", ")" }, s).equals(")"))
							break;
					}
				else
					accept(")", s);
			}
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
	 * 		"{"::F 
	 * 		(
	 * 			STRING
	 * 			| NAME
	 * 			| "[" NAME ("::" NAME)? "]"
	 * 			| NAME "=" VALUE
	 * 			| "(" VALUE ")"
	 * 			| ":" NAME
	 * 		) ("*"|"?"|"{"INT ("," INT)? "}")
	 * 		("=>" VALUE)?
	 * 		"}"::F
	 * 		
	 * }
	 */
	public static class ValueTree extends TreeNode {
		
		public String val, var, property, how_many;
		
		public int tstart, tend;
		
		public ValueTree val2, then, _else;
		public InnerNodeTree list;
		
		public boolean returnsString = true;

		public ArrayList<ValueTree> arguments = new ArrayList<>();
		
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
			else if (val.equals(":")) {
				var = next(s);
			}
			else if (val.equals(".")) {
				var = next(s);
				accept("(", s);
				if (!seek(s).equals(")"))
					while (true) {
						ValueTree t = new ValueTree();
						t.parse(s);	
						arguments.add(t);

						String str = accept(new String[] { ",", ")" }, s);

						if (str.equals(")"))
							break;
					}
				else
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
			
			if (seek(s).equals("=>")) {
				accept("=>", s);
				then = new ValueTree();
				then.parse(s);
				if (seek(s).equals("?>")) {
					accept("?>", s);
					_else = new ValueTree();
					_else.parse(s);
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
