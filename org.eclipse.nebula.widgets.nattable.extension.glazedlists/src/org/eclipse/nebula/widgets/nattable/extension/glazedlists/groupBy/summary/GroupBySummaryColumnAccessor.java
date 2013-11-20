package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.summary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByColumnAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByDataLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByObject;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;

public class GroupBySummaryColumnAccessor<T> extends GroupByColumnAccessor<Object> {

	private Map<Integer, IGroupBySummaryProvider<T>> summaryProviderByColumn;
	private final GroupByDataLayer<T> groupByDataLayer;
	private final IConfigRegistry configRegistry;

	private Map<Integer, IGroupBySummaryProvider<T>> getSummaryProviderByColumn() {
		if (summaryProviderByColumn == null) {
			summaryProviderByColumn = new HashMap<Integer, IGroupBySummaryProvider<T>>();
			for (int columnIndex = 0; columnIndex < groupByDataLayer.getColumnCount(); columnIndex++) {
				int columnPosition = groupByDataLayer.getColumnPositionByIndex(columnIndex);
				List<String> labels = groupByDataLayer.getConfigLabelsByPosition(columnPosition, 0).getLabels();
				@SuppressWarnings("unchecked")
				IGroupBySummaryProvider<T> summaryProvider = configRegistry.getConfigAttribute(
						GroupBySummaryConfigAttributes.GROUP_BY_SUMMARY_PROVIDER, DisplayMode.NORMAL, labels);
				if (summaryProvider != null) {
					summaryProviderByColumn.put(columnIndex, summaryProvider);
				}
			}
		}
		return summaryProviderByColumn;
	}

	public GroupBySummaryColumnAccessor(IColumnAccessor<Object> columnAccessor,
			IConfigRegistry configRegistry, GroupByDataLayer<T> groupByDataLayer) {
		super(columnAccessor);
		this.groupByDataLayer = groupByDataLayer;
		this.configRegistry = configRegistry;
	}

	public Object getDataValue(Object rowObject, int columnIndex) {
		if (rowObject instanceof GroupByObject) {
			IGroupBySummaryProvider<T> summaryProvider = getSummaryProviderByColumn().get(columnIndex);
			if (summaryProvider == null) {
				return super.getDataValue(rowObject, columnIndex);
			}
			GroupByObject groupByObject = (GroupByObject) rowObject;
			List<T> children = groupByDataLayer.getElementsInGroup(groupByObject);
			return summaryProvider.summarize(columnIndex, children);
		} else {
			return columnAccessor.getDataValue(rowObject, columnIndex);
		}
	}
}
