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
package org.eclipse.nebula.widgets.nattable.hierarchical.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.car.CarService;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalHelper;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalSpanningDataProvider;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalTreeLayer;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalWrapper;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.SpanningDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.junit.Before;
import org.junit.Test;

public class HierarchicalTreeColumnReorderDragModeTest {

    private HierarchicalTreeLayer treeLayer;

    @Before
    public void setup() {
        // de-normalize the object graph without parent structure objects
        List<HierarchicalWrapper> data = HierarchicalHelper.deNormalize(CarService.getInput(), false, CarService.PROPERTY_NAMES_COMPACT);

        HierarchicalReflectiveColumnPropertyAccessor columnPropertyAccessor =
                new HierarchicalReflectiveColumnPropertyAccessor(CarService.PROPERTY_NAMES_COMPACT);

        IRowDataProvider<HierarchicalWrapper> bodyDataProvider = new ListDataProvider<>(data, columnPropertyAccessor);
        HierarchicalSpanningDataProvider spanningDataProvider = new HierarchicalSpanningDataProvider(bodyDataProvider, CarService.PROPERTY_NAMES_COMPACT);
        DataLayer bodyDataLayer = new SpanningDataLayer(spanningDataProvider);

        // simply apply labels for every column by index
        bodyDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());
        ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(bodyDataLayer);
        ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);
        SelectionLayer selectionLayer = new SelectionLayer(columnHideShowLayer);
        this.treeLayer = new HierarchicalTreeLayer(selectionLayer, data, CarService.PROPERTY_NAMES_COMPACT);
    }

    @Test
    public void testNoLevelHeaderReorder() {
        HierarchicalTreeColumnReorderDragMode dragMode = new HierarchicalTreeColumnReorderDragMode(this.treeLayer);
        assertFalse(dragMode.isValidTargetColumnPosition(this.treeLayer, 0, 2));
    }

    @Test
    public void testReorderToLastColumnInLevel() {
        HierarchicalTreeColumnReorderDragMode dragMode = new HierarchicalTreeColumnReorderDragMode(this.treeLayer);
        assertTrue(dragMode.isValidTargetColumnPosition(this.treeLayer, 1, 3));
    }

    @Test
    public void testReorderToFirstColumnInLevel() {
        HierarchicalTreeColumnReorderDragMode dragMode = new HierarchicalTreeColumnReorderDragMode(this.treeLayer);
        assertTrue(dragMode.isValidTargetColumnPosition(this.treeLayer, 2, 1));
    }

    @Test
    public void testNoReorderInOtherLevel() {
        HierarchicalTreeColumnReorderDragMode dragMode = new HierarchicalTreeColumnReorderDragMode(this.treeLayer);
        assertFalse(dragMode.isValidTargetColumnPosition(this.treeLayer, 1, 4));
        assertFalse(dragMode.isValidTargetColumnPosition(this.treeLayer, 4, 1));
    }
}
