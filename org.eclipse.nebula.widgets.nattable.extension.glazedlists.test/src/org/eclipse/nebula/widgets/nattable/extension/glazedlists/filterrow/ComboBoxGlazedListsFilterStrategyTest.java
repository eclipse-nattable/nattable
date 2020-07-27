/*******************************************************************************
 * Copyright (c) 2017, 2020 Dirk Fauth.
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

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.edit.EditConstants;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.fixture.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.ComboBoxFilterRowConfiguration;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.config.DefaultFilterRowConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.Matcher;

public class ComboBoxGlazedListsFilterStrategyTest {

    private static FilterList<Person> filterList;

    private static ConfigRegistry configRegistry;
    private static DataLayerFixture columnHeaderLayer;
    private static FilterRowComboBoxDataProvider<Person> comboBoxDataProvider;
    private static ComboBoxGlazedListsFilterStrategy<Person> filterStrategy;
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
        EventList<Person> baseCollection = GlazedLists.eventList(PersonService.getFixedPersons());
        for (int i = 1; i < 1000; i++) {
            baseCollection.addAll(PersonService.getFixedPersons());
        }
        filterList = new FilterList<>(GlazedLists.eventList(baseCollection));

        configRegistry = new ConfigRegistry();

        new DefaultNatTableStyleConfiguration().configureRegistry(configRegistry);
        new DefaultFilterRowConfiguration().configureRegistry(configRegistry);
        new ComboBoxFilterRowConfiguration().configureRegistry(configRegistry);

        columnHeaderLayer = new DataLayerFixture(5, 2, 100, 50);

        IColumnAccessor<Person> bodyDataColumnAccessor = new ReflectiveColumnPropertyAccessor<>(personPropertyNames);
        comboBoxDataProvider = new GlazedListsFilterRowComboBoxDataProvider<>(
                new DataLayer(new ListDataProvider<>(filterList, bodyDataColumnAccessor)),
                baseCollection,
                bodyDataColumnAccessor);

        filterStrategy = new ComboBoxGlazedListsFilterStrategy<>(
                comboBoxDataProvider,
                filterList,
                bodyDataColumnAccessor,
                configRegistry);
        dataProvider = new FilterRowDataProvider<>(
                filterStrategy,
                columnHeaderLayer,
                columnHeaderLayer.getDataProvider(), configRegistry);
    }

    @Before
    public void setup() {
        for (int i = 0; i < dataProvider.getColumnCount(); i++) {
            dataProvider.getFilterIndexToObjectMap().put(i, EditConstants.SELECT_ALL_ITEMS_VALUE);
        }
        filterStrategy.applyFilter(dataProvider.getFilterIndexToObjectMap());
    }

    @Test
    public void shouldFilterForSimpsons() {
        assertEquals(18000, filterList.size());

        dataProvider.setDataValue(1, 1, Arrays.asList("Simpson"));

        assertEquals(10000, filterList.size());
    }

    @Test
    public void shouldFilterForMultipleCriteria() {
        assertEquals(18000, filterList.size());

        dataProvider.setDataValue(0, 1, Arrays.asList("Homer", "Marge", "Maude"));

        assertEquals(7000, filterList.size());

        dataProvider.setDataValue(1, 1, Arrays.asList("Flanders"));

        assertEquals(2000, filterList.size());
    }

    @Test
    public void shouldResetFilterinSameOrder() {
        dataProvider.setDataValue(0, 1, Arrays.asList("Homer", "Marge", "Maude"));
        dataProvider.setDataValue(1, 1, Arrays.asList("Flanders"));
        assertEquals(2000, filterList.size());

        // this will imply to select all values
        dataProvider.setDataValue(1, 1, comboBoxDataProvider.getValues(1, 0));
        assertEquals(7000, filterList.size());

        // setting null should be the same as selecting all
        dataProvider.setDataValue(0, 1, null);
        assertEquals(18000, filterList.size());
    }

    @Test
    public void shouldResetFilterinDifferentOrder() {
        dataProvider.setDataValue(0, 1, Arrays.asList("Homer", "Marge", "Maude"));
        dataProvider.setDataValue(1, 1, Arrays.asList("Flanders"));
        assertEquals(2000, filterList.size());

        dataProvider.setDataValue(0, 1, comboBoxDataProvider.getValues(0, 0));
        assertEquals(8000, filterList.size());

        dataProvider.setDataValue(1, 1, comboBoxDataProvider.getValues(1, 0));
        assertEquals(18000, filterList.size());
    }

    @Test
    public void shouldFilterAll() {
        dataProvider.setDataValue(0, 1, new ArrayList<>());
        assertEquals(0, filterList.size());
    }

    // with static filter

    @Test
    public void shouldFilterForSimpsonsWithStaticHomerFilter() {
        filterStrategy.addStaticFilter(homerFilter);
        assertEquals(15000, filterList.size());

        dataProvider.setDataValue(1, 1, Arrays.asList("Simpson"));
        assertEquals(7000, filterList.size());

        filterStrategy.removeStaticFilter(homerFilter);
        assertEquals(10000, filterList.size());
    }

    @Test
    public void shouldFilterForMultipleCriteriaWithStaticHomerFilter() {
        filterStrategy.addStaticFilter(homerFilter);
        assertEquals(15000, filterList.size());

        dataProvider.setDataValue(0, 1, Arrays.asList("Homer", "Marge", "Maude"));
        assertEquals(4000, filterList.size());

        dataProvider.setDataValue(1, 1, Arrays.asList("Simpson"));
        assertEquals(2000, filterList.size());

        filterStrategy.removeStaticFilter(homerFilter);
        assertEquals(5000, filterList.size());
    }

    @Test
    public void shouldResetFilterinSameOrderWithStaticHomerFilter() {
        filterStrategy.addStaticFilter(homerFilter);

        dataProvider.setDataValue(0, 1, Arrays.asList("Homer", "Marge", "Maude"));
        dataProvider.setDataValue(1, 1, Arrays.asList("Simpson"));
        assertEquals(2000, filterList.size());

        dataProvider.setDataValue(1, 1, comboBoxDataProvider.getValues(1, 0));
        assertEquals(4000, filterList.size());

        dataProvider.setDataValue(0, 1, comboBoxDataProvider.getValues(0, 0));
        assertEquals(15000, filterList.size());

        filterStrategy.removeStaticFilter(homerFilter);
        assertEquals(18000, filterList.size());
    }

    @Test
    public void shouldResetFilterinDifferentOrderWithStaticHomerFilter() {
        filterStrategy.addStaticFilter(homerFilter);

        dataProvider.setDataValue(0, 1, Arrays.asList("Homer", "Marge", "Maude"));
        dataProvider.setDataValue(1, 1, Arrays.asList("Simpson"));
        assertEquals(2000, filterList.size());

        dataProvider.setDataValue(0, 1, comboBoxDataProvider.getValues(0, 0));
        assertEquals(7000, filterList.size());

        dataProvider.setDataValue(1, 1, comboBoxDataProvider.getValues(1, 0));
        assertEquals(15000, filterList.size());

        filterStrategy.removeStaticFilter(homerFilter);
        assertEquals(18000, filterList.size());
    }
}
