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
package org.eclipse.nebula.widgets.nattable.freeze;


import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.test.LayerAssert;
import org.eclipse.nebula.widgets.nattable.test.fixture.TestLayer;
import org.junit.Before;
import org.junit.Test;

public class FreezeLayerTest2 {

	private FreezeLayer freezeLayer;

	@Before
	public void setup() {
		TestLayer dataLayer =
			new TestLayer(
					4, 4,
					"0:0;100 | 1:1;100 | 2:2;100 | 3:3;100",
					"0:0;40  | 1:1;40  | 2:2;40  | 3:3;40",
					"A0 | B0 | C0 | D0 \n" +
					"A1 | B1 | C1 | D1 \n" +
					"A2 | B2 | C2 | D2 \n" +
					"A3 | B3 | C3 | D3 \n"
			);
		
		freezeLayer = new FreezeLayer(dataLayer);
	}
	
	@Test
	public void testFreezeAll() {
		freezeLayer.setTopLeftPosition(0, 0);
		freezeLayer.setBottomRightPosition(3, 3);
		
		TestLayer expectedLayer =
			new TestLayer(
					4, 4,
					"0:0;100 | 1:1;100 | 2:2;100 | 3:3;100",
					"0:0;40  | 1:1;40  | 2:2;40  | 3:3;40",
					"A0 | B0 | C0 | D0 \n" +
					"A1 | B1 | C1 | D1 \n" +
					"A2 | B2 | C2 | D2 \n" +
					"A3 | B3 | C3 | D3 \n"
			);
		
		LayerAssert.assertLayerEquals(expectedLayer, freezeLayer);
	}

	@Test
	public void testFreezeStart() {
		freezeLayer.setTopLeftPosition(0, 0);
		freezeLayer.setBottomRightPosition(1, 1);
		
		TestLayer expectedLayer =
			new TestLayer(
					2, 2,
					"0:0;100 | 1:1;100",
					"0:0;40  | 1:1;40",
					"A0 | B0 \n" +
					"A1 | B1 \n"
			);
		
		LayerAssert.assertLayerEquals(expectedLayer, freezeLayer);
	}

	@Test
	public void testFreezeMiddle() {
		freezeLayer.setTopLeftPosition(1, 1);
		freezeLayer.setBottomRightPosition(2, 2);
		
		TestLayer expectedLayer =
			new TestLayer(
					2, 2,
					"1:1;100 | 2:2;100",
					"1:1;40  | 2:2;40",
					"B1 | C1 \n" +
					"B2 | C2 \n"
			);
		
		LayerAssert.assertLayerEquals(expectedLayer, freezeLayer);
	}

	@Test
	public void testFreezeEnd() {
		freezeLayer.setTopLeftPosition(1, 1);
		freezeLayer.setBottomRightPosition(3, 3);
		
		TestLayer expectedLayer =
			new TestLayer(
					3, 3,
					"1:1;100 | 2:2;100 | 3:3;100",
					"1:1;40  | 2:2;40  | 3:3;40",
					"B1 | C1 | D1 \n" +
					"B2 | C2 | D2 \n" +
					"B3 | C3 | D3 \n"
			);
		
		LayerAssert.assertLayerEquals(expectedLayer, freezeLayer);
	}
	
}
