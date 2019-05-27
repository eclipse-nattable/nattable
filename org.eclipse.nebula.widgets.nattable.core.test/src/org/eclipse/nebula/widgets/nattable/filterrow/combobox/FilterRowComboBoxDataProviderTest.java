/*******************************************************************************
 * Copyright (c) 2019 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.filterrow.combobox;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.junit.Before;
import org.junit.Test;

public class FilterRowComboBoxDataProviderTest {

    private List<MyRowObject> persons = getObjects();
    private FilterRowComboBoxDataProvider<MyRowObject> provider;
    private IColumnAccessor<MyRowObject> bodyDataColumnAccessor =
            new ReflectiveColumnPropertyAccessor<>(new String[] {
                    "firstName",
                    "lastName",
                    "married",
                    "birthday",
                    "city" });

    @Before
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
    public void shouldCollectUniqueValuesWithNull8() {
        List<?> values = collectWithStream(1);
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
    public void shouldCollectUniqueNonComparable8() {
        List<?> values = collectWithStream(4);
        assertEquals(5, values.size());

        assertNull(values.get(0));
    }

    @Test
    public void shouldBenchmark() {
        for (int i = 0; i < 9999; i++) {
            this.persons.addAll(getObjects());
        }

        assertEquals(250000, this.persons.size());

        long start1 = System.currentTimeMillis();
        List<?> values = this.provider.collectValues(1);
        long end1 = System.currentTimeMillis();

        long start2 = System.currentTimeMillis();
        List<?> values2 = collectWithStream(1);
        long end2 = System.currentTimeMillis();

        assertEquals(3, values.size());
        assertNull(values.get(0));
        assertEquals("Flanders", values.get(1));
        assertEquals("Simpson", values.get(2));

        assertEquals(3, values2.size());
        assertNull(values2.get(0));
        assertEquals("Flanders", values2.get(1));
        assertEquals("Simpson", values2.get(2));

        System.out.println("HashSet: " + (end1 - start1) + "ms");
        System.out.println("Stream: " + (end2 - start2) + "ms");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private List<?> collectWithStream(int columnIndex) {
        List result = this.persons.stream()
                .unordered()
                .parallel()
                .map(x -> this.bodyDataColumnAccessor.getDataValue(x, columnIndex))
                .distinct()
                .collect(Collectors.toList());

        Object firstNonNull = result.stream().filter(Objects::nonNull).findFirst().orElse(null);
        if (firstNonNull instanceof Comparable) {
            result.sort(nullsFirst(naturalOrder()));
        } else {
            // always ensure that null is at the first position
            int index = result.indexOf(null);
            if (index >= 0) {
                result.remove(index);
                result.add(0, null);
            }
        }

        return result;
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
