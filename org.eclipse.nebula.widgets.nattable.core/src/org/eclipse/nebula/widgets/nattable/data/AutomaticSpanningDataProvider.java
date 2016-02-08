/*******************************************************************************
 * Copyright (c) 2013, 2016 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *    neal zhang <nujiah001@126.com> - Bug 442009
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.nebula.widgets.nattable.layer.cell.DataCell;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;

/**
 * This implementation of ISpanningDataProvider will automatically span cells if
 * the containing cell values are equal. It supports configuration whether the
 * automatic spanning should be performed for columns or cells. It is even
 * possible to configure which columns/rows should be checked for auto spanning.
 * <p>
 * It wraps the IDataProvider that is used for providing the data to the
 * NatTable, so it is possible to use existing code and enhance it easily with
 * the auto spanning feature.
 * <p>
 * To use the auto spanning feature you simply need to exchange the DataLayer in
 * your layer composition with the SpanningDataLayer and wrap the exising
 * IDataProvider with this AutomaticSpanningDataProvider.
 * <p>
 * <b>Note: </b><br>
 * Mixing of automatic column and row spanning could cause several rendering
 * issues if there can be no rectangle build out of matching cell values. If a
 * mixing is needed, a more complicated calculation algorithm need to be
 * implemented that checks every columns and row by building the spanning cell
 * for the matching rectangle. As this would be quite time consuming
 * calculations, this is not supported out of the box by NatTable.
 */
public class AutomaticSpanningDataProvider implements ISpanningDataProvider, IPersistable {

    public static final String PERSISTENCE_KEY_AUTO_COLUMN_SPAN = ".autoColumnSpan"; //$NON-NLS-1$
    public static final String PERSISTENCE_KEY_AUTO_ROW_SPAN = ".autoRowSpan"; //$NON-NLS-1$
    public static final String PERSISTENCE_KEY_AUTO_SPAN_COLUMNS = ".autoSpanColumns"; //$NON-NLS-1$
    public static final String PERSISTENCE_KEY_AUTO_SPAN_ROWS = ".autoSpanRows"; //$NON-NLS-1$

    /**
     * The IDataProvider that is wrapped by this AutomaticSpanningDataProvider
     */
    private final IDataProvider underlyingDataProvider;
    /**
     * Flag to configure this AutomaticSpanningDataProvider to perform automatic
     * column spanning
     */
    private boolean autoColumnSpan;
    /**
     * Flag to configure this AutomaticSpanningDataProvider to perform automatic
     * row spanning
     */
    private boolean autoRowSpan;
    /**
     * List of column positions for which automatic spanning is enabled.
     * <p>
     * <b>Note: </b>If this list is empty, all columns will do auto row
     * spanning.
     */
    private List<Integer> autoSpanColumns = new ArrayList<Integer>();
    /**
     * List of row positions for which automatic spanning is enabled.
     * <p>
     * <b>Note: </b>If this list is empty, all rows will do auto column
     * spanning.
     */
    private List<Integer> autoSpanRows = new ArrayList<Integer>();

    /**
     *
     * @param underlyingDataProvider
     *            The IDataProvider that should be wrapped by this
     *            AutomaticSpanningDataProvider
     * @param autoColumnSpan
     *            Flag to configure this AutomaticSpanningDataProvider to
     *            perform automatic column spanning
     * @param autoRowSpan
     *            Flag to configure this AutomaticSpanningDataProvider to
     *            perform automatic row spanning
     */
    public AutomaticSpanningDataProvider(
            IDataProvider underlyingDataProvider,
            boolean autoColumnSpan, boolean autoRowSpan) {
        this.underlyingDataProvider = underlyingDataProvider;
        this.autoColumnSpan = autoColumnSpan;
        this.autoRowSpan = autoRowSpan;
    }

    @Override
    public Object getDataValue(int columnIndex, int rowIndex) {
        return this.underlyingDataProvider.getDataValue(columnIndex, rowIndex);
    }

    @Override
    public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
        this.underlyingDataProvider.setDataValue(columnIndex, rowIndex, newValue);
    }

    @Override
    public int getColumnCount() {
        return this.underlyingDataProvider.getColumnCount();
    }

    @Override
    public int getRowCount() {
        return this.underlyingDataProvider.getRowCount();
    }

    @Override
    public DataCell getCellByPosition(int columnPosition, int rowPosition) {
        int cellColumnPosition = isAutoSpanEnabledForColumn(columnPosition, rowPosition)
                ? getStartColumnPosition(columnPosition, rowPosition) : columnPosition;
        int cellRowPosition = isAutoSpanEnabledForRow(columnPosition, rowPosition)
                ? getStartRowPosition(columnPosition, rowPosition) : rowPosition;

        int columnSpan = isAutoSpanEnabledForColumn(columnPosition, rowPosition)
                ? getColumnSpan(cellColumnPosition, cellRowPosition) : 1;
        int rowSpan = isAutoSpanEnabledForRow(columnPosition, rowPosition)
                ? getRowSpan(cellColumnPosition, cellRowPosition) : 1;

        return new DataCell(cellColumnPosition, cellRowPosition, columnSpan, rowSpan);
    }

    /**
     * Check if the given column should be used for auto spanning.
     *
     * @param columnPosition
     *            The column position to check for auto spanning
     * @param rowPosition
     *            The row position for which the column spanning should be
     *            checked
     * @return <code>true</code> if for that column position auto spanning is
     *         enabled
     */
    protected boolean isAutoSpanEnabledForColumn(int columnPosition, int rowPosition) {
        return (this.autoColumnSpan && isAutoSpanRow(rowPosition));
    }

    /**
     * Check if the given row should be used for auto spanning.
     *
     * @param columnPosition
     *            The column position for which the row spanning should be
     *            checked.
     * @param rowPosition
     *            The row position to check for auto spanning
     * @return <code>true</code> if for that row position auto spanning is
     *         enabled
     */
    protected boolean isAutoSpanEnabledForRow(int columnPosition, int rowPosition) {
        return (this.autoRowSpan && isAutoSpanColumn(columnPosition));
    }

    /**
     * Checks if the given column position is configured as a auto span column.
     *
     * @param columnPosition
     *            The column position to check
     * @return <code>true</code> if the given column position is configured as a
     *         auto span column.
     */
    private boolean isAutoSpanColumn(int columnPosition) {
        return (this.autoSpanColumns.isEmpty() || this.autoSpanColumns.contains(columnPosition));
    }

    /**
     * Checks if the given row position is configured as a auto span row.
     *
     * @param rowPosition
     *            The row position to check
     * @return <code>true</code> if the given row position is configured as a
     *         auto span row.
     */
    private boolean isAutoSpanRow(int rowPosition) {
        return (this.autoSpanRows.isEmpty() || this.autoSpanRows.contains(rowPosition));
    }

    /**
     * Configures the given column positions for auto spanning. This means that
     * the rows in the given columns will be automatically spanned if the
     * content is equal. Setting column positions for auto spanning will cause
     * that the rows in all other columns won't be auto spanned anymore.
     *
     * @param columnPositions
     *            The column positions to add for auto spanning.
     */
    public void addAutoSpanningColumnPositions(Integer... columnPositions) {
        this.autoSpanColumns.addAll(Arrays.asList(columnPositions));
    }

    /**
     * Configures the given row positions for auto spanning. This means that the
     * columns in the given rows will be automatically spanned if the content is
     * equal. Setting row positions for auto spanning will cause that the
     * columns in all other rows won't be auto spanned anymore.
     *
     * @param rowPositions
     *            The row positions to add for auto spanning.
     */
    public void addAutoSpanningRowPositions(Integer... rowPositions) {
        this.autoSpanRows.addAll(Arrays.asList(rowPositions));
    }

    /**
     * Removes the given column positions for auto spanning.
     *
     * @param columnPositions
     *            The column positions to remove for auto spanning.
     */
    public void removeAutoSpanningColumnPositions(Integer... columnPositions) {
        this.autoSpanColumns.removeAll(Arrays.asList(columnPositions));
    }

    /**
     * Removes the given row positions for auto spanning.
     *
     * @param rowPositions
     *            The row positions to remove for auto spanning.
     */
    public void removeAutoSpanningRowPositions(Integer... rowPositions) {
        this.autoSpanRows.removeAll(Arrays.asList(rowPositions));
    }

    /**
     * Clears the list of column positions for which auto spanning rows is
     * enabled. Note that clearing the list and leaving the autoRowSpan flag set
     * to <code>true</code> will cause that on all columns the row spanning will
     * be performed.
     */
    public void clearAutoSpanningColumnPositions() {
        this.autoSpanColumns.clear();
    }

    /**
     * Clears the list of row positions for which auto spanning columns is
     * enabled. Note that clearing the list and leaving the autoColumnSpan flag
     * set to <code>true</code> will cause that on all rows the column spanning
     * will be performed.
     */
    public void clearAutoSpanningRowPositions() {
        this.autoSpanRows.clear();
    }

    /**
     * Checks if the column to the left of the given column position contains
     * the same value. In this case the given column is spanned with the one to
     * the left and therefore that column position will be returned here.
     *
     * @param columnPosition
     *            The column position whose spanning starting column is searched
     * @param rowPosition
     *            The row position where the column spanning should be
     *            performed.
     * @return The column position where the spanning starts or the given column
     *         position if it is not spanned with the columns to the left.
     */
    protected int getStartColumnPosition(int columnPosition, int rowPosition) {
        int columnPos;
        for (columnPos = columnPosition; columnPos >= 0; columnPos--) {
            if (columnPos <= 0 || !isAutoSpanColumn(columnPos)
                    || !isAutoSpanColumn(columnPos - 1)) {
                break;
            }

            // get value for the given column
            Object current = getDataValue(columnPos, rowPosition);
            // get value of the column to the left
            Object before = getDataValue(columnPos - 1, rowPosition);

            if (valuesNotEqual(current, before)) {
                // the both values are not equal, therefore return the given
                // column position
                break;
            }
        }
        return columnPos;
    }

    /**
     * Checks if the row above the given row position contains the same value.
     * In this case the given row is spanned with the above and therefore the
     * above row position will be returned here.
     *
     * @param columnPosition
     *            The column position for which the row spanning should be
     *            checked
     * @param rowPosition
     *            The row position whose spanning state should be checked.
     * @return The row position where the spanning starts or the given row
     *         position if it is not spanned with rows above.
     */
    protected int getStartRowPosition(int columnPosition, int rowPosition) {
        int rowPos;
        for (rowPos = rowPosition; rowPos >= 0; rowPos--) {
            if (rowPos <= 0 || !isAutoSpanRow(rowPos)
                    || !isAutoSpanRow(rowPos - 1)) {
                break;
            }

            // get value of given row
            Object current = getDataValue(columnPosition, rowPos);
            // get value of row before
            Object before = getDataValue(columnPosition, rowPos - 1);

            if (valuesNotEqual(current, before)) {
                // the both values are not equal, therefore return the given row
                break;
            }
        }
        return rowPos;
    }

    /**
     * Calculates the number of columns to span regarding the data of the cells.
     *
     * @param columnPosition
     *            The column position to start the check for spanning
     * @param rowPosition
     *            The row position for which the column spanning should be
     *            checked
     * @return The number of columns to span
     */
    protected int getColumnSpan(int columnPosition, int rowPosition) {
        int span = 1;

        while (columnPosition < getColumnCount() - 1
                && isAutoSpanColumn(columnPosition)
                && isAutoSpanColumn(columnPosition + 1)
                && !valuesNotEqual(
                        getDataValue(columnPosition, rowPosition),
                        getDataValue(columnPosition + 1, rowPosition))) {
            span++;
            columnPosition++;
        }
        return span;
    }

    /**
     * Calculates the number of rows to span regarding the data of the cells.
     *
     * @param columnPosition
     *            The column position for which the row spanning should be
     *            checked
     * @param rowPosition
     *            The row position to start the check for spanning
     * @return The number of rows to span
     */
    protected int getRowSpan(int columnPosition, int rowPosition) {
        int span = 1;

        while (rowPosition < getRowCount() - 1
                && isAutoSpanRow(rowPosition)
                && isAutoSpanRow(rowPosition + 1)
                && !valuesNotEqual(
                        getDataValue(columnPosition, rowPosition),
                        getDataValue(columnPosition, rowPosition + 1))) {
            span++;
            rowPosition++;
        }
        return span;
    }

    /**
     * Check if the given values are equal. This method is <code>null</code>
     * sage.
     *
     * @param value1
     *            The first value to check for equality with the second value
     * @param value2
     *            The second value to check for equality with the first value.
     * @return <code>true</code> if the given values are not equal.
     */
    protected boolean valuesNotEqual(Object value1, Object value2) {
        if (value1 == value2) {
            return false;
        }
        return ((value1 == null && value2 != null)
                || (value1 != null && value2 == null)
                || !value1.equals(value2));
    }

    /**
     * @return <code>true</code> if automatic column spanning is enabled
     */
    public boolean isAutoColumnSpan() {
        return this.autoColumnSpan;
    }

    /**
     * @param autoColumnSpan
     *            <code>true</code> to enable automatic column spanning,
     *            <code>false</code> to disable it
     */
    public void setAutoColumnSpan(boolean autoColumnSpan) {
        this.autoColumnSpan = autoColumnSpan;
    }

    /**
     * @return <code>true</code> if automatic row spanning is enabled
     */
    public boolean isAutoRowSpan() {
        return this.autoRowSpan;
    }

    /**
     * @param autoRowSpan
     *            <code>true</code> to enable automatic row spanning,
     *            <code>false</code> to disable it
     */
    public void setAutoRowSpan(boolean autoRowSpan) {
        this.autoRowSpan = autoRowSpan;
    }

    @Override
    public void saveState(String prefix, Properties properties) {
        properties.setProperty(
                prefix + PERSISTENCE_KEY_AUTO_COLUMN_SPAN,
                Boolean.valueOf(this.autoColumnSpan).toString());
        properties.setProperty(
                prefix + PERSISTENCE_KEY_AUTO_ROW_SPAN,
                Boolean.valueOf(this.autoRowSpan).toString());

        if (this.autoSpanColumns.size() > 0) {
            StringBuilder strBuilder = new StringBuilder();
            for (Integer index : this.autoSpanColumns) {
                strBuilder.append(index);
                strBuilder.append(IPersistable.VALUE_SEPARATOR);
            }
            properties.setProperty(
                    prefix + PERSISTENCE_KEY_AUTO_SPAN_COLUMNS,
                    strBuilder.toString());
        }

        if (this.autoSpanRows.size() > 0) {
            StringBuilder strBuilder = new StringBuilder();
            for (Integer index : this.autoSpanRows) {
                strBuilder.append(index);
                strBuilder.append(IPersistable.VALUE_SEPARATOR);
            }
            properties.setProperty(
                    prefix + PERSISTENCE_KEY_AUTO_SPAN_ROWS,
                    strBuilder.toString());
        }
    }

    @Override
    public void loadState(String prefix, Properties properties) {
        String property = properties.getProperty(prefix + PERSISTENCE_KEY_AUTO_COLUMN_SPAN);
        if (property != null) {
            this.autoColumnSpan = Boolean.valueOf(property);
        }

        property = properties.getProperty(prefix + PERSISTENCE_KEY_AUTO_ROW_SPAN);
        if (property != null) {
            this.autoRowSpan = Boolean.valueOf(property);
        }

        this.autoSpanColumns.clear();
        property = properties.getProperty(prefix + PERSISTENCE_KEY_AUTO_SPAN_COLUMNS);
        if (property != null) {
            List<Integer> newAutoSpanColumns = new ArrayList<Integer>();
            StringTokenizer tok = new StringTokenizer(property, IPersistable.VALUE_SEPARATOR);
            while (tok.hasMoreTokens()) {
                String index = tok.nextToken();
                newAutoSpanColumns.add(Integer.valueOf(index));
            }

            this.autoSpanColumns.addAll(newAutoSpanColumns);
        }

        this.autoSpanRows.clear();
        property = properties.getProperty(prefix + PERSISTENCE_KEY_AUTO_SPAN_ROWS);
        if (property != null) {
            List<Integer> newAutoSpanRows = new ArrayList<Integer>();
            StringTokenizer tok = new StringTokenizer(property, IPersistable.VALUE_SEPARATOR);
            while (tok.hasMoreTokens()) {
                String index = tok.nextToken();
                newAutoSpanRows.add(Integer.valueOf(index));
            }

            this.autoSpanRows.addAll(newAutoSpanRows);
        }
    }
}