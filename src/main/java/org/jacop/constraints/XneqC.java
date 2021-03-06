/**
 *  XneqC.java 
 *  This file is part of JaCoP.
 *
 *  JaCoP is a Java Constraint Programming solver. 
 *	
 *	Copyright (C) 2000-2008 Krzysztof Kuchcinski and Radoslaw Szymanek
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *  
 *  Notwithstanding any other provision of this License, the copyright
 *  owners of this work supplement the terms of this License with terms
 *  prohibiting misrepresentation of the origin of this work and requiring
 *  that modified versions of this work be marked in reasonable ways as
 *  different from the original version. This supplement of the license
 *  terms is in accordance with Section 7 of GNU Affero General Public
 *  License version 3.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.jacop.constraints;

import java.util.ArrayList;

import org.jacop.core.Domain;
import org.jacop.core.IntDomain;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.core.Var;

/**
 * Constraints X #\= C
 * 
 * @author Krzysztof Kuchcinski and Radoslaw Szymanek
 * @version 4.4
 */

public class XneqC extends PrimitiveConstraint {

	static int idNumber = 1;

	/**
	 * It specifies variable x in constraint x != c.
	 */
	public IntVar x;

	/**
	 * It specifies constant c in constraint x != c.
	 */
	public int c;

	/**
	 * It specifies the arguments required to be saved by an XML format as well as 
	 * the constructor being called to recreate an object from an XML format.
	 */
	public static String[] xmlAttributes = {"x", "c"};

	/**
	 * It constructs x != c constraint.
	 * @param x variable x.
	 * @param c constant c.
	 */
	public XneqC(IntVar x, int c) {

		assert (x != null) : "Variable x is null";

		numberId = idNumber++;
		numberArgs = 1;

		this.x = x;
		this.c = c;
	}

	@Override
	public ArrayList<Var> arguments() {

		ArrayList<Var> variables = new ArrayList<Var>(1);

		variables.add(x);
		return variables;
	}

	@Override
	public void consistency(Store store) {

		x.domain.inComplement(store.level, x, c);

	}

	@Override
	public int getNestedPruningEvent(Var var, boolean mode) {

		// If consistency function mode
		if (mode) {
			if (consistencyPruningEvents != null) {
				Integer possibleEvent = consistencyPruningEvents.get(var);
				if (possibleEvent != null)
					return possibleEvent;
			}
			return IntDomain.ANY;
		}
		// If notConsistency function mode
		else {
			if (notConsistencyPruningEvents != null) {
				Integer possibleEvent = notConsistencyPruningEvents.get(var);
				if (possibleEvent != null)
					return possibleEvent;
			}
			return IntDomain.ANY;
		}
	}

	@Override
	public int getConsistencyPruningEvent(Var var) {

		// If consistency function mode
			if (consistencyPruningEvents != null) {
				Integer possibleEvent = consistencyPruningEvents.get(var);
				if (possibleEvent != null)
					return possibleEvent;
			}
			return Domain.NONE;
		}

	@Override
	public int getNotConsistencyPruningEvent(Var var) {

	// If notConsistency function mode
			if (notConsistencyPruningEvents != null) {
				Integer possibleEvent = notConsistencyPruningEvents.get(var);
				if (possibleEvent != null)
					return possibleEvent;
			}
			return Domain.NONE;
	}

	@Override
	public void impose(Store store) {
		x.putModelConstraint(this, getConsistencyPruningEvent(x));
		store.addChanged(this);
		store.countConstraint();
	}

	@Override
	public void notConsistency(Store store) {

		x.domain.in(store.level, x, c, c);

	}

	@Override
	public boolean notSatisfied() {
		return x.singleton(c);
	}

	@Override
	public void removeConstraint() {
		x.removeConstraint(this);
	}

	@Override
	public boolean satisfied() {
		return !x.domain.contains(c);
	}

	@Override
	public String toString() {
		return id() + " : XneqC(" + x + ", " + c + " )";
	}

	@Override
	public void increaseWeight() {
		if (increaseWeight) {
			x.weight++;
		}
	}
	
}
