/*****************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.hierarchical.action;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HierarchicalTreeColumnReorderDragModeTest {

    private HierarchicalTreeLayer treeLayer;

    @BeforeEach
    public void setup() {
        // de-normalize the object graph without parent structure objects
        List<HierarchicalWrapper> data = HierarchicalHelper.deNormalize(CarService.getInput(), false, CarService.getPropertyNamesCompact());

        HierarchicalReflectiveColumnPropertyAccessor columnPropertyAccessor =
                new HierarchicalReflectiveColumnPropertyAccessor(CarService.getPropertyNamesCompact());

        IRowDataProvider<HierarchicalWrapper> bodyDataProvider = new ListDataProvider<>(data, columnPropertyAccessor);
        HierarchicalSpanningDataProvider spanningDataProvider = new HierarchicalSpanningDataProvider(bodyDataProvider, CarService.getPropertyNamesCompact());
        DataLayer bodyDataLayer = new SpanningDataLayer(spanningDataProvider);

        // simply apply labels for every column by index
        bodyDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());
        ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(bodyDataLayer);
        ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);
        SelectionLayer selectionLayer = new SelectionLayer(columnHideShowLayer);
        this.treeLayer = new HierarchicalTreeLayer(selectionLayer, data, CarService.getPropertyNamesCompact());
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
