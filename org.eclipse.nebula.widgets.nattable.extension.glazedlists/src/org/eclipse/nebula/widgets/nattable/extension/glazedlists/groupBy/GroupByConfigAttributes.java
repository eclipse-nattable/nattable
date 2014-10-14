package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.summary.IGroupBySummaryProvider;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.graphics.Color;

public interface GroupByConfigAttributes {

    /**
     * The configuration attribute that is used to calculate the summary for a
     * column.
     */
    @SuppressWarnings("rawtypes")
    ConfigAttribute<IGroupBySummaryProvider> GROUP_BY_SUMMARY_PROVIDER = new ConfigAttribute<IGroupBySummaryProvider>();

    /**
     * Configuration attribute to specify a pattern that is used to render the
     * number of children for a group. Use the typical Java placeholders for
     * correct usage, e.g. "[{0}]"
     * <ul>
     * <li>0 = The number of base list entities in the group</li>
     * <li>1 = The number of direct child items in the group (without subchilds)
     * </li>
     * </ul>
     */
    ConfigAttribute<String> GROUP_BY_CHILD_COUNT_PATTERN = new ConfigAttribute<String>();

    /**
     * Configuration attribute to specify a hint that should be shown in case no
     * grouping is applied.
     */
    ConfigAttribute<String> GROUP_BY_HINT = new ConfigAttribute<String>();

    /**
     * Configuration attribute to specify the style of a group by hint. Setting
     * the values for foreground, background and font is supported.
     */
    ConfigAttribute<IStyle> GROUP_BY_HINT_STYLE = new ConfigAttribute<IStyle>();

    /**
     * Attribute for configuring the Color that should be used to render the
     * background of the GroupByHeaderLayer. Will be interpreted by the
     * GroupByHeaderPainter.
     */
    ConfigAttribute<Color> GROUP_BY_HEADER_BACKGROUND_COLOR = new ConfigAttribute<Color>();

}
