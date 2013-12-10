package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.summary;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;

public class SummationGroupBySummaryProvider<T> implements IGroupBySummaryProvider<T> {

	private final IColumnAccessor<T> columnAccessor;

	public SummationGroupBySummaryProvider(IColumnAccessor<T> columnAccessor) {
		this.columnAccessor = columnAccessor;
	}

	@Override
	public Object summarize(int columnIndex, List<T> children) {
		float summaryValue = 0;
		for (T child : children) {
			Object dataValue = columnAccessor.getDataValue(child, columnIndex);
			if (dataValue instanceof Number) {
				summaryValue = summaryValue + Float.parseFloat(dataValue.toString());
			}
		}
		return summaryValue;
	}

}