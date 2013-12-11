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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDoubleDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultIntegerDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples._500_Layers._5832_GroupByCustomTypesExample.MyRowObject.Gender;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByDataLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByHeaderLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByHeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByModel;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.persistence.command.DisplayPersistenceDialogCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemProvider;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;

/**
 * Simple example showing how to add the group by feature to the layer
 * composition of a grid.
 * 
 * @author Dirk Fauth
 *
 */
public class _5832_GroupByCustomTypesExample extends AbstractNatExample {

	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(new _5832_GroupByCustomTypesExample());
	}

	@Override
	public String getDescription() {
		return "This example shows the usage of the group by feature.";
	}
	
	@Override
	public Control createExampleControl(Composite parent) {
		//create a new ConfigRegistry which will be needed for GlazedLists handling
		ConfigRegistry configRegistry = new ConfigRegistry();

		String[] propertyNames = {"name", "age", "money", "gender", "city"};

		//mapping from property to label, needed for column header labels
		Map<String, String> propertyToLabelMap = new HashMap<String, String>();
		propertyToLabelMap.put("name", "Name");
		propertyToLabelMap.put("age", "Age");
		propertyToLabelMap.put("money", "Money");
		propertyToLabelMap.put("gender", "Gender");
		propertyToLabelMap.put("city", "City");

		IColumnPropertyAccessor<MyRowObject> columnPropertyAccessor = 
				new ReflectiveColumnPropertyAccessor<MyRowObject>(propertyNames);
		
		BodyLayerStack<MyRowObject> bodyLayerStack = 
				new BodyLayerStack<MyRowObject>(createMyRowObjects(50), columnPropertyAccessor);
		//add a label accumulator to be able to register converter
		bodyLayerStack.getBodyDataLayer().setConfigLabelAccumulator(new ColumnLabelAccumulator());

		//build the column header layer
		IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
		DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
		ILayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayerStack, bodyLayerStack.getSelectionLayer());
		
		//build the row header layer
		IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyLayerStack.getBodyDataProvider());
		DataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
		ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, bodyLayerStack, bodyLayerStack.getSelectionLayer());
		
		//build the corner layer
		IDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
		DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
		ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);
		
		//build the grid layer
		GridLayer gridLayer = new GridLayer(bodyLayerStack, columnHeaderLayer, rowHeaderLayer, cornerLayer);
		
		//set the group by header on top of the grid
		CompositeLayer compositeGridLayer = new CompositeLayer(1, 2);
		final GroupByHeaderLayer groupByHeaderLayer = 
				new GroupByHeaderLayer(bodyLayerStack.getGroupByModel(), gridLayer, columnHeaderDataProvider);
		compositeGridLayer.setChildLayer(GroupByHeaderLayer.GROUP_BY_REGION, groupByHeaderLayer, 0, 0);
		compositeGridLayer.setChildLayer("Grid", gridLayer, 0, 1);
		
		//turn the auto configuration off as we want to add our header menu configuration
		NatTable natTable = new NatTable(parent, compositeGridLayer, false);
		
		//as the autoconfiguration of the NatTable is turned off, we have to add the 
		//DefaultNatTableStyleConfiguration and the ConfigRegistry manually	
		natTable.setConfigRegistry(configRegistry);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		//add group by configuration
		natTable.addConfiguration(new GroupByHeaderMenuConfiguration(natTable, groupByHeaderLayer));
		natTable.addConfiguration(new MyRowObjectTableConfiguration());
		
		natTable.addConfiguration(new HeaderMenuConfiguration(natTable) {
			@Override
			protected PopupMenuBuilder createCornerMenu(NatTable natTable) {
				return super.createCornerMenu(natTable)
						.withStateManagerMenuItemProvider()
						.withMenuItemProvider(new IMenuItemProvider() {
							
							@Override
							public void addMenuItem(NatTable natTable, Menu popupMenu) {
								MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
								menuItem.setText("Toggle Group By Header"); //$NON-NLS-1$
								menuItem.setEnabled(true);
		
								menuItem.addSelectionListener(new SelectionAdapter() {
									@Override
									public void widgetSelected(SelectionEvent event) {
										groupByHeaderLayer.setVisible(!groupByHeaderLayer.isVisible());
									}
								});
							}
						});
			}
		});
		
		natTable.configure();
		
		natTable.registerCommandHandler(new DisplayPersistenceDialogCommandHandler(natTable));
		
		return natTable;
	}
	
	/**
	 * Always encapsulate the body layer stack in an AbstractLayerTransform to ensure that the
	 * index transformations are performed in later commands.
	 * @param <T>
	 */
	class BodyLayerStack<T> extends AbstractLayerTransform {
		
		private final SortedList<T> sortedList;
		
		private final IDataProvider bodyDataProvider;
		private final GroupByDataLayer<T> bodyDataLayer;
		
		private final SelectionLayer selectionLayer;
		
		private final GroupByModel groupByModel = new GroupByModel();
		
		public BodyLayerStack(List<T> values, IColumnPropertyAccessor<T> columnPropertyAccessor) {
			//wrapping of the list to show into GlazedLists
			//see http://publicobject.com/glazedlists/ for further information
			EventList<T> eventList = GlazedLists.eventList(values);
			TransformedList<T, T> rowObjectsGlazedList = GlazedLists.threadSafeList(eventList);
			
			//use the SortedList constructor with 'null' for the Comparator because the Comparator
			//will be set by configuration
			sortedList = new SortedList<T>(rowObjectsGlazedList, null);
			
			//Use the GroupByDataLayer instead of the default DataLayer
			this.bodyDataLayer = new GroupByDataLayer<T>(getGroupByModel(), sortedList, columnPropertyAccessor);
			//get the IDataProvider that was created by the GroupByDataLayer
			this.bodyDataProvider = bodyDataLayer.getDataProvider();
			
			//layer for event handling of GlazedLists and PropertyChanges
			GlazedListsEventLayer<T> glazedListsEventLayer = 
				new GlazedListsEventLayer<T>(bodyDataLayer, sortedList);

			this.selectionLayer = new SelectionLayer(glazedListsEventLayer);
			
			//add a tree layer to visualise the grouping
			TreeLayer treeLayer = new TreeLayer(selectionLayer, bodyDataLayer.getTreeRowModel());

			ViewportLayer viewportLayer = new ViewportLayer(treeLayer);
			
			setUnderlyingLayer(viewportLayer);
		}

		public SelectionLayer getSelectionLayer() {
			return selectionLayer;
		}
		
		public SortedList<T> getSortedList() {
			return this.sortedList;
		}

		public IDataProvider getBodyDataProvider() {
			return bodyDataProvider;
		}

		public DataLayer getBodyDataLayer() {
			return bodyDataLayer;
		}

		public GroupByModel getGroupByModel() {
			return groupByModel;
		}
	}

	/**
	 * Converter for the Gender enumeration of the MyRowObject type
	 */
	class GenderDisplayConverter extends DisplayConverter {

		@Override
		public Object canonicalToDisplayValue(Object canonicalValue) {
			if (canonicalValue instanceof Gender) {
				String result = canonicalValue.toString();
				result = result.substring(0, 1) + result.substring(1).toLowerCase();
				return result;
			}
			return "";
		}

		@Override
		public Object displayToCanonicalValue(Object displayValue) {
			return Gender.valueOf(displayValue.toString().toUpperCase());
		}
		
	}
	
	/**
	 * Converter for the City type
	 */
	class CityDisplayConverter extends DisplayConverter {

		@Override
		public Object canonicalToDisplayValue(Object canonicalValue) {
			if (canonicalValue instanceof City) {
				return ((City) canonicalValue).getPlz() + " " + ((City) canonicalValue).getName();
			}
			return "";
		}

		@Override
		public Object displayToCanonicalValue(Object displayValue) {
			//I know there are better ways for conversion, this should only be an example
			//for a more complex way to convert custom data types
			String plz = displayValue.toString().substring(0, 4);
			for (City city : possibleCities) {
				if (city.getPlz() == Integer.valueOf(plz)) {
					return city;
				}
			}
			return null;
		}
		
	}
	
	class MyRowObjectTableConfiguration extends AbstractRegistryConfiguration {

		@Override
		public void configureRegistry(IConfigRegistry configRegistry) {
			configRegistry.registerConfigAttribute(
					CellConfigAttributes.DISPLAY_CONVERTER,
					new DefaultIntegerDisplayConverter(),
					DisplayMode.NORMAL,
					ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 1);

			configRegistry.registerConfigAttribute(
					CellConfigAttributes.DISPLAY_CONVERTER,
					new DefaultDoubleDisplayConverter(),
					DisplayMode.NORMAL,
					ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 2);

			configRegistry.registerConfigAttribute(
					CellConfigAttributes.DISPLAY_CONVERTER,
					new GenderDisplayConverter(),
					DisplayMode.NORMAL,
					ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 3);

			configRegistry.registerConfigAttribute(
					CellConfigAttributes.DISPLAY_CONVERTER,
					new CityDisplayConverter(),
					DisplayMode.NORMAL,
					ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 4);
		}
	}
	
	private List<MyRowObject> createMyRowObjects(int amount) {
		List<MyRowObject> result = new ArrayList<MyRowObject>();
		
		MyRowObject obj = null;
		for (int i = 0; i < amount; i++) {
			obj = new MyRowObject();
			
			String[] maleNames = {"Bart", "Homer", "Lenny", "Carl", "Waylon", "Ned", "Timothy"};
			String[] femaleNames = {"Marge", "Lisa", "Maggie", "Edna", "Helen", "Jessica"};
			String[] lastNames = {"Simpson", "Leonard", "Carlson", "Smithers", "Flanders", "Krabappel", "Lovejoy"};
			
			Random randomGenerator = new Random();
			
			obj.setGender(MyRowObject.Gender.values()[randomGenerator.nextInt(2)]);
			
			if (obj.getGender().equals(MyRowObject.Gender.MALE)) {
				obj.setName(maleNames[randomGenerator.nextInt(maleNames.length)] 
						+ " " + lastNames[randomGenerator.nextInt(lastNames.length)]);
			}
			else {
				obj.setName(femaleNames[randomGenerator.nextInt(femaleNames.length)]
						+ " " + lastNames[randomGenerator.nextInt(lastNames.length)]);
			}

			obj.setAge(randomGenerator.nextInt(100));
			obj.setMoney(randomGenerator.nextDouble() * randomGenerator.nextInt(100));
			
			obj.setCity(possibleCities.get(randomGenerator.nextInt(possibleCities.size())));
			
			result.add(obj);
		}
		
		return result;
	}
	
	
	public static class MyRowObject {
		
		enum Gender {
			MALE, FEMALE
		}
		
		String name;
		int age;
		double money;
		Gender gender;
		City city;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getAge() {
			return age;
		}
		public void setAge(int age) {
			this.age = age;
		}
		public double getMoney() {
			return money;
		}
		public void setMoney(double money) {
			this.money = money;
		}
		public Gender getGender() {
			return gender;
		}
		public void setGender(Gender gender) {
			this.gender = gender;
		}
		public City getCity() {
			return city;
		}
		public void setCity(City city) {
			this.city = city;
		}
		
	}
	
	private List<City> possibleCities = new ArrayList<City>();
	{
		possibleCities.add(new City(1111, "Springfield"));
		possibleCities.add(new City(2222, "Shelbyville"));
		possibleCities.add(new City(3333, "Ogdenville"));
		possibleCities.add(new City(4444, "Waverly Hills"));
		possibleCities.add(new City(5555, "North Haverbrook"));
		possibleCities.add(new City(6666, "Capital City"));
	}
	
	public static class City {
		final int plz;
		final String name;

		City(int plz, String name) {
			this.plz = plz;
			this.name = name;
		}
		
		public int getPlz() {
			return plz;
		}
		public String getName() {
			return name;
		}
		
		//we implement toString() here because the GroupByObject does not know about the IDisplayConverter
		@Override
		public String toString() {
			return plz + " " + name;
		}
	}
}
