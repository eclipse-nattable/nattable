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


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class Using_a_custom_DataProvider extends AbstractNatExample {
	
	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(new Using_a_custom_DataProvider());
	}
	
	@Override
	public String getDescription() {
		return
				"One of the first things you will want to do is provide your own data for your NatTable instance. This is done by " +
				"implementing an IDataProvider and configuring your NatTable layer with it.\n" +
				"\n" +
				"This example shows a basic data layer that is backed by a custom data provider that names cells by column letter and " +
				"row number.";
	}
	
	public Control createExampleControl(Composite parent) {
		IDataProvider myDataProvider = new IDataProvider() {

			public int getColumnCount() {
				return 26;
			}

			public int getRowCount() {
				return 10;
			}

			public Object getDataValue(int columnIndex, int rowIndex) {
				String columnLetter = String.valueOf((char) ('A' + columnIndex));
				String rowNumber = String.valueOf(rowIndex + 1);
				return columnLetter + rowNumber;
			}

			public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
				// Do nothing
			}
			
		};
		
		ILayer layer = new DataLayer(myDataProvider);
		
		return new NatTable(parent, layer);
	}
	
}
