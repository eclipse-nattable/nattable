/*****************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.hierarchical;

import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ISpanningDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.DataCell;

/**
 * This implementation of {@link ISpanningDataProvider} will span cells if the
 * level objects of the {@link HierarchicalWrapper} row objects are actually the
 * same in the same column. With this spanning the hierarchical object graph can
 * be visually represented as the parent objects will be shown in a single
 * spanned cell for all child rows.
 * <p>
 * It wraps the {@link IRowDataProvider} that is used for providing the
 * normalized hierarchical object data to the NatTable.
 * </p>
 *
 * @since 1.6
 */
public class HierarchicalSpanningDataProvider implements ISpanningDataProvider {

    /**
     * The {@link IRowDataProvider} that is wrapped by this
     * {@link HierarchicalSpanningDataProvider}.
     */
    private final IRowDataProvider<HierarchicalWrapper> underlyingDataProvider;
    /**
     * The property names that are used to access the data.
     */
    private final List<String> propertyNames;

    /**
     *
     * @param underlyingDataProvider
     *            The {@link IRowDataProvider} that should be wrapped by this
     *            {@link HierarchicalSpanningDataProvider}. Needed to access the
     *            level object via the row object to determine if spanning
     *            should be applied.
     * @param propertyNames
     *            The property names that are used to access the data. Needed to
     *            determine the level object inside the
     *            {@link HierarchicalWrapper}.
     */
    public HierarchicalSpanningDataProvider(
            IRowDataProvider<HierarchicalWrapper> underlyingDataProvider,
            String... propertyNames) {
        this(underlyingDataProvider, Arrays.asList(propertyNames));
    }

    /**
     *
     * @param underlyingDataProvider
     *            The {@link IRowDataProvider} that should be wrapped by this
     *            {@link HierarchicalSpanningDataProvider}. Needed to access the
     *            level object via the row object to determine if spanning
     *            should be applied.
     * @param propertyNames
     *            The property names that are used to access the data. Needed to
     *            determine the level object inside the
     *            {@link HierarchicalWrapper}.
     */
    public HierarchicalSpanningDataProvider(
            IRowDataProvider<HierarchicalWrapper> underlyingDataProvider,
            List<String> propertyNames) {
        this.underlyingDataProvider = underlyingDataProvider;
        this.propertyNames = propertyNames;
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
        // determine the hierarchy level
        String propertyName = this.propertyNames.get(columnPosition);
        int level = propertyName.split(HierarchicalHelper.PROPERTY_SEPARATOR_REGEX).length - 1;

        int cellRowPosition = getStartRowPosition(rowPosition, level);
        int rowSpan = getRowSpan(cellRowPosition, level);

        return new DataCell(columnPosition, cellRowPosition, 1, rowSpan);
    }

    /**
     * Checks if the row above the given row position contains the same value.
     * In this case the given row is spanned with the above and therefore the
     * above row position will be returned here.
     *
     * @param rowPosition
     *            The row position whose spanning state should be checked.
     * @param level
     *            The hierarchy level of the object to check.
     * @return The row position where the spanning starts or the given row
     *         position if it is not spanned with rows above.
     */
    protected int getStartRowPosition(int rowPosition, int level) {
        int rowPos;
        for (rowPos = rowPosition; rowPos >= 0; rowPos--) {
            if (rowPos <= 0) {
                break;
            }

            // get value of given row
            Object current = getLevelObject(rowPos, level);
            // get value of row before
            Object before = getLevelObject(rowPos - 1, level);

            if (!valuesEqual(current, before)) {
                // the both values are not equal, therefore return the given row
                break;
            }
        }
        return rowPos;
    }

    /**
     * Calculates the number of rows to span regarding the data of the cells.
     *
     * @param rowPosition
     *            The row position to start the check for spanning.
     * @param level
     *            The hierarchy level to check.
     * @return The number of rows to span.
     */
    protected int getRowSpan(int rowPosition, int level) {
        int span = 1;

        while (rowPosition < getRowCount() - 1
                && valuesEqual(
                        getLevelObject(rowPosition, level),
                        getLevelObject(rowPosition + 1, level))) {
            span++;
            rowPosition++;
        }
        return span;
    }

    /**
     * Returns the level object for the specified level out of the
     * {@link HierarchicalWrapper} object at the given row position.
     *
     * @param rowPosition
     *            The row position of the object for which the level object is
     *            requested.
     * @param level
     *            The hierarchy level to get the object for.
     * @return The level object for the specified level out of the
     *         {@link HierarchicalWrapper} object at the given row position.
     */
    protected Object getLevelObject(int rowPosition, int level) {
        HierarchicalWrapper hierarchical = this.underlyingDataProvider.getRowObject(rowPosition);
        return hierarchical.getObject(level);
    }

    /**
     * Check if the given values are equal, where two <code>null</code> values
     * are not considered to be equal.
     *
     * @param value1
     *            The first value to check for equality with the second value
     * @param value2
     *            The second value to check for equality with the first value.
     * @return <code>true</code> if the given values are equal.
     */
    protected boolean valuesEqual(Object value1, Object value2) {
        return (value1 != null) && (value2 != null) && (value1 == value2);
    }
}