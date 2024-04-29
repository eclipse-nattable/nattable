/*******************************************************************************
 * Copyright (c) 2018, 2024 Dirk Fauth.
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
import java.util.Date;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.StructuralRefreshCommand;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.command.RowDeleteCommand;
import org.eclipse.nebula.widgets.nattable.data.command.RowDeleteCommandHandler;
import org.eclipse.nebula.widgets.nattable.data.command.RowInsertCommand;
import org.eclipse.nebula.widgets.nattable.data.command.RowInsertCommandHandler;
import org.eclipse.nebula.widgets.nattable.datachange.command.DiscardDataChangesCommand;
import org.eclipse.nebula.widgets.nattable.datachange.command.SaveDataChangesCommand;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class RowStructuralDataChangeIdIndexTest {

    private List<Person> dataModel;
    private ListDataProvider<Person> dataProvider;
    private DataLayer dataLayer;
    private DataChangeLayer dataChangeLayer;
    private PersistenceUpdateDataChangeHandler updateHandler;
    private RowInsertDataChangeHandler insertHandler;
    private RowDeleteDataChangeHandler deleteHandler;

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

        IRowIdAccessor<Person> rowIdAccessor = new IRowIdAccessor<Person>() {

            @Override
            public Serializable getRowId(Person rowObject) {
                return rowObject.getId();
            }
        };

        this.dataLayer = new DataLayer(this.dataProvider);
        this.dataLayer.registerCommandHandler(new RowInsertCommandHandler<>(this.dataModel));
        this.dataLayer.registerCommandHandler(new RowDeleteCommandHandler<>(this.dataModel));

        this.dataChangeLayer = new DataChangeLayer(this.dataLayer,
                new IdIndexKeyHandler<>(
                        this.dataProvider,
                        rowIdAccessor),
                // configure persistence mode for data changes
                false,
                // enable tracking of row structural changes
                true);

        for (DataChangeHandler h : this.dataChangeLayer.dataChangeHandler) {
            if (h instanceof RowInsertDataChangeHandler) {
                this.insertHandler = (RowInsertDataChangeHandler) h;
            } else if (h instanceof RowDeleteDataChangeHandler) {
                this.deleteHandler = (RowDeleteDataChangeHandler) h;
            } else if (h instanceof PersistenceUpdateDataChangeHandler) {
                this.updateHandler = (PersistenceUpdateDataChangeHandler) h;
            }
        }
        assertNotNull(this.insertHandler, "RowInsertDataChangeHandler not found");
        assertNotNull(this.deleteHandler, "RowDeleteDataChangeHandler not found");
        assertNotNull(this.updateHandler, "PersistenceUpdateDataChangeHandler not found");
    }

    @Test
    public void shouldTrackRowInsertEvent() {
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataChangeLayer.doCommand(new RowInsertCommand<>(ralph));

        int ralphIndex = this.dataModel.indexOf(ralph);
        Object key = this.insertHandler.keyHandler.getKey(-1, ralphIndex);

        assertEquals(19, this.dataModel.size());

        assertTrue(this.dataChangeLayer.isDirty(), "State is not dirty");

        // row added so all columns are actually dirty
        assertTrue(this.dataChangeLayer.isColumnDirty(0), "Column 0 is not dirty");
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertTrue(this.dataChangeLayer.isColumnDirty(2), "Column 2 is not dirty");
        assertTrue(this.dataChangeLayer.isColumnDirty(3), "Column 3 is not dirty");
        assertTrue(this.dataChangeLayer.isColumnDirty(4), "Column 4 is not dirty");

        assertTrue(this.dataChangeLayer.isRowDirty(ralphIndex), "Row " + ralphIndex + " is not dirty");

        assertTrue(this.dataChangeLayer.isCellDirty(0, ralphIndex), "Cell 0/" + ralphIndex + " is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, ralphIndex), "Cell 1/" + ralphIndex + " is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(2, ralphIndex), "Cell 2/" + ralphIndex + " is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(3, ralphIndex), "Cell 3/" + ralphIndex + " is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(4, ralphIndex), "Cell 4/" + ralphIndex + " is not dirty");

        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(0, ralphIndex).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, ralphIndex).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(2, ralphIndex).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(3, ralphIndex).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(4, ralphIndex).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");

        assertEquals(1, this.dataChangeLayer.dataChanges.size());
        assertEquals(1, this.insertHandler.dataChanges.size());
        // we use the IdIndexKeyHandler so casting is fine
        IdIndexIdentifier<Person> identifier = (IdIndexIdentifier<Person>) this.insertHandler.dataChanges.get(key).getKey();
        assertEquals(ralph, identifier.rowObject);
        assertEquals(42, identifier.rowId);
    }

    @Test
    public void shouldClearRowInsertWithoutReset() {
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataChangeLayer.doCommand(new RowInsertCommand<>(ralph));

        int ralphIndex = this.dataModel.indexOf(ralph);
        Object key = this.insertHandler.keyHandler.getKey(-1, ralphIndex);

        // test some states
        assertEquals(19, this.dataModel.size());

        assertEquals(1, this.dataChangeLayer.dataChanges.size());
        assertEquals(1, this.insertHandler.dataChanges.size());

        assertTrue(this.dataChangeLayer.isDirty(), "State is not dirty");

        assertTrue(this.dataChangeLayer.isColumnDirty(0), "Column 0 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(ralphIndex), "Row " + ralphIndex + " is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(0, ralphIndex), "Cell 0/" + ralphIndex + " is not dirty");
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(0, ralphIndex).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");

        IdIndexIdentifier<Person> identifier = (IdIndexIdentifier<Person>) this.insertHandler.dataChanges.get(key).getKey();
        assertEquals(ralph, identifier.rowObject);
        assertEquals(42, identifier.rowId);

        // only clear no restore, changed data is not reset
        this.dataChangeLayer.clearDataChanges();

        assertEquals(19, this.dataModel.size());
        assertEquals(ralph, this.dataModel.get(18));

        assertTrue(this.dataChangeLayer.dataChanges.isEmpty());
        assertTrue(this.insertHandler.dataChanges.isEmpty());

        assertFalse(this.dataChangeLayer.isDirty(), "State is dirty");
        assertFalse(this.dataChangeLayer.isColumnDirty(0), "Column 0 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(ralphIndex), "Row " + ralphIndex + " is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(0, ralphIndex), "Cell 0/" + ralphIndex + " is dirty");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(0, ralphIndex).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
    }

    @Test
    public void shouldSaveRowInsertEvent() {
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataChangeLayer.doCommand(new RowInsertCommand<>(this.dataChangeLayer, 10, ralph));

        int ralphIndex = this.dataModel.indexOf(ralph);

        // we triggered the command to set at index 10, so that should be the
        // index we get
        assertEquals(10, ralphIndex);

        // trigger save operation
        this.dataChangeLayer.doCommand(new SaveDataChangesCommand());

        // test some states
        assertEquals(19, this.dataModel.size());
        assertEquals(ralph, this.dataModel.get(10));

        assertTrue(this.dataChangeLayer.dataChanges.isEmpty());
        assertTrue(this.insertHandler.dataChanges.isEmpty());

        assertFalse(this.dataChangeLayer.isDirty(), "State is dirty");
        assertFalse(this.dataChangeLayer.isColumnDirty(0), "Column 0 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(ralphIndex), "Row " + ralphIndex + " is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(0, ralphIndex), "Cell 0/" + ralphIndex + " is dirty");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(0, ralphIndex).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
    }

    @Test
    public void shouldDiscardRowInsertEvent() {
        Person prev = this.dataModel.get(10);

        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataChangeLayer.doCommand(new RowInsertCommand<>(this.dataChangeLayer, 10, ralph));

        assertEquals(19, this.dataModel.size());

        int ralphIndex = this.dataModel.indexOf(ralph);

        // we triggered the command to set at index 10, so that should be the
        // index we get
        assertEquals(10, ralphIndex);

        // trigger discard operation
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        // test some states
        assertEquals(18, this.dataModel.size());
        assertEquals(prev, this.dataModel.get(10));

        assertTrue(this.dataChangeLayer.dataChanges.isEmpty());
        assertTrue(this.insertHandler.dataChanges.isEmpty());

        assertFalse(this.dataChangeLayer.isDirty(), "State is dirty");
        assertFalse(this.dataChangeLayer.isColumnDirty(0), "Column 0 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(ralphIndex), "Row " + ralphIndex + " is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(0, ralphIndex), "Cell 0/" + ralphIndex + " is dirty");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(0, ralphIndex).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
    }

    @Test
    public void shouldClearRowInsertOnStructuralChange() {
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataChangeLayer.doCommand(new RowInsertCommand<>(this.dataChangeLayer, 10, ralph));

        int ralphIndex = this.dataModel.indexOf(ralph);

        // perform a full refresh, like changing the underlying data model
        this.dataChangeLayer.doCommand(new StructuralRefreshCommand());

        // test no changes
        assertEquals(19, this.dataModel.size());
        assertEquals(ralph, this.dataModel.get(10));

        assertTrue(this.dataChangeLayer.dataChanges.isEmpty());
        assertTrue(this.insertHandler.dataChanges.isEmpty());

        assertFalse(this.dataChangeLayer.isDirty(), "State is dirty");
        assertFalse(this.dataChangeLayer.isColumnDirty(0), "Column 0 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(ralphIndex), "Row " + ralphIndex + " is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(0, ralphIndex), "Cell 0/" + ralphIndex + " is dirty");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(0, ralphIndex).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
    }

    @Test
    public void shouldDiscardMultipleRowInsertEvent() {
        Person prev = this.dataModel.get(10);

        Person clancy = new Person(40, "Clancy", "Wiggum", Gender.MALE, true, new Date());
        Person sarah = new Person(41, "Sarah", "Wiggum", Gender.FEMALE, true, new Date());
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());

        this.dataChangeLayer.doCommand(new RowInsertCommand<>(this.dataChangeLayer, 10, clancy, sarah, ralph));

        assertEquals(21, this.dataModel.size());

        int clancyIndex = this.dataModel.indexOf(clancy);
        int sarahIndex = this.dataModel.indexOf(sarah);
        int ralphIndex = this.dataModel.indexOf(ralph);

        // we triggered the command to set at index 10, so that should be the
        // index we get
        assertEquals(10, clancyIndex);
        assertEquals(11, sarahIndex);
        assertEquals(12, ralphIndex);

        // trigger discard operation
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        // test some states
        assertEquals(18, this.dataModel.size());
        assertEquals(prev, this.dataModel.get(10));

        assertTrue(this.dataChangeLayer.dataChanges.isEmpty());
        assertTrue(this.insertHandler.dataChanges.isEmpty());

        assertFalse(this.dataChangeLayer.isDirty(), "State is dirty");
        assertFalse(this.dataChangeLayer.isColumnDirty(0), "Column 0 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(ralphIndex), "Row " + ralphIndex + " is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(0, ralphIndex), "Cell 0/" + ralphIndex + " is dirty");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(0, ralphIndex).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
    }

    @Test
    public void shouldDiscardMultipleRowInsertEventAppending() {
        Person clancy = new Person(40, "Clancy", "Wiggum", Gender.MALE, true, new Date());
        Person sarah = new Person(41, "Sarah", "Wiggum", Gender.FEMALE, true, new Date());
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());

        this.dataChangeLayer.doCommand(new RowInsertCommand<>(this.dataChangeLayer, -1, clancy, sarah, ralph));

        assertEquals(21, this.dataModel.size());

        int clancyIndex = this.dataModel.indexOf(clancy);
        int sarahIndex = this.dataModel.indexOf(sarah);
        int ralphIndex = this.dataModel.indexOf(ralph);

        // we triggered the command append, so the indexes should start with 19
        assertEquals(18, clancyIndex);
        assertEquals(19, sarahIndex);
        assertEquals(20, ralphIndex);

        // trigger discard operation
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        // test some states
        assertEquals(18, this.dataModel.size());

        for (Person p : this.dataModel) {
            assertFalse("Wiggum".equals(p.getLastName()), "Wiggum found");
        }

        assertTrue(this.dataChangeLayer.dataChanges.isEmpty());
        assertTrue(this.insertHandler.dataChanges.isEmpty());

        assertFalse(this.dataChangeLayer.isDirty(), "State is dirty");
        assertFalse(this.dataChangeLayer.isColumnDirty(0), "Column 0 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(ralphIndex), "Row " + ralphIndex + " is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(0, ralphIndex), "Cell 0/" + ralphIndex + " is dirty");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(0, ralphIndex).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");

        int lastRowIndex = this.dataModel.size() - 1;
        assertFalse(this.dataChangeLayer.isRowDirty(lastRowIndex), "Row " + lastRowIndex + " is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(0, lastRowIndex), "Cell 0/" + lastRowIndex + " is dirty");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(0, lastRowIndex).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
    }

    @Test
    public void shouldTrackRowDeleteEvent() {
        Person toDelete = this.dataModel.get(10);

        this.dataChangeLayer.doCommand(new RowDeleteCommand(this.dataChangeLayer, 10));

        assertEquals(17, this.dataModel.size());
        assertFalse(this.dataModel.contains(toDelete));

        assertTrue(this.dataChangeLayer.isDirty(), "State is not dirty");

        // row deleted so all columns are actually dirty
        assertTrue(this.dataChangeLayer.isColumnDirty(0), "Column 0 is not dirty");
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertTrue(this.dataChangeLayer.isColumnDirty(2), "Column 2 is not dirty");
        assertTrue(this.dataChangeLayer.isColumnDirty(3), "Column 3 is not dirty");
        assertTrue(this.dataChangeLayer.isColumnDirty(4), "Column 4 is not dirty");

        assertFalse(this.dataChangeLayer.isRowDirty(9), "Row 9 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(10), "Row 10 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(11), "Row 11 is dirty");

        assertFalse(this.dataChangeLayer.isCellDirty(0, 9), "Cell 0/9 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(0, 10), "Cell 0/10 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(0, 11), "Cell 0/11 is dirty");

        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(0, 9).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(2, 11).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");

        assertEquals(1, this.dataChangeLayer.dataChanges.size());
        assertEquals(1, this.deleteHandler.dataChanges.size());

        RowDeleteDataChange change = this.deleteHandler.dataChanges.get(Integer.valueOf(10));
        assertNotNull(change);
        assertEquals(10, change.getKey());
        assertEquals(toDelete, change.getValue());
    }

    @Test
    public void shouldClearRowDeleteWithoutReset() {
        Person toDelete = this.dataModel.get(10);

        this.dataChangeLayer.doCommand(new RowDeleteCommand(this.dataChangeLayer, 10));

        // test some states
        assertEquals(17, this.dataModel.size());
        assertFalse(this.dataModel.contains(toDelete));

        assertEquals(1, this.dataChangeLayer.dataChanges.size());
        assertEquals(1, this.deleteHandler.dataChanges.size());

        assertTrue(this.dataChangeLayer.isDirty(), "State is not dirty");
        assertTrue(this.dataChangeLayer.isColumnDirty(0), "Column 0 is not dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(10), "Row 10 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(0, 10), "Cell 0/10 is dirty");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");

        RowDeleteDataChange change = this.deleteHandler.dataChanges.get(Integer.valueOf(10));
        assertNotNull(change);
        assertEquals(10, change.getKey());
        assertEquals(toDelete, change.getValue());

        // only clear no restore, changed data is not reset
        this.dataChangeLayer.clearDataChanges();

        assertEquals(17, this.dataModel.size());
        assertFalse(this.dataModel.contains(toDelete));

        assertTrue(this.dataChangeLayer.dataChanges.isEmpty());
        assertTrue(this.deleteHandler.dataChanges.isEmpty());

        assertFalse(this.dataChangeLayer.isDirty(), "State is dirty");
        assertFalse(this.dataChangeLayer.isColumnDirty(0), "Column 0 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(10), "Row 10 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(0, 10), "Cell 0/10 is dirty");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
    }

    @Test
    public void shouldSaveRowDeleteEvent() {
        Person toDelete = this.dataModel.get(10);

        this.dataChangeLayer.doCommand(new RowDeleteCommand(this.dataChangeLayer, 10));

        // trigger save operation
        this.dataChangeLayer.doCommand(new SaveDataChangesCommand());

        // test some states
        assertEquals(17, this.dataModel.size());
        assertFalse(this.dataModel.contains(toDelete));

        assertTrue(this.dataChangeLayer.dataChanges.isEmpty());
        assertTrue(this.deleteHandler.dataChanges.isEmpty());

        assertFalse(this.dataChangeLayer.isDirty(), "State is dirty");
        assertFalse(this.dataChangeLayer.isColumnDirty(0), "Column 0 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(10), "Row 10 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(0, 10), "Cell 0/10 is dirty");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
    }

    @Test
    public void shouldDiscardRowDeleteEvent() {
        Person toDelete = this.dataModel.get(10);

        this.dataChangeLayer.doCommand(new RowDeleteCommand(this.dataChangeLayer, 10));

        assertEquals(17, this.dataModel.size());
        assertFalse(this.dataModel.contains(toDelete));

        // trigger discard operation
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        // test some states
        assertEquals(18, this.dataModel.size());
        assertTrue(this.dataModel.contains(toDelete));
        assertEquals(toDelete, this.dataModel.get(10));

        assertTrue(this.dataChangeLayer.dataChanges.isEmpty());
        assertTrue(this.deleteHandler.dataChanges.isEmpty());

        assertTrue(this.dataChangeLayer.dataChanges.isEmpty());
        assertTrue(this.deleteHandler.dataChanges.isEmpty());

        assertFalse(this.dataChangeLayer.isDirty(), "State is dirty");
        assertFalse(this.dataChangeLayer.isColumnDirty(0), "Column 0 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(10), "Row 10 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(0, 10), "Cell 0/10 is dirty");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
    }

    @Test
    public void shouldClearRowDeleteOnStructuralChange() {
        Person toDelete = this.dataModel.get(10);

        this.dataChangeLayer.doCommand(new RowDeleteCommand(this.dataChangeLayer, 10));

        // perform a full refresh, like changing the underlying data model
        this.dataChangeLayer.doCommand(new StructuralRefreshCommand());

        // test no changes
        assertEquals(17, this.dataModel.size());
        assertFalse(this.dataModel.contains(toDelete));

        assertTrue(this.dataChangeLayer.dataChanges.isEmpty());
        assertTrue(this.deleteHandler.dataChanges.isEmpty());

        assertFalse(this.dataChangeLayer.isDirty(), "State is dirty");
        assertFalse(this.dataChangeLayer.isColumnDirty(0), "Column 0 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(10), "Row 10 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(0, 10), "Cell 0/10 is dirty");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
    }

    @Test
    public void shouldDiscardMultipleRowDeleteEvent() {
        Person toDeleteA = this.dataModel.get(10);
        Person toDeleteB = this.dataModel.get(11);
        Person toDeleteC = this.dataModel.get(12);

        this.dataChangeLayer.doCommand(new RowDeleteCommand(this.dataChangeLayer, 10, 11, 12));

        assertEquals(15, this.dataModel.size());
        assertFalse(this.dataModel.contains(toDeleteA));
        assertFalse(this.dataModel.contains(toDeleteB));
        assertFalse(this.dataModel.contains(toDeleteC));

        // trigger discard operation
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        // test some states
        assertEquals(18, this.dataModel.size());
        assertTrue(this.dataModel.contains(toDeleteA));
        assertTrue(this.dataModel.contains(toDeleteB));
        assertTrue(this.dataModel.contains(toDeleteC));
        assertEquals(toDeleteA, this.dataModel.get(10));
        assertEquals(toDeleteB, this.dataModel.get(11));
        assertEquals(toDeleteC, this.dataModel.get(12));

        assertTrue(this.dataChangeLayer.dataChanges.isEmpty());
        assertTrue(this.deleteHandler.dataChanges.isEmpty());

        assertTrue(this.dataChangeLayer.dataChanges.isEmpty());
        assertTrue(this.deleteHandler.dataChanges.isEmpty());

        assertFalse(this.dataChangeLayer.isDirty(), "State is dirty");
        assertFalse(this.dataChangeLayer.isColumnDirty(0), "Column 0 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(10), "Row 10 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(0, 10), "Cell 0/10 is dirty");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
    }

    @Test
    public void shouldDiscardInsertAndDeleteAgain() {
        Person prev = this.dataModel.get(10);

        // insert a row
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataChangeLayer.doCommand(new RowInsertCommand<>(this.dataChangeLayer, 10, ralph));

        assertEquals(19, this.dataModel.size());

        // delete the inserted row again
        this.dataChangeLayer.doCommand(new RowDeleteCommand(this.dataChangeLayer, 10));

        assertEquals(18, this.dataModel.size());
        assertEquals(2, this.dataChangeLayer.dataChanges.size());

        // discard the changes
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals(18, this.dataModel.size());
        assertEquals(prev, this.dataModel.get(10));
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty());
        assertFalse(this.dataChangeLayer.isDirty(), "State is dirty");
    }

    @Test
    public void shouldResetUpdateDeleteOnDiscard() {
        // update the value of an item
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));

        // delete the changed item
        this.dataChangeLayer.doCommand(new RowDeleteCommand(this.dataChangeLayer, 1));

        assertEquals(17, this.dataModel.size());
        assertEquals(2, this.dataChangeLayer.dataChanges.size());

        // discard the changes
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals(18, this.dataModel.size());
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty());
        assertFalse(this.dataChangeLayer.isDirty(), "State is dirty");
    }

    @Test
    public void shouldDiscardInsertAndUpdateInsertedItem() {
        Person prev = this.dataModel.get(10);

        // insert a row
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataChangeLayer.doCommand(new RowInsertCommand<>(this.dataChangeLayer, 10, ralph));

        assertEquals(19, this.dataModel.size());

        // update the inserted row
        assertEquals("Ralph", this.dataLayer.getDataValue(0, 10));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 0, 10, "Clancy"));

        assertEquals("Clancy", this.dataLayer.getDataValue(0, 10));

        // discard the changes
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals(18, this.dataModel.size());
        assertEquals(prev, this.dataModel.get(10));
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty());
        assertFalse(this.dataChangeLayer.isDirty(), "State is dirty");

        // ensure that there is no Clancy or Ralph in an item afterwards
        for (Person p : this.dataModel) {
            assertFalse("Clancy".equals(p.getFirstName()), "Clancy found");
            assertFalse("Ralph".equals(p.getFirstName()), "Ralph found");
        }
    }

    @Test
    public void shouldDiscardUpdateInsert() {
        // update the value of an item
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));
        assertTrue(this.dataChangeLayer.isCellDirty(1, 1), "Cell is not dirty");

        Person prev = this.dataModel.get(0);

        // insert a row on top of the change
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataChangeLayer.doCommand(new RowInsertCommand<>(this.dataChangeLayer, 0, ralph));

        assertEquals(19, this.dataModel.size());

        // change has moved one row
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 2));
        assertTrue(this.dataChangeLayer.isCellDirty(1, 2), "Cell is not dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 1), "Cell is not dirty");
        assertTrue(this.dataChangeLayer.isDirty(), "State is not dirty");

        // discard the changes
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals(18, this.dataModel.size());
        assertEquals(prev, this.dataModel.get(0));
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty());
        assertFalse(this.dataChangeLayer.isDirty(), "State is dirty");

        // ensure that there is no Wiggum and no Lovejoy in an item afterwards
        for (Person p : this.dataModel) {
            assertFalse("Lovejoy".equals(p.getLastName()), "Lovejoy found");
            assertFalse("Wiggum".equals(p.getLastName()), "Wiggum found");
        }
    }

    @Test
    public void shouldDiscardUpdateInsertAndUpdateBack() {
        // update the value of an item
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));
        assertTrue(this.dataChangeLayer.isCellDirty(1, 1), "Cell is not dirty");

        Person prev = this.dataModel.get(0);

        // insert a row on top of the change
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataChangeLayer.doCommand(new RowInsertCommand<>(this.dataChangeLayer, 0, ralph));

        assertEquals(19, this.dataModel.size());

        // change has moved one row
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 2));
        assertTrue(this.dataChangeLayer.isCellDirty(1, 2), "Cell is not dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 1), "Cell is not dirty");
        assertTrue(this.dataChangeLayer.isDirty(), "State is not dirty");

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 2, "Simpson"));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 2));
        assertFalse(this.dataChangeLayer.isCellDirty(1, 2), "Cell is dirty");

        // discard the changes
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals(18, this.dataModel.size());
        assertEquals(prev, this.dataModel.get(0));
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty());
        assertFalse(this.dataChangeLayer.isDirty(), "State is dirty");

        // ensure that there is no Wiggum and no Lovejoy in an item afterwards
        for (Person p : this.dataModel) {
            assertFalse("Lovejoy".equals(p.getLastName()), "Lovejoy found");
            assertFalse("Wiggum".equals(p.getLastName()), "Wiggum found");
        }
    }

    @Test
    public void shouldDiscardUpdateAfterDelete() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 9, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));

        // delete a row before the change
        this.dataChangeLayer.doCommand(new RowDeleteCommand(this.dataChangeLayer, 2));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 8));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataChangeLayer.getDataValueByPosition(1, 9));
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 8).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(8), "Row 8 is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 8), "Cell is not dirty");
        assertTrue(this.dataChangeLayer.isDirty(), "State is not dirty");

        assertFalse(this.updateHandler.changedColumns.isEmpty(), "changed columns are empty");
        assertFalse(this.updateHandler.changedRows.isEmpty(), "changed rows are empty");
        assertFalse(this.updateHandler.dataChanges.isEmpty(), "changes are empty");
        assertFalse(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are empty");

        // now discard and check that previous state is restored correctly
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 9).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(9), "Row 9 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 9), "Cell is dirty");
        assertFalse(this.dataChangeLayer.isDirty(), "State is dirty");

        assertTrue(this.updateHandler.changedColumns.isEmpty(), "changed columns are not empty");
        assertTrue(this.updateHandler.changedRows.isEmpty(), "changed rows are not empty");
        assertTrue(this.updateHandler.dataChanges.isEmpty(), "changes are not empty");
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
    }

    @Test
    public void shouldDiscardInsertAfterDelete() {
        assertEquals(18, this.dataModel.size());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));

        this.dataChangeLayer.doCommand(new RowInsertCommand<>(this.dataChangeLayer, 10, new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date())));

        assertEquals(19, this.dataModel.size());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Wiggum", this.dataLayer.getDataValue(1, 10));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 11));

        // delete a row before the change
        this.dataChangeLayer.doCommand(new RowDeleteCommand(this.dataChangeLayer, 2));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 8));
        assertEquals("Wiggum", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));

        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 9).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(9), "Row 9 is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 9), "Cell is not dirty");
        assertTrue(this.dataChangeLayer.isDirty(), "State is not dirty");

        assertTrue(this.updateHandler.changedColumns.isEmpty(), "changed columns are empty");
        assertTrue(this.updateHandler.changedRows.isEmpty(), "changed rows are empty");
        assertTrue(this.updateHandler.dataChanges.isEmpty(), "changes are empty");
        assertFalse(this.insertHandler.dataChanges.isEmpty(), "changed columns are empty");
        assertFalse(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are empty");

        // now discard and check that previous state is restored correctly
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 9).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(9), "Row 9 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 9), "Cell is dirty");
        assertFalse(this.dataChangeLayer.isDirty(), "State is dirty");

        assertTrue(this.updateHandler.changedColumns.isEmpty(), "changed columns are not empty");
        assertTrue(this.updateHandler.changedRows.isEmpty(), "changed rows are not empty");
        assertTrue(this.updateHandler.dataChanges.isEmpty(), "changes are not empty");
        assertTrue(this.insertHandler.dataChanges.isEmpty(), "changed columns are empty");
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
    }

    @Test
    public void shouldDiscardInsertAfterInsert() {
        assertEquals(18, this.dataModel.size());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));

        this.dataChangeLayer.doCommand(new RowInsertCommand<>(this.dataChangeLayer, 10, new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date())));

        assertEquals(19, this.dataModel.size());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Wiggum", this.dataLayer.getDataValue(1, 10));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 11));

        // insert a row before the change
        this.dataChangeLayer.doCommand(new RowInsertCommand<>(this.dataChangeLayer, 2, new Person(41, "Clancy", "Wiggum", Gender.MALE, false, new Date())));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 10));
        assertEquals("Wiggum", this.dataLayer.getDataValue(1, 11));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 12));

        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 11).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(11), "Row 9 is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 11), "Cell is not dirty");
        assertTrue(this.dataChangeLayer.isDirty(), "State is not dirty");

        assertTrue(this.updateHandler.changedColumns.isEmpty(), "changed columns are empty");
        assertTrue(this.updateHandler.changedRows.isEmpty(), "changed rows are empty");
        assertTrue(this.updateHandler.dataChanges.isEmpty(), "changes are empty");
        assertFalse(this.insertHandler.dataChanges.isEmpty(), "changed columns are empty");
        assertFalse(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are empty");

        // now discard and check that previous state is restored correctly
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 9).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(9), "Row 9 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 9), "Cell is dirty");
        assertFalse(this.dataChangeLayer.isDirty(), "State is dirty");

        assertTrue(this.updateHandler.changedColumns.isEmpty(), "changed columns are not empty");
        assertTrue(this.updateHandler.changedRows.isEmpty(), "changed rows are not empty");
        assertTrue(this.updateHandler.dataChanges.isEmpty(), "changes are not empty");
        assertTrue(this.insertHandler.dataChanges.isEmpty(), "changed columns are empty");
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
    }

    @Test
    public void shouldDiscardUpdateAfterInsert() {
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 9, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));

        // add a row before the change
        this.dataChangeLayer.doCommand(new RowInsertCommand<>(this.dataChangeLayer, 2, new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date())));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 10));
        assertEquals("Lovejoy", this.dataChangeLayer.getDataValueByPosition(1, 10));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 11));
        assertEquals("Flanders", this.dataChangeLayer.getDataValueByPosition(1, 11));
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(10), "Row 1 is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(1, 10), "Cell is not dirty");
        assertTrue(this.dataChangeLayer.isDirty(), "State is not dirty");

        assertFalse(this.updateHandler.changedColumns.isEmpty(), "changed columns are empty");
        assertFalse(this.updateHandler.changedRows.isEmpty(), "changed rows are empty");
        assertFalse(this.updateHandler.dataChanges.isEmpty(), "changes are empty");
        assertFalse(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");

        // now discard and check that previous state is restored correctly
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(1, 9).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(9), "Row 9 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(1, 9), "Cell is dirty");
        assertFalse(this.dataChangeLayer.isDirty(), "State is dirty");

        assertTrue(this.updateHandler.changedColumns.isEmpty(), "changed columns are not empty");
        assertTrue(this.updateHandler.changedRows.isEmpty(), "changed rows are not empty");
        assertTrue(this.updateHandler.dataChanges.isEmpty(), "changes are not empty");
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
    }

    @Test
    public void shouldDiscardDeleteInsertedObject() {
        assertEquals(18, this.dataModel.size());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));

        this.dataChangeLayer.doCommand(new RowInsertCommand<>(this.dataChangeLayer, 10, new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date())));

        assertEquals(19, this.dataModel.size());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Wiggum", this.dataLayer.getDataValue(1, 10));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 11));

        // delete a row before the change
        this.dataChangeLayer.doCommand(new RowDeleteCommand(this.dataChangeLayer, 10));

        assertEquals(18, this.dataModel.size());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));

        assertTrue(this.dataChangeLayer.isDirty(), "State is not dirty");

        // no row dirty
        for (int row = 0; row < this.dataModel.size(); row++) {
            assertFalse(this.dataChangeLayer.isRowDirty(row), "Row " + row + " is dirty");
        }

        assertTrue(this.updateHandler.changedColumns.isEmpty(), "changed columns are empty");
        assertTrue(this.updateHandler.changedRows.isEmpty(), "changed rows are empty");
        assertTrue(this.updateHandler.dataChanges.isEmpty(), "changes are empty");
        assertFalse(this.insertHandler.dataChanges.isEmpty(), "insert handler changes are empty");
        assertFalse(this.deleteHandler.dataChanges.isEmpty(), "delete handler changes are empty");
        assertFalse(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are empty");

        // now discard and check that previous state is restored correctly
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));

        assertFalse(this.dataChangeLayer.isDirty(), "State is dirty");

        // no row dirty
        for (int row = 0; row < this.dataModel.size(); row++) {
            assertFalse(this.dataChangeLayer.isRowDirty(row), "Row " + row + " is dirty");
        }

        assertTrue(this.updateHandler.changedColumns.isEmpty(), "changed columns are not empty");
        assertTrue(this.updateHandler.changedRows.isEmpty(), "changed rows are not empty");
        assertTrue(this.updateHandler.dataChanges.isEmpty(), "changes are not empty");
        assertTrue(this.insertHandler.dataChanges.isEmpty(), "insert handler changes are not empty");
        assertTrue(this.deleteHandler.dataChanges.isEmpty(), "delete handler changes are not empty");
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty(), "tracked changes are not empty");
    }

}
