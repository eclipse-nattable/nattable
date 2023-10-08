/*******************************************************************************
 * Copyright (c) 2018, 2023 Dirk Fauth.
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

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDoubleDisplayConverter;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.fixture.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;
import org.eclipse.nebula.widgets.nattable.filterrow.config.DefaultFilterRowConfiguration;
import org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;

public class DefaultGlazedListsFilterStrategyTest {

    private static FilterList<Person> filterList;

    private static ConfigRegistry configRegistry;
    private static DataLayerFixture columnHeaderLayer;
    private static FilterRowDataProvider<Person> dataProvider;

    private static String[] personPropertyNames = {
            "firstName",
            "lastName",
            "gender",
            "married",
            "birthday",
            "money" };

    @BeforeAll
    public static void init() {
        // initialize the collection with a big amount of values
        filterList = new FilterList<>(GlazedLists.eventList(PersonService.getFixedPersons()));
        for (int i = 1; i < 1000; i++) {
            filterList.addAll(PersonService.getFixedPersons());
        }
        configRegistry = new ConfigRegistry();

        new DefaultNatTableStyleConfiguration().configureRegistry(configRegistry);
        new DefaultFilterRowConfiguration().configureRegistry(configRegistry);

        columnHeaderLayer = new DataLayerFixture(6, 2, 100, 50);
        dataProvider = new FilterRowDataProvider<>(
                new DefaultGlazedListsFilterStrategy<>(
                        filterList,
                        new ReflectiveColumnPropertyAccessor<>(personPropertyNames),
                        configRegistry),
                columnHeaderLayer,
                columnHeaderLayer.getDataProvider(), configRegistry);
    }

    @BeforeEach
    public void setup() {
        // ensure to start over with a clear filter
        dataProvider.clearAllFilters();
    }

    @Test
    public void shouldFilterForSimpsons() {
        assertEquals(18000, filterList.size());

        dataProvider.setDataValue(1, 1, "Simpson");

        assertEquals(10000, filterList.size());
    }

    @Test
    public void shouldFilterForMultipleCriteria() {
        assertEquals(18000, filterList.size());

        // filter: contains m
        // per fixed we have 3 Homer, 2 Marge, 2 Maude
        dataProvider.setDataValue(0, 1, "m");

        assertEquals(7000, filterList.size());

        dataProvider.setDataValue(1, 1, "Flanders");

        assertEquals(2000, filterList.size());
    }

    @Test
    public void shouldResetFilterinSameOrder() {
        // filter: contains m
        // per fixed we have 3 Homer, 2 Marge, 2 Maude
        dataProvider.setDataValue(0, 1, "m");
        dataProvider.setDataValue(1, 1, "Flanders");
        assertEquals(2000, filterList.size());

        dataProvider.setDataValue(1, 1, null);
        assertEquals(7000, filterList.size());

        dataProvider.setDataValue(0, 1, null);
        assertEquals(18000, filterList.size());
    }

    @Test
    public void shouldResetFilterinDifferentOrder() {
        // filter: contains m
        // per fixed we have 3 Homer, 2 Marge, 2 Maude
        dataProvider.setDataValue(0, 1, "m");
        dataProvider.setDataValue(1, 1, "Flanders");
        assertEquals(2000, filterList.size());

        dataProvider.setDataValue(0, 1, null);
        assertEquals(8000, filterList.size());

        dataProvider.setDataValue(1, 1, null);
        assertEquals(18000, filterList.size());
    }

    @Test
    public void shouldFilterTwoBooleanColumns() {
        EventList<RowDataFixture> eventList = GlazedLists.eventList(RowDataListFixture.getList());
        FilterList<RowDataFixture> filterList = new FilterList<>(eventList);
        IColumnPropertyAccessor<RowDataFixture> columnPropertyAccessor =
                new ReflectiveColumnPropertyAccessor<>(RowDataListFixture.getPropertyNames());

        ConfigRegistry configRegistry = new ConfigRegistry();

        new DefaultNatTableStyleConfiguration().configureRegistry(configRegistry);
        new DefaultFilterRowConfiguration().configureRegistry(configRegistry);

        DataLayerFixture columnHeaderLayer = new DataLayerFixture(6, 2, 100, 50);
        FilterRowDataProvider<RowDataFixture> dataProvider = new FilterRowDataProvider<>(
                new DefaultGlazedListsFilterStrategy<>(
                        filterList,
                        columnPropertyAccessor,
                        configRegistry),
                columnHeaderLayer,
                columnHeaderLayer.getDataProvider(), configRegistry);

        assertEquals(13, filterList.size());
        dataProvider.setDataValue(27, 1, "true");
        assertEquals(13, filterList.size());
        dataProvider.setDataValue(28, 1, "true");
        assertEquals(0, filterList.size());
    }

    @Test
    public void shouldReEvaluateWithoutChange() {
        assertEquals(18000, filterList.size());

        dataProvider.setDataValue(1, 1, "Simpson");

        assertEquals(10000, filterList.size());

        // trigger again, get an event, no changes
        dataProvider.setDataValue(1, 1, "Simpson");

        assertEquals(10000, filterList.size());

        // trigger again, get an event, no changes
        dataProvider.setDataValue(1, 1, "Simpson");

        assertEquals(10000, filterList.size());
    }

    @Test
    public void shouldReEvaluateWithChange() {
        FilterList<Person> persons = new FilterList<>(GlazedLists.eventList(PersonService.getFixedPersons()));
        DataLayerFixture columnHeaderLayer = new DataLayerFixture(6, 2, 100, 50);
        FilterRowDataProvider<Person> dataProvider = new FilterRowDataProvider<>(
                new DefaultGlazedListsFilterStrategy<>(
                        persons,
                        new ReflectiveColumnPropertyAccessor<>(personPropertyNames),
                        configRegistry),
                columnHeaderLayer,
                columnHeaderLayer.getDataProvider(), configRegistry);

        assertEquals(18, persons.size());

        dataProvider.setDataValue(1, 1, "Simpson");

        assertEquals(10, persons.size());

        persons.get(0).setLastName("Flanders");
        assertEquals(10, persons.size());

        // trigger again, get an event, list updated
        dataProvider.setDataValue(1, 1, "Simpson");

        assertEquals(9, persons.size());

        persons.get(0).setLastName("Flanders");

        // trigger again, get an event, list updated
        dataProvider.setDataValue(1, 1, "Simpson");

        assertEquals(8, persons.size());
    }

    @Test
    public void shouldFilterThresholds() {
        FilterList<Person> persons = new FilterList<>(GlazedLists.eventList(PersonService.getFixedPersons()));
        DataLayerFixture columnHeaderLayer = new DataLayerFixture(6, 2, 100, 50);

        ConfigRegistry configRegistry = new ConfigRegistry();

        new DefaultNatTableStyleConfiguration().configureRegistry(configRegistry);
        new DefaultFilterRowConfiguration().configureRegistry(configRegistry);

        // register the FILTER_DISPLAY_CONVERTER and the REGULAR_EXPRESSION text
        // matching mode to enable threshold matching
        configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER,
                new DefaultDoubleDisplayConverter(),
                DisplayMode.NORMAL,
                FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + "5");

        configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.TEXT_MATCHING_MODE,
                TextMatchingMode.REGULAR_EXPRESSION,
                DisplayMode.NORMAL,
                FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + "5");

        // register a TEXT_DELIMITER to be able to combine filters
        // a single character always results in a AND filter combination
        configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.TEXT_DELIMITER, "&");
        //
        // configRegistry.registerConfigAttribute(
        // FilterRowConfigAttributes.TEXT_DELIMITER, "[&|]");

        FilterRowDataProvider<Person> dataProvider = new FilterRowDataProvider<>(
                new DefaultGlazedListsFilterStrategy<>(
                        persons,
                        new ReflectiveColumnPropertyAccessor<>(personPropertyNames),
                        configRegistry),
                columnHeaderLayer,
                columnHeaderLayer.getDataProvider(), configRegistry);

        assertEquals(18, persons.size());

        // set money of all Bart entries to 10
        dataProvider.setDataValue(0, 1, "Bart");
        assertEquals(3, persons.size());
        for (Person person : persons) {
            person.setMoney(10d);
        }

        // set money of all Lisa entries to 90
        dataProvider.setDataValue(0, 1, "Lisa");
        assertEquals(2, persons.size());
        for (Person person : persons) {
            person.setMoney(90d);
        }

        // set money of all Rod entries to 40
        dataProvider.setDataValue(0, 1, "Rod");
        assertEquals(2, persons.size());
        for (Person person : persons) {
            person.setMoney(40d);
        }

        // set money of all Tod entries to 60
        dataProvider.setDataValue(0, 1, "Tod");
        assertEquals(2, persons.size());
        for (Person person : persons) {
            person.setMoney(60d);
        }

        dataProvider.clearAllFilters();

        assertEquals(18, persons.size());

        // get all Persons with >= 100
        dataProvider.setDataValue(5, 1, ">=100");
        assertEquals(9, persons.size());

        // get all Persons with > 30 & < 80
        // this means all Rods and Tods
        dataProvider.setDataValue(5, 1, "> 30 & < 80");
        assertEquals(4, persons.size());
    }

    @Test
    public void shouldFilterThresholdsWithANDOR() {
        FilterList<Person> persons = new FilterList<>(GlazedLists.eventList(PersonService.getFixedPersons()));
        DataLayerFixture columnHeaderLayer = new DataLayerFixture(6, 2, 100, 50);

        ConfigRegistry configRegistry = new ConfigRegistry();

        new DefaultNatTableStyleConfiguration().configureRegistry(configRegistry);
        new DefaultFilterRowConfiguration().configureRegistry(configRegistry);

        // register the FILTER_DISPLAY_CONVERTER and the REGULAR_EXPRESSION text
        // matching mode to enable threshold matching
        configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER,
                new DefaultDoubleDisplayConverter(),
                DisplayMode.NORMAL,
                FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + "5");

        configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.TEXT_MATCHING_MODE,
                TextMatchingMode.REGULAR_EXPRESSION,
                DisplayMode.NORMAL,
                FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + "5");

        // register a TEXT_DELIMITER to be able to combine filters
        // use the regular expression to use & for AND and | for OR
        configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.TEXT_DELIMITER, "[&\\|]");

        FilterRowDataProvider<Person> dataProvider = new FilterRowDataProvider<>(
                new DefaultGlazedListsFilterStrategy<>(
                        persons,
                        new ReflectiveColumnPropertyAccessor<>(personPropertyNames),
                        configRegistry),
                columnHeaderLayer,
                columnHeaderLayer.getDataProvider(), configRegistry);

        assertEquals(18, persons.size());

        // set money of all Bart entries to 10
        dataProvider.setDataValue(0, 1, "Bart");
        assertEquals(3, persons.size());
        for (Person person : persons) {
            person.setMoney(10d);
        }

        // set money of all Lisa entries to 90
        dataProvider.setDataValue(0, 1, "Lisa");
        assertEquals(2, persons.size());
        for (Person person : persons) {
            person.setMoney(90d);
        }

        // set money of all Marge entries to 200
        dataProvider.setDataValue(0, 1, "Marge");
        assertEquals(2, persons.size());
        for (Person person : persons) {
            person.setMoney(200d);
        }

        // set money of all Rod entries to 40
        dataProvider.setDataValue(0, 1, "Rod");
        assertEquals(2, persons.size());
        for (Person person : persons) {
            person.setMoney(40d);
        }

        // set money of all Tod entries to 60
        dataProvider.setDataValue(0, 1, "Tod");
        assertEquals(2, persons.size());
        for (Person person : persons) {
            person.setMoney(60d);
        }

        dataProvider.clearAllFilters();

        assertEquals(18, persons.size());

        // get all Persons with >= 100
        dataProvider.setDataValue(5, 1, ">=100");
        assertEquals(9, persons.size());

        // get all Persons with > 30 & < 80
        // this means all Rods and Tods
        dataProvider.setDataValue(5, 1, "> 30 & < 80");
        assertEquals(4, persons.size());

        // get all Persons with > 30 | < 80
        // should actually return all entries
        dataProvider.setDataValue(5, 1, "> 30 | < 80");
        assertEquals(18, persons.size());

        // get all Persons with < 30 | > 100
        // should return all Bart and all Marge
        dataProvider.setDataValue(5, 1, "< 30 | > 100");
        assertEquals(5, persons.size());

        // now also filter Marge to only get the Bart entries
        // test to verify that combined filters are working
        dataProvider.setDataValue(0, 1, "Bart");
        assertEquals(3, persons.size());
    }

    @Test
    public void shouldFilterThresholdsAndStrings() {
        FilterList<Person> persons = new FilterList<>(GlazedLists.eventList(PersonService.getFixedPersons()));
        DataLayerFixture columnHeaderLayer = new DataLayerFixture(6, 2, 100, 50);

        ConfigRegistry configRegistry = new ConfigRegistry();

        new DefaultNatTableStyleConfiguration().configureRegistry(configRegistry);
        new DefaultFilterRowConfiguration().configureRegistry(configRegistry);

        // register the FILTER_DISPLAY_CONVERTER and the REGULAR_EXPRESSION text
        // matching mode to enable threshold matching
        configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER,
                new DefaultDoubleDisplayConverter(),
                DisplayMode.NORMAL,
                FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + "5");

        configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.TEXT_MATCHING_MODE,
                TextMatchingMode.REGULAR_EXPRESSION,
                DisplayMode.NORMAL,
                FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + "5");

        // register a TEXT_DELIMITER to be able to combine filters
        // use the regular expression to use & for AND and | for OR
        configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.TEXT_DELIMITER, "[&\\|]");

        FilterRowDataProvider<Person> dataProvider = new FilterRowDataProvider<>(
                new DefaultGlazedListsFilterStrategy<>(
                        persons,
                        new ReflectiveColumnPropertyAccessor<>(personPropertyNames),
                        configRegistry),
                columnHeaderLayer,
                columnHeaderLayer.getDataProvider(), configRegistry);

        assertEquals(18, persons.size());

        // set money of all Bart entries to null
        dataProvider.setDataValue(0, 1, "Bart");
        assertEquals(3, persons.size());
        for (Person person : persons) {
            person.setMoney(null);
        }

        // set money of all Lisa entries to 90
        dataProvider.setDataValue(0, 1, "Lisa");
        assertEquals(2, persons.size());
        for (Person person : persons) {
            person.setMoney(90d);
        }

        // set money of all Marge entries to 200
        dataProvider.setDataValue(0, 1, "Marge");
        assertEquals(2, persons.size());
        for (Person person : persons) {
            person.setMoney(200d);
        }

        // set money of all Rod entries to 40
        dataProvider.setDataValue(0, 1, "Rod");
        assertEquals(2, persons.size());
        for (Person person : persons) {
            person.setMoney(40d);
        }

        // set money of all Tod entries to 60
        dataProvider.setDataValue(0, 1, "Tod");
        assertEquals(2, persons.size());
        for (Person person : persons) {
            person.setMoney(60d);
        }

        dataProvider.clearAllFilters();

        assertEquals(18, persons.size());

        // get all Persons with >= 200
        // this means all Marges
        dataProvider.setDataValue(5, 1, ">=200");
        assertEquals(2, persons.size());

        // get all Persons with no money
        // this means all Barts
        dataProvider.setDataValue(5, 1, "^$");
        assertEquals(3, persons.size());

        // get all Persons with >= 100 or no money
        // this means all Marges and all Barts
        dataProvider.setDataValue(5, 1, ">= 200 | ^$");
        assertEquals(5, persons.size());

        // now also filter Marge to only get the Bart entries
        // test to verify that combined filters are working
        dataProvider.setDataValue(0, 1, "Bart");
        assertEquals(3, persons.size());
    }
}
