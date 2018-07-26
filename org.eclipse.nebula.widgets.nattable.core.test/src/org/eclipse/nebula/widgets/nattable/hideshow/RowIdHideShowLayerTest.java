/*******************************************************************************
 * Copyright (c) 2018 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultIntegerDisplayConverter;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.hideshow.indicator.HideIndicatorConstants;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.sort.command.SortColumnCommand;
import org.junit.Before;
import org.junit.Test;

public class RowIdHideShowLayerTest {

    private IRowDataProvider<Person> bodyDataProvider =
            new ListDataProvider<>(
                    PersonService.getPersons(5),
                    new ReflectiveColumnPropertyAccessor<Person>(
                            new String[] { "firstName", "lastName", "gender", "married", "birthday" }));
    private DataLayer bodyDataLayer;
    private RowIdHideShowLayer<Person> rowHideShowLayer;

    @Before
    public void setup() {
        this.bodyDataLayer = new DataLayer(this.bodyDataProvider);
        this.rowHideShowLayer = new RowIdHideShowLayer<>(this.bodyDataLayer, this.bodyDataProvider, new IRowIdAccessor<Person>() {
            @Override
            public Serializable getRowId(Person rowObject) {
                return rowObject.getId();
            }
        });
    }

    @Test
    public void getRowIndexHideRow() {
        this.rowHideShowLayer.hideRowPositions(Arrays.asList(1));

        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(4));
    }

    @Test
    public void getRowPositionForASingleHiddenRow() {
        this.rowHideShowLayer.hideRowPositions(Arrays.asList(2, 4));

        assertEquals(0, this.rowHideShowLayer.getRowPositionByIndex(0));
        assertEquals(1, this.rowHideShowLayer.getRowPositionByIndex(1));
        assertEquals(-1, this.rowHideShowLayer.getRowPositionByIndex(2));
        assertEquals(2, this.rowHideShowLayer.getRowPositionByIndex(3));
        assertEquals(-1, this.rowHideShowLayer.getRowPositionByIndex(4));
        assertEquals(-1, this.rowHideShowLayer.getRowPositionByIndex(5));
    }

    @Test
    public void hideAllRows() {
        this.rowHideShowLayer.hideRowPositions(Arrays.asList(0, 1, 2, 3, 4));

        assertEquals(0, this.rowHideShowLayer.getRowCount());
    }

    @Test
    public void hideAllRows2() {
        List<Integer> rowPositions = Arrays.asList(0);
        this.rowHideShowLayer.hideRowPositions(rowPositions);
        this.rowHideShowLayer.hideRowPositions(rowPositions);
        this.rowHideShowLayer.hideRowPositions(rowPositions);
        this.rowHideShowLayer.hideRowPositions(rowPositions);
        this.rowHideShowLayer.hideRowPositions(rowPositions);
        assertEquals(0, this.rowHideShowLayer.getRowCount());
    }

    @Test
    public void showARow() {
        assertEquals(5, this.rowHideShowLayer.getRowCount());

        List<Integer> rowPositions = Arrays.asList(2);
        this.rowHideShowLayer.hideRowPositions(rowPositions); // index = 2
        rowPositions = Arrays.asList(0);
        this.rowHideShowLayer.hideRowPositions(rowPositions); // index = 4
        assertEquals(3, this.rowHideShowLayer.getRowCount());
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(3));

        this.rowHideShowLayer.showRowIndexes(Arrays.asList(0));
        assertEquals(4, this.rowHideShowLayer.getRowCount());
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(4));

        this.rowHideShowLayer.showRowIndexes(Arrays.asList(2));
        assertEquals(5, this.rowHideShowLayer.getRowCount());
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(5));
    }

    @Test
    public void showAllRows() {
        assertEquals(5, this.rowHideShowLayer.getRowCount());

        this.rowHideShowLayer.hideRowPositions(Arrays.asList(0));
        assertEquals(4, this.rowHideShowLayer.getRowCount());
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(4));

        this.rowHideShowLayer.showAllRows();
        assertEquals(5, this.rowHideShowLayer.getRowCount());
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(4));
    }

    @Test
    public void showRowPositions() {
        this.bodyDataProvider =
                new ListDataProvider<>(
                        PersonService.getPersons(10),
                        new ReflectiveColumnPropertyAccessor<Person>(
                                new String[] { "firstName", "lastName", "gender", "married", "birthday" }));
        this.bodyDataLayer = new DataLayer(this.bodyDataProvider);
        this.rowHideShowLayer = new RowIdHideShowLayer<>(this.bodyDataLayer, this.bodyDataProvider, new IRowIdAccessor<Person>() {
            @Override
            public Serializable getRowId(Person rowObject) {
                return rowObject.getId();
            }
        });

        assertEquals(10, this.rowHideShowLayer.getRowCount());

        this.rowHideShowLayer.hideRowPositions(Arrays.asList(3, 4, 5));
        assertEquals(7, this.rowHideShowLayer.getRowCount());
        assertEquals(-1, this.rowHideShowLayer.getRowPositionByIndex(3));
        assertEquals(-1, this.rowHideShowLayer.getRowPositionByIndex(4));

        this.rowHideShowLayer.showRowIndexes(Arrays.asList(3, 4));
        assertEquals(9, this.rowHideShowLayer.getRowCount());
        assertEquals(3, this.rowHideShowLayer.getRowPositionByIndex(3));
        assertEquals(4, this.rowHideShowLayer.getRowPositionByIndex(4));

    }

    @Test
    public void shouldContainHideIndicatorLabels() {
        assertEquals(5, this.rowHideShowLayer.getRowCount());

        this.rowHideShowLayer.hideRowPositions(Arrays.asList(0));

        LabelStack configLabels = this.rowHideShowLayer.getConfigLabelsByPosition(0, 0);
        assertTrue(configLabels.hasLabel(HideIndicatorConstants.ROW_TOP_HIDDEN));
        assertFalse(configLabels.hasLabel(HideIndicatorConstants.ROW_BOTTOM_HIDDEN));

        this.rowHideShowLayer.hideRowPositions(Arrays.asList(1));

        configLabels = this.rowHideShowLayer.getConfigLabelsByPosition(0, 0);
        assertTrue(configLabels.hasLabel(HideIndicatorConstants.ROW_TOP_HIDDEN));
        assertTrue(configLabels.hasLabel(HideIndicatorConstants.ROW_BOTTOM_HIDDEN));

        this.rowHideShowLayer.showRowIndexes(Arrays.asList(0));

        configLabels = this.rowHideShowLayer.getConfigLabelsByPosition(0, 1);
        assertFalse(configLabels.hasLabel(HideIndicatorConstants.ROW_TOP_HIDDEN));
        assertTrue(configLabels.hasLabel(HideIndicatorConstants.ROW_BOTTOM_HIDDEN));
    }

    @Test
    public void shouldKeepHiddenOnSort() {
        List<Person> persons = PersonService.getPersons(10);
        this.bodyDataProvider =
                new ListDataProvider<>(
                        persons,
                        new ReflectiveColumnPropertyAccessor<Person>(
                                new String[] { "firstName", "lastName", "gender", "married", "birthday" }));
        this.bodyDataLayer = new DataLayer(this.bodyDataProvider);
        this.rowHideShowLayer = new RowIdHideShowLayer<>(this.bodyDataLayer, this.bodyDataProvider, new IRowIdAccessor<Person>() {
            @Override
            public Serializable getRowId(Person rowObject) {
                return rowObject.getId();
            }
        });

        assertEquals(10, this.rowHideShowLayer.getRowCount());

        this.bodyDataProvider.setDataValue(1, 4, "Zoolander");

        this.rowHideShowLayer.hideRowPositions(Arrays.asList(4));

        assertEquals(9, this.rowHideShowLayer.getRowCount());
        assertEquals(-1, this.rowHideShowLayer.getRowPositionByIndex(4));

        // trigger sorting
        Collections.sort(persons, new Comparator<Person>() {
            @Override
            public int compare(Person o1, Person o2) {
                return o1.getLastName().compareTo(o2.getLastName());
            }
        });
        assertEquals("Zoolander", this.bodyDataProvider.getDataValue(1, 9));

        // trigger invalidate cache
        this.rowHideShowLayer.doCommand(new SortColumnCommand(this.rowHideShowLayer, 1));

        assertEquals(4, this.rowHideShowLayer.getRowPositionByIndex(4));
        assertEquals(-1, this.rowHideShowLayer.getRowPositionByIndex(9));

        // trigger sorting
        Collections.sort(persons, new Comparator<Person>() {
            @Override
            public int compare(Person o1, Person o2) {
                return o2.getLastName().compareTo(o1.getLastName());
            }
        });
        assertEquals("Zoolander", this.bodyDataProvider.getDataValue(1, 0));

        // trigger invalidate cache
        this.rowHideShowLayer.doCommand(new SortColumnCommand(this.rowHideShowLayer, 1));

        assertEquals(8, this.rowHideShowLayer.getRowPositionByIndex(9));
        assertEquals(-1, this.rowHideShowLayer.getRowPositionByIndex(0));
    }

    @Test
    public void shouldSaveState() {
        this.rowHideShowLayer.hideRowPositions(Arrays.asList(new Integer[] { 2, 4 }));

        Properties properties = new Properties();
        this.rowHideShowLayer.saveState("prefix", properties);

        assertEquals(
                "2,4,",
                properties.getProperty("prefix" + RowIdHideShowLayer.PERSISTENCE_KEY_HIDDEN_ROW_IDS));

    }

    @Test
    public void shouldLoadState() {
        Properties properties = new Properties();
        properties.setProperty("prefix"
                + RowIdHideShowLayer.PERSISTENCE_KEY_HIDDEN_ROW_IDS,
                "2,4,");

        this.rowHideShowLayer.setIdConverter(new DefaultIntegerDisplayConverter());
        this.rowHideShowLayer.loadState("prefix", properties);

        assertEquals(3, this.rowHideShowLayer.getRowCount());

        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(3));

        assertEquals(0, this.rowHideShowLayer.getRowPositionByIndex(0));
        assertEquals(1, this.rowHideShowLayer.getRowPositionByIndex(1));
        assertEquals(-1, this.rowHideShowLayer.getRowPositionByIndex(2));
        assertEquals(2, this.rowHideShowLayer.getRowPositionByIndex(3));
        assertEquals(-1, this.rowHideShowLayer.getRowPositionByIndex(4));
    }
}
