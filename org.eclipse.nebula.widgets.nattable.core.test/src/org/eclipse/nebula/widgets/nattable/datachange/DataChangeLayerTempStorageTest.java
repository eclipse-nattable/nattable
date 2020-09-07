/*******************************************************************************
 * Copyright (c) 2017, 2020 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.datachange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.StructuralRefreshCommand;
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
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowDeleteEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowInsertEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Before;
import org.junit.Test;

public class DataChangeLayerTempStorageTest {

    private List<Person> dataModel;

    private DataLayer dataLayer;
    private DataChangeLayer dataChangeLayer;
    private TemporaryUpdateDataChangeHandler handler;

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
        this.dataChangeLayer = new DataChangeLayer(this.dataLayer, new PointKeyHandler(), true);
        for (DataChangeHandler h : this.dataChangeLayer.dataChangeHandler) {
            if (h instanceof TemporaryUpdateDataChangeHandler) {
                this.handler = (TemporaryUpdateDataChangeHandler) h;
                break;
            }
        }
        assertNotNull("PersistenceUpdateDataChangeHandler not found", this.handler);
    }

    @Test
    public void shouldNotUpdateDataInDataLayer() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
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

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 1));

        // clear locally stored data changes
        this.dataChangeLayer.clearDataChanges();

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is not dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 1 is not dirty", this.dataChangeLayer.isRowDirty(1));
        assertFalse("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 1));

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

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 1));

        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is not dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 1 is not dirty", this.dataChangeLayer.isRowDirty(1));
        assertFalse("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 1));

        assertTrue("changed columns are not empty", this.handler.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.handler.changedRows.isEmpty());
        assertTrue("changes are not empty", this.handler.dataChanges.isEmpty());
        assertTrue("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());

        // initial CellVisualChangeEvent
        // final DiscardDataChangesCompletedEvent
        assertEquals(2, listener.getEventsCount());
        assertTrue(listener.containsInstanceOf(CellVisualChangeEvent.class));
        assertTrue(listener.getReceivedEvents().get(0) instanceof CellVisualChangeEvent);
        assertTrue(listener.getReceivedEvents().get(1) instanceof DiscardDataChangesCompletedEvent);
    }

    @Test
    public void shouldSaveChanges() {
        LayerListenerFixture listener = new LayerListenerFixture();
        this.dataChangeLayer.addLayerListener(listener);

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 1));

        this.dataChangeLayer.doCommand(new SaveDataChangesCommand());

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is not dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 1 is not dirty", this.dataChangeLayer.isRowDirty(1));
        assertFalse("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 1));

        assertTrue("changed columns are not empty", this.handler.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.handler.changedRows.isEmpty());
        assertTrue("changes are not empty", this.handler.dataChanges.isEmpty());
        assertTrue("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());

        // initial CellVisualChangeEvent
        // update DataUpdateEvent
        // final SaveDataChangesCompletedEvent
        assertEquals(3, listener.getEventsCount());
        assertTrue(listener.containsInstanceOf(SaveDataChangesCompletedEvent.class));
        assertTrue(listener.getReceivedEvents().get(0) instanceof CellVisualChangeEvent);
        assertTrue(listener.getReceivedEvents().get(1) instanceof DataUpdateEvent);
        assertTrue(listener.getReceivedEvents().get(2) instanceof SaveDataChangesCompletedEvent);
    }

    @Test
    public void shouldClearOnStructuralChange() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 1));

        // clear locally stored data changes
        this.dataChangeLayer.doCommand(new StructuralRefreshCommand());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is not dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 1 is not dirty", this.dataChangeLayer.isRowDirty(1));
        assertFalse("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 1));

        assertTrue("changed columns are not empty", this.handler.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.handler.changedRows.isEmpty());
        assertTrue("changes are not empty", this.handler.dataChanges.isEmpty());
        assertTrue("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
    }

    @Test
    public void shouldDiscardAfterDelete() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 9, "Lovejoy"));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));

        // delete a row before the change
        this.dataModel.remove(2);
        this.dataLayer.fireLayerEvent(new RowDeleteEvent(this.dataLayer, 2));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 8));
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

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));

        // delete a row before the change
        this.dataModel.remove(2);
        this.dataLayer.fireLayerEvent(new RowDeleteEvent(this.dataLayer, 2));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 8));
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
    public void shouldSaveAfterInsert() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 9, "Lovejoy"));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));

        // add a row before the change
        this.dataModel.add(2, new Person(42, "Ralph", "Wiggum", Gender.MALE, true, new Date(), 100d));
        this.dataLayer.fireLayerEvent(new RowInsertEvent(this.dataLayer, 2));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 10));
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
        this.dataChangeLayer.doCommand(new SaveDataChangesCommand());

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 10));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 10));
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
    public void shouldNotBeDirtyOnResettingSameValue() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
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

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertEquals(null, this.dataChangeLayer.getDataValueByPosition(1, 1));
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
    public void shouldUpdateNullValueAndBackAgain() {
        this.dataLayer.setDataValue(1, 1, null);

        assertNull(this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertNull(this.dataLayer.getDataValue(1, 1));
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
}
