/*******************************************************************************
 * Copyright (c) 2014, 2017 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 462367
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.ContextualDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.summaryrow.ISummaryProvider;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;

/**
 * {@link IDisplayConverter} that is used for conversion of
 * {@link GroupByObject}s.
 * <p>
 * Should be registered for the label {@link GroupByDataLayer#GROUP_BY_OBJECT}.
 * </p>
 * <p>
 * It only returns a converted value in case of the tree column or a groupBy
 * summary is configured. For this it searches for an underlying
 * {@link IDisplayConverter} based on the GroupBy value or the summary column.
 * </p>
 *
 * @param <T>
 *            the type of the underlying data in the {@link GroupByDataLayer}
 *
 * @see GroupByDataLayer
 * @see GroupByDataLayerConfiguration
 */
public class GroupByDisplayConverter<T> extends ContextualDisplayConverter {

    private Object defaultSummaryValue = ISummaryProvider.DEFAULT_SUMMARY_VALUE;

    protected final Map<Integer, IDisplayConverter> wrappedConverters = new HashMap<Integer, IDisplayConverter>();
    protected final Map<Integer, IDisplayConverter> converterCache = new HashMap<Integer, IDisplayConverter>();

    protected final GroupByDataLayer<T> groupByDataLayer;

    /**
     *
     * @param groupByDataLayer
     *            The {@link GroupByDataLayer} necessary to perform index based
     *            or content based operations.
     */
    public GroupByDisplayConverter(GroupByDataLayer<T> groupByDataLayer) {
        this.groupByDataLayer = groupByDataLayer;
    }

    /**
     *
     * @param groupByDataLayer
     *            The {@link GroupByDataLayer} necessary to perform index based
     *            or content based operations.
     * @param defaultSummaryValue
     *            The value that will be shown in case the summary value is not
     *            calculated yet.
     */
    public GroupByDisplayConverter(GroupByDataLayer<T> groupByDataLayer, Object defaultSummaryValue) {
        this.groupByDataLayer = groupByDataLayer;
        this.defaultSummaryValue = defaultSummaryValue;
    }

    @Override
    public Object canonicalToDisplayValue(ILayerCell cell, IConfigRegistry configRegistry, Object canonicalValue) {
        // if the cell is a tree column cell return the data value
        if (cell.getConfigLabels().hasLabel(TreeLayer.TREE_COLUMN_CELL)) {
            Object displayValue = getDisplayValue(cell, configRegistry, canonicalValue);

            // handle child count pattern
            if (canonicalValue instanceof GroupByObject) {
                GroupByObject groupByObject = (GroupByObject) canonicalValue;

                String childCountPattern = configRegistry.getConfigAttribute(
                        GroupByConfigAttributes.GROUP_BY_CHILD_COUNT_PATTERN,
                        DisplayMode.NORMAL,
                        cell.getConfigLabels().getLabels());

                if (childCountPattern != null && childCountPattern.length() > 0) {
                    List<T> children = this.groupByDataLayer.getItemsInGroup(groupByObject);
                    int directChildCount = this.groupByDataLayer.getTreeRowModel().getDirectChildren(cell.getRowIndex()).size();
                    displayValue = String.valueOf(displayValue)
                            + " " //$NON-NLS-1$
                            + MessageFormat.format(childCountPattern, children.size(), directChildCount);
                }
            }

            return displayValue;
        } else if (cell.getConfigLabels().hasLabel(GroupByDataLayer.GROUP_BY_SUMMARY)) {
            if (canonicalValue == null) {
                return this.defaultSummaryValue;
            }
            return getDisplayValue(cell, configRegistry, canonicalValue);
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    @Override
    public Object displayToCanonicalValue(ILayerCell cell, IConfigRegistry configRegistry, Object displayValue) {
        // this method is called by editing features
        // since the default implementation doesn't support editing of
        // GroupByValues, this method simply returns the display value
        return displayValue;
    }

    /**
     * Searches for the {@link IDisplayConverter} to use for the given
     * {@link ILayerCell} and converts the given canonical value to the display
     * value using the found converter.
     *
     * @param cell
     *            The {@link ILayerCell} for which the display value is asked.
     * @param configRegistry
     *            The {@link ConfigRegistry} necessary to retrieve the
     *            {@link IDisplayConverter}.
     * @param canonicalValue
     *            The canonical value to convert.
     * @return The canonical value converted to the display value.
     */
    protected Object getDisplayValue(ILayerCell cell, IConfigRegistry configRegistry, Object canonicalValue) {
        IDisplayConverter converter = null;
        Object canonical = canonicalValue;

        if (this.wrappedConverters.containsKey(cell.getColumnIndex())) {
            converter = this.wrappedConverters.get(cell.getColumnIndex());
        } else if (canonicalValue instanceof GroupByObject) {
            GroupByObject groupByObject = (GroupByObject) canonicalValue;
            int lastGroupingIndex = -1;
            for (Map.Entry<Integer, Object> groupEntry : groupByObject.getDescriptor().entrySet()) {
                lastGroupingIndex = groupEntry.getKey();
            }

            if (lastGroupingIndex >= 0) {
                canonical = ((GroupByObject) canonicalValue).getValue();

                // check if we already have a converter for that index in the
                // cache
                if (this.converterCache.containsKey(lastGroupingIndex)) {
                    converter = this.converterCache.get(lastGroupingIndex);
                } else {
                    int rowPosition = cell.getRowPosition() + 1;
                    LabelStack stackBelow = this.groupByDataLayer.getConfigLabelsByPosition(lastGroupingIndex, rowPosition);
                    while (stackBelow.hasLabel(GroupByDataLayer.GROUP_BY_OBJECT)) {
                        stackBelow = this.groupByDataLayer.getConfigLabelsByPosition(lastGroupingIndex, ++rowPosition);
                    }

                    converter = configRegistry.getConfigAttribute(
                            CellConfigAttributes.DISPLAY_CONVERTER,
                            DisplayMode.NORMAL,
                            stackBelow.getLabels());

                    // this way we are caching the found converters to avoid
                    // performance issues on searching for the correct one
                    // Note:
                    // Doing this avoids the possibility to change the converter
                    // at runtime, which is a rather uncommon scenario.
                    // In case the exchanging the converter at runtime is
                    // necessary you need to unregister any cached converter in
                    // this converter additionally
                    if (!this.converterCache.containsKey(lastGroupingIndex)) {
                        this.converterCache.put(lastGroupingIndex, converter);
                    }
                }
            }
        } else {
            // create a copy of the label stack to avoid finding this
            // converter again
            // Note: this displayConverter needs to be registered for the
            // GroupByDataLayer.GROUP_BY_OBJECT label
            List<String> labels = new ArrayList<String>(cell.getConfigLabels().getLabels());
            labels.remove(GroupByDataLayer.GROUP_BY_OBJECT);
            converter = configRegistry.getConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    DisplayMode.NORMAL,
                    labels);

            if (converter == this) {
                // we found ourself again, so let's skip this
                converter = null;
            }
        }

        if (converter == null) {
            converter = new DefaultDisplayConverter();
        }

        return converter.canonicalToDisplayValue(cell, configRegistry, canonical);
    }

    /**
     * Registers a given {@link IDisplayConverter} for a column index. Only
     * necessary to override the searching for a {@link IDisplayConverter} that
     * is registered for the column of the grouping.
     *
     * @param columnIndex
     *            The column index for which the groupBy display converter
     *            should be overriden.
     * @param converter
     *            The {@link IDisplayConverter} that should be used for
     *            converting the groupBy value of the given index.
     */
    public void registerUnderlyingDisplayConverter(int columnIndex, IDisplayConverter converter) {
        this.wrappedConverters.put(columnIndex, converter);
    }

    /**
     * Unregister the {@link IDisplayConverter} that is registered for the given
     * column index.
     *
     * @param columnIndex
     *            The index for which the registered {@link IDisplayConverter}
     *            should be unregistered.
     */
    public void unregisterUnderlyingDisplayConverter(int columnIndex) {
        this.wrappedConverters.remove(columnIndex);
    }

    /**
     * Clear the internal converter cache. Needed in case of dynamical converter
     * registry updates.
     */
    public void clearConverterCache() {
        this.converterCache.clear();
    }
}
