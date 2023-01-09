/*******************************************************************************
 * Copyright (c) 2023 Dirk Fauth.
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.nebula.widgets.nattable.command.DisposeResourcesCommand;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultIntegerDisplayConverter;
import org.eclipse.nebula.widgets.nattable.dataset.person.Address;
import org.eclipse.nebula.widgets.nattable.dataset.person.DataModelConstants;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonWithAddress;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.EditConstants;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowPainter;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowTextCellEditor;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.ComboBoxFilterUtils;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowComboUpdateEvent;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.IFilterRowComboUpdateListener;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ClearAllFiltersCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ClearFilterCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes;
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
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.NoScalingDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommand;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.DefaultSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
public class MixedComboBoxFilterRowHeaderIntegrationTest {

    private BodyLayerStack<PersonWithAddress> bodyLayer;
    private FilterRowComboBoxDataProvider<PersonWithAddress> filterRowComboBoxDataProvider;
    private ComboBoxFilterRowHeaderComposite<PersonWithAddress> filterRowHeaderLayer;
    private NatTableFixture natTable;

    private GlazedListsSortModel<PersonWithAddress> sortModel;

    @BeforeEach
    public void setup() {
        // create a new ConfigRegistry which will be needed for GlazedLists
        // handling
        ConfigRegistry configRegistry = new ConfigRegistry();

        // property names of the PersonWithAddress class
        String[] propertyNames = {
                "firstName",
                "lastName",
                "gender",
                "married",
                "birthday",
                "address.street",
                "address.housenumber",
                "address.postalCode",
                "address.city" };

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("firstName", "Firstname");
        propertyToLabelMap.put("lastName", "Lastname");
        propertyToLabelMap.put("gender", "Gender");
        propertyToLabelMap.put("married", "Married");
        propertyToLabelMap.put("birthday", "Birthday");
        propertyToLabelMap.put("address.street", "Street");
        propertyToLabelMap.put("address.housenumber", "Housenumber");
        propertyToLabelMap.put("address.postalCode", "Postal Code");
        propertyToLabelMap.put("address.city", "City");

        final IColumnPropertyAccessor<PersonWithAddress> columnPropertyAccessor =
                new ExtendedReflectiveColumnPropertyAccessor<>(propertyNames);

        List<PersonWithAddress> values = createPersons(0);

        // to enable the group by summary feature, the GroupByDataLayer needs to
        // know the ConfigRegistry
        this.bodyLayer =
                new BodyLayerStack<>(
                        values,
                        columnPropertyAccessor,
                        configRegistry);

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
        SortHeaderLayer<PersonWithAddress> sortHeaderLayer = new SortHeaderLayer<>(
                columnHeaderLayer,
                this.sortModel,
                false);

        // Create a customized GlazedListsFilterRowComboBoxDataProvider that
        // distincts the empty string and null from the collected values. This
        // way null and "" entries in the collection are treated the same way
        // and there is only a single "empty" entry in the dropdown.
        this.filterRowComboBoxDataProvider =
                new GlazedListsFilterRowComboBoxDataProvider<>(
                        this.bodyLayer.getGlazedListsEventLayer(),
                        this.bodyLayer.getSortedList(),
                        columnPropertyAccessor);
        this.filterRowComboBoxDataProvider.setDistinctNullAndEmpty(true);

        // create the ComboBoxFilterRowHeaderComposite
        this.filterRowHeaderLayer =
                new ComboBoxFilterRowHeaderComposite<>(
                        new ComboBoxGlazedListsFilterStrategy<>(
                                this.filterRowComboBoxDataProvider,
                                this.bodyLayer.getFilterList(),
                                columnPropertyAccessor,
                                configRegistry),
                        this.filterRowComboBoxDataProvider,
                        sortHeaderLayer,
                        columnHeaderDataProvider,
                        configRegistry,
                        true);

        // configure using the FilterList as base for combobox content
        this.filterRowComboBoxDataProvider.setFilterCollection(this.bodyLayer.getFilterList(), this.filterRowHeaderLayer);

        // configure different editors to simulate mixed filter row
        this.filterRowHeaderLayer.addConfiguration(new AbstractRegistryConfiguration() {

            @Override
            public void configureRegistry(IConfigRegistry configRegistry) {
                // register a FilterRowTextCellEditor for Firstname
                configRegistry.registerConfigAttribute(
                        EditConfigAttributes.CELL_EDITOR,
                        new FilterRowTextCellEditor(),
                        DisplayMode.NORMAL,
                        FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                                + DataModelConstants.FIRSTNAME_COLUMN_POSITION);
                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        new PaddingDecorator(new FilterRowPainter(), 0, 0, 0, 5),
                        DisplayMode.NORMAL,
                        FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                                + DataModelConstants.FIRSTNAME_COLUMN_POSITION);
                configRegistry.registerConfigAttribute(
                        FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER,
                        new DefaultDisplayConverter(),
                        DisplayMode.NORMAL,
                        FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                                + DataModelConstants.FIRSTNAME_COLUMN_POSITION);

                // register a ComboBoxCellEditor with fixed values for Married
                configRegistry.registerConfigAttribute(
                        EditConfigAttributes.CELL_EDITOR,
                        new ComboBoxCellEditor(Arrays.asList(Boolean.TRUE, Boolean.FALSE)),
                        DisplayMode.NORMAL,
                        FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                                + DataModelConstants.MARRIED_COLUMN_POSITION);
                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        new PaddingDecorator(new FilterRowPainter(), 0, 0, 0, 5),
                        DisplayMode.NORMAL,
                        FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                                + DataModelConstants.MARRIED_COLUMN_POSITION);
                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.DISPLAY_CONVERTER,
                        new DefaultDisplayConverter(),
                        DisplayMode.NORMAL,
                        FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                                + DataModelConstants.MARRIED_COLUMN_POSITION);

                // register a TextCellEditor for Housenumber
                configRegistry.registerConfigAttribute(
                        EditConfigAttributes.CELL_EDITOR,
                        new TextCellEditor(),
                        DisplayMode.NORMAL,
                        FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                                + DataModelConstants.HOUSENUMBER_COLUMN_POSITION);
                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        new PaddingDecorator(new FilterRowPainter(), 0, 0, 0, 5),
                        DisplayMode.NORMAL,
                        FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                                + DataModelConstants.HOUSENUMBER_COLUMN_POSITION);
                configRegistry.registerConfigAttribute(
                        FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER,
                        new DefaultIntegerDisplayConverter(),
                        DisplayMode.NORMAL,
                        FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX
                                + DataModelConstants.HOUSENUMBER_COLUMN_POSITION);
            }
        });

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

        // turn the auto configuration off as we want to add our header menu
        // configuration
        this.natTable = new NatTableFixture(gridLayer, 1000, 400, false);
        this.natTable.setConfigRegistry(configRegistry);
        this.natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        this.natTable.addConfiguration(new DefaultSortConfiguration());
        this.natTable.configure();

        this.natTable.doCommand(new ConfigureScalingCommand(new NoScalingDpiConverter()));

        // test that nothing is filtered
        assertEquals(30, this.bodyLayer.getFilterList().size());
    }

    @AfterEach
    public void tearDown() {
        // clear should get back to initial values
        this.natTable.doCommand(new ClearAllFiltersCommand());

        List<?> lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        List<?> streets = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.STREET_COLUMN_POSITION, 0);
        List<?> cities = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.CITY_COLUMN_POSITION, 0);

        assertTrue(ObjectUtils.collectionsEqual(LASTNAMES, lastnames), "not all values in collection");
        assertTrue(ObjectUtils.collectionsEqual(STREETS, streets), "not all values in collection");
        assertTrue(ObjectUtils.collectionsEqual(CITIES, cities), "not all values in collection");

        this.natTable.doCommand(new DisposeResourcesCommand());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void shouldReturnNoFilterDataInitially(boolean caching) {
        this.filterRowComboBoxDataProvider.setCachingEnabled(caching);

        // null for non-combobox filters
        // SELECT_ALL for combobox-filters

        assertNull(this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.FIRSTNAME_COLUMN_POSITION, 1));
        assertEquals(
                EditConstants.SELECT_ALL_ITEMS_VALUE,
                this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.LASTNAME_COLUMN_POSITION, 1));
        assertEquals(
                EditConstants.SELECT_ALL_ITEMS_VALUE,
                this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.GENDER_COLUMN_POSITION, 1));
        assertNull(this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.MARRIED_COLUMN_POSITION, 1));
        assertEquals(
                EditConstants.SELECT_ALL_ITEMS_VALUE,
                this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.BIRTHDAY_COLUMN_POSITION, 1));
        assertEquals(
                EditConstants.SELECT_ALL_ITEMS_VALUE,
                this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.STREET_COLUMN_POSITION, 1));
        assertNull(this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.HOUSENUMBER_COLUMN_POSITION, 1));
        assertEquals(
                EditConstants.SELECT_ALL_ITEMS_VALUE,
                this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.POSTALCODE_COLUMN_POSITION, 1));
        assertEquals(
                EditConstants.SELECT_ALL_ITEMS_VALUE,
                this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.CITY_COLUMN_POSITION, 1));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void shouldSeeAllComboEntriesSelectedInitially(boolean caching) {
        this.filterRowComboBoxDataProvider.setCachingEnabled(caching);

        assertTrue(
                ComboBoxFilterUtils.isAllSelected(
                        DataModelConstants.LASTNAME_COLUMN_POSITION,
                        this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.LASTNAME_COLUMN_POSITION, 1),
                        this.filterRowComboBoxDataProvider),
                "not all values selected");
        assertTrue(
                ComboBoxFilterUtils.isAllSelected(
                        DataModelConstants.BIRTHDAY_COLUMN_POSITION,
                        this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.BIRTHDAY_COLUMN_POSITION, 1),
                        this.filterRowComboBoxDataProvider),
                "not all values selected");
        assertTrue(
                ComboBoxFilterUtils.isAllSelected(
                        DataModelConstants.STREET_COLUMN_POSITION,
                        this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.STREET_COLUMN_POSITION, 1),
                        this.filterRowComboBoxDataProvider),
                "not all values selected");
        assertTrue(
                ComboBoxFilterUtils.isAllSelected(
                        DataModelConstants.POSTALCODE_COLUMN_POSITION,
                        this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.POSTALCODE_COLUMN_POSITION, 1),
                        this.filterRowComboBoxDataProvider),
                "not all values selected");
        assertTrue(
                ComboBoxFilterUtils.isAllSelected(
                        DataModelConstants.CITY_COLUMN_POSITION,
                        this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.CITY_COLUMN_POSITION, 1),
                        this.filterRowComboBoxDataProvider),
                "not all values selected");
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void shouldCheckForFilterComboBoxCellEditor(boolean caching) {
        this.filterRowComboBoxDataProvider.setCachingEnabled(caching);

        assertFalse(
                ComboBoxFilterUtils.isFilterRowComboBoxCellEditor(
                        this.natTable.getConfigRegistry(),
                        DataModelConstants.FIRSTNAME_COLUMN_POSITION),
                "Identified FilterRowComboBoxCellEditor although another editor is configured");
        assertTrue(
                ComboBoxFilterUtils.isFilterRowComboBoxCellEditor(
                        this.natTable.getConfigRegistry(),
                        DataModelConstants.LASTNAME_COLUMN_POSITION),
                "Did not identify a FilterRowComboBoxCellEditor");
        assertTrue(
                ComboBoxFilterUtils.isFilterRowComboBoxCellEditor(
                        this.natTable.getConfigRegistry(),
                        DataModelConstants.GENDER_COLUMN_POSITION),
                "Did not identify a FilterRowComboBoxCellEditor");
        assertFalse(
                ComboBoxFilterUtils.isFilterRowComboBoxCellEditor(
                        this.natTable.getConfigRegistry(),
                        DataModelConstants.MARRIED_COLUMN_POSITION),
                "Identified FilterRowComboBoxCellEditor although another editor is configured");
        assertTrue(
                ComboBoxFilterUtils.isFilterRowComboBoxCellEditor(
                        this.natTable.getConfigRegistry(),
                        DataModelConstants.BIRTHDAY_COLUMN_POSITION),
                "Did not identify a FilterRowComboBoxCellEditor");
        assertTrue(
                ComboBoxFilterUtils.isFilterRowComboBoxCellEditor(
                        this.natTable.getConfigRegistry(),
                        DataModelConstants.STREET_COLUMN_POSITION),
                "Did not identify a FilterRowComboBoxCellEditor");
        assertFalse(
                ComboBoxFilterUtils.isFilterRowComboBoxCellEditor(
                        this.natTable.getConfigRegistry(),
                        DataModelConstants.HOUSENUMBER_COLUMN_POSITION),
                "Identified FilterRowComboBoxCellEditor although another editor is configured");
        assertTrue(
                ComboBoxFilterUtils.isFilterRowComboBoxCellEditor(
                        this.natTable.getConfigRegistry(),
                        DataModelConstants.POSTALCODE_COLUMN_POSITION),
                "Did not identify a FilterRowComboBoxCellEditor");
        assertTrue(
                ComboBoxFilterUtils.isFilterRowComboBoxCellEditor(
                        this.natTable.getConfigRegistry(),
                        DataModelConstants.CITY_COLUMN_POSITION),
                "Did not identify a FilterRowComboBoxCellEditor");
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void shouldDistinctNullAndEmptyString(boolean caching) {
        this.filterRowComboBoxDataProvider.setCachingEnabled(caching);

        // set the value of a lastname to an empty String
        this.bodyLayer.filterList.get(0).setLastName("");
        List<?> lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        assertTrue(ObjectUtils.collectionsEqual(LASTNAMES, lastnames), "not all values in collection");
        // reset the value
        this.bodyLayer.filterList.get(0).setLastName("Simpson");
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void shouldNotDistinctNullAndEmptyString(boolean caching) {
        this.filterRowComboBoxDataProvider.setCachingEnabled(caching);

        // set the value of a lastname to an empty String
        this.bodyLayer.filterList.get(0).setLastName("");
        // disable the distinct handling
        this.filterRowComboBoxDataProvider.setDistinctNullAndEmpty(false);
        List<?> lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        assertFalse(ObjectUtils.collectionsEqual(LASTNAMES, lastnames), "all values in collection");

        ArrayList<String> enhanced = new ArrayList<String>(LASTNAMES);
        enhanced.add("");
        assertTrue(ObjectUtils.collectionsEqual(enhanced, lastnames), "not all values in collection");

        // reset the value
        this.bodyLayer.filterList.get(0).setLastName("Simpson");
        // enable the distinct handling again
        this.filterRowComboBoxDataProvider.setDistinctNullAndEmpty(true);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void shouldAlwaysReturnAllValuesForSingleComboFilter(boolean caching) {
        this.filterRowComboBoxDataProvider.setCachingEnabled(caching);

        // load the possible values first to simulate same behavior as in UI
        List<?> lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);

        assertTrue(ObjectUtils.collectionsEqual(LASTNAMES, lastnames), "not all values in collection");
        assertTrue(
                ComboBoxFilterUtils.isAllSelected(
                        DataModelConstants.LASTNAME_COLUMN_POSITION,
                        this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.LASTNAME_COLUMN_POSITION, 1),
                        this.filterRowComboBoxDataProvider),
                "not all values selected");

        // filter
        this.natTable.doCommand(new UpdateDataCommand(this.natTable, 2, 1, new ArrayList<>(Arrays.asList("Simpson"))));
        assertEquals(15, this.bodyLayer.getFilterList().size());

        // still all values should be in the collection, but not all is selected
        // anymore
        lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        assertTrue(ObjectUtils.collectionsEqual(LASTNAMES, lastnames), "not all values in collection");
        assertFalse(
                ComboBoxFilterUtils.isAllSelected(
                        DataModelConstants.LASTNAME_COLUMN_POSITION,
                        this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.LASTNAME_COLUMN_POSITION, 1),
                        this.filterRowComboBoxDataProvider),
                "all values selected");
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void shouldOnlyShowVisibleItemsInNotFilteredColumn(boolean caching) {
        this.filterRowComboBoxDataProvider.setCachingEnabled(caching);

        // first check if all street values are there
        List<?> streets = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.STREET_COLUMN_POSITION, 0);
        assertTrue(ObjectUtils.collectionsEqual(STREETS, streets), "not all values in collection");

        // now filter for lastname
        List<?> lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        assertTrue(ObjectUtils.collectionsEqual(LASTNAMES, lastnames), "not all values in collection");
        this.natTable.doCommand(new UpdateDataCommand(this.natTable, 2, 1, new ArrayList<>(Arrays.asList("Simpson"))));

        // now check that the values for the street column are reduced
        streets = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.STREET_COLUMN_POSITION, 0);
        assertFalse(ObjectUtils.collectionsEqual(STREETS, streets), "all values in collection");
        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Evergreen Terrace", "South Street"), streets), "not the reduced collection");
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void shouldOnlyShowVisibleItemsInNotFilteredColumnWithNonComboFilter(boolean caching) {
        this.filterRowComboBoxDataProvider.setCachingEnabled(caching);

        // first check if all lastname values are there
        List<?> lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        assertTrue(ObjectUtils.collectionsEqual(LASTNAMES, lastnames), "not all values in collection");

        // now filter for firstname
        this.natTable.doCommand(new UpdateDataCommand(this.natTable, 1, 1, "Homer"));

        // now check that the values for the lastname column are reduced
        lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        assertFalse(ObjectUtils.collectionsEqual(LASTNAMES, lastnames), "all values in collection");
        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Simpson"), lastnames), "not the reduced collection");
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void shouldReduceFirstComboValuesIfSecondComboFilterIsApplied(boolean caching) {
        this.filterRowComboBoxDataProvider.setCachingEnabled(caching);

        List<?> lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        List<?> streets = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.STREET_COLUMN_POSITION, 0);

        // apply filter in lastname
        this.natTable.doCommand(new UpdateDataCommand(this.natTable, 2, 1, new ArrayList<>(Arrays.asList("Simpson"))));

        // lastnames still complete, streets reduced
        lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        streets = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.STREET_COLUMN_POSITION, 0);

        assertTrue(ObjectUtils.collectionsEqual(LASTNAMES, lastnames), "not all values in collection");
        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Evergreen Terrace", "South Street"), streets), "not the reduced collection");

        // apply filter in street
        this.natTable.doCommand(new UpdateDataCommand(this.natTable, 6, 1, new ArrayList<>(Arrays.asList("Evergreen Terrace"))));

        assertEquals(5, this.bodyLayer.getFilterList().size());

        // lastnames should now only contain the visible entry, streets are
        // still same as before
        lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        streets = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.STREET_COLUMN_POSITION, 0);

        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Simpson"), lastnames), "not the reduced collection");
        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Evergreen Terrace", "South Street"), streets), "not the previous reduced collection");
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void shouldReduceFirstComboValuesIfSecondNonComboFilterIsApplied(boolean caching) {
        this.filterRowComboBoxDataProvider.setCachingEnabled(caching);

        List<?> lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);

        // apply filter in lastname
        this.natTable.doCommand(new UpdateDataCommand(this.natTable, 2, 1, new ArrayList<>(Arrays.asList("Simpson"))));

        // lastnames still complete
        lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);

        assertTrue(ObjectUtils.collectionsEqual(LASTNAMES, lastnames), "not all values in collection");

        // apply filter in firstname
        this.natTable.doCommand(new UpdateDataCommand(this.natTable, 1, 1, "Homer"));

        assertEquals(5, this.bodyLayer.getFilterList().size());

        // lastnames should now only contain the visible entry
        lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);

        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Simpson"), lastnames), "not the reduced collection");
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void shouldReduceSecondComboContentOnClearFirstFilter(boolean caching) {
        this.filterRowComboBoxDataProvider.setCachingEnabled(caching);

        // first load all values
        List<?> lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        List<?> streets = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.STREET_COLUMN_POSITION, 0);

        // apply filter in lastname and street
        this.natTable.doCommand(new UpdateDataCommand(this.natTable, 2, 1, new ArrayList<>(Arrays.asList("Simpson"))));
        this.natTable.doCommand(new UpdateDataCommand(this.natTable, 6, 1, new ArrayList<>(Arrays.asList("Evergreen Terrace"))));

        // lastnames should now only contain the visible entry, streets are
        // still same as before
        lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        streets = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.STREET_COLUMN_POSITION, 0);

        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Simpson"), lastnames), "not the reduced collection");
        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Evergreen Terrace", "South Street"), streets), "not the previous reduced collection");

        // clear filter in lastname
        this.natTable.doCommand(new ClearFilterCommand(this.natTable, 2));

        // lastnames should only contain the current visible values
        // streets should also contain all current visible values, as it is the
        // last applied combobox filter
        lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        streets = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.STREET_COLUMN_POSITION, 0);

        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Simpson", "Flanders", null), lastnames), "not the reduced collection");
        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Evergreen Terrace"), streets), "not the reduced collection");

        // only the applied filter values should be selected
        assertFalse(
                ComboBoxFilterUtils.isAllSelected(
                        DataModelConstants.STREET_COLUMN_POSITION,
                        this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.STREET_COLUMN_POSITION, 1),
                        this.filterRowComboBoxDataProvider),
                "all values selected");
        assertTrue(ObjectUtils.collectionsEqual(
                Arrays.asList("Evergreen Terrace"),
                (Collection<?>) this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.STREET_COLUMN_POSITION, 1)));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void shouldNotShowAllInFirstOnClearSecondFilter(boolean caching) {
        this.filterRowComboBoxDataProvider.setCachingEnabled(caching);

        // first load all values
        List<?> lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        List<?> streets = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.STREET_COLUMN_POSITION, 0);

        // apply filter in lastname and street
        this.natTable.doCommand(new UpdateDataCommand(this.natTable, 2, 1, new ArrayList<>(Arrays.asList("Simpson"))));
        this.natTable.doCommand(new UpdateDataCommand(this.natTable, 6, 1, new ArrayList<>(Arrays.asList("Evergreen Terrace"))));

        // lastnames should now only contain the visible entry, streets are
        // still same as before
        lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        streets = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.STREET_COLUMN_POSITION, 0);

        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Simpson"), lastnames), "not the reduced collection");
        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Evergreen Terrace", "South Street"), streets), "not the previous reduced collection");

        // clear filter in street
        this.natTable.doCommand(new ClearFilterCommand(this.natTable, 6));

        // lastnames should now only contain the current visible values
        // streets should also contain all current visible values
        lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        streets = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.STREET_COLUMN_POSITION, 0);

        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Simpson"), lastnames), "not the reduced collection");
        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Evergreen Terrace", "South Street"), streets), "not the previous reduced collection");

        // Note:
        // the following might not be expected, as the user sees that a filter
        // is applied, but only sees a single entry that is selected. Currently
        // it is not possible to restore to a previous state of the dropdown
        // content.

        // only the applied filter values should be selected
        // although only a single item is selected and visible in the combo,
        // isAllSelected should be false
        assertFalse(
                ComboBoxFilterUtils.isAllSelected(
                        DataModelConstants.LASTNAME_COLUMN_POSITION,
                        this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.LASTNAME_COLUMN_POSITION, 1),
                        this.filterRowComboBoxDataProvider),
                "all values selected");
        assertTrue(ObjectUtils.collectionsEqual(
                Arrays.asList("Simpson"),
                (Collection<?>) this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.LASTNAME_COLUMN_POSITION, 1)));
    }

    @Test
    public void shouldKeepFilterOnEditWithCaching() throws InterruptedException {
        // the update on edit does only work if caching is enabled
        this.filterRowComboBoxDataProvider.setCachingEnabled(true);

        // first load values
        List<?> lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        Object lastnameFilter = this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.LASTNAME_COLUMN_POSITION, 1);

        assertEquals(EditConstants.SELECT_ALL_ITEMS_VALUE, lastnameFilter);
        assertTrue(
                ComboBoxFilterUtils.isAllSelected(
                        DataModelConstants.LASTNAME_COLUMN_POSITION,
                        lastnameFilter,
                        this.filterRowComboBoxDataProvider),
                "not all values selected");

        // apply filter in lastname
        this.natTable.doCommand(new UpdateDataCommand(this.natTable, 2, 1, new ArrayList<>(Arrays.asList("Simpson"))));

        // still all values should be in the collection, but not all is selected
        // anymore
        lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        lastnameFilter = this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.LASTNAME_COLUMN_POSITION, 1);
        assertTrue(ObjectUtils.collectionsEqual(LASTNAMES, lastnames), "not all values in collection");
        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Simpson"), (Collection<?>) lastnameFilter), "not all values in collection");
        assertFalse(
                ComboBoxFilterUtils.isAllSelected(
                        DataModelConstants.LASTNAME_COLUMN_POSITION,
                        lastnameFilter,
                        this.filterRowComboBoxDataProvider),
                "all values selected");

        ComboUpdateListener listener = new ComboUpdateListener();
        CountDownLatch countDown = new CountDownLatch(1);
        listener.setCountDown(countDown);
        this.filterRowComboBoxDataProvider.addCacheUpdateListener(listener);

        // edit one entry
        this.natTable.doCommand(new UpdateDataCommand(this.natTable, 2, 3, "Wiggum"));

        boolean completed = countDown.await(2000, TimeUnit.MILLISECONDS);

        assertTrue(completed, "Timeout - no event received");

        assertEquals(1, listener.getEventsCount());

        FilterRowComboUpdateEvent evt = listener.getReceivedEvents().get(0);
        assertEquals(1, evt.getColumnIndex());
        assertEquals(1, evt.getAddedItems().size());
        assertEquals("Wiggum", evt.getAddedItems().iterator().next());

        assertEquals("Wiggum", this.natTable.getDataValueByPosition(2, 3));

        lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        lastnameFilter = this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.LASTNAME_COLUMN_POSITION, 1);
        ArrayList<String> modifiedLastnames = new ArrayList<>(LASTNAMES);
        modifiedLastnames.add("Wiggum");
        assertTrue(ObjectUtils.collectionsEqual(modifiedLastnames, lastnames), "not all values in collection");
        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Simpson", "Wiggum"), (Collection<?>) lastnameFilter), "not all values in collection");
        assertFalse(
                ComboBoxFilterUtils.isAllSelected(
                        DataModelConstants.LASTNAME_COLUMN_POSITION,
                        lastnameFilter,
                        this.filterRowComboBoxDataProvider),
                "all values selected");

        // allValues should also be updated and contain the new value
        assertTrue(
                ObjectUtils.collectionsEqual(
                        modifiedLastnames,
                        this.filterRowComboBoxDataProvider.getAllValues(DataModelConstants.LASTNAME_COLUMN_POSITION)),
                "not all values in collection");

        listener.clearReceivedEvents();

        // edit back
        countDown = new CountDownLatch(1);
        listener.setCountDown(countDown);

        this.natTable.doCommand(new UpdateDataCommand(this.natTable, 2, 3, "Simpson"));

        completed = countDown.await(2000, TimeUnit.MILLISECONDS);

        assertTrue(completed, "Timeout - no event received");

        assertEquals(1, listener.getEventsCount());

        evt = listener.getReceivedEvents().get(0);
        assertEquals(1, evt.getColumnIndex());
        assertEquals(0, evt.getAddedItems().size());
        assertEquals(1, evt.getRemovedItems().size());
        assertEquals("Wiggum", evt.getRemovedItems().iterator().next());

        assertEquals("Simpson", this.natTable.getDataValueByPosition(2, 3));

        lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        lastnameFilter = this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.LASTNAME_COLUMN_POSITION, 1);
        assertTrue(ObjectUtils.collectionsEqual(LASTNAMES, lastnames), "not all values in collection");
        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Simpson"), (Collection<?>) lastnameFilter), "not all values in collection");
        assertFalse(
                ComboBoxFilterUtils.isAllSelected(
                        DataModelConstants.LASTNAME_COLUMN_POSITION,
                        lastnameFilter,
                        this.filterRowComboBoxDataProvider),
                "all values selected");

        // allValues should also be updated and not contain the removed value
        assertTrue(
                ObjectUtils.collectionsEqual(
                        lastnames,
                        this.filterRowComboBoxDataProvider.getAllValues(DataModelConstants.LASTNAME_COLUMN_POSITION)),
                "not all values in collection");
    }

    @Test
    public void shouldKeepFilterOnStructuralChangesWithCaching() throws InterruptedException {
        // the update on edit does only work if caching is enabled
        this.filterRowComboBoxDataProvider.setCachingEnabled(true);

        // first load values
        List<?> lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        Object lastnameFilter = this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.LASTNAME_COLUMN_POSITION, 1);
        List<?> streets = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.STREET_COLUMN_POSITION, 0);
        Object streetFilter = this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.STREET_COLUMN_POSITION, 1);

        assertEquals(EditConstants.SELECT_ALL_ITEMS_VALUE, lastnameFilter);
        assertTrue(
                ComboBoxFilterUtils.isAllSelected(
                        DataModelConstants.LASTNAME_COLUMN_POSITION,
                        lastnameFilter,
                        this.filterRowComboBoxDataProvider),
                "not all values selected");
        assertEquals(EditConstants.SELECT_ALL_ITEMS_VALUE, streetFilter);
        assertTrue(
                ComboBoxFilterUtils.isAllSelected(
                        DataModelConstants.STREET_COLUMN_POSITION,
                        streetFilter,
                        this.filterRowComboBoxDataProvider),
                "not all values selected");

        // apply filter in lastname and street
        this.natTable.doCommand(new UpdateDataCommand(this.natTable, 2, 1, new ArrayList<>(Arrays.asList("Simpson"))));
        this.natTable.doCommand(new UpdateDataCommand(this.natTable, 6, 1, new ArrayList<>(Arrays.asList("Evergreen Terrace"))));

        // lastnames should now only contain the visible entry, streets are
        // still same as before
        lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        lastnameFilter = this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.LASTNAME_COLUMN_POSITION, 1);
        streets = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.STREET_COLUMN_POSITION, 0);
        streetFilter = this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.STREET_COLUMN_POSITION, 1);

        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Simpson"), lastnames), "not the reduced collection");
        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Simpson"), (Collection<?>) lastnameFilter), "not the reduced collection");
        assertFalse(
                ComboBoxFilterUtils.isAllSelected(
                        DataModelConstants.LASTNAME_COLUMN_POSITION,
                        lastnameFilter,
                        this.filterRowComboBoxDataProvider),
                "all values selected");
        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Evergreen Terrace", "South Street"), streets), "not the previous reduced collection");
        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Evergreen Terrace"), (Collection<?>) streetFilter), "not all values from previous collection");
        assertFalse(
                ComboBoxFilterUtils.isAllSelected(
                        DataModelConstants.LASTNAME_COLUMN_POSITION,
                        lastnameFilter,
                        this.filterRowComboBoxDataProvider),
                "all values selected");

        // add an entry
        Person person = new Person(42, "Ralph", "Wiggum", Gender.MALE, false, new Date());
        Address address = new Address();
        address.setStreet("Some Street");
        address.setHousenumber(42);
        address.setPostalCode(12345);
        address.setCity("In the clouds");

        PersonWithAddress entry = new PersonWithAddress(person, address);
        this.bodyLayer.eventList.add(entry);

        // we need to wait here as the listChanged handling is triggered with a
        // delay of 100 ms to avoid too frequent calculations
        Thread.sleep(200);

        // test that still all filters are set, but the combobox collection now
        // contains all values
        lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        lastnameFilter = this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.LASTNAME_COLUMN_POSITION, 1);
        ArrayList<String> modifiedLastnames = new ArrayList<>(LASTNAMES);
        modifiedLastnames.add("Wiggum");
        streets = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.STREET_COLUMN_POSITION, 0);
        streetFilter = this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.STREET_COLUMN_POSITION, 1);
        ArrayList<String> modifiedStreets = new ArrayList<>(STREETS);
        modifiedStreets.add("Some Street");

        assertTrue(ObjectUtils.collectionsEqual(modifiedLastnames, lastnames), "not all values in collection");
        // Note: Wiggum is not added as we can not determine added values if we
        // collect the combobox content from the filterlist
        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Simpson"), (Collection<?>) lastnameFilter), "not all values in collection");
        assertFalse(
                ComboBoxFilterUtils.isAllSelected(
                        DataModelConstants.LASTNAME_COLUMN_POSITION,
                        lastnameFilter,
                        this.filterRowComboBoxDataProvider),
                "all values selected");
        assertTrue(ObjectUtils.collectionsEqual(modifiedStreets, streets), "not all values in collection");
        // Note: Some Street is not added as we can not determine added values
        // if we collect the combobox content from the filterlist
        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Evergreen Terrace"), (Collection<?>) streetFilter), "not all values in collection");
        assertFalse(
                ComboBoxFilterUtils.isAllSelected(
                        DataModelConstants.STREET_COLUMN_POSITION,
                        streetFilter,
                        this.filterRowComboBoxDataProvider),
                "all values selected");

        // allValues should also be updated and contain the new value
        assertTrue(
                ObjectUtils.collectionsEqual(
                        modifiedLastnames,
                        this.filterRowComboBoxDataProvider.getAllValues(DataModelConstants.LASTNAME_COLUMN_POSITION)),
                "not all values in collection");
        assertTrue(
                ObjectUtils.collectionsEqual(
                        modifiedStreets,
                        this.filterRowComboBoxDataProvider.getAllValues(DataModelConstants.STREET_COLUMN_POSITION)),
                "not all values in collection");

        // remove entry again
        this.bodyLayer.eventList.remove(entry);

        Thread.sleep(200);

        lastnames = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.LASTNAME_COLUMN_POSITION, 0);
        lastnameFilter = this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.LASTNAME_COLUMN_POSITION, 1);
        streets = this.filterRowComboBoxDataProvider.getValues(DataModelConstants.STREET_COLUMN_POSITION, 0);
        streetFilter = this.filterRowHeaderLayer.getDataValueByPosition(DataModelConstants.STREET_COLUMN_POSITION, 1);

        assertTrue(ObjectUtils.collectionsEqual(LASTNAMES, lastnames), "not the reduced collection");
        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Simpson"), (Collection<?>) lastnameFilter), "not the reduced collection");
        assertFalse(
                ComboBoxFilterUtils.isAllSelected(
                        DataModelConstants.LASTNAME_COLUMN_POSITION,
                        lastnameFilter,
                        this.filterRowComboBoxDataProvider),
                "all values selected");
        assertTrue(ObjectUtils.collectionsEqual(STREETS, streets), "not the previous reduced collection");
        assertTrue(ObjectUtils.collectionsEqual(Arrays.asList("Evergreen Terrace"), (Collection<?>) streetFilter), "not all values from previous collection");
        assertFalse(
                ComboBoxFilterUtils.isAllSelected(
                        DataModelConstants.LASTNAME_COLUMN_POSITION,
                        lastnameFilter,
                        this.filterRowComboBoxDataProvider),
                "all values selected");
    }

    private static List<String> LASTNAMES = Arrays.asList("Simpson", "Flanders", "Leonard", "Carlson", "Lovejoy", null);
    private static List<String> STREETS = Arrays.asList("Evergreen Terrace", "South Street", "Main Street");
    private static List<String> CITIES = Arrays.asList("Springfield", "Shelbyville", "Ogdenville");

    private static List<PersonWithAddress> createPersons(int startId) {
        List<PersonWithAddress> result = new ArrayList<>();

        Address evergreen = new Address();
        evergreen.setStreet("Evergreen Terrace");
        evergreen.setHousenumber(42);
        evergreen.setPostalCode(11111);
        evergreen.setCity("Springfield");

        Address south = new Address();
        south.setStreet("South Street");
        south.setHousenumber(23);
        south.setPostalCode(22222);
        south.setCity("Shelbyville");

        Address main = new Address();
        main.setStreet("Main Street");
        main.setHousenumber(4711);
        main.setPostalCode(33333);
        main.setCity("Ogdenville");

        result.add(new PersonWithAddress(
                new Person(startId + 1, "Homer", "Simpson", Gender.MALE, true, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 2, "Homer", "Simpson", Gender.MALE, true, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 3, "Marge", "Simpson", Gender.FEMALE, true, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 4, "Marge", "Simpson", Gender.FEMALE, true, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 5, "Marge", "Simpson", Gender.FEMALE, true, new Date(), null),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 6, "Ned", null, Gender.MALE, true, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 7, "Maude", null, Gender.FEMALE, true, new Date()),
                evergreen));

        result.add(new PersonWithAddress(
                new Person(startId + 8, "Homer", "Simpson", Gender.MALE, true, new Date()),
                south));
        result.add(new PersonWithAddress(
                new Person(startId + 9, "Homer", "Simpson", Gender.MALE, true, new Date()),
                south));
        result.add(new PersonWithAddress(
                new Person(startId + 10, "Homer", "Simpson", Gender.MALE, true, new Date()),
                south));
        result.add(new PersonWithAddress(
                new Person(startId + 11, "Bart", "Simpson", Gender.MALE, false, new Date()),
                south));
        result.add(new PersonWithAddress(
                new Person(startId + 12, "Bart", "Simpson", Gender.MALE, false, new Date()),
                south));
        result.add(new PersonWithAddress(
                new Person(startId + 13, "Bart", "Simpson", Gender.MALE, false, new Date()),
                south));
        result.add(new PersonWithAddress(
                new Person(startId + 14, "Marge", "Simpson", Gender.FEMALE, true, new Date()),
                south));
        result.add(new PersonWithAddress(
                new Person(startId + 15, "Marge", "Simpson", Gender.FEMALE, true, new Date()),
                south));
        result.add(new PersonWithAddress(
                new Person(startId + 16, "Lisa", "Simpson", Gender.FEMALE, false, new Date()),
                south));
        result.add(new PersonWithAddress(
                new Person(startId + 17, "Lisa", "Simpson", Gender.FEMALE, false, new Date()),
                south));

        result.add(new PersonWithAddress(
                new Person(startId + 18, "Ned", "Flanders", Gender.MALE, true, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 19, "Ned", "Flanders", Gender.MALE, true, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 20, "Maude", "Flanders", Gender.FEMALE, true, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 21, "Maude", "Flanders", Gender.FEMALE, true, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 22, "Rod", "Flanders", Gender.MALE, false, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 23, "Rod", "Flanders", Gender.MALE, false, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 24, "Tod", "Flanders", Gender.MALE, false, new Date()),
                evergreen));
        result.add(new PersonWithAddress(
                new Person(startId + 25, "Tod", "Flanders", Gender.MALE, false, new Date()),
                evergreen));

        result.add(new PersonWithAddress(
                new Person(startId + 26, "Lenny", "Leonard", Gender.MALE, false, new Date()),
                main));
        result.add(new PersonWithAddress(
                new Person(startId + 27, "Lenny", "Leonard", Gender.MALE, false, new Date()),
                main));

        result.add(new PersonWithAddress(
                new Person(startId + 28, "Carl", "Carlson", Gender.MALE, false, new Date()),
                main));
        result.add(new PersonWithAddress(
                new Person(startId + 29, "Carl", "Carlson", Gender.MALE, false, new Date()),
                main));

        result.add(new PersonWithAddress(
                new Person(startId + 30, "Timothy", "Lovejoy", Gender.MALE, false, new Date()),
                main));
        return result;

    }

    static class BodyLayerStack<T> extends AbstractLayerTransform {

        private final EventList<T> eventList;
        private final SortedList<T> sortedList;
        private final FilterList<T> filterList;

        private final IDataProvider bodyDataProvider;

        private final DataLayer bodyDataLayer;

        private final SelectionLayer selectionLayer;

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

            this.bodyDataProvider =
                    new ListDataProvider<>(this.filterList, columnPropertyAccessor);
            this.bodyDataLayer = new DataLayer(getBodyDataProvider());

            this.bodyDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());

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

            ViewportLayer viewportLayer = new ViewportLayer(this.selectionLayer);

            setUnderlyingLayer(viewportLayer);
        }

        public ILayer getGlazedListsEventLayer() {
            return this.glazedListsEventLayer;
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

        public DataLayer getBodyDataLayer() {
            return this.bodyDataLayer;
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
