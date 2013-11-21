package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.summary.IGroupBySummaryProvider;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;

public interface GroupByConfigAttributes {

	/**
	 * The configuration attribute that is used to calculate the summary for a column.
	 */
	@SuppressWarnings("rawtypes")
	ConfigAttribute<IGroupBySummaryProvider> GROUP_BY_SUMMARY_PROVIDER = new ConfigAttribute<IGroupBySummaryProvider>();
	
	/**
	 * Configuration attribute to specify a pattern that is used to render the number of children
	 * for a group. Use the typical Java placeholders for correct usage, e.g. "[{0}]"
	 */
	ConfigAttribute<String> GROUP_BY_CHILD_COUNT_PATTERN = new ConfigAttribute<String>();
}
