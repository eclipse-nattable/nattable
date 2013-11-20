package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.summary;

import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;

public class GroupBySummaryConfigAttributes {

	/**
	 * The configuration attribute that is used to calculate the summary for a column.
	 */
	@SuppressWarnings("rawtypes")
	public static final ConfigAttribute<IGroupBySummaryProvider> GROUP_BY_SUMMARY_PROVIDER = new ConfigAttribute<IGroupBySummaryProvider>();
}
