/*******************************************************************************
 * Copyright (c) 2019 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.preserve;

import java.io.Serializable;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.selection.preserve.Selections.Row;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowLayer;

/**
 * Specialization of {@link PreserveSelectionModel} that is also able to handle
 * and preserve selections in a summary row.
 *
 * @param <T>
 *            the type of object in the backing data list.
 *
 * @since 1.6
 */
public class SummaryRowPreserveSelectionModel<T> extends PreserveSelectionModel<T> {

    /**
     * Creates a row sortable selection model that supports selection in a
     * summary row.
     *
     * @param selectionLayer
     *            provider of cell information
     * @param rowDataProvider
     *            provider of underlying row objects
     * @param rowIdAccessor
     *            provider of unique IDs for the rows
     */
    public SummaryRowPreserveSelectionModel(
            IUniqueIndexLayer selectionLayer,
            IRowDataProvider<T> rowDataProvider,
            IRowIdAccessor<T> rowIdAccessor) {
        super(selectionLayer, rowDataProvider, rowIdAccessor);
    }

    @Override
    protected Serializable getRowIdByPosition(int rowPosition) {
        if (rowPosition == getSummaryRowPosition()) {
            return SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL;
        }
        return super.getRowIdByPosition(rowPosition);
    }

    @Override
    protected int getRowPositionByRowObject(T rowObject) {
        if (rowObject == null) {
            // a special case of a summary row
            return getSummaryRowPosition();
        }
        return super.getRowPositionByRowObject(rowObject);
    }

    @Override
    protected boolean ignoreVerticalChange(Row<T> row) {
        return row.getId() == SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL;
    }

    private int getSummaryRowPosition() {
        int lastPosition = this.selectionLayer.getRowCount() - 1;
        LabelStack configLabelsByPosition = this.selectionLayer.getUnderlyingLayerByPosition(0, 0).getConfigLabelsByPosition(0, lastPosition);
        if ((configLabelsByPosition != null) && configLabelsByPosition.hasLabel(SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL)) {
            return lastPosition;
        }
        return -1;
    }

}
