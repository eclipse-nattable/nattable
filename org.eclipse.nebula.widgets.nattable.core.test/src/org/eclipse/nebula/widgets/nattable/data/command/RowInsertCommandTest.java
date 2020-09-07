/*******************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.data.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowInsertEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

public class RowInsertCommandTest {

    private List<Person> dataModel;
    private ListDataProvider<Person> dataProvider;
    private DataLayer dataLayer;
    private ViewportLayer viewportLayer;
    private LayerListenerFixture listener;

    @Before
    public void setup() {
        this.dataModel = PersonService.getFixedPersons();
        this.dataProvider = new ListDataProvider<>(
                this.dataModel,
                new ReflectiveColumnPropertyAccessor<>(new String[] {
                        "firstName",
                        "lastName",
                        "gender",
                        "married",
                        "birthday" }));

        this.dataLayer = new DataLayer(this.dataProvider);
        this.dataLayer.registerCommandHandler(new RowInsertCommandHandler<>(this.dataModel));

        this.listener = new LayerListenerFixture();
        this.dataLayer.addLayerListener(this.listener);

        this.viewportLayer = new ViewportLayer(this.dataLayer);
        this.viewportLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                // show 10 entries (20px height x 10 = 200)
                return new Rectangle(0, 0, 500, 200);
            }

        });
    }

    @Test
    public void shouldAppendSingleRow() {
        assertEquals(18, this.dataModel.size());

        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataLayer.doCommand(new RowInsertCommand<>(ralph));

        // test that the object is inserted
        assertEquals(19, this.dataModel.size());
        assertEquals(18, this.dataModel.indexOf(ralph));

        // test the received event
        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowInsertEvent.class));
        RowInsertEvent event = (RowInsertEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(new Range(18, 19), event.getRowPositionRanges().iterator().next());
    }

    @Test
    public void shouldInsertSingleRow() {
        assertEquals(18, this.dataModel.size());

        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataLayer.doCommand(new RowInsertCommand<>(this.dataLayer, 10, ralph));

        // test that the objects are inserted
        assertEquals(19, this.dataModel.size());
        assertEquals(10, this.dataModel.indexOf(ralph));

        // test the received event
        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowInsertEvent.class));
        RowInsertEvent event = (RowInsertEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(new Range(10, 11), event.getRowPositionRanges().iterator().next());
    }

    @Test
    public void shouldAppendMultipleRows() {
        assertEquals(18, this.dataModel.size());

        Person clancy = new Person(40, "Clancy", "Wiggum", Gender.MALE, true, new Date());
        Person sarah = new Person(41, "Sarah", "Wiggum", Gender.FEMALE, true, new Date());
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());

        this.dataLayer.doCommand(new RowInsertCommand<>(this.dataLayer, -1, clancy, sarah, ralph));

        // test that the objects are inserted
        assertEquals(21, this.dataModel.size());
        assertEquals(18, this.dataModel.indexOf(clancy));
        assertEquals(19, this.dataModel.indexOf(sarah));
        assertEquals(20, this.dataModel.indexOf(ralph));

        // test the received event
        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowInsertEvent.class));
        RowInsertEvent event = (RowInsertEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(new Range(18, 21), event.getRowPositionRanges().iterator().next());
    }

    @Test
    public void shouldInsertMultipleRows() {
        assertEquals(18, this.dataModel.size());

        Person clancy = new Person(40, "Clancy", "Wiggum", Gender.MALE, true, new Date());
        Person sarah = new Person(41, "Sarah", "Wiggum", Gender.FEMALE, true, new Date());
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());

        this.dataLayer.doCommand(new RowInsertCommand<>(this.dataLayer, 10, clancy, sarah, ralph));

        // test that the objects are inserted
        assertEquals(21, this.dataModel.size());
        assertEquals(10, this.dataModel.indexOf(clancy));
        assertEquals(11, this.dataModel.indexOf(sarah));
        assertEquals(12, this.dataModel.indexOf(ralph));

        // test the received event
        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowInsertEvent.class));
        RowInsertEvent event = (RowInsertEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(new Range(10, 13), event.getRowPositionRanges().iterator().next());
    }

    @Test
    public void shouldInsertInScrolledState() {
        // scroll to bottom
        this.viewportLayer.moveRowPositionIntoViewport(17);

        // ensure that the last visible row is the last row in the backing data
        assertEquals(17, this.viewportLayer.getRowIndexByPosition(9));
        assertEquals(8, this.viewportLayer.getRowIndexByPosition(0));

        // insert a row at visible position 2 which should be 10 in the backing
        // data
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.viewportLayer.doCommand(new RowInsertCommand<>(this.viewportLayer, 2, ralph));

        // test that the objects are inserted
        assertEquals(19, this.dataModel.size());
        assertEquals(10, this.dataModel.indexOf(ralph));

        // test the received event
        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowInsertEvent.class));
        RowInsertEvent event = (RowInsertEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(new Range(10, 11), event.getRowPositionRanges().iterator().next());
    }

    @Test
    public void shouldInsertInScrolledStateAtBottom() {
        // scroll to bottom
        this.viewportLayer.moveRowPositionIntoViewport(17);

        // insert a row at last visible position
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.viewportLayer.doCommand(new RowInsertCommand<>(this.viewportLayer, 10, ralph));

        // test that the objects are inserted
        assertEquals(19, this.dataModel.size());
        assertEquals(18, this.dataModel.indexOf(ralph));

        // test the received event
        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowInsertEvent.class));
        RowInsertEvent event = (RowInsertEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(new Range(18, 19), event.getRowPositionRanges().iterator().next());
    }

    @Test
    public void shouldInsertInScrolledStateInTable() {
        IDataProvider colDataProvider = new DummyColumnHeaderDataProvider(this.dataProvider);
        ColumnHeaderLayer colHeader = new ColumnHeaderLayer(
                new DataLayer(colDataProvider), this.viewportLayer, (SelectionLayer) null);

        IDataProvider rowDataProvider = new DefaultRowHeaderDataProvider(this.dataProvider);
        RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(
                new DataLayer(rowDataProvider), this.viewportLayer, (SelectionLayer) null);

        CornerLayer cornerLayer = new CornerLayer(
                new DataLayer(new DefaultCornerDataProvider(colDataProvider, rowDataProvider)),
                rowHeaderLayer, colHeader);

        GridLayer grid = new GridLayer(this.viewportLayer, colHeader, rowHeaderLayer, cornerLayer);

        NatTable natTable = new NatTable(new Shell(), grid);

        // height 10 x 20 height + 20 column header
        natTable.setSize(GUIHelper.convertHorizontalPixelToDpi(500), GUIHelper.convertVerticalPixelToDpi(220));

        // scroll to bottom
        this.viewportLayer.moveRowPositionIntoViewport(17);

        // insert a row at last visible position
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        natTable.doCommand(new RowInsertCommand<>(natTable, 3, ralph));

        // test that the objects are inserted
        assertEquals(19, this.dataModel.size());
        assertEquals(10, this.dataModel.indexOf(ralph));

        // test the received event
        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowInsertEvent.class));
        RowInsertEvent event = (RowInsertEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(new Range(10, 11), event.getRowPositionRanges().iterator().next());
    }

    @Test
    public void shouldInsertInScrolledStateInTableAtBottom() {
        IDataProvider colDataProvider = new DummyColumnHeaderDataProvider(this.dataProvider);
        ColumnHeaderLayer colHeader = new ColumnHeaderLayer(
                new DataLayer(colDataProvider), this.viewportLayer, (SelectionLayer) null);

        IDataProvider rowDataProvider = new DefaultRowHeaderDataProvider(this.dataProvider);
        RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(
                new DataLayer(rowDataProvider), this.viewportLayer, (SelectionLayer) null);

        CornerLayer cornerLayer = new CornerLayer(
                new DataLayer(new DefaultCornerDataProvider(colDataProvider, rowDataProvider)),
                rowHeaderLayer, colHeader);

        GridLayer grid = new GridLayer(this.viewportLayer, colHeader, rowHeaderLayer, cornerLayer);

        NatTable natTable = new NatTable(new Shell(), grid);
        natTable.setSize(500, 200);

        // scroll to bottom
        this.viewportLayer.moveRowPositionIntoViewport(17);

        // insert a row at last visible position
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        natTable.doCommand(new RowInsertCommand<>(natTable, 10, ralph));

        // test that the objects are inserted
        assertEquals(19, this.dataModel.size());
        assertEquals(18, this.dataModel.indexOf(ralph));

        // test the received event
        assertEquals(1, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(RowInsertEvent.class));
        RowInsertEvent event = (RowInsertEvent) this.listener.getReceivedEvents().get(0);
        assertEquals(new Range(18, 19), event.getRowPositionRanges().iterator().next());
    }
}
