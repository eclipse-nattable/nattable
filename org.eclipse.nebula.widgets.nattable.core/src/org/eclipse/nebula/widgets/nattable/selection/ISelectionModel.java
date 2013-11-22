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

package org.eclipse.nebula.widgets.nattable.selection;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.RangeList;
import org.eclipse.nebula.widgets.nattable.coordinate.Rectangle;


/**
 * Tracks the selections made in the table.
 * <p>
 * A column or row is <i>selected</i>, if any cells in that column/row are selected.
 * A column or row is <i>fully selected</i>, if all cells in that column/row are selected.
 * A column or row is not selected and not fully selected, if the selection layer is empty
 * (any dim has size 0).</p>
 */
public interface ISelectionModel {
	
	
	boolean isMultipleSelectionAllowed();
	
	void setMultipleSelectionAllowed(boolean multipleSelectionAllowed);
	
	
	/**
	 * Adds the specified position to the selection.
	 * 
	 * @param columnPosition the column position in the selection layer
	 * @param rowPosition the row position in the selection layer
	 */
	void addSelection(int columnPosition, int rowPosition);
	
	/**
	 * Adds the specified position rectangle to the selection.
	 * 
	 * @param rect the position rectangle in the selection layer
	 */
	void addSelection(Rectangle positions);
	
	/**
	 * Clears the selection completely.
	 */
	void clearSelection();
	
	/**
	 * Removes the specified position from the selection.
	 * 
	 * @param columnPosition the column position in the selection layer
	 * @param rowPosition the row position in the selection layer
	 */
	void clearSelection(int columnPosition, int rowPosition);
	
	/**
	 * Removes the specified position rectangle from the selection.
	 * 
	 * @param rect the position rectangle in the selection layer
	 */
	void clearSelection(Rectangle positions);
	
	/**
	 * Returns if the selection is empty.
	 * 
	 * @return <code>true</code> if the selection is empty, otherwise <code>false</code>
	 */
	boolean isEmpty();
	
	/**
	 * Returns a collection of rectangles of selected positions. If a position is inside a
	 * rectangle of the list, it is selected, otherwise it is not selected.
	 * 
	 * @return a list of position rectangles
	 */
	List<Rectangle> getSelections();
	
	//-- Cell features --//
	
	/**
	 * Returns if the specified position is selected.
	 * 
	 * @param columnPosition the column position in the selection layer
	 * @param rowPosition the row position in the selection layer
	 * @return <code>true</code> if the position is selected, otherwise <code>false</code>
	 */
	boolean isCellPositionSelected(int columnPosition, int rowPosition);
	
	//-- Column features --//
	
	/**
	 * Returns the positions of all selected columns. 
	 * 
	 * @return the positions of selected columns as ordered and disjoint list of position ranges
	 */
	RangeList getSelectedColumnPositions();
	
	/**
	 * Returns if the specified column position is selected. A column is selected, if any cell in 
	 * the column is selected.
	 * 
	 * @param columnPosition the column position in the selection layer
	 * @return <code>true</code> if the column is selected, otherwise <code>false</code>
	 */
	boolean isColumnPositionSelected(int columnPosition);
	
	/**
	 * Returns the positions of all fully selected columns. 
	 * 
	 * @return the positions of fully selected columns as ordered and disjoint list of position ranges
	 */
	RangeList getFullySelectedColumnPositions();
	
	/**
	 * Returns if the specified column position is fully selected.
	 * 
	 * @param columnPosition the column position in the selection layer
	 * @return <code>true</code> if the column is fully selected, otherwise <code>false</code>
	 */
	boolean isColumnPositionFullySelected(int columnPosition);
	
	//-- Row features --//
	
	int getSelectedRowCount();
	
	/**
	 * Returns the positions of all selected rows. 
	 * 
	 * @return the positions of selected rows as ordered and disjoint list of position ranges
	 */
	RangeList getSelectedRowPositions();
	
	/**
	 * Returns if the specified row position is selected.
	 * 
	 * @param rowPosition the row position in the selection layer
	 * @return <code>true</code> if the row is selected, otherwise <code>false</code>
	 */
	boolean isRowPositionSelected(int rowPosition);
	
	/**
	 * Returns the positions of all fully selected rows. 
	 * 
	 * @return the positions of fully selected rows as ordered and disjoint list of position ranges
	 */
	RangeList getFullySelectedRowPositions();
	
	/**
	 * Returns if the specified row position is fully selected.
	 * 
	 * @param rowPosition the row position in the selection layer
	 * @return <code>true</code> if the row is fully selected, otherwise <code>false</code>
	 */
	boolean isRowPositionFullySelected(int rowPosition);
	
}
