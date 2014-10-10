/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth <dirk.fauth@googlemail.com> and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.selection.preserve.PreserveSelectionModel;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.junit.Before;
import org.junit.Test;

public class SelectionModelStructuralChangeEventTest {

	private NatTable nattable;
	private List<RowDataFixture> listFixture;
	private IRowDataProvider<RowDataFixture> bodyDataProvider;
	private DataLayer bodyDataLayer;
	private SelectionLayer selectionLayer;
	
	@Before
	public void setup() {
		listFixture = RowDataListFixture.getList(10);
		bodyDataProvider = new ListDataProvider<RowDataFixture>(listFixture, 
				new ReflectiveColumnPropertyAccessor<RowDataFixture>(RowDataListFixture.getPropertyNames()));
		
		GridLayerFixture gridLayer = new GridLayerFixture(bodyDataProvider);		
		nattable = new NatTableFixture(gridLayer, false);

		bodyDataLayer = (DataLayer) gridLayer.getBodyDataLayer();
		selectionLayer = gridLayer.getBodyLayer().getSelectionLayer();
	}
	
	@Test
	public void shouldClearSelectionOnDataUpdates() throws Exception {
		//test SelectionModel updates
		assertEquals(0, selectionLayer.getFullySelectedRowPositions().length);

		nattable.doCommand(new SelectRowsCommand(nattable, 1, 1, false, false));
		assertEquals(1, selectionLayer.getFullySelectedRowPositions().length);

		// Ford motor at top and selected
		assertEquals("B Ford Motor", nattable.getDataValueByPosition(2, 1).toString());
		assertEquals("B Ford Motor", getSelected().getSecurity_description());

		listFixture.add(0, RowDataFixture.getInstance("Tata motors", "A"));

		//fire event to trigger structural refresh
		bodyDataLayer.fireLayerEvent(new StructuralRefreshEvent(bodyDataLayer));
		
		assertEquals(0, selectionLayer.getFullySelectedRowPositions().length);
	}
	
	@Test
	public void shouldPreserveRowSelectionOnDataUpdates() throws Exception {
		//test RowSelectionModel updates
		selectionLayer.setSelectionModel(new RowSelectionModel<RowDataFixture>(
				selectionLayer, bodyDataProvider, new IRowIdAccessor<RowDataFixture>() {

			@Override
			public Serializable getRowId(RowDataFixture rowObject) {
				return rowObject.getSecurity_id();
			}
			
		}));

		assertEquals(0, selectionLayer.getFullySelectedRowPositions().length);

		nattable.doCommand(new SelectRowsCommand(nattable, 1, 1, false, false));
		assertEquals(1, selectionLayer.getFullySelectedRowPositions().length);

		// Ford motor at top and selected
		assertEquals("B Ford Motor", nattable.getDataValueByPosition(2, 1).toString());
		assertEquals("B Ford Motor", getSelected().getSecurity_description());

		listFixture.add(0, RowDataFixture.getInstance("Tata motors", "A"));

		//fire event to trigger structural refresh
		bodyDataLayer.fireLayerEvent(new StructuralRefreshEvent(bodyDataLayer));
		
		// Tata motors at top but Ford motors still selected
		assertEquals("Tata motors", nattable.getDataValueByPosition(2, 1).toString());
		assertEquals("B Ford Motor", getSelected().getSecurity_description());
	}
	
	@Test
	public void shouldPreserveSelectionOnDataUpdates() throws Exception {
		//test PreserveSelectionModel updates
		selectionLayer.setSelectionModel(new PreserveSelectionModel<RowDataFixture>(
				selectionLayer, bodyDataProvider, new IRowIdAccessor<RowDataFixture>() {

			@Override
			public Serializable getRowId(RowDataFixture rowObject) {
				return rowObject.getSecurity_id();
			}
			
		}));

		assertEquals(0, selectionLayer.getFullySelectedRowPositions().length);

		nattable.doCommand(new SelectRowsCommand(nattable, 1, 1, false, false));
		assertEquals(1, selectionLayer.getFullySelectedRowPositions().length);

		// Ford motor at top and selected
		assertEquals("B Ford Motor", nattable.getDataValueByPosition(2, 1).toString());
		assertEquals("B Ford Motor", getSelected().getSecurity_description());

		listFixture.add(0, RowDataFixture.getInstance("Tata motors", "A"));

		//fire event to trigger structural refresh
		bodyDataLayer.fireLayerEvent(new StructuralRefreshEvent(bodyDataLayer));
		
		// Tata motors at top but Ford motors still selected
		assertEquals("Tata motors", nattable.getDataValueByPosition(2, 1).toString());
		assertEquals("B Ford Motor", getSelected().getSecurity_description());
	}
	
	private RowDataFixture getSelected() {
		Range selection = selectionLayer.getSelectedRowPositions().iterator().next();
		return listFixture.get(selection.start);
	}

}
