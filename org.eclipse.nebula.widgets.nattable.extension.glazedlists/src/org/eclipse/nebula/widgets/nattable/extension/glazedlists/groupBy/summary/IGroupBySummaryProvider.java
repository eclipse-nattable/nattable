package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.summary;

import java.util.List;

public interface IGroupBySummaryProvider<T> {

    public static final Object DEFAULT_SUMMARY_VALUE = "..."; //$NON-NLS-1$

    /**
     * @param columnIndex
     *            The column index of the column for which the summary should be
     *            calculated.
     * @return The calculated summary value for the column.
     */
    public Object summarize(int columnIndex, List<T> children);
}