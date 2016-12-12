/*******************************************************************************
 * Copyright (c) 2013, 2015 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._300_Data;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.person.DataModelConstants;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonWithAddress;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Example showing how to implement and use a custom IColumnPropertyAccessor.
 */
public class _302_CustomColumnPropertyAccessorExample extends
        AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 400, new _302_CustomColumnPropertyAccessorExample());
    }

    @Override
    public String getDescription() {
        return "This is an example to show how to implement a custom IColumnPropertyAccessor.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        IColumnPropertyAccessor<PersonWithAddress> columnPropertyAccessor =
                new PersonWithAddressColumnPropertyAccessor();

        IDataProvider bodyDataProvider =
                new ListDataProvider<>(
                        PersonService.getPersonsWithAddress(10),
                        columnPropertyAccessor);
        final DataLayer bodyDataLayer =
                new DataLayer(bodyDataProvider);
        final SelectionLayer selectionLayer =
                new SelectionLayer(bodyDataLayer);
        ViewportLayer viewportLayer =
                new ViewportLayer(selectionLayer);

        ILayer columnHeaderLayer =
                new ColumnHeaderLayer(
                        new DataLayer(createColumnHeaderDataProvider()),
                        viewportLayer,
                        selectionLayer);

        // set the region labels to make default configurations work, e.g.
        // selection
        CompositeLayer compositeLayer = new CompositeLayer(1, 2);
        compositeLayer.setChildLayer(GridRegion.COLUMN_HEADER, columnHeaderLayer, 0, 0);
        compositeLayer.setChildLayer(GridRegion.BODY, viewportLayer, 0, 1);

        return new NatTable(parent, compositeLayer);
    }

    /**
     * Creates the {@link IDataProvider} for the column header of this
     * {@link GridLayer}. Should always return the same column count and values
     * for all columns that are defined within the {@link IDataProvider} of the
     * body layer stack. Uses the {@link DefaultColumnHeaderDataProvider} which
     * simply checks for the property name within the propertyNames array and
     * returns the corresponding value out of the propertyToLabelMap. Another
     * approach is to implement a completely new {@link IDataProvider}
     */
    protected IDataProvider createColumnHeaderDataProvider() {
        String[] propertyNames = {
                "firstName",
                "lastName",
                "gender",
                "married",
                "birthday",
                "street",
                "housenumber",
                "postalCode",
                "city"
        };

        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put(DataModelConstants.FIRSTNAME_PROPERTYNAME, "Firstname");
        propertyToLabelMap.put(DataModelConstants.LASTNAME_PROPERTYNAME, "Lastname");
        propertyToLabelMap.put(DataModelConstants.GENDER_PROPERTYNAME, "Gender");
        propertyToLabelMap.put(DataModelConstants.MARRIED_PROPERTYNAME, "Married");
        propertyToLabelMap.put(DataModelConstants.BIRTHDAY_PROPERTYNAME, "Birthday");
        propertyToLabelMap.put(DataModelConstants.STREET_PROPERTYNAME, "Street");
        propertyToLabelMap.put(DataModelConstants.HOUSENUMBER_PROPERTYNAME, "Housenumber");
        propertyToLabelMap.put(DataModelConstants.POSTALCODE_PROPERTYNAME, "Postal Code");
        propertyToLabelMap.put(DataModelConstants.CITY_PROPERTYNAME, "City");

        return new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
    }

    /**
     * This is an implementation for a custom IColumnPropertyAccessor to access
     * PersonWithAddress objects in a NatTable. It is used for the
     * ListDataProvider in the body aswell as for the column header labels.
     */
    class PersonWithAddressColumnPropertyAccessor implements IColumnPropertyAccessor<PersonWithAddress> {

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
        public void setDataValue(PersonWithAddress rowObject, int columnIndex,
                Object newValue) {
            switch (columnIndex) {
                case DataModelConstants.FIRSTNAME_COLUMN_POSITION:
                    rowObject.setFirstName((String) newValue);
                    break;
                case DataModelConstants.LASTNAME_COLUMN_POSITION:
                    rowObject.setLastName((String) newValue);
                    break;
                case DataModelConstants.GENDER_COLUMN_POSITION:
                    rowObject.setGender((Gender) newValue);
                    break;
                case DataModelConstants.MARRIED_COLUMN_POSITION:
                    rowObject.setMarried((Boolean) newValue);
                    break;
                case DataModelConstants.BIRTHDAY_COLUMN_POSITION:
                    rowObject.setBirthday((Date) newValue);
                    break;
                case DataModelConstants.STREET_COLUMN_POSITION:
                    rowObject.getAddress().setStreet((String) newValue);
                    break;
                case DataModelConstants.HOUSENUMBER_COLUMN_POSITION:
                    rowObject.getAddress().setHousenumber((Integer) newValue);
                    break;
                case DataModelConstants.POSTALCODE_COLUMN_POSITION:
                    rowObject.getAddress().setPostalCode((Integer) newValue);
                    break;
                case DataModelConstants.CITY_COLUMN_POSITION:
                    rowObject.getAddress().setCity((String) newValue);
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
}
