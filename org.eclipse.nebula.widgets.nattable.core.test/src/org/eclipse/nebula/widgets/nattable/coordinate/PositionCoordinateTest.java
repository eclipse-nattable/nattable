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


import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PositionCoordinateTest {
	
	private PositionCoordinate p1;
	private PositionCoordinate p2;

	@Before
	public void setup() {
		ILayer layer = new DataLayerFixture();
		p1 = new PositionCoordinate(layer, 1, 2);
		p2 = new PositionCoordinate(layer, 1, 2);
	}
	
	@Test
	public void testIdentity() {
		Assert.assertEquals(p1, p1);
	}

	@Test
	public void testEquals() {
		Assert.assertEquals(p1, p2);
	}
	
	@Test
	public void testIdentityHashCode() {
		Assert.assertEquals(p1.hashCode(), p1.hashCode());
	}
	
	@Test
	public void testHashCode() {
		Assert.assertEquals(p1.hashCode(), p2.hashCode());
	}
	
}
