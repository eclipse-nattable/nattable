/*******************************************************************************
 * Copyright (c) 2012, 2018 Original authors and others.
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
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ResizeStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommandHandler;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnSizeConfigurationCommandHandler;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnWidthResetCommandHandler;
import org.eclipse.nebula.widgets.nattable.resize.command.MultiColumnResizeCommandHandler;
import org.eclipse.nebula.widgets.nattable.resize.command.MultiRowResizeCommandHandler;
import org.eclipse.nebula.widgets.nattable.resize.command.RowHeightResetCommandHandler;
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
        registerCommandHandler(new ConfigureScalingCommandHandler(this.columnWidthConfig, this.rowHeightConfig));
        registerCommandHandler(new ColumnWidthResetCommandHandler(this));
        registerCommandHandler(new RowHeightResetCommandHandler(this));
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
     * <p>
     * <b>Note:</b> The configuration of this flag impacts the size calculation
     * in mixed mode. If this flag is set to <code>false</code>, positions that
     * are configured for fixed percentages will use the full available space
     * for percentage calculation. Setting it to <code>true</code> will cause
     * using the remaining space for percentage calculation. This means if also
     * fixed pixel sized positions are configured, they will be subtracted from
     * the full available space.
     * </p>
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
     * <p>
     * <b>Note:</b> The configuration of this flag impacts the size calculation
     * in mixed mode. If this flag is set to <code>false</code>, positions that
     * are configured for fixed percentages will use the full available space
     * for percentage calculation. Setting it to <code>true</code> will cause
     * using the remaining space for percentage calculation. This means if also
     * fixed pixel sized positions are configured, they will be subtracted from
     * the full available space.
     * </p>
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

    /**
     * This method will reset all column width customizations, e.g. set column
     * widths and whether columns can be resizable.
     *
     * @param fireEvent
     *            flag to indicate whether a refresh event should be triggered
     *            or not. Should be set to <code>false</code> in case additional
     *            actions should be executed before the refresh should be done.
     * @since 1.6
     */
    public void resetColumnWidthConfiguration(boolean fireEvent) {
        this.columnWidthConfig.reset();
        if (fireEvent) {
            fireLayerEvent(new ColumnStructuralRefreshEvent(this));
        }
    }

    /**
     * This method will reset all row height customizations, e.g. set row
     * heights and whether rows can be resizable.
     *
     * @param fireEvent
     *            flag to indicate whether a refresh event should be triggered
     *            or not. Should be set to <code>false</code> in case additional
     *            actions should be executed before the refresh should be done.
     * @since 1.6
     */
    public void resetRowHeightConfiguration(boolean fireEvent) {
        this.rowHeightConfig.reset();
        if (fireEvent) {
            fireLayerEvent(new RowStructuralRefreshEvent(this));
        }
    }

    /**
     * This method will reset a custom set column width to the default size.
     *
     * @param position
     *            The column position that should be reset.
     * @param fireEvent
     *            flag to indicate whether a refresh event should be triggered
     *            or not. Should be set to <code>false</code> in case additional
     *            actions should be executed before the refresh should be done.
     * @since 1.6
     */
    public void resetColumnWidth(int position, boolean fireEvent) {
        this.columnWidthConfig.resetConfiguredSize(position);
        if (fireEvent) {
            fireLayerEvent(new ColumnStructuralRefreshEvent(this));
        }
    }

    /**
     * This method will reset a custom set row height to the default size.
     *
     * @param position
     *            The row position that should be reset.
     * @param fireEvent
     *            flag to indicate whether a refresh event should be triggered
     *            or not. Should be set to <code>false</code> in case additional
     *            actions should be executed before the refresh should be done.
     * @since 1.6
     */
    public void resetRowHeight(int position, boolean fireEvent) {
        this.rowHeightConfig.resetConfiguredSize(position);
        if (fireEvent) {
            fireLayerEvent(new RowStructuralRefreshEvent(this));
        }
    }

    /**
     * This method will reset a custom set minimum column width to the default
     * minimum size.
     *
     * @param position
     *            The column position that should be reset.
     * @param fireEvent
     *            flag to indicate whether a refresh event should be triggered
     *            or not. Should be set to <code>false</code> in case additional
     *            actions should be executed before the refresh should be done.
     * @since 1.6
     */
    public void resetMinColumnWidth(int position, boolean fireEvent) {
        this.columnWidthConfig.resetConfiguredMinSize(position);
        if (fireEvent) {
            fireLayerEvent(new ColumnStructuralRefreshEvent(this));
        }
    }

    /**
     * This method will reset a custom set minimum row height to the default
     * minimum size.
     *
     * @param position
     *            The row position that should be reset.
     * @param fireEvent
     *            flag to indicate whether a refresh event should be triggered
     *            or not. Should be set to <code>false</code> in case additional
     *            actions should be executed before the refresh should be done.
     * @since 1.6
     */
    public void resetMinRowHeight(int position, boolean fireEvent) {
        this.rowHeightConfig.resetConfiguredMinSize(position);
        if (fireEvent) {
            fireLayerEvent(new RowStructuralRefreshEvent(this));
        }
    }

    /**
     * Returns the width of the given column position without any
     * transformation. That means it returns the value that was set and not an
     * upscaled value. For percentage sizing it also returns the percentage
     * value and not the calculated pixel value.
     *
     * @param columnPosition
     *            The column position for which the configured width should be
     *            returned.
     * @return The width that is configured for the given column position
     *         without transformation.
     * @see #getColumnWidthByPosition(int)
     *
     * @since 1.6
     */
    public int getConfiguredColumnWidthByPosition(int columnPosition) {
        return this.columnWidthConfig.getConfiguredSize(columnPosition);
    }

    /**
     * Returns the height of the given row position without any transformation.
     * That means it returns the value that was set and not an upscaled value.
     * For percentage sizing it also returns the percentage value and not the
     * calculated pixel value.
     *
     * @param rowPosition
     *            The row position for which the configured height should be
     *            returned.
     * @return The height that is configured for the given row position without
     *         transformation.
     * @see #getRowHeightByPosition(int)
     *
     * @since 1.6
     */
    public int getConfiguredRowHeightByPosition(int rowPosition) {
        return this.rowHeightConfig.getConfiguredSize(rowPosition);
    }

    /**
     * Returns the configured minimum width of the given column position without
     * any transformation. If no explicit minimum width is set for the column
     * position it returns -1 instead of the default minimum width.
     *
     * @param columnPosition
     *            The column position for which the configured minimum width
     *            should be returned.
     * @return The minimum width that is configured for the given column
     *         position without transformation or -1 if no explicit minimum
     *         width is set for that column position.
     * @see #getMinColumnWidth(int)
     *
     * @since 1.6
     */
    public int getConfiguredMinColumnWidthByPosition(int columnPosition) {
        return this.columnWidthConfig.getConfiguredMinSize(columnPosition);
    }

    /**
     * Returns the configured minimum height of the given row position without
     * any transformation. If no explicit minimum height is set for the row
     * position it returns -1 instead of the default minimum height.
     *
     * @param rowPosition
     *            The row position for which the configured minimum height
     *            should be returned.
     * @return The minimum height that is configured for the given row position
     *         without transformation or -1 if no explicit minimum height is set
     *         for that row position.
     * @see #getMinRowHeight(int)
     *
     * @since 1.6
     */
    public int getConfiguredMinRowHeightByPosition(int rowPosition) {
        return this.rowHeightConfig.getConfiguredMinSize(rowPosition);
    }

    /**
     *
     * @return <code>true</code> if remaining space on fixed percentage sizing
     *         is distributed to other percentage sized columns,
     *         <code>false</code> if not. Default is <code>false</code>.
     *
     * @since 1.6
     */
    public boolean isDistributeRemainingColumnSpace() {
        return this.columnWidthConfig.isDistributeRemainingSpace();
    }

    /**
     * Configure the percentage sizing behavior when manually specifying
     * percentages and not having 100% configured. By default the remaining
     * space is not distributed to the configured positions. That means for
     * example that 25% of 100 pixels will be 25, regardless of the other
     * positions. When setting this flag to <code>true</code> the 25% will be
     * increased so the whole available space is filled.
     *
     * @param distributeRemaining
     *            <code>true</code> if remaining space on fixed percentage
     *            sizing should be distributed to other percentage sized
     *            columns, <code>false</code> if not.
     *
     * @since 1.6
     */
    public void setDistributeRemainingColumnSpace(boolean distributeRemaining) {
        this.columnWidthConfig.setDistributeRemainingSpace(distributeRemaining);
    }

    /**
     *
     * @return <code>true</code> if remaining space on fixed percentage sizing
     *         is distributed to other percentage sized rows, <code>false</code>
     *         if not. Default is <code>false</code>.
     *
     * @since 1.6
     */
    public boolean isDistributeRemainingRowSpace() {
        return this.rowHeightConfig.isDistributeRemainingSpace();
    }

    /**
     * Configure the percentage sizing behavior when manually specifying
     * percentages and not having 100% configured. By default the remaining
     * space is not distributed to the configured positions. That means for
     * example that 25% of 100 pixels will be 25, regardless of the other
     * positions. When setting this flag to <code>true</code> the 25% will be
     * increased so the whole available space is filled.
     *
     * @param distributeRemaining
     *            <code>true</code> if remaining space on fixed percentage
     *            sizing should be distributed to other percentage sized rows,
     *            <code>false</code> if not.
     *
     * @since 1.6
     */
    public void setDistributeRemainingRowSpace(boolean distributeRemaining) {
        this.rowHeightConfig.setDistributeRemainingSpace(distributeRemaining);
    }

    /**
     *
     * @return The default minimum column width. Default value is 0.
     *
     * @since 1.6
     */
    public int getDefaultMinColumnWidth() {
        return this.columnWidthConfig.getDefaultMinSize();
    }

    /**
     * Set the default minimum column width. Will affect percentage sizing to
     * avoid column widths smaller than the given minimum value.
     *
     * @param defaultMinWidth
     *            The default minimum column width to use, can not be less than
     *            0.
     * @throws IllegalArgumentException
     *             if defaultMinWidth is less than 0.
     *
     * @since 1.6
     */
    public void setDefaultMinColumnWidth(int defaultMinWidth) {
        this.columnWidthConfig.setDefaultMinSize(defaultMinWidth);
    }

    /**
     * Returns the minimum column width for the given position. If no specific
     * value is configured for the given position, the default minimum size is
     * returned.
     *
     * @param position
     *            The position for which the minimum column width is requested.
     * @return The minimum column width for the given position.
     * @see #getDefaultMinColumnWidth()
     *
     * @since 1.6
     */
    public int getMinColumnWidth(int position) {
        return this.columnWidthConfig.getMinSize(position);
    }

    /**
     * Set the minimum column width for the given position. Will affect
     * percentage sizing to avoid column widths smaller than the given minimum
     * value.
     *
     * @param position
     *            The column position for which the minimum width should be set.
     * @param minWidth
     *            The minimum width for the given position.
     * @throws IllegalArgumentException
     *             if size is less than 0.
     *
     * @since 1.6
     */
    public void setMinColumnWidth(int position, int minWidth) {
        this.columnWidthConfig.setMinSize(position, minWidth);
    }

    /**
     *
     * @return <code>true</code> if the default min column width or at least one
     *         position has a min column width configured, <code>false</code> if
     *         no min column width configuration is set.
     *
     * @since 1.6
     */
    public boolean isMinColumnWidthConfigured() {
        return this.columnWidthConfig.isMinSizeConfigured();
    }

    /**
     *
     * @param position
     *            The position for which it should be checked if a minimum
     *            column width is configured.
     * @return <code>true</code> if the given column has a minimum width
     *         configured or a default minimum column width is configured,
     *         <code>false</code> if not
     *
     * @since 1.6
     */
    public boolean isMinColumnWidthConfigured(int position) {
        return this.columnWidthConfig.isMinSizeConfigured(position);
    }

    /**
     *
     * @return The default minimum row height. Default value is 0.
     *
     * @since 1.6
     */
    public int getDefaultMinRowHeight() {
        return this.rowHeightConfig.getDefaultMinSize();
    }

    /**
     * Set the default minimum row height. Will affect percentage sizing to
     * avoid row heights smaller than the given minimum value.
     *
     * @param defaultMinHeight
     *            The default minimum row height to use, can not be less than 0.
     * @throws IllegalArgumentException
     *             if defaultMinWidth is less than 0.
     *
     * @since 1.6
     */
    public void setDefaultMinRowHeight(int defaultMinHeight) {
        this.rowHeightConfig.setDefaultMinSize(defaultMinHeight);
    }

    /**
     * Returns the minimum row height for the given position. If no specific
     * value is configured for the given position, the default minimum size is
     * returned.
     *
     * @param position
     *            The position for which the minimum row height is requested.
     * @return The minimum row height for the given position.
     * @see #getDefaultMinRowHeight()
     *
     * @since 1.6
     */
    public int getMinRowHeight(int position) {
        return this.rowHeightConfig.getMinSize(position);
    }

    /**
     * Set the minimum row height for the given position. Will affect percentage
     * sizing to avoid row heights smaller than the given minimum value.
     *
     * @param position
     *            The position for which the minimum height should be set.
     * @param minHeight
     *            The minimum height for the given position.
     * @throws IllegalArgumentException
     *             if size is less than 0.
     *
     * @since 1.6
     */
    public void setMinRowHeight(int position, int minHeight) {
        this.rowHeightConfig.setMinSize(position, minHeight);
    }

    /**
     *
     * @return <code>true</code> if the default min row height or at least one
     *         position has a min row height configured, <code>false</code> if
     *         no min row height configuration is set.
     *
     * @since 1.6
     */
    public boolean isMinRowHeightConfigured() {
        return this.rowHeightConfig.isMinSizeConfigured();
    }

    /**
     *
     * @param position
     *            The position for which it should be checked if a minimum row
     *            height is configured.
     * @return <code>true</code> if the given row has a minimum height
     *         configured or a default minimum row height is configured,
     *         <code>false</code> if not
     *
     * @since 1.6
     */
    public boolean isMinRowHeightConfigured(int position) {
        return this.rowHeightConfig.isMinSizeConfigured(position);
    }

    /**
     * Calculates the column width value dependent on a possible configured
     * scaling from pixel to DPI value.
     *
     * @param value
     *            The value that should be up scaled.
     * @return The scaled value if a {@link IDpiConverter} is configured, the
     *         value itself if no {@link IDpiConverter} is set.
     *
     * @see IDpiConverter#convertPixelToDpi(int)
     *
     * @since 1.6
     */
    public int upScaleColumnWidth(int value) {
        return this.columnWidthConfig.upScale(value);
    }

    /**
     * Calculates the column width value dependent on a possible configured
     * scaling from DPI to pixel value.
     *
     * @param value
     *            The value that should be down scaled.
     * @return The scaled value if a {@link IDpiConverter} is configured, the
     *         value itself if no {@link IDpiConverter} is set.
     *
     * @since 1.6
     */
    public int downScaleColumnWidth(int value) {
        return this.columnWidthConfig.downScale(value);
    }

    /**
     * Calculates the row height value dependent on a possible configured
     * scaling from pixel to DPI value.
     *
     * @param value
     *            The value that should be up scaled.
     * @return The scaled value if a {@link IDpiConverter} is configured, the
     *         value itself if no {@link IDpiConverter} is set.
     *
     * @see IDpiConverter#convertPixelToDpi(int)
     *
     * @since 1.6
     */
    public int upScaleRowHeight(int value) {
        return this.rowHeightConfig.upScale(value);
    }

    /**
     * Calculates the row height value dependent on a possible configured
     * scaling from DPI to pixel value.
     *
     * @param value
     *            The value that should be down scaled.
     * @return The scaled value if a {@link IDpiConverter} is configured, the
     *         value itself if no {@link IDpiConverter} is set.
     *
     * @since 1.6
     */
    public int downScaleRowHeight(int value) {
        return this.rowHeightConfig.downScale(value);
    }

    /**
     * Configure whether dynamic percentage sized column positions should be
     * fixed on any resize or not. This means, if column positions are
     * configured for percentage sizing without a specific percentage value, the
     * size is calculated based on the space that is still available. If this
     * flag is set to <code>false</code> only the column position that is
     * resized will get a fixed value. The other column positions will still be
     * dynamic and therefore will also resize as the available space is changed.
     * Setting this flag to <code>true</code> will cause that all column
     * positions with dynamic percentage configuration will get a fixed
     * percentage value to have a deterministic resize behavior for the user
     * that triggers the resize. Default is <code>true</code>.
     *
     * @param enabled
     *            <code>true</code> to calculate the fix percentage value for
     *            dynamic percentage sized column positions on resize,
     *            <code>false</code> if the dynamic percentage sized column
     *            positions should stay dynamic on resize.
     *
     * @since 1.6
     */
    public void setFixDynamicColumnPercentageValues(boolean enabled) {
        this.columnWidthConfig.setFixDynamicPercentageValues(enabled);
    }

    /**
     * Configure whether dynamic percentage sized row positions should be fixed
     * on any resize or not. This means, if row positions are configured for
     * percentage sizing without a specific percentage value, the size is
     * calculated based on the space that is still available. If this flag is
     * set to <code>false</code> only the row position that is resized will get
     * a fixed value. The other row positions will still be dynamic and
     * therefore will also resize as the available space is changed. Setting
     * this flag to <code>true</code> will cause that all row positions with
     * dynamic percentage configuration will get a fixed percentage value to
     * have a deterministic resize behavior for the user that triggers the
     * resize. Default is <code>true</code>.
     *
     * @param enabled
     *            <code>true</code> to calculate the fix percentage value for
     *            dynamic percentage sized row positions on resize,
     *            <code>false</code> if the dynamic percentage sized row
     *            positions should stay dynamic on resize.
     *
     * @since 1.6
     */
    public void setFixDynamicRowPercentageValues(boolean enabled) {
        this.rowHeightConfig.setFixDynamicPercentageValues(enabled);
    }

}