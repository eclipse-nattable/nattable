/*******************************************************************************
 * Copyright (c) 2018 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples._600_GlazedLists._604_Tree;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.EditableRule;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.car.CarService;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.DefaultGlazedListsFilterStrategy;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.hierarchical.HierarchicalWrapperSortModel;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowHeaderComposite;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.config.DefaultGridLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalHelper;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalSpanningDataProvider;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalTreeLayer;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalWrapper;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalWrapperComparator;
import org.eclipse.nebula.widgets.nattable.hierarchical.action.HierarchicalTreeExpandCollapseAction;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.SpanningDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.event.RowDeleteEvent;
import org.eclipse.nebula.widgets.nattable.painter.cell.ComboBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.preserve.PreserveSelectionModel;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.tree.action.TreeExpandCollapseAction;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeCollapseAllCommand;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandAllCommand;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandToLevelCommand;
import org.eclipse.nebula.widgets.nattable.tree.config.TreeLayerExpandCollapseKeyBindings;
import org.eclipse.nebula.widgets.nattable.tree.painter.TreeImagePainter;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellLabelMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellPainterMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
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
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;

/**
 * Example showing the HierarchicalTreeLayer.
 */
public class _6045_HierarchicalTreeLayerExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(800, 400, new _6045_HierarchicalTreeLayerExample());
    }

    @Override
    public String getDescription() {
        return "This example shows the usage of the HierarchicalTreeLayer to implement a multi level tree.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());

        // mapping from property to label, needed for column header labels
        Map<String, String> propertyToLabelMap = new HashMap<>();
        propertyToLabelMap.put("manufacturer", "Manufacturer");
        propertyToLabelMap.put("model", "Model");
        propertyToLabelMap.put("motors.identifier", "Identifier");
        propertyToLabelMap.put("motors.capacity", "Capacity");
        propertyToLabelMap.put("motors.capacityUnit", "Capacity Unit");
        propertyToLabelMap.put("motors.maximumSpeed", "Maximum Speed");
        propertyToLabelMap.put("motors.feedbacks.creationTime", "Creation Time");
        propertyToLabelMap.put("motors.feedbacks.classification", "Classification");
        propertyToLabelMap.put("motors.feedbacks.comment", "Comment");

        ConfigRegistry configRegistry = new ConfigRegistry();

        BodyLayerStack bodyLayerStack = new BodyLayerStack(
                CarService.getInput(),
                CarService.PROPERTY_NAMES);

        // create the column header layer stack
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(CarService.PROPERTY_NAMES, propertyToLabelMap);
        DataLayer columnHeaderDataLayer = new DataLayer(columnHeaderDataProvider);
        ILayer columnHeaderLayer = new ColumnHeaderLayer(
                columnHeaderDataLayer,
                bodyLayerStack,
                bodyLayerStack.getSelectionLayer());

        // add the SortHeaderLayer to the column header layer stack
        final SortHeaderLayer<HierarchicalWrapper> sortHeaderLayer =
                new SortHeaderLayer<>(
                        columnHeaderLayer,
                        new HierarchicalWrapperSortModel(
                                bodyLayerStack.getSortedList(),
                                bodyLayerStack.getColumnPropertyAccessor(),
                                bodyLayerStack.getTreeLayer().getLevelIndexMapping(),
                                columnHeaderDataLayer,
                                configRegistry),
                        false);

        // add the filter row functionality
        final FilterRowHeaderComposite<HierarchicalWrapper> filterRowHeaderLayer =
                new FilterRowHeaderComposite<>(
                        new DefaultGlazedListsFilterStrategy<>(
                                bodyLayerStack.getFilterList(),
                                bodyLayerStack.getColumnPropertyAccessor(),
                                configRegistry),
                        sortHeaderLayer,
                        columnHeaderDataLayer.getDataProvider(),
                        configRegistry);

        filterRowHeaderLayer.addConfigLabelAccumulatorForRegion(GridRegion.FILTER_ROW, new IConfigLabelAccumulator() {
            @Override
            public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
                int columnIndex = filterRowHeaderLayer.getColumnIndexByPosition(columnPosition);
                // if the conversion to index returns -1, we reached the level
                // header column
                if (columnIndex < 0) {
                    configLabels.addLabelOnTop(HierarchicalTreeLayer.LEVEL_HEADER_CELL);
                }
            }
        });

        // create the composite layer composed with the prior created layer
        // stacks
        CompositeLayer compositeLayer = new CompositeLayer(1, 2);
        compositeLayer.setChildLayer(GridRegion.COLUMN_HEADER, filterRowHeaderLayer, 0, 0);
        compositeLayer.setChildLayer(GridRegion.BODY, bodyLayerStack, 0, 1);
        compositeLayer.addConfiguration(new DefaultGridLayerConfiguration(compositeLayer) {
            @Override
            protected void addAlternateRowColoringConfig(CompositeLayer gridLayer) {
                // do nothing to avoid the default grid alternate row coloring
                // needed because the alternate row coloring in the hierarchical
                // tree
                // is based on the spanned cells and not per individual row
                // position
            }
        });

        NatTable natTable = new NatTable(container, compositeLayer, false);
        natTable.setConfigRegistry(configRegistry);

        natTable.addConfiguration(new DefaultNatTableStyleConfiguration() {
            {
                this.vAlign = VerticalAlignmentEnum.TOP;
                this.hAlign = HorizontalAlignmentEnum.LEFT;
                this.cellPainter = new PaddingDecorator(new TextPainter(), 2);
            }
        });

        // add editing configuration
        natTable.addConfiguration(new AbstractRegistryConfiguration() {

            @Override
            public void configureRegistry(IConfigRegistry configRegistry) {
                // make identifier editable
                configRegistry.registerConfigAttribute(
                        EditConfigAttributes.CELL_EDITABLE_RULE,
                        EditableRule.ALWAYS_EDITABLE,
                        DisplayMode.NORMAL,
                        ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 2);

                // make capacity unit editable
                configRegistry.registerConfigAttribute(
                        EditConfigAttributes.CELL_EDITABLE_RULE,
                        EditableRule.ALWAYS_EDITABLE,
                        DisplayMode.NORMAL,
                        ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 4);
                configRegistry.registerConfigAttribute(
                        EditConfigAttributes.CELL_EDITOR,
                        new ComboBoxCellEditor("KW", "PS"),
                        DisplayMode.NORMAL,
                        ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 4);
                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.CELL_PAINTER,
                        new PaddingDecorator(new ComboBoxPainter(), 2),
                        DisplayMode.NORMAL,
                        ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 4);

                // make comment editable
                configRegistry.registerConfigAttribute(
                        EditConfigAttributes.CELL_EDITABLE_RULE,
                        EditableRule.ALWAYS_EDITABLE,
                        DisplayMode.NORMAL,
                        ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 8);
            }
        });

        // adds the key bindings that allows pressing space bar to
        // expand/collapse tree nodes
        natTable.addConfiguration(
                new TreeLayerExpandCollapseKeyBindings(
                        bodyLayerStack.getTreeLayer(),
                        bodyLayerStack.getSelectionLayer()));

        // add sorting configuration
        natTable.addConfiguration(new SingleClickSortConfiguration());

        // add some additional filter configuration
        natTable.addConfiguration(new AbstractRegistryConfiguration() {

            @Override
            public void configureRegistry(IConfigRegistry configRegistry) {
                Style style = new Style();
                style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.getColor(173, 216, 230));
                configRegistry.registerConfigAttribute(
                        CellConfigAttributes.CELL_STYLE,
                        style,
                        DisplayMode.NORMAL,
                        GridRegion.FILTER_ROW);
            }
        });

        natTable.addConfiguration(new HeaderMenuConfiguration(natTable));
        natTable.addConfiguration(new AbstractUiBindingConfiguration() {

            @Override
            public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
                uiBindingRegistry.registerMouseDownBinding(
                        new CellLabelMouseEventMatcher(GridRegion.BODY, MouseEventMatcher.RIGHT_BUTTON, HierarchicalTreeLayer.LEVEL_HEADER_CELL),
                        new PopupMenuAction(new PopupMenuBuilder(natTable)
                                .withHideRowPositionMenuItem()
                                .withShowAllRowsMenuItem()
                                .build()));
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

        Button expandAllFirstLevelButton = new Button(buttonPanel, SWT.PUSH);
        expandAllFirstLevelButton.setText("Expand All First Level");
        expandAllFirstLevelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                natTable.doCommand(new TreeExpandToLevelCommand(0));
            }
        });

        Button toggleExpandButton = new Button(buttonPanel, SWT.PUSH);
        toggleExpandButton.setText("Enable Advanced Expand");
        toggleExpandButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                _6045_HierarchicalTreeLayerExample.this.advancedExpandEnabled = !_6045_HierarchicalTreeLayerExample.this.advancedExpandEnabled;
                if (_6045_HierarchicalTreeLayerExample.this.advancedExpandEnabled) {
                    toggleExpandButton.setText("Disable Advanced Expand");
                    // configure advanced action
                    natTable.getUiBindingRegistry().registerFirstSingleClickBinding(
                            _6045_HierarchicalTreeLayerExample.this.treeImagePainterMouseEventMatcher, _6045_HierarchicalTreeLayerExample.this.advancedTreeExpandCollapseAction);
                } else {
                    toggleExpandButton.setText("Enable Advanced Expand");
                    // configure simple action
                    natTable.getUiBindingRegistry().registerFirstSingleClickBinding(
                            _6045_HierarchicalTreeLayerExample.this.treeImagePainterMouseEventMatcher, _6045_HierarchicalTreeLayerExample.this.simpleTreeExpandCollapseAction);
                }
            }
        });

        Button multiReorderButton = new Button(buttonPanel, SWT.PUSH);
        multiReorderButton.setText("Reorder Second Level");
        multiReorderButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                List<Integer> fromColumnPositions = Arrays.asList(4, 5);
                natTable.doCommand(new MultiColumnReorderCommand(natTable, fromColumnPositions, 8));
            }
        });

        Button deleteButton = new Button(buttonPanel, SWT.PUSH);
        deleteButton.setText("Delete Row 4");
        deleteButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // remove the element
                HierarchicalWrapper rowObject = bodyLayerStack.getFilterList().remove(3);

                // fire the event to refresh
                bodyLayerStack.getBodyDataLayer().fireLayerEvent(
                        new RowDeleteEvent(bodyLayerStack.getBodyDataLayer(), 3));

                bodyLayerStack.getTreeLayer().cleanupRetainedCollapsedNodes(rowObject);
            }
        });

        return container;
    }

    private boolean advancedExpandEnabled = false;
    private TreeExpandCollapseAction simpleTreeExpandCollapseAction =
            new TreeExpandCollapseAction();
    private HierarchicalTreeExpandCollapseAction advancedTreeExpandCollapseAction =
            new HierarchicalTreeExpandCollapseAction(1);
    private CellPainterMouseEventMatcher treeImagePainterMouseEventMatcher =
            new CellPainterMouseEventMatcher(
                    GridRegion.BODY,
                    MouseEventMatcher.LEFT_BUTTON,
                    TreeImagePainter.class);

    /**
     * Always encapsulate the body layer stack in an AbstractLayerTransform to
     * ensure that the index transformations are performed in later commands.
     */
    class BodyLayerStack extends AbstractLayerTransform {
        private final EventList<HierarchicalWrapper> eventList;
        private final SortedList<HierarchicalWrapper> sortedList;
        private final FilterList<HierarchicalWrapper> filterList;

        private final HierarchicalReflectiveColumnPropertyAccessor columnPropertyAccessor;
        private final IRowDataProvider<HierarchicalWrapper> bodyDataProvider;

        private final HierarchicalSpanningDataProvider spanningDataProvider;

        private final DataLayer bodyDataLayer;

        private final SelectionLayer selectionLayer;

        private final ViewportLayer viewportLayer;

        private final HierarchicalTreeLayer treeLayer;

        public BodyLayerStack(List<?> values, String[] propertyNames) {

            this.columnPropertyAccessor = new HierarchicalReflectiveColumnPropertyAccessor(propertyNames);

            // de-normalize the object graph without parent structure objects
            List<HierarchicalWrapper> data = HierarchicalHelper.deNormalize(values, false, propertyNames);

            this.eventList = GlazedLists.eventList(data);
            TransformedList<HierarchicalWrapper, HierarchicalWrapper> rowObjectsGlazedList = GlazedLists.threadSafeList(this.eventList);

            // use the SortedList constructor with a
            // HierarchicalWrapperComparator for initial sorting
            // for dynamic sorting the Comparator will be set by configuration
            this.sortedList = new SortedList<>(
                    rowObjectsGlazedList,
                    new HierarchicalWrapperComparator(this.columnPropertyAccessor, HierarchicalHelper.getLevelIndexMapping(propertyNames)));
            this.filterList = new FilterList<>(this.sortedList);

            this.bodyDataProvider = new ListDataProvider<>(this.filterList, this.columnPropertyAccessor);
            this.spanningDataProvider = new HierarchicalSpanningDataProvider(this.bodyDataProvider, propertyNames);
            this.bodyDataLayer = new SpanningDataLayer(this.spanningDataProvider);

            // simply apply labels for every column by index
            this.bodyDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());

            // layer for event handling of GlazedLists and PropertyChanges
            GlazedListsEventLayer<HierarchicalWrapper> glazedListsEventLayer = new GlazedListsEventLayer<>(this.bodyDataLayer, this.filterList);

            ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(glazedListsEventLayer, false);
            ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);
            RowHideShowLayer rowHideShowLayer = new RowHideShowLayer(columnHideShowLayer);

            this.selectionLayer = new SelectionLayer(rowHideShowLayer);

            // add the PreserveSelectionModel to avoid that the selection is
            // cleared on expand collapse
            this.selectionLayer.setSelectionModel(new PreserveSelectionModel<>(
                    this.selectionLayer, this.bodyDataProvider, new IRowIdAccessor<HierarchicalWrapper>() {

                        @Override
                        public Serializable getRowId(HierarchicalWrapper rowObject) {
                            return rowObject.hashCode();
                        }
                    }));

            this.treeLayer = new HierarchicalTreeLayer(this.selectionLayer, this.filterList, propertyNames, this.selectionLayer);
            this.viewportLayer = new ViewportLayer(this.treeLayer);

            setUnderlyingLayer(this.viewportLayer);
        }

        public SelectionLayer getSelectionLayer() {
            return this.selectionLayer;
        }

        public HierarchicalTreeLayer getTreeLayer() {
            return this.treeLayer;
        }

        public ViewportLayer getViewportLayer() {
            return this.viewportLayer;
        }

        public IDataProvider getBodyDataProvider() {
            return this.bodyDataProvider;
        }

        public HierarchicalSpanningDataProvider getSpanningDataProvider() {
            return this.spanningDataProvider;
        }

        public SortedList<HierarchicalWrapper> getSortedList() {
            return this.sortedList;
        }

        public FilterList<HierarchicalWrapper> getFilterList() {
            return this.filterList;
        }

        public HierarchicalReflectiveColumnPropertyAccessor getColumnPropertyAccessor() {
            return this.columnPropertyAccessor;
        }

        public DataLayer getBodyDataLayer() {
            return this.bodyDataLayer;
        }
    }
}
