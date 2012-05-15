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
package org.eclipse.nebula.widgets.nattable.selection;


import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.test.LayerAssert;
import org.eclipse.nebula.widgets.nattable.test.fixture.TestLayer;
import org.junit.Before;
import org.junit.Test;

public class SelectionLayerTest {

	private TestLayer testLayer;
	private SelectionLayer selectionLayer;
	
	@Before
	public void setup() {
		String columnInfo = "0:0;100 | 1:1;100 | 2:2;100 | 3:3;100";
		String rowInfo =    "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40";
		
		String cellInfo = 
			"A0 | <  | C0 | D0 \n" +
			"^  | <  | C1 | D1 \n" +
			"A2 | B2 | C2 | D2 \n" +
			"A3 | B3 | C3 | D3 \n";
		
		testLayer = new TestLayer(4, 4, columnInfo, rowInfo, cellInfo);
		
		selectionLayer = new SelectionLayer(testLayer);
	}
	
	@Test
	public void testIdentityLayerTransform() {
		LayerAssert.assertLayerEquals(testLayer, selectionLayer);
	}
	
}
