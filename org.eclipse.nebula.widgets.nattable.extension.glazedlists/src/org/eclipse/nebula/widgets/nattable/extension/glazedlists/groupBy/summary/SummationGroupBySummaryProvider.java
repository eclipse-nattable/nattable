/*******************************************************************************
 * Copyright (c) 2013, 2020 Alexandre Pauzies and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexandre Pauzies <alexandre@pauzies.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.summary;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;

public class SummationGroupBySummaryProvider<T> implements
        IGroupBySummaryProvider<T> {

    private final IColumnAccessor<T> columnAccessor;

    public SummationGroupBySummaryProvider(IColumnAccessor<T> columnAccessor) {
        this.columnAccessor = columnAccessor;
    }

    @Override
    public Object summarize(int columnIndex, List<T> children) {
        double summaryValue = 0;
        for (T child : children) {
            Object dataValue = this.columnAccessor.getDataValue(child, columnIndex);
            if (dataValue instanceof Number) {
                summaryValue += ((Number) dataValue).doubleValue();
            }
        }

        return summaryValue;
    }

}