/*******************************************************************************
 * Copyright (c) 2017, 2020 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.grid.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.junit.Test;

public class DefaultColumnHeaderDataProviderTest {

    @Test
    public void shouldReturnColumnLabel() {
        IDataProvider dataProvider =
                new DefaultColumnHeaderDataProvider(new String[] { "One", "Two", "Three" });

        assertEquals(1, dataProvider.getRowCount());
        assertEquals(3, dataProvider.getColumnCount());
        assertEquals("One", dataProvider.getDataValue(0, 0));
        assertEquals("Two", dataProvider.getDataValue(1, 0));
        assertEquals("Three", dataProvider.getDataValue(2, 0));

        // any other row will work too
        assertEquals("One", dataProvider.getDataValue(0, 1));
    }

    @Test
    public void shouldReturnPropertyLabel() {
        String[] properties = { "firstname", "lastname", "gender", "birthday" };
        Map<String, String> mapping = new HashMap<>();
        mapping.put("firstname", "Vorname");
        mapping.put("lastname", "Nachname");
        mapping.put("gender", "Geschlecht");
        mapping.put("birthday", "Geburtstag");

        IDataProvider dataProvider =
                new DefaultColumnHeaderDataProvider(properties, mapping);

        assertEquals(1, dataProvider.getRowCount());
        assertEquals(4, dataProvider.getColumnCount());
        assertEquals("Vorname", dataProvider.getDataValue(0, 0));
        assertEquals("Nachname", dataProvider.getDataValue(1, 0));
        assertEquals("Geschlecht", dataProvider.getDataValue(2, 0));
        assertEquals("Geburtstag", dataProvider.getDataValue(3, 0));

        // any other row will work too
        assertEquals("Vorname", dataProvider.getDataValue(0, 1));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowExceptionOnSet() {
        IDataProvider dataProvider =
                new DefaultColumnHeaderDataProvider(new String[] { "One", "Two", "Three" });
        dataProvider.setDataValue(0, 0, "Foo");
    }

    @Test
    public void shouldReturnNullOnInvalidColumnIndex() {
        String[] properties = { "firstname", "lastname", "gender", "birthday" };
        Map<String, String> mapping = new HashMap<>();
        mapping.put("firstname", "Vorname");
        mapping.put("lastname", "Nachname");
        mapping.put("gender", "Geschlecht");
        mapping.put("birthday", "Geburtstag");

        IDataProvider dataProvider =
                new DefaultColumnHeaderDataProvider(properties, mapping);

        assertNull(dataProvider.getDataValue(-1, 0));
        assertNull(dataProvider.getDataValue(4, 0));
    }
}
