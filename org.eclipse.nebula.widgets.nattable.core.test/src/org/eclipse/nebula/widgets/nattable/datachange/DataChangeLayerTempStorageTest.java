/*******************************************************************************
 * Copyright (c) 2017, 2022 Dirk Fauth.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DataChangeLayerTempStorageTest {

    private List<Person> dataModel;

    private DataLayer dataLayer;
    private DataChangeLayer dataChangeLayer;
    private TemporaryUpdateDataChangeHandler handler;

    @BeforeEach
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
        assertNotNull(this.handler, "PersistenceUpdateDataChangeHandler not found");
    }

    @Test
    public void shouldNotUpdateDataInDataLayer() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(1), "Row 1 is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 1), "Cell is not dirty");
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
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(1), "Row 1 is not dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 1), "Cell is not dirty");

        assertTrue(this.handler.changedColumns.isEmpty(), "changed columns are not empty");
        assertTrue(this.handler.changedRows.isEmpty(), "changed rows are not empty");
        assertTrue(this.handler.dataChanges.isEmpty(), "changes are not empty");
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
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
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(1), "Row 1 is not dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 1), "Cell is not dirty");

        assertTrue(this.handler.changedColumns.isEmpty(), "changed columns are not empty");
        assertTrue(this.handler.changedRows.isEmpty(), "changed rows are not empty");
        assertTrue(this.handler.dataChanges.isEmpty(), "changes are not empty");
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");

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
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(1), "Row 1 is not dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 1), "Cell is not dirty");

        assertTrue(this.handler.changedColumns.isEmpty(), "changed columns are not empty");
        assertTrue(this.handler.changedRows.isEmpty(), "changed rows are not empty");
        assertTrue(this.handler.dataChanges.isEmpty(), "changes are not empty");
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");

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
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(1), "Row 1 is not dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 1), "Cell is not dirty");

        assertTrue(this.handler.changedColumns.isEmpty(), "changed columns are not empty");
        assertTrue(this.handler.changedRows.isEmpty(), "changed rows are not empty");
        assertTrue(this.handler.dataChanges.isEmpty(), "changes are not empty");
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
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
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 8).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(8), "Row 1 is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 8), "Cell is not dirty");

        assertFalse(this.handler.changedColumns.isEmpty(), "changed columns are empty");
        assertFalse(this.handler.changedRows.isEmpty(), "changed rows are empty");
        assertFalse(this.handler.dataChanges.isEmpty(), "changes are empty");
        assertFalse(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");

        // now discard and check that previous state is restored correctly
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 8));
        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 8));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataChangeLayer.getDataValueByPosition(1, 9));
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 8).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(8), "Row 1 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 8), "Cell is dirty");

        assertTrue(this.handler.changedColumns.isEmpty(), "changed columns are not empty");
        assertTrue(this.handler.changedRows.isEmpty(), "changed rows are not empty");
        assertTrue(this.handler.dataChanges.isEmpty(), "changes are not empty");
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
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
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 8).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(8), "Row 1 is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 8), "Cell is not dirty");

        assertFalse(this.handler.changedColumns.isEmpty(), "changed columns are empty");
        assertFalse(this.handler.changedRows.isEmpty(), "changed rows are empty");
        assertFalse(this.handler.dataChanges.isEmpty(), "changes are empty");
        assertFalse(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");

        // now discard and check that previous state is restored correctly
        this.dataChangeLayer.doCommand(new SaveDataChangesCommand());

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 8));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 8));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataChangeLayer.getDataValueByPosition(1, 9));
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 8).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(8), "Row 1 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 8), "Cell is dirty");

        assertTrue(this.handler.changedColumns.isEmpty(), "changed columns are not empty");
        assertTrue(this.handler.changedRows.isEmpty(), "changed rows are not empty");
        assertTrue(this.handler.dataChanges.isEmpty(), "changes are not empty");
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
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
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(10), "Row 1 is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 10), "Cell is not dirty");

        assertFalse(this.handler.changedColumns.isEmpty(), "changed columns are empty");
        assertFalse(this.handler.changedRows.isEmpty(), "changed rows are empty");
        assertFalse(this.handler.dataChanges.isEmpty(), "changes are empty");
        assertFalse(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");

        // now discard and check that previous state is restored correctly
        this.dataChangeLayer.doCommand(new SaveDataChangesCommand());

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 10));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 10));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 11));
        assertEquals("Flanders", this.dataChangeLayer.getDataValueByPosition(1, 11));
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(10), "Row 1 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 10), "Cell is dirty");

        assertTrue(this.handler.changedColumns.isEmpty(), "changed columns are not empty");
        assertTrue(this.handler.changedRows.isEmpty(), "changed rows are not empty");
        assertTrue(this.handler.dataChanges.isEmpty(), "changes are not empty");
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
    }

    @Test
    public void shouldNotBeDirtyForSameValue() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Simpson"));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(1), "Row 1 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 1), "Cell is dirty");
    }

    @Test
    public void shouldNotBeDirtyOnResettingSameValue() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(1), "Row 1 is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 1), "Cell is not dirty");

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Simpson"));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(1), "Row 1 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 1), "Cell is dirty");
    }

    @Test
    public void shouldUpdateToNullAndBackAgain() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, null));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertEquals(null, this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(1), "Row 1 is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 1), "Cell is not dirty");

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Simpson"));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(1), "Row 1 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 1), "Cell is dirty");
    }

    @Test
    public void shouldUpdateNullValueAndBackAgain() {
        this.dataLayer.setDataValue(1, 1, null);

        assertNull(this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertNull(this.dataLayer.getDataValue(1, 1));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(1), "Row 1 is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 1), "Cell is not dirty");

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, null));

        assertNull(this.dataLayer.getDataValue(1, 1));
        assertNull(this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(1), "Row 1 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 1), "Cell is dirty");
    }

    @Test
    public void shouldNotDirtyNullToNull() {
        this.dataLayer.setDataValue(1, 1, null);

        assertNull(this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, null));

        assertNull(this.dataLayer.getDataValue(1, 1));
        assertNull(this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(1), "Row 1 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 1), "Cell is dirty");
    }
}
