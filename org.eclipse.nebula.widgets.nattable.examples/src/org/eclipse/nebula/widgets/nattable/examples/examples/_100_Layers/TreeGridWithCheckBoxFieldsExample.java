/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.examples._100_Layers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultComparator;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.CheckBoxStateEnum;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.action.CellEditDragMode;
import org.eclipse.nebula.widgets.nattable.edit.action.MouseEditAction;
import org.eclipse.nebula.widgets.nattable.edit.action.ToggleCheckBoxColumnAction;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.edit.editor.CheckBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeData;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeRowModel;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
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
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ColumnHeaderCheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TreeCheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.BeveledBorderDecorator;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortConfigAttributes;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.sort.painter.SortableHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.tree.ITreeData;
import org.eclipse.nebula.widgets.nattable.tree.SortableTreeComparator;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellPainterMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TreeList;

public class TreeGridWithCheckBoxFieldsExample extends AbstractNatExample {

	protected static final String NO_SORT_LABEL = "noSortLabel";
	
	public static void main(String[] args) {
		StandaloneNatExampleRunner.run(800, 400, new TreeGridWithCheckBoxFieldsExample());
	}
	
	public Control createExampleControl(Composite parent) {
		ConfigRegistry configRegistry = new ConfigRegistry();
		configRegistry.registerConfigAttribute(SortConfigAttributes.SORT_COMPARATOR, new DefaultComparator());
		
		// Underlying data source
		createDatums();
		EventList<Datum> eventList = GlazedLists.eventList(datums.values());
		SortedList<Datum> sortedList = new SortedList<Datum>(eventList, null);
//		TreeList <RowDataFixture> treeList = new TreeList<RowDataFixture>(eventList, new RowDataFixtureTreeFormat(), new RowDataFixtureExpansionModel());
		
		String[] propertyNames = new String[] { "self", "bar" };
		IColumnPropertyAccessor<Datum> columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<Datum>(propertyNames);
		
		// Column header layer
		IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames);
		DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
		
		ISortModel sortModel = new GlazedListsSortModel<Datum>(
				sortedList,
				columnPropertyAccessor,
				configRegistry, 
				columnHeaderDataLayer);
		
		final TreeList <Datum> treeList = new TreeList<Datum>(sortedList, new DatumTreeFormat(sortModel), new DatumExpansionModel());
		GlazedListTreeData <Datum> treeData = new DatumTreeData(treeList);
		
		GlazedListsDataProvider<Datum> bodyDataProvider = new GlazedListsDataProvider<Datum>(treeList, columnPropertyAccessor);
		final DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
		
		// Handle update of CheckBoxField objects in column 0
		bodyDataLayer.registerCommandHandler(new UpdateDataCommandHandler(bodyDataLayer) {
			@Override
			protected boolean doCommand(UpdateDataCommand command) {
				int columnPosition = command.getColumnPosition();
				int rowPosition = command.getRowPosition();
				
				if (columnPosition == 0) {
					Datum datum = (Datum) bodyDataLayer.getDataProvider().getDataValue(columnPosition, rowPosition);
					datum.setOn((Boolean) command.getNewValue());
					
					bodyDataLayer.fireLayerEvent(new CellVisualChangeEvent(bodyDataLayer, columnPosition, rowPosition));
					return true;
				} else {
					return super.doCommand(command);
				}
			}
		});
		
		// Body layer
		ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(bodyDataLayer);
		ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);
		SelectionLayer selectionLayer = new SelectionLayer(columnHideShowLayer);
		
		// Switch the ITreeRowModel implementation between using native grid Hide/Show or GlazedList TreeList Hide/Show  
//		TreeLayer treeLayer = new TreeLayer(selectionLayer, new TreeRowModel<Datum>(treeData), true);
		final TreeLayer treeLayer = new TreeLayer(selectionLayer, new GlazedListTreeRowModel<Datum>(treeData));
		
		ViewportLayer viewportLayer = new ViewportLayer(treeLayer);
		
		ColumnHeaderLayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, viewportLayer, selectionLayer);
		//	Note: The column header layer is wrapped in a filter row composite.
		//	This plugs in the filter row functionality
	

		ColumnOverrideLabelAccumulator labelAccumulator = new ColumnOverrideLabelAccumulator(columnHeaderDataLayer);
		columnHeaderDataLayer.setConfigLabelAccumulator(labelAccumulator);

		// Register labels
		SortHeaderLayer<Datum> sortHeaderLayer = new SortHeaderLayer<Datum>(
				columnHeaderLayer, 
				sortModel, 
				false);

		// Row header layer
		DefaultRowHeaderDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
		DefaultRowHeaderDataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
		RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, viewportLayer, selectionLayer);

		// Corner layer
		DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
		DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
//		CornerLayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);
		CornerLayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, sortHeaderLayer);

		// Grid
		GridLayer gridLayer = new GridLayer(
				viewportLayer,
//				columnHeaderLayer,
				sortHeaderLayer,
				rowHeaderLayer,
				cornerLayer);
		
		NatTable natTable = new NatTable(parent, gridLayer, false);
		natTable.setConfigRegistry(configRegistry);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new HeaderMenuConfiguration(natTable));
		natTable.addConfiguration(new SingleClickSortConfiguration());
		
		// Uncomment to see the native tree list printed to stout.
//		printTree(treeList, treeData);
		
		columnHeaderDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());
		
		final ColumnHeaderCheckBoxPainter columnHeaderCheckBoxPainter = new ColumnHeaderCheckBoxPainter(bodyDataLayer) {
			@Override
			protected Boolean convertDataType(ILayerCell cell, IConfigRegistry configRegistry) {
				Datum dataValue = (Datum) cell.getDataValue();
				return dataValue.isOn();
			}
		};
		final ICellPainter checkBoxPainter = new TreeCheckBoxPainter() {
			@Override
			protected CheckBoxStateEnum getCheckBoxState(ILayerCell cell) {
				Datum dataValue = (Datum) cell.getDataValue();
				return dataValue.getCheckBoxState();
			}
		};
		natTable.addConfiguration(new AbstractRegistryConfiguration() {
			public void configureRegistry(IConfigRegistry configRegistry) {
				// Column header
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
						new BeveledBorderDecorator(new CellPainterDecorator(new SortableHeaderTextPainter(), CellEdgeEnum.LEFT, columnHeaderCheckBoxPainter)),
						DisplayMode.NORMAL,
						ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 0);
				
				configRegistry.registerConfigAttribute(
						CellConfigAttributes.CELL_PAINTER,
						new BackgroundPainter(
								new CellPainterDecorator(
										new TextPainter() {
											@Override
											protected String convertDataType(ILayerCell cell, IConfigRegistry configRegistry) {
												Datum dataValue = (Datum) cell.getDataValue();
												return dataValue.getName();
											}
										},
										CellEdgeEnum.LEFT,
										checkBoxPainter
								)
						),
						DisplayMode.NORMAL,
						TreeLayer.TREE_COLUMN_CELL
				);
				
				configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultBooleanDisplayConverter(), DisplayMode.NORMAL, TreeLayer.TREE_COLUMN_CELL);
				configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT, TreeLayer.TREE_COLUMN_CELL);
				configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, new CheckBoxCellEditor() {
					@Override
					public void setCanonicalValue(Object canonicalValue) {
						Datum value = (Datum) canonicalValue;
						super.setCanonicalValue(value.isOn());
					}
				}, DisplayMode.NORMAL, TreeLayer.TREE_COLUMN_CELL);
			}
			
			@Override
			public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
				uiBindingRegistry.registerFirstSingleClickBinding(
						new CellPainterMouseEventMatcher(GridRegion.COLUMN_HEADER, MouseEventMatcher.LEFT_BUTTON, columnHeaderCheckBoxPainter),
						new ToggleCheckBoxColumnAction(columnHeaderCheckBoxPainter, bodyDataLayer)
				);
				
				uiBindingRegistry.registerFirstSingleClickBinding(
		                new CellPainterMouseEventMatcher(GridRegion.BODY, MouseEventMatcher.LEFT_BUTTON, checkBoxPainter),
		                new MouseEditAction());
				
				uiBindingRegistry.registerFirstMouseDragMode(
		                new CellPainterMouseEventMatcher(GridRegion.BODY, MouseEventMatcher.LEFT_BUTTON, checkBoxPainter),
						new CellEditDragMode());
			}
		});
		
		natTable.configure();
		return natTable;
	}

	private static class DatumTreeData extends GlazedListTreeData<Datum>{

		public DatumTreeData(TreeList<Datum> treeList) {
			super(treeList);
		}

		@Override
		public String formatDataForDepth(int depth, Datum object) {
			return object.getName();
		}

		
	}
	
	private static class DatumTreeFormat implements TreeList.Format<Datum> {
		
		private final ISortModel sortModel;

		public DatumTreeFormat(ISortModel sortModel) {
			this.sortModel = sortModel;
		}
		
		public void getPath(List<Datum> path, Datum element) {
			path.add(element);
			Datum parent = element.getParent();
			while (parent != null) {
				path.add(parent);
				parent = parent.getParent();
			}
			Collections.reverse(path);
		}
		
		public boolean allowsChildren(Datum element) {
			return true;
		}

		public Comparator<Datum> getComparator(int depth) {
			return new SortableTreeComparator<Datum>(GlazedLists.beanPropertyComparator(Datum.class, "self"), sortModel);
		}
	}

	private static class DatumExpansionModel implements TreeList.ExpansionModel<Datum> {
		public boolean isExpanded(Datum element, List<Datum> path) {
			return true;
		}

		public void setExpanded(Datum element,	List<Datum> path, boolean expanded) {
		}
	}
	
	private void printTree(TreeList <Datum> treeList, ITreeData<Datum> treeData){
		System.out.println(treeList.size());
		   for (int i = 0; i < treeList.size(); i++) {
	            final Datum location = treeList.get(i);
	            final int depth = treeList.depth(i);
	            final boolean hasChildren = treeList.hasChildren(i);
	            final boolean isExpanded = treeList.isExpanded(i);

	            for (int j = 0; j < depth; j++)
	                System.out.print("\t");

	            if (hasChildren)
	                System.out.print(isExpanded ? "- " : "+ ");
	            else
	                System.out.print("  ");

	            System.out.println(treeData.formatDataForDepth(depth, location));
	        }
	}
	
	public class Datum implements Comparable<Datum> {

		private final Datum parent;
		private final List<Datum> children = new ArrayList<Datum>();
		
		private final String name;
		private boolean on;
		private int bar;
		
		public Datum(Datum parent, String name, boolean on, int bar) {
			this.parent = parent;
			if (parent != null) {
				parent.addChild(this);
			}
			
			this.name = name;
			this.on = on;
			this.bar = bar;
		}
		
		public Datum getParent() {
			return parent;
		}
		
		public void addChild(Datum child) {
			children.add(child);
		}
		
		public List<Datum> getChildren() {
			return children;
		}
		
		public Datum getSelf() {
			return this;
		}
		
		public String getName() {
			return name;
		}
		
		public boolean isOn() {
			if (children.size() == 0) {
				return on;
			} else {
				return getCheckBoxState() == CheckBoxStateEnum.CHECKED;
			}
		}
		
		public void setOn(boolean on) {
			if (children.size() == 0) {
				this.on = on;
			} else {
				for (Datum child : children) {
					child.setOn(on);
				}
			}
		}
		
		public int getBar() {
			return bar;
		}
		
		public CheckBoxStateEnum getCheckBoxState() {
			if (children.size() == 0) {
				return on ? CheckBoxStateEnum.CHECKED : CheckBoxStateEnum.UNCHECKED;
			} else {
				boolean atLeastOneChildChecked = false;
				boolean atLeastOneChildUnchecked = false;
				
				for (Datum child : children) {
					CheckBoxStateEnum childCheckBoxState = child.getCheckBoxState();
					switch (childCheckBoxState) {
					case CHECKED:
						atLeastOneChildChecked = true;
						break;
					case SEMICHECKED:
						return CheckBoxStateEnum.SEMICHECKED;
					case UNCHECKED:
						atLeastOneChildUnchecked = true;
						break;
					}
				}
				
				if (atLeastOneChildChecked) {
					if (atLeastOneChildUnchecked) {
						return CheckBoxStateEnum.SEMICHECKED;
					} else {
						return CheckBoxStateEnum.CHECKED;
					}
				} else {
					return CheckBoxStateEnum.UNCHECKED;
				}
			}
		}
		
		@Override
		public String toString() {
			return "[" +
					"parent=" + parent +
					", name=" + name +
					", on=" + on +
					", bar=" + bar +
					"]";
		}
		
		/**
		 * Comparison is based on name only
		 */
		public int compareTo(Datum o) {
			return this.name.compareTo(o.name);
		}
		
	}
	
	private Map<String, Datum> datums = new HashMap<String, Datum>();
	
	private void createDatum(String parent, String foo, boolean fooFlag, int bar) {
		Datum datum = new Datum(datums.get(parent), foo, fooFlag, bar);
		datums.put(foo, datum);
	}
	
	private void createDatums() {
		createDatum(null, "root", false, 1);
			createDatum("root", "A", false, 10);
				createDatum("A", "A.1", false, 100);
				createDatum("A", "A.2", false, 110);
				createDatum("A", "A.3", true, 120);
			createDatum("root", "B", true, 20);
				createDatum("B", "B.1", true, 200);
				createDatum("B", "B.2", true, 210);
			createDatum("root", "C", false, 30);
				createDatum("C", "C.1", true, 330);
				createDatum("C", "C.2", false, 370);
				createDatum("C", "C.3", true, 322);
				createDatum("C", "C.4", false, 310);
				createDatum("C", "C.5", true, 315);
		createDatum(null, "root2", false, 2);
			createDatum("root2", "X", false, 70);
				createDatum("X", "X.1", false, 700);
				createDatum("X", "X.2", false, 710);
				createDatum("X", "X.3", false, 720);
			createDatum("root2", "Y", false, 80);
				createDatum("Y", "Y.1", false, 800);
				createDatum("Y", "Y.2", false, 810);
			createDatum("root2", "Z", false, 90);
				createDatum("Z", "Z.1", false, 900);
				createDatum("Z", "Z.2", false, 910);
				createDatum("Z", "Z.3", false, 920);
				createDatum("Z", "Z.4", false, 930);
				createDatum("Z", "Z.5", false, 940);
	}
	
}
