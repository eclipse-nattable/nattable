/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/

package org.eclipse.nebula.widgets.nattable.layer;

import java.util.Collection;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.StructuralRefreshCommandHandler;
import org.eclipse.nebula.widgets.nattable.command.VisualRefreshCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.layer.event.ResizeStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommandHandler;
import org.eclipse.nebula.widgets.nattable.resize.command.MultiColumnResizeCommandHandler;
import org.eclipse.nebula.widgets.nattable.resize.command.MultiRowResizeCommandHandler;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommandHandler;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEvent;
import org.eclipse.nebula.widgets.nattable.resize.event.RowResizeEvent;


/**
 * Wraps the {@link IDataProvider}, and serves as the data source for all
 * other layers. Also, tracks the size of the columns and the rows using
 * {@link SizeConfig} objects. Since this layer sits directly on top of the
 * data source, at this layer index == position.
 */
public class DataLayer extends AbstractLayer implements IUniqueIndexLayer {

	public static final String PERSISTENCE_KEY_ROW_HEIGHT = ".rowHeight"; //$NON-NLS-1$
	public static final String PERSISTENCE_KEY_COLUMN_WIDTH = ".columnWidth"; //$NON-NLS-1$

	public static final int DEFAULT_COLUMN_WIDTH = 100;
	public static final int DEFAULT_ROW_HEIGHT = 20;

	protected IDataProvider dataProvider;

	private final SizeConfig columnWidthConfig;
	private final SizeConfig rowHeightConfig;
	
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
		columnWidthConfig = new SizeConfig(defaultColumnWidth);
		rowHeightConfig = new SizeConfig(defaultRowHeight);

		registerCommandHandlers();
	}

	// Persistence

	@Override
	public void saveState(String prefix, Properties properties) {
		super.saveState(prefix, properties);
		columnWidthConfig.saveState(prefix + PERSISTENCE_KEY_COLUMN_WIDTH, properties);
		rowHeightConfig.saveState(prefix + PERSISTENCE_KEY_ROW_HEIGHT, properties);
	}

	@Override
	public void loadState(String prefix, Properties properties) {
		super.loadState(prefix, properties);
		columnWidthConfig.loadState(prefix + PERSISTENCE_KEY_COLUMN_WIDTH, properties);
		rowHeightConfig.loadState(prefix + PERSISTENCE_KEY_ROW_HEIGHT, properties);
		
		if (!properties.containsKey(NatTable.INITIAL_PAINT_COMPLETE_FLAG))
			fireLayerEvent(new StructuralRefreshEvent(this));
	}

	// Configuration

	@Override
	protected void registerCommandHandlers() {
		registerCommandHandler(new ColumnResizeCommandHandler(this));
		registerCommandHandler(new MultiColumnResizeCommandHandler(this));
		registerCommandHandler(new RowResizeCommandHandler(this));
		registerCommandHandler(new MultiRowResizeCommandHandler(this));
		registerCommandHandler(new UpdateDataCommandHandler(this));
		registerCommandHandler(new StructuralRefreshCommandHandler());
		registerCommandHandler(new VisualRefreshCommandHandler());
	}

	public IDataProvider getDataProvider() {
		return dataProvider;
	}

	protected void setDataProvider(IDataProvider dataProvider) {
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
	 * @param rowIndex
	 * @return the data value associated with the specified cell
	 */
	public Object getDataValue(int columnIndex, int rowIndex) {
		return this.dataProvider.getDataValue(columnIndex, rowIndex);
	}

	/**
	 * Sets the value at the given column and row index. Optional operation. Should throw UnsupportedOperationException
	 * if this operation is not supported.
	 *
	 * @param columnIndex
	 * @param rowIndex
	 * @param newValue
	 */
	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		this.dataProvider.setDataValue(columnIndex, rowIndex, newValue);
	}
	
	// Horizontal features

	// Columns

	@Override
	public int getColumnCount() {
		return dataProvider.getColumnCount();
	}

	@Override
	public int getPreferredColumnCount() {
		return getColumnCount();
	}

    /**
	 * This is the root coordinate system, so the column index is always equal to the column position.
	 */
	@Override
	public int getColumnIndexByPosition(int columnPosition) {
		if (columnPosition >=0 && columnPosition < getColumnCount()) {
			return columnPosition;
		} else {
			return -1;
		}
	}

	/**
	 * This is the root coordinate system, so the column position is always equal to the column index.
	 */
	@Override
	public int getColumnPositionByIndex(int columnIndex) {
		if (columnIndex >=0 && columnIndex < getColumnCount()) {
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
	public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer, int underlyingColumnPosition) {
		return underlyingColumnPosition;
	}
	
	@Override
	public Collection<Range> underlyingToLocalColumnPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingColumnPositionRanges) {
		return underlyingColumnPositionRanges;
	}

	// Width

    @Override
	public int getWidth() {
		return columnWidthConfig.getAggregateSize(getColumnCount());
	}

	@Override
	public int getPreferredWidth() {
		return getWidth();
	}

	@Override
	public int getColumnWidthByPosition(int columnPosition) {
        return columnWidthConfig.getSize(columnPosition);
    }

	public void setColumnWidthByPosition(int columnPosition, int width) {
		columnWidthConfig.setSize(columnPosition, width);
		fireLayerEvent(new ColumnResizeEvent(this, columnPosition));
	}
	
	public void setColumnWidthPercentageByPosition(int columnPosition, int width) {
		columnWidthConfig.setPercentage(columnPosition, width);
		fireLayerEvent(new ColumnResizeEvent(this, columnPosition));
	}

	public void setDefaultColumnWidth(int width) {
		columnWidthConfig.setDefaultSize(width);
	}

	public void setDefaultColumnWidthByPosition(int columnPosition, int width) {
		columnWidthConfig.setDefaultSize(columnPosition, width);
	}

	// Column resize

	@Override
	public boolean isColumnPositionResizable(int columnPosition) {
		return columnWidthConfig.isPositionResizable(columnPosition);
	}

	public void setColumnPositionResizable(int columnPosition, boolean resizable) {
		columnWidthConfig.setPositionResizable(columnPosition, resizable);
	}

	public void setColumnsResizableByDefault(boolean resizableByDefault) {
		columnWidthConfig.setResizableByDefault(resizableByDefault);
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
		return dataProvider.getRowCount();
	}

	@Override
	public int getPreferredRowCount() {
		return getRowCount();
	}

	/**
	 * This is the root coordinate system, so the row index is always equal to the row position.
	 */
	@Override
	public int getRowIndexByPosition(int rowPosition) {
		if (rowPosition >=0 && rowPosition < getRowCount()) {
			return rowPosition;
		} else {
			return -1;
		}
	}

	/**
	 * This is the root coordinate system, so the row position is always equal to the row index.
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
	public int underlyingToLocalRowPosition(ILayer sourceUnderlyingLayer, int underlyingRowPosition) {
		return underlyingRowPosition;
	}
	
	@Override
	public Collection<Range> underlyingToLocalRowPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingRowPositionRanges) {
		return underlyingRowPositionRanges;
	}

	// Height

    @Override
	public int getHeight() {
		return rowHeightConfig.getAggregateSize(getRowCount());
	}

	@Override
	public int getPreferredHeight() {
		return getHeight();
	}

	@Override
	public int getRowHeightByPosition(int rowPosition) {
		return rowHeightConfig.getSize(rowPosition);
	}

	public void setRowHeightByPosition(int rowPosition, int height) {
		rowHeightConfig.setSize(rowPosition, height);
		fireLayerEvent(new RowResizeEvent(this, rowPosition));
	}
	
	public void setRowHeightPercentageByPosition(int rowPosition, int height) {
		rowHeightConfig.setPercentage(rowPosition, height);
		fireLayerEvent(new ColumnResizeEvent(this, rowPosition));
	}

	public void setDefaultRowHeight(int height) {
		rowHeightConfig.setDefaultSize(height);
	}

	public void setDefaultRowHeightByPosition(int rowPosition, int height) {
		rowHeightConfig.setDefaultSize(rowPosition, height);
	}

	// Row resize

	@Override
	public boolean isRowPositionResizable(int rowPosition) {
		return rowHeightConfig.isPositionResizable(rowPosition);
	}

	public void setRowPositionResizable(int rowPosition, boolean resizable) {
		rowHeightConfig.setPositionResizable(rowPosition, resizable);
	}

	public void setRowsResizableByDefault(boolean resizableByDefault) {
		rowHeightConfig.setResizableByDefault(resizableByDefault);
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
		return dataProvider.getDataValue(columnIndex, rowIndex);
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
		return columnWidthConfig.getAggregateSize(columnPosition);
	}

	@Override
	public int getStartYOfRowPosition(int rowPosition) {
		return rowHeightConfig.getAggregateSize(rowPosition);
	}

	@Override
	public ILayer getUnderlyingLayerByPosition(int columnPosition, int rowPosition) {
		return null;
	}
	
	@Override
	public boolean doCommand(ILayerCommand command) {
		if (command instanceof ClientAreaResizeCommand && command.convertToTargetLayer(this)) {
			ClientAreaResizeCommand clientAreaResizeCommand = (ClientAreaResizeCommand) command;
			
			boolean refresh = false;
			if (isColumnPercentageSizing()) {
				this.columnWidthConfig.calculatePercentages(clientAreaResizeCommand.getCalcArea().width, getColumnCount());
				refresh = true;
			}
			if (isRowPercentageSizing()) {
				this.rowHeightConfig.calculatePercentages(clientAreaResizeCommand.getCalcArea().height, getRowCount());
				refresh = true;
			}
			
			if (refresh) {
				fireLayerEvent(new ResizeStructuralRefreshEvent(this));
			}

			return refresh;
		}
		return super.doCommand(command);
	}
	
	/**
	 * @return <code>true</code> if the column sizing is done by percentage calculation,
	 * 			<code>false</code> if the column sizing is done by pixel (default)
	 */
	public boolean isColumnPercentageSizing() {
		return this.columnWidthConfig.isPercentageSizing();
	}
	
	/**
	 * Configures how the column sizing of this {@link DataLayer} is handled.
	 * Default is pixel sizing.
	 * If percentage sizing should be used you have to ensure that the size value for every 
	 * column is set explicitly and that the sum of the column sizes doesn't exceed 100.
	 * @param percentageSizing <code>true</code> if the column sizing should be done by percentage 
	 * 			calculation, <code>false</code> if the column sizing should be done by pixel (default)
	 */
	public void setColumnPercentageSizing(boolean percentageSizing) {
		this.columnWidthConfig.setPercentageSizing(percentageSizing);
	}
	
	/**
	 * @param position The position which is asked for the percentage sizing configuration.
	 * @return <code>true</code> if the column sizing for the given position is done by percentage 
	 * 			calculation, <code>false</code> if the column sizing is done by pixel (default)
	 */
	public boolean isColumnPercentageSizing(int position) {
		return this.columnWidthConfig.isPercentageSizing(position);
	}
	
	/**
	 * Configures how the column sizing of this {@link DataLayer} is handled.
	 * Default is pixel sizing.
	 * If percentage sizing should be used you have to ensure that the size value for every 
	 * column is set explicitly and that the sum of the column sizes doesn't exceed 100.
	 * @param position The position for which the sizing configuration should be set.
	 * @param percentageSizing <code>true</code> if the column sizing should be done by percentage 
	 * 			calculation, <code>false</code> if the column sizing should be done by pixel (default)
	 */
	public void setColumnPercentageSizing(int position, boolean percentageSizing) {
		this.columnWidthConfig.setPercentageSizing(position, percentageSizing);
	}
	
	/**
	 * @return <code>true</code> if the row sizing is done by percentage calculation, 
	 * 			<code>false</code> if the row sizing is done by pixel (default)
	 */
	public boolean isRowPercentageSizing() {
		return this.rowHeightConfig.isPercentageSizing();
	}
	
	/**
	 * Configures how the row sizing of this {@link DataLayer} is handled.
	 * Default is pixel sizing.
	 * If percentage sizing should be used you have to ensure that the size value for every 
	 * row is set explicitly and that the sum of the row sizes doesn't exceed 100.
	 * @param percentageSizing <code>true</code> if the row sizing should be done by percentage 
	 * 			calculation, <code>false</code> if the row sizing should be done by pixel (default)
	 */
	public void setRowPercentageSizing(boolean percentageSizing) {
		this.rowHeightConfig.setPercentageSizing(percentageSizing);
	}
	
	/**
	 * @param position The position which is asked for the percentage sizing configuration.
	 * @return <code>true</code> if the row sizing for the given position is done by percentage 
	 * 			calculation, <code>false</code> if the row sizing is done by pixel (default)
	 */
	public boolean isRowPercentageSizing(int position) {
		return this.rowHeightConfig.isPercentageSizing(position);
	}
	
	/**
	 * Configures how the row sizing of this {@link DataLayer} is handled.
	 * Default is pixel sizing.
	 * If percentage sizing should be used you have to ensure that the size value for every 
	 * row is set explicitly and that the sum of the row sizes doesn't exceed 100.
	 * @param position The row position for which the sizing configuration should be set.
	 * @param percentageSizing <code>true</code> if the row sizing should be done by percentage 
	 * 			calculation, <code>false</code> if the row sizing should be done by pixel (default)
	 */
	public void setRowPercentageSizing(int position, boolean percentageSizing) {
		this.rowHeightConfig.setPercentageSizing(position, percentageSizing);
	}
}