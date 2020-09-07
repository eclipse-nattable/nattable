/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

    public TestLayer(int columnCount, int rowCount, String columnsInfo,
            String rowsInfo, String cellsInfo) {
        this(columnCount, columnCount, -1, rowCount, rowCount, -1, columnsInfo,
                rowsInfo, cellsInfo);
    }

    public TestLayer(int columnCount, int preferredColumnCount,
            int preferredWidth, int rowCount, int preferredRowCount,
            int preferredHeight, String columnsInfo, String rowsInfo,
            String cellsInfo) {
        this.columnCount = columnCount;
        this.preferredColumnCount = preferredColumnCount;
        this.preferredWidth = preferredWidth;
        this.rowCount = rowCount;
        this.preferredRowCount = preferredRowCount;
        this.preferredHeight = preferredHeight;

        this.columnIndexes = new int[columnCount];
        this.columnWidths = new int[columnCount];
        this.underlyingColumnPositions = new int[columnCount];

        this.rowIndexes = new int[rowCount];
        this.rowHeights = new int[rowCount];
        this.underlyingRowPositions = new int[rowCount];

        this.cells = new ILayerCell[columnCount][rowCount];
        this.bounds = new Rectangle[columnCount][rowCount];
        this.displayModes = new String[columnCount][rowCount];
        this.configLabels = new String[columnCount][rowCount];
        this.dataValues = new Object[columnCount][rowCount];

        parseColumnsInfo(columnsInfo);
        parseRowsInfo(rowsInfo);
        parseCellsInfo(cellsInfo);
    }

    private void parseColumnsInfo(String columnsInfo) {
        int columnPosition = 0;
        StringTokenizer columnInfoTokenizer = new StringTokenizer(columnsInfo,
                "|");
        while (columnInfoTokenizer.hasMoreTokens()) {
            String columnInfo = columnInfoTokenizer.nextToken();

            StringTokenizer columnInfoFieldTokenizer = new StringTokenizer(
                    columnInfo, ":;", true);
            while (columnInfoFieldTokenizer.hasMoreTokens()) {
                String token = columnInfoFieldTokenizer.nextToken().trim();

                if (":".equals(token)) {
                    String nextToken = columnInfoFieldTokenizer.nextToken()
                            .trim();

                    if (":".equals(nextToken)) {
                        throw new IllegalArgumentException(
                                "Bad "
                                        + nextToken
                                        + " delimiter found when parsing underlying column position for column position "
                                        + columnPosition);
                    } else if (";".equals(nextToken)) {
                        // Parse column width
                        parseColumnWidth(columnPosition,
                                columnInfoFieldTokenizer,
                                columnInfoFieldTokenizer.nextToken().trim());
                        break;
                    } else {
                        // Parse underlying column position
                        int underlyingColumnPosition = Integer.valueOf(
                                nextToken).intValue();
                        this.underlyingColumnPositions[columnPosition] = underlyingColumnPosition;
                    }
                } else if (";".equals(token)) {
                    String nextToken = columnInfoFieldTokenizer.nextToken()
                            .trim();

                    if (":".equals(nextToken)) {
                        throw new IllegalArgumentException(
                                "Bad "
                                        + nextToken
                                        + " delimiter found when parsing column width for column position "
                                        + columnPosition);
                    } else if (";".equals(nextToken)) {
                        throw new IllegalArgumentException(
                                "Bad "
                                        + nextToken
                                        + " delimiter found when parsing column width for column position "
                                        + columnPosition);
                    } else {
                        // Parse column width
                        parseColumnWidth(columnPosition,
                                columnInfoFieldTokenizer, nextToken);
                        break;
                    }
                } else {
                    // Parse column index
                    int columnIndex = Integer.valueOf(token).intValue();
                    this.columnIndexes[columnPosition] = columnIndex;
                }
            }

            columnPosition++;
        }
    }

    private void parseColumnWidth(int columnPosition,
            StringTokenizer columnInfoFieldTokenizer, String nextToken) {
        int columnWidth = Integer.valueOf(nextToken).intValue();
        this.columnWidths[columnPosition] = columnWidth;

        if (columnInfoFieldTokenizer.hasMoreTokens()) {
            System.out
                    .println("Extra tokens detected after parsing column width for column position "
                            + columnPosition + "; ignoring");
        }
    }

    private void parseRowsInfo(String rowsInfo) {
        int rowPosition = 0;
        StringTokenizer rowInfoTokenizer = new StringTokenizer(rowsInfo, "|");
        while (rowInfoTokenizer.hasMoreTokens()) {
            String rowInfo = rowInfoTokenizer.nextToken();

            StringTokenizer rowInfoFieldTokenizer = new StringTokenizer(
                    rowInfo, ":;", true);
            while (rowInfoFieldTokenizer.hasMoreTokens()) {
                String token = rowInfoFieldTokenizer.nextToken().trim();

                if (":".equals(token)) {
                    String nextToken = rowInfoFieldTokenizer.nextToken().trim();

                    if (":".equals(nextToken)) {
                        throw new IllegalArgumentException(
                                "Bad "
                                        + nextToken
                                        + " delimiter found when parsing underlying row position for row position "
                                        + rowPosition);
                    } else if (";".equals(nextToken)) {
                        // Parse row height
                        parseRowHeight(rowPosition, rowInfoFieldTokenizer,
                                rowInfoFieldTokenizer.nextToken().trim());
                        break;
                    } else {
                        // Parse underlying row position
                        int underlyingRowPosition = Integer.valueOf(nextToken)
                                .intValue();
                        this.underlyingRowPositions[rowPosition] = underlyingRowPosition;
                    }
                } else if (";".equals(token)) {
                    String nextToken = rowInfoFieldTokenizer.nextToken().trim();

                    if (":".equals(nextToken)) {
                        throw new IllegalArgumentException(
                                "Bad "
                                        + nextToken
                                        + " delimiter found when parsing row height for row position "
                                        + rowPosition);
                    } else if (";".equals(nextToken)) {
                        throw new IllegalArgumentException(
                                "Bad "
                                        + nextToken
                                        + " delimiter found when parsing row height for row position "
                                        + rowPosition);
                    } else {
                        // Parse row height
                        parseRowHeight(rowPosition, rowInfoFieldTokenizer,
                                nextToken);
                        break;
                    }
                } else {
                    // Parse row index
                    int rowIndex = Integer.valueOf(token).intValue();
                    this.rowIndexes[rowPosition] = rowIndex;
                }
            }

            rowPosition++;
        }
    }

    private void parseRowHeight(int rowPosition,
            StringTokenizer rowInfoFieldTokenizer, String nextToken) {
        int rowHeight = Integer.valueOf(nextToken).intValue();
        this.rowHeights[rowPosition] = rowHeight;

        if (rowInfoFieldTokenizer.hasMoreTokens()) {
            System.out
                    .println("Extra tokens detected after parsing row height for column position "
                            + rowPosition + "; ignoring");
        }
    }

    public void parseCellsInfo(String cellsInfo) {
        int rowPosition = 0;
        StringTokenizer rowOfCellInfoTokenizer = new StringTokenizer(cellsInfo,
                "\n");
        while (rowOfCellInfoTokenizer.hasMoreTokens()) {
            String rowOfCellInfo = rowOfCellInfoTokenizer.nextToken().trim();

            int columnPosition = 0;
            StringTokenizer cellInfoTokenizer = new StringTokenizer(
                    rowOfCellInfo, "|");
            while (cellInfoTokenizer.hasMoreTokens()) {
                String cellInfo = cellInfoTokenizer.nextToken().trim();

                StringTokenizer cellInfoFieldTokenizer = new StringTokenizer(
                        cellInfo, "~:", true);
                while (cellInfoFieldTokenizer.hasMoreTokens()) {
                    String token = cellInfoFieldTokenizer.nextToken().trim();

                    if ("<".equals(token)) {
                        // Span from left
                        this.dataValues[columnPosition][rowPosition] = this.dataValues[columnPosition - 1][rowPosition];

                        ILayerCell cell = this.cells[columnPosition - 1][rowPosition];
                        Rectangle boundsRect = this.bounds[columnPosition - 1][rowPosition];

                        if (columnPosition >= cell.getColumnPosition()
                                + cell.getColumnSpan()) {
                            boundsRect = new Rectangle(
                                    boundsRect.x,
                                    boundsRect.y,
                                    boundsRect.width
                                            + getColumnWidthByPosition(columnPosition),
                                    boundsRect.height);

                            final ILayerCell underlyingCell = cell;
                            cell = new TransformedLayerCell(cell) {
                                @Override
                                public int getColumnSpan() {
                                    return underlyingCell.getColumnSpan() + 1;
                                }
                            };
                        }

                        this.cells[columnPosition][rowPosition] = cell;
                        this.bounds[columnPosition][rowPosition] = boundsRect;

                        if (cellInfoFieldTokenizer.hasMoreTokens()) {
                            System.out
                                    .println("Extra tokens detected after parsing span for cell position "
                                            + columnPosition
                                            + ","
                                            + rowPosition + "; ignoring");
                        }
                        break;
                    } else if ("^".equals(token)) {
                        // Span from above
                        this.dataValues[columnPosition][rowPosition] = this.dataValues[columnPosition][rowPosition - 1];

                        ILayerCell cell = this.cells[columnPosition][rowPosition - 1];
                        Rectangle boundsRect = this.bounds[columnPosition][rowPosition - 1];

                        if (rowPosition >= cell.getRowPosition()
                                + cell.getRowSpan()) {
                            boundsRect = new Rectangle(
                                    boundsRect.x,
                                    boundsRect.y,
                                    boundsRect.width,
                                    boundsRect.height
                                            + getRowHeightByPosition(rowPosition));

                            final ILayerCell underlyingCell = cell;
                            cell = new TransformedLayerCell(cell) {
                                @Override
                                public int getRowSpan() {
                                    return underlyingCell.getRowSpan() + 1;
                                }
                            };
                        }

                        this.cells[columnPosition][rowPosition] = cell;
                        this.bounds[columnPosition][rowPosition] = boundsRect;

                        if (cellInfoFieldTokenizer.hasMoreTokens()) {
                            System.out
                                    .println("Extra tokens detected after parsing span for cell position "
                                            + columnPosition
                                            + ","
                                            + rowPosition + "; ignoring");
                        }
                        break;
                    } else if ("~".equals(token)) {
                        String nextToken = cellInfoFieldTokenizer.nextToken()
                                .trim();

                        if ("~".equals(nextToken)) {
                            throw new IllegalArgumentException(
                                    "Bad "
                                            + nextToken
                                            + " delimiter found when parsing display mode for cell position "
                                            + columnPosition + ","
                                            + rowPosition);
                        } else if (":".equals(nextToken)) {
                            // Parse config labels
                            parseConfigLabels(columnPosition, rowPosition,
                                    cellInfoFieldTokenizer,
                                    cellInfoFieldTokenizer.nextToken().trim());
                            break;
                        } else {
                            // Parse display mode
                            this.displayModes[columnPosition][rowPosition] = nextToken;
                        }
                    } else if (":".equals(token)) {
                        String nextToken = cellInfoFieldTokenizer.nextToken()
                                .trim();

                        if ("~".equals(nextToken)) {
                            throw new IllegalArgumentException(
                                    "Bad "
                                            + nextToken
                                            + " delimiter found when parsing config labels for cell position "
                                            + columnPosition + ","
                                            + rowPosition);
                        } else if (":".equals(nextToken)) {
                            throw new IllegalArgumentException(
                                    "Bad "
                                            + nextToken
                                            + " delimiter found when parsing config labels for cell position "
                                            + columnPosition + ","
                                            + rowPosition);
                        } else {
                            // Parse config labels
                            parseConfigLabels(columnPosition, rowPosition,
                                    cellInfoFieldTokenizer, nextToken);
                            break;
                        }
                    } else {
                        // Parse data value
                        this.dataValues[columnPosition][rowPosition] = token;
                        this.cells[columnPosition][rowPosition] = new TestLayerCell(
                                this, columnPosition, rowPosition);
                        this.bounds[columnPosition][rowPosition] = new Rectangle(
                                getStartXOfColumnPosition(columnPosition),
                                getStartYOfRowPosition(rowPosition),
                                getColumnWidthByPosition(columnPosition),
                                getRowHeightByPosition(rowPosition));
                    }
                }

                columnPosition++;
            }

            rowPosition++;
        }
    }

    private void parseConfigLabels(int columnPosition, int rowPosition,
            StringTokenizer cellInfoFieldTokenizer, String nextToken) {
        this.configLabels[columnPosition][rowPosition] = nextToken;

        if (cellInfoFieldTokenizer.hasMoreTokens()) {
            System.out
                    .println("Extra tokens detected after parsing config labels for cell position "
                            + columnPosition + "," + rowPosition + "; ignoring");
        }
    }

    @Override
    public void dispose() {
        // Do nothing
    }

    @Override
    public void registerPersistable(IPersistable persistable) {
        // Do nothing
    }

    @Override
    public void unregisterPersistable(IPersistable persistable) {
        // Do nothing
    }

    @Override
    public void saveState(String prefix, Properties properties) {
        // Do nothing
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        // Do nothing
    }

    @Override
    public void configure(IConfigRegistry configRegistry, UiBindingRegistry uiBindingRegistry) {
        // Do nothing
    }

    @Override
    public boolean doCommand(ILayerCommand command) {
        // Do nothing
        return false;
    }

    @Override
    public void registerCommandHandler(ILayerCommandHandler<?> commandHandler) {
        // Do nothing
    }

    @Override
    public void unregisterCommandHandler(
            Class<? extends ILayerCommand> commandClass) {
        // Do nothing
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        // Do nothing
    }

    @Override
    public void addLayerListener(ILayerListener listener) {
        // Do nothing
    }

    @Override
    public void removeLayerListener(ILayerListener listener) {
        // Do nothing
    }

    @Override
    public boolean hasLayerListener(
            Class<? extends ILayerListener> layerListenerClass) {
        return false;
    }

    @Override
    public ILayerPainter getLayerPainter() {
        return null;
    }

    @Override
    public IClientAreaProvider getClientAreaProvider() {
        return null;
    }

    @Override
    public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
        // Do nothing
    }

    @Override
    public LabelStack getRegionLabelsByXY(int x, int y) {
        return null;
    }

    // Horizontal features

    // Columns

    @Override
    public int getColumnCount() {
        return this.columnCount;
    }

    @Override
    public int getPreferredColumnCount() {
        return this.preferredColumnCount;
    }

    @Override
    public int getColumnIndexByPosition(int columnPosition) {
        if (columnPosition >= this.columnIndexes.length || columnPosition < 0) {
            return -1;
        }
        return this.columnIndexes[columnPosition];
    }

    @Override
    public int localToUnderlyingColumnPosition(int localColumnPosition) {
        return this.underlyingColumnPositions[localColumnPosition];
    }

    @Override
    public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer,
            int underlyingColumnPosition) {
        for (int localColumnPosition = 0; localColumnPosition < this.underlyingColumnPositions.length; localColumnPosition++) {
            if (this.underlyingColumnPositions[localColumnPosition] == underlyingColumnPosition) {
                return localColumnPosition;
            }
        }
        return -1;
    }

    @Override
    public Collection<Range> underlyingToLocalColumnPositions(
            ILayer sourceUnderlyingLayer,
            Collection<Range> underlyingColumnPositionRanges) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public int getColumnPositionByIndex(int columnIndex) {
        for (int columnPosition = 0; columnPosition < this.columnIndexes.length; columnPosition++) {
            if (this.columnIndexes[columnPosition] == columnIndex) {
                return columnPosition;
            }
        }
        return -1;
    }

    // Width

    @Override
    public int getWidth() {
        int width = 0;
        for (int columnPosition = 0; columnPosition < getColumnCount(); columnPosition++) {
            width += this.columnWidths[columnPosition];
        }
        return width;
    }

    @Override
    public int getPreferredWidth() {
        if (this.preferredWidth >= 0) {
            return this.preferredWidth;
        } else {
            return getWidth();
        }
    }

    @Override
    public int getColumnWidthByPosition(int columnPosition) {
        return this.columnWidths[columnPosition];
    }

    // Column resize

    @Override
    public boolean isColumnPositionResizable(int columnPosition) {
        return true;
    }

    // X

    @Override
    public int getColumnPositionByX(int x) {
        int width = 0;
        for (int columnPosition = 0; columnPosition < getColumnCount(); columnPosition++) {
            width += this.columnWidths[columnPosition];
            if (width > x) {
                return columnPosition;
            }
        }
        return -1;
    }

    @Override
    public int getStartXOfColumnPosition(int targetColumnPosition) {
        int width = 0;
        for (int columnPosition = 0; columnPosition < targetColumnPosition; columnPosition++) {
            width += this.columnWidths[columnPosition];
        }
        return width;
    }

    // Underlying

    @Override
    public Collection<ILayer> getUnderlyingLayersByColumnPosition(
            int columnPosition) {
        return null;
    }

    // Vertical features

    // Rows

    @Override
    public int getRowCount() {
        return this.rowCount;
    }

    @Override
    public int getPreferredRowCount() {
        return this.preferredRowCount;
    }

    @Override
    public int getRowIndexByPosition(int rowPosition) {
        return this.rowIndexes[rowPosition];
    }

    @Override
    public int localToUnderlyingRowPosition(int localRowPosition) {
        return this.underlyingRowPositions[localRowPosition];
    }

    @Override
    public int underlyingToLocalRowPosition(ILayer sourceUnderlyingLayer,
            int underlyingRowPosition) {
        for (int localRowPosition = 0; localRowPosition < this.underlyingRowPositions.length; localRowPosition++) {
            if (this.underlyingRowPositions[localRowPosition] == underlyingRowPosition) {
                return localRowPosition;
            }
        }
        return -1;
    }

    @Override
    public Collection<Range> underlyingToLocalRowPositions(
            ILayer sourceUnderlyingLayer,
            Collection<Range> underlyingRowPositionRanges) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public int getRowPositionByIndex(int rowIndex) {
        for (int rowPosition = 0; rowPosition < this.rowIndexes.length; rowPosition++) {
            if (this.rowIndexes[rowPosition] == rowIndex) {
                return rowPosition;
            }
        }
        return -1;
    }

    // Height

    @Override
    public int getHeight() {
        int height = 0;
        for (int rowPosition = 0; rowPosition < getRowCount(); rowPosition++) {
            height += this.rowHeights[rowPosition];
        }
        return height;
    }

    @Override
    public int getPreferredHeight() {
        if (this.preferredHeight >= 0) {
            return this.preferredHeight;
        } else {
            return getHeight();
        }
    }

    @Override
    public int getRowHeightByPosition(int rowPosition) {
        return this.rowHeights[rowPosition];
    }

    // Row resize

    @Override
    public boolean isRowPositionResizable(int rowPosition) {
        return true;
    }

    // Y

    @Override
    public int getRowPositionByY(int y) {
        int height = 0;
        for (int rowPosition = 0; rowPosition < getRowCount(); rowPosition++) {
            height += this.rowHeights[rowPosition];
            if (height > y) {
                return rowPosition;
            }
        }
        return -1;
    }

    @Override
    public int getStartYOfRowPosition(int targetRowPosition) {
        int height = 0;
        for (int rowPosition = 0; rowPosition < targetRowPosition; rowPosition++) {
            height += this.rowHeights[rowPosition];
        }
        return height;
    }

    // Underlying

    @Override
    public Collection<ILayer> getUnderlyingLayersByRowPosition(int rowPosition) {
        return null;
    }

    // Cell features

    @Override
    public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
        return new TestLayerCell(this.cells[columnPosition][rowPosition]);
    }

    @Override
    public Rectangle getBoundsByPosition(int columnPosition, int rowPosition) {
        return this.bounds[columnPosition][rowPosition];
    }

    @Override
    public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
        return this.displayModes[columnPosition][rowPosition];
    }

    @Override
    public LabelStack getConfigLabelsByPosition(int columnPosition,
            int rowPosition) {
        LabelStack labelStack = new LabelStack();
        String configLabelsString = this.configLabels[columnPosition][rowPosition];
        if (configLabelsString != null) {
            StringTokenizer configLabelTokenizer = new StringTokenizer(
                    configLabelsString, ",");
            while (configLabelTokenizer.hasMoreTokens()) {
                labelStack.addLabel(configLabelTokenizer.nextToken());
            }
        }
        return labelStack;
    }

    @Override
    public Object getDataValueByPosition(int columnPosition, int rowPosition) {
        return this.dataValues[columnPosition][rowPosition];
    }

    @Override
    public ILayer getUnderlyingLayerByPosition(int columnPosition,
            int rowPosition) {
        return null;
    }

    @Override
    public void fireLayerEvent(ILayerEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public ICellPainter getCellPainter(int columnPosition, int rowPosition,
            ILayerCell cell, IConfigRegistry configRegistry) {
        // TODO Auto-generated method stub
        return null;
    }

}
