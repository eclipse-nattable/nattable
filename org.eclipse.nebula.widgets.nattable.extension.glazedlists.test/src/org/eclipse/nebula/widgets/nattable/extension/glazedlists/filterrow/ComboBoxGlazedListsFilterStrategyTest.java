/*******************************************************************************
 * Copyright (c) 2017, 2024 Dirk Fauth.
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
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.ComboBoxFilterRowConfiguration;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.config.DefaultFilterRowConfiguration;
import org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.Matcher;

public class ComboBoxGlazedListsFilterStrategyTest {

    private static EventList<Person> baseCollection;
    private FilterList<Person> filterList;

    private ConfigRegistry configRegistry;
    private DataLayerFixture columnHeaderLayer;
    private FilterRowComboBoxDataProvider<Person> comboBoxDataProvider;
    private ComboBoxGlazedListsFilterStrategy<Person> filterStrategy;
    private FilterRowDataProvider<Person> dataProvider;

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

    @BeforeAll
    public static void init() {
        // initialize the collection with a big amount of values
        baseCollection = GlazedLists.eventList(PersonService.getFixedPersons());
        for (int i = 1; i < 1000; i++) {
            baseCollection.addAll(PersonService.getFixedPersons());
        }
    }

    @BeforeEach
    public void setup() {
        this.filterList = new FilterList<>(GlazedLists.eventList(baseCollection));

        this.configRegistry = new ConfigRegistry();

        new DefaultNatTableStyleConfiguration().configureRegistry(this.configRegistry);
        new DefaultFilterRowConfiguration().configureRegistry(this.configRegistry);
        new ComboBoxFilterRowConfiguration().configureRegistry(this.configRegistry);

        this.columnHeaderLayer = new DataLayerFixture(5, 2, 100, 50);

        IColumnAccessor<Person> bodyDataColumnAccessor = new ReflectiveColumnPropertyAccessor<>(personPropertyNames);
        this.comboBoxDataProvider = new GlazedListsFilterRowComboBoxDataProvider<>(
                new DataLayer(new ListDataProvider<>(this.filterList, bodyDataColumnAccessor)),
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
    }

    void setMatchMode(String matchMode) {
        TextMatchingMode mode = TextMatchingMode.valueOf(matchMode);
        this.configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.TEXT_MATCHING_MODE,
                mode);
    }

    @ParameterizedTest(name = "matchMode={0}")
    @CsvSource({
            "REGULAR_EXPRESSION",
            "EXACT"
    })
    public void shouldFilterForSimpsons(String matchMode) {
        setMatchMode(matchMode);

        assertEquals(18000, this.filterList.size());

        this.dataProvider.setDataValue(1, 1, Arrays.asList("Simpson"));

        assertEquals(10000, this.filterList.size());
    }

    @ParameterizedTest(name = "matchMode={0}")
    @CsvSource({
            "REGULAR_EXPRESSION",
            "EXACT"
    })
    public void shouldFilterForMultipleCriteria(String matchMode) {
        setMatchMode(matchMode);

        assertEquals(18000, this.filterList.size());

        this.dataProvider.setDataValue(0, 1, Arrays.asList("Homer", "Marge", "Maude"));

        assertEquals(7000, this.filterList.size());

        this.dataProvider.setDataValue(1, 1, Arrays.asList("Flanders"));

        assertEquals(2000, this.filterList.size());
    }

    @ParameterizedTest(name = "matchMode={0}")
    @CsvSource({
            "REGULAR_EXPRESSION",
            "EXACT"
    })
    public void shouldResetFilterinSameOrder(String matchMode) {
        setMatchMode(matchMode);

        this.dataProvider.setDataValue(0, 1, Arrays.asList("Homer", "Marge", "Maude"));
        this.dataProvider.setDataValue(1, 1, Arrays.asList("Flanders"));
        assertEquals(2000, this.filterList.size());

        // this will imply to select all values
        this.dataProvider.setDataValue(1, 1, this.comboBoxDataProvider.getValues(1, 0));
        assertEquals(7000, this.filterList.size());

        // setting null should be the same as selecting all
        this.dataProvider.setDataValue(0, 1, null);
        assertEquals(18000, this.filterList.size());
    }

    @ParameterizedTest(name = "matchMode={0}")
    @CsvSource({
            "REGULAR_EXPRESSION",
            "EXACT"
    })
    public void shouldResetFilterinDifferentOrder(String matchMode) {
        setMatchMode(matchMode);

        this.dataProvider.setDataValue(0, 1, Arrays.asList("Homer", "Marge", "Maude"));
        this.dataProvider.setDataValue(1, 1, Arrays.asList("Flanders"));
        assertEquals(2000, this.filterList.size());

        this.dataProvider.setDataValue(0, 1, this.comboBoxDataProvider.getValues(0, 0));
        assertEquals(8000, this.filterList.size());

        this.dataProvider.setDataValue(1, 1, this.comboBoxDataProvider.getValues(1, 0));
        assertEquals(18000, this.filterList.size());
    }

    @ParameterizedTest(name = "matchMode={0}")
    @CsvSource({
            "REGULAR_EXPRESSION",
            "EXACT"
    })
    public void shouldFilterAll(String matchMode) {
        setMatchMode(matchMode);

        this.dataProvider.setDataValue(0, 1, new ArrayList<>());
        assertEquals(0, this.filterList.size());
    }

    // with static filter

    @ParameterizedTest(name = "matchMode={0}")
    @CsvSource({
            "REGULAR_EXPRESSION",
            "EXACT"
    })
    public void shouldFilterForSimpsonsWithStaticHomerFilter(String matchMode) {
        setMatchMode(matchMode);

        this.filterStrategy.addStaticFilter(homerFilter);
        assertEquals(15000, this.filterList.size());

        this.dataProvider.setDataValue(1, 1, Arrays.asList("Simpson"));
        assertEquals(7000, this.filterList.size());

        this.filterStrategy.removeStaticFilter(homerFilter);
        assertEquals(10000, this.filterList.size());
    }

    @ParameterizedTest(name = "matchMode={0}")
    @CsvSource({
            "REGULAR_EXPRESSION",
            "EXACT"
    })
    public void shouldFilterForMultipleCriteriaWithStaticHomerFilter(String matchMode) {
        setMatchMode(matchMode);

        this.filterStrategy.addStaticFilter(homerFilter);
        assertEquals(15000, this.filterList.size());

        this.dataProvider.setDataValue(0, 1, Arrays.asList("Homer", "Marge", "Maude"));
        assertEquals(4000, this.filterList.size());

        this.dataProvider.setDataValue(1, 1, Arrays.asList("Simpson"));
        assertEquals(2000, this.filterList.size());

        this.filterStrategy.removeStaticFilter(homerFilter);
        assertEquals(5000, this.filterList.size());
    }

    @ParameterizedTest(name = "matchMode={0}")
    @CsvSource({
            "REGULAR_EXPRESSION",
            "EXACT"
    })
    public void shouldResetFilterinSameOrderWithStaticHomerFilter(String matchMode) {
        setMatchMode(matchMode);

        this.filterStrategy.addStaticFilter(homerFilter);

        this.dataProvider.setDataValue(0, 1, Arrays.asList("Homer", "Marge", "Maude"));
        this.dataProvider.setDataValue(1, 1, Arrays.asList("Simpson"));
        assertEquals(2000, this.filterList.size());

        this.dataProvider.setDataValue(1, 1, this.comboBoxDataProvider.getValues(1, 0));
        assertEquals(4000, this.filterList.size());

        this.dataProvider.setDataValue(0, 1, this.comboBoxDataProvider.getValues(0, 0));
        assertEquals(15000, this.filterList.size());

        this.filterStrategy.removeStaticFilter(homerFilter);
        assertEquals(18000, this.filterList.size());
    }

    @ParameterizedTest(name = "matchMode={0}")
    @CsvSource({
            "REGULAR_EXPRESSION",
            "EXACT"
    })
    public void shouldResetFilterinDifferentOrderWithStaticHomerFilter(String matchMode) {
        setMatchMode(matchMode);

        this.filterStrategy.addStaticFilter(homerFilter);

        this.dataProvider.setDataValue(0, 1, Arrays.asList("Homer", "Marge", "Maude"));
        this.dataProvider.setDataValue(1, 1, Arrays.asList("Simpson"));
        assertEquals(2000, this.filterList.size());

        this.dataProvider.setDataValue(0, 1, this.comboBoxDataProvider.getValues(0, 0));
        assertEquals(7000, this.filterList.size());

        this.dataProvider.setDataValue(1, 1, this.comboBoxDataProvider.getValues(1, 0));
        assertEquals(15000, this.filterList.size());

        this.filterStrategy.removeStaticFilter(homerFilter);
        assertEquals(18000, this.filterList.size());
    }
}
