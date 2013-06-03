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
package org.eclipse.nebula.widgets.nattable.test.fixture;

import java.util.Collection;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.TransformedLayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Ignore;

@Ignore
public class TestLayer implements IUniqueIndexLayer {

	private final int columnCount;
	private final int preferredColumnCount;
	private final int preferredWidth;
	private final int rowCount;
	private final int preferredRowCount;
	private final int preferredHeight;

	private final int[] columnIndexes;
	private final int[] columnWidths;
	private final int[] underlyingColumnPositions;

	private final int[] rowIndexes;
	private final int[] rowHeights;
	private final int[] underlyingRowPositions;

	private final ILayerCell[][] cells;
	private final Rectangle[][] bounds;
	private final String[][] displayModes;
	private final String[][] configLabels;
	private final Object[][] dataValues;

	public TestLayer(int columnCount, int rowCount, String columnsInfo, String rowsInfo, String cellsInfo) {
		this(columnCount, columnCount, -1, rowCount, rowCount, -1, columnsInfo, rowsInfo, cellsInfo);
	}

	public TestLayer(int columnCount, int preferredColumnCount, int preferredWidth, int rowCount, int preferredRowCount, int preferredHeight, String columnsInfo, String rowsInfo, String cellsInfo) {
		this.columnCount = columnCount;
		this.preferredColumnCount = preferredColumnCount;
		this.preferredWidth = preferredWidth;
		this.rowCount = rowCount;
		this.preferredRowCount = preferredRowCount;
		this.preferredHeight = preferredHeight;

		columnIndexes = new int[columnCount];
		columnWidths = new int[columnCount];
		underlyingColumnPositions = new int[columnCount];

		rowIndexes = new int[rowCount];
		rowHeights = new int[rowCount];
		underlyingRowPositions = new int[rowCount];

		cells = new ILayerCell[columnCount][rowCount];
		bounds = new Rectangle[columnCount][rowCount];
		displayModes = new String[columnCount][rowCount];
		configLabels = new String[columnCount][rowCount];
		dataValues = new Object[columnCount][rowCount];

		parseColumnsInfo(columnsInfo);
		parseRowsInfo(rowsInfo);
		parseCellsInfo(cellsInfo);
	}

	private void parseColumnsInfo(String columnsInfo) {
		int columnPosition = 0;
		StringTokenizer columnInfoTokenizer = new StringTokenizer(columnsInfo, "|");
		while (columnInfoTokenizer.hasMoreTokens()) {
			String columnInfo = columnInfoTokenizer.nextToken();

			StringTokenizer columnInfoFieldTokenizer = new StringTokenizer(columnInfo, ":;", true);
			while (columnInfoFieldTokenizer.hasMoreTokens()) {
				String token = columnInfoFieldTokenizer.nextToken().trim();

				if (":".equals(token)) {
					String nextToken = columnInfoFieldTokenizer.nextToken().trim();

					if (":".equals(nextToken)) {
						throw new IllegalArgumentException("Bad " + nextToken + " delimiter found when parsing underlying column position for column position " + columnPosition);
					} else if (";".equals(nextToken)) {
						// Parse column width
						parseColumnWidth(columnPosition, columnInfoFieldTokenizer, columnInfoFieldTokenizer.nextToken().trim());
						break;
					} else {
						// Parse underlying column position
						int underlyingColumnPosition = Integer.valueOf(nextToken).intValue();
						underlyingColumnPositions[columnPosition] = underlyingColumnPosition;
					}
				} else if (";".equals(token)) {
					String nextToken = columnInfoFieldTokenizer.nextToken().trim();

					if (":".equals(nextToken)) {
						throw new IllegalArgumentException("Bad " + nextToken + " delimiter found when parsing column width for column position " + columnPosition);
					} else if (";".equals(nextToken)) {
						throw new IllegalArgumentException("Bad " + nextToken + " delimiter found when parsing column width for column position " + columnPosition);
					} else {
						// Parse column width
						parseColumnWidth(columnPosition, columnInfoFieldTokenizer, nextToken);
						break;
					}
				} else {
					// Parse column index
					int columnIndex = Integer.valueOf(token).intValue();
					columnIndexes[columnPosition] = columnIndex;
				}
			}

			columnPosition++;
		}
	}

	private void parseColumnWidth(int columnPosition, StringTokenizer columnInfoFieldTokenizer, String nextToken) {
		int columnWidth = Integer.valueOf(nextToken).intValue();
		columnWidths[columnPosition] = columnWidth;

		if (columnInfoFieldTokenizer.hasMoreTokens()) {
			System.out.println("Extra tokens detected after parsing column width for column position " + columnPosition + "; ignoring");
		}
	}

	private void parseRowsInfo(String rowsInfo) {
		int rowPosition = 0;
		StringTokenizer rowInfoTokenizer = new StringTokenizer(rowsInfo, "|");
		while (rowInfoTokenizer.hasMoreTokens()) {
			String rowInfo = rowInfoTokenizer.nextToken();

			StringTokenizer rowInfoFieldTokenizer = new StringTokenizer(rowInfo, ":;", true);
			while (rowInfoFieldTokenizer.hasMoreTokens()) {
				String token = rowInfoFieldTokenizer.nextToken().trim();

				if (":".equals(token)) {
					String nextToken = rowInfoFieldTokenizer.nextToken().trim();

					if (":".equals(nextToken)) {
						throw new IllegalArgumentException("Bad " + nextToken + " delimiter found when parsing underlying row position for row position " + rowPosition);
					} else if (";".equals(nextToken)) {
						// Parse row height
						parseRowHeight(rowPosition, rowInfoFieldTokenizer, rowInfoFieldTokenizer.nextToken().trim());
						break;
					} else {
						// Parse underlying row position
						int underlyingRowPosition = Integer.valueOf(nextToken).intValue();
						underlyingRowPositions[rowPosition] = underlyingRowPosition;
					}
				} else if (";".equals(token)) {
					String nextToken = rowInfoFieldTokenizer.nextToken().trim();

					if (":".equals(nextToken)) {
						throw new IllegalArgumentException("Bad " + nextToken + " delimiter found when parsing row height for row position " + rowPosition);
					} else if (";".equals(nextToken)) {
						throw new IllegalArgumentException("Bad " + nextToken + " delimiter found when parsing row height for row position " + rowPosition);
					} else {
						// Parse row height
						parseRowHeight(rowPosition, rowInfoFieldTokenizer, nextToken);
						break;
					}
				} else {
					// Parse row index
					int rowIndex = Integer.valueOf(token).intValue();
					rowIndexes[rowPosition] = rowIndex;
				}
			}

			rowPosition++;
		}
	}

	private void parseRowHeight(int rowPosition, StringTokenizer rowInfoFieldTokenizer, String nextToken) {
		int rowHeight = Integer.valueOf(nextToken).intValue();
		rowHeights[rowPosition] = rowHeight;

		if (rowInfoFieldTokenizer.hasMoreTokens()) {
			System.out.println("Extra tokens detected after parsing row height for column position " + rowPosition + "; ignoring");
		}
	}

	public void parseCellsInfo(String cellsInfo) {
		int rowPosition = 0;
		StringTokenizer rowOfCellInfoTokenizer = new StringTokenizer(cellsInfo, "\n");
		while (rowOfCellInfoTokenizer.hasMoreTokens()) {
			String rowOfCellInfo = rowOfCellInfoTokenizer.nextToken().trim();

			int columnPosition = 0;
			StringTokenizer cellInfoTokenizer = new StringTokenizer(rowOfCellInfo, "|");
			while (cellInfoTokenizer.hasMoreTokens()) {
				String cellInfo = cellInfoTokenizer.nextToken().trim();

				StringTokenizer cellInfoFieldTokenizer = new StringTokenizer(cellInfo, "~:", true);
				while (cellInfoFieldTokenizer.hasMoreTokens()) {
					String token = cellInfoFieldTokenizer.nextToken().trim();

					if ("<".equals(token)) {
						// Span from left
						dataValues[columnPosition][rowPosition] = dataValues[columnPosition - 1][rowPosition];

						ILayerCell cell = cells[columnPosition - 1][rowPosition];
						Rectangle boundsRect = bounds[columnPosition - 1][rowPosition];

						if (columnPosition >= cell.getColumnPosition() + cell.getColumnSpan()) {
							boundsRect = new Rectangle(boundsRect.x, boundsRect.y, boundsRect.width + getColumnWidthByPosition(columnPosition), boundsRect.height);
							
							final ILayerCell underlyingCell = cell;
							cell = new TransformedLayerCell(cell) {
								@Override
								public int getColumnSpan() {
									return underlyingCell.getColumnSpan() + 1;
								}
							};
						}

						cells[columnPosition][rowPosition] = cell;
						bounds[columnPosition][rowPosition] = boundsRect;

						if (cellInfoFieldTokenizer.hasMoreTokens()) {
							System.out.println("Extra tokens detected after parsing span for cell position " + columnPosition + "," + rowPosition + "; ignoring");
						}
						break;
					} else if ("^".equals(token)) {
						// Span from above
						dataValues[columnPosition][rowPosition] = dataValues[columnPosition][rowPosition - 1];

						ILayerCell cell = cells[columnPosition][rowPosition - 1];
						Rectangle boundsRect = bounds[columnPosition][rowPosition - 1];

						if (rowPosition >= cell.getRowPosition() + cell.getRowSpan()) {
							boundsRect = new Rectangle(boundsRect.x, boundsRect.y, boundsRect.width, boundsRect.height + getRowHeightByPosition(rowPosition));
							
							final ILayerCell underlyingCell = cell;
							cell = new TransformedLayerCell(cell) {
								@Override
								public int getRowSpan() {
									return underlyingCell.getRowSpan() + 1;
								}
							};
						}

						cells[columnPosition][rowPosition] = cell;
						bounds[columnPosition][rowPosition] = boundsRect;

						if (cellInfoFieldTokenizer.hasMoreTokens()) {
							System.out.println("Extra tokens detected after parsing span for cell position " + columnPosition + "," + rowPosition + "; ignoring");
						}
						break;
					} else if ("~".equals(token)) {
						String nextToken = cellInfoFieldTokenizer.nextToken().trim();

						if ("~".equals(nextToken)) {
							throw new IllegalArgumentException("Bad " + nextToken + " delimiter found when parsing display mode for cell position " + columnPosition + "," + rowPosition);
						} else if (":".equals(nextToken)) {
							// Parse config labels
							parseConfigLabels(columnPosition, rowPosition, cellInfoFieldTokenizer, cellInfoFieldTokenizer.nextToken().trim());
							break;
						} else {
							// Parse display mode
							displayModes[columnPosition][rowPosition] = nextToken;
						}
					} else if (":".equals(token)) {
						String nextToken = cellInfoFieldTokenizer.nextToken().trim();

						if ("~".equals(nextToken)) {
							throw new IllegalArgumentException("Bad " + nextToken + " delimiter found when parsing config labels for cell position " + columnPosition + "," + rowPosition);
						} else if (":".equals(nextToken)) {
							throw new IllegalArgumentException("Bad " + nextToken + " delimiter found when parsing config labels for cell position " + columnPosition + "," + rowPosition);
						} else {
							// Parse config labels
							parseConfigLabels(columnPosition, rowPosition, cellInfoFieldTokenizer, nextToken);
							break;
						}
					} else {
						// Parse data value
						dataValues[columnPosition][rowPosition] = token;
						cells[columnPosition][rowPosition] = new TestLayerCell(this, columnPosition, rowPosition);
						bounds[columnPosition][rowPosition] = new Rectangle(getStartXOfColumnPosition(columnPosition), getStartYOfRowPosition(rowPosition), getColumnWidthByPosition(columnPosition), getRowHeightByPosition(rowPosition));
					}
				}

				columnPosition++;
			}

			rowPosition++;
		}
	}

	private void parseConfigLabels(int columnPosition, int rowPosition, StringTokenizer cellInfoFieldTokenizer, String nextToken) {
		configLabels[columnPosition][rowPosition] = nextToken;

		if (cellInfoFieldTokenizer.hasMoreTokens()) {
			System.out.println("Extra tokens detected after parsing config labels for cell position " + columnPosition + "," + rowPosition + "; ignoring");
		}
	}

	public void dispose() {
		// Do nothing
	}
	
	public void registerPersistable(IPersistable persistable) {
		// Do nothing
	}

	public void unregisterPersistable(IPersistable persistable) {
		// Do nothing
	}

	public void saveState(String prefix, Properties properties) {
		// Do nothing
	}

	public void loadState(String prefix, Properties properties) {
		// Do nothing
	}

	public void configure(ConfigRegistry configRegistry, UiBindingRegistry uiBindingRegistry) {
		// Do nothing
	}

	public boolean doCommand(ILayerCommand command) {
		// Do nothing
		return false;
	}

	public void registerCommandHandler(ILayerCommandHandler<?> commandHandler) {
		// Do nothing
	}

	public void unregisterCommandHandler(Class<? extends ILayerCommand> commandClass) {
		// Do nothing
	}

	public void handleLayerEvent(ILayerEvent event) {
		// Do nothing
	}

	public void addLayerListener(ILayerListener listener) {
		// Do nothing
	}

	public void removeLayerListener(ILayerListener listener) {
		// Do nothing
	}

	public boolean hasLayerListener(Class<? extends ILayerListener> layerListenerClass) {
		return false;
	}

	public ILayerPainter getLayerPainter() {
		return null;
	}

	public IClientAreaProvider getClientAreaProvider() {
		return null;
	}

	public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
		// Do nothing
	}

	public LabelStack getRegionLabelsByXY(int x, int y) {
		return null;
	}

	// Horizontal features

	// Columns

	public int getColumnCount() {
		return columnCount;
	}

	public int getPreferredColumnCount() {
		return preferredColumnCount;
	}

	public int getColumnIndexByPosition(int columnPosition) {
		return columnIndexes[columnPosition];
	}

	public int localToUnderlyingColumnPosition(int localColumnPosition) {
		return underlyingColumnPositions[localColumnPosition];
	}

	public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer, int underlyingColumnPosition) {
		for (int localColumnPosition = 0; localColumnPosition < underlyingColumnPositions.length; localColumnPosition++) {
			if (underlyingColumnPositions[localColumnPosition] == underlyingColumnPosition) {
				return localColumnPosition;
			}
		}
		return -1;
	}

	public Collection<Range> underlyingToLocalColumnPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingColumnPositionRanges) {
		throw new RuntimeException("Not yet implemented");
	}
	
	public int getColumnPositionByIndex(int columnIndex) {
		for (int columnPosition = 0; columnPosition < columnIndexes.length; columnPosition++) {
			if (columnIndexes[columnPosition] == columnIndex) {
				return columnPosition;
			}
		}
		return -1;
	}

	// Width

	public int getWidth() {
		int width = 0;
		for (int columnPosition = 0; columnPosition < getColumnCount(); columnPosition++) {
			width += columnWidths[columnPosition];
		}
		return width;
	}

	public int getPreferredWidth() {
		if (preferredWidth >= 0) {
			return preferredWidth;
		} else {
			return getWidth();
		}
	}

	public int getColumnWidthByPosition(int columnPosition) {
		return columnWidths[columnPosition];
	}

	// Column resize

	public boolean isColumnPositionResizable(int columnPosition) {
		return true;
	}

	// X

	public int getColumnPositionByX(int x) {
		int width = 0;
		for (int columnPosition = 0; columnPosition < getColumnCount(); columnPosition++) {
			width += columnWidths[columnPosition];
			if (width > x) {
				return columnPosition;
			}
		}
		return -1;
	}

	public int getStartXOfColumnPosition(int targetColumnPosition) {
		int width = 0;
		for (int columnPosition = 0; columnPosition < targetColumnPosition; columnPosition++) {
			width += columnWidths[columnPosition];
		}
		return width;
	}

	// Underlying

	public Collection<ILayer> getUnderlyingLayersByColumnPosition(int columnPosition) {
		return null;
	}

	// Vertical features

	// Rows

	public int getRowCount() {
		return rowCount;
	}

	public int getPreferredRowCount() {
		return preferredRowCount;
	}

	public int getRowIndexByPosition(int rowPosition) {
		return rowIndexes[rowPosition];
	}

	public int localToUnderlyingRowPosition(int localRowPosition) {
		return underlyingRowPositions[localRowPosition];
	}

	public int underlyingToLocalRowPosition(ILayer sourceUnderlyingLayer, int underlyingRowPosition) {
		for (int localRowPosition = 0; localRowPosition < underlyingRowPositions.length; localRowPosition++) {
			if (underlyingRowPositions[localRowPosition] == underlyingRowPosition) {
				return localRowPosition;
			}
		}
		return -1;
	}

	public Collection<Range> underlyingToLocalRowPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingRowPositionRanges) {
		throw new RuntimeException("Not yet implemented");
	}
	
	public int getRowPositionByIndex(int rowIndex) {
		for (int rowPosition = 0; rowPosition < rowIndexes.length; rowPosition++) {
			if (rowIndexes[rowPosition] == rowIndex) {
				return rowPosition;
			}
		}
		return -1;
	}

	// Height

	public int getHeight() {
		int height = 0;
		for (int rowPosition = 0; rowPosition < getRowCount(); rowPosition++) {
			height += rowHeights[rowPosition];
		}
		return height;
	}

	public int getPreferredHeight() {
		if (preferredHeight >= 0) {
			return preferredHeight;
		} else {
			return getHeight();
		}
	}

	public int getRowHeightByPosition(int rowPosition) {
		return rowHeights[rowPosition];
	}

	// Row resize

	public boolean isRowPositionResizable(int rowPosition) {
		return true;
	}

	// Y

	public int getRowPositionByY(int y) {
		int height = 0;
		for (int rowPosition = 0; rowPosition < getRowCount(); rowPosition++) {
			height += rowHeights[rowPosition];
			if (height > y) {
				return rowPosition;
			}
		}
		return -1;
	}

	public int getStartYOfRowPosition(int targetRowPosition) {
		int height= 0;
		for (int rowPosition = 0; rowPosition < targetRowPosition; rowPosition++) {
			height += rowHeights[rowPosition];
		}
		return height;
	}

	// Underlying

	public Collection<ILayer> getUnderlyingLayersByRowPosition(int rowPosition) {
		return null;
	}

	// Cell features

	public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
		return new TestLayerCell(cells[columnPosition][rowPosition]);
	}

	public Rectangle getBoundsByPosition(int columnPosition, int rowPosition) {
		return bounds[columnPosition][rowPosition];
	}

	public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
		return displayModes[columnPosition][rowPosition];
	}

	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		LabelStack labelStack = new LabelStack();
		String configLabelsString = configLabels[columnPosition][rowPosition];
		if (configLabelsString != null) {
			StringTokenizer configLabelTokenizer = new StringTokenizer(configLabelsString, ",");
			while (configLabelTokenizer.hasMoreTokens()) {
				labelStack.addLabel(configLabelTokenizer.nextToken());
			}
		}
		return labelStack;
	}

	public Object getDataValueByPosition(int columnPosition, int rowPosition) {
		return dataValues[columnPosition][rowPosition];
	}

	public ILayer getUnderlyingLayerByPosition(int columnPosition, int rowPosition) {
		return null;
	}

	public void fireLayerEvent(ILayerEvent event) {
		// TODO Auto-generated method stub

	}

	public ICellPainter getCellPainter(int columnPosition, int rowPosition, ILayerCell cell, IConfigRegistry configRegistry) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
