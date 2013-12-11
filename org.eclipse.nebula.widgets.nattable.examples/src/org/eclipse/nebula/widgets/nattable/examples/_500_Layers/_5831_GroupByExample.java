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
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.data.person.PersonWithAddress;
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
import org.eclipse.nebula.widgets.nattable.persistence.command.DisplayPersistenceDialogCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeCollapseAllCommand;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandAllCommand;
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
public class _5831_GroupByExample extends AbstractNatExample {

	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(new _5831_GroupByExample());
	}

	@Override
	public String getDescription() {
		return "This example shows the usage of the group by feature.";
	}
	
	@Override
	public Control createExampleControl(Composite parent) {
		//create a new ConfigRegistry which will be needed for GlazedLists handling
		ConfigRegistry configRegistry = new ConfigRegistry();

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
		
		BodyLayerStack<PersonWithAddress> bodyLayerStack = 
				new BodyLayerStack<PersonWithAddress>(PersonService.getPersonsWithAddress(100), columnPropertyAccessor);

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
						})
						.withMenuItemProvider(new IMenuItemProvider() {
							
							@Override
							public void addMenuItem(final NatTable natTable, Menu popupMenu) {
								MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
								menuItem.setText("Collapse All"); //$NON-NLS-1$
								menuItem.setEnabled(true);
		
								menuItem.addSelectionListener(new SelectionAdapter() {
									@Override
									public void widgetSelected(SelectionEvent event) {
										natTable.doCommand(new TreeCollapseAllCommand());
									}
								});
							}
						})
						.withMenuItemProvider(new IMenuItemProvider() {
							
							@Override
							public void addMenuItem(final NatTable natTable, Menu popupMenu) {
								MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
								menuItem.setText("Expand All"); //$NON-NLS-1$
								menuItem.setEnabled(true);
		
								menuItem.addSelectionListener(new SelectionAdapter() {
									@Override
									public void widgetSelected(SelectionEvent event) {
										natTable.doCommand(new TreeExpandAllCommand());
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
			GroupByDataLayer<T> bodyDataLayer = new GroupByDataLayer<T>(getGroupByModel(), sortedList, columnPropertyAccessor);
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

		public GroupByModel getGroupByModel() {
			return groupByModel;
		}
	}
}
