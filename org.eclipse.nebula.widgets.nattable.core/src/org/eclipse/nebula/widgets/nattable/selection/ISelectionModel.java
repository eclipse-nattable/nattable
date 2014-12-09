/*******************************************************************************
 * Copyright (c) 2012, 2013, 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 446275 ISelectionModel extends ILayerEventHandler
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import java.util.List;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEventHandler;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Tracks the selections made in the table.
 */
public interface ISelectionModel extends ILayerEventHandler<IStructuralChangeEvent> {

    /**
     * Determines whether multiple cells can be selected simultaneously
     *
     * @return whether multiple cells can be selected simultaneously
     */
    public boolean isMultipleSelectionAllowed();

    /**
     * Sets whether multiple cells can be selected simultaneously
     *
     * @param multipleSelectionAllowed
     *            whether multiple cells can be selected simultaneously
     */
    public void setMultipleSelectionAllowed(boolean multipleSelectionAllowed);

    /**
     * Selects a specified cell
     *
     * @param columnPosition
     *            column position of the cell to select
     * @param rowPosition
     *            row position of the cell to select
     */
    public void addSelection(int columnPosition, int rowPosition);

    /**
     * Selects the cells of a specified area
     *
     * @param range
     *            the position based area to select
     */
    public void addSelection(final Rectangle range);

    /**
     * Removes all cell selections
     */
    public void clearSelection();

    /**
     * Deselects a specified cell
     *
     * @param columnPosition
     *            column position of the cell to deselect
     * @param rowPosition
     *            row position of the cell to deselect
     */
    public void clearSelection(int columnPosition, int rowPosition);

    /**
     * Removes the selection of specified cells
     *
     * @param removedSelection
     *            the position based area to deselect
     */
    public void clearSelection(Rectangle removedSelection);

    /**
     * Determines whether there are any selected cells
     *
     * @return whether there are any selected cells
     */
    public boolean isEmpty();

    /**
     * Retrieves the cells that are selected
     *
     * @return the cells that are selected, expressed in position coordinates
     */
    public List<Rectangle> getSelections();

    // Cell features

    /**
     * Determines whether a specified cell is selected
     *
     * @param columnPosition
     *            column position of the cell to inspect
     * @param rowPosition
     *            row position of the cell to inspect
     * @return whether the specified cell is selected
     */
    public boolean isCellPositionSelected(int columnPosition, int rowPosition);

    // Column features

    /**
     * Retrieves the columns that have any selected cells
     *
     * @return the column positions that have any selected cells
     */
    public int[] getSelectedColumnPositions();

    /**
     * Determines whether a specified column contains any selected cell
     *
     * @param columnPosition
     *            column position to inspect
     * @return whether the specified column contains any selected cell
     */
    public boolean isColumnPositionSelected(int columnPosition);

    /**
     * @param columnHeight
     *            the number of rows in a fully selected column
     */
    public int[] getFullySelectedColumnPositions(int columnHeight);

    /**
     * @param columnHeight
     *            the number of rows in a fully selected column
     */
    public boolean isColumnPositionFullySelected(int columnPosition,
            int columnHeight);

    // Row features

    /**
     * Retrieves the number of rows that have any selected cell
     *
     * @return the number of rows that have any selected cell
     */
    public int getSelectedRowCount();

    /**
     * Retrieves the rows with a valid row position that have any selected cells
     *
     * @return the row positions with a valid row position that have any
     *         selected cells
     */
    public Set<Range> getSelectedRowPositions();

    /**
     * Determines whether a specified row contains any selected cell
     *
     * @param rowPosition
     *            row position to inspect
     * @return whether the specified row contains any selected cell
     */
    public boolean isRowPositionSelected(int rowPosition);

    /**
     * @param rowWidth
     *            the number of columns in a fully selected row
     */
    public int[] getFullySelectedRowPositions(int rowWidth);

    /**
     * Check if all cells in a row are selected, which means the row is fully
     * selected.
     *
     * @param rowPosition
     *            The row position that should be checked.
     * @param rowWidth
     *            The number of columns in the row which is needed to determine
     *            if the all cells in a row are selected.
     * @return <code>true</code> if all cells in a row are selected,
     *         <code>false</code> if not
     */
    public boolean isRowPositionFullySelected(int rowPosition, int rowWidth);
}
