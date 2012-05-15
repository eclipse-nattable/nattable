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
public class KittenDataProviderFixture implements IRowDataProvider<KittenDataProviderFixture.Kitten> {

    private List<Kitten> kittens = new ArrayList<Kitten>();

    private IColumnAccessor<Kitten> columnAccessor = new IColumnAccessor<Kitten>() {

        public int getColumnCount() {
            return 5;
        }

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
                throw new IllegalArgumentException("unknown column " + columnIndex);
        }
        
        public void setDataValue(Kitten rowObj, int columnIndex, Object newValue) {
        	throw new UnsupportedOperationException();
        }
        
    };
    
    private IRowIdAccessor<KittenDataProviderFixture.Kitten> rowIdAccessor =
        new IRowIdAccessor<KittenDataProviderFixture.Kitten>() {
            public Serializable getRowId(Kitten k) {
                return k.getName();
            }
        };
    
    public KittenDataProviderFixture() {
        kittens.add(new Kitten("Tabitha", 2, "Orange", .5, "Grass"));
        kittens.add(new Kitten("Midnighter", 3, "Black", .7, "Tabitha"));
        kittens.add(new Kitten("Lightning", 1, "Black and White", .25, "Wind"));
    }

    public int getColumnCount() {
        return columnAccessor.getColumnCount();
    }

    public int getRowCount() {
        return kittens.size();
    }
    
    public Object getDataValue(int columnIndex, int rowIndex) {
        return columnAccessor.getDataValue(getRowObject(rowIndex), columnIndex);
    }
    
    public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
    	throw new UnsupportedOperationException();
    }
    
    public Kitten getRowObject(int rowIndex) {
        return kittens.get(rowIndex);
    }

	public int indexOfRowObject(Kitten rowObject) {
		return kittens.indexOf(rowObject);
	}
    
    public IColumnAccessor<Kitten> getColumnAccessor() {
        return columnAccessor;
    }
    
    public IRowIdAccessor<Kitten> getRowIdAccessor() {
        return rowIdAccessor;
    }
    
    public static class Kitten {

        private String name;

        private int age;

        private String color;
        
        private double weight;
        
        private String favoriteToy;

        public Kitten(String name, int age, String color, double weight, String favoriteToy) {
            if (name == null || color == null || favoriteToy == null)
                throw new IllegalArgumentException("null");
            this.name = name;
            this.age = age;
            this.color = color;
            this.weight = weight;
            this.favoriteToy = favoriteToy;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public String getColor() {
            return color;
        }
        
        public double getWeight() {
            return weight;
        }
        
        public String getFavoriteToy() {
            return favoriteToy;
        }
    }

}
