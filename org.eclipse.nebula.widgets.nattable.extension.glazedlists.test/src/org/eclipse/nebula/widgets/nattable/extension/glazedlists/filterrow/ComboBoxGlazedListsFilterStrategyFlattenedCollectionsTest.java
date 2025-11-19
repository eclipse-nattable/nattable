/*******************************************************************************
 * Copyright (c) 2025 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.ExtendedPersonWithAddress;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.edit.EditConstants;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.fixture.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.ComboBoxFilterRowConfiguration;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.ComboBoxFilterUtils;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowCategoryValueMapper;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.config.DefaultFilterRowConfiguration;
import org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;

public class ComboBoxGlazedListsFilterStrategyFlattenedCollectionsTest {

    private static EventList<ExtendedPersonWithAddress> baseCollection;
    private FilterList<ExtendedPersonWithAddress> filterList;
    private DataLayer bodyDataLayer;

    private ConfigRegistry configRegistry;
    private DataLayerFixture columnHeaderLayer;
    private FilterRowComboBoxDataProvider<ExtendedPersonWithAddress> comboBoxDataProvider;
    private ComboBoxGlazedListsFilterStrategy<ExtendedPersonWithAddress> filterStrategy;
    private FilterRowDataProvider<ExtendedPersonWithAddress> dataProvider;

    private static String[] personPropertyNames = {
            "firstName",
            "lastName",
            "gender",
            "married",
            "birthday",
            "description",
            "password",
            "favouriteFood",
            "favouriteDrinks" };

    private static FilterRowCategoryValueMapper<String> categoryValueMapper = new FilterRowCategoryValueMapper<String>() {

        @Override
        public List<String> valuesToCategories(List<String> values) {
            return values.stream().map(value -> {
                if (value != null) {
                    switch (value) {
                        case "Burger", "Fries" -> {
                            return "American Food";
                        }
                        case "Pizza", "Pasta", "Salad" -> {
                            return "Italian Food";
                        }
                        case "Fish", "Ham", "Sausages", "Vegetables" -> {
                            return "British Food";
                        }
                    }
                }
                return null;
            }).distinct().collect(Collectors.toList());
        }

        @Override
        public Collection<String> resolveCategories(Collection<String> valuesWithCategories) {
            return valuesWithCategories.stream().map(category -> {
                switch (category) {
                    case "American Food" -> {
                        return Arrays.asList("Burger", "Fries");
                    }
                    case "Italian Food" -> {
                        return Arrays.asList("Pasta", "Pizza", "Salad");
                    }
                    case "British Food" -> {
                        return Arrays.asList("Fish", "Ham", "Sausages", "Vegetables");
                    }
                }
                return Arrays.asList(category);
            }).flatMap(List::stream).distinct().collect(Collectors.toList());
        }

    };

    @BeforeAll
    public static void init() {
        // initialize the collection with a big amount of values
        baseCollection = GlazedLists.eventList(createFixedExtendedPersonsWithAddress());
        for (int i = 1; i < 100; i++) {
            baseCollection.addAll(createFixedExtendedPersonsWithAddress());
        }
    }

    private static List<ExtendedPersonWithAddress> createFixedExtendedPersonsWithAddress() {
        List<ExtendedPersonWithAddress> persons = new ArrayList<>();
        List<Person> fixedPersons = PersonService.getFixedPersons();
        for (int i = 0; i < fixedPersons.size(); i++) {
            Person person = fixedPersons.get(i);
            int modulo = i % 6;
            // description values with a space after the comma
            String description = "";
            switch (modulo) {
                case 0 -> description = "One";
                case 1 -> description = "One, Two";
                case 2 -> description = "One, Two, Three";
                case 3 -> description = "One, Three, Five";
                case 4 -> description = "Two, Four, Six";
                case 5 -> description = "Two, Three, Six";
            }

            // password values without a space after the comma
            String password = "";
            switch (modulo) {
                case 0 -> password = "One";
                case 1 -> password = "One,Two";
                case 2 -> password = "One,Two,Three";
                case 3 -> password = "One,Three,Five";
                case 4 -> password = "Two,Four,Six";
                case 5 -> password = "Two,Three,Six";
            }

            List<String> favouriteFood = null;
            switch (modulo) {
                case 0 -> favouriteFood = Arrays.asList("Pizza", "Burger");
                case 1 -> favouriteFood = Arrays.asList("Salad", "Vegetables");
                case 2 -> favouriteFood = Arrays.asList("Pasta", "Salad");
                case 3 -> favouriteFood = Arrays.asList("Pizza", "Pasta", "Salad");
                case 4 -> favouriteFood = Arrays.asList("Fries", "Burger");
                case 5 -> favouriteFood = Arrays.asList("Ham", "Sausages", "Fish");
            }

            persons.add(new ExtendedPersonWithAddress(person, PersonService.createAddress(),
                    password, description, 1000.0,
                    favouriteFood,
                    Arrays.asList("Water", "Soda", "Beer")));
        }
        return persons;
    }

    @BeforeEach
    public void setup() {
        this.filterList = new FilterList<>(GlazedLists.eventList(baseCollection));

        this.configRegistry = new ConfigRegistry();

        new DefaultNatTableStyleConfiguration().configureRegistry(this.configRegistry);
        new DefaultFilterRowConfiguration().configureRegistry(this.configRegistry);
        new ComboBoxFilterRowConfiguration().configureRegistry(this.configRegistry);

        this.columnHeaderLayer = new DataLayerFixture(8, 2, 100, 50);

        IColumnAccessor<ExtendedPersonWithAddress> bodyDataColumnAccessor = new ReflectiveColumnPropertyAccessor<>(personPropertyNames);
        this.bodyDataLayer = new DataLayer(new ListDataProvider<>(this.filterList, bodyDataColumnAccessor));
        this.comboBoxDataProvider = new GlazedListsFilterRowComboBoxDataProvider<>(
                this.bodyDataLayer,
                baseCollection,
                bodyDataColumnAccessor);

        this.filterStrategy = new ComboBoxGlazedListsFilterStrategy<>(
                this.comboBoxDataProvider,
                this.filterList,
                bodyDataColumnAccessor,
                this.configRegistry);
        this.dataProvider = new FilterRowDataProvider<>(
                this.filterStrategy,
                this.columnHeaderLayer,
                this.columnHeaderLayer.getDataProvider(), this.configRegistry);

        for (int i = 0; i < this.dataProvider.getColumnCount(); i++) {
            this.dataProvider.getFilterIndexToObjectMap().put(i, EditConstants.SELECT_ALL_ITEMS_VALUE);
        }
        this.filterStrategy.applyFilter(this.dataProvider.getFilterIndexToObjectMap());

        // flatten the values for description, password, favouriteFood and
        // favouriteDrinks
        // columns
        this.comboBoxDataProvider.setFlattenCollectionValues(5, true);
        this.comboBoxDataProvider.setFlattenCollectionValues(6, true);
        this.comboBoxDataProvider.setFlattenCollectionValues(7, true);
        this.comboBoxDataProvider.setFlattenCollectionValues(8, true);

        // configure the mapping function for description and password column
        this.comboBoxDataProvider.setCustomMapper(5, ComboBoxFilterUtils.DEFAULT_LIST_VALUE_MAP_FUNCTION);
        this.comboBoxDataProvider.setCustomMapper(6, ComboBoxFilterUtils.DEFAULT_LIST_VALUE_MAP_FUNCTION);
    }

    @Test
    public void shouldFlattenDescription() {
        List<String> allValues = Arrays.asList("Five", "Four", "One", "Six", "Three", "Two");
        assertEquals(allValues, this.comboBoxDataProvider.getAllValues(5));

        // disable flattening and check values again
        this.comboBoxDataProvider.setFlattenCollectionValues(5, false);
        this.comboBoxDataProvider.setCustomMapper(5, null);

        List<String> nonFlattenedValues = Arrays.asList(
                "One",
                "One, Three, Five",
                "One, Two",
                "One, Two, Three",
                "Two, Four, Six",
                "Two, Three, Six");
        assertEquals(nonFlattenedValues, this.comboBoxDataProvider.getAllValues(5));
    }

    @Test
    public void shouldFlattenPassword() {
        List<String> allValues = Arrays.asList("Five", "Four", "One", "Six", "Three", "Two");
        assertEquals(allValues, this.comboBoxDataProvider.getAllValues(6));

        // disable flattening and check values again
        this.comboBoxDataProvider.setFlattenCollectionValues(6, false);
        this.comboBoxDataProvider.setCustomMapper(6, null);

        List<String> nonFlattenedValues = Arrays.asList(
                "One",
                "One,Three,Five",
                "One,Two",
                "One,Two,Three",
                "Two,Four,Six",
                "Two,Three,Six");
        assertEquals(nonFlattenedValues, this.comboBoxDataProvider.getAllValues(6));
    }

    @Test
    public void shouldFlattenFood() {
        List<String> allValues = Arrays.asList("Burger", "Fish", "Fries", "Ham", "Pasta", "Pizza", "Salad", "Sausages", "Vegetables");
        assertEquals(allValues, this.comboBoxDataProvider.getAllValues(7));

        // disable flattening and check values again
        this.comboBoxDataProvider.setFlattenCollectionValues(7, false);

        List<List<String>> nonFlattenedValues = Arrays.asList(
                Arrays.asList("Pizza", "Burger"),
                Arrays.asList("Salad", "Vegetables"),
                Arrays.asList("Pasta", "Salad"),
                Arrays.asList("Pizza", "Pasta", "Salad"),
                Arrays.asList("Fries", "Burger"),
                Arrays.asList("Ham", "Sausages", "Fish"));
        assertTrue(this.comboBoxDataProvider.getAllValues(7).containsAll(nonFlattenedValues), "Not all flattened values contained in all values");
        assertTrue(nonFlattenedValues.containsAll(this.comboBoxDataProvider.getAllValues(7)), "Not all values contained in flattened values");
    }

    @Test
    public void shouldConfigureFlatten() {
        // check if default is false in if no configuration is set
        assertFalse(this.comboBoxDataProvider.isFlattenCollectionValues(4));

        // configure flattening programmatically to enable flatten
        this.comboBoxDataProvider.setFlattenCollectionValues(4, true);

        // check that the flattening is enabled
        assertTrue(this.comboBoxDataProvider.isFlattenCollectionValues(4));

        // disable flattening again
        this.comboBoxDataProvider.setFlattenCollectionValues(4, false);

        // check that the flattening is disabled
        assertFalse(this.comboBoxDataProvider.isFlattenCollectionValues(4));

        // provide a category value mapper which should
        // enable the flattening automatically again
        this.comboBoxDataProvider.setCategoryValueMapper(4, new FilterRowCategoryValueMapper<String>() {

            @Override
            public List<String> valuesToCategories(List<String> values) {
                return values;
            }

            @Override
            public Collection<String> resolveCategories(Collection<String> valuesWithCategories) {
                return valuesWithCategories;
            }

        });

        // check that the flattening is enabled
        assertTrue(this.comboBoxDataProvider.isFlattenCollectionValues(4));
    }

    @Test
    public void shouldConfigureFlattenWithConfigRegistry() {
        // configure the labels on the column header
        this.columnHeaderLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());
        // enable ConfigRegistry handling in the data provider
        this.comboBoxDataProvider.configureConfigRegistryAccess(this.columnHeaderLayer, this.configRegistry);

        // check if default is false in if no configuration is set
        assertFalse(this.comboBoxDataProvider.isFlattenCollectionValues(4));

        // configure flattening programmatically to enable flatten
        this.comboBoxDataProvider.setFlattenCollectionValues(4, true);

        // check that the flattening is enabled
        assertTrue(this.comboBoxDataProvider.isFlattenCollectionValues(4));

        // disable flattening via ConfigRegistry
        this.configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.FLATTEN_COLLECTION_VALUES,
                Boolean.FALSE,
                DisplayMode.NORMAL,
                ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 4);

        // check that flattening is disabled
        assertFalse(this.comboBoxDataProvider.isFlattenCollectionValues(4));

        // provide a category value mapper via ConfigRegistry which should
        // enable the flattening automatically again
        this.configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.CATEGORY_VALUE_MAPPER,
                new FilterRowCategoryValueMapper<String>() {

                    @Override
                    public List<String> valuesToCategories(List<String> values) {
                        return values;
                    }

                    @Override
                    public Collection<String> resolveCategories(Collection<String> valuesWithCategories) {
                        return valuesWithCategories;
                    }

                },
                DisplayMode.NORMAL,
                ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 4);

        // check that the flattening is enabled
        assertTrue(this.comboBoxDataProvider.isFlattenCollectionValues(4));
    }

    @Test
    public void shouldConfigureFlattenViaConfigurationAttribute() {
        List<String> allValues = Arrays.asList("Five", "Four", "One", "Six", "Three", "Two");
        List<String> nonFlattenedValues = Arrays.asList(
                "One",
                "One, Three, Five",
                "One, Two",
                "One, Two, Three",
                "Two, Four, Six",
                "Two, Three, Six");

        // disable flattening programmatically
        this.comboBoxDataProvider.setFlattenCollectionValues(5, false);
        this.comboBoxDataProvider.setCustomMapper(5, null);

        // check that now no flattening is done
        assertEquals(nonFlattenedValues, this.comboBoxDataProvider.getAllValues(5));

        // configure the labels on the column header
        this.columnHeaderLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());

        // enable ConfigRegistry handling in the data provider
        this.comboBoxDataProvider.configureConfigRegistryAccess(this.columnHeaderLayer, this.configRegistry);
        // enable flattening via configuration attributes
        this.configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.LIST_VALUE_MAP_FUNCTION,
                ComboBoxFilterUtils.DEFAULT_LIST_VALUE_MAP_FUNCTION,
                DisplayMode.NORMAL,
                ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 5);
        this.configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.FLATTEN_COLLECTION_VALUES,
                Boolean.TRUE,
                DisplayMode.NORMAL,
                ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 5);

        // after changing the configuration via ConfigRegistry we need to clear
        // the cache manually
        this.comboBoxDataProvider.setFilterCollection(baseCollection, this.columnHeaderLayer);

        assertEquals(allValues, this.comboBoxDataProvider.getAllValues(5));

        // remove the configuration again and check that flattening is disabled
        // again
        this.configRegistry.unregisterConfigAttribute(
                FilterRowConfigAttributes.LIST_VALUE_MAP_FUNCTION,
                DisplayMode.NORMAL,
                ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 5);
        this.configRegistry.unregisterConfigAttribute(
                FilterRowConfigAttributes.FLATTEN_COLLECTION_VALUES,
                DisplayMode.NORMAL,
                ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 5);

        // after changing the configuration via ConfigRegistry we need to clear
        // the cache manually
        this.comboBoxDataProvider.setFilterCollection(baseCollection, this.columnHeaderLayer);

        // check that now no flattening is done
        assertEquals(nonFlattenedValues, this.comboBoxDataProvider.getAllValues(5));
    }

    @Test
    public void shouldFilterForDescription() {
        assertEquals(1800, this.filterList.size());

        this.dataProvider.setDataValue(5, 1, Arrays.asList("Five"));

        assertEquals(300, this.filterList.size());

        for (var person : this.filterList) {
            assertEquals(true, person.getDescription().contains("Five"));
        }
        assertEquals("One, Three, Five", this.filterList.get(0).getDescription());

        this.dataProvider.setDataValue(5, 1, Arrays.asList("Three", "Five"));

        assertEquals(900, this.filterList.size());

        // just check once, works because of the modulo based data setup
        assertEquals("One, Two, Three", this.filterList.get(0).getDescription());
        assertEquals("One, Three, Five", this.filterList.get(1).getDescription());
        assertEquals("Two, Three, Six", this.filterList.get(2).getDescription());
    }

    @Test
    public void shouldFilterForPassword() {
        assertEquals(1800, this.filterList.size());

        this.dataProvider.setDataValue(6, 1, Arrays.asList("Five"));

        assertEquals(300, this.filterList.size());

        for (var person : this.filterList) {
            assertEquals(true, person.getPassword().contains("Five"));
        }
        assertEquals("One,Three,Five", this.filterList.get(0).getPassword());

        this.dataProvider.setDataValue(6, 1, Arrays.asList("Three", "Five"));

        assertEquals(900, this.filterList.size());

        // just check once, works because of the modulo based data setup
        assertEquals("One,Two,Three", this.filterList.get(0).getPassword());
        assertEquals("One,Three,Five", this.filterList.get(1).getPassword());
        assertEquals("Two,Three,Six", this.filterList.get(2).getPassword());
    }

    @Test
    public void shouldFilterForFood() {
        assertEquals(1800, this.filterList.size());

        this.dataProvider.setDataValue(7, 1, Arrays.asList("Pizza", "Burger"));

        assertEquals(900, this.filterList.size());

        // just check once, works because of the modulo based data setup
        assertIterableEquals(Arrays.asList("Pizza", "Burger"), this.filterList.get(0).getFavouriteFood());
        assertIterableEquals(Arrays.asList("Pizza", "Pasta", "Salad"), this.filterList.get(1).getFavouriteFood());
        assertIterableEquals(Arrays.asList("Fries", "Burger"), this.filterList.get(2).getFavouriteFood());
    }

    @Test
    public void shouldResetFilterinSameOrder() {
        this.dataProvider.setDataValue(5, 1, Arrays.asList("Six"));
        this.dataProvider.setDataValue(7, 1, Arrays.asList("Ham", "Sausages"));
        assertEquals(300, this.filterList.size());

        // this will imply to select all values
        this.dataProvider.setDataValue(7, 1, this.comboBoxDataProvider.getValues(7, 0));
        assertEquals(600, this.filterList.size());

        // setting null should be the same as selecting all
        this.dataProvider.setDataValue(5, 1, null);
        assertEquals(1800, this.filterList.size());
    }

    @Test
    public void shouldResetFilterinDifferentOrder() {
        this.dataProvider.setDataValue(5, 1, Arrays.asList("Six"));
        this.dataProvider.setDataValue(7, 1, Arrays.asList("Ham", "Sausages"));
        assertEquals(300, this.filterList.size());

        this.dataProvider.setDataValue(5, 1, this.comboBoxDataProvider.getValues(5, 0));
        assertEquals(300, this.filterList.size());

        this.dataProvider.setDataValue(7, 1, this.comboBoxDataProvider.getValues(7, 0));
        assertEquals(1800, this.filterList.size());
    }

    @Test
    public void shouldFilterAll() {
        this.dataProvider.setDataValue(5, 1, new ArrayList<>());
        assertEquals(0, this.filterList.size());
    }

    @Test
    public void shouldAddCategories() {
        this.comboBoxDataProvider.setCategoryValueMapper(7, categoryValueMapper);

        List<String> allValues = Arrays.asList("American Food", "British Food", "Italian Food", "Burger", "Fish", "Fries", "Ham", "Pasta", "Pizza", "Salad", "Sausages", "Vegetables");
        assertEquals(allValues, this.comboBoxDataProvider.getAllValues(7));
    }

    @Test
    public void shouldReplaceValuesWithCategories() {
        this.comboBoxDataProvider.setCategoryValueMapper(7, categoryValueMapper);
        this.comboBoxDataProvider.setCategoriesOnly(7, true);

        List<String> allValues = Arrays.asList("American Food", "British Food", "Italian Food");
        assertEquals(allValues, this.comboBoxDataProvider.getAllValues(7));
    }

    @Test
    public void shouldHandleNullWithCategories() {
        this.comboBoxDataProvider.setCategoryValueMapper(7, categoryValueMapper);

        // set one value explicitly to null
        List<String> favouriteFood0 = baseCollection.get(0).getFavouriteFood();
        baseCollection.get(0).setFavouriteFood(null);

        // set one value to something that is not mapped which results in a null
        // category
        List<String> favouriteFood1 = baseCollection.get(1).getFavouriteFood();
        baseCollection.get(1).setFavouriteFood(Arrays.asList("Sushi"));

        List<String> allValues = Arrays.asList(null, "American Food", "British Food", "Italian Food", "Burger", "Fish", "Fries", "Ham", "Pasta", "Pizza", "Salad", "Sausages", "Sushi", "Vegetables");
        assertEquals(allValues, this.comboBoxDataProvider.getAllValues(7));

        baseCollection.get(0).setFavouriteFood(favouriteFood0);
        baseCollection.get(1).setFavouriteFood(favouriteFood1);
    }

    @Test
    public void shouldFilterForCategories() {
        this.comboBoxDataProvider.setCategoryValueMapper(7, categoryValueMapper);

        assertEquals(1800, this.filterList.size());

        this.dataProvider.setDataValue(7, 1, Arrays.asList("British Food"));

        assertEquals(600, this.filterList.size());

        // just check once, works because of the modulo based data setup
        assertIterableEquals(Arrays.asList("Salad", "Vegetables"), this.filterList.get(0).getFavouriteFood());
        assertIterableEquals(Arrays.asList("Ham", "Sausages", "Fish"), this.filterList.get(1).getFavouriteFood());
    }
}
