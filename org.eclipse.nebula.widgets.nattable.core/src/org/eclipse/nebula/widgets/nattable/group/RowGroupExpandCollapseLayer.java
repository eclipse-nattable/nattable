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

import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.eclipse.nebula.widgets.nattable.group.command.RowGroupExpandCollapseCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.model.IRowGroup;
import org.eclipse.nebula.widgets.nattable.group.model.IRowGroupModel;
import org.eclipse.nebula.widgets.nattable.group.model.IRowGroupModelListener;
import org.eclipse.nebula.widgets.nattable.hideshow.AbstractRowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;

public class RowGroupExpandCollapseLayer<T> extends AbstractRowHideShowLayer implements IRowGroupModelListener {

    private final IRowGroupModel<T> model;

    public RowGroupExpandCollapseLayer(IUniqueIndexLayer underlyingLayer, IRowGroupModel<T> model) {
        super(underlyingLayer);
        this.model = model;

        model.registerRowGroupModelListener(this);

        registerCommandHandler(new RowGroupExpandCollapseCommandHandler<T>(this));
    }

    public IRowGroupModel<T> getModel() {
        return this.model;
    }

    // Expand/collapse

    @Override
    public boolean isRowIndexHidden(int rowIndex) {

        // This can happen if a row has just been removed.
        if (rowIndex >= this.model.getDataProvider().getRowCount()) {
            return true;
        }

        IUniqueIndexLayer underlyingLayer = getUnderlyingLayer();

        boolean isHiddeninUnderlyingLayer =
                RowGroupUtils.isRowIndexHiddenInUnderLyingLayer(rowIndex, this, underlyingLayer);

        // Get the row and the group from our cache and model.
        final T row = this.model.getRowFromIndexCache(rowIndex);
        IRowGroup<T> rowGroup = this.model.getRowGroupForRow(row);

        if (rowGroup == null) {
            return false;
        }

        boolean isCollapsedAndNotStaticRow = RowGroupUtils.isCollapsed(this.model, rowGroup)
                && !rowGroup.getOwnStaticMemberRows().contains(row);

        return isHiddeninUnderlyingLayer || isCollapsedAndNotStaticRow;
    }

    @Override
    public Collection<Integer> getHiddenRowIndexes() {
        return ArrayUtil.asIntegerList(getHiddenRowIndexesArray());
    }

    @Override
    public int[] getHiddenRowIndexesArray() {
        MutableIntSet hiddenRowIndexes = IntSets.mutable.empty();

        IUniqueIndexLayer underlyingLayer = getUnderlyingLayer();
        int underlyingColumnCount = underlyingLayer.getRowCount();
        for (int i = 0; i < underlyingColumnCount; i++) {
            int rowIndex = underlyingLayer.getRowIndexByPosition(i);
            if (isRowIndexHidden(rowIndex)) {
                hiddenRowIndexes.add(rowIndex);
            }
        }

        return hiddenRowIndexes.toSortedArray();
    }

    @Override
    public boolean hasHiddenRows() {
        for (IRowGroup<T> rowGroup : this.model.getRowGroups()) {
            if (rowGroup.isCollapsed()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void rowGroupModelChanged() {
        invalidateCache();
    }

    @Override
    protected synchronized void invalidateCache() {
        super.invalidateCache();
        this.model.invalidateIndexCache();
    }

}
