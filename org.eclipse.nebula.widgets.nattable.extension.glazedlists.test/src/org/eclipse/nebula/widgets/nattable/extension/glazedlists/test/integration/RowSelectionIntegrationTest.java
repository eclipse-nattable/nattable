/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
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
import org.eclipse.nebula.widgets.nattable.command.DisposeResourcesCommand;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.fixture.LayerListenerFixture;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.fixture.NatTableFixture;
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
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;
import org.junit.After;
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
        this.eventListFixture = GlazedLists.eventList(RowDataListFixture.getList(10));

        GlazedListsGridLayer<RowDataFixture> gridLayer =
                new GlazedListsGridLayer<>(
                        this.eventListFixture,
                        RowDataListFixture.getPropertyNames(),
                        RowDataListFixture.getPropertyToLabelMap(),
                        configRegistry);
        this.nattable = new NatTableFixture(gridLayer, false);
        this.nattable.setConfigRegistry(configRegistry);

        this.selectionLayer = gridLayer.getBodyLayerStack().getSelectionLayer();
        this.bodyDataProvider = gridLayer.getBodyDataProvider();
        this.selectionProvider = new RowSelectionProvider<>(this.selectionLayer, this.bodyDataProvider);

        this.nattable.addConfiguration(new DefaultSortConfiguration());

        // Enable preserve selection on data update
        this.selectionLayer.setSelectionModel(
                new RowSelectionModel<>(
                        this.selectionLayer,
                        this.bodyDataProvider,
                        new IRowIdAccessor<RowDataFixture>() {

                            @Override
                            public Serializable getRowId(RowDataFixture rowObject) {
                                return rowObject.getSecurity_id();
                            }

                        }));

        // Enable test mode - events can be fired outside the Display thread
        gridLayer.getGlazedListsEventLayer().setTestMode(true);

        this.nattable.configure();
    }

    @After
    public void tearDown() {
        this.nattable.doCommand(new DisposeResourcesCommand());
    }

    @Test
    public void shouldPreserveRowSelectionOnDataUpdates() throws Exception {
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions().length);

        this.nattable.doCommand(new SelectRowsCommand(this.nattable, 1, 1, false, false));
        assertEquals(1, this.selectionLayer.getFullySelectedRowPositions().length);

        // Ford motor at top and selected
        assertEquals("B Ford Motor", this.nattable.getDataValueByPosition(2, 1).toString());
        assertEquals("B Ford Motor", getSelected().getSecurity_description());

        this.eventListFixture.add(0, RowDataFixture.getInstance("Tata motors", "A"));

        // Event layer will conflate list change events
        Thread.sleep(100);

        // Tata motors at top but Ford motors still selected
        assertEquals("Tata motors", this.nattable.getDataValueByPosition(2, 1).toString());
        assertEquals("B Ford Motor", getSelected().getSecurity_description());
    }

    @Test
    public void shouldPreserveRowSelectionOnSort() throws Exception {
        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions().length);

        // Unsorted order - Ford motor at top
        assertEquals("B Ford Motor", this.nattable.getDataValueByPosition(2, 1).toString());
        assertEquals("A Alphabet Co.", this.nattable.getDataValueByPosition(2, 2).toString());
        assertEquals("C General Electric Co", this.nattable.getDataValueByPosition(2, 3).toString());

        // Select 'Ford Motor'
        this.nattable.doCommand(new SelectRowsCommand(this.nattable, 1, 1, false, false));
        assertEquals("B Ford Motor", getSelected().getSecurity_description());

        // Sort
        this.nattable.doCommand(new SortColumnCommand(this.nattable, 2, false));

        // Sorted order - Alphabet co. at top
        assertEquals("A Alphabet Co.", this.nattable.getDataValueByPosition(2, 1).toString());
        assertEquals("B Ford Motor", this.nattable.getDataValueByPosition(2, 2).toString());
        assertEquals("C General Electric Co", this.nattable.getDataValueByPosition(2, 3).toString());

        // Ford motor still selected
        assertEquals("B Ford Motor", getSelected().getSecurity_description());
    }

    @Test
    public void onlyOneRowSelectedAtAnyTime() {
        this.selectionLayer.getSelectionModel().setMultipleSelectionAllowed(false);

        this.selectionLayer.clear();
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 1, 0, false, true));

        Collection<PositionCoordinate> cells = ArrayUtil.asCollection(this.selectionLayer.getSelectedCellPositions());
        Assert.assertEquals(this.selectionLayer.getColumnCount(), cells.size());
        Assert.assertEquals(1, this.selectionLayer.getSelectedRowCount());

        // select another cell with control mask
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 2, 1, false, true));

        cells = ArrayUtil.asCollection(this.selectionLayer.getSelectedCellPositions());
        Assert.assertEquals(this.selectionLayer.getColumnCount(), cells.size());
        Assert.assertEquals(1, this.selectionLayer.getSelectedRowCount());

        // select additional cells with shift mask
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 2, 10, true, false));

        cells = ArrayUtil.asCollection(this.selectionLayer.getSelectedCellPositions());
        Assert.assertEquals(this.selectionLayer.getColumnCount(), cells.size());
        Assert.assertEquals(1, this.selectionLayer.getSelectedRowCount());

        // select additional cells with shift mask
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 10, 0, true, false));

        cells = ArrayUtil.asCollection(this.selectionLayer.getSelectedCellPositions());
        Assert.assertEquals(this.selectionLayer.getColumnCount(), cells.size());
        Assert.assertEquals(1, this.selectionLayer.getSelectedRowCount());
    }

    @Test
    public void onlySelectRowEventsFired() {
        this.nattable.addLayerListener(new ILayerListener() {
            @Override
            public void handleLayerEvent(ILayerEvent event) {
                if (event instanceof CellSelectionEvent) {
                    fail("CellSelectionEvent fired for row selection");
                }
            }
        });

        this.nattable.doCommand(new SelectRowsCommand(this.selectionLayer, 0, 0, false, false));
        // the second call first clears the selection and then applies the new
        // one clearing by default also fires a CellSelectionEvent with negative
        // values
        this.nattable.doCommand(new SelectRowsCommand(this.selectionLayer, 0, 3, false, false));
    }

    @Test
    public void setSingleSelectionViaProvider() {
        this.selectionProvider.setSelection(
                new StructuredSelection(
                        new RowDataFixture[] { this.eventListFixture.get(1) }));

        assertEquals(1, this.selectionLayer.getFullySelectedRowPositions().length);
    }

    @Test
    public void setMultipleSelectionViaProvider() {
        this.selectionProvider.setSelection(
                new StructuredSelection(
                        new RowDataFixture[] { this.eventListFixture.get(1), this.eventListFixture.get(3) }));

        assertEquals(2, this.selectionLayer.getFullySelectedRowPositions().length);

        this.selectionProvider.setSelection(
                new StructuredSelection(
                        new RowDataFixture[] { this.eventListFixture.get(5), this.eventListFixture.get(7) }));

        assertEquals(2, this.selectionLayer.getFullySelectedRowPositions().length);
    }

    @Test
    public void setMultipleSelectionViaProviderWithAdd() {
        this.selectionProvider.setAddSelectionOnSet(true);

        this.selectionProvider.setSelection(
                new StructuredSelection(
                        new RowDataFixture[] { this.eventListFixture.get(1), this.eventListFixture.get(3) }));

        assertEquals(2, this.selectionLayer.getFullySelectedRowPositions().length);

        this.selectionProvider.setSelection(
                new StructuredSelection(
                        new RowDataFixture[] { this.eventListFixture.get(5), this.eventListFixture.get(7) }));

        assertEquals(4, this.selectionLayer.getFullySelectedRowPositions().length);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testColumnSelectionProcessing() {
        final List selectedObjects = new ArrayList();

        // add a listener to see how many rows are selected
        this.selectionProvider.addSelectionChangedListener(new ISelectionChangedListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                selectedObjects.addAll(selection.toList());
            }
        });

        // first execute column selection with default configuration to see that
        // all rows get selected
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 1, 0, false, false));

        assertEquals(10, selectedObjects.size());

        // now clear set the flag for column selection processing to false and
        // fire the event again
        selectedObjects.clear();
        this.selectionProvider.setProcessColumnSelection(false);
        this.selectionLayer.doCommand(new SelectColumnCommand(this.selectionLayer, 1, 0, false, false));
        assertEquals(0, selectedObjects.size());

        // now select a cell to verify that other selections are still processed
        this.selectionLayer.doCommand(new SelectRowsCommand(this.selectionLayer, 1, 1, false, false));
        assertEquals(1, selectedObjects.size());
    }

    @Test
    public void shouldClearSelectionOnSetEmpty() {
        LayerListenerFixture listener = new LayerListenerFixture();
        this.nattable.addLayerListener(listener);

        this.selectionProvider.setSelection(
                new StructuredSelection(
                        new RowDataFixture[] { this.eventListFixture.get(1), this.eventListFixture.get(3) }));

        assertEquals(2, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(1, listener.getEventsCount());

        this.selectionProvider.setSelection(StructuredSelection.EMPTY);

        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(2, listener.getEventsCount());
    }

    @Test
    public void shouldClearSelectionOnSetEmptyOnAdd() {
        this.selectionProvider.setAddSelectionOnSet(true);

        LayerListenerFixture listener = new LayerListenerFixture();
        this.nattable.addLayerListener(listener);

        this.selectionProvider.setSelection(
                new StructuredSelection(
                        new RowDataFixture[] { this.eventListFixture.get(1), this.eventListFixture.get(3) }));

        assertEquals(2, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(1, listener.getEventsCount());

        this.selectionProvider.setSelection(StructuredSelection.EMPTY);

        assertEquals(0, this.selectionLayer.getFullySelectedRowPositions().length);
        assertEquals(2, listener.getEventsCount());
    }

    @Test
    public void shouldReturnEmptySelectionOnNoSelection() {
        this.selectionLayer.clear();
        assertEquals(StructuredSelection.EMPTY, this.selectionProvider.getSelection());
    }

    @Test
    public void shouldIgnoreNullSelectionListener() {
        // this test succeeds if no NPE occurs
        this.selectionProvider.addSelectionChangedListener(null);
        this.selectionLayer.doCommand(new SelectCellCommand(this.selectionLayer, 1, 0, false, true));
    }

    private RowDataFixture getSelected() {
        return (RowDataFixture) ((StructuredSelection) this.selectionProvider.getSelection()).iterator().next();
    }
}
