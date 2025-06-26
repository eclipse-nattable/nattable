/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group;

import java.util.Collection;

import org.eclipse.collections.api.factory.primitive.IntSets;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.group.command.ColumnGroupExpandCollapseCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.AbstractColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;

/**
 * Tracks the Expand/Collapse of a Column Group header NOTE: Only relevant when
 * Column Grouping is enabled.
 */
public class ColumnGroupExpandCollapseLayer extends AbstractColumnHideShowLayer implements IColumnGroupModelListener {

    private final ColumnGroupModel[] models;

    public ColumnGroupExpandCollapseLayer(IUniqueIndexLayer underlyingLayer, ColumnGroupModel model) {
        this(underlyingLayer, new ColumnGroupModel[] { model });
    }

    public ColumnGroupExpandCollapseLayer(IUniqueIndexLayer underlyingLayer, ColumnGroupModel... models) {
        super(underlyingLayer);
        this.models = models;

        for (ColumnGroupModel model : models) {
            model.registerColumnGroupModelListener(this);
        }

        registerCommandHandler(new ColumnGroupExpandCollapseCommandHandler(this));
    }

    public ColumnGroupModel getModel(int row) {
        // fallback in case of more complex layer compositions
        // if there is a ColumnGroupModel requested for a row that is greater
        // than the registered models, always use the bottom most
        // ColumnGroupModel this is the same behaviour as it was before the
        // modifications to support expand/collapse for two level column groups
        if (row >= this.models.length) {
            row = this.models.length - 1;
        }
        return this.models[row];
    }

    // Expand/collapse

    @Override
    public boolean isColumnIndexHidden(int columnIndex) {

        IUniqueIndexLayer underlyingLayer = getUnderlyingLayer();

        boolean isHiddeninUnderlyingLayer =
                ColumnGroupUtils.isColumnIndexHiddenInUnderLyingLayer(columnIndex, underlyingLayer);

        if (isHiddeninUnderlyingLayer) {
            return true;
        }

        for (ColumnGroupModel model : this.models) {
            ColumnGroup columnGroup = model.getColumnGroupByIndex(columnIndex);
            boolean isCollapsedAndStaticColumn = columnGroup != null
                    && columnGroup.isCollapsed()
                    && !ColumnGroupUtils.isStaticOrFirstVisibleColumn(columnIndex, underlyingLayer, model);

            if (isCollapsedAndStaticColumn) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Collection<Integer> getHiddenColumnIndexes() {
        return ArrayUtil.asIntegerList(getHiddenColumnIndexesArray());
    }

    @Override
    public int[] getHiddenColumnIndexesArray() {
        MutableIntSet hiddenColumnIndexes = IntSets.mutable.empty();

        IUniqueIndexLayer underlyingLayer = getUnderlyingLayer();
        int underlyingColumnCount = underlyingLayer.getColumnCount();
        for (int i = 0; i < underlyingColumnCount; i++) {
            int columnIndex = underlyingLayer.getColumnIndexByPosition(i);

            for (ColumnGroupModel model : this.models) {
                ColumnGroup columnGroup = model.getColumnGroupByIndex(columnIndex);

                if (columnGroup != null && columnGroup.isCollapsed()) {
                    if (!ColumnGroupUtils.isStaticOrFirstVisibleColumn(columnIndex, underlyingLayer, model)) {
                        hiddenColumnIndexes.add(columnIndex);
                    }
                }
            }
        }

        return hiddenColumnIndexes.toSortedArray();
    }

    @Override
    public boolean hasHiddenColumns() {
        for (ColumnGroupModel model : this.models) {
            if (model.getCollapsedColumnCount() > 0) {
                return true;
            }
        }
        return false;
    }

    // IColumnGroupModelListener

    @Override
    public void columnGroupModelChanged() {
        invalidateCache();
    }

}
