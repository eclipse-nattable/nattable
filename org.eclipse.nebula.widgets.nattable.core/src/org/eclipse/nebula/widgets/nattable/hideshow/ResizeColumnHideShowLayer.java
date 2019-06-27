/*****************************************************************************
 * Copyright (c) 2017, 2019 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnShowCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnShowCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllColumnsCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.indicator.HideIndicatorConstants;
import org.eclipse.nebula.widgets.nattable.layer.AbstractIndexLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEvent;
import org.eclipse.nebula.widgets.nattable.search.strategy.ISearchStrategy;

/**
 * Layer to add support for column hide/show feature to a NatTable. Technically
 * hides columns by setting the column width to 0. This way percentage sized
 * columns increase correctly to take the remaining space. This approach is
 * different from the classical {@link ColumnHideShowLayer} where the column is
 * really hidden in the layer which leads to index-position-transformation
 * instead of basic column resizing.
 *
 * <p>
 * <b>Note:</b> It is suggested to set
 * {@link DataLayer#setDistributeRemainingColumnSpace(boolean)} to
 * <code>true</code> when using this layer, or set
 * {@link DataLayer#setFixColumnPercentageValuesOnResize(boolean)} to
 * <code>false</code>. Otherwise a column resize triggers the percentage value
 * calculation of dynamic sized columns, which then leads to gaps as the fixed
 * percentage sized columns to not grow by default.
 * </p>
 *
 * @see ColumnHideShowLayer
 *
 * @since 1.6
 */
public class ResizeColumnHideShowLayer extends AbstractIndexLayerTransform implements IColumnHideShowLayer {

    public static final String PERSISTENCE_KEY_HIDDEN_COLUMNS = ".hiddenColumns"; //$NON-NLS-1$

    /**
     * Map that contains the columns hidden by this layer with the initial width
     * so it can be shown again with the previous width.
     */
    protected final Map<Integer, ColumnSizeInfo> hiddenColumns = new TreeMap<Integer, ColumnSizeInfo>();

    /**
     * The {@link DataLayer} of the body region needed to retrieve the
     * configured column width. Can be removed once the necessary methods become
     * part of the ILayer interface in the next major release.
     */
    private DataLayer bodyDataLayer;

    public ResizeColumnHideShowLayer(IUniqueIndexLayer underlyingLayer, DataLayer bodyDataLayer) {
        super(underlyingLayer);

        this.bodyDataLayer = bodyDataLayer;

        registerCommandHandler(new ColumnHideCommandHandler(this));
        registerCommandHandler(new MultiColumnHideCommandHandler(this));
        registerCommandHandler(new ShowAllColumnsCommandHandler(this));
        registerCommandHandler(new MultiColumnShowCommandHandler(this));
        registerCommandHandler(new ColumnShowCommandHandler(this));
    }

    // Persistence

    @Override
    public void saveState(String prefix, Properties properties) {
        saveMap(this.hiddenColumns, prefix + PERSISTENCE_KEY_HIDDEN_COLUMNS, properties);

        super.saveState(prefix, properties);
    }

    private void saveMap(Map<?, ?> map, String key, Properties properties) {
        if (map.size() > 0) {
            StringBuilder strBuilder = new StringBuilder();
            for (Object index : map.keySet()) {
                strBuilder.append(index);
                strBuilder.append(':');
                strBuilder.append(map.get(index));
                strBuilder.append(',');
            }
            properties.setProperty(key, strBuilder.toString());
        }
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        this.hiddenColumns.clear();
        loadMap(prefix + PERSISTENCE_KEY_HIDDEN_COLUMNS, properties, this.hiddenColumns);

        // there is no need to actually perform additional actions because the
        // width configuration is persisted by the DataLayer itself

        super.loadState(prefix, properties);
    }

    private void loadMap(String key, Properties properties, Map<Integer, ColumnSizeInfo> map) {
        String property = properties.getProperty(key);
        if (property != null) {
            map.clear();

            StringTokenizer tok = new StringTokenizer(property, ","); //$NON-NLS-1$
            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                int separatorIndex = token.indexOf(':');
                map.put(Integer.valueOf(token.substring(0, separatorIndex)),
                        ColumnSizeInfo.valueOf(token.substring(separatorIndex + 1)));
            }
        }
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
        LabelStack labels = super.getConfigLabelsByPosition(columnPosition, rowPosition);
        if (this.hiddenColumns.containsKey(getColumnIndexByPosition(columnPosition))) {
            labels.addLabel(ISearchStrategy.SKIP_SEARCH_RESULT_LABEL);
        }
        if (this.hiddenColumns.containsKey(getColumnIndexByPosition(columnPosition - 1))) {
            labels.addLabel(HideIndicatorConstants.COLUMN_LEFT_HIDDEN);
        }
        if (this.hiddenColumns.containsKey(getColumnIndexByPosition(columnPosition + 1))) {
            labels.addLabel(HideIndicatorConstants.COLUMN_RIGHT_HIDDEN);
        }
        return labels;
    }

    @Override
    public Collection<Integer> getHiddenColumnIndexes() {
        return this.hiddenColumns.keySet();
    }

    @Override
    public void hideColumnPositions(Integer... columnPositions) {
        hideColumnPositions(Arrays.asList(columnPositions));
    }

    @Override
    public void hideColumnPositions(Collection<Integer> columnPositions) {
        Map<Integer, ColumnSizeInfo> positionsToHide = new TreeMap<Integer, ColumnSizeInfo>();

        // On hide we expect that all remaining visible columns share the free
        // space. To avoid that only the adjacent column is increased, we
        // disable fixColumnPercentageValuesOnResize in any case and restore it
        // afterwards
        boolean fix = this.bodyDataLayer.isFixColumnPercentageValuesOnResize();
        this.bodyDataLayer.setFixColumnPercentageValuesOnResize(false);

        for (Integer columnPosition : columnPositions) {
            // transform the position to index
            int columnIndex = getColumnIndexByPosition(columnPosition);
            if (!this.hiddenColumns.containsKey(columnIndex)) {
                // get the currently applied width of the column
                int configuredWidth = this.bodyDataLayer.getConfiguredColumnWidthByPosition(columnIndex);
                // get the currently applied min width of the column
                int configuredMinWidth = this.bodyDataLayer.getConfiguredMinColumnWidthByPosition(columnIndex);
                // get the currently applied resizable info
                boolean configuredResizable = this.bodyDataLayer.isColumnPositionResizable(columnIndex);
                // get the information if the column is configured for
                // percentage sizing
                boolean configuredPercentage = this.bodyDataLayer.isColumnPercentageSizing(columnIndex);
                // get the currently applied percentage width of the column
                double configuredPercentageValue = this.bodyDataLayer.getConfiguredColumnWidthPercentageByPosition(columnIndex);

                positionsToHide.put(
                        columnIndex,
                        new ColumnSizeInfo(
                                configuredWidth,
                                configuredMinWidth,
                                configuredResizable,
                                configuredPercentage,
                                configuredPercentageValue));
            }
        }

        for (Integer columnIndex : positionsToHide.keySet()) {
            // if column is not resizable we need to make it resizable for the
            // moment to make hiding work
            if (!this.bodyDataLayer.isColumnPositionResizable(columnIndex)) {
                this.bodyDataLayer.setColumnPositionResizable(columnIndex, true);
            }
            // if a min width is configured, set it to 0 to make hiding work
            if (this.bodyDataLayer.isMinColumnWidthConfigured()) {
                this.bodyDataLayer.setMinColumnWidth(columnIndex, 0);
            }
            // set the column width to 0
            if (positionsToHide.get(columnIndex).configuredPercentage) {
                this.bodyDataLayer.setColumnWidthPercentageByPosition(columnIndex, 0d);
            } else {
                this.bodyDataLayer.setColumnWidthByPosition(columnIndex, 0, false);
            }
            // make that column not resizable
            this.bodyDataLayer.setColumnPositionResizable(columnIndex, false);
        }

        this.hiddenColumns.putAll(positionsToHide);

        // reset the fixColumnPercentageValuesOnResize flag
        this.bodyDataLayer.setFixColumnPercentageValuesOnResize(fix);

        // fire events
        List<Range> ranges = PositionUtil.getRanges(positionsToHide.keySet());
        for (Range range : ranges) {
            this.bodyDataLayer.fireLayerEvent(new ColumnResizeEvent(this.bodyDataLayer, range));
        }
    }

    @Override
    public void showColumnIndexes(Integer... columnIndexes) {
        showColumnIndexes(Arrays.asList(columnIndexes));
    }

    @Override
    public void showColumnIndexes(Collection<Integer> columnIndexes) {
        List<Integer> toProcess = new ArrayList<Integer>(columnIndexes);

        // only handle column indexes that are hidden
        toProcess.retainAll(this.hiddenColumns.keySet());

        // On show we expect that all visible columns share the free
        // space. To avoid that only the adjacent column is decreased, we
        // disable fixColumnPercentageValuesOnResize in any case and restore it
        // afterwards
        boolean fix = this.bodyDataLayer.isFixColumnPercentageValuesOnResize();
        this.bodyDataLayer.setFixColumnPercentageValuesOnResize(false);

        List<Integer> processed = new ArrayList<Integer>();
        for (Integer index : toProcess) {
            ColumnSizeInfo info = this.hiddenColumns.remove(index);
            if (info != null) {
                processed.add(index);

                // first make the column resizable
                this.bodyDataLayer.setColumnPositionResizable(index, true);
                // set the previous configured width
                if (info.configuredPercentage && info.configuredPercentageValue >= 0) {
                    this.bodyDataLayer.setColumnWidthPercentageByPosition(index, info.configuredPercentageValue);
                } else if (!info.configuredPercentage && info.configuredSize >= 0) {
                    this.bodyDataLayer.setColumnWidthByPosition(index, info.configuredSize, false);
                } else {
                    this.bodyDataLayer.resetColumnWidth(index, false);
                }
                // set the configured resizable value
                this.bodyDataLayer.setColumnPositionResizable(index, info.configuredResizable);
                // set the previous configured min width
                if (info.configuredMinWidth < 0) {
                    this.bodyDataLayer.resetMinColumnWidth(index, false);
                } else {
                    this.bodyDataLayer.setMinColumnWidth(index, info.configuredMinWidth);
                }
            }
        }

        // reset the fixColumnPercentageValuesOnResize flag
        this.bodyDataLayer.setFixColumnPercentageValuesOnResize(fix);

        if (!processed.isEmpty()) {
            List<Range> ranges = PositionUtil.getRanges(processed);

            // fire events
            for (Range range : ranges) {
                this.bodyDataLayer.fireLayerEvent(new ColumnResizeEvent(this.bodyDataLayer, range));
            }
        }
    }

    @Override
    public void showColumnPosition(int columnPosition, boolean showToLeft, boolean showAll) {
        Set<Integer> columnIndexes = new HashSet<Integer>();
        if (showToLeft) {
            int leftColumnIndex = getColumnIndexByPosition(columnPosition - 1);
            if (showAll) {
                int move = 1;
                while (this.hiddenColumns.containsKey(leftColumnIndex)) {
                    columnIndexes.add(leftColumnIndex);
                    move++;
                    leftColumnIndex = getColumnIndexByPosition(columnPosition - move);
                }
            } else if (this.hiddenColumns.containsKey(leftColumnIndex)) {
                columnIndexes.add(leftColumnIndex);
            }
        } else {
            int rightColumnIndex = getColumnIndexByPosition(columnPosition + 1);
            if (showAll) {
                int move = 1;
                while (this.hiddenColumns.containsKey(rightColumnIndex)) {
                    columnIndexes.add(rightColumnIndex);
                    move++;
                    rightColumnIndex = getColumnIndexByPosition(columnPosition + move);
                }
            } else if (this.hiddenColumns.containsKey(rightColumnIndex)) {
                columnIndexes.add(rightColumnIndex);
            }
        }

        if (!columnIndexes.isEmpty()) {
            showColumnIndexes(columnIndexes);
        }
    }

    @Override
    public void showAllColumns() {
        // On show we expect that all visible columns share the free
        // space. To avoid that only the adjacent column is decreased, we
        // disable fixColumnPercentageValuesOnResize in any case and restore it
        // afterwards
        boolean fix = this.bodyDataLayer.isFixColumnPercentageValuesOnResize();
        this.bodyDataLayer.setFixColumnPercentageValuesOnResize(false);

        for (Map.Entry<Integer, ColumnSizeInfo> entry : this.hiddenColumns.entrySet()) {
            Integer index = entry.getKey();
            ColumnSizeInfo info = entry.getValue();
            // first make the column resizable
            this.bodyDataLayer.setColumnPositionResizable(index, true);

            // set the previous configured min width
            if (info.configuredMinWidth < 0) {
                this.bodyDataLayer.resetMinColumnWidth(index, false);
            } else {
                this.bodyDataLayer.setMinColumnWidth(index, info.configuredMinWidth);
            }

            // set the previous configured width
            if (info.configuredPercentage && info.configuredPercentageValue >= 0) {
                this.bodyDataLayer.setColumnWidthPercentageByPosition(index, info.configuredPercentageValue);
            } else if (!info.configuredPercentage && info.configuredSize >= 0) {
                this.bodyDataLayer.setColumnWidthByPosition(index, info.configuredSize, false);
            } else {
                this.bodyDataLayer.resetColumnWidth(index, false);
            }

            // set the configured resizable value
            this.bodyDataLayer.setColumnPositionResizable(index, info.configuredResizable);
        }

        List<Range> ranges = PositionUtil.getRanges(this.hiddenColumns.keySet());

        this.hiddenColumns.clear();

        // reset the fixColumnPercentageValuesOnResize flag
        this.bodyDataLayer.setFixColumnPercentageValuesOnResize(fix);

        // fire events
        for (Range range : ranges) {
            this.bodyDataLayer.fireLayerEvent(new ColumnResizeEvent(this.bodyDataLayer, range));
        }
    }

    @Override
    public Collection<String> getProvidedLabels() {
        Collection<String> result = super.getProvidedLabels();
        result.add(HideIndicatorConstants.COLUMN_LEFT_HIDDEN);
        result.add(HideIndicatorConstants.COLUMN_RIGHT_HIDDEN);
        return result;
    }

    protected static class ColumnSizeInfo {
        public final int configuredSize;
        public final int configuredMinWidth;
        public final boolean configuredResizable;
        public final boolean configuredPercentage;
        public final double configuredPercentageValue;

        public ColumnSizeInfo(int configuredSize, int configuredMinWidth, boolean configuredResizable, boolean configuredPercentage, double configuredPercentageValue) {
            this.configuredSize = configuredSize;
            this.configuredMinWidth = configuredMinWidth;
            this.configuredResizable = configuredResizable;
            this.configuredPercentage = configuredPercentage;
            this.configuredPercentageValue = configuredPercentageValue;
        }

        public static ColumnSizeInfo valueOf(String s) {
            String[] token = s.substring(1, s.length() - 1).split("\\|"); //$NON-NLS-1$
            Integer size = Integer.valueOf(token[0]);
            Integer minWidth = Integer.valueOf(token[1]);
            Boolean resizable = Boolean.valueOf(token[2]);
            Boolean percentage = Boolean.valueOf(token[3]);
            Double percentageValue = Double.valueOf(token[4]);
            return new ColumnSizeInfo(size, minWidth, resizable, percentage, percentageValue);
        }

        @Override
        public String toString() {
            return "[" + this.configuredSize + "|" + this.configuredMinWidth + "|" + this.configuredResizable + "|" + this.configuredPercentage + "|" + this.configuredPercentageValue + "]"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        }
    }
}
