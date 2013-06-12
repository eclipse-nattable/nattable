/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder.command;


import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReorderRowCommandTest {

	private RowReorderLayer rowReorderLayer;
	
	@Before
	public void setup() {
		rowReorderLayer = new RowReorderLayer(new DataLayerFixture());
	}
	
	@Test
	public void testReorderRowCommand() {
		int fromRowPosition = 4;
		int toRowPosition = 1;
		ILayerCommand reorderRowCommand = new RowReorderCommand(rowReorderLayer, fromRowPosition, toRowPosition);

		rowReorderLayer.doCommand(reorderRowCommand);
		
		Assert.assertEquals(0, rowReorderLayer.getRowIndexByPosition(0));
		Assert.assertEquals(4, rowReorderLayer.getRowIndexByPosition(1));
		Assert.assertEquals(1, rowReorderLayer.getRowIndexByPosition(2));
		Assert.assertEquals(2, rowReorderLayer.getRowIndexByPosition(3));
		Assert.assertEquals(3, rowReorderLayer.getRowIndexByPosition(4));
	}
	
}
