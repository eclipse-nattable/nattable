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
package org.eclipse.nebula.widgets.nattable.examples.examples._101_Data;

import java.util.ArrayList;
import java.util.List;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class Derived_or_computed_column_data extends AbstractNatExample {
	
	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(new Derived_or_computed_column_data());
	}
	
	@Override
	public String getDescription() {
		return "This example shows how to add a derived fullName column which is computed by concatenating the firstName and lastName column values";
	}
	
	public Control createExampleControl(Composite parent) {
		List<Person> myList = new ArrayList<Person>();
		myList.add(new Person("Homer", "Simpson", "Sargeant", 1234567890L));
		myList.add(new Person("Waylon", "Smithers", "Admiral", 6666666666L));
		myList.add(new Person("Bart", "Smithers", "General", 9125798342L));
		myList.add(new Person("Nelson", "Muntz", "Private", 0000000001L));
		myList.add(new Person("John", "Frink", "Lieutenant", 3141592654L));
		
		String[] propertyNames = {
				"firstName",
				"lastName",
				"rank",
				"serialNumber"
		};
		
		final IColumnPropertyAccessor<Person> columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<Person>(propertyNames);
		
		// Add derived 'fullName' column
		final IColumnPropertyAccessor<Person> derivedColumnPropertyAccessor = new IColumnPropertyAccessor<Person>() {

			public Object getDataValue(Person rowObject, int columnIndex) {
				if (columnIndex < columnPropertyAccessor.getColumnCount()) {
					return columnPropertyAccessor.getDataValue(rowObject, columnIndex);
				} else if (columnIndex == columnPropertyAccessor.getColumnCount()) {
					return columnPropertyAccessor.getDataValue(rowObject, 0) + " " + columnPropertyAccessor.getDataValue(rowObject, 1);
				} else {
					return null;
				}
			}

			public void setDataValue(Person rowObject, int columnIndex, Object newValue) {
				columnPropertyAccessor.setDataValue(rowObject, columnIndex, newValue);
			}

			public int getColumnCount() {
				return columnPropertyAccessor.getColumnCount() + 1;
			}

			public String getColumnProperty(int columnIndex) {
				if (columnIndex < columnPropertyAccessor.getColumnCount()) {
					return columnPropertyAccessor.getColumnProperty(columnIndex);
				} else if (columnIndex == columnPropertyAccessor.getColumnCount()) {
					return "fullName";
				} else {
					return null;
				}
			}

			public int getColumnIndex(String propertyName) {
				if ("fullName".equals(propertyName)) {
					return columnPropertyAccessor.getColumnCount() + 1;
				} else {
					return columnPropertyAccessor.getColumnIndex(propertyName);
				}
			}
		};
		
		final IDataProvider listDataProvider = new ListDataProvider<Person>(myList, derivedColumnPropertyAccessor);
		
		// Column header data provider includes derived properties
		IDataProvider columnHeaderDataProvider = new IDataProvider() {

			public Object getDataValue(int columnIndex, int rowIndex) {
				return derivedColumnPropertyAccessor.getColumnProperty(columnIndex);
			}

			public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
				// noop
			}

			public int getColumnCount() {
				return derivedColumnPropertyAccessor.getColumnCount();
			}

			public int getRowCount() {
				return 1;
			}
			
		};
		
		ILayer layer = new DefaultGridLayer(listDataProvider, columnHeaderDataProvider);
		
		return new NatTable(parent, layer);
	}
	
	public class Person {
		private String firstName;
		private String lastName;
		private String rank;
		private long serialNumber;
		
		public Person(String firstName, String lastName, String rank, long serialNumber) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.rank = rank;
			this.serialNumber = serialNumber;
		}
		
		public String getFirstName() {
			return firstName;
		}
		public String getLastName() {
			return lastName;
		}
		public String getRank() {
			return rank;
		}
		public long getSerialNumber() {
			return serialNumber;
		}
	}
	
}
