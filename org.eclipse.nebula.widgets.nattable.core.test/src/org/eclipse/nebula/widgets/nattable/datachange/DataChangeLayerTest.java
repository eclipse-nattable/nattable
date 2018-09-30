/*******************************************************************************
 * Copyright (c) 2017, 2018 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.datachange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.StructuralRefreshCommand;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.datachange.command.DiscardDataChangesCommand;
import org.eclipse.nebula.widgets.nattable.datachange.command.SaveDataChangesCommand;
import org.eclipse.nebula.widgets.nattable.datachange.event.DiscardDataChangesCompletedEvent;
import org.eclipse.nebula.widgets.nattable.datachange.event.SaveDataChangesCompletedEvent;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.edit.event.DataUpdateEvent;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnDeleteEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnInsertEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowDeleteEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowInsertEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Before;
import org.junit.Test;

public class DataChangeLayerTest {

    private List<Person> dataModel;

    private DataLayer dataLayer;
    private DataChangeLayer dataChangeLayer;
    private PersistenceUpdateDataChangeHandler handler;

    @Before
    public void setup() {
        this.dataModel = PersonService.getFixedPersons();
        this.dataLayer = new DataLayer(
                new ListDataProvider<>(
                        this.dataModel,
                        new ReflectiveColumnPropertyAccessor<>(new String[] {
                                "firstName",
                                "lastName",
                                "gender",
                                "married",
                                "birthday" })));
        this.dataChangeLayer = new DataChangeLayer(this.dataLayer, new PointKeyHandler(), false);
        for (DataChangeHandler h : this.dataChangeLayer.dataChangeHandler) {
            if (h instanceof PersistenceUpdateDataChangeHandler) {
                this.handler = (PersistenceUpdateDataChangeHandler) h;
                break;
            }
        }
        assertNotNull("PersistenceUpdateDataChangeHandler not found", this.handler);
    }

    @Test
    public void shouldUpdateDataInDataLayer() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Column 1 is not dirty", this.dataChangeLayer.isColumnDirty(1));
        assertTrue("Row 1 is not dirty", this.dataChangeLayer.isRowDirty(1));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 1));
    }

    @Test
    public void shouldClearWithoutReset() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));

        // only clear no restore, changed data is not reset
        this.dataChangeLayer.clearDataChanges();

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 1 is dirty", this.dataChangeLayer.isRowDirty(1));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 1));

        assertTrue("changed columns are not empty", this.handler.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.handler.changedRows.isEmpty());
        assertTrue("changes are not empty", this.handler.dataChanges.isEmpty());
        assertTrue("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
    }

    @Test
    public void shouldDiscardChanges() {
        LayerListenerFixture listener = new LayerListenerFixture();
        this.dataChangeLayer.addLayerListener(listener);

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 1 is dirty", this.dataChangeLayer.isRowDirty(1));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 1));

        assertTrue("changed columns are not empty", this.handler.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.handler.changedRows.isEmpty());
        assertTrue("changes are not empty", this.handler.dataChanges.isEmpty());
        assertTrue("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());

        // initial DataUpdateEvent
        // discard DataUpdateEvent
        // final DiscardDataChangesCompletedEvent
        assertEquals(3, listener.getEventsCount());
        assertTrue(listener.containsInstanceOf(DiscardDataChangesCompletedEvent.class));
        assertTrue(listener.getReceivedEvents().get(0) instanceof DataUpdateEvent);
        assertTrue(listener.getReceivedEvents().get(1) instanceof DataUpdateEvent);
        assertTrue(listener.getReceivedEvents().get(2) instanceof DiscardDataChangesCompletedEvent);
    }

    @Test
    public void shouldDiscardToOriginalAfterMultipleChanges() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Flanders"));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 1 is dirty", this.dataChangeLayer.isRowDirty(1));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 1));

        assertTrue("changed columns are not empty", this.handler.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.handler.changedRows.isEmpty());
        assertTrue("changes are not empty", this.handler.dataChanges.isEmpty());
        assertTrue("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
    }

    @Test
    public void shouldSaveChanges() {
        LayerListenerFixture listener = new LayerListenerFixture();
        this.dataChangeLayer.addLayerListener(listener);

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new SaveDataChangesCommand());

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 1 is dirty", this.dataChangeLayer.isRowDirty(1));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 1));

        assertTrue("changed columns are not empty", this.handler.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.handler.changedRows.isEmpty());
        assertTrue("changes are not empty", this.handler.dataChanges.isEmpty());
        assertTrue("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());

        // initial DataUpdateEvent
        // final SaveDataChangesCompletedEvent
        assertEquals(2, listener.getEventsCount());
        assertTrue(listener.containsInstanceOf(SaveDataChangesCompletedEvent.class));
        assertTrue(listener.getReceivedEvents().get(0) instanceof DataUpdateEvent);
        assertTrue(listener.getReceivedEvents().get(1) instanceof SaveDataChangesCompletedEvent);
    }

    @Test
    public void shouldClearOnStructuralChange() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));

        // perform a full refresh, like changing the underlying data model
        this.dataChangeLayer.doCommand(new StructuralRefreshCommand());

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 1 is dirty", this.dataChangeLayer.isRowDirty(1));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 1));

        assertTrue("changed columns are not empty", this.handler.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.handler.changedRows.isEmpty());
        assertTrue("changes are not empty", this.handler.dataChanges.isEmpty());
        assertTrue("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
    }

    @Test
    public void shouldRemoveChangeOnRowDelete() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 2));
        assertEquals("Homer", this.dataLayer.getDataValue(0, 2));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 2, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 2));
        assertEquals("Homer", this.dataLayer.getDataValue(0, 2));

        // delete the row that has changes
        this.dataModel.remove(2);
        this.dataLayer.fireLayerEvent(new RowDeleteEvent(this.dataLayer, 2));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 2));
        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 2));
        assertEquals("Bart", this.dataLayer.getDataValue(0, 2));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 2).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 1 is dirty", this.dataChangeLayer.isRowDirty(2));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 2));

        assertTrue("changed columns are not empty", this.handler.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.handler.changedRows.isEmpty());
        assertTrue("changes are not empty", this.handler.dataChanges.isEmpty());
        assertTrue("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
    }

    @Test
    public void shouldUpdateChangeOnRowDelete() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 3));
        assertEquals("Bart", this.dataLayer.getDataValue(0, 3));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 3, "Lovejoy"));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 2));
        assertEquals("Homer", this.dataLayer.getDataValue(0, 2));
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 3));
        assertEquals("Bart", this.dataLayer.getDataValue(0, 3));

        // delete a row before the change
        this.dataModel.remove(2);
        this.dataLayer.fireLayerEvent(new RowDeleteEvent(this.dataLayer, 2));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 2));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 2));
        assertEquals("Bart", this.dataLayer.getDataValue(0, 2));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 2).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Column 1 is not dirty", this.dataChangeLayer.isColumnDirty(1));
        assertTrue("Row 1 is not dirty", this.dataChangeLayer.isRowDirty(2));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 2));

        assertFalse("changed columns are empty", this.handler.changedColumns.isEmpty());
        assertFalse("changed rows are empty", this.handler.changedRows.isEmpty());
        assertFalse("changes are empty", this.handler.dataChanges.isEmpty());
        assertFalse("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
    }

    @Test
    public void shouldUpdateOnMultiRowDelete() {
        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));
        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 3, "Lovejoy"));
        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 5, "Lovejoy"));
        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 7, "Lovejoy"));
        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 9, "Lovejoy"));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 0));
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 2));
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 3));
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 4));
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 5));
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 6));
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 7));
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 8));
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 9));

        // delete all the Simpsons
        this.dataModel.remove(8);
        this.dataModel.remove(6);
        this.dataModel.remove(4);
        this.dataModel.remove(2);
        this.dataModel.remove(0);

        this.dataLayer.fireLayerEvent(new RowDeleteEvent(this.dataLayer,
                new Range(0, 1),
                new Range(2, 3),
                new Range(4, 5),
                new Range(6, 7),
                new Range(8, 9)));

        assertEquals(13, this.dataModel.size());

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 0));
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 2));
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 3));
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 4));

        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 0));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 2));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 3));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 4));

        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 0).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 2).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 3).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 4).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 5).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 6).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 7).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 8).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 9).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 11).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 12).hasLabel(DataChangeLayer.DIRTY));

        assertTrue("Column 1 is not dirty", this.dataChangeLayer.isColumnDirty(1));

        assertTrue("Row 0 is not dirty", this.dataChangeLayer.isRowDirty(0));
        assertTrue("Row 1 is not dirty", this.dataChangeLayer.isRowDirty(1));
        assertTrue("Row 2 is not dirty", this.dataChangeLayer.isRowDirty(2));
        assertTrue("Row 3 is not dirty", this.dataChangeLayer.isRowDirty(3));
        assertTrue("Row 4 is not dirty", this.dataChangeLayer.isRowDirty(4));
        assertFalse("Row 5 is dirty", this.dataChangeLayer.isRowDirty(5));
        assertFalse("Row 6 is dirty", this.dataChangeLayer.isRowDirty(6));
        assertFalse("Row 7 is dirty", this.dataChangeLayer.isRowDirty(7));
        assertFalse("Row 8 is dirty", this.dataChangeLayer.isRowDirty(8));
        assertFalse("Row 9 is dirty", this.dataChangeLayer.isRowDirty(9));
        assertFalse("Row 10 is dirty", this.dataChangeLayer.isRowDirty(10));
        assertFalse("Row 11 is dirty", this.dataChangeLayer.isRowDirty(11));
        assertFalse("Row 12 is dirty", this.dataChangeLayer.isRowDirty(12));

        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 0));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 1));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 2));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 3));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 4));

        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 5));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(0, 2));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(2, 3));

        assertFalse("changed columns are empty", this.handler.changedColumns.isEmpty());
        assertFalse("changed rows are empty", this.handler.changedRows.isEmpty());
        assertFalse("changes are empty", this.handler.dataChanges.isEmpty());
        assertFalse("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
    }

    @Test
    public void shouldUpdateChangeOnRowInsert() {
        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 2, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 2));

        // input a new row
        this.dataModel.add(2, new Person(40, "Nelson", "Muntz", Gender.MALE, false, new Date(), 100d));
        this.dataLayer.fireLayerEvent(new RowInsertEvent(this.dataLayer, 2));

        assertEquals("Muntz", this.dataLayer.getDataValue(1, 2));
        assertEquals("Muntz", this.dataChangeLayer.getDataValueByPosition(1, 2));
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 3));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 3));

        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 2).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Row 1 is dirty", this.dataChangeLayer.isRowDirty(2));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 2));

        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 3).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Column 1 is not dirty", this.dataChangeLayer.isColumnDirty(1));
        assertTrue("Row 1 is not dirty", this.dataChangeLayer.isRowDirty(3));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 3));

        assertFalse("changed columns are empty", this.handler.changedColumns.isEmpty());
        assertFalse("changed rows are empty", this.handler.changedRows.isEmpty());
        assertFalse("changes are empty", this.handler.dataChanges.isEmpty());
        assertFalse("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
    }

    @Test
    public void shouldUpdateOnMultiRowInsert() {
        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));
        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 3, "Lovejoy"));
        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 5, "Lovejoy"));
        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 7, "Lovejoy"));
        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 9, "Lovejoy"));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 0));
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 2));
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 3));
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 4));
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 5));
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 6));
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 7));
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 8));
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 9));

        this.dataModel.add(9, new Person(44, "Nelson", "Muntz", Gender.MALE, false, new Date(), 100d));
        this.dataModel.add(7, new Person(43, "Nelson", "Muntz", Gender.MALE, false, new Date(), 100d));
        this.dataModel.add(5, new Person(42, "Nelson", "Muntz", Gender.MALE, false, new Date(), 100d));
        this.dataModel.add(3, new Person(41, "Nelson", "Muntz", Gender.MALE, false, new Date(), 100d));
        this.dataModel.add(1, new Person(40, "Nelson", "Muntz", Gender.MALE, false, new Date(), 100d));
        this.dataLayer.fireLayerEvent(new RowInsertEvent(this.dataLayer,
                new Range(1, 1),
                new Range(3, 3),
                new Range(5, 5),
                new Range(7, 7),
                new Range(9, 9)));

        assertEquals(23, this.dataModel.size());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 0));
        assertEquals("Muntz", this.dataLayer.getDataValue(1, 1));
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 2));
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 3));
        assertEquals("Muntz", this.dataLayer.getDataValue(1, 4));
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 5));
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 6));
        assertEquals("Muntz", this.dataLayer.getDataValue(1, 7));
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 8));
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Muntz", this.dataLayer.getDataValue(1, 10));
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 11));
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 12));
        assertEquals("Muntz", this.dataLayer.getDataValue(1, 13));
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 14));

        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 0));
        assertEquals("Muntz", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 2));
        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 3));
        assertEquals("Muntz", this.dataChangeLayer.getDataValueByPosition(1, 4));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 5));
        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 6));
        assertEquals("Muntz", this.dataChangeLayer.getDataValueByPosition(1, 7));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 8));
        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 9));
        assertEquals("Muntz", this.dataChangeLayer.getDataValueByPosition(1, 10));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 11));
        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 12));
        assertEquals("Muntz", this.dataChangeLayer.getDataValueByPosition(1, 13));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 14));

        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 0).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 2).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 3).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 4).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 5).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 6).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 7).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 8).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 9).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 11).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 12).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 13).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 14).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 15).hasLabel(DataChangeLayer.DIRTY));

        assertTrue("Column 1 is not dirty", this.dataChangeLayer.isColumnDirty(1));

        assertFalse("Row 0 is not dirty", this.dataChangeLayer.isRowDirty(0));
        assertFalse("Row 1 is not dirty", this.dataChangeLayer.isRowDirty(1));
        assertTrue("Row 2 is not dirty", this.dataChangeLayer.isRowDirty(2));
        assertFalse("Row 3 is not dirty", this.dataChangeLayer.isRowDirty(3));
        assertFalse("Row 4 is not dirty", this.dataChangeLayer.isRowDirty(4));
        assertTrue("Row 5 is not dirty", this.dataChangeLayer.isRowDirty(5));
        assertFalse("Row 6 is not dirty", this.dataChangeLayer.isRowDirty(6));
        assertFalse("Row 7 is not dirty", this.dataChangeLayer.isRowDirty(7));
        assertTrue("Row 8 is not dirty", this.dataChangeLayer.isRowDirty(8));
        assertFalse("Row 9 is not dirty", this.dataChangeLayer.isRowDirty(9));
        assertFalse("Row 10 is not dirty", this.dataChangeLayer.isRowDirty(10));
        assertTrue("Row 11 is not dirty", this.dataChangeLayer.isRowDirty(11));
        assertFalse("Row 12 is not dirty", this.dataChangeLayer.isRowDirty(12));
        assertFalse("Row 13 is not dirty", this.dataChangeLayer.isRowDirty(13));
        assertTrue("Row 14 is not dirty", this.dataChangeLayer.isRowDirty(14));
        assertFalse("Row 15 is not dirty", this.dataChangeLayer.isRowDirty(15));
        assertFalse("Row 16 is not dirty", this.dataChangeLayer.isRowDirty(16));
        assertFalse("Row 17 is not dirty", this.dataChangeLayer.isRowDirty(17));
        assertFalse("Row 18 is not dirty", this.dataChangeLayer.isRowDirty(18));

        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 2));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 5));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 8));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 11));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 14));

        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 4));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(0, 2));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(2, 3));

        assertFalse("changed columns are empty", this.handler.changedColumns.isEmpty());
        assertFalse("changed rows are empty", this.handler.changedRows.isEmpty());
        assertFalse("changes are empty", this.handler.dataChanges.isEmpty());
        assertFalse("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
    }

    @Test
    public void shouldRemoveChangeOnColumnDelete() {
        assertEquals("Homer", this.dataLayer.getDataValue(0, 2));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 0, 2, "Nelson"));

        assertEquals("Nelson", this.dataLayer.getDataValue(0, 2));

        // simulate column deletion
        this.dataLayer.fireLayerEvent(new ColumnDeleteEvent(this.dataLayer, 0));

        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 2).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 2 is dirty", this.dataChangeLayer.isRowDirty(2));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 2));

        assertTrue("changed columns are not empty", this.handler.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.handler.changedRows.isEmpty());
        assertTrue("changes are not empty", this.handler.dataChanges.isEmpty());
        assertTrue("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
    }

    @Test
    public void shouldUpdateChangeOnColumnDelete() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 2));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 2, "Muntz"));

        assertEquals("Muntz", this.dataLayer.getDataValue(1, 2));

        // simulate column deletion
        this.dataLayer.fireLayerEvent(new ColumnDeleteEvent(this.dataLayer, 0));

        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 2).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(0, 2).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is dirty", this.dataChangeLayer.isColumnDirty(1));
        assertTrue("Column 0 is not dirty", this.dataChangeLayer.isColumnDirty(0));
        assertTrue("Row 2 is not dirty", this.dataChangeLayer.isRowDirty(2));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(0, 2));

        assertFalse("changed columns are empty", this.handler.changedColumns.isEmpty());
        assertFalse("changed rows are empty", this.handler.changedRows.isEmpty());
        assertFalse("changes are empty", this.handler.dataChanges.isEmpty());
        assertFalse("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
    }

    @Test
    public void shouldUpdateChangeOnColumnInsert() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 2));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 2, "Muntz"));

        assertEquals("Muntz", this.dataLayer.getDataValue(1, 2));

        // simulate column deletion
        this.dataLayer.fireLayerEvent(new ColumnInsertEvent(this.dataLayer, 0));

        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 2).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(2, 2).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is dirty", this.dataChangeLayer.isColumnDirty(1));
        assertTrue("Column 2 is not dirty", this.dataChangeLayer.isColumnDirty(2));
        assertTrue("Row 2 is not dirty", this.dataChangeLayer.isRowDirty(2));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(2, 2));

        assertFalse("changed columns are empty", this.handler.changedColumns.isEmpty());
        assertFalse("changed rows are empty", this.handler.changedRows.isEmpty());
        assertFalse("changes are empty", this.handler.dataChanges.isEmpty());
        assertFalse("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
    }

    @Test
    public void shouldDiscardAfterDelete() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 9, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));

        // delete a row before the change
        this.dataModel.remove(2);
        this.dataLayer.fireLayerEvent(new RowDeleteEvent(this.dataLayer, 2));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 8));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 8));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataChangeLayer.getDataValueByPosition(1, 9));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 8).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Column 1 is not dirty", this.dataChangeLayer.isColumnDirty(1));
        assertTrue("Row 1 is not dirty", this.dataChangeLayer.isRowDirty(8));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 8));

        assertFalse("changed columns are empty", this.handler.changedColumns.isEmpty());
        assertFalse("changed rows are empty", this.handler.changedRows.isEmpty());
        assertFalse("changes are empty", this.handler.dataChanges.isEmpty());
        assertFalse("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());

        // now discard and check that previous state is restored correctly
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 8));
        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 8));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataChangeLayer.getDataValueByPosition(1, 9));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 8).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 1 is dirty", this.dataChangeLayer.isRowDirty(8));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 8));

        assertTrue("changed columns are not empty", this.handler.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.handler.changedRows.isEmpty());
        assertTrue("changes are not empty", this.handler.dataChanges.isEmpty());
        assertTrue("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
    }

    @Test
    public void shouldSaveAfterDelete() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 9, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));

        // delete a row before the change
        this.dataModel.remove(2);
        this.dataLayer.fireLayerEvent(new RowDeleteEvent(this.dataLayer, 2));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 8));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 8));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataChangeLayer.getDataValueByPosition(1, 9));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 8).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Column 1 is not dirty", this.dataChangeLayer.isColumnDirty(1));
        assertTrue("Row 1 is not dirty", this.dataChangeLayer.isRowDirty(8));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 8));

        assertFalse("changed columns are empty", this.handler.changedColumns.isEmpty());
        assertFalse("changed rows are empty", this.handler.changedRows.isEmpty());
        assertFalse("changes are empty", this.handler.dataChanges.isEmpty());
        assertFalse("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());

        // now discard and check that previous state is restored correctly
        this.dataChangeLayer.doCommand(new SaveDataChangesCommand());

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 8));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 8));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataChangeLayer.getDataValueByPosition(1, 9));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 8).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 1 is dirty", this.dataChangeLayer.isRowDirty(8));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 8));

        assertTrue("changed columns are not empty", this.handler.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.handler.changedRows.isEmpty());
        assertTrue("changes are not empty", this.handler.dataChanges.isEmpty());
        assertTrue("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
    }

    @Test
    public void shouldNotBeDirtyForSameValue() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Simpson"));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 1 is dirty", this.dataChangeLayer.isRowDirty(1));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 1));
    }

    @Test
    public void shouldNotBeDirtyOnSettingSameValue() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Column 1 is not dirty", this.dataChangeLayer.isColumnDirty(1));
        assertTrue("Row 1 is not dirty", this.dataChangeLayer.isRowDirty(1));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Simpson"));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 1 is dirty", this.dataChangeLayer.isRowDirty(1));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 1));
    }

    @Test
    public void shouldUpdateToNullAndBackAgain() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, null));

        assertEquals(null, this.dataLayer.getDataValue(1, 1));
        assertEquals(null, this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Column 1 is not dirty", this.dataChangeLayer.isColumnDirty(1));
        assertTrue("Row 1 is not dirty", this.dataChangeLayer.isRowDirty(1));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Simpson"));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 1 is dirty", this.dataChangeLayer.isRowDirty(1));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 1));
    }

    @Test
    public void shouldUpdateNullValueAndBackAgain() {
        this.dataLayer.setDataValue(1, 1, null);

        assertNull(this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Column 1 is not dirty", this.dataChangeLayer.isColumnDirty(1));
        assertTrue("Row 1 is not dirty", this.dataChangeLayer.isRowDirty(1));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, null));

        assertNull(this.dataLayer.getDataValue(1, 1));
        assertNull(this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 1 is dirty", this.dataChangeLayer.isRowDirty(1));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 1));
    }

    @Test
    public void shouldNotDirtyNullToNull() {
        this.dataLayer.setDataValue(1, 1, null);

        assertNull(this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, null));

        assertNull(this.dataLayer.getDataValue(1, 1));
        assertNull(this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 1 is dirty", this.dataChangeLayer.isRowDirty(1));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 1));
    }

    @Test
    public void shouldDiscardAfterInsert() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 9, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));

        // add a row before the change
        this.dataModel.add(2, new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date()));
        this.dataLayer.fireLayerEvent(new RowInsertEvent(this.dataLayer, 2));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 10));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 10));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 11));
        assertEquals("Flanders", this.dataChangeLayer.getDataValueByPosition(1, 11));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Column 1 is not dirty", this.dataChangeLayer.isColumnDirty(1));
        assertTrue("Row 1 is not dirty", this.dataChangeLayer.isRowDirty(10));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 10));

        assertFalse("changed columns are empty", this.handler.changedColumns.isEmpty());
        assertFalse("changed rows are empty", this.handler.changedRows.isEmpty());
        assertFalse("changes are empty", this.handler.dataChanges.isEmpty());
        assertFalse("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());

        // now discard and check that previous state is restored correctly
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 10));
        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 10));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 11));
        assertEquals("Flanders", this.dataChangeLayer.getDataValueByPosition(1, 11));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 1 is dirty", this.dataChangeLayer.isRowDirty(10));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 10));

        assertTrue("changed columns are not empty", this.handler.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.handler.changedRows.isEmpty());
        assertTrue("changes are not empty", this.handler.dataChanges.isEmpty());
        assertTrue("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
    }
}
