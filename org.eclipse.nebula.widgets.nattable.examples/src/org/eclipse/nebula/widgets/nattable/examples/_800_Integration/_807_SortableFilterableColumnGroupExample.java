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
package org.eclipse.nebula.widgets.nattable.examples._800_Integration;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.person.DataModelConstants;
import org.eclipse.nebula.widgets.nattable.examples.data.person.ExtendedPersonWithAddress;
import org.eclipse.nebula.widgets.nattable.examples.data.person.Person.Gender;
import org.eclipse.nebula.widgets.nattable.examples.data.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.DefaultGlazedListsFilterStrategy;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowHeaderComposite;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowTextCellEditor;
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;
import org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupExpandCollapseLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupReorderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;

public class _807_SortableFilterableColumnGroupExample extends AbstractNatExample {

	private final ColumnGroupModel columnGroupModel = new ColumnGroupModel();
	private final ColumnGroupModel sndColumnGroupModel = new ColumnGroupModel();
	
	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(600, 400, new _807_SortableFilterableColumnGroupExample());
	}

	@Override
	public Control createExampleControl(Composite parent) {
		//create a new ConfigRegistry which will be needed for GlazedLists handling
		ConfigRegistry configRegistry = new ConfigRegistry();

		//property names of the Person class
		String[] propertyNames = {"firstName", "lastName", "gender", "married", 
				"address.street", "address.housenumber", "address.postalCode", "address.city", 
				"age", "birthday", "money",
				"description", "favouriteFood", "favouriteDrinks"};

		//mapping from property to label, needed for column header labels
		Map<String, String> propertyToLabelMap = new HashMap<String, String>();
		propertyToLabelMap.put("firstName", "Firstname");
		propertyToLabelMap.put("lastName", "Lastname");
		propertyToLabelMap.put("gender", "Gender");
		propertyToLabelMap.put("married", "Married");
		propertyToLabelMap.put("address.street", "Street");
		propertyToLabelMap.put("address.housenumber", "Housenumber");
		propertyToLabelMap.put("address.postalCode", "Postalcode");
		propertyToLabelMap.put("address.city", "City");
		propertyToLabelMap.put("age", "Age");
		propertyToLabelMap.put("birthday", "Birthday");
		propertyToLabelMap.put("money", "Money");
		propertyToLabelMap.put("description", "Description");
		propertyToLabelMap.put("favouriteFood", "Food");
		propertyToLabelMap.put("favouriteDrinks", "Drinks");

		IColumnPropertyAccessor<ExtendedPersonWithAddress> columnPropertyAccessor = 
				new ExtendedReflectiveColumnPropertyAccessor<ExtendedPersonWithAddress>(propertyNames);

		BodyLayerStack<ExtendedPersonWithAddress> bodyLayer = 
				new BodyLayerStack<ExtendedPersonWithAddress>(PersonService.getExtendedPersonsWithAddress(10), 
						columnPropertyAccessor, 
						sndColumnGroupModel, columnGroupModel);

		IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
		DataLayer columnHeaderDataLayer = new DataLayer(columnHeaderDataProvider);
		ILayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayer, bodyLayer.getSelectionLayer());
		
		SortHeaderLayer<ExtendedPersonWithAddress> sortHeaderLayer = 
				new SortHeaderLayer<ExtendedPersonWithAddress>(columnHeaderLayer, 
					new GlazedListsSortModel<ExtendedPersonWithAddress>(bodyLayer.getSortedList(), 
							columnPropertyAccessor, configRegistry, columnHeaderDataLayer));
		
		ColumnGroupHeaderLayer columnGroupHeaderLayer = new ColumnGroupHeaderLayer(sortHeaderLayer, bodyLayer.getSelectionLayer(), columnGroupModel);

		columnGroupHeaderLayer.addColumnsIndexesToGroup("Person", 0, 1, 2, 3);
		columnGroupHeaderLayer.addColumnsIndexesToGroup("Address", 4, 5, 6, 7);
		columnGroupHeaderLayer.addColumnsIndexesToGroup("Facts", 8, 9, 10);
		columnGroupHeaderLayer.addColumnsIndexesToGroup("Personal", 11, 12, 13);
		
		columnGroupHeaderLayer.setStaticColumnIndexesByGroup("Person", 0, 1);
		columnGroupHeaderLayer.setStaticColumnIndexesByGroup("Address", 4, 5, 6);
		
		ColumnGroupGroupHeaderLayer sndGroup = 
				new ColumnGroupGroupHeaderLayer(columnGroupHeaderLayer, bodyLayer.getSelectionLayer(), sndColumnGroupModel);
		
		sndGroup.addColumnsIndexesToGroup("PersonWithAddress", 0, 1, 2, 3, 4, 5, 6, 7);
		sndGroup.addColumnsIndexesToGroup("Additional Information", 8, 9, 10, 11, 12, 13);

		sndGroup.setStaticColumnIndexesByGroup("PersonWithAddress", 0, 1);
		
		//	Note: The column header layer is wrapped in a filter row composite.
		//	This plugs in the filter row functionality
		FilterRowHeaderComposite<ExtendedPersonWithAddress> filterRowHeaderLayer =
			new FilterRowHeaderComposite<ExtendedPersonWithAddress>(
					new DefaultGlazedListsFilterStrategy<ExtendedPersonWithAddress>(bodyLayer.getFilterList(), columnPropertyAccessor, configRegistry),
					sndGroup, columnHeaderDataLayer.getDataProvider(), configRegistry
			);

		DefaultRowHeaderDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyLayer.getBodyDataProvider());
		DataLayer rowHeaderDataLayer = new DataLayer(rowHeaderDataProvider);
		rowHeaderDataLayer.setDefaultColumnWidth(40);
		ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, bodyLayer, bodyLayer.getSelectionLayer());

		ILayer cornerLayer = new CornerLayer(
				new DataLayer(
						new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider)), 
						rowHeaderLayer, filterRowHeaderLayer);
		
		GridLayer gridLayer = new GridLayer(bodyLayer, filterRowHeaderLayer, rowHeaderLayer, cornerLayer);

		NatTable natTable = new NatTable(parent, gridLayer, false);
		natTable.setConfigRegistry(configRegistry);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		//add filter row configuration
		natTable.addConfiguration(new FilterRowConfiguration());
		
		natTable.configure();

		return natTable;
	}
	
	
	class BodyLayerStack<T> extends AbstractLayerTransform {
		
		private final SortedList<T> sortedList;
		private final FilterList<T> filterList;
		
		private final IDataProvider bodyDataProvider;

		private ColumnReorderLayer columnReorderLayer;
		private ColumnGroupReorderLayer columnGroupReorderLayer;
		private ColumnHideShowLayer columnHideShowLayer;
		private ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer;
		private SelectionLayer selectionLayer;
		private ViewportLayer viewportLayer;

		public BodyLayerStack(List<T> values, IColumnPropertyAccessor<T> columnPropertyAccessor, 
				ColumnGroupModel... columnGroupModel) {
			//wrapping of the list to show into GlazedLists
			//see http://publicobject.com/glazedlists/ for further information
			EventList<T> eventList = GlazedLists.eventList(values);
			TransformedList<T, T> rowObjectsGlazedList = GlazedLists.threadSafeList(eventList);
			
			//use the SortedList constructor with 'null' for the Comparator because the Comparator
			//will be set by configuration
			this.sortedList = new SortedList<T>(rowObjectsGlazedList, null);
			// wrap the SortedList with the FilterList
			this.filterList = new FilterList<T>(getSortedList());
			
			this.bodyDataProvider = 
				new ListDataProvider<T>(filterList, columnPropertyAccessor);
			DataLayer bodyDataLayer = new DataLayer(this.bodyDataProvider);
			
			//layer for event handling of GlazedLists and PropertyChanges
			GlazedListsEventLayer<T> glazedListsEventLayer = 
				new GlazedListsEventLayer<T>(bodyDataLayer, filterList);

			columnReorderLayer = new ColumnReorderLayer(glazedListsEventLayer);
			columnGroupReorderLayer = new ColumnGroupReorderLayer(columnReorderLayer, columnGroupModel[columnGroupModel.length-1]);
			columnHideShowLayer = new ColumnHideShowLayer(columnGroupReorderLayer);
			columnGroupExpandCollapseLayer = new ColumnGroupExpandCollapseLayer(columnHideShowLayer, columnGroupModel);
			selectionLayer = new SelectionLayer(columnGroupExpandCollapseLayer);
			viewportLayer = new ViewportLayer(selectionLayer);
			setUnderlyingLayer(viewportLayer);
		}

		public SortedList<T> getSortedList() {
			return sortedList;
		}
		
		public FilterList<T> getFilterList() {
			return filterList;
		}

		public IDataProvider getBodyDataProvider() {
			return bodyDataProvider;
		}

		public ColumnReorderLayer getColumnReorderLayer() {
			return columnReorderLayer;
		}

		public ColumnGroupReorderLayer getColumnGroupReorderLayer() {
			return columnGroupReorderLayer;
		}

		public ColumnHideShowLayer getColumnHideShowLayer() {
			return columnHideShowLayer;
		}

		public ColumnGroupExpandCollapseLayer getColumnGroupExpandCollapseLayer() {
			return columnGroupExpandCollapseLayer;
		}

		public SelectionLayer getSelectionLayer() {
			return selectionLayer;
		}

		public ViewportLayer getViewportLayer() {
			return viewportLayer;
		}

	}

	/**
	 * The configuration to enable the edit mode for the grid and additional
	 * edit configurations like converters and validators.
	 * 
	 * @author Dirk Fauth
	 */
	class FilterRowConfiguration extends AbstractRegistryConfiguration {

		@Override
		public void configureRegistry(IConfigRegistry configRegistry) {

			//register the FilterRowTextCellEditor in the first column which immediately commits on key press
			configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, 
					new FilterRowTextCellEditor(), 
					DisplayMode.NORMAL, 
					FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + DataModelConstants.FIRSTNAME_COLUMN_POSITION);
			
			//register a combo box cell editor for the gender column in the filter row
			//the label is set automatically to the value of 
			//FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + column position
			ICellEditor comboBoxCellEditor = new ComboBoxCellEditor(Arrays.asList(Gender.FEMALE, Gender.MALE));
			configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, 
					comboBoxCellEditor, 
					DisplayMode.NORMAL, 
					FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + DataModelConstants.GENDER_COLUMN_POSITION);

			
			//register a combo box cell editor for the married column in the filter row
			//the label is set automatically to the value of 
			//FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + column position
			comboBoxCellEditor = new ComboBoxCellEditor(Arrays.asList(Boolean.TRUE, Boolean.FALSE));
			configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, 
					comboBoxCellEditor, 
					DisplayMode.NORMAL, 
					FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + DataModelConstants.MARRIED_COLUMN_POSITION);
			
			configRegistry.registerConfigAttribute(
					FilterRowConfigAttributes.TEXT_MATCHING_MODE, 
					TextMatchingMode.EXACT, 
					DisplayMode.NORMAL, 
					FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + DataModelConstants.GENDER_COLUMN_POSITION);

			configRegistry.registerConfigAttribute(FilterRowConfigAttributes.TEXT_DELIMITER, "&"); //$NON-NLS-1$

		}

	}
}
