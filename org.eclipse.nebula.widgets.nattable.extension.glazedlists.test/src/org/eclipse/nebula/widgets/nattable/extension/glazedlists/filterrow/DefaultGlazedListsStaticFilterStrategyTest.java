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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow;

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.fixture.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.config.DefaultFilterRowConfiguration;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.Matcher;

public class DefaultGlazedListsStaticFilterStrategyTest {

    private static FilterList<Person> filterList;

    private static ConfigRegistry configRegistry;
    private static DataLayerFixture columnHeaderLayer;
    private static DefaultGlazedListsStaticFilterStrategy<Person> filterStrategy;
    private static FilterRowDataProvider<Person> dataProvider;

    private static String[] personPropertyNames = {
            "firstName",
            "lastName",
            "gender",
            "married",
            "birthday" };

    private static Matcher<Person> homerFilter = new Matcher<Person>() {

        @Override
        public boolean matches(Person item) {
            return !"Homer".equals(item.getFirstName());
        }
    };

    @BeforeClass
    public static void init() {
        // initialize the collection with a big amount of values
        filterList = new FilterList<>(GlazedLists.eventList(PersonService.getFixedPersons()));
        for (int i = 1; i < 1000; i++) {
            filterList.addAll(PersonService.getFixedPersons());
        }
        configRegistry = new ConfigRegistry();

        new DefaultNatTableStyleConfiguration().configureRegistry(configRegistry);
        new DefaultFilterRowConfiguration().configureRegistry(configRegistry);

        columnHeaderLayer = new DataLayerFixture(5, 2, 100, 50);
        filterStrategy = new DefaultGlazedListsStaticFilterStrategy<>(
                filterList,
                new ReflectiveColumnPropertyAccessor<Person>(personPropertyNames),
                configRegistry);
        dataProvider = new FilterRowDataProvider<>(
                filterStrategy,
                columnHeaderLayer,
                columnHeaderLayer.getDataProvider(), configRegistry);
    }

    @After
    public void tearDown() {
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

    // with static filter

    @Test
    public void shouldFilterForSimpsonsWithStaticHomerFilter() {
        filterStrategy.addStaticFilter(homerFilter);
        assertEquals(15000, filterList.size());

        dataProvider.setDataValue(1, 1, "Simpson");
        assertEquals(7000, filterList.size());

        filterStrategy.removeStaticFilter(homerFilter);
        assertEquals(10000, filterList.size());
    }

    @Test
    public void shouldFilterForMultipleCriteriaWithStaticHomerFilter() {
        filterStrategy.addStaticFilter(homerFilter);
        assertEquals(15000, filterList.size());

        // filter: contains m
        // per fixed we have 3 Homer, 2 Marge, 2 Maude
        dataProvider.setDataValue(0, 1, "m");
        assertEquals(4000, filterList.size());

        dataProvider.setDataValue(1, 1, "Simpson");
        assertEquals(2000, filterList.size());

        filterStrategy.removeStaticFilter(homerFilter);
        assertEquals(5000, filterList.size());
    }

    @Test
    public void shouldResetFilterinSameOrderWithStaticHomerFilter() {
        filterStrategy.addStaticFilter(homerFilter);

        // filter: contains m
        // per fixed we have 3 Homer, 2 Marge, 2 Maude
        dataProvider.setDataValue(0, 1, "m");
        dataProvider.setDataValue(1, 1, "Simpson");
        assertEquals(2000, filterList.size());

        dataProvider.setDataValue(1, 1, null);
        assertEquals(4000, filterList.size());

        dataProvider.setDataValue(0, 1, null);
        assertEquals(15000, filterList.size());

        filterStrategy.removeStaticFilter(homerFilter);
        assertEquals(18000, filterList.size());
    }

    @Test
    public void shouldResetFilterinDifferentOrderWithStaticHomerFilter() {
        filterStrategy.addStaticFilter(homerFilter);

        // filter: contains m
        // per fixed we have 3 Homer, 2 Marge, 2 Maude
        dataProvider.setDataValue(0, 1, "m");
        dataProvider.setDataValue(1, 1, "Simpson");
        assertEquals(2000, filterList.size());

        dataProvider.setDataValue(0, 1, null);
        assertEquals(7000, filterList.size());

        dataProvider.setDataValue(1, 1, null);
        assertEquals(15000, filterList.size());

        filterStrategy.removeStaticFilter(homerFilter);
        assertEquals(18000, filterList.size());
    }

    @Test
    public void shouldReEvaluateWithoutChange() {
        assertEquals(18000, filterList.size());
        filterStrategy.addStaticFilter(homerFilter);
        assertEquals(15000, filterList.size());

        dataProvider.setDataValue(1, 1, "Simpson");

        assertEquals(7000, filterList.size());

        // trigger again, get an event, no changes
        dataProvider.setDataValue(1, 1, "Simpson");

        assertEquals(7000, filterList.size());

        // trigger again, get an event, no changes
        dataProvider.setDataValue(1, 1, "Simpson");

        assertEquals(7000, filterList.size());
    }

    @Test
    public void shouldReEvaluateWithChange() {
        FilterList<Person> persons = new FilterList<>(GlazedLists.eventList(PersonService.getFixedPersons()));
        DataLayerFixture columnHeaderLayer = new DataLayerFixture(5, 2, 100, 50);
        DefaultGlazedListsStaticFilterStrategy<Person> strategy = new DefaultGlazedListsStaticFilterStrategy<>(
                persons,
                new ReflectiveColumnPropertyAccessor<Person>(personPropertyNames),
                configRegistry);
        FilterRowDataProvider<Person> dataProvider = new FilterRowDataProvider<>(
                strategy,
                columnHeaderLayer,
                columnHeaderLayer.getDataProvider(), configRegistry);

        assertEquals(18, persons.size());
        strategy.addStaticFilter(homerFilter);
        assertEquals(15, persons.size());

        dataProvider.setDataValue(1, 1, "Simpson");

        assertEquals(7, persons.size());

        persons.get(0).setLastName("Flanders");
        assertEquals(7, persons.size());

        // trigger again, get an event, list updated
        dataProvider.setDataValue(1, 1, "Simpson");

        assertEquals(6, persons.size());

        persons.get(0).setLastName("Flanders");

        // trigger again, get an event, list updated
        dataProvider.setDataValue(1, 1, "Simpson");

        assertEquals(5, persons.size());
    }
}
