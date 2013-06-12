/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow.command;


import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HideColumnCommandTest {

	private ColumnHideShowLayer columnHideShowLayer;
	
	@Before
	public void setup() {
		columnHideShowLayer = new ColumnHideShowLayer(new DataLayerFixture());
	}
	
	@Test
	public void testHideColumnCommand() {
		int columnPosition = 2;
		ILayerCommand hideColumnCommand = new MultiColumnHideCommand(columnHideShowLayer, columnPosition);
		
		Assert.assertEquals(5, columnHideShowLayer.getColumnCount());
		
		columnHideShowLayer.doCommand(hideColumnCommand);
		
		Assert.assertEquals(4, columnHideShowLayer.getColumnCount());
		
		Assert.assertEquals(0, columnHideShowLayer.getColumnIndexByPosition(0));
		Assert.assertEquals(1, columnHideShowLayer.getColumnIndexByPosition(1));
		Assert.assertEquals(3, columnHideShowLayer.getColumnIndexByPosition(2));
		Assert.assertEquals(4, columnHideShowLayer.getColumnIndexByPosition(3));
	}
	
}
