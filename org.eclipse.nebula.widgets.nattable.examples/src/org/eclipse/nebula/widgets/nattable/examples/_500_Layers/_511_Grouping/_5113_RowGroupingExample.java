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
package org.eclipse.nebula.widgets.nattable.examples._500_Layers._511_Grouping;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.person.Person;
import org.eclipse.nebula.widgets.nattable.examples.data.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.RowGroupExpandCollapseLayer;
import org.eclipse.nebula.widgets.nattable.group.RowGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.model.RowGroup;
import org.eclipse.nebula.widgets.nattable.group.model.RowGroupModel;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultRowHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.theme.ModernNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class _5113_RowGroupingExample extends AbstractNatExample {

	public static void main(String[] args) {
		StandaloneNatExampleRunner.run(800, 400, new _5113_RowGroupingExample());
	}
	
	@Override
	public String getDescription() {
		return
				"This example demonstrates row grouping functionality:\n" +
				"\n" +
				"* EXPAND/COLLAPSE A ROW GROUP by double-clicking on the row group header.";
	}

	@Override
	public Control createExampleControl(Composite parent) {
		// Body

		//property names of the Person class
		String[] propertyNames = {"firstName", "lastName", "gender", "married", "birthday"};

		//mapping from property to label, needed for column header labels
		Map<String, String> propertyToLabelMap = new HashMap<String, String>();
		propertyToLabelMap.put("firstName", "Firstname");
		propertyToLabelMap.put("lastName", "Lastname");
		propertyToLabelMap.put("gender", "Gender");
		propertyToLabelMap.put("married", "Married");
		propertyToLabelMap.put("birthday", "Birthday");

		//build the body layer stack 
		//Usually you would create a new layer stack by extending AbstractIndexLayerTransform and
		//setting the ViewportLayer as underlying layer. But in this case using the ViewportLayer
		//directly as body layer is also working.
		IRowDataProvider<Person> bodyDataProvider = new DefaultBodyDataProvider<Person>(getStaticPersonList(), propertyNames);
		DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
		
		ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(bodyDataLayer);
		ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);
		
		RowHideShowLayer rowHideShowLayer = new RowHideShowLayer(columnHideShowLayer);		
		RowGroupModel<Person> rowGroupModel = new RowGroupModel<Person>();
		rowGroupModel.setDataProvider(bodyDataProvider);
		RowGroupExpandCollapseLayer<Person> rowExpandCollapseLayer = new RowGroupExpandCollapseLayer<Person>(rowHideShowLayer, rowGroupModel);
		
		SelectionLayer selectionLayer = new SelectionLayer(rowExpandCollapseLayer);
		ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);
		
		// Column header

		DefaultColumnHeaderDataProvider defaultColumnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
		DefaultColumnHeaderDataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(defaultColumnHeaderDataProvider);
		ColumnHeaderLayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, viewportLayer, selectionLayer);
		
		// Row header
		
		DefaultRowHeaderDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
		DefaultRowHeaderDataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
		
		RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, viewportLayer, selectionLayer, false);
		rowHeaderLayer.addConfiguration(new RowHeaderConfiguration());
		
		RowGroupHeaderLayer<Person> rowGroupHeaderLayer = new RowGroupHeaderLayer<Person>(rowHeaderLayer, selectionLayer, rowGroupModel);
		rowGroupHeaderLayer.setColumnWidth(20);
		
		// Create a group of rows for the model.
		RowGroup<Person> rowGroup = new RowGroup<Person>(rowGroupModel, "Simpson", false);		
		rowGroup.addMemberRow(bodyDataProvider.getRowObject(0));
		rowGroup.addStaticMemberRow(bodyDataProvider.getRowObject(1));
		rowGroup.addMemberRow(bodyDataProvider.getRowObject(2));
		rowGroup.addMemberRow(bodyDataProvider.getRowObject(3));
		rowGroup.addMemberRow(bodyDataProvider.getRowObject(4));
		rowGroupModel.addRowGroup(rowGroup);
		
		rowGroup = new RowGroup<Person>(rowGroupModel, "Flanders", false);		
		rowGroup.addMemberRow(bodyDataProvider.getRowObject(5));
		rowGroup.addStaticMemberRow(bodyDataProvider.getRowObject(6));
		rowGroup.addMemberRow(bodyDataProvider.getRowObject(7));
		rowGroup.addMemberRow(bodyDataProvider.getRowObject(8));
		rowGroupModel.addRowGroup(rowGroup);
		
		rowGroup = new RowGroup<Person>(rowGroupModel, "Lovejoy", true);		
		rowGroup.addMemberRow(bodyDataProvider.getRowObject(9));
		rowGroup.addStaticMemberRow(bodyDataProvider.getRowObject(10));
		rowGroup.addMemberRow(bodyDataProvider.getRowObject(11));
		rowGroupModel.addRowGroup(rowGroup);
		
		// Corner
		final DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider(defaultColumnHeaderDataProvider, rowHeaderDataProvider);
		DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
		ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowGroupHeaderLayer, columnHeaderLayer);

		// Grid
		GridLayer gridLayer = new GridLayer(
				viewportLayer,
				columnHeaderLayer,
				rowGroupHeaderLayer,
				cornerLayer);

		NatTable natTable = new NatTable(parent, gridLayer, false);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new HeaderMenuConfiguration(natTable));

		natTable.configure();
		
		natTable.setTheme(new ModernNatTableThemeConfiguration());
		
		return natTable;
	}
	
	
	private List<Person> getStaticPersonList() {
		List<Person> result = new ArrayList<Person>();
		
		//create some persons
		result.add(new Person(1, "Homer", "Simpson", Gender.MALE, true, new Date()));
		result.add(new Person(2, "Marge", "Simpson", Gender.FEMALE, true, new Date()));
		result.add(new Person(3, "Bart", "Simpson", Gender.MALE, false, new Date()));
		result.add(new Person(4, "Lisa", "Simpson", Gender.FEMALE, false, new Date()));
		result.add(new Person(5, "Maggie", "Simpson", Gender.FEMALE, false, new Date()));

		result.add(new Person(6, "Ned", "Flanders", Gender.MALE, true, new Date()));
		result.add(new Person(7, "Maude", "Flanders", Gender.FEMALE, true, new Date()));
		result.add(new Person(8, "Rod", "Flanders", Gender.MALE, false, new Date()));
		result.add(new Person(9, "Todd", "Flanders", Gender.MALE, false, new Date()));

		result.add(new Person(10, "Timothy", "Lovejoy", Gender.MALE, true, new Date()));
		result.add(new Person(11, "Helen", "Lovejoy", Gender.FEMALE, true, new Date()));
		result.add(new Person(12, "Jessica", "Lovejoy", Gender.FEMALE, false, new Date()));
		
		return result;
	}
	
	private class RowHeaderConfiguration extends DefaultRowHeaderLayerConfiguration {		
		@Override
		protected void addRowHeaderUIBindings() {
			// We're suppressing the row resize bindings.
		}
	}

}
