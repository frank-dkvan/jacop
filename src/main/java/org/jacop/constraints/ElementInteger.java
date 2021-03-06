/**
 *  ElementInteger.java 
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
import java.util.TreeMap;

import org.jacop.core.IntDomain;
import org.jacop.core.Interval;
import org.jacop.core.IntVar;
import org.jacop.core.IntervalDomain;
import org.jacop.core.Store;
import org.jacop.core.ValueEnumeration;
import org.jacop.core.Var;

/**
 * ElementInteger constraint defines a relation 
 * list[index - indexOffset] = value.
 * 
 * The first element of the list corresponds to index - indexOffset = 1.
 * By default indexOffset is equal 0 so first value within a list corresponds to index equal 1.
 * 
 * If index has a domain from 0 to list.length-1 then indexOffset has to be equal -1 to 
 * make addressing of list array starting from 1.
 * 
 * @author Radoslaw Szymanek and Krzysztof Kuchcinski
 * @version 4.4
 */

public class ElementInteger extends Constraint {

	static int idNumber = 1;

	boolean firstConsistencyCheck = true;
	int firstConsistencyLevel;

	/**
	 * It specifies the maximal size of index domain when the constraint will apply domain consistency for value.
	 * Otherwise bound consistency is applied. This limit applies to both duplicates and index.
	 */
        static final int limitForDomainPruning = 100;
	
	/**
	 * It specifies variable index within an element constraint list[index-indexOffset] = value.
	 */
	public IntVar index;

	/**
	 * It specifies variable value within an element constraint list[index-indexOffset] = value.
	 */
	public IntVar value;

	/**
	 * It specifies indexOffset within an element constraint list[index-indexOffset] = value.
	 */
	public final int indexOffset;

	/**
	 * It specifies whether duplicate values should be treated specially (combined to a single check).
	 * In general a good idea but when lists are long it makes the process slower instead of faster.
	 */
	public final boolean checkDuplicates;

        /**
	 * It specifies list of variables within an element constraint list[index-indexOffset] = value.
	 * The list is addressed by positive integers ({@code >=1}) if indexOffset is equal to 0. 
	 */
	public int list[];

	/**
	 * It specifies for each value what are the possible values of the index variable (it 
	 * takes into account indexOffset. 
	 */
	// Hashtable<Integer, IntDomain> mappingValuesToIndex = new Hashtable<Integer, IntDomain>();

	boolean indexHasChanged = true;
	boolean valueHasChanged = true;

	/**
	 * It holds information about the positions within list array that are equal. It allows
	 * to safely skip duplicates when enumerating index domain. 
	 * duplicatesIndexes is a domain having indexes of all indexes for duplicates.
	 */
	ArrayList<IntDomain> duplicates;
        IntDomain duplicatesIndexes;
    
	/**
	 * It specifies the arguments required to be saved by an XML format as well as 
	 * the constructor being called to recreate an object from an XML format.
	 */	
	public static String[] xmlAttributes = {"index", "list", "value", "indexOffset"};

	/**
	 * It constructs an element constraint. 
	 * 
	 * @param index variable index
	 * @param list list of integers from which an index-th element is taken
	 * @param value a value of the index-th element from list
	 * @param indexOffset shift applied to index variable. 
	 */
    public ElementInteger(IntVar index, int[] list, IntVar value, int indexOffset) {

		this.indexOffset = indexOffset;
		this.checkDuplicates = true;
		commonInitialization(index, list, value);
		
	}

    public ElementInteger(IntVar index, int[] list, IntVar value, int indexOffset, boolean checkDuplicates) {

		this.indexOffset = indexOffset;
		this.checkDuplicates = checkDuplicates;
		commonInitialization(index, list, value);
		
	}

	private void commonInitialization(IntVar index, int[] list, IntVar value) {

	        queueIndex = 2;

		assert (index != null) : "Argument index is null";
		assert (list != null) : "Argument list is null";
		assert (value != null) : "Argument value is null";
				
		this.numberId = idNumber++;
		this.index = index;
		this.value = value;
		this.numberArgs = (short) (numberArgs + 2);
		this.list = new int[list.length];
		this.queueIndex = 1;
		
		for (int i = 0; i < list.length; i++) {
						
			Integer listElement = list[i];
			this.list[i] = list[i];
			
// 			IntDomain oldFD = mappingValuesToIndex.get(listElement);
// 			if (oldFD == null) {
//  			    mappingValuesToIndex.put(listElement, new IntervalDomain(i + 1 + indexOffset, i + 1 + indexOffset));
// 			}
// 			else
//     			    ((IntervalDomain)oldFD).addLastElement(i + 1 + indexOffset);
// //     			    oldFD.unionAdapt(i + 1 + indexOffset, i + 1 + indexOffset);
			
		}

	}
	
	/**
	 * It constructs an element constraint with default indexOffset equal 0.
	 * 
	 * @param index index variable.
	 * @param list list containing variables which one pointed out by index variable is made equal to value variable.  
	 * @param value a value variable equal to the specified element from the list. 
	 */
	public ElementInteger(IntVar index, ArrayList<Integer> list, IntVar value) {

		this(index, list, value, 0);
		
	}

	/**
	 * It constructs an element constraint. 
	 * 
	 * @param index variable index
	 * @param list list of integers from which an index-th element is taken
	 * @param value a value of the index-th element from list
	 * @param indexOffset shift applied to index variable. 
	 */
	public ElementInteger(IntVar index, ArrayList<Integer> list, IntVar value, int indexOffset) {
		
		this.indexOffset = indexOffset;
		this.checkDuplicates = true;
		
		int [] listOfInts = new int[list.size()];
		for (int i = 0; i < list.size(); i++)
			listOfInts[i] = list.get(i);
		
		commonInitialization(index, listOfInts, value);
		
	}

	/**
	 * It constructs an element constraint. 
	 * 
	 * @param index variable index
	 * @param list list of integers from which an index-th element is taken
	 * @param value a value of the index-th element from list
	 * @param indexOffset shift applied to index variable. 
	 * @param checkDuplicates informs whether to create duplicates list for values from list (default = true). 
	 */
        public ElementInteger(IntVar index, ArrayList<Integer> list, IntVar value, int indexOffset, boolean checkDuplicates) {
		
		this.indexOffset = indexOffset;
		this.checkDuplicates = checkDuplicates;
		
		int [] listOfInts = new int[list.size()];
		for (int i = 0; i < list.size(); i++)
			listOfInts[i] = list.get(i);
		
		commonInitialization(index, listOfInts, value);
		
	}
    
	/**
	 * It constructs an element constraint with indexOffset by default set to 0.  
	 * 
	 * @param index variable index
	 * @param list list of integers from which an index-th element is taken
	 * @param value a value of the index-th element from list
	 */

	ElementInteger(IntVar index, int[] list, IntVar value) {

		this(index, list, value, 0);
		
	}


	@Override
	public ArrayList<Var> arguments() {

		ArrayList<Var> variables = new ArrayList<Var>(2);

		variables.add(index);
		variables.add(value);
		
		return variables;
		
	}

	@Override
	public void removeLevel(int level) {
		if (level == firstConsistencyLevel)
			firstConsistencyCheck = true;
	}

	@Override
	public void consistency(Store store) {

		if (firstConsistencyCheck) {

			index.domain.in(store.level, index, 1 + indexOffset, list.length + indexOffset);
			firstConsistencyCheck = false;
			firstConsistencyLevel = store.level;

		}

		
		boolean copyOfValueHasChanged = valueHasChanged;
		
		if (indexHasChanged) {

			indexHasChanged = false;
			IntDomain indexDom = index.dom().cloneLight();
			IntDomain domValue = new IntervalDomain(5);

			if (checkDuplicates) 
			    // if (indexDom.getSize() < limitForDomainPruning)
				for (IntDomain duplicate : duplicates) {
				    if (indexDom.isIntersecting(duplicate)) {
					if (domValue.getSize() == 0)
					    domValue.unionAdapt(list[duplicate.min() - 1 - indexOffset]);
					else
					    ((IntervalDomain)domValue).addLastElement(list[duplicate.min() - 1 - indexOffset]);

					// indexDom = indexDom.subtract(duplicate);
				    }
				}
				// else {
				//     int min = IntDomain.MaxInt, max = IntDomain.MinInt;
				//     for (IntDomain duplicate : duplicates) {
				// 	if (indexDom.isIntersecting(duplicate)) {					
				// 	    int valueOfElement = list[duplicate.min() - 1 - indexOffset];
					    
				// 	    min = Math.min(min, valueOfElement);
				// 	    max = Math.max(max, valueOfElement);

				// 	    // indexDom = indexDom.subtract(duplicate);
				// 	}
				//     }
				//     domValue.unionAdapt(min, max);
				// }
			
			indexDom = indexDom.subtract(duplicatesIndexes);

			if (indexDom.getSize() < limitForDomainPruning) { // domain consistency for small index domains
			    // values of index for duplicated values within list are already taken care of above.
			    for (ValueEnumeration e = indexDom.valueEnumeration(); e.hasMoreElements();) {
				int valueOfElement = list[e.nextElement() - 1 - indexOffset];
				domValue.unionAdapt(valueOfElement);
			    }

			    value.domain.in(store.level, value, domValue);
			    valueHasChanged = false;
			}
			else {  // bound consistency for large index domains
			    // values of index for duplicated values within list are already taken care of above.
			    int min = IntDomain.MaxInt, max = IntDomain.MinInt;
			    for (ValueEnumeration e = indexDom.valueEnumeration(); e.hasMoreElements();) {
				int valueOfElement = list[e.nextElement() - 1 - indexOffset];

				min = Math.min(min, valueOfElement);
				max = Math.max(max, valueOfElement);

			    }
			    domValue.unionAdapt(min, max);

			    value.domain.in(store.level, value, domValue);
			    valueHasChanged = false;
			}
		}

		// the if statement above can change value variable but those changes can be ignored.
		if (copyOfValueHasChanged) {

		    valueHasChanged = false;

		    IntervalDomain indexDom = new IntervalDomain(5);
		    for (ValueEnumeration e = index.domain.valueEnumeration(); e.hasMoreElements();) {
			int position = e.nextElement() - 1 - indexOffset;
			int val = list[position];
		    
			if (disjoint(value.domain, val))
			    if (indexDom.size == 0)
				indexDom.unionAdapt(position + 1 + indexOffset);
			    else
			    	// indexes are in ascending order and can be added at the end if the last element
			    	// plus 1 is not equal a new value. In such case the max must be changed.
				indexDom.addLastElement(position + 1 + indexOffset);
		    }

		    index.domain.in(store.level, index, indexDom.complement());
		    indexHasChanged = false;
			
		}

		// !!! removing this part since it is too slow; specially addDom is very costly
		// !!! the version above is much faster
		// if (copyOfValueHasChanged) {
		
		// 	valueHasChanged = false;
		// 	IntDomain valDom = value.dom();
		// 	IntDomain domIndex = new IntervalDomain(5);

		// 	for (ValueEnumeration e = valDom.valueEnumeration(); e.hasMoreElements();) {
		// 		IntDomain i = mappingValuesToIndex.get(e.nextElement());
		// 		if (i != null) {
		// 		    domIndex.addDom(i);
		// 		}
		// 	}

		// 	index.domain.in(store.level, index, domIndex);
		// 	indexHasChanged = false;
			
		// }
		
	}

    boolean disjoint(IntDomain v1, int v2) {
	if (v1.min() > v2 || v2 > v1.max()) 
	    return true;
	else
	    if (! v1.contains(v2))
		return true;
	    else
		return false;
    }
    

	@Override
	public int getConsistencyPruningEvent(Var var) {

		// If consistency function mode
			if (consistencyPruningEvents != null) {
				Integer possibleEvent = consistencyPruningEvents.get(var);
				if (possibleEvent != null)
					return possibleEvent;
			}
			return IntDomain.ANY;
	}

	@Override
	public void impose(Store store) {

		index.putModelConstraint(this, getConsistencyPruningEvent(index));
		value.putModelConstraint(this, getConsistencyPruningEvent(value));

		store.addChanged(this);
		store.countConstraint();

		if (checkDuplicates) {
		    duplicates = new ArrayList<IntDomain>();
		
		    TreeMap<Integer, IntervalDomain> map = new TreeMap<Integer, IntervalDomain>();

		    for (int pos = 0; pos < list.length; pos++) {
		
			int el = list[pos];
			IntervalDomain indexes = map.get(el);
			int elementIndex = pos + 1 + indexOffset;
			if (indexes == null) {
			    indexes = new IntervalDomain(elementIndex, elementIndex);
			    map.put(el, indexes);
			}
			else 
			    indexes.addLastElement(elementIndex);
		    }

		    duplicatesIndexes = new IntervalDomain();
		    for (IntDomain duplicate: map.values()) {
			if ( duplicate.getSize() > 10 ) {
				duplicates.add(duplicate);

				duplicatesIndexes.unionAdapt(duplicate);
			}
		    }
		}

		valueHasChanged = true;
		indexHasChanged = true;
	}

	@Override
	public void queueVariable(int level, Var var) {
		if (var == index)
			indexHasChanged = true;
		else
			valueHasChanged = true;
	}

	@Override
	public void removeConstraint() {
		index.removeConstraint(this);
		value.removeConstraint(this);
	}

	@Override
	public boolean satisfied() {

		if (value.singleton()) {
		
			int v = value.min();

            IntDomain duplicate = null;

            for (IntDomain d : duplicates) {
                if (index.domain.isIntersecting(d)) {
                    duplicate = d;
                    break;
                }
            }

			if (duplicate == null) {
				
				if (!index.singleton())
					return false;
				else 
					if (list[index.value() - 1 - indexOffset] == v)
						return true;
			
			}
			else {
			
				if (duplicate.contains(index.domain) && list[index.min() - 1 - indexOffset] == v)
					return true;
			
			}
			
			return false;
		
		}
		else
			return false;
		
	}

	@Override
	public String toString() {
		
		StringBuffer result = new StringBuffer( id() );
		
		result.append(" : elementInteger").append("( ").append(index).append(", [");
		
		for (int i = 0; i < list.length; i++) {
			result.append( list[i] );
			
			if (i < list.length - 1)
				result.append(", ");
		}
		
		result.append("], ").append(value).append(", " + indexOffset + " )");

		return result.toString();
	}

    @Override
	public void increaseWeight() {
		if (increaseWeight) {
			index.weight++;
			value.weight++;
		}
	}

}
