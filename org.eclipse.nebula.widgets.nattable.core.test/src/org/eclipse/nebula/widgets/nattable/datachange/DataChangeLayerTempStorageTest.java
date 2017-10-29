/*******************************************************************************
 * Copyright (c) 2017 Dirk Fauth.
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.StructuralRefreshCommand;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.datachange.command.DiscardDataChangesCommand;
import org.eclipse.nebula.widgets.nattable.datachange.command.SaveDataChangesCommand;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.edit.event.DataUpdateEvent;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowDeleteEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Before;
import org.junit.Test;

public class DataChangeLayerTempStorageTest {

    private List<Person> dataModel;

    private DataLayer dataLayer;
    private DataChangeLayer dataChangeLayer;

    private LayerListenerFixture listener;

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

        this.listener = new LayerListenerFixture();
        this.dataLayer.addLayerListener(this.listener);
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

        assertTrue("changed columns are not empty", this.dataChangeLayer.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.dataChangeLayer.changedRows.isEmpty());
        assertTrue("changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
    }

    @Test
    public void shouldDiscardChanges() {
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

        assertTrue("changed columns are not empty", this.dataChangeLayer.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.dataChangeLayer.changedRows.isEmpty());
        assertTrue("changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
    }

    @Test
    public void shouldSaveChanges() {
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

        assertTrue("changed columns are not empty", this.dataChangeLayer.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.dataChangeLayer.changedRows.isEmpty());
        assertTrue("changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());

        assertEquals(1, this.listener.getReceivedEvents().size());
        assertTrue(this.listener.getReceivedEvents().get(0) instanceof DataUpdateEvent);
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

        assertTrue("changed columns are not empty", this.dataChangeLayer.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.dataChangeLayer.changedRows.isEmpty());
        assertTrue("changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
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

        assertFalse("changed columns are empty", this.dataChangeLayer.changedColumns.isEmpty());
        assertFalse("changed rows are empty", this.dataChangeLayer.changedRows.isEmpty());
        assertFalse("changes are empty", this.dataChangeLayer.dataChanges.isEmpty());

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

        assertTrue("changed columns are not empty", this.dataChangeLayer.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.dataChangeLayer.changedRows.isEmpty());
        assertTrue("changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
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

        assertFalse("changed columns are empty", this.dataChangeLayer.changedColumns.isEmpty());
        assertFalse("changed rows are empty", this.dataChangeLayer.changedRows.isEmpty());
        assertFalse("changes are empty", this.dataChangeLayer.dataChanges.isEmpty());

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

        assertTrue("changed columns are not empty", this.dataChangeLayer.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.dataChangeLayer.changedRows.isEmpty());
        assertTrue("changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
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
        // this behavior is different to temporaryDataStorage=true
        // without temporary data storage it is not possible to check for the
        // pre-saved state
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
}
