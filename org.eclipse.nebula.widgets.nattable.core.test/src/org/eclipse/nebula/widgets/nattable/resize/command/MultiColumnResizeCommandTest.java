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
package org.eclipse.nebula.widgets.nattable.resize.command;


import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.resize.command.MultiColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.ColumnReorderLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Assert;
import org.junit.Test;

public class MultiColumnResizeCommandTest {

	@Test
	public void getColumnWidth() throws Exception {
		MultiColumnResizeCommand resizeCommand = new MultiColumnResizeCommand(new DataLayerFixture(),
				new int[] { 5, 9 }, new int[] { 12, 20 });

		Assert.assertEquals(12, resizeCommand.getColumnWidth(5));
		Assert.assertEquals(20, resizeCommand.getColumnWidth(9));

		Assert.assertEquals(-1, resizeCommand.getColumnWidth(10)); //Error case
	}

	@Test
	public void getCommonColumnWidth() throws Exception {
		MultiColumnResizeCommand resizeCommand = new MultiColumnResizeCommand(new DataLayerFixture(),
				new int[] { 1, 2 }, 100);

		Assert.assertEquals(100, resizeCommand.getCommonColumnWidth());
		Assert.assertEquals(100, resizeCommand.getColumnWidth(1));
	}

	@Test
	public void getColumnWidthWhenTheColumnPositionsHaveBeenConverted() throws Exception {
		DataLayerFixture dataLayer = new DataLayerFixture();
		// Indexes re-ordered: 4 1 0 2 3
		ColumnReorderLayer reorderLayerFixture = new ColumnReorderLayerFixture(dataLayer);

		MultiColumnResizeCommand resizeCommand = new MultiColumnResizeCommand(reorderLayerFixture, new int[] { 1, 2 }, new int[] { 100, 150 });
		reorderLayerFixture.doCommand(resizeCommand);

		// As the Commands goes down the stack - positions might get converted
		// to entirely different values.
		Assert.assertEquals(-1, resizeCommand.getCommonColumnWidth());
		Assert.assertEquals(-1, resizeCommand.getColumnWidth(5));
		Assert.assertEquals(-1, resizeCommand.getColumnWidth(12));

		Assert.assertEquals(100, resizeCommand.getColumnWidth(1));
		Assert.assertEquals(150, resizeCommand.getColumnWidth(0));
	}
}
