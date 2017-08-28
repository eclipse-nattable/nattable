/*****************************************************************************
 * Copyright (c) 2017 Dirk Fauth.
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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnHideCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnShowCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllColumnsCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.AbstractIndexLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEvent;

/**
 * Layer to add support for column hide/show feature to a NatTable. Technically
 * hides columns by setting the column width to 0. This way percentage sized
 * columns increase correctly to take the remaining space. This approach is
 * different from the classical {@link ColumnHideShowLayer} where the column is
 * really hidden in the layer which leads to index-position-transformation
 * instead of basic column resizing.
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
    public void hideColumnPositions(Integer... columnPositions) {
        hideColumnPositions(Arrays.asList(columnPositions));
    }

    @Override
    public void hideColumnPositions(Collection<Integer> columnPositions) {
        Map<Integer, ColumnSizeInfo> positionsToHide = new TreeMap<Integer, ColumnSizeInfo>();

        for (Integer columnPosition : columnPositions) {
            // transform the position to index
            int columnIndex = getColumnIndexByPosition(columnPosition);
            // get the currently applied width of the column
            int configuredWidth = this.bodyDataLayer.getConfiguredColumnWidthByPosition(columnIndex);
            // get the currently applied resizable info
            boolean configuredResizable = this.bodyDataLayer.isColumnPositionResizable(columnIndex);
            // get the information if the column is configured for percentage
            // sizing
            boolean configuredPercentage = this.bodyDataLayer.isColumnPercentageSizing(columnIndex);

            positionsToHide.put(columnIndex, new ColumnSizeInfo(configuredWidth, configuredResizable, configuredPercentage));
        }

        for (Integer columnIndex : positionsToHide.keySet()) {
            // if column is not resizable we need to make it resizable for the
            // moment to make hiding work
            if (!this.bodyDataLayer.isColumnPositionResizable(columnIndex)) {
                this.bodyDataLayer.setColumnPositionResizable(columnIndex, true);
            }
            // set the column width to 0
            if (positionsToHide.get(columnIndex).configuredPercentage) {
                this.bodyDataLayer.setColumnWidthPercentageByPosition(columnIndex, 0);
            } else {
                this.bodyDataLayer.setColumnWidthByPosition(columnIndex, 0, false);
            }
            // make that column not resizable
            this.bodyDataLayer.setColumnPositionResizable(columnIndex, false);
        }

        this.hiddenColumns.putAll(positionsToHide);

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
        List<Integer> processed = new ArrayList<Integer>();
        for (Integer index : columnIndexes) {
            ColumnSizeInfo info = this.hiddenColumns.remove(index);
            if (info != null) {
                processed.add(index);

                // first make the column resizable
                this.bodyDataLayer.setColumnPositionResizable(index, true);
                // set the previous configured width
                if (info.configuredSize < 0) {
                    this.bodyDataLayer.resetColumnSize(index, false);
                } else if (info.configuredPercentage) {
                    this.bodyDataLayer.setColumnWidthPercentageByPosition(index, info.configuredSize);
                } else {
                    this.bodyDataLayer.setColumnWidthByPosition(index, info.configuredSize, false);
                }
                // set the configured resizable value
                this.bodyDataLayer.setColumnPositionResizable(index, info.configuredResizable);
            }
        }

        if (!processed.isEmpty()) {
            List<Range> ranges = PositionUtil.getRanges(processed);

            // fire events
            for (Range range : ranges) {
                this.bodyDataLayer.fireLayerEvent(new ColumnResizeEvent(this.bodyDataLayer, range));
            }
        }
    }

    @Override
    public void showAllColumns() {
        for (Map.Entry<Integer, ColumnSizeInfo> entry : this.hiddenColumns.entrySet()) {
            // first make the column resizable
            this.bodyDataLayer.setColumnPositionResizable(entry.getKey(), true);
            // set the previous configured width
            if (entry.getValue().configuredSize < 0) {
                this.bodyDataLayer.resetColumnSize(entry.getKey(), false);
            } else if (entry.getValue().configuredPercentage) {
                this.bodyDataLayer.setColumnWidthPercentageByPosition(entry.getKey(), entry.getValue().configuredSize);
            } else {
                this.bodyDataLayer.setColumnWidthByPosition(entry.getKey(), entry.getValue().configuredSize, false);
            }
            // set the configured resizable value
            this.bodyDataLayer.setColumnPositionResizable(entry.getKey(), entry.getValue().configuredResizable);
        }

        List<Range> ranges = PositionUtil.getRanges(this.hiddenColumns.keySet());

        this.hiddenColumns.clear();

        // fire events
        for (Range range : ranges) {
            this.bodyDataLayer.fireLayerEvent(new ColumnResizeEvent(this.bodyDataLayer, range));
        }

    }

    protected static class ColumnSizeInfo {
        public final int configuredSize;
        public final boolean configuredResizable;
        public final boolean configuredPercentage;

        public ColumnSizeInfo(int configuredSize, boolean configuredResizable, boolean configuredPercentage) {
            this.configuredSize = configuredSize;
            this.configuredResizable = configuredResizable;
            this.configuredPercentage = configuredPercentage;
        }

        public static ColumnSizeInfo valueOf(String s) {
            String[] token = s.substring(1, s.length() - 1).split("\\|"); //$NON-NLS-1$
            Integer size = Integer.valueOf(token[0]);
            Boolean resizable = Boolean.valueOf(token[1]);
            Boolean percentage = Boolean.valueOf(token[2]);
            return new ColumnSizeInfo(size, resizable, percentage);
        }

        @Override
        public String toString() {
            return "[" + this.configuredSize + "|" + this.configuredResizable + "|" + this.configuredPercentage + "]"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
    }
}
