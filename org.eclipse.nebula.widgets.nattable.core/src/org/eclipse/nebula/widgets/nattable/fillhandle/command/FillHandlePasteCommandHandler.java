/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *      Vincent Lorenzo <vincent.lorenzo@cea.fr> - Bug 486624
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.fillhandle.command;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.edit.command.EditUtils;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.fillhandle.config.FillHandleConfigAttributes;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Command handler for the {@link FillHandlePasteCommand}.
 *
 * @see FillHandlePasteCommand
 *
 * @since 1.4
 */
public class FillHandlePasteCommandHandler implements ILayerCommandHandler<FillHandlePasteCommand> {

    protected SelectionLayer selectionLayer;
    protected InternalCellClipboard clipboard;

    /**
     * Creates a {@link FillHandlePasteCommandHandler}
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} needed to determine the fill handle
     *            region and perform the update command.
     * @param clipboard
     *            The internal clipboard that carries the cells for the copy
     *            &amp; paste operation triggered by using the fill handle.
     */
    public FillHandlePasteCommandHandler(
            SelectionLayer selectionLayer,
            InternalCellClipboard clipboard) {

        this.selectionLayer = selectionLayer;
        this.clipboard = clipboard;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, FillHandlePasteCommand command) {
        if (this.clipboard.getCopiedCells() != null) {
            int pasteColumn = -1;
            int pasteRow = -1;
            int pasteWidth = this.clipboard.getCopiedCells().length;
            int pasteHeight = this.clipboard.getCopiedCells()[0].length;
            Rectangle handleRegion = this.selectionLayer.getFillHandleRegion();
            if (handleRegion != null) {
                pasteColumn = handleRegion.x;
                pasteRow = handleRegion.y;
                pasteWidth = handleRegion.width;
                pasteHeight = handleRegion.height;
            } else {
                PositionCoordinate coord = this.selectionLayer.getSelectionAnchor();
                pasteColumn = coord.getColumnPosition();
                pasteRow = coord.getRowPosition();
            }

            int pasteStartColumn = pasteColumn;

            int rowStartAdjustment = 0;
            if (command.direction == MoveDirectionEnum.UP) {
                rowStartAdjustment = pasteHeight % this.clipboard.getCopiedCells().length;
            }

            int columnStartAdjustment = 0;
            if (command.direction == MoveDirectionEnum.LEFT) {
                columnStartAdjustment = pasteWidth % this.clipboard.getCopiedCells()[0].length;
            }

            for (int i = 0; i < pasteHeight; i++) {
                ILayerCell[] cells = this.clipboard.getCopiedCells()[(i + rowStartAdjustment) % this.clipboard.getCopiedCells().length];
                for (int j = 0; j < pasteWidth; j++) {
                    ILayerCell cell = cells[(j + columnStartAdjustment) % this.clipboard.getCopiedCells()[0].length];

                    Object cellValue = getPasteValue(cell, command, pasteColumn, pasteRow);

                    if (EditUtils.isCellEditable(
                            this.selectionLayer,
                            command.configRegistry,
                            new PositionCoordinate(this.selectionLayer,
                                    pasteColumn,
                                    pasteRow))) {
                        this.selectionLayer.doCommand(new UpdateDataCommand(this.selectionLayer, pasteColumn, pasteRow, cellValue));
                    }

                    pasteColumn++;

                    if (pasteColumn >= this.selectionLayer.getColumnCount()) {
                        break;
                    }
                }

                pasteRow++;
                pasteColumn = pasteStartColumn;
            }
        }
        return true;
    }

    /**
     * Returns the value from the given cell that should be pasted to the given
     * position.
     *
     * @param cell
     *            The cell that is copied.
     * @param command
     *            The command that contains the necessary information for the
     *            paste operation.
     * @param toColumn
     *            The column position of the cell to which the value should be
     *            pasted.
     * @param toRow
     *            The row position of the cell to which the value should be
     *            pasted.
     * @return The value that should be set to the given position.
     */
    protected Object getPasteValue(ILayerCell cell, FillHandlePasteCommand command, int toColumn, int toRow) {
        switch (command.operation) {
            case COPY:
                return cell.getDataValue();
            case SERIES:
                Object diff = 0;
                if (command.direction == MoveDirectionEnum.LEFT || command.direction == MoveDirectionEnum.RIGHT) {
                    diff = calculateVerticalDiff(cell, toColumn, command.configRegistry);
                } else if (command.direction == MoveDirectionEnum.UP || command.direction == MoveDirectionEnum.DOWN) {
                    diff = calculateHorizontalDiff(cell, toRow, command.configRegistry);
                }
                Object value = cell.getDataValue();
                // if we can not determine a common diff value we perform a copy
                if (diff != null) {
                    if (value instanceof Byte) {
                        byte result = (byte) (((Byte) value).byteValue() + (Byte) diff);
                        return result;
                    } else if (value instanceof Short) {
                        short result = (short) (((Short) value).shortValue() + (Short) diff);
                        return result;
                    } else if (value instanceof Integer) {
                        return (Integer) value + (Integer) diff;
                    } else if (value instanceof Long) {
                        return (Long) value + (Long) diff;
                    } else if (value instanceof Float) {
                        return (Float) value + (Float) diff;
                    } else if (value instanceof Double) {
                        return (Double) value + (Double) diff;
                    } else if (value instanceof BigInteger) {
                        return ((BigInteger) value).add((BigInteger) diff);
                    } else if (value instanceof BigDecimal) {
                        return ((BigDecimal) value).add((BigDecimal) diff);
                    } else if (value instanceof Date) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime((Date) value);
                        cal.add(getIncrementDateField(cell, command.configRegistry), (Integer) diff);
                        return cal.getTime();
                    }
                }
                // if the value is neither a number nor a date simply return the
                // value as we can't calculate a series for other data types
                return value;
            default:
                return cell.getDataValue();
        }
    }

    protected Number calculateHorizontalDiff(ILayerCell cell, int toRow, IConfigRegistry configRegistry) {
        Class<?> type = cell.getDataValue() != null ? cell.getDataValue().getClass() : null;

        ILayerCell[][] cells = this.clipboard.getCopiedCells();
        int rowDiff = getRowDiff(cell, toRow);
        if (cells.length == 1) {
            return getCastValue(rowDiff, type);
        } else if (type != null) {
            int columnArrayIndex = cell.getColumnIndex() - this.clipboard.getCopiedCells()[0][0].getColumnIndex();
            if (type == Byte.class) {
                Byte diff = calculateByteDiff(cells[1][columnArrayIndex], cells[0][columnArrayIndex]);
                if (diff == null) {
                    return null;
                }
                Byte temp = diff;
                for (int i = 1; i < cells.length; i++) {
                    temp = calculateByteDiff(cells[i][columnArrayIndex], cells[i - 1][columnArrayIndex]);
                    if (temp == null || !temp.equals(diff)) {
                        return null;
                    }
                }
                byte result = (byte) (diff * rowDiff);
                return result;
            } else if (type == Short.class) {
                Short diff = calculateShortDiff(cells[1][columnArrayIndex], cells[0][columnArrayIndex]);
                if (diff == null) {
                    return null;
                }
                Short temp = diff;
                for (int i = 1; i < cells.length; i++) {
                    temp = calculateShortDiff(cells[i][columnArrayIndex], cells[i - 1][columnArrayIndex]);
                    if (temp == null || !temp.equals(diff)) {
                        return null;
                    }
                }
                short result = (short) (diff * rowDiff);
                return result;
            } else if (type == Integer.class) {
                Integer diff = calculateIntDiff(cells[1][columnArrayIndex], cells[0][columnArrayIndex]);
                if (diff == null) {
                    return null;
                }
                Integer temp = diff;
                for (int i = 1; i < cells.length; i++) {
                    temp = calculateIntDiff(cells[i][columnArrayIndex], cells[i - 1][columnArrayIndex]);
                    if (temp == null || !temp.equals(diff)) {
                        return null;
                    }
                }
                return diff * rowDiff;
            } else if (type == Long.class) {
                Long diff = calculateLongDiff(cells[1][columnArrayIndex], cells[0][columnArrayIndex]);
                if (diff == null) {
                    return null;
                }
                Long temp = diff;
                for (int i = 1; i < cells.length; i++) {
                    temp = calculateLongDiff(cells[i][columnArrayIndex], cells[i - 1][columnArrayIndex]);
                    if (temp == null || !temp.equals(diff)) {
                        return null;
                    }
                }
                return diff * rowDiff;
            } else if (type == Float.class) {
                Float diff = calculateFloatDiff(cells[1][columnArrayIndex], cells[0][columnArrayIndex]);
                if (diff == null) {
                    return null;
                }
                Float temp = diff;
                for (int i = 1; i < cells.length; i++) {
                    temp = calculateFloatDiff(cells[i][columnArrayIndex], cells[i - 1][columnArrayIndex]);
                    if (temp == null || !temp.equals(diff)) {
                        return null;
                    }
                }
                return BigDecimal.valueOf(diff).multiply(BigDecimal.valueOf(rowDiff)).floatValue();
            } else if (type == Double.class) {
                Double diff = calculateDoubleDiff(cells[1][columnArrayIndex], cells[0][columnArrayIndex]);
                if (diff == null) {
                    return null;
                }
                Double temp = diff;
                for (int i = 1; i < cells.length; i++) {
                    temp = calculateDoubleDiff(cells[i][columnArrayIndex], cells[i - 1][columnArrayIndex]);
                    if (temp == null || !temp.equals(diff)) {
                        return null;
                    }
                }
                return BigDecimal.valueOf(diff).multiply(BigDecimal.valueOf(rowDiff)).doubleValue();
            } else if (type == BigInteger.class) {
                BigInteger diff = calculateBigIntegerDiff(cells[1][columnArrayIndex], cells[0][columnArrayIndex]);
                if (diff == null) {
                    return null;
                }
                BigInteger temp = diff;
                for (int i = 1; i < cells.length; i++) {
                    temp = calculateBigIntegerDiff(cells[i][columnArrayIndex], cells[i - 1][columnArrayIndex]);
                    if (temp == null || !temp.equals(diff)) {
                        return null;
                    }
                }
                return diff.multiply(BigInteger.valueOf(rowDiff));
            } else if (type == BigDecimal.class) {
                BigDecimal diff = calculateBigDecimalDiff(cells[1][columnArrayIndex], cells[0][columnArrayIndex]);
                if (diff == null) {
                    return null;
                }
                BigDecimal temp = diff;
                for (int i = 1; i < cells.length; i++) {
                    temp = calculateBigDecimalDiff(cells[i][columnArrayIndex], cells[i - 1][columnArrayIndex]);
                    if (temp == null || !temp.equals(diff)) {
                        return null;
                    }
                }
                return diff.multiply(BigDecimal.valueOf(rowDiff));
            } else if (type == Date.class) {
                Integer diff = calculateDateDiff(cells[1][columnArrayIndex], cells[0][columnArrayIndex], configRegistry);
                if (diff == null) {
                    return null;
                }
                Integer temp = diff;
                for (int i = 1; i < cells.length; i++) {
                    temp = calculateDateDiff(cells[i][columnArrayIndex], cells[i - 1][columnArrayIndex], configRegistry);
                    if (temp == null || !temp.equals(diff)) {
                        return null;
                    }
                }
                return diff * rowDiff;
            }
        }
        return null;
    }

    protected Number calculateVerticalDiff(ILayerCell cell, int toColumn, IConfigRegistry configRegistry) {
        Class<?> type = cell.getDataValue() != null ? cell.getDataValue().getClass() : null;

        ILayerCell[][] cells = this.clipboard.getCopiedCells();
        int columnDiff = getColumnDiff(cell, toColumn);
        int rowArrayIndex = cell.getRowIndex() - this.clipboard.getCopiedCells()[0][0].getRowIndex();
        if (cells[rowArrayIndex].length == 1) {
            return getCastValue(columnDiff, type);
        } else if (type != null) {
            if (type == Byte.class) {
                Byte diff = calculateByteDiff(cells[rowArrayIndex][1], cells[rowArrayIndex][0]);
                if (diff == null) {
                    return null;
                }
                Byte temp = diff;
                for (int i = 1; i < cells.length; i++) {
                    temp = calculateByteDiff(cells[rowArrayIndex][i], cells[rowArrayIndex][i - 1]);
                    if (temp == null || !temp.equals(diff)) {
                        return null;
                    }
                }
                byte result = (byte) (diff * columnDiff);
                return result;
            } else if (type == Short.class) {
                Short diff = calculateShortDiff(cells[rowArrayIndex][1], cells[rowArrayIndex][0]);
                if (diff == null) {
                    return null;
                }
                Short temp = diff;
                for (int i = 1; i < cells.length; i++) {
                    temp = calculateShortDiff(cells[rowArrayIndex][i], cells[rowArrayIndex][i - 1]);
                    if (temp == null || !temp.equals(diff)) {
                        return null;
                    }
                }
                short result = (short) (diff * columnDiff);
                return result;
            } else if (type == Integer.class) {
                Integer diff = calculateIntDiff(cells[rowArrayIndex][1], cells[rowArrayIndex][0]);
                if (diff == null) {
                    return null;
                }
                Integer temp = diff;
                for (int i = 1; i < cells.length; i++) {
                    temp = calculateIntDiff(cells[rowArrayIndex][i], cells[rowArrayIndex][i - 1]);
                    if (temp == null || !temp.equals(diff)) {
                        return null;
                    }
                }
                return diff * columnDiff;
            } else if (type == Long.class) {
                Long diff = calculateLongDiff(cells[rowArrayIndex][1], cells[rowArrayIndex][0]);
                if (diff == null) {
                    return null;
                }
                Long temp = diff;
                for (int i = 1; i < cells.length; i++) {
                    temp = calculateLongDiff(cells[rowArrayIndex][i], cells[rowArrayIndex][i - 1]);
                    if (temp == null || !temp.equals(diff)) {
                        return null;
                    }
                }
                return diff * columnDiff;
            } else if (type == Float.class) {
                Float diff = calculateFloatDiff(cells[rowArrayIndex][1], cells[rowArrayIndex][0]);
                if (diff == null) {
                    return null;
                }
                Float temp = diff;
                for (int i = 1; i < cells.length; i++) {
                    temp = calculateFloatDiff(cells[rowArrayIndex][i], cells[rowArrayIndex][i - 1]);
                    if (temp == null || !temp.equals(diff)) {
                        return null;
                    }
                }
                return BigDecimal.valueOf(diff).multiply(BigDecimal.valueOf(columnDiff)).floatValue();
            } else if (type == Double.class) {
                Double diff = calculateDoubleDiff(cells[rowArrayIndex][1], cells[rowArrayIndex][0]);
                if (diff == null) {
                    return null;
                }
                Double temp = diff;
                for (int i = 1; i < cells.length; i++) {
                    temp = calculateDoubleDiff(cells[rowArrayIndex][i], cells[rowArrayIndex][i - 1]);
                    if (temp == null || !temp.equals(diff)) {
                        return null;
                    }
                }
                return BigDecimal.valueOf(diff).multiply(BigDecimal.valueOf(columnDiff)).doubleValue();
            } else if (type == BigInteger.class) {
                BigInteger diff = calculateBigIntegerDiff(cells[rowArrayIndex][1], cells[rowArrayIndex][0]);
                if (diff == null) {
                    return null;
                }
                BigInteger temp = diff;
                for (int i = 1; i < cells.length; i++) {
                    temp = calculateBigIntegerDiff(cells[rowArrayIndex][i], cells[rowArrayIndex][i - 1]);
                    if (temp == null || !temp.equals(diff)) {
                        return null;
                    }
                }
                return diff.multiply(BigInteger.valueOf(columnDiff));
            } else if (type == BigDecimal.class) {
                BigDecimal diff = calculateBigDecimalDiff(cells[rowArrayIndex][1], cells[rowArrayIndex][0]);
                if (diff == null) {
                    return null;
                }
                BigDecimal temp = diff;
                for (int i = 1; i < cells.length; i++) {
                    temp = calculateBigDecimalDiff(cells[rowArrayIndex][i], cells[rowArrayIndex][i - 1]);
                    if (temp == null || !temp.equals(diff)) {
                        return null;
                    }
                }
                return diff.multiply(BigDecimal.valueOf(columnDiff));
            } else if (type == Date.class) {
                Integer diff = calculateDateDiff(cells[rowArrayIndex][1], cells[rowArrayIndex][0], configRegistry);
                if (diff == null) {
                    return null;
                }
                Integer temp = diff;
                for (int i = 1; i < cells.length; i++) {
                    temp = calculateDateDiff(cells[rowArrayIndex][i], cells[rowArrayIndex][i - 1], configRegistry);
                    if (temp == null || !temp.equals(diff)) {
                        return null;
                    }
                }
                return diff * columnDiff;
            }
        }
        return null;
    }

    /**
     * Calculate the row difference between the cell row index and the row index
     * of the cell to copy to.
     *
     * @param currentCell
     *            The current cell to manage.
     * @param toRow
     *            The row index of the cell to copy to.
     * @return The difference as integer.
     */
    protected int getRowDiff(final ILayerCell currentCell, final int toRow) {
        return toRow - currentCell.getRowIndex();
    }

    /**
     * Calculate the column difference between the cell column index and the
     * column index of the cell to copy to.
     *
     * @param currentCell
     *            The current cell to manage.
     * @param toColumn
     *            The column index of the cell to copy to.
     * @return The difference as integer.
     */
    protected int getColumnDiff(final ILayerCell currentCell, final int toColumn) {
        return toColumn - currentCell.getColumnIndex();
    }

    protected Number getCastValue(int diff, Class<?> type) {
        if (type != null) {
            if (type == Byte.class) {
                return (byte) diff;
            } else if (type == Short.class) {
                return (short) diff;
            } else if (type == Long.class) {
                return (long) diff;
            } else if (type == Float.class) {
                return (float) diff;
            } else if (type == Double.class) {
                return (double) diff;
            } else if (type == BigInteger.class) {
                return BigInteger.valueOf(diff);
            } else if (type == BigDecimal.class) {
                return BigDecimal.valueOf(diff);
            }
        }
        return diff;
    }

    protected Byte calculateByteDiff(ILayerCell c1, ILayerCell c2) {
        return (c1.getDataValue() == null || c2.getDataValue() == null
                || !(c1.getDataValue() instanceof Byte) || !(c2.getDataValue() instanceof Byte))
                        ? null : (byte) (((Byte) c1.getDataValue()) - ((Byte) c2.getDataValue()));
    }

    protected Short calculateShortDiff(ILayerCell c1, ILayerCell c2) {
        return (c1.getDataValue() == null || c2.getDataValue() == null
                || !(c1.getDataValue() instanceof Short) || !(c2.getDataValue() instanceof Short))
                        ? null : (short) (((Short) c1.getDataValue()) - ((Short) c2.getDataValue()));
    }

    protected Integer calculateIntDiff(ILayerCell c1, ILayerCell c2) {
        return (c1.getDataValue() == null || c2.getDataValue() == null
                || !(c1.getDataValue() instanceof Integer) || !(c2.getDataValue() instanceof Integer))
                        ? null : (Integer) (c1.getDataValue()) - (Integer) (c2.getDataValue());
    }

    protected Long calculateLongDiff(ILayerCell c1, ILayerCell c2) {
        return (c1.getDataValue() == null || c2.getDataValue() == null
                || !(c1.getDataValue() instanceof Long) || !(c2.getDataValue() instanceof Long))
                        ? null : ((Long) c1.getDataValue()) - ((Long) c2.getDataValue());
    }

    protected Float calculateFloatDiff(ILayerCell c1, ILayerCell c2) {
        if (c1.getDataValue() == null || c2.getDataValue() == null
                || !(c1.getDataValue() instanceof Float) || !(c2.getDataValue() instanceof Float)) {
            return null;
        }

        // Use BigDecimal for exact calculations because of floating point
        // arithmetic issues. Because of that we also use the String constructor
        // of BigDecimal.
        BigDecimal v1 = new BigDecimal(c1.getDataValue().toString());
        BigDecimal v2 = new BigDecimal(c2.getDataValue().toString());
        return v1.subtract(v2).floatValue();
    }

    protected Double calculateDoubleDiff(ILayerCell c1, ILayerCell c2) {
        if (c1.getDataValue() == null || c2.getDataValue() == null
                || !(c1.getDataValue() instanceof Double) || !(c2.getDataValue() instanceof Double)) {
            return null;
        }

        // Use BigDecimal for exact calculations because of floating point
        // arithmetic issues. Because of that we also use the String constructor
        // of BigDecimal.
        BigDecimal v1 = new BigDecimal(c1.getDataValue().toString());
        BigDecimal v2 = new BigDecimal(c2.getDataValue().toString());
        return v1.subtract(v2).doubleValue();
    }

    protected BigInteger calculateBigIntegerDiff(ILayerCell c1, ILayerCell c2) {
        return (c1.getDataValue() == null || c2.getDataValue() == null
                || !(c1.getDataValue() instanceof BigInteger) || !(c2.getDataValue() instanceof BigInteger))
                        ? null : ((BigInteger) c1.getDataValue()).subtract((BigInteger) c2.getDataValue());
    }

    protected BigDecimal calculateBigDecimalDiff(ILayerCell c1, ILayerCell c2) {
        return (c1.getDataValue() == null || c2.getDataValue() == null
                || !(c1.getDataValue() instanceof BigDecimal) || !(c2.getDataValue() instanceof BigDecimal))
                        ? null : ((BigDecimal) c1.getDataValue()).subtract((BigDecimal) c2.getDataValue());
    }

    protected Integer calculateDateDiff(ILayerCell c1, ILayerCell c2, IConfigRegistry configRegistry) {
        if (c1.getDataValue() == null || c2.getDataValue() == null
                || !(c1.getDataValue() instanceof Date) || !(c2.getDataValue() instanceof Date)) {
            return null;
        }

        int dateField = getIncrementDateField(c1, configRegistry);

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime((Date) c1.getDataValue());
        int fieldValue1 = cal1.get(dateField);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime((Date) c2.getDataValue());
        int fieldValue2 = cal2.get(dateField);

        return fieldValue1 - fieldValue2;
    }

    protected int getIncrementDateField(ILayerCell cell, IConfigRegistry configRegistry) {
        Integer dateField = configRegistry.getConfigAttribute(
                FillHandleConfigAttributes.INCREMENT_DATE_FIELD,
                DisplayMode.NORMAL,
                cell.getConfigLabels().getLabels());

        if (dateField == null) {
            dateField = Calendar.DATE;
        }

        return dateField;
    }

    @Override
    public Class<FillHandlePasteCommand> getCommandClass() {
        return FillHandlePasteCommand.class;
    }

}
