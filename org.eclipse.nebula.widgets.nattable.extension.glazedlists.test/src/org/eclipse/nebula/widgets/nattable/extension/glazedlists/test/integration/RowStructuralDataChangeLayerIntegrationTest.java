/*******************************************************************************
 * Copyright (c) 2018, 2022 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.test.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.nebula.widgets.nattable.command.DisposeResourcesCommand;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.command.RowDeleteCommandHandler;
import org.eclipse.nebula.widgets.nattable.data.command.RowInsertCommand;
import org.eclipse.nebula.widgets.nattable.data.command.RowObjectDeleteCommandHandler;
import org.eclipse.nebula.widgets.nattable.datachange.DataChangeHandler;
import org.eclipse.nebula.widgets.nattable.datachange.DataChangeLayer;
import org.eclipse.nebula.widgets.nattable.datachange.IdIndexIdentifier;
import org.eclipse.nebula.widgets.nattable.datachange.IdIndexKeyHandler;
import org.eclipse.nebula.widgets.nattable.datachange.RowDeleteDataChangeHandler;
import org.eclipse.nebula.widgets.nattable.datachange.RowInsertDataChangeHandler;
import org.eclipse.nebula.widgets.nattable.datachange.command.DiscardDataChangesCommand;
import org.eclipse.nebula.widgets.nattable.datachange.command.KeyRowInsertCommandHandler;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;

@SuppressWarnings("unchecked")
public class RowStructuralDataChangeLayerIntegrationTest {

    private List<Person> dataModel;
    private EventList<Person> eventList;
    private SortedList<Person> sortedList;
    private FilterList<Person> filterList;
    private IRowDataProvider<Person> dataProvider;
    private DataLayer dataLayer;
    private DataChangeLayer dataChangeLayer;
    private RowInsertDataChangeHandler insertHandler;
    private RowDeleteDataChangeHandler deleteHandler;

    private CountDownLatch lock = new CountDownLatch(1);

    @BeforeEach
    public void setup() {
        this.dataModel = PersonService.getFixedPersons();
        this.eventList = GlazedLists.eventList(this.dataModel);
        this.sortedList = new SortedList<>(this.eventList, null);
        this.filterList = new FilterList<>(this.sortedList);

        IColumnPropertyAccessor<Person> columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<>(new String[] {
                "firstName",
                "lastName",
                "gender",
                "married",
                "birthday" });

        IRowIdAccessor<Person> rowIdAccessor = Person::getId;

        this.dataProvider = new ListDataProvider<>(
                this.filterList,
                columnPropertyAccessor);
        this.dataLayer = new DataLayer(this.dataProvider);

        // register the RowDeleteCommandHandler for delete operations by
        // index, e.g. used for reverting row insert operations
        this.dataLayer.registerCommandHandler(new RowDeleteCommandHandler<>(this.eventList));
        // register the RowObjectDeleteCommandHandler for delete operations
        // by object, e.g. delete by UI interaction
        this.dataLayer.registerCommandHandler(new RowObjectDeleteCommandHandler<>(this.eventList));
        // register the KeyRowInsertCommandHandler to be able to revert key
        // insert operations by firing KeyRowInsertEvents
        // uses an IdIndexKeyHandler with an alternative ListDataProvider on the
        // base list in order to be able to discard the change on the base list
        this.dataLayer.registerCommandHandler(
                new KeyRowInsertCommandHandler<>(
                        this.eventList,
                        new IdIndexKeyHandler<>(new ListDataProvider<>(this.eventList, columnPropertyAccessor), rowIdAccessor)));

        GlazedListsEventLayer<Person> glazedListsEventLayer =
                new GlazedListsEventLayer<>(this.dataLayer, this.filterList);
        glazedListsEventLayer.setTestMode(true);

        this.dataChangeLayer = new DataChangeLayer(glazedListsEventLayer,
                new IdIndexKeyHandler<>(
                        this.dataProvider,
                        rowIdAccessor),
                // configure persistence mode for data changes
                false,
                // enable tracking of row structural changes
                true);

        this.dataChangeLayer.addLayerListener(event -> {
            if (event instanceof RowStructuralRefreshEvent) {
                RowStructuralDataChangeLayerIntegrationTest.this.lock.countDown();
            }
        });

        for (DataChangeHandler h : this.dataChangeLayer.getDataChangeHandler()) {
            if (h instanceof RowInsertDataChangeHandler) {
                this.insertHandler = (RowInsertDataChangeHandler) h;
            } else if (h instanceof RowDeleteDataChangeHandler) {
                this.deleteHandler = (RowDeleteDataChangeHandler) h;
            }
        }
        assertNotNull(this.insertHandler, "RowInsertDataChangeHandler not found");
        assertNotNull(this.deleteHandler, "RowDeleteDataChangeHandler not found");

    }

    @AfterEach
    public void tearDown() {
        this.dataChangeLayer.doCommand(new DisposeResourcesCommand());
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
    public void shouldTrackRowInsert() {
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataChangeLayer.doCommand(new RowInsertCommand<>(ralph));

        int ralphIndex = this.eventList.indexOf(ralph);
        Object key = this.insertHandler.getKeyHandler().getKey(-1, ralphIndex);

        assertEquals(19, this.eventList.size());

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

        assertEquals(1, this.dataChangeLayer.getDataChanges().size());
        assertEquals(1, this.insertHandler.getDataChanges().size());
        // we use the IdIndexKeyHandler so casting is fine
        IdIndexIdentifier<Person> identifier = (IdIndexIdentifier<Person>) this.insertHandler.getDataChanges().get(key).getKey();
        assertEquals(ralph, identifier.rowObject);
        assertEquals(42, identifier.rowId);
    }

    @Test
    public void shouldTrackRowInsertOnSort() {
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataChangeLayer.doCommand(new RowInsertCommand<>(ralph));

        assertEquals(18, this.sortedList.indexOf(ralph));
        assertEquals(19, this.sortedList.size());

        // trigger DESC sorting
        this.sortedList.setComparator(Comparator.comparing(Person::getLastName, (p1, p2) -> {
            return p2.compareTo(p1);
        }));

        int ralphIndex = this.sortedList.indexOf(ralph);
        Object key = this.insertHandler.getKeyHandler().getKey(-1, ralphIndex);

        assertEquals(0, ralphIndex);
        assertTrue(this.dataChangeLayer.isColumnDirty(0), "Column 0 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(ralphIndex), "Row " + ralphIndex + " is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(0, ralphIndex), "Cell 0/" + ralphIndex + " is not dirty");
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(0, ralphIndex).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");

        assertEquals(1, this.dataChangeLayer.getDataChanges().size());
        assertEquals(1, this.insertHandler.getDataChanges().size());
        // we use the IdIndexKeyHandler so casting is fine
        IdIndexIdentifier<Person> identifier = (IdIndexIdentifier<Person>) this.insertHandler.getDataChanges().get(key).getKey();
        assertEquals(ralph, identifier.rowObject);
        assertEquals(42, identifier.rowId);
    }

    @Test
    public void shouldTrackRowInsertInSortedState() {
        assertEquals(18, this.sortedList.size());

        // trigger DESC sorting
        this.sortedList.setComparator(Comparator.comparing(Person::getLastName, (p1, p2) -> {
            return p2.compareTo(p1);
        }));

        // insert while the list is sorted
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataChangeLayer.doCommand(new RowInsertCommand<>(ralph));

        assertEquals(19, this.sortedList.size());
        int ralphIndex = this.sortedList.indexOf(ralph);
        Object key = this.insertHandler.getKeyHandler().getKey(-1, ralphIndex);

        assertEquals(0, ralphIndex);
        assertTrue(this.dataChangeLayer.isColumnDirty(0), "Column 0 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(ralphIndex), "Row " + ralphIndex + " is not dirty");
        assertTrue(this.dataChangeLayer.isCellDirty(0, ralphIndex), "Cell 0/" + ralphIndex + " is not dirty");
        assertTrue(this.dataChangeLayer.getConfigLabelsByPosition(0, ralphIndex).hasLabel(DataChangeLayer.DIRTY), "Dirty label not set");

        assertEquals(1, this.dataChangeLayer.getDataChanges().size());
        assertEquals(1, this.insertHandler.getDataChanges().size());
        // we use the IdIndexKeyHandler so casting is fine
        IdIndexIdentifier<Person> identifier = (IdIndexIdentifier<Person>) this.insertHandler.getDataChanges().get(key).getKey();
        assertEquals(ralph, identifier.rowObject);
        assertEquals(42, identifier.rowId);
    }

    @Test
    public void shouldDiscardRowInsertInSortedState() {
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataChangeLayer.doCommand(new RowInsertCommand<>(ralph));

        assertEquals(18, this.sortedList.indexOf(ralph));
        assertEquals(19, this.sortedList.size());

        // trigger DESC sorting
        this.sortedList.setComparator(Comparator.comparing(Person::getLastName, (p1, p2) -> {
            return p2.compareTo(p1);
        }));

        assertEquals(0, this.sortedList.indexOf(ralph));

        // discard the change
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals(18, this.sortedList.size());

        assertTrue(this.dataChangeLayer.getDataChanges().isEmpty());
        assertTrue(this.insertHandler.getDataChanges().isEmpty());

        // check for states on row 0
        assertFalse(this.dataChangeLayer.isColumnDirty(0), "Column 0 is dirty");
        assertFalse(this.dataChangeLayer.isRowDirty(0), "Row 0 is dirty");
        assertFalse(this.dataChangeLayer.isCellDirty(0, 0), "Cell 0/0 is dirty");
        assertFalse(this.dataChangeLayer.getConfigLabelsByPosition(0, 0).hasLabel(DataChangeLayer.DIRTY), "Dirty label set");

        assertFalse(this.sortedList.contains(ralph), "Ralph is still here");
    }

    @Test
    public void shouldDiscardRowInsertOnChangeSortedState() {
        // trigger DESC sorting
        this.sortedList.setComparator(Comparator.comparing(Person::getLastName, (p1, p2) -> {
            return p2.compareTo(p1);
        }));

        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataChangeLayer.doCommand(new RowInsertCommand<>(ralph));

        assertEquals(0, this.sortedList.indexOf(ralph));
        assertEquals(19, this.sortedList.size());

        this.sortedList.setComparator(null);

        assertEquals(18, this.sortedList.indexOf(ralph));

        // discard the change
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals(18, this.sortedList.size());

        assertTrue(this.dataChangeLayer.getDataChanges().isEmpty());
        assertTrue(this.insertHandler.getDataChanges().isEmpty());

        // check that no row is dirty
        for (int row = 0; row < this.sortedList.size(); row++) {
            assertFalse(this.dataChangeLayer.isRowDirty(row), "Row " + row + " is dirty");
        }

        assertFalse(this.sortedList.contains(ralph), "Ralph is still here");
    }

    @Test
    public void shouldDiscardMultipleRowInsertOnChangeSortedStateAppending() {
        // trigger DESC sorting
        this.sortedList.setComparator(Comparator.comparing(Person::getLastName, (p1, p2) -> {
            return p2.compareTo(p1);
        }));

        Person clancy = new Person(40, "Clancy", "Wiggum", Gender.MALE, true, new Date());
        Person sarah = new Person(41, "Sarah", "Wiggum", Gender.FEMALE, true, new Date());
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataChangeLayer.doCommand(new RowInsertCommand<>(this.dataChangeLayer, -1, clancy, sarah, ralph));

        assertEquals(0, this.sortedList.indexOf(clancy));
        assertEquals(1, this.sortedList.indexOf(sarah));
        assertEquals(2, this.sortedList.indexOf(ralph));
        assertEquals(21, this.sortedList.size());

        this.sortedList.setComparator(null);

        assertEquals(18, this.sortedList.indexOf(clancy));
        assertEquals(19, this.sortedList.indexOf(sarah));
        assertEquals(20, this.sortedList.indexOf(ralph));

        assertTrue(this.dataChangeLayer.isRowDirty(18), "Row 18 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(19), "Row 19 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(20), "Row 20 is not dirty");

        // discard the change
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals(18, this.sortedList.size());

        assertTrue(this.dataChangeLayer.getDataChanges().isEmpty());
        assertTrue(this.insertHandler.getDataChanges().isEmpty());

        // check that no row is dirty
        for (int row = 0; row < this.sortedList.size(); row++) {
            assertFalse(this.dataChangeLayer.isRowDirty(row), "Row " + row + " is dirty");
        }

        assertFalse(this.sortedList.contains(clancy), "Clancy is still here");
        assertFalse(this.sortedList.contains(sarah), "Sarah is still here");
        assertFalse(this.sortedList.contains(ralph), "Ralph is still here");
    }

    @Test
    public void shouldDiscardMultipleRowInsertOnChangeSortedState() {
        // trigger DESC sorting
        this.sortedList.setComparator(Comparator.comparing(Person::getLastName, (p1, p2) -> {
            return p2.compareTo(p1);
        }));

        Person clancy = new Person(40, "Clancy", "Wiggum", Gender.MALE, true, new Date());
        Person sarah = new Person(41, "Sarah", "Wiggum", Gender.FEMALE, true, new Date());
        Person ralph = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        this.dataChangeLayer.doCommand(new RowInsertCommand<>(this.dataChangeLayer, 10, clancy, sarah, ralph));

        assertEquals(0, this.sortedList.indexOf(clancy));
        assertEquals(1, this.sortedList.indexOf(sarah));
        assertEquals(2, this.sortedList.indexOf(ralph));
        assertEquals(21, this.sortedList.size());

        this.sortedList.setComparator(null);

        assertEquals(10, this.sortedList.indexOf(clancy));
        assertEquals(11, this.sortedList.indexOf(sarah));
        assertEquals(12, this.sortedList.indexOf(ralph));

        assertTrue(this.dataChangeLayer.isRowDirty(10), "Row 10 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(11), "Row 11 is not dirty");
        assertTrue(this.dataChangeLayer.isRowDirty(12), "Row 12 is not dirty");

        // discard the change
        this.dataChangeLayer.doCommand(new DiscardDataChangesCommand());

        assertEquals(18, this.sortedList.size());

        assertTrue(this.dataChangeLayer.getDataChanges().isEmpty());
        assertTrue(this.insertHandler.getDataChanges().isEmpty());

        // check that no row is dirty
        for (int row = 0; row < this.sortedList.size(); row++) {
            assertFalse(this.dataChangeLayer.isRowDirty(row), "Row " + row + " is dirty");
        }

        assertFalse(this.sortedList.contains(clancy), "Clancy is still here");
        assertFalse(this.sortedList.contains(sarah), "Sarah is still here");
        assertFalse(this.sortedList.contains(ralph), "Ralph is still here");
    }

    @Test
    public void shouldKeepChangeOnFilter() throws InterruptedException {
        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        this.filterList.setMatcher(item -> item.getLastName().equals("Simpson"));

        // give the GlazedListsEventLayer some time to trigger the
        // RowStructuralRefreshEvent
        boolean completed = this.lock.await(1000, TimeUnit.MILLISECONDS);

        assertTrue(completed, "Timeout - no event received");

        assertEquals(9, this.filterList.size());
        assertFalse(this.dataChangeLayer.getDataChanges().isEmpty());
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");

        this.lock = new CountDownLatch(1);
        this.filterList.setMatcher(null);

        // give the GlazedListsEventLayer some time to trigger the
        // RowStructuralRefreshEvent
        completed = this.lock.await(1000, TimeUnit.MILLISECONDS);

        assertTrue(completed, "Timeout - no event received");

        assertEquals(18, this.filterList.size());
        assertFalse(this.dataChangeLayer.getDataChanges().isEmpty());
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
    }

    @Test
    public void shouldNotThrowAnExceptionOnResize() throws InterruptedException {
        this.dataChangeLayer.doCommand(new UpdateDataCommand(this.dataChangeLayer, 1, 1, "Lovejoy"));

        this.filterList.setMatcher(item -> item.getLastName().equals("Simpson"));

        this.dataLayer.setColumnWidthByPosition(2, 75);

        assertEquals(9, this.filterList.size());
        assertFalse(this.dataChangeLayer.getDataChanges().isEmpty());
        assertFalse(this.dataChangeLayer.isColumnDirty(1), "Column 1 is dirty");

        this.filterList.setMatcher(null);

        // give the GlazedListsEventLayer some time to trigger the
        // RowStructuralRefreshEvent
        boolean completed = this.lock.await(1000, TimeUnit.MILLISECONDS);

        assertTrue(completed, "Timeout - no event received");

        assertEquals(18, this.filterList.size());
        assertFalse(this.dataChangeLayer.getDataChanges().isEmpty());
        assertTrue(this.dataChangeLayer.isColumnDirty(1), "Column 1 is not dirty");
    }
}
