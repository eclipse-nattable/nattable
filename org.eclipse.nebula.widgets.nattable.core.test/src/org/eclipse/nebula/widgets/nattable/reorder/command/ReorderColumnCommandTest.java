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
package org.eclipse.nebula.widgets.nattable.reorder.command;


import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReorderColumnCommandTest {

	private ColumnReorderLayer columnReorderLayer;
	
	@Before
	public void setup() {
		columnReorderLayer = new ColumnReorderLayer(new DataLayerFixture());
	}
	
	@Test
	public void testReorderColumnCommand() {
		int fromColumnPosition = 4;
		int toColumnPosition = 1;
		ILayerCommand reorderColumnCommand = new ColumnReorderCommand(columnReorderLayer, fromColumnPosition, toColumnPosition);

		columnReorderLayer.doCommand(reorderColumnCommand);
		
		Assert.assertEquals(0, columnReorderLayer.getColumnIndexByPosition(0));
		Assert.assertEquals(4, columnReorderLayer.getColumnIndexByPosition(1));
		Assert.assertEquals(1, columnReorderLayer.getColumnIndexByPosition(2));
		Assert.assertEquals(2, columnReorderLayer.getColumnIndexByPosition(3));
		Assert.assertEquals(3, columnReorderLayer.getColumnIndexByPosition(4));
	}
	
}
