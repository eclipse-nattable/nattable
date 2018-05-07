/*******************************************************************************
 * Copyright (c) 2012, 2018 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectAllCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectColumnCommand;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SelectAllTest {

    private SelectionLayer selectionLayer;

    @Before
    public void setUp() {
        this.selectionLayer = new SelectionLayer(new DataLayerFixture());
        // Selection all cells in grid
        this.selectionLayer.selectAll();
    }

    @After
    public void cleanUp() {
        this.selectionLayer.clear();
    }

    @Test
    public void shouldHaveAllCellsSelected() {
        for (int columnPosition = 0; columnPosition < this.selectionLayer.getColumnCount(); columnPosition++) {
            for (int rowPosition = 0; rowPosition < this.selectionLayer.getRowCount(); rowPosition++) {
                ILayerCell cell = this.selectionLayer.getCellByPosition(columnPosition, rowPosition);
                assertEquals(DisplayMode.SELECT, cell.getDisplayMode());
            }
        }
    }

    @Test
    public void shouldSendEventOnSelectAllWhenHidingLastSelectedColumn() throws Exception {
        List<Person> listFixture = PersonService.getPersons(10);
        IRowDataProvider<Person> bodyDataProvider = new ListDataProvider<>(listFixture,
                new ReflectiveColumnPropertyAccessor<Person>(new String[] { "firstName", "lastName", "gender", "married", "birthday" }));

        GridLayerFixture gridLayer = new GridLayerFixture(bodyDataProvider);
        NatTable natTable = new NatTableFixture(gridLayer, false);

        SelectionLayer selectionLayer = gridLayer.getBodyLayer().getSelectionLayer();

        natTable.doCommand(new SelectColumnCommand(selectionLayer, 4, 0, false, false));
        assertEquals(1, selectionLayer.getFullySelectedColumnPositions().length);

        gridLayer.getBodyLayer().getColumnHideShowLayer().hideColumnPositions(4);

        LayerListenerFixture listener = new LayerListenerFixture();
        natTable.addLayerListener(listener);

        natTable.doCommand(new SelectAllCommand());

        assertEquals(4, selectionLayer.getFullySelectedColumnPositions().length);
        assertEquals(1, listener.getEventsCount());
        assertTrue(listener.getReceivedEvents().get(0) instanceof CellSelectionEvent);
    }

}
