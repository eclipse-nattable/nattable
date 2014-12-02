/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.test.fixture.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;

/**
 * DataProvider that provides for real row objects.
 */
public class KittenDataProviderFixture implements
        IRowDataProvider<KittenDataProviderFixture.Kitten> {

    private List<Kitten> kittens = new ArrayList<Kitten>();

    private IColumnAccessor<Kitten> columnAccessor = new IColumnAccessor<Kitten>() {

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        @SuppressWarnings("boxing")
        public Object getDataValue(Kitten k, int columnIndex) {
            if (columnIndex == 0)
                return k.getName();
            else if (columnIndex == 1)
                return k.getAge();
            else if (columnIndex == 2)
                return k.getColor();
            else if (columnIndex == 3)
                return k.getWeight();
            else if (columnIndex == 4)
                return k.getFavoriteToy();
            else
                throw new IllegalArgumentException("unknown column "
                        + columnIndex);
        }

        @Override
        public void setDataValue(Kitten rowObj, int columnIndex, Object newValue) {
            throw new UnsupportedOperationException();
        }

    };

    private IRowIdAccessor<KittenDataProviderFixture.Kitten> rowIdAccessor = new IRowIdAccessor<KittenDataProviderFixture.Kitten>() {
        @Override
        public Serializable getRowId(Kitten k) {
            return k.getName();
        }
    };

    public KittenDataProviderFixture() {
        this.kittens.add(new Kitten("Tabitha", 2, "Orange", .5, "Grass"));
        this.kittens.add(new Kitten("Midnighter", 3, "Black", .7, "Tabitha"));
        this.kittens.add(new Kitten("Lightning", 1, "Black and White", .25, "Wind"));
    }

    @Override
    public int getColumnCount() {
        return this.columnAccessor.getColumnCount();
    }

    @Override
    public int getRowCount() {
        return this.kittens.size();
    }

    @Override
    public Object getDataValue(int columnIndex, int rowIndex) {
        return this.columnAccessor.getDataValue(getRowObject(rowIndex), columnIndex);
    }

    @Override
    public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Kitten getRowObject(int rowIndex) {
        return this.kittens.get(rowIndex);
    }

    @Override
    public int indexOfRowObject(Kitten rowObject) {
        return this.kittens.indexOf(rowObject);
    }

    public IColumnAccessor<Kitten> getColumnAccessor() {
        return this.columnAccessor;
    }

    public IRowIdAccessor<Kitten> getRowIdAccessor() {
        return this.rowIdAccessor;
    }

    public static class Kitten {

        private String name;

        private int age;

        private String color;

        private double weight;

        private String favoriteToy;

        public Kitten(String name, int age, String color, double weight,
                String favoriteToy) {
            if (name == null || color == null || favoriteToy == null)
                throw new IllegalArgumentException("null");
            this.name = name;
            this.age = age;
            this.color = color;
            this.weight = weight;
            this.favoriteToy = favoriteToy;
        }

        public String getName() {
            return this.name;
        }

        public int getAge() {
            return this.age;
        }

        public String getColor() {
            return this.color;
        }

        public double getWeight() {
            return this.weight;
        }

        public String getFavoriteToy() {
            return this.favoriteToy;
        }
    }

}
