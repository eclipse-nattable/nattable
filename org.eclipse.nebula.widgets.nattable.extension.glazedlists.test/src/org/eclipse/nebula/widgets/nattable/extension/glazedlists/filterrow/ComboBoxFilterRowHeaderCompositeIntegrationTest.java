/*******************************************************************************
 * Copyright (c) 2019, 2022 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.nebula.widgets.nattable.command.DisposeResourcesCommand;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.person.Address;
import org.eclipse.nebula.widgets.nattable.dataset.person.ExtendedPersonWithAddress;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByConfigLabelModifier;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByDataLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByHeaderLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByModel;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowComboUpdateEvent;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.IFilterRowComboUpdateListener;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ClearAllFiltersCommand;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.DefaultSortConfiguration;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;

/**
 * This test class is intended to verify the integration of the
 * {@link ComboBoxFilterRowHeaderComposite}. Especially the updates of the
 * filter combobox contents in case of structural changes. As those updates are
 * done in background threads to not blocking the UI, the tests are complicated.
 * To be sure that everything works as intended, the cases can be replayed with
 * a UI via the _813_SortableGroupByWithComboBoxFilterExample.
 */
public class ComboBoxFilterRowHeaderCompositeIntegrationTest {

    private static ArrayList<ExtendedPersonWithAddress> values = new ArrayList<>();

    private BodyLayerStack<ExtendedPersonWithAddress> bodyLayer;
    private ComboBoxFilterRowHeaderComposite<ExtendedPersonWithAddress> filterRowHeaderLayer;
    private NatTableFixture natTable;

    private GlazedListsSortModel<ExtendedPersonWithAddress> sortModel;

    @BeforeAll
    public static void setupClass() {
        for (int i = 0; i < 300; i++) {
            values.addAll(createValues(i * 30));
        }
    }

    @BeforeEach
    public void setup() {
        // create a new ConfigRegistry which will be needed for GlazedLists
        // handling
        ConfigRegistry configRegistry = new ConfigRegistry();

        // property names of the ExtendedPersonWithAddress class
        String[] propertyNames = { "firstName", "lastName", "gender",
                "married", "address.street", "address.housenumber",
                "address.postalCode", "address.city", "age", "birthday",
                "money", "description", "favouriteFood", "favouriteDrinks" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("firstName", "Firstname");
        propertyToLabelMap.put("lastName", "Lastname");
        propertyToLabelMap.put("gender", "Gender");
        propertyToLabelMap.put("married", "Married");
        propertyToLabelMap.put("address.street", "Street");
        propertyToLabelMap.put("address.housenumber", "Housenumber");
        propertyToLabelMap.put("address.postalCode", "Postalcode");
        propertyToLabelMap.put("address.city", "City");
        propertyToLabelMap.put("age", "Age");
        propertyToLabelMap.put("birthday", "Birthday");
        propertyToLabelMap.put("money", "Money");
        propertyToLabelMap.put("description", "Description");
        propertyToLabelMap.put("favouriteFood", "Food");
        propertyToLabelMap.put("favouriteDrinks", "Drinks");

        final IColumnPropertyAccessor<ExtendedPersonWithAddress> columnPropertyAccessor =
                new ExtendedReflectiveColumnPropertyAccessor<>(propertyNames);

        // to enable the group by summary feature, the GroupByDataLayer needs to
        // know the ConfigRegistry
        this.bodyLayer =
                new BodyLayerStack<>(
                        values,
                        columnPropertyAccessor,
                        configRegistry);

        this.bodyLayer.getBodyDataLayer().setConfigLabelAccumulator(new ColumnLabelAccumulator());

        // build the column header layer
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ILayer columnHeaderLayer =
                new ColumnHeaderLayer(columnHeaderDataLayer, this.bodyLayer, this.bodyLayer.getSelectionLayer());

        // add sorting
        this.sortModel = new GlazedListsSortModel<>(
                this.bodyLayer.getSortedList(),
                columnPropertyAccessor,
                configRegistry,
                columnHeaderDataLayer);
        SortHeaderLayer<ExtendedPersonWithAddress> sortHeaderLayer = new SortHeaderLayer<>(
                columnHeaderLayer,
                this.sortModel,
                false);

        // connect sortModel to GroupByDataLayer to support sorting by group by
        // summary values
        this.bodyLayer.getBodyDataLayer().initializeTreeComparator(
                sortHeaderLayer.getSortModel(),
                this.bodyLayer.getTreeLayer(),
                true);

        this.filterRowHeaderLayer =
                new ComboBoxFilterRowHeaderComposite<>(
                        this.bodyLayer.getFilterList(),
                        this.bodyLayer.getGlazedListsEventLayer(),
                        this.bodyLayer.getSortedList(),
                        columnPropertyAccessor,
                        sortHeaderLayer,
                        columnHeaderDataProvider,
                        configRegistry);

        // build the row header layer
        IDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(this.bodyLayer.getBodyDataProvider());
        DataLayer rowHeaderDataLayer =
                new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        ILayer rowHeaderLayer =
                new RowHeaderLayer(rowHeaderDataLayer, this.bodyLayer, this.bodyLayer.getSelectionLayer());

        // build the corner layer
        IDataProvider cornerDataProvider =
                new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
        DataLayer cornerDataLayer =
                new DataLayer(cornerDataProvider);
        ILayer cornerLayer =
                new CornerLayer(cornerDataLayer, rowHeaderLayer, this.filterRowHeaderLayer);

        // build the grid layer
        GridLayer gridLayer = new GridLayer(this.bodyLayer, this.filterRowHeaderLayer, rowHeaderLayer, cornerLayer);

        // set the group by header on top of the grid
        CompositeLayer compositeGridLayer = new CompositeLayer(1, 2);
        final GroupByHeaderLayer groupByHeaderLayer =
                new GroupByHeaderLayer(this.bodyLayer.getGroupByModel(), gridLayer, columnHeaderDataProvider);
        compositeGridLayer.setChildLayer(GroupByHeaderLayer.GROUP_BY_REGION, groupByHeaderLayer, 0, 0);
        compositeGridLayer.setChildLayer("Grid", gridLayer, 0, 1);

        // turn the auto configuration off as we want to add our header menu
        // configuration
        this.natTable = new NatTableFixture(compositeGridLayer, false);
        this.natTable.setConfigRegistry(configRegistry);
        this.natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        this.natTable.addConfiguration(new DefaultSortConfiguration());
        this.natTable.configure();

        // test that nothing is filtered
        assertEquals(9000, this.bodyLayer.getFilterList().size());
    }

    @AfterEach
    public void tearDown() {
        this.natTable.doCommand(new ClearAllFiltersCommand());
        this.natTable.doCommand(new DisposeResourcesCommand());
    }

    @Test
    public void shouldFilterForSimpsons() {
        // load the possible values first to simulate same behavior as in UI,
        // otherwise exceptions might occur
        this.filterRowHeaderLayer.comboBoxDataProvider.getValues(1, 0);
        // filter
        this.natTable.doCommand(new UpdateDataCommand(this.natTable, 2, 2, new ArrayList<>(Arrays.asList("Simpson"))));
        assertEquals(4500, this.bodyLayer.getFilterList().size());
    }

    @Test
    public void shouldFilterForMaleSimpsons() {
        // load the possible values first to simulate same behavior as in UI,
        // otherwise exceptions might occur
        this.filterRowHeaderLayer.comboBoxDataProvider.getValues(0, 0);
        this.filterRowHeaderLayer.comboBoxDataProvider.getValues(1, 0);

        // filter
        List<String> firstNames = new ArrayList<>(Arrays.asList("Homer", "Bart"));
        List<String> lastNames = new ArrayList<>(Arrays.asList("Simpson"));
        this.natTable.doCommand(new UpdateDataCommand(this.natTable, 1, 2, firstNames));
        this.natTable.doCommand(new UpdateDataCommand(this.natTable, 2, 2, lastNames));
        assertEquals(2400, this.bodyLayer.getFilterList().size());
    }

    @Test
    public void shouldFilterOnLoadPersistedState() {
        ((GlazedListsFilterRowComboBoxDataProvider<ExtendedPersonWithAddress>) this.filterRowHeaderLayer.getComboBoxDataProvider()).deactivate();

        // load the possible values first to simulate same behavior as in UI,
        // otherwise exceptions might occur
        this.filterRowHeaderLayer.comboBoxDataProvider.getValues(0, 0);

        // filter
        List<String> firstNames = new ArrayList<>(Arrays.asList("Homer", "Bart"));
        this.natTable.doCommand(new UpdateDataCommand(this.natTable, 1, 2, firstNames));
        assertEquals(2400, this.bodyLayer.getFilterList().size());

        // persist
        Properties properties = new Properties();
        this.natTable.saveState("filtered", properties);

        this.natTable.doCommand(new ClearAllFiltersCommand());
        // test that nothing is filtered
        assertEquals(9000, this.bodyLayer.getFilterList().size());

        // load saved state
        this.natTable.loadState("filtered", properties);
        assertEquals(2400, this.bodyLayer.getFilterList().size());
    }

    // TODO should not remove filter on sort
    // seems the sort operation is not finished in time so the test fails
    @Disabled
    @Test
    public void shouldKeepFilterOnSort() throws InterruptedException {
        shouldFilterForMaleSimpsons();

        // change a value to check that sorting was applied
        ExtendedPersonWithAddress person = this.bodyLayer.filterList.get(this.bodyLayer.filterList.size() - 1);
        person.getAddress().setCity("Al Bundy Street");

        // sort by street (column 4)
        // natTable.doCommand(new SortColumnCommand(natTable, 5));
        // natTable.doCommand(new SortColumnCommand(natTable, 5));
        this.sortModel.sort(4, SortDirectionEnum.ASC, false);

        Thread.sleep(500);

        Object street = this.natTable.getDataValueByPosition(5, 3);
        assertEquals("Al Bundy Street", street);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void shouldKeepFilterOnEdit() throws InterruptedException {
        shouldFilterForMaleSimpsons();

        Object columnOneFilter = this.filterRowHeaderLayer.filterRowDataLayer.getDataValue(0, 0);
        assertTrue(columnOneFilter instanceof List);
        List filter = (List) columnOneFilter;
        assertEquals(2, filter.size());
        assertTrue(filter.contains("Homer"));
        assertTrue(filter.contains("Bart"));

        ComboUpdateListener listener = new ComboUpdateListener();
        CountDownLatch countDown = new CountDownLatch(1);
        listener.setCountDown(countDown);
        this.filterRowHeaderLayer.comboBoxDataProvider.addCacheUpdateListener(listener);

        // edit one entry
        this.natTable.doCommand(new UpdateDataCommand(this.natTable, 1, 3, "Bort"));

        boolean completed = countDown.await(2000, TimeUnit.MILLISECONDS);

        assertTrue(completed, "Timeout - no event received");

        assertEquals(1, listener.getEventsCount());

        FilterRowComboUpdateEvent evt = listener.getReceivedEvents().get(0);
        assertEquals(0, evt.getColumnIndex());
        assertEquals(1, evt.getAddedItems().size());
        assertEquals("Bort", evt.getAddedItems().iterator().next());

        assertEquals("Bort", this.natTable.getDataValueByPosition(1, 3));

        columnOneFilter = this.filterRowHeaderLayer.filterRowDataLayer.getDataValue(0, 0);
        assertTrue(columnOneFilter instanceof List);
        assertEquals(3, filter.size());
        assertTrue(filter.contains("Homer"));
        assertTrue(filter.contains("Bart"));
        assertTrue(filter.contains("Bort"));

        listener.clearReceivedEvents();

        // edit back
        countDown = new CountDownLatch(1);
        listener.setCountDown(countDown);

        this.natTable.doCommand(new UpdateDataCommand(this.natTable, 1, 3, "Bart"));

        completed = countDown.await(2000, TimeUnit.MILLISECONDS);

        assertTrue(completed, "Timeout - no event received");

        assertEquals(1, listener.getEventsCount());

        evt = listener.getReceivedEvents().get(0);
        assertEquals(0, evt.getColumnIndex());
        assertEquals(0, evt.getAddedItems().size());
        assertEquals(1, evt.getRemovedItems().size());
        assertEquals("Bort", evt.getRemovedItems().iterator().next());

        assertEquals("Bart", this.natTable.getDataValueByPosition(1, 3));

        columnOneFilter = this.filterRowHeaderLayer.filterRowDataLayer.getDataValue(0, 0);
        assertTrue(columnOneFilter instanceof List);
        assertEquals(2, filter.size());
        assertTrue(filter.contains("Homer"));
        assertTrue(filter.contains("Bart"));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void shouldKeepFilterOnStructuralChanges() throws InterruptedException {
        shouldFilterForMaleSimpsons();

        // add an entry
        Person person = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        Address address = new Address();
        address.setStreet("Some Street");
        address.setHousenumber(42);
        address.setPostalCode(12345);
        address.setCity("In the clouds");

        // we need to wait here as the listChanged handling is triggered with a
        // delay of 100 ms to avoid too frequent calculations
        ComboUpdateListener listener = new ComboUpdateListener();
        CountDownLatch countDown = new CountDownLatch(1);
        listener.setCountDown(countDown);
        this.filterRowHeaderLayer.comboBoxDataProvider.addCacheUpdateListener(listener);

        ExtendedPersonWithAddress entry = new ExtendedPersonWithAddress(person, address,
                "0000", "The little Ralphy", 0,
                new ArrayList<String>(), new ArrayList<String>());
        this.bodyLayer.eventList.add(entry);

        // long start = System.currentTimeMillis();
        boolean completed = countDown.await(10000, TimeUnit.MILLISECONDS);
        // long end = System.currentTimeMillis();
        // System.out.println("duration " + (end - start));

        assertTrue(completed, "Timeout - no event received");

        assertEquals(1, listener.getEventsCount());

        FilterRowComboUpdateEvent evt = listener.getReceivedEvents().get(0);
        assertEquals(2, evt.updateContentSize());

        assertEquals(0, evt.getColumnIndex(0));
        assertEquals(1, evt.getAddedItems(0).size());
        assertEquals("Ralph", evt.getAddedItems(0).iterator().next());

        assertEquals(1, evt.getColumnIndex(1));
        assertEquals(1, evt.getAddedItems(1).size());
        assertEquals("Wiggum", evt.getAddedItems(1).iterator().next());

        // test that still all filters are set
        Object columnOneFilter = this.filterRowHeaderLayer.filterRowDataLayer.getDataValue(0, 0);
        assertTrue(columnOneFilter instanceof List);
        List filter = (List) columnOneFilter;
        assertEquals(3, filter.size());
        assertTrue(filter.contains("Homer"));
        assertTrue(filter.contains("Bart"));
        assertTrue(filter.contains("Ralph"));

        Object columnTwoFilter = this.filterRowHeaderLayer.filterRowDataLayer.getDataValue(1, 0);
        assertTrue(columnTwoFilter instanceof List);
        filter = (List) columnTwoFilter;
        assertEquals(2, filter.size());
        assertTrue(filter.contains("Simpson"));
        assertTrue(filter.contains("Wiggum"));

        this.bodyLayer.eventList.remove(entry);

        countDown = new CountDownLatch(1);
        listener.setCountDown(countDown);

        // start = System.currentTimeMillis();
        completed = countDown.await(10000, TimeUnit.MILLISECONDS);
        // end = System.currentTimeMillis();
        // System.out.println("duration " + (end - start));

        assertTrue(completed, "Timeout - no event received");

        this.filterRowHeaderLayer.comboBoxDataProvider.removeCacheUpdateListener(listener);
    }

    private static List<ExtendedPersonWithAddress> createValues(int startId) {
        return createPersons(startId).stream()
                .map(p -> new ExtendedPersonWithAddress(
                        p,
                        PersonService.createAddress(),
                        PersonService.generateSimplePassword(),
                        PersonService.createRandomLengthText(),
                        PersonService.createRandomMoneyAmount(),
                        null,
                        null))
                .collect(Collectors.toList());
    }

    private static List<Person> createPersons(int startId) {
        List<Person> result = new ArrayList<>();

        result.add(new Person(startId + 1, "Homer", "Simpson", Gender.MALE, true, new Date()));
        result.add(new Person(startId + 2, "Homer", "Simpson", Gender.MALE, true, new Date()));
        result.add(new Person(startId + 3, "Marge", "Simpson", Gender.FEMALE, true, new Date()));
        result.add(new Person(startId + 4, "Marge", "Simpson", Gender.FEMALE, true, new Date()));
        result.add(new Person(startId + 5, "Marge", "Simpson", Gender.FEMALE, true, new Date(), null));
        result.add(new Person(startId + 6, "Ned", null, Gender.MALE, true, new Date()));
        result.add(new Person(startId + 7, "Maude", null, Gender.FEMALE, true, new Date()));

        result.add(new Person(startId + 8, "Homer", "Simpson", Gender.MALE, true, new Date()));
        result.add(new Person(startId + 9, "Homer", "Simpson", Gender.MALE, true, new Date()));
        result.add(new Person(startId + 10, "Homer", "Simpson", Gender.MALE, true, new Date()));
        result.add(new Person(startId + 11, "Bart", "Simpson", Gender.MALE, false, new Date()));
        result.add(new Person(startId + 12, "Bart", "Simpson", Gender.MALE, false, new Date()));
        result.add(new Person(startId + 13, "Bart", "Simpson", Gender.MALE, false, new Date()));
        result.add(new Person(startId + 14, "Marge", "Simpson", Gender.FEMALE, true, new Date()));
        result.add(new Person(startId + 15, "Marge", "Simpson", Gender.FEMALE, true, new Date()));
        result.add(new Person(startId + 16, "Lisa", "Simpson", Gender.FEMALE, false, new Date()));
        result.add(new Person(startId + 17, "Lisa", "Simpson", Gender.FEMALE, false, new Date()));

        result.add(new Person(startId + 18, "Ned", "Flanders", Gender.MALE, true, new Date()));
        result.add(new Person(startId + 19, "Ned", "Flanders", Gender.MALE, true, new Date()));
        result.add(new Person(startId + 20, "Maude", "Flanders", Gender.FEMALE, true, new Date()));
        result.add(new Person(startId + 21, "Maude", "Flanders", Gender.FEMALE, true, new Date()));
        result.add(new Person(startId + 22, "Rod", "Flanders", Gender.MALE, false, new Date()));
        result.add(new Person(startId + 23, "Rod", "Flanders", Gender.MALE, false, new Date()));
        result.add(new Person(startId + 24, "Tod", "Flanders", Gender.MALE, false, new Date()));
        result.add(new Person(startId + 25, "Tod", "Flanders", Gender.MALE, false, new Date()));

        result.add(new Person(startId + 26, "Lenny", "Leonard", Gender.MALE, false, new Date()));
        result.add(new Person(startId + 27, "Lenny", "Leonard", Gender.MALE, false, new Date()));

        result.add(new Person(startId + 28, "Carl", "Carlson", Gender.MALE, false, new Date()));
        result.add(new Person(startId + 29, "Carl", "Carlson", Gender.MALE, false, new Date()));

        result.add(new Person(startId + 30, "Timothy", "Lovejoy", Gender.MALE, false, new Date()));
        return result;

    }

    static class BodyLayerStack<T> extends AbstractLayerTransform {

        private final EventList<T> eventList;
        private final SortedList<T> sortedList;
        private final FilterList<T> filterList;

        private final IDataProvider bodyDataProvider;

        private final GroupByDataLayer<T> bodyDataLayer;

        private final SelectionLayer selectionLayer;

        private final TreeLayer treeLayer;

        private final GroupByModel groupByModel = new GroupByModel();

        private final GlazedListsEventLayer<T> glazedListsEventLayer;

        public BodyLayerStack(List<T> values,
                IColumnPropertyAccessor<T> columnPropertyAccessor,
                ConfigRegistry configRegistry) {
            // wrapping of the list to show into GlazedLists
            // see http://publicobject.com/glazedlists/ for further information
            this.eventList = GlazedLists.eventList(values);
            TransformedList<T, T> rowObjectsGlazedList = GlazedLists.threadSafeList(this.eventList);

            // use the SortedList constructor with 'null' for the Comparator
            // because the Comparator
            // will be set by configuration
            this.sortedList = new SortedList<>(rowObjectsGlazedList, null);
            // wrap the SortedList with the FilterList
            this.filterList = new FilterList<>(this.sortedList);

            // Use the GroupByDataLayer instead of the default DataLayer
            this.bodyDataLayer = new GroupByDataLayer<>(
                    getGroupByModel(),
                    this.filterList,
                    columnPropertyAccessor,
                    configRegistry);
            // get the IDataProvider that was created by the GroupByDataLayer
            this.bodyDataProvider = this.bodyDataLayer.getDataProvider();

            // layer for event handling of GlazedLists and PropertyChanges
            this.glazedListsEventLayer =
                    new GlazedListsEventLayer<>(this.bodyDataLayer, this.filterList);
            this.glazedListsEventLayer.setTestMode(true);

            ColumnReorderLayer columnReorderLayer =
                    new ColumnReorderLayer(this.glazedListsEventLayer);
            ColumnHideShowLayer columnHideShowLayer =
                    new ColumnHideShowLayer(columnReorderLayer);
            this.selectionLayer =
                    new SelectionLayer(columnHideShowLayer);

            // add a tree layer to visualise the grouping
            this.treeLayer = new TreeLayer(this.selectionLayer, this.bodyDataLayer.getTreeRowModel());

            ViewportLayer viewportLayer = new ViewportLayer(this.treeLayer);

            // this will avoid tree specific rendering regarding alignment and
            // indentation in case no grouping is active
            viewportLayer.setConfigLabelAccumulator(new GroupByConfigLabelModifier(getGroupByModel()));

            setUnderlyingLayer(viewportLayer);
        }

        public ILayer getGlazedListsEventLayer() {
            return this.glazedListsEventLayer;
        }

        public TreeLayer getTreeLayer() {
            return this.treeLayer;
        }

        public SelectionLayer getSelectionLayer() {
            return this.selectionLayer;
        }

        public EventList<T> getEventList() {
            return this.eventList;
        }

        public SortedList<T> getSortedList() {
            return this.sortedList;
        }

        public FilterList<T> getFilterList() {
            return this.filterList;
        }

        public IDataProvider getBodyDataProvider() {
            return this.bodyDataProvider;
        }

        public GroupByDataLayer<T> getBodyDataLayer() {
            return this.bodyDataLayer;
        }

        public GroupByModel getGroupByModel() {
            return this.groupByModel;
        }
    }

    static class ComboUpdateListener implements IFilterRowComboUpdateListener {

        // Received events are kept in order
        private final List<FilterRowComboUpdateEvent> receivedEvents = new LinkedList<>();

        private CountDownLatch countDownLatch;

        public void setCountDown(CountDownLatch countDown) {
            this.countDownLatch = countDown;
        }

        @Override
        public void handleEvent(FilterRowComboUpdateEvent event) {
            this.receivedEvents.add(event);
            if (this.countDownLatch != null) {
                this.countDownLatch.countDown();
            }
        }

        public List<FilterRowComboUpdateEvent> getReceivedEvents() {
            return this.receivedEvents;
        }

        public void clearReceivedEvents() {
            this.receivedEvents.clear();
        }

        public int getEventsCount() {
            return this.receivedEvents.size();
        }

    }
}
