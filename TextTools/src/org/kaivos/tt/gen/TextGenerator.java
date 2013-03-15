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

package org.kaivos.tt.gen;

import java.util.HashMap;
import java.util.Random;

import org.kaivos.tt.parser.TextToolsTree.GlobalTree;
import org.kaivos.tt.parser.TextToolsTree.InnerNodeTree;
import org.kaivos.tt.parser.TextToolsTree.ListTree;
import org.kaivos.tt.parser.TextToolsTree.NodeTree;
import org.kaivos.tt.parser.TextToolsTree.StartTree;
import org.kaivos.tt.parser.TextToolsTree.ValueTree;

public class TextGenerator {

	private static class ImpossibleException extends Exception {
		private static final long serialVersionUID = 1219484843485542860L;
	}
	
	private final Random rnd;
	
	public TextGenerator() {
		rnd = new Random();
	}
	
	public TextGenerator(long seed) {
		rnd = new Random(seed);
	}
	
	public String node;
	
	private NodeTree _node;
	private StartTree start;
	
	private HashMap<String, String> properties;
	
	private HashMap<String, String> vars = new HashMap<>();
	
	public String generate(StartTree tree) {
		start = tree;
		
		node = "<top>";
		
		for (GlobalTree g : start.globals) {
			try {
				vars.put(g.name, generateValue(g.val));
			} catch (ImpossibleException e) {
				e.printStackTrace();
			}
		}
		
		for (NodeTree node : start.nodes) {
			if (node.name.equals("start")) {
				return generateNode(node);
			}
		}
	
	
		throw new RuntimeException("Unresolved node " + "start");
	}

	private String generateNode(NodeTree t) {
		String n = node; NodeTree _n = _node; HashMap<String, String> p = properties;
		node = t.name; _node = t;
		
		properties = new HashMap<String, String>();
		
		int i = 0;
		
		do {
			if (++i > t.lists.size()) throw new RuntimeException();
			int list = rnd.nextInt(t.lists.size());
			try {
				String s = generateList(t.lists.get(list));
				node = n; _node = _n;
				
				t.properties.add(properties);
				
				properties = p;
				
				return s;
			} catch (ImpossibleException ex) {
				continue;
			}
		} while (true);
		
	}
	
	private String generateInnerNode(InnerNodeTree t) throws ImpossibleException {
		int i = 0;
		do {
			if (i++ > t.lists.size()) throw new ImpossibleException();
			int list = rnd.nextInt(t.lists.size());
			try {
				String s = generateList(t.lists.get(list));
				
				return s;
			} catch (ImpossibleException ex) {
				continue;
			}
		} while (true);
	}

	private String generateList(ListTree t) throws ImpossibleException {
		String a = "";
		for (ValueTree t2 : t.values) {
			a += generateValue(t2);
		}
		return a;
	}

	private String generateValue(ValueTree t) throws ImpossibleException {
		
		int times = 1;
		if ("?".equals(t.how_many)) times = rnd.nextInt(2);
		if ("*".equals(t.how_many)) times = rnd.nextInt(10);
		if ("{".equals(t.how_many)) {
			times = rnd.nextInt(t.tend-t.tstart+1)+t.tstart;
		}
		
		String ans = "";
		
		loop: for (int i = 0; i < times; i++) {
		
			if (t.val.startsWith("\"")) {
				if (t.returnsString) ans += t.val.substring(1, t.val.length()-1);
				continue;
			}
			else if (t.val.equals("$")){
				if (t.val2 != null){
					String s = generateValue(t.val2);
					vars.put(t.var, s);
					if (t.returnsString) ans += s;
					continue;
				}
				if (vars.get(t.var)==null) throw new NullPointerException(t.var + " is null");
				if (t.returnsString) ans += vars.get(t.var);
				continue;
			}
			else if (t.val.equals("[")){
				for (NodeTree node : start.nodes) {
					if (node.name.equals(t.var)) {
						//System.err.println(node.properties);
						
						if (node.properties.size() == 0) throw new ImpossibleException();
						
						int list = rnd.nextInt(node.properties.size());
						
						if (t.returnsString) ans += node.properties.get(list).get(t.property);
						continue loop;
					}
				}
				
				throw new RuntimeException("Unresolved node " + t.var);
			}
			else if (t.val.equals("(")){
				if (t.returnsString) ans += generateInnerNode(t.list);
				continue;
			}
			else if (t.val2 != null){
				String s = generateValue(t.val2);
				properties.put(t.val, s);
				if (t.returnsString) ans += s;
				continue;
			}
			else {
				for (NodeTree node : start.nodes) {
					if (node.name.equals(t.val)) {
						if (t.returnsString) ans += generateNode(node);
						continue loop;
					}
				}
				
				throw new RuntimeException("Unresolved node " + t.val);
			}
		
		}
		
		return ans;
	}

}
