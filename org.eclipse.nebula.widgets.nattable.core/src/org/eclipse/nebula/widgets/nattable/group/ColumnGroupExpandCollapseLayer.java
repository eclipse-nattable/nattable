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
package org.eclipse.nebula.widgets.nattable.group;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.group.command.ColumnGroupExpandCollapseCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.AbstractColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;

/**
 * Tracks the Expand/Collapse of a Column Group header NOTE: Only relevant when
 * Column Grouping is enabled.
 */
public class ColumnGroupExpandCollapseLayer extends AbstractColumnHideShowLayer
        implements IColumnGroupModelListener {

    private final ColumnGroupModel[] models;

    public ColumnGroupExpandCollapseLayer(IUniqueIndexLayer underlyingLayer,
            ColumnGroupModel model) {
        this(underlyingLayer, new ColumnGroupModel[] { model });
    }

    public ColumnGroupExpandCollapseLayer(IUniqueIndexLayer underlyingLayer,
            ColumnGroupModel... models) {
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
        // than the
        // registered models, always use the bottom most ColumnGroupModel
        // this is the same behaviour as it was before the modifications to
        // support
        // expand/collapse for two level column groups
        if (row >= this.models.length) {
            row = this.models.length - 1;
        }
        return this.models[row];
    }

    // Expand/collapse

    @Override
    public boolean isColumnIndexHidden(int columnIndex) {

        IUniqueIndexLayer underlyingLayer = (IUniqueIndexLayer) getUnderlyingLayer();

        boolean isHiddeninUnderlyingLayer = ColumnGroupUtils
                .isColumnIndexHiddenInUnderLyingLayer(columnIndex, this,
                        underlyingLayer);

        if (isHiddeninUnderlyingLayer)
            return true;

        for (ColumnGroupModel model : this.models) {
            ColumnGroup columnGroup = model.getColumnGroupByIndex(columnIndex);
            boolean isCollapsedAndStaticColumn = columnGroup != null
                    && columnGroup.isCollapsed()
                    && !ColumnGroupUtils.isStaticOrFirstVisibleColumn(
                            columnIndex, underlyingLayer, underlyingLayer,
                            model);

            if (isCollapsedAndStaticColumn)
                return true;
        }

        return false;
    }

    @Override
    public Collection<Integer> getHiddenColumnIndexes() {
        Collection<Integer> hiddenColumnIndexes = new HashSet<Integer>();

        IUniqueIndexLayer underlyingLayer = (IUniqueIndexLayer) getUnderlyingLayer();
        int underlyingColumnCount = underlyingLayer.getColumnCount();
        for (int i = 0; i < underlyingColumnCount; i++) {
            int columnIndex = underlyingLayer.getColumnIndexByPosition(i);

            for (ColumnGroupModel model : this.models) {
                ColumnGroup columnGroup = model
                        .getColumnGroupByIndex(columnIndex);

                if (columnGroup != null && columnGroup.isCollapsed()) {
                    if (!ColumnGroupUtils.isStaticOrFirstVisibleColumn(
                            columnIndex, underlyingLayer, underlyingLayer,
                            model)) {
                        hiddenColumnIndexes.add(Integer.valueOf(columnIndex));
                    }
                }
            }
        }

        return hiddenColumnIndexes;
    }

    // IColumnGroupModelListener

    @Override
    public void columnGroupModelChanged() {
        invalidateCache();
    }

}
