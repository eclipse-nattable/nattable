/*******************************************************************************
 * Copyright (c) 2019, 2024 Dirk Fauth and others.
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
package org.eclipse.nebula.widgets.nattable.filterrow.combobox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.sort.SortConfigAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FilterRowComboBoxDataProviderTest {

    private List<MyRowObject> persons = getObjects();
    private FilterRowComboBoxDataProvider<MyRowObject> provider;
    private String[] propertyNames = new String[] {
            "firstName",
            "lastName",
            "married",
            "birthday",
            "city" };
    private IColumnAccessor<MyRowObject> bodyDataColumnAccessor =
            new ReflectiveColumnPropertyAccessor<>(this.propertyNames);

    @BeforeEach
    public void setup() {

        DataLayer dataLayer = new DataLayer(
                new ListDataProvider<>(this.persons, this.bodyDataColumnAccessor));

        this.provider = new FilterRowComboBoxDataProvider<>(dataLayer, this.persons, this.bodyDataColumnAccessor);
    }

    @Test
    public void shouldCollectUniqueValues() {
        List<?> values = this.provider.collectValues(0);
        assertEquals(8, values.size());

        assertEquals("Bart", values.get(0));
        assertEquals("Homer", values.get(1));
        assertEquals("Lisa", values.get(2));
        assertEquals("Marge", values.get(3));
        assertEquals("Maude", values.get(4));
        assertEquals("Ned", values.get(5));
        assertEquals("Rod", values.get(6));
        assertEquals("Tod", values.get(7));
    }

    @Test
    public void shouldCollectUniqueValuesWithNull() {
        List<?> values = this.provider.collectValues(1);
        assertEquals(3, values.size());

        assertNull(values.get(0));
        assertEquals("Flanders", values.get(1));
        assertEquals("Simpson", values.get(2));
    }

    @Test
    public void shouldCollectUniqueNonComparable() {
        List<?> values = this.provider.collectValues(4);
        assertEquals(5, values.size());

        assertNull(values.get(0));
    }

    @Test
    public void shouldUseConfiguredComparator() {
        // build a fake column header layer
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(this.propertyNames);
        DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);

        // build a fake ConfigRegistry with a comparator that sorts descending
        ConfigRegistry configRegistry = new ConfigRegistry();
        configRegistry.registerConfigAttribute(
                SortConfigAttributes.SORT_COMPARATOR,
                new Comparator<String>() {

                    @Override
                    public int compare(String o1, String o2) {
                        return o2.toString().compareTo(o1.toString());
                    }
                });

        this.provider.configureComparator(columnHeaderDataLayer, configRegistry);

        List<?> values = this.provider.collectValues(0);
        assertEquals(8, values.size());

        assertEquals("Tod", values.get(0));
        assertEquals("Rod", values.get(1));
        assertEquals("Ned", values.get(2));
        assertEquals("Maude", values.get(3));
        assertEquals("Marge", values.get(4));
        assertEquals("Lisa", values.get(5));
        assertEquals("Homer", values.get(6));
        assertEquals("Bart", values.get(7));

        values = this.provider.collectValues(1);
        assertEquals(3, values.size());

        assertNull(values.get(0));
        assertEquals("Simpson", values.get(1));
        assertEquals("Flanders", values.get(2));

    }

    private List<MyRowObject> getObjects() {
        List<MyRowObject> result = new ArrayList<>();

        City city1 = new City(1111, "Springfield");
        City city2 = new City(2222, "Shelbyville");
        City city3 = new City(3333, "Ogdenville");
        City city4 = new City(4444, "Waverly Hills");

        result.add(new MyRowObject(1, "Homer", "Simpson", true, new Date(), city1));
        result.add(new MyRowObject(2, "Homer", "Simpson", true, new Date(), city1));
        result.add(new MyRowObject(3, "Marge", "Simpson", true, new Date(), city1));
        result.add(new MyRowObject(4, "Marge", "Simpson", true, new Date(), city1));
        result.add(new MyRowObject(5, "Marge", "Simpson", true, new Date(), null));
        result.add(new MyRowObject(6, "Ned", null, true, new Date(), city4));
        result.add(new MyRowObject(7, "Maude", null, true, new Date(), city4));

        result.add(new MyRowObject(8, "Homer", "Simpson", true, new Date(), city2));
        result.add(new MyRowObject(9, "Homer", "Simpson", true, new Date(), city2));
        result.add(new MyRowObject(10, "Homer", "Simpson", true, new Date(), city2));
        result.add(new MyRowObject(11, "Bart", "Simpson", false, new Date(), city2));
        result.add(new MyRowObject(12, "Bart", "Simpson", false, new Date(), city2));
        result.add(new MyRowObject(13, "Bart", "Simpson", false, new Date(), city2));
        result.add(new MyRowObject(14, "Marge", "Simpson", true, new Date(), city2));
        result.add(new MyRowObject(15, "Marge", "Simpson", true, new Date(), city2));
        result.add(new MyRowObject(16, "Lisa", "Simpson", false, new Date(), city2));
        result.add(new MyRowObject(17, "Lisa", "Simpson", false, new Date(), city2));

        result.add(new MyRowObject(18, "Ned", "Flanders", true, new Date(), city3));
        result.add(new MyRowObject(19, "Ned", "Flanders", true, new Date(), city3));
        result.add(new MyRowObject(20, "Maude", "Flanders", true, new Date(), city3));
        result.add(new MyRowObject(21, "Maude", "Flanders", true, new Date(), city3));
        result.add(new MyRowObject(22, "Rod", "Flanders", false, new Date(), city3));
        result.add(new MyRowObject(23, "Rod", "Flanders", false, new Date(), city3));
        result.add(new MyRowObject(24, "Tod", "Flanders", false, new Date(), city3));
        result.add(new MyRowObject(25, "Tod", "Flanders", false, new Date(), city3));

        return result;
    }

    public class MyRowObject {

        private final int id;
        private String firstName;
        private String lastName;
        private boolean married;
        private Date birthday;
        private City city;

        public MyRowObject(int id) {
            this.id = id;
        }

        public MyRowObject(
                int id,
                String firstName,
                String lastName,
                boolean married,
                Date birthday,
                City city) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.married = married;
            this.birthday = birthday;
            this.city = city;
        }

        public int getId() {
            return this.id;
        }

        public String getFirstName() {
            return this.firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return this.lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public boolean isMarried() {
            return this.married;
        }

        public void setMarried(boolean married) {
            this.married = married;
        }

        public Date getBirthday() {
            return this.birthday;
        }

        public void setBirthday(Date birthday) {
            this.birthday = birthday;
        }

        public City getCity() {
            return this.city;
        }

        public void setCity(City city) {
            this.city = city;
        }
    }

    static class City {
        final int plz;
        final String name;

        City(int plz, String name) {
            this.plz = plz;
            this.name = name;
        }

        public int getPlz() {
            return this.plz;
        }

        public String getName() {
            return this.name;
        }
    }

}
