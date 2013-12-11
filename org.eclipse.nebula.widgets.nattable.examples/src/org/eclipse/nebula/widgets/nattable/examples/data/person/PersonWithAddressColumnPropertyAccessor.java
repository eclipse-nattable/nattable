/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.data.person;

import java.util.Arrays;
import java.util.Date;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.examples.data.person.Person.Gender;


public class PersonWithAddressColumnPropertyAccessor implements IColumnPropertyAccessor<PersonWithAddress> {

	@Override
	public Object getDataValue(PersonWithAddress rowObject, int columnIndex) {
		switch (columnIndex) {
			case DataModelConstants.FIRSTNAME_COLUMN_POSITION:
				return rowObject.getFirstName();
			case DataModelConstants.LASTNAME_COLUMN_POSITION:
				return rowObject.getLastName();
			case DataModelConstants.GENDER_COLUMN_POSITION:
				return rowObject.getGender();
			case DataModelConstants.MARRIED_COLUMN_POSITION:
				return rowObject.isMarried();
			case DataModelConstants.BIRTHDAY_COLUMN_POSITION:
				return rowObject.getBirthday();
			case DataModelConstants.STREET_COLUMN_POSITION:
				return rowObject.getAddress().getStreet();
			case DataModelConstants.HOUSENUMBER_COLUMN_POSITION:
				return rowObject.getAddress().getHousenumber();
			case DataModelConstants.POSTALCODE_COLUMN_POSITION:
				return rowObject.getAddress().getPostalCode();
			case DataModelConstants.CITY_COLUMN_POSITION:
				return rowObject.getAddress().getCity();
		}
		return "";
	}

	/**
	 * Very simple implementation without any type checks. 
	 */
	@Override
	public void setDataValue(PersonWithAddress rowObject, int columnIndex, Object newValue) {
		switch (columnIndex) {
			case DataModelConstants.FIRSTNAME_COLUMN_POSITION:
				rowObject.setFirstName((String)newValue);
				break;
			case DataModelConstants.LASTNAME_COLUMN_POSITION:
				rowObject.setLastName((String)newValue);
				break;
			case DataModelConstants.GENDER_COLUMN_POSITION:
				rowObject.setGender((Gender)newValue);
				break;
			case DataModelConstants.MARRIED_COLUMN_POSITION:
				rowObject.setMarried((Boolean)newValue);
				break;
			case DataModelConstants.BIRTHDAY_COLUMN_POSITION:
				rowObject.setBirthday((Date)newValue);
				break;
			case DataModelConstants.STREET_COLUMN_POSITION:
				rowObject.getAddress().setStreet((String)newValue);
				break;
			case DataModelConstants.HOUSENUMBER_COLUMN_POSITION:
				rowObject.getAddress().setHousenumber((Integer)newValue);
				break;
			case DataModelConstants.POSTALCODE_COLUMN_POSITION:
				rowObject.getAddress().setPostalCode((Integer)newValue);
				break;
			case DataModelConstants.CITY_COLUMN_POSITION:
				rowObject.getAddress().setCity((String)newValue);
				break;
		}
	}

	@Override
	public int getColumnCount() {
		return DataModelConstants.PERSONWITHADDRESS_NUMBER_OF_COLUMNS;
	}

	@Override
	public String getColumnProperty(int columnIndex) {
		return DataModelConstants.PERSONWITHADDRESS_PROPERTY_NAMES[columnIndex];
	}

	@Override
	public int getColumnIndex(String propertyName) {
		return Arrays.asList(DataModelConstants.PERSONWITHADDRESS_PROPERTY_NAMES).indexOf(propertyName);
	}

}
