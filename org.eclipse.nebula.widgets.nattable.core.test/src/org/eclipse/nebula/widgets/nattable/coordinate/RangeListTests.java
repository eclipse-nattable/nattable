/*******************************************************************************
 * Copyright (c) 2013 Stephan Wahlbrink and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 ******************************************************************************/

package org.eclipse.nebula.widgets.nattable.coordinate;

import static org.junit.Assert.assertArrayEquals;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.coordinate.RangeList;
import org.junit.Test;


public class RangeListTests {
	
	
	private RangeList create() {
		RangeList list = new RangeList();
		list.add(new Range(10, 20));
		list.add(new Range(30, 40));
		list.add(new Range(50, 60));
		list.add(new Range(70, 80));
		list.add(new Range(81, 100));
		return list;
	}
	
	
	public void addRange() {
		RangeList list = create();
		
		assertArrayEquals(new Object[] {
				new Range(10, 20), new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	
	@Test
	public void addValueBefore() {
		RangeList list = create();
		
		list.addValue(8);
		
		assertArrayEquals(new Object[] {
				new Range(8, 9),
				new Range(10, 20), new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void addValueBetweenRanges() {
		RangeList list = create();
		
		list.addValue(21);
		
		assertArrayEquals(new Object[] {
				new Range(10, 20),
				new Range(21, 22),
				new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void addValueBeforeBegin() {
		RangeList list = create();
		
		list.addValue(9);
		
		assertArrayEquals(new Object[] {
				new Range(9, 20),
				new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void addValueInRange1() {
		RangeList list = create();
		
		list.addValue(10);
		
		assertArrayEquals(new Object[] {
				new Range(10, 20), new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void addValueInRange2() {
		RangeList list = create();
		
		list.addValue(11);
		
		assertArrayEquals(new Object[] {
				new Range(10, 20), new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void addValueInRange3() {
		RangeList list = create();
		
		list.addValue(19);
		
		assertArrayEquals(new Object[] {
				new Range(10, 20), new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void addValueAtEnd() {
		RangeList list = create();
		
		list.addValue(20);
		
		assertArrayEquals(new Object[] {
				new Range(10, 21),
				new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void addValueMergeRanges() {
		RangeList list = create();
		
		list.addValue(80);
		
		assertArrayEquals(new Object[] {
				new Range(10, 20), new Range(30, 40), new Range(50, 60),
				new Range(70, 100)
		}, list.toArray() );
	}
	
	
	@Test
	public void addRangeBefore() {
		RangeList list = create();
		
		list.add(new Range(5, 9));
		
		assertArrayEquals(new Object[] {
				new Range(5, 9),
				new Range(10, 20), new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void addRangeBetweenRanges() {
		RangeList list = create();
		
		list.add(new Range(21, 29));
		
		assertArrayEquals(new Object[] {
				new Range(10, 20),
				new Range(21, 29),
				new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void addRangeBeforeBegin() {
		RangeList list = create();
		
		list.add(new Range(5, 10));
		
		assertArrayEquals(new Object[] {
				new Range(5, 20),
				new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void addRangeInRange1() {
		RangeList list = create();
		
		list.add(new Range(10, 15));
		
		assertArrayEquals(new Object[] {
				new Range(10, 20), new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void addRangeInRange2() {
		RangeList list = create();
		
		list.add(new Range(11, 19));
		
		assertArrayEquals(new Object[] {
				new Range(10, 20), new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void addRangeInRange3() {
		RangeList list = create();
		
		list.add(new Range(15, 19));
		
		assertArrayEquals(new Object[] {
				new Range(10, 20), new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void addRangeSame() {
		RangeList list = create();
		
		list.add(new Range(10, 20));
		
		assertArrayEquals(new Object[] {
				new Range(10, 20), new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void addRangeAtEnd() {
		RangeList list = create();
		
		list.add(new Range(20, 25));
		
		assertArrayEquals(new Object[] {
				new Range(10, 25),
				new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void addRangeOverlappingByOne() {
		RangeList list = create();
		
		list.add(new Range(9, 21));
		
		assertArrayEquals(new Object[] {
				new Range(9, 21),
				new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void addRangeOverlapping() {
		RangeList list = create();
		
		list.add(new Range(5, 25));
		
		assertArrayEquals(new Object[] {
				new Range(5, 25),
				new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void addRangeMergeRanges() {
		RangeList list = create();
		
		list.add(new Range(20, 30));
		
		assertArrayEquals(new Object[] {
				new Range(10, 40),
				new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void addRangeMergeRangesOverlapping() {
		RangeList list = create();
		
		list.add(new Range(15, 35));
		
		assertArrayEquals(new Object[] {
				new Range(10, 40),
				new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void addRangeMergeRangesMulti() {
		RangeList list = create();
		
		list.add(new Range(15, 105));
		
		assertArrayEquals(new Object[] {
				new Range(10, 105)
		}, list.toArray() );
	}
	
	
	@Test
	public void removeValueBefore() {
		RangeList list = create();
		
		list.removeValue(8);
		
		assertArrayEquals(new Object[] {
				new Range(10, 20), new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void removeValueBeforeBegin() {
		RangeList list = create();
		
		list.removeValue(9);
		
		assertArrayEquals(new Object[] {
				new Range(10, 20), new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void removeValueInRange1() {
		RangeList list = create();
		
		list.removeValue(10);
		
		assertArrayEquals(new Object[] {
				new Range(11, 20), new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void removeValueInRange2() {
		RangeList list = create();
		
		list.removeValue(11);
		
		assertArrayEquals(new Object[] {
				new Range(10, 11),
				new Range(12, 20),
				new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void removeValueInRange3() {
		RangeList list = create();
		
		list.removeValue(19);
		
		assertArrayEquals(new Object[] {
				new Range(10, 19),
				new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void removeValueAtEnd() {
		RangeList list = create();
		
		list.removeValue(20);
		
		assertArrayEquals(new Object[] {
				new Range(10, 20), new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	
	@Test
	public void removeRangeBefore() {
		RangeList list = create();
		
		list.remove(new Range(5, 9));
		
		assertArrayEquals(new Object[] {
				new Range(10, 20), new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void removeRangeBetweenRanges1() {
		RangeList list = create();
		
		list.remove(new Range(21, 29));
		
		assertArrayEquals(new Object[] {
				new Range(10, 20), new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void removeRangeBetweenRanges2() {
		RangeList list = create();
		
		list.remove(new Range(20, 30));
		
		assertArrayEquals(new Object[] {
				new Range(10, 20), new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void removeRangeBeforeBegin() {
		RangeList list = create();
		
		list.remove(new Range(5, 10));
		
		assertArrayEquals(new Object[] {
				new Range(10, 20), new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void removeRangeInRange1() {
		RangeList list = create();
		
		list.remove(new Range(10, 15));
		
		assertArrayEquals(new Object[] {
				new Range(15, 20),
				new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void removeRangeInRange2() {
		RangeList list = create();
		
		list.remove(new Range(10, 19));
		
		assertArrayEquals(new Object[] {
				new Range(19, 20),
				new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void removeRangeInRange3() {
		RangeList list = create();
		
		list.remove(new Range(15, 19));
		
		assertArrayEquals(new Object[] {
				new Range(10, 15),
				new Range(19, 20),
				new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void removeRangeSame() {
		RangeList list = create();
		
		list.remove(new Range(10, 20));
		
		assertArrayEquals(new Object[] {
				new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void removeRangeAtEnd() {
		RangeList list = create();
		
		list.remove(new Range(20, 25));
		
		assertArrayEquals(new Object[] {
				new Range(10, 20), new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void removeRangeOverlappingByOne() {
		RangeList list = create();
		
		list.remove(new Range(9, 21));
		
		assertArrayEquals(new Object[] {
				new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void removeRangeOverlapping() {
		RangeList list = create();
		
		list.remove(new Range(5, 25));
		
		assertArrayEquals(new Object[] {
				new Range(30, 40), new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void removeMultiRanges1() {
		RangeList list = create();
		
		list.remove(new Range(11, 35));
		
		assertArrayEquals(new Object[] {
				new Range(10, 11),
				new Range(35, 40),
				new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void removeMultiRanges2() {
		RangeList list = create();
		
		list.remove(new Range(10, 35));
		
		assertArrayEquals(new Object[] {
				new Range(35, 40),
				new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void removeMultiRanges3() {
		RangeList list = create();
		
		list.remove(new Range(11, 40));
		
		assertArrayEquals(new Object[] {
				new Range(10, 11),
				new Range(50, 60), new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
	@Test
	public void removeMultiRanges4() {
		RangeList list = create();
		
		list.remove(new Range(15, 105));
		
		assertArrayEquals(new Object[] {
				new Range(10, 15)
		}, list.toArray() );
	}
	
	@Test
	public void removeMultiRanges5() {
		RangeList list = create();
		
		list.remove(new Range(0, 65));
		
		assertArrayEquals(new Object[] {
				new Range(70, 80), new Range(81, 100)
		}, list.toArray() );
	}
	
}
