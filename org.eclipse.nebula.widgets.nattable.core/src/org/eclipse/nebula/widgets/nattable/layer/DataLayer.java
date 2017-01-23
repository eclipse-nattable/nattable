/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     neal zhang <nujiah001@126.com> - Bug 448934
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Added scaling
 ******************************************************************************/

package org.eclipse.nebula.widgets.nattable.layer;

import java.util.Collection;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.StructuralRefreshCommand;
import org.eclipse.nebula.widgets.nattable.command.StructuralRefreshCommandHandler;
import org.eclipse.nebula.widgets.nattable.command.VisualRefreshCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.event.ResizeStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommandHandler;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnSizeConfigurationCommandHandler;
import org.eclipse.nebula.widgets.nattable.resize.command.MultiColumnResizeCommandHandler;
import org.eclipse.nebula.widgets.nattable.resize.command.MultiRowResizeCommandHandler;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommandHandler;
import org.eclipse.nebula.widgets.nattable.resize.command.RowSizeConfigurationCommandHandler;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEvent;
import org.eclipse.nebula.widgets.nattable.resize.event.RowResizeEvent;

/**
 * Wraps the {@link IDataProvider}, and serves as the data source for all other
 * layers. Also, tracks the size of the columns and the rows using
 * {@link SizeConfig} objects. Since this layer sits directly on top of the data
 * source, at this layer index == position.
 */
public class DataLayer extends AbstractLayer implements IUniqueIndexLayer {

    public static final String PERSISTENCE_KEY_ROW_HEIGHT = ".rowHeight"; //$NON-NLS-1$
    public static final String PERSISTENCE_KEY_COLUMN_WIDTH = ".columnWidth"; //$NON-NLS-1$

    public static final int DEFAULT_COLUMN_WIDTH = 100;
    public static final int DEFAULT_ROW_HEIGHT = 20;

    protected IDataProvider dataProvider;

    protected SizeConfig columnWidthConfig;
    protected SizeConfig rowHeightConfig;

    public DataLayer(IDataProvider dataProvider) {
        this(dataProvider, DEFAULT_COLUMN_WIDTH, DEFAULT_ROW_HEIGHT);
    }

    public DataLayer(IDataProvider dataProvider, int defaultColumnWidth, int defaultRowHeight) {

        this(defaultColumnWidth, defaultRowHeight);

        setDataProvider(dataProvider);
    }

    protected DataLayer() {
        this(DEFAULT_COLUMN_WIDTH, DEFAULT_ROW_HEIGHT);
    }

    protected DataLayer(int defaultColumnWidth, int defaultRowHeight) {
        this.columnWidthConfig = new SizeConfig(defaultColumnWidth);
        this.rowHeightConfig = new SizeConfig(defaultRowHeight);

        registerCommandHandlers();
    }

    // Persistence

    @Override
    public void saveState(String prefix, Properties properties) {
        super.saveState(prefix, properties);
        this.columnWidthConfig.saveState(
                prefix + PERSISTENCE_KEY_COLUMN_WIDTH,
                properties);
        this.rowHeightConfig.saveState(
                prefix + PERSISTENCE_KEY_ROW_HEIGHT,
                properties);
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        super.loadState(prefix, properties);
        this.columnWidthConfig.loadState(
                prefix + PERSISTENCE_KEY_COLUMN_WIDTH,
                properties);
        this.rowHeightConfig.loadState(
                prefix + PERSISTENCE_KEY_ROW_HEIGHT,
                properties);

        if (!properties.containsKey(NatTable.INITIAL_PAINT_COMPLETE_FLAG))
            fireLayerEvent(new StructuralRefreshEvent(this));
    }

    // Configuration

    @Override
    protected void registerCommandHandlers() {
        registerCommandHandler(new ColumnResizeCommandHandler(this));
        registerCommandHandler(new MultiColumnResizeCommandHandler(this));
        registerCommandHandler(new ColumnSizeConfigurationCommandHandler(this));
        registerCommandHandler(new RowResizeCommandHandler(this));
        registerCommandHandler(new MultiRowResizeCommandHandler(this));
        registerCommandHandler(new RowSizeConfigurationCommandHandler(this));
        registerCommandHandler(new UpdateDataCommandHandler(this));
        registerCommandHandler(new StructuralRefreshCommandHandler());
        registerCommandHandler(new VisualRefreshCommandHandler());
        registerCommandHandler(new ConfigureScalingCommandHandler(
                this.columnWidthConfig, this.rowHeightConfig));
    }

    /**
     *
     * @return The {@link IDataProvider} that is used by this {@link DataLayer}.
     */
    public IDataProvider getDataProvider() {
        return this.dataProvider;
    }

    /**
     * Set the {@link IDataProvider} to use.
     *
     * @param dataProvider
     *            The {@link IDataProvider} that should be used by this
     *            {@link DataLayer}.
     * @since 1.4
     */
    public void setDataProvider(IDataProvider dataProvider) {
        if (this.dataProvider instanceof IPersistable) {
            unregisterPersistable((IPersistable) this.dataProvider);
        }

        this.dataProvider = dataProvider;

        if (dataProvider instanceof IPersistable) {
            registerPersistable((IPersistable) dataProvider);
        }
    }

    /**
     * Gets the value at the given column and row index.
     *
     * @param columnIndex
     *            The column index of the cell whose value is requested.
     * @param rowIndex
     *            The row index of the cell whose value is requested.
     * @return the data value associated with the specified cell
     */
    public Object getDataValue(int columnIndex, int rowIndex) {
        return this.dataProvider.getDataValue(columnIndex, rowIndex);
    }

    /**
     * Sets the value at the given column and row index. Optional operation.
     * Should throw UnsupportedOperationException if this operation is not
     * supported.
     *
     * @param columnIndex
     *            The column index of the cell whose value is requested.
     * @param rowIndex
     *            The row index of the cell whose value is requested.
     * @param newValue
     *            The new value that should be set.
     */
    public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
        this.dataProvider.setDataValue(columnIndex, rowIndex, newValue);
    }

    // Horizontal features

    // Columns

    @Override
    public int getColumnCount() {
        return this.dataProvider.getColumnCount();
    }

    @Override
    public int getPreferredColumnCount() {
        return getColumnCount();
    }

    /**
     * This is the root coordinate system, so the column index is always equal
     * to the column position.
     */
    @Override
    public int getColumnIndexByPosition(int columnPosition) {
        if (columnPosition >= 0 && columnPosition < getColumnCount()) {
            return columnPosition;
        } else {
            return -1;
        }
    }

    /**
     * This is the root coordinate system, so the column position is always
     * equal to the column index.
     */
    @Override
    public int getColumnPositionByIndex(int columnIndex) {
        if (columnIndex >= 0 && columnIndex < getColumnCount()) {
            return columnIndex;
        } else {
            return -1;
        }
    }

    @Override
    public int localToUnderlyingColumnPosition(int localColumnPosition) {
        return localColumnPosition;
    }

    @Override
    public int underlyingToLocalColumnPosition(
            ILayer sourceUnderlyingLayer,
            int underlyingColumnPosition) {
        return underlyingColumnPosition;
    }

    @Override
    public Collection<Range> underlyingToLocalColumnPositions(
            ILayer sourceUnderlyingLayer,
            Collection<Range> underlyingColumnPositionRanges) {
        return underlyingColumnPositionRanges;
    }

    // Width

    @Override
    public int getWidth() {
        return this.columnWidthConfig.getAggregateSize(getColumnCount());
    }

    @Override
    public int getPreferredWidth() {
        return getWidth();
    }

    /**
     * @return The default column width that is used if no specialized width is
     *         configured for a column.
     */
    public int getDefaultColumnWidth() {
        return this.columnWidthConfig.getDefaultSize();
    }

    @Override
    public int getColumnWidthByPosition(int columnPosition) {
        return this.columnWidthConfig.getSize(columnPosition);
    }

    public void setColumnWidthByPosition(int columnPosition, int width) {
        setColumnWidthByPosition(columnPosition, width, true);
    }

    public void setColumnWidthByPosition(int columnPosition, int width, boolean fireEvent) {
        this.columnWidthConfig.setSize(columnPosition, width);
        if (fireEvent)
            fireLayerEvent(new ColumnResizeEvent(this, columnPosition));
    }

    public void setColumnWidthPercentageByPosition(int columnPosition, int width) {
        this.columnWidthConfig.setPercentage(columnPosition, width);
        fireLayerEvent(new ColumnResizeEvent(this, columnPosition));
    }

    public void setDefaultColumnWidth(int width) {
        this.columnWidthConfig.setDefaultSize(width);
    }

    public void setDefaultColumnWidthByPosition(int columnPosition, int width) {
        this.columnWidthConfig.setDefaultSize(columnPosition, width);
    }

    // Column resize

    @Override
    public boolean isColumnPositionResizable(int columnPosition) {
        return this.columnWidthConfig.isPositionResizable(columnPosition);
    }

    public void setColumnPositionResizable(int columnPosition, boolean resizable) {
        this.columnWidthConfig.setPositionResizable(columnPosition, resizable);
    }

    public void setColumnsResizableByDefault(boolean resizableByDefault) {
        this.columnWidthConfig.setResizableByDefault(resizableByDefault);
    }

    // Underlying

    @Override
    public Collection<ILayer> getUnderlyingLayersByColumnPosition(int columnPosition) {
        return null;
    }

    // Vertical features

    // Rows

    @Override
    public int getRowCount() {
        return this.dataProvider.getRowCount();
    }

    @Override
    public int getPreferredRowCount() {
        return getRowCount();
    }

    /**
     * This is the root coordinate system, so the row index is always equal to
     * the row position.
     */
    @Override
    public int getRowIndexByPosition(int rowPosition) {
        if (rowPosition >= 0 && rowPosition < getRowCount()) {
            return rowPosition;
        } else {
            return -1;
        }
    }

    /**
     * This is the root coordinate system, so the row position is always equal
     * to the row index.
     */
    @Override
    public int getRowPositionByIndex(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < getRowCount()) {
            return rowIndex;
        } else {
            return -1;
        }
    }

    @Override
    public int localToUnderlyingRowPosition(int localRowPosition) {
        return localRowPosition;
    }

    @Override
    public int underlyingToLocalRowPosition(
            ILayer sourceUnderlyingLayer,
            int underlyingRowPosition) {
        return underlyingRowPosition;
    }

    @Override
    public Collection<Range> underlyingToLocalRowPositions(
            ILayer sourceUnderlyingLayer,
            Collection<Range> underlyingRowPositionRanges) {
        return underlyingRowPositionRanges;
    }

    // Height

    @Override
    public int getHeight() {
        return this.rowHeightConfig.getAggregateSize(getRowCount());
    }

    @Override
    public int getPreferredHeight() {
        return getHeight();
    }

    /**
     * @return The default row height that is used if no specialized height is
     *         configured for a row.
     */
    public int getDefaultRowHeight() {
        return this.rowHeightConfig.getDefaultSize();
    }

    @Override
    public int getRowHeightByPosition(int rowPosition) {
        return this.rowHeightConfig.getSize(rowPosition);
    }

    public void setRowHeightByPosition(int rowPosition, int height) {
        setRowHeightByPosition(rowPosition, height, true);
    }

    public void setRowHeightByPosition(int rowPosition, int height, boolean fireEvent) {
        this.rowHeightConfig.setSize(rowPosition, height);
        if (fireEvent)
            fireLayerEvent(new RowResizeEvent(this, rowPosition));
    }

    public void setRowHeightPercentageByPosition(int rowPosition, int height) {
        this.rowHeightConfig.setPercentage(rowPosition, height);
        fireLayerEvent(new RowResizeEvent(this, rowPosition));
    }

    public void setDefaultRowHeight(int height) {
        this.rowHeightConfig.setDefaultSize(height);
    }

    public void setDefaultRowHeightByPosition(int rowPosition, int height) {
        this.rowHeightConfig.setDefaultSize(rowPosition, height);
    }

    // Row resize

    @Override
    public boolean isRowPositionResizable(int rowPosition) {
        return this.rowHeightConfig.isPositionResizable(rowPosition);
    }

    public void setRowPositionResizable(int rowPosition, boolean resizable) {
        this.rowHeightConfig.setPositionResizable(rowPosition, resizable);
    }

    public void setRowsResizableByDefault(boolean resizableByDefault) {
        this.rowHeightConfig.setResizableByDefault(resizableByDefault);
    }

    // Underlying

    @Override
    public Collection<ILayer> getUnderlyingLayersByRowPosition(int rowPosition) {
        return null;
    }

    // Cell features

    @Override
    public Object getDataValueByPosition(int columnPosition, int rowPosition) {
        int columnIndex = getColumnIndexByPosition(columnPosition);
        int rowIndex = getRowIndexByPosition(rowPosition);
        return getDataValue(columnIndex, rowIndex);
    }

    public void setDataValueByPosition(int columnPosition, int rowPosition, Object newValue) {
        int columnIndex = getColumnIndexByPosition(columnPosition);
        int rowIndex = getRowIndexByPosition(rowPosition);
        setDataValue(columnIndex, rowIndex, newValue);
    }

    @Override
    public int getColumnPositionByX(int x) {
        return LayerUtil.getColumnPositionByX(this, x);
    }

    @Override
    public int getRowPositionByY(int y) {
        return LayerUtil.getRowPositionByY(this, y);
    }

    @Override
    public int getStartXOfColumnPosition(int columnPosition) {
        return this.columnWidthConfig.getAggregateSize(columnPosition);
    }

    @Override
    public int getStartYOfRowPosition(int rowPosition) {
        return this.rowHeightConfig.getAggregateSize(rowPosition);
    }

    @Override
    public ILayer getUnderlyingLayerByPosition(int columnPosition, int rowPosition) {
        return null;
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        if (command instanceof ClientAreaResizeCommand
                && command.convertToTargetLayer(this)) {
            ClientAreaResizeCommand clientAreaResizeCommand = (ClientAreaResizeCommand) command;

            boolean refresh = false;
            if (isColumnPercentageSizing()) {
                this.columnWidthConfig.calculatePercentages(
                        this.columnWidthConfig.downScale(clientAreaResizeCommand.getCalcArea().width),
                        getColumnCount());
                refresh = true;
            }
            if (isRowPercentageSizing()) {
                this.rowHeightConfig.calculatePercentages(
                        this.rowHeightConfig.downScale(clientAreaResizeCommand.getCalcArea().height),
                        getRowCount());
                refresh = true;
            }

            if (refresh) {
                fireLayerEvent(new ResizeStructuralRefreshEvent(this));
            }

            return refresh;
        } else if (command instanceof StructuralRefreshCommand) {
            // if we receive a StructuralRefreshCommand we need to ensure
            // that the percentage values are re-calculated
            if (isColumnPercentageSizing()) {
                this.columnWidthConfig.updatePercentageValues(getColumnCount());
            }
            if (isRowPercentageSizing()) {
                this.rowHeightConfig.updatePercentageValues(getRowCount());
            }
        }
        return super.doCommand(command);
    }

    /**
     * @return <code>true</code> if the column sizing is done by percentage
     *         calculation, <code>false</code> if the column sizing is done by
     *         pixel (default)
     */
    public boolean isColumnPercentageSizing() {
        return this.columnWidthConfig.isPercentageSizing();
    }

    /**
     * Configures how the column sizing of this {@link DataLayer} is handled,
     * either pixel sizing or percentage sizing. Default is pixel sizing.
     *
     * @param percentageSizing
     *            <code>true</code> if the column sizing should be done by
     *            percentage calculation, <code>false</code> if the column
     *            sizing should be done by pixel (default)
     */
    public void setColumnPercentageSizing(boolean percentageSizing) {
        this.columnWidthConfig.setPercentageSizing(percentageSizing);
    }

    /**
     * @param position
     *            The position which is asked for the percentage sizing
     *            configuration.
     * @return <code>true</code> if the column sizing for the given position is
     *         done by percentage calculation, <code>false</code> if the column
     *         sizing is done by pixel (default)
     */
    public boolean isColumnPercentageSizing(int position) {
        return this.columnWidthConfig.isPercentageSizing(position);
    }

    /**
     * Configures how the column sizing of this {@link DataLayer} is handled for
     * the given column. Default is pixel sizing.
     *
     * @param position
     *            The position for which the sizing configuration should be set.
     * @param percentageSizing
     *            <code>true</code> if the column sizing should be done by
     *            percentage calculation, <code>false</code> if the column
     *            sizing should be done by pixel (default)
     */
    public void setColumnPercentageSizing(int position, boolean percentageSizing) {
        this.columnWidthConfig.setPercentageSizing(position, percentageSizing);
    }

    /**
     * @return <code>true</code> if the row sizing is done by percentage
     *         calculation, <code>false</code> if the row sizing is done by
     *         pixel (default)
     */
    public boolean isRowPercentageSizing() {
        return this.rowHeightConfig.isPercentageSizing();
    }

    /**
     * Configures how the row sizing of this {@link DataLayer} is handled,
     * either pixel sizing or percentage sizing. Default is pixel sizing.
     *
     * @param percentageSizing
     *            <code>true</code> if the row sizing should be done by
     *            percentage calculation, <code>false</code> if the row sizing
     *            should be done by pixel (default)
     */
    public void setRowPercentageSizing(boolean percentageSizing) {
        this.rowHeightConfig.setPercentageSizing(percentageSizing);
    }

    /**
     * @param position
     *            The position which is asked for the percentage sizing
     *            configuration.
     * @return <code>true</code> if the row sizing for the given position is
     *         done by percentage calculation, <code>false</code> if the row
     *         sizing is done by pixel (default)
     */
    public boolean isRowPercentageSizing(int position) {
        return this.rowHeightConfig.isPercentageSizing(position);
    }

    /**
     * Configures how the row sizing of this {@link DataLayer} is handled for
     * the given row. Default is pixel sizing.
     *
     * @param position
     *            The row position for which the sizing configuration should be
     *            set.
     * @param percentageSizing
     *            <code>true</code> if the row sizing should be done by
     *            percentage calculation, <code>false</code> if the row sizing
     *            should be done by pixel (default)
     */
    public void setRowPercentageSizing(int position, boolean percentageSizing) {
        this.rowHeightConfig.setPercentageSizing(position, percentageSizing);
    }
}