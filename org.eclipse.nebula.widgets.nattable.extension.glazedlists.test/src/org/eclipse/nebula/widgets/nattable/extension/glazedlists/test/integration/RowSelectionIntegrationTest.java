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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.test.integration;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Collection;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionProvider;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.sort.command.SortColumnCommand;
import org.eclipse.nebula.widgets.nattable.sort.config.DefaultSortConfiguration;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

public class RowSelectionIntegrationTest {

	private NatTable nattable;
	private EventList<RowDataFixture> eventListFixture;
	private ListDataProvider<RowDataFixture> bodyDataProvider;
	private SelectionLayer selectionLayer;
	private RowSelectionProvider<RowDataFixture> selectionProvider;

	@Before
	public void setup() {
		IConfigRegistry configRegistry = new ConfigRegistry();

		// 10 rows in fixture
		eventListFixture = GlazedLists.eventList(RowDataListFixture.getList(10));

		GlazedListsGridLayer<RowDataFixture> gridLayer = new GlazedListsGridLayer<RowDataFixture>(
																eventListFixture,
																RowDataListFixture.getPropertyNames(),
																RowDataListFixture.getPropertyToLabelMap(),
																configRegistry);
		nattable = new NatTableFixture(gridLayer, false);
		nattable.setConfigRegistry(configRegistry);

		selectionLayer = gridLayer.getBodyLayerStack().getSelectionLayer();
		bodyDataProvider = gridLayer.getBodyDataProvider();
		selectionProvider = new RowSelectionProvider<RowDataFixture>(selectionLayer, bodyDataProvider);

		nattable.addConfiguration(new DefaultSortConfiguration());

		// Enable preserve selection on data update
		selectionLayer.setSelectionModel(new RowSelectionModel<RowDataFixture>(selectionLayer, bodyDataProvider, new IRowIdAccessor<RowDataFixture>() {

			@Override
			public Serializable getRowId(RowDataFixture rowObject) {
				return rowObject.getSecurity_id();
			}
			
		}));

		// Enable test mode - events can be fired outside the Display thread
		gridLayer.getGlazedListsEventLayer().setTestMode(true);

		nattable.configure();
	}

	@Test
	public void shouldPreserveRowSelectionOnDataUpdates() throws Exception {
		assertEquals(0, selectionLayer.getFullySelectedRowPositions().length);

		nattable.doCommand(new SelectRowsCommand(nattable, 1, 1, false, false));
		assertEquals(1, selectionLayer.getFullySelectedRowPositions().length);

		// Ford motor at top and selected
		assertEquals("B Ford Motor", nattable.getDataValueByPosition(2, 1).toString());
		assertEquals("B Ford Motor", getSelected().getSecurity_description());

		eventListFixture.add(0, RowDataFixture.getInstance("Tata motors", "A"));

		// Event layer will conflate list change events
		Thread.sleep(100);

		// Tata motors at top but Ford motors still selected
		assertEquals("Tata motors", nattable.getDataValueByPosition(2, 1).toString());
		assertEquals("B Ford Motor", getSelected().getSecurity_description());
	}

	@Test
	public void shouldPreserveRowSelectionOnSort() throws Exception {
		assertEquals(0, selectionLayer.getFullySelectedRowPositions().length);

		// Unsorted order - Ford motor at top
		assertEquals("B Ford Motor", nattable.getDataValueByPosition(2, 1).toString());
		assertEquals("A Alphabet Co.", nattable.getDataValueByPosition(2, 2).toString());
		assertEquals("C General Electric Co", nattable.getDataValueByPosition(2, 3).toString());

		// Select 'Ford Motor'
		nattable.doCommand(new SelectRowsCommand(nattable, 1, 1, false, false));
		assertEquals("B Ford Motor", getSelected().getSecurity_description());

		// Sort
		nattable.doCommand(new SortColumnCommand(nattable, 2, false));

		// Sorted order - Alphabet co. at top
		assertEquals("A Alphabet Co.", nattable.getDataValueByPosition(2, 1).toString());
		assertEquals("B Ford Motor", nattable.getDataValueByPosition(2, 2).toString());
		assertEquals("C General Electric Co", nattable.getDataValueByPosition(2, 3).toString());

		// Ford motor still selected
		assertEquals("B Ford Motor", getSelected().getSecurity_description());
	}

	
	@Test
	public void onlyOneRowSelectedAtAnyTime() {
		selectionLayer.getSelectionModel().setMultipleSelectionAllowed(false);

		selectionLayer.clear();
		selectionLayer.doCommand(new SelectCellCommand(selectionLayer, 1, 0, false, true));

		Collection<PositionCoordinate> cells = ArrayUtil.asCollection(selectionLayer.getSelectedCellPositions());
		Assert.assertEquals(selectionLayer.getColumnCount(), cells.size());
		Assert.assertEquals(1, selectionLayer.getSelectedRowCount());

		//select another cell with control mask
		selectionLayer.doCommand(new SelectCellCommand(selectionLayer, 2, 1, false, true));

		cells = ArrayUtil.asCollection(selectionLayer.getSelectedCellPositions());
		Assert.assertEquals(selectionLayer.getColumnCount(), cells.size());
		Assert.assertEquals(1, selectionLayer.getSelectedRowCount());

		//select additional cells with shift mask
		selectionLayer.doCommand(new SelectCellCommand(selectionLayer, 2, 10, true, false));

		cells = ArrayUtil.asCollection(selectionLayer.getSelectedCellPositions());
		Assert.assertEquals(selectionLayer.getColumnCount(), cells.size());
		Assert.assertEquals(1, selectionLayer.getSelectedRowCount());

		//select additional cells with shift mask
		selectionLayer.doCommand(new SelectCellCommand(selectionLayer, 10, 0, true, false));

		cells = ArrayUtil.asCollection(selectionLayer.getSelectedCellPositions());
		Assert.assertEquals(selectionLayer.getColumnCount(), cells.size());
		Assert.assertEquals(1, selectionLayer.getSelectedRowCount());
	}

	private RowDataFixture getSelected() {
		return (RowDataFixture) ((StructuredSelection) selectionProvider.getSelection()).iterator().next();
	}
}
