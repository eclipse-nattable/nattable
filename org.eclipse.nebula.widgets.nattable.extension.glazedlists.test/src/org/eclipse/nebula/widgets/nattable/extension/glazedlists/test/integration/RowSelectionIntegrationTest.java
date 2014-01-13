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
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionProvider;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectColumnCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
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

	@Test
	public void onlySelectRowEventsFired() {
		nattable.addLayerListener(new ILayerListener() {
			@Override
			public void handleLayerEvent(ILayerEvent event) {
				if (event instanceof CellSelectionEvent) {
					fail("CellSelectionEvent fired for row selection");
				}
			}
		});
		
		nattable.doCommand(new SelectRowsCommand(selectionLayer, 0, 0, false, false));
		//the second call first clears the selection and then applies the new one
		//clearing by default also fires a CellSelectionEvent with negative values
		nattable.doCommand(new SelectRowsCommand(selectionLayer, 0, 3, false, false));
	}
	
	@Test
	public void setSingleSelectionViaProvider() {
		selectionProvider.setSelection(new StructuredSelection(
				new RowDataFixture[] { eventListFixture.get(1) }));
		
		assertEquals(1, selectionLayer.getFullySelectedRowPositions().length);
	}
	
	@Test
	public void setMultipleSelectionViaProvider() {
		selectionProvider.setSelection(new StructuredSelection(
				new RowDataFixture[] { eventListFixture.get(1), eventListFixture.get(3) }));
		
		assertEquals(2, selectionLayer.getFullySelectedRowPositions().length);

		selectionProvider.setSelection(new StructuredSelection(
				new RowDataFixture[] { eventListFixture.get(5), eventListFixture.get(7) }));
		
		assertEquals(2, selectionLayer.getFullySelectedRowPositions().length);
	}
	
	@Test
	public void setMultipleSelectionViaProviderWithAdd() {
		selectionProvider.setAddSelectionOnSet(true);
		
		selectionProvider.setSelection(new StructuredSelection(
				new RowDataFixture[] { eventListFixture.get(1), eventListFixture.get(3) }));
		
		assertEquals(2, selectionLayer.getFullySelectedRowPositions().length);

		selectionProvider.setSelection(new StructuredSelection(
				new RowDataFixture[] { eventListFixture.get(5), eventListFixture.get(7) }));
		
		assertEquals(4, selectionLayer.getFullySelectedRowPositions().length);
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testColumnSelectionProcessing() {
		final List selectedObjects = new ArrayList();
		
		//add a listener to see how many rows are selected
		selectionProvider.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				selectedObjects.addAll(selection.toList());
			}
		});
		
		//first execute column selection with default configuration to see that all rows get selected
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 1, 0, false, false));
		
		assertEquals(10, selectedObjects.size());
		
		//now clear set the flag for column selection processing to false and fire the event again
		selectedObjects.clear();
		selectionProvider.setProcessColumnSelection(false);
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 1, 0, false, false));
		assertEquals(0, selectedObjects.size());
		
		//now select a cell to verify that other selections are still processed
		selectionLayer.doCommand(new SelectRowsCommand(selectionLayer, 1, 1, false, false));
		assertEquals(1, selectedObjects.size());
	}
	
	private RowDataFixture getSelected() {
		return (RowDataFixture) ((StructuredSelection) selectionProvider.getSelection()).iterator().next();
	}
}
