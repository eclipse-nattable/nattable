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
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class Using_the_ListDataProvider extends AbstractNatExample {
	
	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(new Using_the_ListDataProvider());
	}
	
	@Override
	public String getDescription() {
		return
				"NatTable provides a useful class that implements the common case of implementing an IDataProvider based on a List. This " +
				"example shows how to use it.\n" +
				"\n" +
				"In this case the ListDataProvider is given an array of property names and uses reflection to retrieve the property " +
				"values for each column. The ListDataProvider can also be given an instance of an IColumnPropertyAccessor to enable it to " +
				"retrieve column values from a row object.";
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
		
		IColumnPropertyAccessor<Person> columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<Person>(propertyNames);
		IDataProvider listDataProvider = new ListDataProvider<Person>(myList, columnPropertyAccessor);
		ILayer layer = new DataLayer(listDataProvider);
		
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
