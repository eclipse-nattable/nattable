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
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.formula;

/**
 * Implementations of this interface are intended to report errors on evaluating
 * formulas via {@link FormulaDataProvider}.
 *
 * @see FormulaDataProvider#setErrorReporter(FormulaErrorReporter)
 *
 * @since 1.4
 */
public interface FormulaErrorReporter {

    /**
     * Registers the error message for the given cell coordinates to this
     * {@link FormulaErrorReporter}.
     * 
     * @param column
     *            The column index of the cell where the error happened.
     * @param row
     *            The row index of the cell where the error happened.
     * @param message
     *            The error message.
     */
    void addFormulaError(int column, int row, String message);

    /**
     * Removes the error message for the given cell coordinates.
     * 
     * @param column
     *            The column index of the cell for which an error message is
     *            registered.
     * @param row
     *            The row index of the cell for which an error message is
     *            registered.
     */
    void clearFormulaError(int column, int row);

    /**
     * Checks if an error message is registered for the given cell coordinates.
     * 
     * @param column
     *            The column index of the cell for which a check should be
     *            performed.
     * @param row
     *            The row index of the cell for which a check should be
     *            performed.
     * @return <code>true</code> if an error message is registered for the given
     *         cell coordinates, <code>false</code> if not.
     */
    boolean hasFormulaError(int column, int row);

    /**
     * Returns the error message that is registered for the given cell
     * coordinates or <code>null</code> if no error message is registered for
     * that cell coordinates.
     * 
     * @param column
     *            The column index of the cell for which the error message is
     *            requested.
     * @param row
     *            The row index of the cell for which the error message is
     *            requested.
     * @return The error message that is registered for the given cell
     *         coordinates or <code>null</code> if no error message is
     *         registered for that cell coordinates.
     */
    String getFormulaError(int column, int row);
}
