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
package org.eclipse.nebula.widgets.nattable.datachange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class RowStructuralDataChangeIdIndexTest {

    private List<Person> dataModel;
    private ListDataProvider<Person> dataProvider;
    private DataLayer dataLayer;
    private DataChangeLayer dataChangeLayer;
    private PersistenceUpdateDataChangeHandler updateHandler;
    private RowInsertDataChangeHandler insertHandler;
    private RowDeleteDataChangeHandler deleteHandler;

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
        assertNotNull("RowInsertDataChangeHandler not found", this.insertHandler);
        assertNotNull("RowDeleteDataChangeHandler not found", this.deleteHandler);
        assertNotNull("PersistenceUpdateDataChangeHandler not found", this.updateHandler);
    }

    @Test
    public void shouldTrackRowInsertEvent() {
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataChangeLayer.doCommand(new RowInsertCommand<>(ralph));

        int ralphIndex = this.dataModel.indexOf(ralph);
        Object key = this.insertHandler.keyHandler.getKey(-1, ralphIndex);

        assertEquals(19, this.dataModel.size());

        // row added so all columns are actually dirty
        assertTrue("Column 0 is not dirty", this.dataChangeLayer.isColumnDirty(0));
        assertTrue("Column 1 is not dirty", this.dataChangeLayer.isColumnDirty(1));
        assertTrue("Column 2 is not dirty", this.dataChangeLayer.isColumnDirty(2));
        assertTrue("Column 3 is not dirty", this.dataChangeLayer.isColumnDirty(3));
        assertTrue("Column 4 is not dirty", this.dataChangeLayer.isColumnDirty(4));

        assertTrue("Row " + ralphIndex + " is not dirty", this.dataChangeLayer.isRowDirty(ralphIndex));

        assertTrue("Cell 0/" + ralphIndex + " is not dirty", this.dataChangeLayer.isCellDirty(0, ralphIndex));
        assertTrue("Cell 1/" + ralphIndex + " is not dirty", this.dataChangeLayer.isCellDirty(1, ralphIndex));
        assertTrue("Cell 2/" + ralphIndex + " is not dirty", this.dataChangeLayer.isCellDirty(2, ralphIndex));
        assertTrue("Cell 3/" + ralphIndex + " is not dirty", this.dataChangeLayer.isCellDirty(3, ralphIndex));
        assertTrue("Cell 4/" + ralphIndex + " is not dirty", this.dataChangeLayer.isCellDirty(4, ralphIndex));

        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(0, ralphIndex).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, ralphIndex).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(2, ralphIndex).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(3, ralphIndex).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(4, ralphIndex).hasLabel(DataChangeLayer.DIRTY));

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

        assertTrue("Column 0 is not dirty", this.dataChangeLayer.isColumnDirty(0));
        assertTrue("Row " + ralphIndex + " is not dirty", this.dataChangeLayer.isRowDirty(ralphIndex));
        assertTrue("Cell 0/" + ralphIndex + " is not dirty", this.dataChangeLayer.isCellDirty(0, ralphIndex));
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(0, ralphIndex).hasLabel(DataChangeLayer.DIRTY));

        IdIndexIdentifier<Person> identifier = (IdIndexIdentifier<Person>) this.insertHandler.dataChanges.get(key).getKey();
        assertEquals(ralph, identifier.rowObject);
        assertEquals(42, identifier.rowId);

        // only clear no restore, changed data is not reset
        this.dataChangeLayer.clearDataChanges();

        assertEquals(19, this.dataModel.size());
        assertEquals(ralph, this.dataModel.get(18));

        assertTrue(this.dataChangeLayer.dataChanges.isEmpty());
        assertTrue(this.insertHandler.dataChanges.isEmpty());

        assertFalse("Column 0 is dirty", this.dataChangeLayer.isColumnDirty(0));
        assertFalse("Row " + ralphIndex + " is dirty", this.dataChangeLayer.isRowDirty(ralphIndex));
        assertFalse("Cell 0/" + ralphIndex + " is dirty", this.dataChangeLayer.isCellDirty(0, ralphIndex));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(0, ralphIndex).hasLabel(DataChangeLayer.DIRTY));
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

        assertFalse("Column 0 is dirty", this.dataChangeLayer.isColumnDirty(0));
        assertFalse("Row " + ralphIndex + " is dirty", this.dataChangeLayer.isRowDirty(ralphIndex));
        assertFalse("Cell 0/" + ralphIndex + " is dirty", this.dataChangeLayer.isCellDirty(0, ralphIndex));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(0, ralphIndex).hasLabel(DataChangeLayer.DIRTY));
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

        assertFalse("Column 0 is dirty", this.dataChangeLayer.isColumnDirty(0));
        assertFalse("Row " + ralphIndex + " is dirty", this.dataChangeLayer.isRowDirty(ralphIndex));
        assertFalse("Cell 0/" + ralphIndex + " is dirty", this.dataChangeLayer.isCellDirty(0, ralphIndex));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(0, ralphIndex).hasLabel(DataChangeLayer.DIRTY));
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

        assertFalse("Column 0 is dirty", this.dataChangeLayer.isColumnDirty(0));
        assertFalse("Row " + ralphIndex + " is dirty", this.dataChangeLayer.isRowDirty(ralphIndex));
        assertFalse("Cell 0/" + ralphIndex + " is dirty", this.dataChangeLayer.isCellDirty(0, ralphIndex));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(0, ralphIndex).hasLabel(DataChangeLayer.DIRTY));
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

        assertFalse("Column 0 is dirty", this.dataChangeLayer.isColumnDirty(0));
        assertFalse("Row " + ralphIndex + " is dirty", this.dataChangeLayer.isRowDirty(ralphIndex));
        assertFalse("Cell 0/" + ralphIndex + " is dirty", this.dataChangeLayer.isCellDirty(0, ralphIndex));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(0, ralphIndex).hasLabel(DataChangeLayer.DIRTY));
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
            assertFalse("Wiggum found", "Wiggum".equals(p.getLastName()));
        }

        assertTrue(this.dataChangeLayer.dataChanges.isEmpty());
        assertTrue(this.insertHandler.dataChanges.isEmpty());

        assertFalse("Column 0 is dirty", this.dataChangeLayer.isColumnDirty(0));
        assertFalse("Row " + ralphIndex + " is dirty", this.dataChangeLayer.isRowDirty(ralphIndex));
        assertFalse("Cell 0/" + ralphIndex + " is dirty", this.dataChangeLayer.isCellDirty(0, ralphIndex));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(0, ralphIndex).hasLabel(DataChangeLayer.DIRTY));

        int lastRowIndex = this.dataModel.size() - 1;
        assertFalse("Row " + lastRowIndex + " is dirty", this.dataChangeLayer.isRowDirty(lastRowIndex));
        assertFalse("Cell 0/" + lastRowIndex + " is dirty", this.dataChangeLayer.isCellDirty(0, lastRowIndex));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(0, lastRowIndex).hasLabel(DataChangeLayer.DIRTY));
    }

    @Test
    public void shouldTrackRowDeleteEvent() {
        Person toDelete = this.dataModel.get(10);

        this.dataChangeLayer.doCommand(new RowDeleteCommand(this.dataChangeLayer, 10));

        assertEquals(17, this.dataModel.size());
        assertFalse(this.dataModel.contains(toDelete));

        // row deleted so all columns are actually dirty
        assertTrue("Column 0 is not dirty", this.dataChangeLayer.isColumnDirty(0));
        assertTrue("Column 1 is not dirty", this.dataChangeLayer.isColumnDirty(1));
        assertTrue("Column 2 is not dirty", this.dataChangeLayer.isColumnDirty(2));
        assertTrue("Column 3 is not dirty", this.dataChangeLayer.isColumnDirty(3));
        assertTrue("Column 4 is not dirty", this.dataChangeLayer.isColumnDirty(4));

        assertFalse("Row 9 is dirty", this.dataChangeLayer.isRowDirty(9));
        assertFalse("Row 10 is dirty", this.dataChangeLayer.isRowDirty(10));
        assertFalse("Row 11 is dirty", this.dataChangeLayer.isRowDirty(11));

        assertFalse("Cell 0/9 is dirty", this.dataChangeLayer.isCellDirty(0, 9));
        assertFalse("Cell 0/10 is dirty", this.dataChangeLayer.isCellDirty(0, 10));
        assertFalse("Cell 0/11 is dirty", this.dataChangeLayer.isCellDirty(0, 11));

        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(0, 9).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(2, 11).hasLabel(DataChangeLayer.DIRTY));

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

        assertTrue("Column 0 is not dirty", this.dataChangeLayer.isColumnDirty(0));
        assertFalse("Row 10 is dirty", this.dataChangeLayer.isRowDirty(10));
        assertFalse("Cell 0/10 is dirty", this.dataChangeLayer.isCellDirty(0, 10));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY));

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

        assertFalse("Column 0 is dirty", this.dataChangeLayer.isColumnDirty(0));
        assertFalse("Row 10 is dirty", this.dataChangeLayer.isRowDirty(10));
        assertFalse("Cell 0/10 is dirty", this.dataChangeLayer.isCellDirty(0, 10));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY));
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

        assertFalse("Column 0 is dirty", this.dataChangeLayer.isColumnDirty(0));
        assertFalse("Row 10 is dirty", this.dataChangeLayer.isRowDirty(10));
        assertFalse("Cell 0/10 is dirty", this.dataChangeLayer.isCellDirty(0, 10));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY));
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

        assertFalse("Column 0 is dirty", this.dataChangeLayer.isColumnDirty(0));
        assertFalse("Row 10 is dirty", this.dataChangeLayer.isRowDirty(10));
        assertFalse("Cell 0/10 is dirty", this.dataChangeLayer.isCellDirty(0, 10));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY));
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

        assertFalse("Column 0 is dirty", this.dataChangeLayer.isColumnDirty(0));
        assertFalse("Row 10 is dirty", this.dataChangeLayer.isRowDirty(10));
        assertFalse("Cell 0/10 is dirty", this.dataChangeLayer.isCellDirty(0, 10));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY));
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

        assertFalse("Column 0 is dirty", this.dataChangeLayer.isColumnDirty(0));
        assertFalse("Row 10 is dirty", this.dataChangeLayer.isRowDirty(10));
        assertFalse("Cell 0/10 is dirty", this.dataChangeLayer.isCellDirty(0, 10));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY));
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

        // ensure that there is no Clancy or Ralph in an item afterwards
        for (Person p : this.dataModel) {
            assertFalse("Clancy found", "Clancy".equals(p.getFirstName()));
            assertFalse("Ralph found", "Ralph".equals(p.getFirstName()));
        }
    }

    @Test
    public void shouldDiscardUpdateInsert() {
        // update the value of an item
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 1));

        Person prev = this.dataModel.get(0);

        // insert a row on top of the change
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataChangeLayer.doCommand(new RowInsertCommand<>(this.dataChangeLayer, 0, ralph));

        assertEquals(19, this.dataModel.size());

        // change has moved one row
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 2));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 2));
        assertFalse("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 1));

        // discard the changes
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals(18, this.dataModel.size());
        assertEquals(prev, this.dataModel.get(0));
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty());

        // ensure that there is no Wiggum and no Lovejoy in an item afterwards
        for (Person p : this.dataModel) {
            assertFalse("Lovejoy found", "Lovejoy".equals(p.getLastName()));
            assertFalse("Wiggum found", "Wiggum".equals(p.getLastName()));
        }
    }

    @Test
    public void shouldDiscardUpdateInsertAndUpdateBack() {
        // update the value of an item
        assertEquals("Simpson", this.dataLayer.getDataValue(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 1));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 1));

        Person prev = this.dataModel.get(0);

        // insert a row on top of the change
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataChangeLayer.doCommand(new RowInsertCommand<>(this.dataChangeLayer, 0, ralph));

        assertEquals(19, this.dataModel.size());

        // change has moved one row
        assertEquals("Lovejoy", this.dataLayer.getDataValue(1, 2));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 2));
        assertFalse("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 1));

        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 2, "Simpson"));

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 2));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 2));

        // discard the changes
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals(18, this.dataModel.size());
        assertEquals(prev, this.dataModel.get(0));
        assertTrue(this.dataChangeLayer.dataChanges.isEmpty());

        // ensure that there is no Wiggum and no Lovejoy in an item afterwards
        for (Person p : this.dataModel) {
            assertFalse("Lovejoy found", "Lovejoy".equals(p.getLastName()));
            assertFalse("Wiggum found", "Wiggum".equals(p.getLastName()));
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
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 8).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Column 1 is not dirty", this.dataChangeLayer.isColumnDirty(1));
        assertTrue("Row 8 is not dirty", this.dataChangeLayer.isRowDirty(8));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 8));

        assertFalse("changed columns are empty", this.updateHandler.changedColumns.isEmpty());
        assertFalse("changed rows are empty", this.updateHandler.changedRows.isEmpty());
        assertFalse("changes are empty", this.updateHandler.dataChanges.isEmpty());
        assertFalse("tracked changes are empty", this.dataChangeLayer.dataChanges.isEmpty());

        // now discard and check that previous state is restored correctly
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 9).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 9 is dirty", this.dataChangeLayer.isRowDirty(9));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 9));

        assertTrue("changed columns are not empty", this.updateHandler.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.updateHandler.changedRows.isEmpty());
        assertTrue("changes are not empty", this.updateHandler.dataChanges.isEmpty());
        assertTrue("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
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

        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 9).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Column 1 is not dirty", this.dataChangeLayer.isColumnDirty(1));
        assertTrue("Row 9 is not dirty", this.dataChangeLayer.isRowDirty(9));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 9));

        assertTrue("changed columns are empty", this.updateHandler.changedColumns.isEmpty());
        assertTrue("changed rows are empty", this.updateHandler.changedRows.isEmpty());
        assertTrue("changes are empty", this.updateHandler.dataChanges.isEmpty());
        assertFalse("changed columns are empty", this.insertHandler.dataChanges.isEmpty());
        assertFalse("tracked changes are empty", this.dataChangeLayer.dataChanges.isEmpty());

        // now discard and check that previous state is restored correctly
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 9).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 9 is dirty", this.dataChangeLayer.isRowDirty(9));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 9));

        assertTrue("changed columns are not empty", this.updateHandler.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.updateHandler.changedRows.isEmpty());
        assertTrue("changes are not empty", this.updateHandler.dataChanges.isEmpty());
        assertTrue("changed columns are empty", this.insertHandler.dataChanges.isEmpty());
        assertTrue("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
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

        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 11).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Column 1 is not dirty", this.dataChangeLayer.isColumnDirty(1));
        assertTrue("Row 9 is not dirty", this.dataChangeLayer.isRowDirty(11));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 11));

        assertTrue("changed columns are empty", this.updateHandler.changedColumns.isEmpty());
        assertTrue("changed rows are empty", this.updateHandler.changedRows.isEmpty());
        assertTrue("changes are empty", this.updateHandler.dataChanges.isEmpty());
        assertFalse("changed columns are empty", this.insertHandler.dataChanges.isEmpty());
        assertFalse("tracked changes are empty", this.dataChangeLayer.dataChanges.isEmpty());

        // now discard and check that previous state is restored correctly
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 9).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 9 is dirty", this.dataChangeLayer.isRowDirty(9));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 9));

        assertTrue("changed columns are not empty", this.updateHandler.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.updateHandler.changedRows.isEmpty());
        assertTrue("changes are not empty", this.updateHandler.dataChanges.isEmpty());
        assertTrue("changed columns are empty", this.insertHandler.dataChanges.isEmpty());
        assertTrue("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
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
        assertTrue("Dirty label not set", this.dataChangeLayer.getConfigLabelsByPosition(1, 10).hasLabel(DataChangeLayer.DIRTY));
        assertTrue("Column 1 is not dirty", this.dataChangeLayer.isColumnDirty(1));
        assertTrue("Row 1 is not dirty", this.dataChangeLayer.isRowDirty(10));
        assertTrue("Cell is not dirty", this.dataChangeLayer.isCellDirty(1, 10));

        assertFalse("changed columns are empty", this.updateHandler.changedColumns.isEmpty());
        assertFalse("changed rows are empty", this.updateHandler.changedRows.isEmpty());
        assertFalse("changes are empty", this.updateHandler.dataChanges.isEmpty());
        assertFalse("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());

        // now discard and check that previous state is restored correctly
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));
        assertFalse("Dirty label set", this.dataChangeLayer.getConfigLabelsByPosition(1, 9).hasLabel(DataChangeLayer.DIRTY));
        assertFalse("Column 1 is dirty", this.dataChangeLayer.isColumnDirty(1));
        assertFalse("Row 9 is dirty", this.dataChangeLayer.isRowDirty(9));
        assertFalse("Cell is dirty", this.dataChangeLayer.isCellDirty(1, 9));

        assertTrue("changed columns are not empty", this.updateHandler.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.updateHandler.changedRows.isEmpty());
        assertTrue("changes are not empty", this.updateHandler.dataChanges.isEmpty());
        assertTrue("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
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

        // no row dirty
        for (int row = 0; row < this.dataModel.size(); row++) {
            assertFalse("Row " + row + " is dirty", this.dataChangeLayer.isRowDirty(row));
        }

        assertTrue("changed columns are empty", this.updateHandler.changedColumns.isEmpty());
        assertTrue("changed rows are empty", this.updateHandler.changedRows.isEmpty());
        assertTrue("changes are empty", this.updateHandler.dataChanges.isEmpty());
        assertFalse("insert handler changes are empty", this.insertHandler.dataChanges.isEmpty());
        assertFalse("delete handler changes are empty", this.deleteHandler.dataChanges.isEmpty());
        assertFalse("tracked changes are empty", this.dataChangeLayer.dataChanges.isEmpty());

        // now discard and check that previous state is restored correctly
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals("Simpson", this.dataLayer.getDataValue(1, 9));
        assertEquals("Flanders", this.dataLayer.getDataValue(1, 10));

        // no row dirty
        for (int row = 0; row < this.dataModel.size(); row++) {
            assertFalse("Row " + row + " is dirty", this.dataChangeLayer.isRowDirty(row));
        }

        assertTrue("changed columns are not empty", this.updateHandler.changedColumns.isEmpty());
        assertTrue("changed rows are not empty", this.updateHandler.changedRows.isEmpty());
        assertTrue("changes are not empty", this.updateHandler.dataChanges.isEmpty());
        assertTrue("insert handler changes are not empty", this.insertHandler.dataChanges.isEmpty());
        assertTrue("delete handler changes are not empty", this.deleteHandler.dataChanges.isEmpty());
        assertTrue("tracked changes are not empty", this.dataChangeLayer.dataChanges.isEmpty());
    }

}
