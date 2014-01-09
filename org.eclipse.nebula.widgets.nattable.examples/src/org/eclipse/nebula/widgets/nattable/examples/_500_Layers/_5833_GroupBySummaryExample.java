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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.VisualRefreshCommand;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.data.person.ExtendedPersonWithAddress;
import org.eclipse.nebula.widgets.nattable.examples.data.person.PersonService;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByConfigAttributes;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByDataLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByHeaderLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByHeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByModel;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.summary.IGroupBySummaryProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.summary.SummationGroupBySummaryProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.persistence.command.DisplayPersistenceDialogCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeCollapseAllCommand;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandAllCommand;
import org.eclipse.nebula.widgets.nattable.ui.menu.AbstractHeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
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

/**
 * Simple example showing how to add the group by feature to the layer
 * composition of a grid in conjunction with showing summary values of groupings.
 * 
 * @author Dirk Fauth
 *
 */
public class _5833_GroupBySummaryExample extends AbstractNatExample {

	private IGroupBySummaryProvider<ExtendedPersonWithAddress> sumMoneySummaryProvider;
	private IGroupBySummaryProvider<ExtendedPersonWithAddress> avgMoneySummaryProvider;
	
	private boolean useMoneySum = true;
	
	public static void main(String[] args) throws Exception {
		StandaloneNatExampleRunner.run(new _5833_GroupBySummaryExample());
	}

	@Override
	public String getDescription() {
		return "This example shows the usage of the group by feature in conjunction with summary values of the groupings.";
	}
	
	@Override
	public Control createExampleControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		
		//create a new ConfigRegistry which will be needed for GlazedLists handling
		final ConfigRegistry configRegistry = new ConfigRegistry();

		//property names of the ExtendedPersonWithAddress class
		String[] propertyNames = {"firstName", "lastName", "age", "money",
				"married", "gender", "birthday"};

		//mapping from property to label, needed for column header labels
		Map<String, String> propertyToLabelMap = new HashMap<String, String>();
		propertyToLabelMap.put("firstName", "Firstname");
		propertyToLabelMap.put("lastName", "Lastname");
		propertyToLabelMap.put("age", "Age");
		propertyToLabelMap.put("money", "Money");
		propertyToLabelMap.put("married", "Married");
		propertyToLabelMap.put("gender", "Gender");
		propertyToLabelMap.put("birthday", "Birthday");

		final IColumnPropertyAccessor<ExtendedPersonWithAddress> columnPropertyAccessor = 
				new ExtendedReflectiveColumnPropertyAccessor<ExtendedPersonWithAddress>(propertyNames);
		
		//to enable the group by summary feature, the GroupByDataLayer needs to know the ConfigRegistry
		final BodyLayerStack<ExtendedPersonWithAddress> bodyLayerStack = 
				new BodyLayerStack<ExtendedPersonWithAddress>(
						PersonService.getExtendedPersonsWithAddress(10000), columnPropertyAccessor, configRegistry);

		bodyLayerStack.getBodyDataLayer().setConfigLabelAccumulator(new ColumnLabelAccumulator());
		
		//build the column header layer
		IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
		DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
		ILayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayerStack, bodyLayerStack.getSelectionLayer());
		
		//add sorting
		SortHeaderLayer<ExtendedPersonWithAddress> sortHeaderLayer = new SortHeaderLayer<ExtendedPersonWithAddress>(
				columnHeaderLayer, 
				new GlazedListsSortModel<ExtendedPersonWithAddress>(
						bodyLayerStack.getSortedList(), 
						columnPropertyAccessor,
						configRegistry, 
						bodyLayerStack.getBodyDataLayer()), 
				false);
		
		//connect sortModel to GroupByDataLayer to support sorting by group by summary values
		bodyLayerStack.getBodyDataLayer().setSortModel(sortHeaderLayer.getSortModel());

		//build the row header layer
		IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyLayerStack.getBodyDataProvider());
		DataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
		ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, bodyLayerStack, bodyLayerStack.getSelectionLayer());
		
		//build the corner layer
		IDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
		DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
		ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, sortHeaderLayer);
		
		//build the grid layer
		GridLayer gridLayer = new GridLayer(bodyLayerStack, sortHeaderLayer, rowHeaderLayer, cornerLayer);
		
		//set the group by header on top of the grid
		CompositeLayer compositeGridLayer = new CompositeLayer(1, 2);
		final GroupByHeaderLayer groupByHeaderLayer = 
				new GroupByHeaderLayer(bodyLayerStack.getGroupByModel(), gridLayer, columnHeaderDataProvider);
		compositeGridLayer.setChildLayer(GroupByHeaderLayer.GROUP_BY_REGION, groupByHeaderLayer, 0, 0);
		compositeGridLayer.setChildLayer("Grid", gridLayer, 0, 1);
		
		//turn the auto configuration off as we want to add our header menu configuration
		final NatTable natTable = new NatTable(container, compositeGridLayer, false);
		
		//as the autoconfiguration of the NatTable is turned off, we have to add the 
		//DefaultNatTableStyleConfiguration and the ConfigRegistry manually	
		natTable.setConfigRegistry(configRegistry);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());

		//add some additional styling
		natTable.addConfiguration(new AbstractRegistryConfiguration() {
			
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
				configRegistry.registerConfigAttribute(
						CellConfigAttributes.CELL_PAINTER, 
						new CheckBoxPainter(), 
						DisplayMode.NORMAL, 
						ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 4);
			}
		});
		
		//add sorting configuration
		natTable.addConfiguration(new SingleClickSortConfiguration());
		
		sumMoneySummaryProvider = 
				new SummationGroupBySummaryProvider<ExtendedPersonWithAddress>(columnPropertyAccessor);
		avgMoneySummaryProvider = 
				new AverageMoneyGroupBySummaryProvider();
		
		//add group by summary configuration
		natTable.addConfiguration(new AbstractRegistryConfiguration() {
			
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
				configRegistry.registerConfigAttribute(GroupByConfigAttributes.GROUP_BY_SUMMARY_PROVIDER,
						sumMoneySummaryProvider,
						DisplayMode.NORMAL, GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + 3);

				configRegistry.registerConfigAttribute(GroupByConfigAttributes.GROUP_BY_SUMMARY_PROVIDER,
						new AverageAgeGroupBySummaryProvider(),
						DisplayMode.NORMAL, GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + 2);

				configRegistry.registerConfigAttribute(
						GroupByConfigAttributes.GROUP_BY_CHILD_COUNT_PATTERN,
						"[{0}] - ({1})");
			}
		});
		
		//add group by header configuration
		natTable.addConfiguration(new GroupByHeaderMenuConfiguration(natTable, groupByHeaderLayer));

		natTable.addConfiguration(new AbstractHeaderMenuConfiguration(natTable) {
			
			@Override
			protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
				return super.createColumnHeaderMenu(natTable)
							.withHideColumnMenuItem()
							.withShowAllColumnsMenuItem()
							.withStateManagerMenuItemProvider();
			}
			
			@Override
			protected PopupMenuBuilder createCornerMenu(NatTable natTable) {
				return super.createCornerMenu(natTable)
							.withShowAllColumnsMenuItem()
							.withStateManagerMenuItemProvider();
			}
		});
		
		natTable.configure();
		
		natTable.registerCommandHandler(new DisplayPersistenceDialogCommandHandler(natTable));
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
		
		Composite buttonPanel = new Composite(container, SWT.NONE);
		buttonPanel.setLayout(new RowLayout());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonPanel);
		
		Button toggleHeaderButton = new Button(buttonPanel, SWT.PUSH);
		toggleHeaderButton.setText("Toggle Group By Header");
		toggleHeaderButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				groupByHeaderLayer.setVisible(!groupByHeaderLayer.isVisible());
			}
		});
		
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
		
		Button toggleMoneySummaryButton = new Button(buttonPanel, SWT.PUSH);
		toggleMoneySummaryButton.setText("Toggle Money Group Summary (SUM/AVG)");
		toggleMoneySummaryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//clear the group by summary cache so the new summary calculation gets triggered
				bodyLayerStack.getBodyDataLayer().clearCache();
				
				useMoneySum = !useMoneySum;
				if (useMoneySum) {
					configRegistry.registerConfigAttribute(GroupByConfigAttributes.GROUP_BY_SUMMARY_PROVIDER,
							sumMoneySummaryProvider,
							DisplayMode.NORMAL, GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + 3);
				}
				else {
					configRegistry.registerConfigAttribute(GroupByConfigAttributes.GROUP_BY_SUMMARY_PROVIDER,
							avgMoneySummaryProvider,
							DisplayMode.NORMAL, GroupByDataLayer.GROUP_BY_COLUMN_PREFIX + 3);
				}
				natTable.doCommand(new VisualRefreshCommand());
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
		
		private final SortedList<T> sortedList;
		
		private final IDataProvider bodyDataProvider;
		
		private final GroupByDataLayer<T> bodyDataLayer;
		
		private final SelectionLayer selectionLayer;
		
		private final GroupByModel groupByModel = new GroupByModel();
		
		public BodyLayerStack(List<T> values, IColumnPropertyAccessor<T> columnPropertyAccessor, ConfigRegistry configRegistry) {
			//wrapping of the list to show into GlazedLists
			//see http://publicobject.com/glazedlists/ for further information
			EventList<T> eventList = GlazedLists.eventList(values);
			TransformedList<T, T> rowObjectsGlazedList = GlazedLists.threadSafeList(eventList);
			
			//use the SortedList constructor with 'null' for the Comparator because the Comparator
			//will be set by configuration
			this.sortedList = new SortedList<T>(rowObjectsGlazedList, null);
			
			//Use the GroupByDataLayer instead of the default DataLayer
			this.bodyDataLayer = new GroupByDataLayer<T>(
					getGroupByModel(), sortedList, columnPropertyAccessor, configRegistry);
			//get the IDataProvider that was created by the GroupByDataLayer
			this.bodyDataProvider = bodyDataLayer.getDataProvider();
			
			//layer for event handling of GlazedLists and PropertyChanges
			GlazedListsEventLayer<T> glazedListsEventLayer = 
				new GlazedListsEventLayer<T>(bodyDataLayer, sortedList);

			ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(glazedListsEventLayer);
			ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);
			this.selectionLayer = new SelectionLayer(columnHideShowLayer);
			
			//add a tree layer to visualise the grouping
			TreeLayer treeLayer = new TreeLayer(selectionLayer, bodyDataLayer.getTreeRowModel());

			ViewportLayer viewportLayer = new ViewportLayer(treeLayer);
			
			setUnderlyingLayer(viewportLayer);
		}

		public SelectionLayer getSelectionLayer() {
			return this.selectionLayer;
		}
		
		public SortedList<T> getSortedList() {
			return this.sortedList;
		}

		public IDataProvider getBodyDataProvider() {
			return this.bodyDataProvider;
		}

		public GroupByDataLayer<T> getBodyDataLayer() {
			return this.bodyDataLayer;
		}
		
		public GroupByModel getGroupByModel() {
			return this.groupByModel;
		}
	}
	
	/**
	 * Example implementation for a typed IGroupBySummaryProvider that calculates the average age of
	 * ExtendedPersonWithAddress objects in a grouping.
	 */
	class AverageAgeGroupBySummaryProvider implements IGroupBySummaryProvider<ExtendedPersonWithAddress> {

		@Override
		public Object summarize(int columnIndex, List<ExtendedPersonWithAddress> children) {
			int summaryValue = 0;
			for (ExtendedPersonWithAddress child : children) {
				summaryValue += child.getAge();
			}
			return summaryValue / (children.size() > 0 ? children.size() : 1);
		}
		
	}
	
	/**
	 * Example implementation for a typed IGroupBySummaryProvider that calculates the average money of
	 * ExtendedPersonWithAddress objects in a grouping.
	 */
	class AverageMoneyGroupBySummaryProvider implements IGroupBySummaryProvider<ExtendedPersonWithAddress> {

		@Override
		public Object summarize(int columnIndex, List<ExtendedPersonWithAddress> children) {
			int summaryValue = 0;
			for (ExtendedPersonWithAddress child : children) {
				summaryValue += child.getMoney();
			}
			return summaryValue / (children.size() > 0 ? children.size() : 1);
		}
		
	}
}
