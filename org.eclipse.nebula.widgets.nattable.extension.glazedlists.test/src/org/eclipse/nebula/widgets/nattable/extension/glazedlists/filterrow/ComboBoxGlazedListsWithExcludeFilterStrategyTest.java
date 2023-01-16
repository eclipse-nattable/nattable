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
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.Matcher;

public class ComboBoxGlazedListsWithExcludeFilterStrategyTest {

    private FilterList<Person> filterList;

    private ConfigRegistry configRegistry;
    private DataLayerFixture columnHeaderLayer;
    private FilterRowComboBoxDataProvider<Person> comboBoxDataProvider;
    private ComboBoxGlazedListsWithExcludeFilterStrategy<Person> filterStrategy;
    private FilterRowDataProvider<Person> dataProvider;

    private static String[] personPropertyNames = {
            "firstName",
            "lastName",
            "gender",
            "married",
            "birthday" };

    // Homer should never be filtered
    private static Matcher<Person> homerFilter = item -> "Homer".equals(item.getFirstName());

    @BeforeEach
    public void setup() {
        // initialize the collection with a big amount of values
        EventList<Person> baseCollection = GlazedLists.eventList(PersonService.getFixedPersons());
        for (int i = 1; i < 1000; i++) {
            baseCollection.addAll(PersonService.getFixedPersons());
        }
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

        this.filterStrategy = new ComboBoxGlazedListsWithExcludeFilterStrategy<>(
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

    @Test
    public void shouldFilterForFlanders() {
        assertEquals(18000, this.filterList.size());

        this.dataProvider.setDataValue(1, 1, Arrays.asList("Flanders"));

        assertEquals(8000, this.filterList.size());
    }

    // with exclude filter

    @Test
    public void shouldFilterForFlandersWithHomerExcludeFilter() {
        this.filterStrategy.addExcludeFilter(homerFilter);
        assertEquals(18000, this.filterList.size());

        this.dataProvider.setDataValue(1, 1, Arrays.asList("Flanders"));
        // we now expect 8000 Flanders and the 3000 Homers that are excluded
        // from filtering
        assertEquals(11000, this.filterList.size());

        this.filterStrategy.removeExcludeFilter(homerFilter);
        // if Homer is not excluded anymore, only Flanders should be there
        assertEquals(8000, this.filterList.size());
    }

    @Test
    public void shouldFilterForFlandersWithHomerExcludeFilterAndClear() {
        this.filterStrategy.addExcludeFilter(homerFilter);
        assertEquals(18000, this.filterList.size());

        this.dataProvider.setDataValue(1, 1, Arrays.asList("Flanders"));
        // we now expect 8000 Flanders and the 3000 Homers that are excluded
        // from filtering
        assertEquals(11000, this.filterList.size());

        this.filterStrategy.clearExcludeFilter();
        // if Homer is not excluded anymore, only Flanders should be there
        assertEquals(8000, this.filterList.size());
    }

    @Test
    public void shouldFilterForNedFlandersWithHomerExcludeFilter() {
        this.filterStrategy.addExcludeFilter(homerFilter);
        assertEquals(18000, this.filterList.size());

        this.dataProvider.setDataValue(0, 1, Arrays.asList("Ned"));
        this.dataProvider.setDataValue(1, 1, Arrays.asList("Flanders"));
        // we now expect 2000 Ned Flanders and the 3000 Homers that are excluded
        // from filtering
        assertEquals(5000, this.filterList.size());

        this.filterStrategy.clearExcludeFilter();
        // if Homer is not excluded anymore, only Ned Flanders should be there
        assertEquals(2000, this.filterList.size());
    }

}
