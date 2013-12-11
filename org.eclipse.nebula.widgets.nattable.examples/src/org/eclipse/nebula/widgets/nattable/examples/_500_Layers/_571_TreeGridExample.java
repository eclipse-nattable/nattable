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

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDateDisplayConverter;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.data.person.PersonWithAddress;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeData;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeRowModel;
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
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModel;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeCollapseAllCommand;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandAllCommand;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.TreeList;

/**
 * Simple example showing how to create a tree within a grid.
 * 
 * @author Dirk Fauth
 *
 */
public class _571_TreeGridExample extends AbstractNatExample {

	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(new _571_TreeGridExample());
	}

	@Override
	public String getDescription() {
		return "This example shows how to create a tree within a grid.";
	}
	
	@Override
	public Control createExampleControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		
		//create a new ConfigRegistry which will be needed for GlazedLists handling
		ConfigRegistry configRegistry = new ConfigRegistry();

		//property names of the Person class
		String[] propertyNames = {"lastName", "firstName", "gender", "married", "birthday"};

		//mapping from property to label, needed for column header labels
		Map<String, String> propertyToLabelMap = new HashMap<String, String>();
		propertyToLabelMap.put("lastName", "Lastname");
		propertyToLabelMap.put("firstName", "Firstname");
		propertyToLabelMap.put("gender", "Gender");
		propertyToLabelMap.put("married", "Married");
		propertyToLabelMap.put("birthday", "Birthday");

		IColumnPropertyAccessor<PersonWithAddress> columnPropertyAccessor = 
				new ReflectiveColumnPropertyAccessor<PersonWithAddress>(propertyNames);
		
		final BodyLayerStack<PersonWithAddress> bodyLayerStack = 
				new BodyLayerStack<PersonWithAddress>(
						PersonService.getPersonsWithAddress(50), columnPropertyAccessor, 
						new PersonWithAddressTreeFormat());

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
		
		//turn the auto configuration off as we want to add our header menu configuration
		final NatTable natTable = new NatTable(container, gridLayer, false);
		
		//as the autoconfiguration of the NatTable is turned off, we have to add the 
		//DefaultNatTableStyleConfiguration and the ConfigRegistry manually	
		natTable.setConfigRegistry(configRegistry);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		
		natTable.addConfiguration(new AbstractRegistryConfiguration() {
			
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
				//register a CheckBoxPainter as CellPainter for the married information
				configRegistry.registerConfigAttribute(
						CellConfigAttributes.CELL_PAINTER, 
						new CheckBoxPainter(), 
						DisplayMode.NORMAL, 
						ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 3);
				
				configRegistry.registerConfigAttribute(
						CellConfigAttributes.DISPLAY_CONVERTER, 
						new DefaultDateDisplayConverter("MM/dd/yyyy"), 
						DisplayMode.NORMAL, 
						ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 4);

				
			}
		});
		
		natTable.configure();
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
		
		Composite buttonPanel = new Composite(container, SWT.NONE);
		buttonPanel.setLayout(new RowLayout());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonPanel);
		
		Button collapseAllButton = new Button(buttonPanel, SWT.PUSH);
		collapseAllButton.setText("Collapse All");
		collapseAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				natTable.doCommand(new TreeCollapseAllCommand());
			}
		});
		
		Button expandAllButton = new Button(buttonPanel, SWT.PUSH);
		expandAllButton.setText("Expand All");
		expandAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				natTable.doCommand(new TreeExpandAllCommand());
			}
		});
		
		return container;
	}
	
	/**
	 * Always encapsulate the body layer stack in an AbstractLayerTransform to ensure that the
	 * index transformations are performed in later commands.
	 * @param <T>
	 */
	class BodyLayerStack<T> extends AbstractLayerTransform {
		
		private final TreeList<T> treeList;
		
		private final IDataProvider bodyDataProvider;
		
		private final SelectionLayer selectionLayer;
		
		@SuppressWarnings("unchecked")
		public BodyLayerStack(List<T> values, IColumnPropertyAccessor<T> columnPropertyAccessor,
				TreeList.Format<T> treeFormat) {
			//wrapping of the list to show into GlazedLists
			//see http://publicobject.com/glazedlists/ for further information
			EventList<T> eventList = GlazedLists.eventList(values);
			TransformedList<T, T> rowObjectsGlazedList = GlazedLists.threadSafeList(eventList);
			
			//use the SortedList constructor with 'null' for the Comparator because the Comparator
			//will be set by configuration
			SortedList<T> sortedList = new SortedList<T>(rowObjectsGlazedList, null);
			// wrap the SortedList with the TreeList
			this.treeList = new TreeList<T>(sortedList, treeFormat, TreeList.NODES_START_EXPANDED);
			
			this.bodyDataProvider = 
					new GlazedListsDataProvider<T>(treeList, columnPropertyAccessor);
			DataLayer bodyDataLayer = new DataLayer(this.bodyDataProvider);
			
			//simply apply labels for every column by index
			bodyDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());
			
			//layer for event handling of GlazedLists and PropertyChanges
			GlazedListsEventLayer<T> glazedListsEventLayer = 
				new GlazedListsEventLayer<T>(bodyDataLayer, treeList);

			GlazedListTreeData<T> treeData = new GlazedListTreeData<T>(treeList) {
				@Override
				public String formatDataForDepth(int depth, T object) {
					if (object instanceof PersonWithAddress) {
						return ((PersonWithAddress)object).getLastName();
					}
					return null;
				}
			};
			ITreeRowModel<T> treeRowModel = new GlazedListTreeRowModel<T>(treeData);
			
			this.selectionLayer = new SelectionLayer(glazedListsEventLayer);
			
			TreeLayer treeLayer = new TreeLayer(selectionLayer, treeRowModel)  {
				@Override
				public ICellPainter getCellPainter(int columnPosition,
						int rowPosition, ILayerCell cell,
						IConfigRegistry configRegistry) {
					ICellPainter painter = super.getCellPainter(columnPosition, rowPosition, cell, configRegistry);
					//this is a little hack to get a padding between tree node icon and left border
					//the reason for this hack is that the TreeLayer internally wraps the registered
					//cell painter with the IndentedTreeImagePainter
					//so for backwards compatibility instead of adding it to the TreeLayer itself
					//we override getCellPainter() and wrap the created painter for the tree column again
					if (columnPosition == TREE_COLUMN_NUMBER) {
						painter = new PaddingDecorator(painter, 0, 0, 0, 2);
					}
					return painter;
				}

			};
			ViewportLayer viewportLayer = new ViewportLayer(treeLayer);
			
			setUnderlyingLayer(viewportLayer);
		}

		public SelectionLayer getSelectionLayer() {
			return selectionLayer;
		}
		
		public TreeList<T> getTreeList() {
			return this.treeList;
		}

		public IDataProvider getBodyDataProvider() {
			return bodyDataProvider;
		}
	}
	
	/**
	 * Simple TreeList.Format implementation that uses the lastname of the PersonWithAddress
	 * object as tree item.
	 * <p>
	 * Using a String directly as the tree item has the possible disadvantage of haven non-unique
	 * items in the tree within subtrees. 
	 */
	private class PersonWithAddressTreeFormat implements TreeList.Format<PersonWithAddress> {
		
		private Map<String, PersonWithAddress> parentMapping = new HashMap<String, PersonWithAddress>();
		
		/**
		 * Populate path with a list describing the path from a root node to this element. 
		 * Upon returning, the list must have size >= 1, where the provided element identical to the 
		 * list's last element.
		 * This implementation will use the first object found for a last name as root node by
		 * storing it within a map. If there is already an object stored for the lastname of the
		 * given element, it will be used as root for the path.
		 */
		@Override
		public void getPath(List<PersonWithAddress> path, PersonWithAddress element) {
			if (parentMapping.get(element.getLastName()) != null) {
				path.add(parentMapping.get(element.getLastName()));
			}
			else {
				parentMapping.put(element.getLastName(), element);
			}
			path.add(element);
		}
		
		/**
		 * Simply always return <code>true</code>.
		 * @return <code>true</code> if this element can have child elements, 
		 * 			or <code>false</code> if it is always a leaf node.
		 */
		@Override
		public boolean allowsChildren(PersonWithAddress element) {
			return true;
		}

		/**
		 * Returns the comparator used to order path elements of the specified depth. 
		 * If enforcing order at this level is not intended, this method should return <code>null</code>.
		 * We do a simple sorting of the last names of the persons to show so the tree nodes
		 * are sorted in alphabetical order.
		 */
		@Override
		public Comparator<? super PersonWithAddress> getComparator(int depth) {
			return new Comparator<PersonWithAddress>() {

				@Override
				public int compare(PersonWithAddress o1, PersonWithAddress o2) {
					return o1.getLastName().compareTo(o2.getLastName());
				}
				
			};
		}
	}

}
