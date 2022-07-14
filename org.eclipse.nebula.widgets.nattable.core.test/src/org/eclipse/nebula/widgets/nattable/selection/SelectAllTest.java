/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectAllCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectColumnCommand;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Test;

public class SelectAllTest {

    @Test
    public void shouldHaveAllCellsSelected() {
        SelectionLayer selectionLayer = new SelectionLayer(new DataLayerFixture());

        // Selection all cells in grid
        selectionLayer.selectAll();

        for (int columnPosition = 0; columnPosition < selectionLayer.getColumnCount(); columnPosition++) {
            for (int rowPosition = 0; rowPosition < selectionLayer.getRowCount(); rowPosition++) {
                ILayerCell cell = selectionLayer.getCellByPosition(columnPosition, rowPosition);
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

    @Test
    public void shouldNotUpdateSelectionAnchorOnSelectAll() {
        SelectionLayer selectionLayer = new SelectionLayer(new DataLayerFixture());

        selectionLayer.selectCell(3, 3, false, false);

        assertEquals(3, selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(3, selectionLayer.getSelectionAnchor().getRowPosition());

        selectionLayer.selectAll();

        assertEquals(3, selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(3, selectionLayer.getSelectionAnchor().getRowPosition());
    }

    @Test
    public void shouldNotUpdateSelectionAnchorOnSelectAllWithRowSelection() {
        String[] propertyNames = Arrays.copyOfRange(RowDataListFixture.getPropertyNames(), 0, 10);

        IRowDataProvider<RowDataFixture> bodyDataProvider = new ListDataProvider<RowDataFixture>(
                RowDataListFixture.getList(10),
                new ReflectiveColumnPropertyAccessor<RowDataFixture>(propertyNames));

        SelectionLayer selectionLayer = new SelectionLayer(new DataLayer(bodyDataProvider));
        selectionLayer.registerCommandHandler(new MoveRowSelectionCommandHandler(selectionLayer));

        selectionLayer.setSelectionModel(new RowSelectionModel<RowDataFixture>(
                selectionLayer, bodyDataProvider,
                new IRowIdAccessor<RowDataFixture>() {

                    @Override
                    public Serializable getRowId(RowDataFixture rowObject) {
                        return rowObject.getSecurity_id();
                    }

                }));

        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);
        // only show 6 columns
        viewportLayer.setClientAreaProvider(() -> new Rectangle(0, 0, 600, 200));

        selectionLayer.selectCell(3, 3, false, false);

        assertEquals(3, selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(3, selectionLayer.getSelectionAnchor().getRowPosition());
        assertTrue(selectionLayer.isRowPositionFullySelected(3));

        // viewport shows first column
        assertEquals(0, viewportLayer.getColumnIndexByPosition(0));

        selectionLayer.selectAll();

        // anchor not moved
        assertEquals(3, selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(3, selectionLayer.getSelectionAnchor().getRowPosition());

        // viewport not scrolled
        assertEquals(0, viewportLayer.getColumnIndexByPosition(0));

        // all rows selected
        for (int i = 0; i < selectionLayer.getRowCount(); i++) {
            assertTrue(selectionLayer.isRowPositionFullySelected(i));
        }
    }
}