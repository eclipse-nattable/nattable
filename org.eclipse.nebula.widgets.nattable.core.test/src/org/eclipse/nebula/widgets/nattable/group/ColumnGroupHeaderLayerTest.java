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
package org.eclipse.nebula.widgets.nattable.group;

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

public class ColumnGroupHeaderLayerTest {

	public static final String TEST_GROUP_NAME_3 = "testGroupName3";
	public static final String TEST_GROUP_NAME_2 = "testGroupName2";
	public static final String TEST_GROUP_NAME_1 = "testGroupName";
	public static final String NO_GROUP_NAME = "";
	public ColumnGroupHeaderLayer columnGroupLayer;
	private ColumnGroupModel model;
	private DefaultGridLayer gridLayer;

	@Before
	public void setup() {
		gridLayer = new GridLayerFixture();		
		model = new ColumnGroupModel();
		//10 columns in header
		columnGroupLayer = new ColumnGroupHeaderLayer(gridLayer.getColumnHeaderLayer(), gridLayer.getBodyLayer().getSelectionLayer(), model);
		columnGroupLayer.addColumnsIndexesToGroup(TEST_GROUP_NAME_1, 0, 1);
		columnGroupLayer.addColumnsIndexesToGroup(TEST_GROUP_NAME_2, 5, 6);
		columnGroupLayer.addColumnsIndexesToGroup(TEST_GROUP_NAME_3, 8, 9);
		gridLayer.setClientAreaProvider(new IClientAreaProvider() {

			public Rectangle getClientArea() {
				return new Rectangle(0, 0, 1050, 250);
			}
			
		});
		gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));
	}

	/**
	 * @seet {@link DataLayerFixture}
	 */
	@Test
	public void getCellForACellInAColumnGroup() throws Exception {
		ILayerCell cell = columnGroupLayer.getCellByPosition(0, 0);
		
		assertEquals(TEST_GROUP_NAME_1, cell.getDataValue());
		assertEquals(0, cell.getBounds().x);
		assertEquals(0, cell.getBounds().y);
		assertEquals(200, cell.getBounds().width);
		assertEquals(20, cell.getBounds().height);

		cell = columnGroupLayer.getCellByPosition(1, 0);
		assertEquals(TEST_GROUP_NAME_1, cell.getDataValue());
		assertEquals(0, cell.getBounds().x);
		assertEquals(0, cell.getBounds().y);
		assertEquals(200, cell.getBounds().width);
		assertEquals(20, cell.getBounds().height);
	}

	@Test
	public void aCollapsedColumnGroupShouldNotBeSpanned() throws Exception {
		assertEquals(2, columnGroupLayer.getCellByPosition(0, 0).getColumnSpan());

		columnGroupLayer.setGroupAsCollapsed(0);
		
		assertEquals(1, columnGroupLayer.getCellByPosition(0, 0).getColumnSpan());

	}
	
	@Test
	public void getCellForACellNotInAColumnGroup() throws Exception {
		ILayerCell cell = columnGroupLayer.getCellByPosition(5, 0);
		
		assertEquals(500, cell.getBounds().x);
		assertEquals(0, cell.getBounds().y);
		assertEquals(200, cell.getBounds().width);
		assertEquals(20, cell.getBounds().height);
	}
	
	@Test
	public void getDataValueByPosition() throws Exception {
		String dataValueGroup1 = (String) columnGroupLayer.getDataValueByPosition(1, 0);
		String dataValue = (String) columnGroupLayer.getDataValueByPosition(2, 0);

		assertEquals(TEST_GROUP_NAME_1, dataValueGroup1);
		assertEquals("[2,0]", dataValue); //Regular header
	}
	
	@Test
	public void getColumnWidthByPosition() throws Exception {
		// Col 0,1 are in group 150 + 100
		assertEquals(100, columnGroupLayer.getColumnWidthByPosition(0));
		assertEquals(100, columnGroupLayer.getColumnWidthByPosition(1));
	}
	
	@Test
	public void getColumnWidthWhenNoColumnGroupsPresent() throws Exception {
		columnGroupLayer.clearAllGroups();
		//Width of the vsible columns - see fixture
		
		assertEquals(1000, columnGroupLayer.getWidth());
	}
	
	@Test
	public void getColumnWidthByPositionForAColumnOutsideTheViewport() throws Exception {
		//Returns default column width from DataLayer
		assertEquals(100, columnGroupLayer.getColumnWidthByPosition(100));
	}
	
	@Test
	public void getCellSpan() throws Exception {
		//Index in group: 5, 6, 7
		model.addColumnsIndexesToGroup(TEST_GROUP_NAME_2, 7);
		assertEquals(3, columnGroupLayer.getColumnSpan(5));
		
		model.getColumnGroupByIndex(5).setCollapsed(true);
		assertEquals(1, columnGroupLayer.getColumnSpan(5));
	}
	
	@Test
	public void getCellSpanWhenColumnsInGroupAreHidden() throws Exception {
		//Index in group: 5, 6, 7
		model.addColumnsIndexesToGroup(TEST_GROUP_NAME_2, 7);
		assertEquals(3, columnGroupLayer.getColumnSpan(5));

		//Hide position 6
		ColumnHideCommand hideColumnCommand = new ColumnHideCommand(gridLayer.getBodyLayer(), 6);
		gridLayer.getBodyLayer().getColumnHideShowLayer().doCommand(hideColumnCommand);

		assertEquals(2, columnGroupLayer.getColumnSpan(5));
		
		//Hide position 5
		hideColumnCommand = new ColumnHideCommand(gridLayer.getBodyLayer(), 6);
		gridLayer.getBodyLayer().getColumnHideShowLayer().doCommand(hideColumnCommand);

		assertEquals(1, columnGroupLayer.getColumnSpan(5));
	}
}
