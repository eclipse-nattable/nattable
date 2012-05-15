/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.coordinate;

import org.eclipse.nebula.widgets.nattable.coordinate.IndexCoordinate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IndexCoordinateTest {
	
	private IndexCoordinate i1;
	private IndexCoordinate i2;

	@Before
	public void setup() {
		i1 = new IndexCoordinate(1, 2);
		i2 = new IndexCoordinate(1, 2);
	}
	
	@Test
	public void testIdentity() {
		Assert.assertEquals(i1, i1);
	}

	@Test
	public void testEquals() {
		Assert.assertEquals(i1, i2);
	}
	
	@Test
	public void testIdentityHashCode() {
		Assert.assertEquals(i1.hashCode(), i1.hashCode());
	}
	
	@Test
	public void testHashCode() {
		Assert.assertEquals(i1.hashCode(), i2.hashCode());
	}
	
}
