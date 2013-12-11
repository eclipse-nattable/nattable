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
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
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
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.AbstractOverrider;
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
public class _572_TreeGridExample extends AbstractNatExample {

	public static final String MARRIED_LABEL = "marriedLabel";
	public static final String DATE_LABEL = "dateLabel";
	
	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(new _572_TreeGridExample());
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
		
		final BodyLayerStack bodyLayerStack = 
				new BodyLayerStack(
						PersonService.getPersonsWithAddress(50), columnPropertyAccessor, 
						new PersonWithAddressTwoLevelTreeFormat());
//						new PersonWithAddressTreeFormat());

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
						MARRIED_LABEL);
				
				configRegistry.registerConfigAttribute(
						CellConfigAttributes.DISPLAY_CONVERTER, 
						new DefaultDateDisplayConverter("MM/dd/yyyy"), 
						DisplayMode.NORMAL, 
						DATE_LABEL);
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
	 * @param <PersonWithAddress>
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	class BodyLayerStack extends AbstractLayerTransform {
		
		private final TreeList treeList;
		
		private final IRowDataProvider bodyDataProvider;
		
		private final SelectionLayer selectionLayer;
		
		public BodyLayerStack(List<PersonWithAddress> values, IColumnPropertyAccessor<PersonWithAddress> columnPropertyAccessor,
				TreeList.Format treeFormat) {
			//wrapping of the list to show into GlazedLists
			//see http://publicobject.com/glazedlists/ for further information
			EventList<PersonWithAddress> eventList = GlazedLists.eventList(values);
			TransformedList<PersonWithAddress, PersonWithAddress> rowObjectsGlazedList = GlazedLists.threadSafeList(eventList);
			
			//use the SortedList constructor with 'null' for the Comparator because the Comparator
			//will be set by configuration
			SortedList<PersonWithAddress> sortedList = new SortedList<PersonWithAddress>(rowObjectsGlazedList, null);
			// wrap the SortedList with the TreeList
			this.treeList = new TreeList(sortedList, treeFormat, TreeList.nodesStartExpanded());
			
			this.bodyDataProvider = 
					new GlazedListsDataProvider<Object>(treeList, 
							new PersonWithAddressTreeColumnPropertyAccessor(columnPropertyAccessor));
			DataLayer bodyDataLayer = new DataLayer(this.bodyDataProvider);
			
			//simply apply labels for every column by index
			bodyDataLayer.setConfigLabelAccumulator(new AbstractOverrider() {
				
				@Override
				public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
					Object rowObject = bodyDataProvider.getRowObject(rowPosition);
					if (rowObject instanceof PersonWithAddress) {
						if (columnPosition == 3) {
							configLabels.addLabel(MARRIED_LABEL);
						}
						else if (columnPosition == 4) {
							configLabels.addLabel(DATE_LABEL);
						}
					}
				}
			});
			
			//layer for event handling of GlazedLists and PropertyChanges
			GlazedListsEventLayer<PersonWithAddress> glazedListsEventLayer = 
				new GlazedListsEventLayer<PersonWithAddress>(bodyDataLayer, treeList);

			GlazedListTreeData<Object> treeData = new GlazedListTreeData<Object>(treeList) {
				@Override
				public String formatDataForDepth(int depth, Object object) {
					if (object instanceof PersonWithAddress) {
						return ((PersonWithAddress)object).getLastName();
					}
					return object.toString();
				}
			};
			ITreeRowModel<Object> treeRowModel = new GlazedListTreeRowModel<Object>(treeData);
			
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
		
		public TreeList<PersonWithAddress> getTreeList() {
			return this.treeList;
		}

		public IDataProvider getBodyDataProvider() {
			return bodyDataProvider;
		}
	}

	
	private class PersonWithAddressTreeColumnPropertyAccessor implements IColumnPropertyAccessor<Object> {

		private IColumnPropertyAccessor<PersonWithAddress> cpa;
		
		public PersonWithAddressTreeColumnPropertyAccessor(IColumnPropertyAccessor<PersonWithAddress> cpa) {
			this.cpa = cpa;
		}
		
		@Override
		public Object getDataValue(Object rowObject, int columnIndex) {
			if (rowObject instanceof PersonWithAddress) {
				return this.cpa.getDataValue((PersonWithAddress)rowObject, columnIndex);
			}
			else if (columnIndex == 0) {
				return rowObject;
			}
			return null;
		}

		@Override
		public void setDataValue(Object rowObject, int columnIndex, Object newValue) {
			if (rowObject instanceof PersonWithAddress) {
				this.cpa.setDataValue((PersonWithAddress)rowObject, columnIndex, newValue);
			}
		}

		@Override
		public int getColumnCount() {
			return this.cpa.getColumnCount();
		}

		@Override
		public String getColumnProperty(int columnIndex) {
			return this.cpa.getColumnProperty(columnIndex);
		}

		@Override
		public int getColumnIndex(String propertyName) {
			return this.cpa.getColumnIndex(propertyName);
		}
		
	}
	
	
	@SuppressWarnings("unused")
	private class PersonWithAddressTreeFormat implements TreeList.Format<Object> {
		
		@Override
		public void getPath(List<Object> path, Object element) {
			if (element instanceof PersonWithAddress) {
				PersonWithAddress ele = (PersonWithAddress) element;
				path.add(ele.getLastName());
			}
			path.add(element);
		}
		
		@Override
		public boolean allowsChildren(Object element) {
			return true;
		}

		@Override
		public Comparator<? super Object> getComparator(int depth) {
			return new Comparator<Object>() {

				@Override
				public int compare(Object o1, Object o2) {
					String e1 = (o1 instanceof PersonWithAddress) ? ((PersonWithAddress)o1).getLastName() : o1.toString();
					String e2 = (o2 instanceof PersonWithAddress) ? ((PersonWithAddress)o2).getLastName() : o2.toString();
					return e1.compareTo(e2);
				}
				
			};
		}
	}
	
	private class PersonWithAddressTwoLevelTreeFormat implements TreeList.Format<Object> {
		
		@Override
		public void getPath(List<Object> path, Object element) {
			if (element instanceof PersonWithAddress) {
				PersonWithAddress ele = (PersonWithAddress) element;
				path.add(ele.getLastName());
				path.add(ele.getFirstName());
			}
			path.add(element);
		}
		
		@Override
		public boolean allowsChildren(Object element) {
			return true;
		}

		@Override
		public Comparator<? super Object> getComparator(final int depth) {
			return new Comparator<Object>() {

				@Override
				public int compare(Object o1, Object o2) {
					String e1 = (o1 instanceof PersonWithAddress) ? 
							(depth == 0 ? ((PersonWithAddress)o1).getLastName() : ((PersonWithAddress)o1).getFirstName()) 
								: o1.toString();
					String e2 = (o2 instanceof PersonWithAddress) ? 
							(depth == 0 ? ((PersonWithAddress)o2).getLastName() : ((PersonWithAddress)o2).getFirstName()) 
								: o2.toString();
					return e1.compareTo(e2);
				}
				
			};
		}
	}

}
