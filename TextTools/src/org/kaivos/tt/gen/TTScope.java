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

public class TTScope {

	private TTScope _super;
	
	private HashMap<String, String> vars = new HashMap<>();
	
	public TTScope() {
	}
	
	public TTScope(TTScope s) {
		_super = s;
	}
	
	public void putNew(String var, String val) {
		vars.put(var, val);
	}
	
	public void put(String var, String val) {
		if (vars.containsKey(var) || _super==null) vars.put(var, val);
		else _super.put(var, val);
	}
	
	public String get(String var) {
		if (vars.containsKey(var) || _super==null) return vars.get(var);
		else return _super.get(var);
	}

	public TTScope getSuper() {
		return _super;
	}
}
