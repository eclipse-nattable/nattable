/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._500_Layers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.data.person.PersonWithAddress;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Simple example showing the viewport.
 * 
 * @author Dirk Fauth
 *
 */
public class _511_ViewportExample extends AbstractNatExample {

	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(600, 400, new _511_ViewportExample());
	}

	@Override
	public String getDescription() {
		return "This example shows a simple composition using a viewport.";
	}
	
	@Override
	public Control createExampleControl(Composite parent) {
		//property names of the Person class
		String[] propertyNames = {"firstName", "lastName", "gender", "married", "birthday", 
				"address.street", "address.housenumber", "address.postalCode", "address.city"};

		//mapping from property to label, needed for column header labels
		Map<String, String> propertyToLabelMap = new HashMap<String, String>();
		propertyToLabelMap.put("firstName", "Firstname");
		propertyToLabelMap.put("lastName", "Lastname");
		propertyToLabelMap.put("gender", "Gender");
		propertyToLabelMap.put("married", "Married");
		propertyToLabelMap.put("birthday", "Birthday");
		propertyToLabelMap.put("address.street", "Street");
		propertyToLabelMap.put("address.housenumber", "Housenumber");
		propertyToLabelMap.put("address.postalCode", "Postal Code");
		propertyToLabelMap.put("address.city", "City");

		IColumnPropertyAccessor<PersonWithAddress> columnPropertyAccessor = 
				new ExtendedReflectiveColumnPropertyAccessor<PersonWithAddress>(propertyNames);
		
		IDataProvider bodyDataProvider = new ListDataProvider<PersonWithAddress>(
				PersonService.getPersonsWithAddress(50), columnPropertyAccessor);
		DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
		ViewportLayer viewportLayer = new ViewportLayer(bodyDataLayer);
		
		NatTable natTable = new NatTable(parent, viewportLayer);
		
		return natTable;
	}

}
