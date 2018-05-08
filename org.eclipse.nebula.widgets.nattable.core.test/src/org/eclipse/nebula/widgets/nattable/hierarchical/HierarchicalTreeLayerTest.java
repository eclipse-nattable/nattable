/*****************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.hierarchical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.dataset.car.CarService;
import org.eclipse.nebula.widgets.nattable.dataset.car.Classification;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllColumnsCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.event.ShowRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalTreeLayer.HierarchicalTreeNode;
import org.eclipse.nebula.widgets.nattable.layer.AbstractDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.SpanningDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommand;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.search.CellValueAsStringComparator;
import org.eclipse.nebula.widgets.nattable.search.ISearchDirection;
import org.eclipse.nebula.widgets.nattable.search.command.SearchCommand;
import org.eclipse.nebula.widgets.nattable.search.strategy.GridSearchStrategy;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.selection.preserve.PreserveSelectionModel;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeCollapseAllCommand;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandAllCommand;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandCollapseCommand;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandToLevelCommand;
import org.eclipse.nebula.widgets.nattable.tree.config.DefaultTreeLayerConfiguration;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Before;
import org.junit.Test;

public class HierarchicalTreeLayerTest {

    private List<HierarchicalWrapper> data;
    private IRowDataProvider<HierarchicalWrapper> bodyDataProvider;
    private DataLayer bodyDataLayer;
    private ColumnReorderLayer columnReorderLayer;
    private ColumnHideShowLayer columnHideShowLayer;
    private SelectionLayer selectionLayer;
    private HierarchicalTreeLayer treeLayer;

    private LayerListenerFixture layerListener;

    @Before
    public void setup() {
        // de-normalize the object graph without parent structure objects
        this.data = HierarchicalHelper.deNormalize(CarService.getInput(), false, CarService.PROPERTY_NAMES_COMPACT);

        HierarchicalReflectiveColumnPropertyAccessor columnPropertyAccessor =
                new HierarchicalReflectiveColumnPropertyAccessor(CarService.PROPERTY_NAMES_COMPACT);

        this.bodyDataProvider = new ListDataProvider<>(this.data, columnPropertyAccessor);
        HierarchicalSpanningDataProvider spanningDataProvider = new HierarchicalSpanningDataProvider(this.bodyDataProvider, CarService.PROPERTY_NAMES_COMPACT);
        this.bodyDataLayer = new SpanningDataLayer(spanningDataProvider);

        // simply apply labels for every column by index
        this.bodyDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());
        this.columnReorderLayer = new ColumnReorderLayer(this.bodyDataLayer);
        this.columnHideShowLayer = new ColumnHideShowLayer(this.columnReorderLayer);
        this.selectionLayer = new SelectionLayer(this.columnHideShowLayer);
        this.treeLayer = new HierarchicalTreeLayer(this.selectionLayer, this.data, CarService.PROPERTY_NAMES_COMPACT);

        this.layerListener = new LayerListenerFixture();
        this.treeLayer.addLayerListener(this.layerListener);
    }

    @Test
    public void testIsLevelHeaderColumn() {
        assertTrue(this.treeLayer.isLevelHeaderColumn(0));
        assertFalse(this.treeLayer.isLevelHeaderColumn(1));
        assertFalse(this.treeLayer.isLevelHeaderColumn(2));
        assertTrue(this.treeLayer.isLevelHeaderColumn(3));
        assertFalse(this.treeLayer.isLevelHeaderColumn(4));
        assertFalse(this.treeLayer.isLevelHeaderColumn(5));
        assertTrue(this.treeLayer.isLevelHeaderColumn(6));
        assertFalse(this.treeLayer.isLevelHeaderColumn(7));
        assertFalse(this.treeLayer.isLevelHeaderColumn(8));

        this.treeLayer.setShowTreeLevelHeader(false);

        assertFalse(this.treeLayer.isLevelHeaderColumn(0));
        assertFalse(this.treeLayer.isLevelHeaderColumn(1));
        assertFalse(this.treeLayer.isLevelHeaderColumn(2));
        assertFalse(this.treeLayer.isLevelHeaderColumn(3));
        assertFalse(this.treeLayer.isLevelHeaderColumn(4));
        assertFalse(this.treeLayer.isLevelHeaderColumn(5));
        assertFalse(this.treeLayer.isLevelHeaderColumn(6));
        assertFalse(this.treeLayer.isLevelHeaderColumn(7));
        assertFalse(this.treeLayer.isLevelHeaderColumn(8));
    }

    @Test
    public void testIsLevelHeaderColumnOnHideShow() {
        assertTrue(this.treeLayer.isLevelHeaderColumn(0));
        assertTrue(this.treeLayer.isLevelHeaderColumn(3));
        assertTrue(this.treeLayer.isLevelHeaderColumn(6));

        this.treeLayer.doCommand(new ColumnHideCommand(this.treeLayer, 1));

        assertTrue(this.treeLayer.isLevelHeaderColumn(0));
        assertTrue(this.treeLayer.isLevelHeaderColumn(2));
        assertTrue(this.treeLayer.isLevelHeaderColumn(5));

        this.treeLayer.doCommand(new ShowAllColumnsCommand());

        this.treeLayer.doCommand(new ColumnHideCommand(this.treeLayer, 4));

        assertTrue(this.treeLayer.isLevelHeaderColumn(0));
        assertTrue(this.treeLayer.isLevelHeaderColumn(3));
        assertTrue(this.treeLayer.isLevelHeaderColumn(5));

        this.treeLayer.doCommand(new ShowAllColumnsCommand());

        assertTrue(this.treeLayer.isLevelHeaderColumn(0));
        assertTrue(this.treeLayer.isLevelHeaderColumn(3));
        assertTrue(this.treeLayer.isLevelHeaderColumn(6));
    }

    @Test
    public void testIsTreeColumn() {
        // with level header column 1 and 4 are node columns
        assertFalse(this.treeLayer.isTreeColumn(0));
        assertTrue(this.treeLayer.isTreeColumn(1));
        assertFalse(this.treeLayer.isTreeColumn(2));
        assertFalse(this.treeLayer.isTreeColumn(3));
        assertTrue(this.treeLayer.isTreeColumn(4));
        assertFalse(this.treeLayer.isTreeColumn(5));
        assertFalse(this.treeLayer.isTreeColumn(6));
        assertFalse(this.treeLayer.isTreeColumn(7));
        assertFalse(this.treeLayer.isTreeColumn(8));

        this.treeLayer.setShowTreeLevelHeader(false);

        // without level header column 0 and 2 are node columns
        assertTrue(this.treeLayer.isTreeColumn(0));
        assertFalse(this.treeLayer.isTreeColumn(1));
        assertTrue(this.treeLayer.isTreeColumn(2));
        assertFalse(this.treeLayer.isTreeColumn(3));
        assertFalse(this.treeLayer.isTreeColumn(4));
        assertFalse(this.treeLayer.isTreeColumn(5));
    }

    @Test
    public void testConfigLabelsByPosition() {
        LabelStack stack = this.treeLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(HierarchicalTreeLayer.LEVEL_HEADER_CELL));

        stack = this.treeLayer.getConfigLabelsByPosition(1, 0);
        assertEquals(4, stack.getLabels().size());
        assertTrue(stack.hasLabel(TreeLayer.TREE_COLUMN_CELL));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + 0));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_EXPANDED_CONFIG_TYPE));
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 0));

        stack = this.treeLayer.getConfigLabelsByPosition(2, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 1));

        stack = this.treeLayer.getConfigLabelsByPosition(3, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(HierarchicalTreeLayer.LEVEL_HEADER_CELL));

        stack = this.treeLayer.getConfigLabelsByPosition(4, 0);
        assertEquals(4, stack.getLabels().size());
        assertTrue(stack.hasLabel(TreeLayer.TREE_COLUMN_CELL));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + 0));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_EXPANDED_CONFIG_TYPE));
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 2));

        stack = this.treeLayer.getConfigLabelsByPosition(5, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 3));

        stack = this.treeLayer.getConfigLabelsByPosition(6, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(HierarchicalTreeLayer.LEVEL_HEADER_CELL));

        stack = this.treeLayer.getConfigLabelsByPosition(7, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 4));

        stack = this.treeLayer.getConfigLabelsByPosition(8, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 5));

        this.treeLayer.setShowTreeLevelHeader(false);

        stack = this.treeLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(4, stack.getLabels().size());
        assertTrue(stack.hasLabel(TreeLayer.TREE_COLUMN_CELL));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + 0));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_EXPANDED_CONFIG_TYPE));
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 0));

        stack = this.treeLayer.getConfigLabelsByPosition(1, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 1));

        stack = this.treeLayer.getConfigLabelsByPosition(2, 0);
        assertEquals(4, stack.getLabels().size());
        assertTrue(stack.hasLabel(TreeLayer.TREE_COLUMN_CELL));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + 0));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_EXPANDED_CONFIG_TYPE));
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 2));

        stack = this.treeLayer.getConfigLabelsByPosition(3, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 3));

        stack = this.treeLayer.getConfigLabelsByPosition(4, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 4));

        stack = this.treeLayer.getConfigLabelsByPosition(5, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 5));
    }

    @Test
    public void testExpandCollapseConfigLabelsByPosition() {
        LabelStack stack = this.treeLayer.getConfigLabelsByPosition(1, 0);
        assertEquals(4, stack.getLabels().size());
        assertTrue(stack.hasLabel(TreeLayer.TREE_COLUMN_CELL));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + 0));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_EXPANDED_CONFIG_TYPE));
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 0));

        // collapse first level - note that this is done by index
        this.treeLayer.expandOrCollapse(0, 0);

        stack = this.treeLayer.getConfigLabelsByPosition(1, 0);
        assertEquals(4, stack.getLabels().size());
        assertTrue(stack.hasLabel(TreeLayer.TREE_COLUMN_CELL));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + 0));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_COLLAPSED_CONFIG_TYPE));
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 0));

        // expand first level again
        this.treeLayer.expandOrCollapse(0, 0);

        stack = this.treeLayer.getConfigLabelsByPosition(4, 0);
        assertEquals(4, stack.getLabels().size());
        assertTrue(stack.hasLabel(TreeLayer.TREE_COLUMN_CELL));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + 0));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_EXPANDED_CONFIG_TYPE));
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 2));

        // collapse second level
        this.treeLayer.expandOrCollapse(2, 0);

        stack = this.treeLayer.getConfigLabelsByPosition(4, 0);
        assertEquals(4, stack.getLabels().size());
        assertTrue(stack.hasLabel(TreeLayer.TREE_COLUMN_CELL));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + 0));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_COLLAPSED_CONFIG_TYPE));
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 2));

        // expand second level again
        this.treeLayer.expandOrCollapse(2, 0);

        stack = this.treeLayer.getConfigLabelsByPosition(5, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 3));

        stack = this.treeLayer.getConfigLabelsByPosition(6, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(HierarchicalTreeLayer.LEVEL_HEADER_CELL));

        stack = this.treeLayer.getConfigLabelsByPosition(7, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 4));

        stack = this.treeLayer.getConfigLabelsByPosition(8, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 5));

        this.treeLayer.setShowTreeLevelHeader(false);

        stack = this.treeLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(4, stack.getLabels().size());
        assertTrue(stack.hasLabel(TreeLayer.TREE_COLUMN_CELL));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + 0));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_EXPANDED_CONFIG_TYPE));
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 0));

        // collapse first level
        this.treeLayer.expandOrCollapse(0, 0);

        stack = this.treeLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(4, stack.getLabels().size());
        assertTrue(stack.hasLabel(TreeLayer.TREE_COLUMN_CELL));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + 0));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_COLLAPSED_CONFIG_TYPE));
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 0));

        // expand first level again
        this.treeLayer.expandOrCollapse(0, 0);

        stack = this.treeLayer.getConfigLabelsByPosition(2, 0);
        assertEquals(4, stack.getLabels().size());
        assertTrue(stack.hasLabel(TreeLayer.TREE_COLUMN_CELL));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + 0));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_EXPANDED_CONFIG_TYPE));
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 2));

        // collapse second level
        this.treeLayer.expandOrCollapse(2, 0);

        stack = this.treeLayer.getConfigLabelsByPosition(2, 0);
        assertEquals(4, stack.getLabels().size());
        assertTrue(stack.hasLabel(TreeLayer.TREE_COLUMN_CELL));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + 0));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_COLLAPSED_CONFIG_TYPE));
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 2));
    }

    @Test
    public void testExpandCollapseConfigLabelsByPositionForChildColumns() {
        // collapse first level - note that this is done by index
        this.treeLayer.expandOrCollapse(0, 0);

        LabelStack stack = this.treeLayer.getConfigLabelsByPosition(1, 0);
        assertEquals(4, stack.getLabels().size());
        assertTrue(stack.hasLabel(TreeLayer.TREE_COLUMN_CELL));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + 0));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_COLLAPSED_CONFIG_TYPE));
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 0));

        stack = this.treeLayer.getConfigLabelsByPosition(2, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 1));

        stack = this.treeLayer.getConfigLabelsByPosition(3, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(HierarchicalTreeLayer.LEVEL_HEADER_CELL));

        stack = this.treeLayer.getConfigLabelsByPosition(4, 0);
        assertEquals(2, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 2));
        assertTrue(stack.hasLabel(HierarchicalTreeLayer.COLLAPSED_CHILD));

        stack = this.treeLayer.getConfigLabelsByPosition(5, 0);
        assertEquals(2, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 3));
        assertTrue(stack.hasLabel(HierarchicalTreeLayer.COLLAPSED_CHILD));

        stack = this.treeLayer.getConfigLabelsByPosition(6, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(HierarchicalTreeLayer.LEVEL_HEADER_CELL));

        stack = this.treeLayer.getConfigLabelsByPosition(7, 0);
        assertEquals(2, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 4));
        assertTrue(stack.hasLabel(HierarchicalTreeLayer.COLLAPSED_CHILD));

        stack = this.treeLayer.getConfigLabelsByPosition(8, 0);
        assertEquals(2, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 5));
        assertTrue(stack.hasLabel(HierarchicalTreeLayer.COLLAPSED_CHILD));

        // expand first level again
        this.treeLayer.expandOrCollapse(0, 0);

        // collapse second level
        this.treeLayer.expandOrCollapse(2, 0);

        stack = this.treeLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(HierarchicalTreeLayer.LEVEL_HEADER_CELL));

        stack = this.treeLayer.getConfigLabelsByPosition(1, 0);
        assertEquals(4, stack.getLabels().size());
        assertTrue(stack.hasLabel(TreeLayer.TREE_COLUMN_CELL));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + 0));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_EXPANDED_CONFIG_TYPE));
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 0));

        stack = this.treeLayer.getConfigLabelsByPosition(2, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 1));

        stack = this.treeLayer.getConfigLabelsByPosition(3, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(HierarchicalTreeLayer.LEVEL_HEADER_CELL));

        stack = this.treeLayer.getConfigLabelsByPosition(4, 0);
        assertEquals(4, stack.getLabels().size());
        assertTrue(stack.hasLabel(TreeLayer.TREE_COLUMN_CELL));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + 0));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_COLLAPSED_CONFIG_TYPE));
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 2));

        stack = this.treeLayer.getConfigLabelsByPosition(5, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 3));

        stack = this.treeLayer.getConfigLabelsByPosition(6, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(HierarchicalTreeLayer.LEVEL_HEADER_CELL));

        stack = this.treeLayer.getConfigLabelsByPosition(7, 0);
        assertEquals(2, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 4));
        assertTrue(stack.hasLabel(HierarchicalTreeLayer.COLLAPSED_CHILD));

        stack = this.treeLayer.getConfigLabelsByPosition(8, 0);
        assertEquals(2, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 5));
        assertTrue(stack.hasLabel(HierarchicalTreeLayer.COLLAPSED_CHILD));

        // expand second level again
        this.treeLayer.expandOrCollapse(2, 0);

        // disable handle collapsed children, collapse first level, check
        this.treeLayer.setHandleCollapsedChildren(false);

        // collapse first level
        this.treeLayer.expandOrCollapse(0, 0);

        stack = this.treeLayer.getConfigLabelsByPosition(0, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(HierarchicalTreeLayer.LEVEL_HEADER_CELL));

        stack = this.treeLayer.getConfigLabelsByPosition(1, 0);
        assertEquals(4, stack.getLabels().size());
        assertTrue(stack.hasLabel(TreeLayer.TREE_COLUMN_CELL));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + 0));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_COLLAPSED_CONFIG_TYPE));
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 0));

        stack = this.treeLayer.getConfigLabelsByPosition(2, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 1));

        stack = this.treeLayer.getConfigLabelsByPosition(3, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(HierarchicalTreeLayer.LEVEL_HEADER_CELL));

        stack = this.treeLayer.getConfigLabelsByPosition(4, 0);
        assertEquals(4, stack.getLabels().size());
        assertTrue(stack.hasLabel(TreeLayer.TREE_COLUMN_CELL));
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + 0));
        // special situation
        // the parent is collapsed but we show the data of the child level
        // in that case we can not show the expand/collapse of the child,
        // because the parent is collapsed
        assertTrue(stack.hasLabel(DefaultTreeLayerConfiguration.TREE_LEAF_CONFIG_TYPE));
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 2));

        stack = this.treeLayer.getConfigLabelsByPosition(5, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 3));

        stack = this.treeLayer.getConfigLabelsByPosition(6, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(HierarchicalTreeLayer.LEVEL_HEADER_CELL));

        stack = this.treeLayer.getConfigLabelsByPosition(7, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 4));

        stack = this.treeLayer.getConfigLabelsByPosition(8, 0);
        assertEquals(1, stack.getLabels().size());
        assertTrue(stack.hasLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 5));
    }

    @Test
    public void testGetCellByPosition() {
        // test first level header
        ILayerCell cell = this.treeLayer.getCellByPosition(0, 0);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(5, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        cell = this.treeLayer.getCellByPosition(0, 1);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(5, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());
        cell = this.treeLayer.getCellByPosition(0, 2);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(5, cell.getRowSpan());
        assertEquals(0, cell.getOriginColumnPosition());

        // test first level column
        cell = this.treeLayer.getCellByPosition(1, 0);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(5, cell.getRowSpan());
        assertEquals(1, cell.getOriginColumnPosition());
        cell = this.treeLayer.getCellByPosition(1, 1);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(5, cell.getRowSpan());
        assertEquals(1, cell.getOriginColumnPosition());
        cell = this.treeLayer.getCellByPosition(1, 2);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(5, cell.getRowSpan());
        assertEquals(1, cell.getOriginColumnPosition());

        // test second level header
        cell = this.treeLayer.getCellByPosition(3, 0);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(2, cell.getRowSpan());
        assertEquals(3, cell.getOriginColumnPosition());

        cell = this.treeLayer.getCellByPosition(3, 1);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(2, cell.getRowSpan());
        assertEquals(3, cell.getOriginColumnPosition());

        cell = this.treeLayer.getCellByPosition(4, 0);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(2, cell.getRowSpan());
        assertEquals(4, cell.getOriginColumnPosition());

        cell = this.treeLayer.getCellByPosition(4, 1);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(2, cell.getRowSpan());
        assertEquals(4, cell.getOriginColumnPosition());

        // test for third level header
        cell = this.treeLayer.getCellByPosition(7, 0);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowSpan());
        assertEquals(7, cell.getOriginColumnPosition());

        cell = this.treeLayer.getCellByPosition(8, 0);
        assertEquals(0, cell.getOriginRowPosition());
        assertEquals(1, cell.getRowSpan());
        assertEquals(8, cell.getOriginColumnPosition());
    }

    @Test
    public void testGetDataValueByPosition() {
        assertNull(this.treeLayer.getDataValueByPosition(0, 0));
        assertEquals("Mercedes", this.treeLayer.getDataValueByPosition(1, 0));
        assertEquals("C Klasse", this.treeLayer.getDataValueByPosition(2, 0));
        assertNull(this.treeLayer.getDataValueByPosition(3, 0));
        assertEquals("C320", this.treeLayer.getDataValueByPosition(4, 0));
        assertEquals("160", this.treeLayer.getDataValueByPosition(5, 0));
        assertNull(this.treeLayer.getDataValueByPosition(6, 0));
        assertEquals(Classification.POSITIVE, this.treeLayer.getDataValueByPosition(7, 0));
        assertEquals("Blubb", this.treeLayer.getDataValueByPosition(8, 0));

        this.treeLayer.setShowTreeLevelHeader(false);

        assertEquals("Mercedes", this.treeLayer.getDataValueByPosition(0, 0));
        assertEquals("C Klasse", this.treeLayer.getDataValueByPosition(1, 0));
        assertEquals("C320", this.treeLayer.getDataValueByPosition(2, 0));
        assertEquals("160", this.treeLayer.getDataValueByPosition(3, 0));
        assertEquals(Classification.POSITIVE, this.treeLayer.getDataValueByPosition(4, 0));
        assertEquals("Blubb", this.treeLayer.getDataValueByPosition(5, 0));
    }

    @Test
    public void testGetDisplayModeByPosition() {
        // first test that all cells in a row are NORMAL
        for (int i = 0; i < this.treeLayer.getColumnCount(); i++) {
            assertEquals(DisplayMode.NORMAL, this.treeLayer.getDisplayModeByPosition(i, 0));
        }

        // select a cell
        this.treeLayer.doCommand(new SelectCellCommand(
                this.treeLayer,
                5,
                0,
                false,
                false));

        // test that all cells despite the selected one are NORMAL
        boolean selectedFound = false;
        for (int i = 0; i < this.treeLayer.getColumnCount(); i++) {
            if (i != 5) {
                assertEquals(DisplayMode.NORMAL, this.treeLayer.getDisplayModeByPosition(i, 0));
            } else {
                selectedFound = true;
                assertEquals(DisplayMode.SELECT, this.treeLayer.getDisplayModeByPosition(i, 0));
            }
        }

        assertTrue(selectedFound);
    }

    @Test
    public void testGetDisplayModeByPositionWithSelectionLayer() {
        this.treeLayer = new HierarchicalTreeLayer(this.selectionLayer, this.data, CarService.PROPERTY_NAMES_COMPACT, this.selectionLayer);

        // first test that all cells in a row are NORMAL
        for (int i = 0; i < this.treeLayer.getColumnCount(); i++) {
            assertEquals(DisplayMode.NORMAL, this.treeLayer.getDisplayModeByPosition(i, 0));
        }

        // select a cell in first level
        this.treeLayer.doCommand(new SelectCellCommand(
                this.treeLayer,
                1,
                0,
                false,
                false));

        // test that the level header column of the first level and the first
        // content cell are selected
        assertEquals(DisplayMode.SELECT, this.treeLayer.getDisplayModeByPosition(0, 0));
        assertEquals(DisplayMode.SELECT, this.treeLayer.getDisplayModeByPosition(1, 0));

        // then check that all other cells are NORMAL
        for (int i = 2; i < this.treeLayer.getColumnCount(); i++) {
            assertEquals(DisplayMode.NORMAL, this.treeLayer.getDisplayModeByPosition(i, 0));
        }

        // select a cell in second level
        this.treeLayer.doCommand(new SelectCellCommand(
                this.treeLayer,
                5,
                0,
                false,
                false));

        // test that only the second level header is SELECT
        assertEquals(DisplayMode.NORMAL, this.treeLayer.getDisplayModeByPosition(0, 0));
        assertEquals(DisplayMode.SELECT, this.treeLayer.getDisplayModeByPosition(3, 0));
        assertEquals(DisplayMode.NORMAL, this.treeLayer.getDisplayModeByPosition(6, 0));

        // test the state of the other columns
        assertEquals(DisplayMode.NORMAL, this.treeLayer.getDisplayModeByPosition(1, 0));
        assertEquals(DisplayMode.NORMAL, this.treeLayer.getDisplayModeByPosition(2, 0));
        assertEquals(DisplayMode.NORMAL, this.treeLayer.getDisplayModeByPosition(4, 0));
        assertEquals(DisplayMode.SELECT, this.treeLayer.getDisplayModeByPosition(5, 0));
        assertEquals(DisplayMode.NORMAL, this.treeLayer.getDisplayModeByPosition(7, 0));
        assertEquals(DisplayMode.NORMAL, this.treeLayer.getDisplayModeByPosition(8, 0));

        // select a cell in third level
        this.treeLayer.doCommand(new SelectCellCommand(
                this.treeLayer,
                7,
                0,
                false,
                false));

        // test that only the third level header is SELECT
        assertEquals(DisplayMode.NORMAL, this.treeLayer.getDisplayModeByPosition(0, 0));
        assertEquals(DisplayMode.NORMAL, this.treeLayer.getDisplayModeByPosition(3, 0));
        assertEquals(DisplayMode.SELECT, this.treeLayer.getDisplayModeByPosition(6, 0));

        // test the state of the other columns
        assertEquals(DisplayMode.NORMAL, this.treeLayer.getDisplayModeByPosition(1, 0));
        assertEquals(DisplayMode.NORMAL, this.treeLayer.getDisplayModeByPosition(2, 0));
        assertEquals(DisplayMode.NORMAL, this.treeLayer.getDisplayModeByPosition(4, 0));
        assertEquals(DisplayMode.NORMAL, this.treeLayer.getDisplayModeByPosition(5, 0));
        assertEquals(DisplayMode.SELECT, this.treeLayer.getDisplayModeByPosition(7, 0));
        assertEquals(DisplayMode.NORMAL, this.treeLayer.getDisplayModeByPosition(8, 0));

        // clear selection and test that no SELECT is there anymore
        this.selectionLayer.clear();
        for (int i = 0; i < this.treeLayer.getColumnCount(); i++) {
            assertEquals(DisplayMode.NORMAL, this.treeLayer.getDisplayModeByPosition(i, 0));
        }
    }

    @Test
    public void testSelectLevelHeader() {
        // select first level header in first row
        this.treeLayer.doCommand(new SelectCellCommand(
                this.treeLayer,
                0,
                0,
                false,
                false));

        // all cells in the first 5 rows should be selected
        assertTrue(this.selectionLayer.isRowPositionFullySelected(0));
        assertTrue(this.selectionLayer.isRowPositionFullySelected(1));
        assertTrue(this.selectionLayer.isRowPositionFullySelected(2));
        assertTrue(this.selectionLayer.isRowPositionFullySelected(3));
        assertTrue(this.selectionLayer.isRowPositionFullySelected(4));

        // last row is a new element, so not selected
        assertFalse(this.selectionLayer.isRowPositionFullySelected(5));

        // select second level header in first row
        this.treeLayer.doCommand(new SelectCellCommand(
                this.treeLayer,
                3,
                0,
                false,
                false));

        // there is no full row selection at all
        assertFalse(this.selectionLayer.isRowPositionFullySelected(0));
        assertFalse(this.selectionLayer.isRowPositionFullySelected(1));
        assertFalse(this.selectionLayer.isRowPositionFullySelected(2));
        assertFalse(this.selectionLayer.isRowPositionFullySelected(3));
        assertFalse(this.selectionLayer.isRowPositionFullySelected(4));
        assertFalse(this.selectionLayer.isRowPositionFullySelected(5));

        // there is no selection in row 2 and 3
        assertFalse(this.selectionLayer.isRowPositionSelected(2));
        assertFalse(this.selectionLayer.isRowPositionSelected(3));

        // there is no selection in the first level
        assertFalse(this.selectionLayer.isColumnPositionSelected(0));
        assertFalse(this.selectionLayer.isColumnPositionSelected(1));

        // there is a region selection for second and third level in the first
        // two rows
        // start column 2, start row 0, four columns and two rows
        assertTrue(this.selectionLayer.allCellsSelectedInRegion(new Rectangle(2, 0, 4, 2)));

        // select third level header in second row
        this.treeLayer.doCommand(new SelectCellCommand(
                this.treeLayer,
                6,
                1,
                false,
                false));

        // there is no full row selection at all
        assertFalse(this.selectionLayer.isRowPositionFullySelected(0));
        assertFalse(this.selectionLayer.isRowPositionFullySelected(1));
        assertFalse(this.selectionLayer.isRowPositionFullySelected(2));
        assertFalse(this.selectionLayer.isRowPositionFullySelected(3));

        // there is no selection in row 0, 2 and 3
        assertFalse(this.selectionLayer.isRowPositionSelected(0));
        assertFalse(this.selectionLayer.isRowPositionSelected(2));
        assertFalse(this.selectionLayer.isRowPositionSelected(3));

        // there is no selection in the first and second level
        assertFalse(this.selectionLayer.isColumnPositionSelected(0));
        assertFalse(this.selectionLayer.isColumnPositionSelected(1));
        assertFalse(this.selectionLayer.isColumnPositionSelected(2));
        assertFalse(this.selectionLayer.isColumnPositionSelected(3));

        // there is a region selection for third level in the second row
        // start column 4, start row 1, two columns and one row
        assertTrue(this.selectionLayer.allCellsSelectedInRegion(new Rectangle(4, 1, 2, 1)));
    }

    @Test
    public void testSelectSpannedCellIsOrigin() {
        // add the PreserveSelectionModel to avoid that the selection is cleared
        // on expand collapse
        this.selectionLayer.setSelectionModel(
                new PreserveSelectionModel<>(this.selectionLayer,
                        this.bodyDataProvider,
                        new IRowIdAccessor<HierarchicalWrapper>() {

                            @Override
                            public Serializable getRowId(HierarchicalWrapper rowObject) {
                                return rowObject.hashCode();
                            }
                        }));

        assertTrue(this.selectionLayer.getSelectedCells().isEmpty());

        // select a cell in first level
        this.treeLayer.doCommand(new SelectCellCommand(
                this.treeLayer,
                1,
                3,
                false,
                false));

        assertEquals(1, this.selectionLayer.getSelectedCells().size());

        ILayerCell cell = this.selectionLayer.getSelectedCells().iterator().next();
        assertEquals(0, cell.getColumnPosition());
        assertEquals(0, cell.getRowPosition());
    }

    @Test
    public void testgetLevelByColumnIndex() {
        assertEquals(-1, this.treeLayer.getLevelByColumnIndex(-1));
        assertEquals(0, this.treeLayer.getLevelByColumnIndex(0));
        assertEquals(0, this.treeLayer.getLevelByColumnIndex(1));
        assertEquals(1, this.treeLayer.getLevelByColumnIndex(2));
        assertEquals(1, this.treeLayer.getLevelByColumnIndex(3));
        assertEquals(2, this.treeLayer.getLevelByColumnIndex(4));
        assertEquals(2, this.treeLayer.getLevelByColumnIndex(5));
        assertEquals(-1, this.treeLayer.getLevelByColumnIndex(6));
    }

    @Test
    public void testGetColumnIndexesForLevel() {
        List<Integer> columnIndexesForLevel = this.treeLayer.getColumnIndexesForLevel(0);
        assertEquals(2, columnIndexesForLevel.size());
        assertTrue(columnIndexesForLevel.contains(Integer.valueOf(0)));
        assertTrue(columnIndexesForLevel.contains(Integer.valueOf(1)));

        columnIndexesForLevel = this.treeLayer.getColumnIndexesForLevel(1);
        assertEquals(2, columnIndexesForLevel.size());
        assertTrue(columnIndexesForLevel.contains(Integer.valueOf(2)));
        assertTrue(columnIndexesForLevel.contains(Integer.valueOf(3)));

        columnIndexesForLevel = this.treeLayer.getColumnIndexesForLevel(2);
        assertEquals(2, columnIndexesForLevel.size());
        assertTrue(columnIndexesForLevel.contains(Integer.valueOf(4)));
        assertTrue(columnIndexesForLevel.contains(Integer.valueOf(5)));
    }

    @Test
    public void testColumnCount() {
        // we expect 6 content columns and 3 level headers for 3 levels
        assertEquals(9, this.treeLayer.getColumnCount());

        this.treeLayer.setShowTreeLevelHeader(false);

        // now only the content columns should be shown
        assertEquals(6, this.treeLayer.getColumnCount());
    }

    @Test
    public void testGetColumnIndexByPosition() {
        assertEquals(-13, this.treeLayer.getColumnIndexByPosition(0));
        assertEquals(0, this.treeLayer.getColumnIndexByPosition(1));
        assertEquals(1, this.treeLayer.getColumnIndexByPosition(2));
        assertEquals(-26, this.treeLayer.getColumnIndexByPosition(3));
        assertEquals(2, this.treeLayer.getColumnIndexByPosition(4));
        assertEquals(3, this.treeLayer.getColumnIndexByPosition(5));
        assertEquals(-39, this.treeLayer.getColumnIndexByPosition(6));
        assertEquals(4, this.treeLayer.getColumnIndexByPosition(7));
        assertEquals(5, this.treeLayer.getColumnIndexByPosition(8));

        this.treeLayer.setShowTreeLevelHeader(false);

        assertEquals(-1, this.treeLayer.getColumnIndexByPosition(-1));
        assertEquals(0, this.treeLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.treeLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.treeLayer.getColumnIndexByPosition(2));
        assertEquals(3, this.treeLayer.getColumnIndexByPosition(3));
        assertEquals(4, this.treeLayer.getColumnIndexByPosition(4));
        assertEquals(5, this.treeLayer.getColumnIndexByPosition(5));
        assertEquals(-1, this.treeLayer.getColumnIndexByPosition(6));
    }

    @Test
    public void testGetColumnPositionByIndex() {
        assertEquals(0, this.treeLayer.getColumnPositionByIndex(-13));
        assertEquals(1, this.treeLayer.getColumnPositionByIndex(0));
        assertEquals(2, this.treeLayer.getColumnPositionByIndex(1));
        assertEquals(3, this.treeLayer.getColumnPositionByIndex(-26));
        assertEquals(4, this.treeLayer.getColumnPositionByIndex(2));
        assertEquals(5, this.treeLayer.getColumnPositionByIndex(3));
        assertEquals(6, this.treeLayer.getColumnPositionByIndex(-39));
        assertEquals(7, this.treeLayer.getColumnPositionByIndex(4));
        assertEquals(8, this.treeLayer.getColumnPositionByIndex(5));

        this.treeLayer.setShowTreeLevelHeader(false);

        assertEquals(0, this.treeLayer.getColumnPositionByIndex(0));
        assertEquals(1, this.treeLayer.getColumnPositionByIndex(1));
        assertEquals(2, this.treeLayer.getColumnPositionByIndex(2));
        assertEquals(3, this.treeLayer.getColumnPositionByIndex(3));
        assertEquals(4, this.treeLayer.getColumnPositionByIndex(4));
        assertEquals(5, this.treeLayer.getColumnPositionByIndex(5));
    }

    @Test
    public void testUnderlyingToLocalColumnPosition() {
        assertEquals(1, this.treeLayer.underlyingToLocalColumnPosition(this.selectionLayer, 0));
        assertEquals(2, this.treeLayer.underlyingToLocalColumnPosition(this.selectionLayer, 1));
        assertEquals(4, this.treeLayer.underlyingToLocalColumnPosition(this.selectionLayer, 2));
        assertEquals(5, this.treeLayer.underlyingToLocalColumnPosition(this.selectionLayer, 3));
        assertEquals(7, this.treeLayer.underlyingToLocalColumnPosition(this.selectionLayer, 4));
        assertEquals(8, this.treeLayer.underlyingToLocalColumnPosition(this.selectionLayer, 5));

        this.treeLayer.setShowTreeLevelHeader(false);

        assertEquals(0, this.treeLayer.underlyingToLocalColumnPosition(this.selectionLayer, 0));
        assertEquals(1, this.treeLayer.underlyingToLocalColumnPosition(this.selectionLayer, 1));
        assertEquals(2, this.treeLayer.underlyingToLocalColumnPosition(this.selectionLayer, 2));
        assertEquals(3, this.treeLayer.underlyingToLocalColumnPosition(this.selectionLayer, 3));
        assertEquals(4, this.treeLayer.underlyingToLocalColumnPosition(this.selectionLayer, 4));
        assertEquals(5, this.treeLayer.underlyingToLocalColumnPosition(this.selectionLayer, 5));
    }

    @Test
    public void testUnderlyingToLocalColumnPositions() {
        ArrayList<Range> positionRanges = new ArrayList<>();
        positionRanges.add(new Range(0, 2));
        positionRanges.add(new Range(2, 4));
        positionRanges.add(new Range(4, 6));

        Collection<Range> underlying = this.treeLayer.underlyingToLocalColumnPositions(this.selectionLayer, positionRanges);

        ArrayList<Range> list = new ArrayList<>(underlying);
        assertEquals(new Range(1, 3), list.get(0));
        assertEquals(new Range(4, 6), list.get(1));
        assertEquals(new Range(7, 9), list.get(2));

        this.treeLayer.setShowTreeLevelHeader(false);

        underlying = this.treeLayer.underlyingToLocalColumnPositions(this.selectionLayer, positionRanges);

        list = new ArrayList<>(underlying);
        assertEquals(new Range(0, 2), list.get(0));
        assertEquals(new Range(2, 4), list.get(1));
        assertEquals(new Range(4, 6), list.get(2));
    }

    @Test
    public void testReorderInLevel() {
        this.treeLayer.doCommand(new ColumnReorderCommand(this.treeLayer, 1, 3));

        assertEquals(1, this.treeLayer.getColumnIndexByPosition(1));
        assertEquals(0, this.treeLayer.getColumnIndexByPosition(2));
    }

    @Test
    public void testReorderInLevelWithoutLevelHeader() {
        this.treeLayer.setShowTreeLevelHeader(false);

        this.treeLayer.doCommand(new ColumnReorderCommand(this.treeLayer, 0, 2));

        assertEquals(1, this.treeLayer.getColumnIndexByPosition(0));
        assertEquals(0, this.treeLayer.getColumnIndexByPosition(1));
    }

    @Test
    public void testNoReorderBetweenLevels() {
        this.treeLayer.doCommand(new ColumnReorderCommand(this.treeLayer, 2, 5));

        // nothing should have changed because that reorder is not allowed
        assertEquals(0, this.treeLayer.getColumnIndexByPosition(1));
        assertEquals(1, this.treeLayer.getColumnIndexByPosition(2));
    }

    @Test
    public void testNoReorderBetweenLevelsWithoutLevelHeader() {
        this.treeLayer.setShowTreeLevelHeader(false);

        this.treeLayer.doCommand(new ColumnReorderCommand(this.treeLayer, 1, 3));

        // nothing should have changed because that reorder is not allowed
        assertEquals(0, this.treeLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.treeLayer.getColumnIndexByPosition(1));
    }

    @Test
    public void testLevelHeaderCanNotBeReordered() {
        this.treeLayer.doCommand(new ColumnReorderCommand(this.treeLayer, 0, 2));

        assertEquals(-13, this.treeLayer.getColumnIndexByPosition(0));
        assertEquals(0, this.treeLayer.getColumnIndexByPosition(1));
        assertEquals(1, this.treeLayer.getColumnIndexByPosition(2));
        assertEquals(-26, this.treeLayer.getColumnIndexByPosition(3));
    }

    @Test
    public void testNoMultiReorderBetweenLevels() {
        List<Integer> fromColumnPositions = Arrays.asList(1, 2);
        this.treeLayer.doCommand(new MultiColumnReorderCommand(this.treeLayer, fromColumnPositions, 5));

        // nothing should have changed because that reorder is not allowed
        assertEquals(-13, this.treeLayer.getColumnIndexByPosition(0));
        assertEquals(0, this.treeLayer.getColumnIndexByPosition(1));
        assertEquals(1, this.treeLayer.getColumnIndexByPosition(2));
        assertEquals(-26, this.treeLayer.getColumnIndexByPosition(3));
    }

    @Test
    public void testGetWidth() {
        assertEquals(660, this.treeLayer.getWidth());

        this.treeLayer.setShowTreeLevelHeader(false);

        assertEquals(600, this.treeLayer.getWidth());
    }

    @Test
    public void testGetColumnWidthByPosition() {
        assertEquals(20, this.treeLayer.getColumnWidthByPosition(0));
        assertEquals(100, this.treeLayer.getColumnWidthByPosition(1));
        assertEquals(100, this.treeLayer.getColumnWidthByPosition(2));
        assertEquals(20, this.treeLayer.getColumnWidthByPosition(3));
        assertEquals(100, this.treeLayer.getColumnWidthByPosition(4));
        assertEquals(100, this.treeLayer.getColumnWidthByPosition(5));
        assertEquals(20, this.treeLayer.getColumnWidthByPosition(6));
        assertEquals(100, this.treeLayer.getColumnWidthByPosition(7));
        assertEquals(100, this.treeLayer.getColumnWidthByPosition(8));

        this.treeLayer.setShowTreeLevelHeader(false);

        assertEquals(100, this.treeLayer.getColumnWidthByPosition(0));
        assertEquals(100, this.treeLayer.getColumnWidthByPosition(1));
        assertEquals(100, this.treeLayer.getColumnWidthByPosition(2));
        assertEquals(100, this.treeLayer.getColumnWidthByPosition(3));
        assertEquals(100, this.treeLayer.getColumnWidthByPosition(4));
        assertEquals(100, this.treeLayer.getColumnWidthByPosition(5));
    }

    @Test
    public void testGetStartXOfColumnPosition() {
        assertEquals(0, this.treeLayer.getStartXOfColumnPosition(0));
        assertEquals(20, this.treeLayer.getStartXOfColumnPosition(1));
        assertEquals(120, this.treeLayer.getStartXOfColumnPosition(2));
        assertEquals(220, this.treeLayer.getStartXOfColumnPosition(3));
        assertEquals(240, this.treeLayer.getStartXOfColumnPosition(4));
        assertEquals(340, this.treeLayer.getStartXOfColumnPosition(5));
        assertEquals(440, this.treeLayer.getStartXOfColumnPosition(6));
        assertEquals(460, this.treeLayer.getStartXOfColumnPosition(7));
        assertEquals(560, this.treeLayer.getStartXOfColumnPosition(8));

        this.treeLayer.setShowTreeLevelHeader(false);

        assertEquals(0, this.treeLayer.getStartXOfColumnPosition(0));
        assertEquals(100, this.treeLayer.getStartXOfColumnPosition(1));
        assertEquals(200, this.treeLayer.getStartXOfColumnPosition(2));
        assertEquals(300, this.treeLayer.getStartXOfColumnPosition(3));
        assertEquals(400, this.treeLayer.getStartXOfColumnPosition(4));
        assertEquals(500, this.treeLayer.getStartXOfColumnPosition(5));
    }

    @Test
    public void testGetColumnPositionByX() {
        assertEquals(0, this.treeLayer.getColumnPositionByX(5));
        assertEquals(1, this.treeLayer.getColumnPositionByX(21));
        assertEquals(2, this.treeLayer.getColumnPositionByX(180));
        assertEquals(3, this.treeLayer.getColumnPositionByX(220));
        assertEquals(4, this.treeLayer.getColumnPositionByX(339));

        this.treeLayer.setShowTreeLevelHeader(false);

        assertEquals(0, this.treeLayer.getColumnPositionByX(5));
        assertEquals(0, this.treeLayer.getColumnPositionByX(21));
        assertEquals(1, this.treeLayer.getColumnPositionByX(180));
        assertEquals(2, this.treeLayer.getColumnPositionByX(220));
        assertEquals(3, this.treeLayer.getColumnPositionByX(339));
        assertEquals(4, this.treeLayer.getColumnPositionByX(499));
    }

    private void scaleTo150() {
        IDpiConverter dpiConverter = new AbstractDpiConverter() {

            @Override
            protected void readDpiFromDisplay() {
                this.dpi = 144;
            }

        };

        this.treeLayer.doCommand(new ConfigureScalingCommand(dpiConverter, dpiConverter));
    }

    @Test
    public void testGetWidthScaled() {
        scaleTo150();

        assertEquals(990, this.treeLayer.getWidth());

        this.treeLayer.setShowTreeLevelHeader(false);

        assertEquals(900, this.treeLayer.getWidth());
    }

    @Test
    public void testGetColumnWidthByPositionScaled() {
        scaleTo150();

        assertEquals(30, this.treeLayer.getColumnWidthByPosition(0));
        assertEquals(150, this.treeLayer.getColumnWidthByPosition(1));
        assertEquals(150, this.treeLayer.getColumnWidthByPosition(2));
        assertEquals(30, this.treeLayer.getColumnWidthByPosition(3));
        assertEquals(150, this.treeLayer.getColumnWidthByPosition(4));
        assertEquals(150, this.treeLayer.getColumnWidthByPosition(5));
        assertEquals(30, this.treeLayer.getColumnWidthByPosition(6));
        assertEquals(150, this.treeLayer.getColumnWidthByPosition(7));
        assertEquals(150, this.treeLayer.getColumnWidthByPosition(8));

        this.treeLayer.setShowTreeLevelHeader(false);

        assertEquals(150, this.treeLayer.getColumnWidthByPosition(0));
        assertEquals(150, this.treeLayer.getColumnWidthByPosition(1));
        assertEquals(150, this.treeLayer.getColumnWidthByPosition(2));
        assertEquals(150, this.treeLayer.getColumnWidthByPosition(3));
        assertEquals(150, this.treeLayer.getColumnWidthByPosition(4));
        assertEquals(150, this.treeLayer.getColumnWidthByPosition(5));
    }

    @Test
    public void testGetStartXOfColumnPositionScaled() {
        scaleTo150();

        assertEquals(0, this.treeLayer.getStartXOfColumnPosition(0));
        assertEquals(30, this.treeLayer.getStartXOfColumnPosition(1));
        assertEquals(180, this.treeLayer.getStartXOfColumnPosition(2));
        assertEquals(330, this.treeLayer.getStartXOfColumnPosition(3));
        assertEquals(360, this.treeLayer.getStartXOfColumnPosition(4));
        assertEquals(510, this.treeLayer.getStartXOfColumnPosition(5));
        assertEquals(660, this.treeLayer.getStartXOfColumnPosition(6));
        assertEquals(690, this.treeLayer.getStartXOfColumnPosition(7));
        assertEquals(840, this.treeLayer.getStartXOfColumnPosition(8));

        this.treeLayer.setShowTreeLevelHeader(false);

        assertEquals(0, this.treeLayer.getStartXOfColumnPosition(0));
        assertEquals(150, this.treeLayer.getStartXOfColumnPosition(1));
        assertEquals(300, this.treeLayer.getStartXOfColumnPosition(2));
        assertEquals(450, this.treeLayer.getStartXOfColumnPosition(3));
        assertEquals(600, this.treeLayer.getStartXOfColumnPosition(4));
        assertEquals(750, this.treeLayer.getStartXOfColumnPosition(5));
    }

    @Test
    public void testGetColumnPositionByXScaled() {
        scaleTo150();

        assertEquals(0, this.treeLayer.getColumnPositionByX(7));
        assertEquals(1, this.treeLayer.getColumnPositionByX(32));
        assertEquals(2, this.treeLayer.getColumnPositionByX(270));
        assertEquals(3, this.treeLayer.getColumnPositionByX(330));
        assertEquals(4, this.treeLayer.getColumnPositionByX(508));

        this.treeLayer.setShowTreeLevelHeader(false);

        assertEquals(0, this.treeLayer.getColumnPositionByX(7));
        assertEquals(0, this.treeLayer.getColumnPositionByX(32));
        assertEquals(1, this.treeLayer.getColumnPositionByX(270));
        assertEquals(2, this.treeLayer.getColumnPositionByX(330));
        assertEquals(3, this.treeLayer.getColumnPositionByX(508));
        assertEquals(4, this.treeLayer.getColumnPositionByX(749));
    }

    @Test
    public void testCollapseExpandSingleItemFirstLevel() {
        assertEquals(11, this.treeLayer.getRowCount());
        assertTrue(this.treeLayer.collapsedNodes.isEmpty());
        assertTrue(this.treeLayer.getHiddenRowIndexes().isEmpty());

        // collapse first node in first level
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(0, 0));

        assertEquals(7, this.treeLayer.getRowCount());
        assertEquals(1, this.treeLayer.collapsedNodes.size());
        assertEquals(4, this.treeLayer.getHiddenRowIndexes().size());

        assertEquals(1, this.layerListener.getEventsCount());
        assertTrue(this.layerListener.containsInstanceOf(HideRowPositionsEvent.class));

        HideRowPositionsEvent hideEvent = (HideRowPositionsEvent) this.layerListener.getReceivedEvent(HideRowPositionsEvent.class);
        Collection<Range> rowPositionRanges = hideEvent.getRowPositionRanges();
        assertEquals(1, rowPositionRanges.size());
        assertEquals(new Range(1, 5), rowPositionRanges.iterator().next());

        // expand first node in first level again
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(0, 0));

        assertEquals(11, this.treeLayer.getRowCount());
        assertTrue(this.treeLayer.collapsedNodes.isEmpty());
        assertTrue(this.treeLayer.getHiddenRowIndexes().isEmpty());

        assertEquals(2, this.layerListener.getEventsCount());
        assertTrue(this.layerListener.containsInstanceOf(ShowRowPositionsEvent.class));

        ShowRowPositionsEvent showEvent = (ShowRowPositionsEvent) this.layerListener.getReceivedEvent(ShowRowPositionsEvent.class);
        rowPositionRanges = showEvent.getRowPositionRanges();
        assertEquals(1, rowPositionRanges.size());
        assertEquals(new Range(1, 5), rowPositionRanges.iterator().next());
    }

    // collapse second - collapse first - expand first - second still collapsed
    @Test
    public void testCollapseMultipleExpandSingle() {
        assertEquals(11, this.treeLayer.getRowCount());
        assertTrue(this.treeLayer.collapsedNodes.isEmpty());
        assertTrue(this.treeLayer.getHiddenRowIndexes().isEmpty());

        // collapse first node in second level
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(0, 2));

        assertEquals(10, this.treeLayer.getRowCount());
        assertEquals(1, this.treeLayer.collapsedNodes.size());
        HierarchicalTreeNode node = this.treeLayer.collapsedNodes.iterator().next();
        assertEquals(0, node.rowIndex);
        assertEquals(2, node.columnIndex);
        assertNotNull(node.rowObject);
        assertEquals(1, this.treeLayer.getHiddenRowIndexes().iterator().next().intValue());

        assertEquals(1, this.layerListener.getEventsCount());
        assertTrue(this.layerListener.containsInstanceOf(HideRowPositionsEvent.class));

        HideRowPositionsEvent hideEvent = (HideRowPositionsEvent) this.layerListener.getReceivedEvent(HideRowPositionsEvent.class);
        Collection<Range> rowPositionRanges = hideEvent.getRowPositionRanges();
        assertEquals(1, rowPositionRanges.size());
        assertEquals(new Range(1, 2), rowPositionRanges.iterator().next());
        this.layerListener.clearReceivedEvents();

        // collapse first node in first level
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(0, 0));

        assertEquals(7, this.treeLayer.getRowCount());
        assertEquals(2, this.treeLayer.collapsedNodes.size());
        assertTrue(this.treeLayer.collapsedNodes.contains(new HierarchicalTreeNode(2, 0, null)));
        assertTrue(this.treeLayer.collapsedNodes.contains(new HierarchicalTreeNode(0, 0, null)));
        assertEquals(4, this.treeLayer.getHiddenRowIndexes().size());

        assertEquals(1, this.layerListener.getEventsCount());
        assertTrue(this.layerListener.containsInstanceOf(HideRowPositionsEvent.class));

        hideEvent = (HideRowPositionsEvent) this.layerListener.getReceivedEvent(HideRowPositionsEvent.class);
        rowPositionRanges = hideEvent.getRowPositionRanges();
        assertEquals(1, rowPositionRanges.size());
        assertEquals(new Range(1, 5), rowPositionRanges.iterator().next());

        // expand first node in first level again
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(0, 0));

        assertEquals(10, this.treeLayer.getRowCount());
        assertEquals(1, this.treeLayer.collapsedNodes.size());
        node = this.treeLayer.collapsedNodes.iterator().next();
        assertEquals(0, node.rowIndex);
        assertEquals(2, node.columnIndex);
        assertNotNull(node.rowObject);
        assertEquals(1, this.treeLayer.getHiddenRowIndexes().iterator().next().intValue());

        assertEquals(2, this.layerListener.getEventsCount());
        assertTrue(this.layerListener.containsInstanceOf(ShowRowPositionsEvent.class));

        ShowRowPositionsEvent showEvent = (ShowRowPositionsEvent) this.layerListener.getReceivedEvent(ShowRowPositionsEvent.class);
        rowPositionRanges = showEvent.getRowPositionRanges();
        assertEquals(1, rowPositionRanges.size());
        assertEquals(new Range(2, 5), rowPositionRanges.iterator().next());
    }

    // collapse second - collapse first - expand first to level 1 - second also
    // expanded
    @Test
    public void testCollapseMultipleExpandSingleToLevel() {
        // collapse first node in second level
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(0, 2));
        // collapse first node in first level
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(0, 0));

        assertEquals(7, this.treeLayer.getRowCount());
        assertEquals(2, this.treeLayer.collapsedNodes.size());
        assertTrue(this.treeLayer.collapsedNodes.contains(new HierarchicalTreeNode(2, 0, null)));
        assertTrue(this.treeLayer.collapsedNodes.contains(new HierarchicalTreeNode(0, 0, null)));
        assertEquals(4, this.treeLayer.getHiddenRowIndexes().size());

        // expand first node in first level to level 1
        this.treeLayer.doCommand(new TreeExpandToLevelCommand(0, 1));

        assertEquals(11, this.treeLayer.getRowCount());
        assertTrue(this.treeLayer.collapsedNodes.isEmpty());
        assertTrue(this.treeLayer.getHiddenRowIndexes().isEmpty());

        assertEquals(3, this.layerListener.getEventsCount());
        assertTrue(this.layerListener.containsInstanceOf(ShowRowPositionsEvent.class));

        ShowRowPositionsEvent showEvent = (ShowRowPositionsEvent) this.layerListener.getReceivedEvent(ShowRowPositionsEvent.class);
        Collection<Range> rowPositionRanges = showEvent.getRowPositionRanges();
        assertEquals(1, rowPositionRanges.size());
        assertEquals(new Range(1, 5), rowPositionRanges.iterator().next());
    }

    // collapseAll - expand single item with sub items - sub items are all
    // collapsed
    @Test
    public void testCollapseAll() {
        this.treeLayer.doCommand(new TreeCollapseAllCommand());

        assertEquals(3, this.treeLayer.getRowCount());
        assertEquals(6, this.treeLayer.collapsedNodes.size());
        assertEquals(8, this.treeLayer.getHiddenRowIndexes().size());

        HideRowPositionsEvent hideEvent = (HideRowPositionsEvent) this.layerListener.getReceivedEvent(HideRowPositionsEvent.class);
        Collection<Range> rowPositionRanges = hideEvent.getRowPositionRanges();
        assertEquals(2, rowPositionRanges.size());
        assertTrue(rowPositionRanges.contains(new Range(1, 5)));
        assertTrue(rowPositionRanges.contains(new Range(7, 11)));

        this.treeLayer.doCommand(new TreeExpandCollapseCommand(0, 0));

        assertEquals(5, this.treeLayer.getRowCount());
        assertEquals(5, this.treeLayer.collapsedNodes.size());
        assertEquals(6, this.treeLayer.getHiddenRowIndexes().size());

        ShowRowPositionsEvent showEvent = (ShowRowPositionsEvent) this.layerListener.getReceivedEvent(ShowRowPositionsEvent.class);
        rowPositionRanges = showEvent.getRowPositionRanges();
        assertEquals(1, rowPositionRanges.size());
        assertEquals(new Range(2, 4), rowPositionRanges.iterator().next());
    }

    @Test
    public void testCollapseAllExpandAll() {
        this.treeLayer.doCommand(new TreeCollapseAllCommand());

        assertEquals(3, this.treeLayer.getRowCount());
        assertEquals(6, this.treeLayer.collapsedNodes.size());
        assertEquals(8, this.treeLayer.getHiddenRowIndexes().size());

        this.treeLayer.doCommand(new TreeExpandAllCommand());

        assertEquals(11, this.treeLayer.getRowCount());
        assertEquals(0, this.treeLayer.collapsedNodes.size());
        assertEquals(0, this.treeLayer.getHiddenRowIndexes().size());
    }

    // collapseAll - expand to first level - expand one element in second level
    // - ensure others in second level are still collapsed
    @Test
    public void testExpandToLevel() {
        // first collapse all
        this.treeLayer.doCommand(new TreeCollapseAllCommand());

        // expand first level
        this.treeLayer.doCommand(new TreeExpandToLevelCommand(0));

        assertEquals(7, this.treeLayer.getRowCount());
        assertEquals(4, this.treeLayer.collapsedNodes.size());
        assertEquals(4, this.treeLayer.getHiddenRowIndexes().size());

        // expand one single node on second level to ensure the collapsed states
        // are correct
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(0, 2));

        assertEquals(8, this.treeLayer.getRowCount());
        assertEquals(3, this.treeLayer.collapsedNodes.size());
        assertEquals(3, this.treeLayer.getHiddenRowIndexes().size());
    }

    // collapse second level - collapseAll - all nodes collapsed
    @Test
    public void testConsecutiveCollapse() {
        // collapse first node in second level
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(0, 2));

        assertEquals(10, this.treeLayer.getRowCount());
        assertEquals(1, this.treeLayer.collapsedNodes.size());
        HierarchicalTreeNode node = this.treeLayer.collapsedNodes.iterator().next();
        assertEquals(2, node.columnIndex);
        assertEquals(0, node.rowIndex);
        assertNotNull(node.rowObject);
        assertEquals(1, this.treeLayer.getHiddenRowIndexes().iterator().next().intValue());

        this.treeLayer.doCommand(new TreeCollapseAllCommand());

        assertEquals(3, this.treeLayer.getRowCount());
        assertEquals(6, this.treeLayer.collapsedNodes.size());
        assertEquals(8, this.treeLayer.getHiddenRowIndexes().size());
    }

    @Test
    public void testExpandOnSearch() {
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(9, 2));

        assertEquals(10, this.treeLayer.getRowCount());
        assertEquals(1, this.treeLayer.collapsedNodes.size());
        HierarchicalTreeNode node = this.treeLayer.collapsedNodes.iterator().next();
        assertEquals(2, node.columnIndex);
        assertEquals(9, node.rowIndex);
        assertNotNull(node.rowObject);
        assertEquals(10, this.treeLayer.getHiddenRowIndexes().iterator().next().intValue());

        // search for the collapsed row
        ConfigRegistry configRegistry = new ConfigRegistry();
        configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultDisplayConverter());
        GridSearchStrategy gridSearchStrategy = new GridSearchStrategy(configRegistry, false, true);
        SearchCommand searchCommand = new SearchCommand(
                "sing",
                this.selectionLayer,
                gridSearchStrategy,
                ISearchDirection.SEARCH_FORWARD,
                false,
                false,
                false,
                false,
                false,
                false,
                new CellValueAsStringComparator<>());

        this.treeLayer.doCommand(searchCommand);

        assertEquals(11, this.treeLayer.getRowCount());
        assertEquals(0, this.treeLayer.collapsedNodes.size());
        assertEquals(0, this.treeLayer.getHiddenRowIndexes().size());
    }

    @Test
    public void testExpandAllLevelsOnSearch() {
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(9, 2));
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(6, 0));

        assertEquals(7, this.treeLayer.getRowCount());
        assertEquals(2, this.treeLayer.collapsedNodes.size());
        assertEquals(4, this.treeLayer.getHiddenRowIndexes().size());

        // search for the collapsed row
        ConfigRegistry configRegistry = new ConfigRegistry();
        configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultDisplayConverter());
        GridSearchStrategy gridSearchStrategy = new GridSearchStrategy(configRegistry, false, true);
        SearchCommand searchCommand = new SearchCommand(
                "sing",
                this.selectionLayer,
                gridSearchStrategy,
                ISearchDirection.SEARCH_FORWARD,
                false,
                false,
                false,
                false,
                false,
                false,
                new CellValueAsStringComparator<>());

        this.treeLayer.doCommand(searchCommand);

        assertEquals(11, this.treeLayer.getRowCount());
        assertEquals(0, this.treeLayer.collapsedNodes.size());
        assertEquals(0, this.treeLayer.getHiddenRowIndexes().size());
    }

    @Test
    public void testUnhideOnSearch() {
        this.treeLayer.setExpandOnSearch(false);
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(9, 2));

        assertEquals(10, this.treeLayer.getRowCount());
        assertEquals(1, this.treeLayer.collapsedNodes.size());
        HierarchicalTreeNode node = this.treeLayer.collapsedNodes.iterator().next();
        assertEquals(2, node.columnIndex);
        assertEquals(9, node.rowIndex);
        assertNotNull(node.rowObject);
        assertEquals(10, this.treeLayer.getHiddenRowIndexes().iterator().next().intValue());

        // search for the collapsed row
        ConfigRegistry configRegistry = new ConfigRegistry();
        configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultDisplayConverter());
        GridSearchStrategy gridSearchStrategy = new GridSearchStrategy(configRegistry, false, true);
        SearchCommand searchCommand = new SearchCommand(
                "sing",
                this.selectionLayer,
                gridSearchStrategy,
                ISearchDirection.SEARCH_FORWARD,
                false,
                false,
                false,
                false,
                false,
                false,
                new CellValueAsStringComparator<>());

        this.treeLayer.doCommand(searchCommand);

        assertEquals(11, this.treeLayer.getRowCount());
        assertEquals(1, this.treeLayer.collapsedNodes.size());
        node = this.treeLayer.collapsedNodes.iterator().next();
        assertEquals(2, node.columnIndex);
        assertEquals(9, node.rowIndex);
        assertNotNull(node.rowObject);
        assertEquals(0, this.treeLayer.getHiddenRowIndexes().size());
    }

    @Test
    public void testExpandOnSearchWithoutLevelHeader() {
        this.treeLayer.setShowTreeLevelHeader(false);
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(9, 2));

        assertEquals(10, this.treeLayer.getRowCount());
        assertEquals(1, this.treeLayer.collapsedNodes.size());
        HierarchicalTreeNode node = this.treeLayer.collapsedNodes.iterator().next();
        assertEquals(2, node.columnIndex);
        assertEquals(9, node.rowIndex);
        assertNotNull(node.rowObject);
        assertEquals(10, this.treeLayer.getHiddenRowIndexes().iterator().next().intValue());

        // search for the collapsed row
        ConfigRegistry configRegistry = new ConfigRegistry();
        configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultDisplayConverter());
        GridSearchStrategy gridSearchStrategy = new GridSearchStrategy(configRegistry, false, true);
        SearchCommand searchCommand = new SearchCommand(
                "sing",
                this.selectionLayer,
                gridSearchStrategy,
                ISearchDirection.SEARCH_FORWARD,
                false,
                false,
                false,
                false,
                false,
                false,
                new CellValueAsStringComparator<>());

        this.treeLayer.doCommand(searchCommand);

        assertEquals(11, this.treeLayer.getRowCount());
        assertEquals(0, this.treeLayer.collapsedNodes.size());
        assertEquals(0, this.treeLayer.getHiddenRowIndexes().size());
    }

    @Test
    public void testExpandAllLevelsOnSearchWithoutLevelHeader() {
        this.treeLayer.setShowTreeLevelHeader(false);
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(9, 2));
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(6, 0));

        assertEquals(7, this.treeLayer.getRowCount());
        assertEquals(2, this.treeLayer.collapsedNodes.size());
        assertEquals(4, this.treeLayer.getHiddenRowIndexes().size());

        // search for the collapsed row
        ConfigRegistry configRegistry = new ConfigRegistry();
        configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultDisplayConverter());
        GridSearchStrategy gridSearchStrategy = new GridSearchStrategy(configRegistry, false, true);
        SearchCommand searchCommand = new SearchCommand(
                "sing",
                this.selectionLayer,
                gridSearchStrategy,
                ISearchDirection.SEARCH_FORWARD,
                false,
                false,
                false,
                false,
                false,
                false,
                new CellValueAsStringComparator<>());

        this.treeLayer.doCommand(searchCommand);

        assertEquals(11, this.treeLayer.getRowCount());
        assertEquals(0, this.treeLayer.collapsedNodes.size());
        assertEquals(0, this.treeLayer.getHiddenRowIndexes().size());
    }

    @Test
    public void testUnhideOnSearchWithoutLevelHeader() {
        this.treeLayer.setShowTreeLevelHeader(false);
        this.treeLayer.setExpandOnSearch(false);
        this.treeLayer.doCommand(new TreeExpandCollapseCommand(9, 2));

        assertEquals(10, this.treeLayer.getRowCount());
        assertEquals(1, this.treeLayer.collapsedNodes.size());
        HierarchicalTreeNode node = this.treeLayer.collapsedNodes.iterator().next();
        assertEquals(2, node.columnIndex);
        assertEquals(9, node.rowIndex);
        assertNotNull(node.rowObject);
        assertEquals(10, this.treeLayer.getHiddenRowIndexes().iterator().next().intValue());

        // search for the collapsed row
        ConfigRegistry configRegistry = new ConfigRegistry();
        configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultDisplayConverter());
        GridSearchStrategy gridSearchStrategy = new GridSearchStrategy(configRegistry, false, true);
        SearchCommand searchCommand = new SearchCommand(
                "sing",
                this.selectionLayer,
                gridSearchStrategy,
                ISearchDirection.SEARCH_FORWARD,
                false,
                false,
                false,
                false,
                false,
                false,
                new CellValueAsStringComparator<>());

        this.treeLayer.doCommand(searchCommand);

        assertEquals(11, this.treeLayer.getRowCount());
        assertEquals(1, this.treeLayer.collapsedNodes.size());
        node = this.treeLayer.collapsedNodes.iterator().next();
        assertEquals(2, node.columnIndex);
        assertEquals(9, node.rowIndex);
        assertNotNull(node.rowObject);
        assertEquals(0, this.treeLayer.getHiddenRowIndexes().size());
    }

    @Test
    public void testGetProvidedLabels() {
        Collection<String> providedLabels = this.treeLayer.getProvidedLabels();
        assertEquals(14, providedLabels.size());
        assertTrue(providedLabels.contains(TreeLayer.TREE_COLUMN_CELL));
        assertTrue(providedLabels.contains(DefaultTreeLayerConfiguration.TREE_COLLAPSED_CONFIG_TYPE));
        assertTrue(providedLabels.contains(DefaultTreeLayerConfiguration.TREE_EXPANDED_CONFIG_TYPE));
        assertTrue(providedLabels.contains(DefaultTreeLayerConfiguration.TREE_LEAF_CONFIG_TYPE));
        assertTrue(providedLabels.contains(DefaultTreeLayerConfiguration.TREE_DEPTH_CONFIG_TYPE + "0"));
        assertTrue(providedLabels.contains(HierarchicalTreeLayer.COLLAPSED_CHILD));
        assertTrue(providedLabels.contains(HierarchicalTreeLayer.LEVEL_HEADER_CELL));
    }

}
