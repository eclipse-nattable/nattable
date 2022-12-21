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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.StructuralRefreshCommand;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.datachange.command.DiscardDataChangesCommand;
import org.eclipse.nebula.widgets.nattable.datachange.command.SaveDataChangesCommand;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnDeleteEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnInsertEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowDeleteEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowInsertEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DataChangeLayerIdIndexTest {

    private List<Person> dataModel;
    private IRowDataProvider<Person> dataProvider;
    private DataLayer dataLayer;
    private DataChangeLayer dataChangeLayer;
    private PersistenceUpdateDataChangeHandler handler;

    @BeforeEach
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
        this.dataChangeLayer = new DataChangeLayer(this.dataLayer,
                new IdIndexKeyHandler<>(
                        this.dataProvider,
                        new IRowIdAccessor<Person>() {

                            @Override
                            public Serializable getRowId(Person rowObject) {
                                return rowObject.getId();
                            }
                        }),
                false);
        for (DataChangeHandler h : this.dataChangeLayer.dataChangeHandler) {
            if (h instanceof PersistenceUpdateDataChangeHandler) {
                this.handler = (PersistenceUpdateDataChangeHandler) h;
                break;
            }
        }
        assertNotNull(this.handler, "PersistenceUpdateDataChangeHandler not found");
    }

    @Test
    public void shouldUpdateDataInDataLayer() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));
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

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));

        // only clear no restore, changed data is not reset
        this.dataChangeLayer.clearDataChanges();

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(1), "Row 1 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 1), "Cell is dirty");

        assertTrue(this.handler.changedColumns.isEmpty(), "changed columns are not empty");
        assertTrue(this.handler.changedRows.isEmpty(), "changed rows are not empty");
        assertTrue(this.handler.dataChanges.isEmpty(), "changes are not empty");
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
    }

    @Test
    public void shouldDiscardChanges() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(1), "Row 1 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 1), "Cell is dirty");

        assertTrue(this.handler.changedColumns.isEmpty(), "changed columns are not empty");
        assertTrue(this.handler.changedRows.isEmpty(), "changed rows are not empty");
        assertTrue(this.handler.dataChanges.isEmpty(), "changes are not empty");
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
    }

    @Test
    public void shouldSaveChanges() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new SaveDataChangesCommand());

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(1), "Row 1 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 1), "Cell is dirty");

        assertTrue(this.handler.changedColumns.isEmpty(), "changed columns are not empty");
        assertTrue(this.handler.changedRows.isEmpty(), "changed rows are not empty");
        assertTrue(this.handler.dataChanges.isEmpty(), "changes are not empty");
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
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
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(1), "Row 1 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 1), "Cell is dirty");

        assertTrue(this.handler.changedColumns.isEmpty(), "changed columns are not empty");
        assertTrue(this.handler.changedRows.isEmpty(), "changed rows are not empty");
        assertTrue(this.handler.dataChanges.isEmpty(), "changes are not empty");
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
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
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 2).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(2), "Row 1 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 2), "Cell is dirty");

        assertTrue(this.handler.changedColumns.isEmpty(), "changed columns are not empty");
        assertTrue(this.handler.changedRows.isEmpty(), "changed rows are not empty");
        assertTrue(this.handler.dataChanges.isEmpty(), "changes are not empty");
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
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

        // delete the row that has changes
        this.dataModel.remove(2);
        this.dataLayer.fireLayerEvent(new RowDeleteEvent(this.dataLayer, 2));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 2));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 2));
        assertEquals("Bart", this.dataLayer.getDataValue(0, 2));
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 2).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(2), "Row 1 is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 2), "Cell is not dirty");

        assertFalse(this.handler.changedColumns.isEmpty(), "changed columns are empty");
        assertFalse(this.handler.changedRows.isEmpty(), "changed rows are empty");
        assertFalse(this.handler.dataChanges.isEmpty(), "changes are empty");
        assertFalse(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
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

        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 0).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 2).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 3).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 4).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 5).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 6).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 7).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 8).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 9).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 11).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 12).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");

        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");

        assertTrue(this.dataChangeLayer.isRowDirty(0), "Row 0 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(1), "Row 1 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(2), "Row 2 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(3), "Row 3 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(4), "Row 4 is not dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(5), "Row 5 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(6), "Row 6 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(7), "Row 7 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(8), "Row 8 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(9), "Row 9 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(10), "Row 10 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(11), "Row 11 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(12), "Row 12 is dirty");

        assertTrue(this.dataChangeLayer.isCellDirty(1, 0), "Cell is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 1), "Cell is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 2), "Cell is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 3), "Cell is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 4), "Cell is not dirty");

        assertFalse(this.dataChangeLayer.isCellDirty(1, 5), "Cell is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(0, 2), "Cell is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(2, 3), "Cell is dirty");

        assertFalse(this.handler.changedColumns.isEmpty(), "changed columns are empty");
        assertFalse(this.handler.changedRows.isEmpty(), "changed rows are empty");
        assertFalse(this.handler.dataChanges.isEmpty(), "changes are empty");
        assertFalse(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
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

        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 2).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.isRowDirty(2), "Row 1 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 2), "Cell is dirty");

        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 3).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(3), "Row 1 is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 3), "Cell is not dirty");

        assertFalse(this.handler.changedColumns.isEmpty(), "changed columns are empty");
        assertFalse(this.handler.changedRows.isEmpty(), "changed rows are empty");
        assertFalse(this.handler.dataChanges.isEmpty(), "changes are empty");
        assertFalse(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
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

        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 0).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 2).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 3).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 4).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 5).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 6).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 7).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 8).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 9).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 11).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 12).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 13).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 14).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 15).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");

        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");

        assertFalse(this.dataChangeLayer.isRowDirty(0), "Row 0 is not dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(1), "Row 1 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(2), "Row 2 is not dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(3), "Row 3 is not dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(4), "Row 4 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(5), "Row 5 is not dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(6), "Row 6 is not dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(7), "Row 7 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(8), "Row 8 is not dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(9), "Row 9 is not dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(10), "Row 10 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(11), "Row 11 is not dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(12), "Row 12 is not dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(13), "Row 13 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(14), "Row 14 is not dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(15), "Row 15 is not dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(16), "Row 16 is not dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(17), "Row 17 is not dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(18), "Row 18 is not dirty");

        assertTrue(this.dataChangeLayer.isCellDirty(1, 2), "Cell is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 5), "Cell is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 8), "Cell is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 11), "Cell is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 14), "Cell is not dirty");

        assertFalse(this.dataChangeLayer.isCellDirty(1, 4), "Cell is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(0, 2), "Cell is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(2, 3), "Cell is dirty");

        assertFalse(this.handler.changedColumns.isEmpty(), "changed columns are empty");
        assertFalse(this.handler.changedRows.isEmpty(), "changed rows are empty");
        assertFalse(this.handler.dataChanges.isEmpty(), "changes are empty");
        assertFalse(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
    }

    @Test
    public void shouldRemoveChangeOnColumnDelete() {
        assertEquals("Homer", this.dataLayer.getDataValue(0, 2));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 0, 2, "Nelson"));

        assertEquals("Nelson", this.dataLayer.getDataValue(0, 2));

        // simulate column deletion
        this.dataLayer.fireLayerEvent(new ColumnDeleteEvent(this.dataLayer, 0));

        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 2).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(2), "Row 2 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 2), "Cell is dirty");

        assertTrue(this.handler.changedColumns.isEmpty(), "changed columns are not empty");
        assertTrue(this.handler.changedRows.isEmpty(), "changed rows are not empty");
        assertTrue(this.handler.dataChanges.isEmpty(), "changes are not empty");
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
    }

    @Test
    public void shouldUpdateChangeOnColumnDelete() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 2));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 2, "Muntz"));

        assertEquals("Muntz", this.dataLayer.getDataValue(1, 2));

        // simulate column deletion
        this.dataLayer.fireLayerEvent(new ColumnDeleteEvent(this.dataLayer, 0));

        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 2).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(0, 2).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertTrue(this.dataChangeLayer.isColumnDirty(0), "Column 0 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(2), "Row 2 is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(0, 2), "Cell is not dirty");

        assertFalse(this.handler.changedColumns.isEmpty(), "changed columns are empty");
        assertFalse(this.handler.changedRows.isEmpty(), "changed rows are empty");
        assertFalse(this.handler.dataChanges.isEmpty(), "changes are empty");
        assertFalse(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
    }

    @Test
    public void shouldUpdateChangeOnColumnInsert() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 2));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 2, "Muntz"));

        assertEquals("Muntz", this.dataLayer.getDataValue(1, 2));

        // simulate column deletion
        this.dataLayer.fireLayerEvent(new ColumnInsertEvent(this.dataLayer, 0));

        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 2).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(2, 2).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertTrue(this.dataChangeLayer.isColumnDirty(2), "Column 2 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(2), "Row 2 is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(2, 2), "Cell is not dirty");

        assertFalse(this.handler.changedColumns.isEmpty(), "changed columns are empty");
        assertFalse(this.handler.changedRows.isEmpty(), "changed rows are empty");
        assertFalse(this.handler.dataChanges.isEmpty(), "changes are empty");
        assertFalse(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
    }

    @Test
    public void shouldUpdateChangeOnSortAndDiscard() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 2));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 2, "Zoolander"));

        assertEquals("Zoolander", this.dataLayer.getDataValue(1, 2));

        // sort
        Collections.sort(this.dataModel, (o1, o2) -> o1.getLastName().compareTo(o2.getLastName()));
        // fire event to trigger update of cached row indexes
        this.dataLayer.fireLayerEvent(new RowStructuralRefreshEvent(this.dataLayer));

        // now the dirty state should be on the last row

        assertEquals("Zoolander", this.dataLayer.getDataValue(1, 17));
        assertEquals("Zoolander", this.dataChangeLayer.getDataValueByPosition(1, 17));
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 2).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 17).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(2), "Row 2 is dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(17), "Row 17 is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 17), "Cell is not dirty");

        assertFalse(this.handler.changedColumns.isEmpty(), "changed columns are empty");
        assertFalse(this.handler.changedRows.isEmpty(), "changed rows are empty");
        assertFalse(this.handler.dataChanges.isEmpty(), "changes are empty");
        assertFalse(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");

        // check if discard is applied correctly
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 17));
        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 17));
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 2).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 17).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(2), "Row 2 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(17), "Row 17 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 17), "Cell is dirty");

        assertTrue(this.handler.changedColumns.isEmpty(), "changed columns are not empty");
        assertTrue(this.handler.changedRows.isEmpty(), "changed rows are not empty");
        assertTrue(this.handler.dataChanges.isEmpty(), "changes are not empty");
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
    }

    @Test
    public void shouldUpdateChangeOnSortAndSaveWithTemp() {
        // enable temp data storage to test update on save
        this.dataChangeLayer = new DataChangeLayer(this.dataLayer,
                new IdIndexKeyHandler<>(
                        this.dataProvider,
                        new IRowIdAccessor<Person>() {

                            @Override
                            public Serializable getRowId(Person rowObject) {
                                return rowObject.getId();
                            }
                        }),
                true);

        TemporaryUpdateDataChangeHandler tempHandler = null;
        for (DataChangeHandler h : this.dataChangeLayer.dataChangeHandler) {
            if (h instanceof TemporaryUpdateDataChangeHandler) {
                tempHandler = (TemporaryUpdateDataChangeHandler) h;
                break;
            }
        }
        assertNotNull(tempHandler, "TemporaryUpdateDataChangeHandler not found");

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 2));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 2, "Zoolander"));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 2));
        assertEquals("Zoolander", this.dataChangeLayer.getDataValueByPosition(1, 2));

        // sort via firstname
        // NOTE: this is because we are storing the data temporary and therefore
        // sorting for lastname would not respect the shown data.
        this.dataLayer.setDataValueByPosition(0, 2, "Yosefine");
        Collections.sort(this.dataModel, (o1, o2) -> o1.getFirstName().compareTo(o2.getFirstName()));
        // fire event to trigger update of cached row indexes
        this.dataLayer.fireLayerEvent(new RowStructuralRefreshEvent(this.dataLayer));

        // now the dirty state should be on the last row

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 17));
        assertEquals("Zoolander", this.dataChangeLayer.getDataValueByPosition(1, 17));
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 2).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 17).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(2), "Row 2 is dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(17), "Row 17 is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 17), "Cell is not dirty");

        assertFalse(tempHandler.changedColumns.isEmpty(), "changed columns are empty");
        assertFalse(tempHandler.changedRows.isEmpty(), "changed rows are empty");
        assertFalse(tempHandler.dataChanges.isEmpty(), "changes are empty");
        assertFalse(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");

        // check if discard is applied correctly
        this.dataChangeLayer.doCommand(new SaveDataChangesCommand());

        assertEquals("Zoolander", this.dataLayer.getDataValue(1, 17));
        assertEquals("Zoolander", this.dataChangeLayer.getDataValueByPosition(1, 17));
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 2).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 17).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(2), "Row 2 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(17), "Row 17 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 17), "Cell is dirty");

        assertTrue(tempHandler.changedColumns.isEmpty(), "changed columns are not empty");
        assertTrue(tempHandler.changedRows.isEmpty(), "changed rows are not empty");
        assertTrue(tempHandler.dataChanges.isEmpty(), "changes are not empty");
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
        assertEquals(0, this.dataChangeLayer.dataChanges.size());
    }

    @Test
    public void shouldNotBeDirtyOnSettingSameValue() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(1), "Row 1 is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 1), "Cell is not dirty");
        assertEquals(1, this.dataChangeLayer.dataChanges.size());

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Simpson"));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertEquals("Simpson", this.dataChangeLayer.getDataValueByPosition(1, 1));
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 1).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(1), "Row 1 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 1), "Cell is dirty");
        assertEquals(2, this.dataChangeLayer.dataChanges.size());
    }

}
